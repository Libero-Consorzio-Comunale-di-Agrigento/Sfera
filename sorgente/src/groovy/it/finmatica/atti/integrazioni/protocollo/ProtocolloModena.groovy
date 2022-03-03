package it.finmatica.atti.integrazioni.protocollo

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.AbstractProtocolloEsterno
import it.finmatica.atti.IDocumentaleEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.IProtocolloEsterno.Classifica
import it.finmatica.atti.IProtocolloEsterno.Fascicolo
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.commons.TokenIntegrazione
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.documenti.*
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.integrazioniws.comunemodena.fascicolo.FascicoloOut
import it.finmatica.atti.integrazioniws.comunemodena.fascicolo.WSFascicoloSoap
import it.finmatica.atti.integrazioniws.comunemodena.protocollo.*
import it.finmatica.atti.integrazioniws.comunemodena.titolario.Classification
import it.finmatica.atti.integrazioniws.comunemodena.titolario.Dossier
import it.finmatica.atti.integrazioniws.comunemodena.titolario.ProtocolForADS
import it.finmatica.atti.integrazioniws.comunemodena.titolario.ProtocolForADSPortType
import org.apache.commons.io.IOUtils
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import java.text.SimpleDateFormat

/**
 * Integrazione del protocollo con Modena.
 */
@Component("protocolloComuneModena")
@Lazy
class ProtocolloModena extends AbstractProtocolloEsterno {

    private static final Logger log = Logger.getLogger(ProtocolloModena.class)

    @Autowired ProtocolloModenaConfig protocolloModenaConfig
    @Autowired IDocumentaleEsterno gestoreDocumentaleEsterno
    @Autowired TokenIntegrazioneService tokenIntegrazioneService
    @Autowired SpringSecurityService springSecurityService
    @Autowired IGestoreFile gestoreFile

    @Autowired
    @Qualifier("protocolloComuneModenaServiceClient")
    ProtocolloSoap protocolloComuneModenaServiceClient

    @Autowired WSFascicoloSoap fascicoloComuneModenaServiceClient
    @Autowired ProtocolForADSPortType titolarioComuneModenaServiceClient

    String getUtenteWsProtocollo() {
        return protocolloModenaConfig.getUtenteWebService()
    }

    @Override
    @Transactional
    void protocolla(IProtocollabile atto) {
        // ottengo il lock pessimistico per evitare doppie protocollazioni.
        atto.lock()
        if (atto.numeroProtocollo > 0) {
            throw new AttiRuntimeException("Il documento è già protocollato!");
        }

        // creo il token di protocollazione: se lo trovo ed ha successo, vuol dire che ho già protocollato:
        TokenIntegrazione token = tokenIntegrazioneService.beginTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO);
        if (token.isStatoSuccesso()) {
            // significa che ho già protocollato: prendo il numero di protocollo, lo assegno al documento ed esco:
            def map = Eval.me(token.dati);
            atto.numeroProtocollo = map.numero;
            atto.annoProtocollo = map.anno;
            atto.dataNumeroProtocollo = Date.parse("dd/MM/yyyy", map.data);
            atto.save()

            // allineo il documento gdm:
            gestoreDocumentaleEsterno.salvaDocumento(atto);

            fascicola(atto);

            // elimino il token: tutto è andato bene e verrà eliminato solo alla commit sull transaction principale
            tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
            return;
        }

        try {
            ProtocolloIn protIn = new ProtocolloIn();

            protIn.setData(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
            protIn.setClassifica(atto.getClassificaCodice());
            protIn.setTipoDocumento(atto.tipologiaDocumento.codiceEsterno);
            protIn.setOggetto(atto.oggetto);
            protIn.setOrigine("I");
            protIn.setAggiornaAnagrafiche("N");    // S o N.

            String codiceUoProponente = atto.getUnitaProponente()?.codice;
            if (codiceUoProponente == null) {
                throw new AttiRuntimeException("Non è possibile protocollare: l'unità proponente non ha un CODICE associato (su SO4).")
            }

            String unitaProtocollazione = protocolloModenaConfig.getCodiceUnitaProponente(codiceUoProponente)
            if (unitaProtocollazione == null) {
                throw new AttiRuntimeException("Non è possibile protocollare: non ho trovato il codice dell'unità corrispondente all'unità proponente con codice: ${codiceUoProponente}");
            }

            protIn.setMittenteInterno(unitaProtocollazione);
            protIn.setInCaricoA(unitaProtocollazione);

            protIn.setUtente(utenteWsProtocollo);
            protIn.setRuolo(unitaProtocollazione);

            ArrayOfAllegatoIn allegatiIn = new ArrayOfAllegatoIn();
            protIn.setAllegati(allegatiIn);

            // Aggiungo l'allegato principale al documento:
            allegatiIn.getAllegato().add(creaAllegato(atto, atto.testo));

            // aggiungo gli allegati del documento al protocollo:
            for (Allegato allegato : atto.allegati) {
                for (FileAllegato file : allegato.fileAllegati) {
                    allegatiIn.getAllegato().add(creaAllegato(allegato, file));
                }
            }

            // Aggiungo gli allegati dei visti:
            def vistiPareriAtto = atto.visti.findAll{it.valido == true}
            def vistiPareriProposta = (atto instanceof Delibera) ? atto.proposta.visti.findAll{it.valido == true} : []
            def vistiPareri = (vistiPareriProposta ?: []) + (vistiPareriAtto ?: [])

            for (def vistoParere : vistiPareriProposta){
                if (vistiPareriAtto.findAll{ it.tipologia.codice == vistoParere.tipologia.codice}.size() > 0){
                    vistiPareri.remove(vistoParere)
                }
            }

            for (VistoParere visto : vistiPareri) {
                if (v.valido) {
                    for (Allegato allegato : v.allegati) {
                        for (FileAllegato file : allegato.fileAllegati) {
                            allegatiIn.getAllegato().add(creaAllegato(allegato, file));
                        }
                    }
                }
            }

            InserisciProtocolloEAnagrafiche request = new InserisciProtocolloEAnagrafiche(protoIn: protIn);

            // invoco il ws di protocollazione
            InserisciProtocolloEAnagraficheResponse response = protocolloComuneModenaServiceClient.inserisciProtocollo(request);

            ProtocolloOut protocollo = response.getInserisciProtocolloEAnagraficheResult();
            if (protocollo.errore?.length() > 0) {
                throw new Exception("Il webservice di protocollazione ha ritornato un errore: ${protocollo.errore}, ${protocollo.messaggio}");
            }

            // la prima cosa che faccio dopo la protocollazione è salvare il record su db:
            tokenIntegrazioneService.setTokenSuccess("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO, "[numero:${protocollo.numeroProtocollo}, anno:${protocollo.annoProtocollo}, data:'${protocollo.dataProtocollo.toGregorianCalendar().getTime().format("dd/MM/yyyy")}', idDocumento:${protocollo.idDocumento}]");

            // poi salvo il n. protocollato sul documento.
            atto.numeroProtocollo = protocollo.numeroProtocollo;
            atto.annoProtocollo = protocollo.annoProtocollo;
            atto.dataNumeroProtocollo = protocollo.dataProtocollo.toGregorianCalendar().getTime();

            atto.save();

            // invoco la fascicolazione
            fascicola(atto, protocollo.idDocumento, unitaProtocollazione);

            // elimino il token: questo avverrà solo se la transazione "normale" di grails andrà a buon fine:
            tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)

        } catch (Exception e) {
            log.error("Errore nella chiamata alla protocollazione: ${e.getMessage()}", e);
            // elimino il token (solo in caso di errore successivo la protocollazione) e interrompo la transazione del token (così si possono fare più tentativi)
            tokenIntegrazioneService.stopTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO);
            throw new AttiRuntimeException("Errore in fase di protocollazione: ${e.getMessage()}.", e);
        }
    }

    private AllegatoIn creaAllegato(def documento, FileAllegato allegato) {
        AllegatoIn allegatoIn = new AllegatoIn();
        allegatoIn.setTipoFile(allegato.getEstensione());
        allegatoIn.setContentType(allegato.contentType);
        allegatoIn.setImage(IOUtils.toByteArray(gestoreFile.getFile(documento, allegato)));
        allegatoIn.setCommento(allegato.nome);
        return allegatoIn;
    }

    @Override
    List<Classifica> getListaClassificazioni(String filtro, String codiceUoProponente) {
        List<Classification> classificazioni = titolarioComuneModenaServiceClient.getClassifications(filtro, null, null);
        def classifiche = [];
        for (Classification c : classificazioni) {
            classifiche << new Classifica(dal: c.startDate?.toGregorianCalendar()?.getTime(), codice: c.id, descrizione: c.description);
        }
        return classifiche;
    }

    @Override
    List<Fascicolo> getListaFascicoli(String filtro, String codiceClassifica, Date classificaDal, String codiceUoProponente) {
        if (codiceUoProponente == null) {
            throw new AttiRuntimeException("Non è possibile cercare i fascicoli: l'unità proponente deve avere il campo 'CODICE' valorizzato.")
        }

        String ruoloRicercaTitolario = protocolloModenaConfig.getCodiceUnitaProponente(codiceUoProponente)
        if (ruoloRicercaTitolario == null) {
            throw new AttiRuntimeException("Non è possibile cercare i fascicoli: non ho trovato il codice del ruolo corrispondente al codice unità: $codiceUoProponente. Controllare la configurazione nella tabella di mapping.");
        }

        if (codiceClassifica?.trim()?.length() == 0) {
            codiceClassifica = null;
        }

        List<Dossier> dossiers = titolarioComuneModenaServiceClient.getDossiers(ruoloRicercaTitolario, filtro, codiceClassifica, null, null, null);
        def fascicoli = [];
        for (Dossier d : dossiers) {
            fascicoli << new Fascicolo(classifica: new Classifica(codice: d.classificationId, descrizione: d.classificationDescr)
                    , anno: d.year
                    , numero: d.number
                    , oggetto: d.subject
                    , sub: d.subNumber);
        }
        return fascicoli;
    }

    @Override
    void fascicola(IFascicolabile fascicolabile) {
        IAtto atto
        if (fascicolabile instanceof IProposta) {
            atto = fascicolabile.atto
        } else {
            atto = (IAtto) fascicolabile
        }

        String codiceUoProponente = atto.getUnitaProponente()?.codice
        if (codiceUoProponente == null) {
            throw new AttiRuntimeException("Non è possibile protocollare: l'unità proponente non ha un CODICE associato (su SO4).")
        }

        String unitaProtocollazione = protocolloModenaConfig.getCodiceUnitaProponente(codiceUoProponente)
        if (unitaProtocollazione == null) {
            throw new AttiRuntimeException("Non è possibile protocollare: non ho trovato il codice dell'unità corrispondente all'unità proponente con codice: ${codiceUoProponente}");
        }

        DocumentoOut protocollo = protocolloComuneModenaServiceClient.leggiProtocollo((short) atto.annoProtocollo, (int) atto.numeroProtocollo, utenteWsProtocollo, unitaProtocollazione);

        fascicola(atto, protocollo.idDocumento, unitaProtocollazione);
    }

    void fascicola(IAtto atto, int idDocumento, String unitaProtocollazione) {
        // se non ho il fascicolo del documento in cui fascicolare, esco e non faccio niente.
        if (!(atto.fascicoloAnno > 0 && atto.fascicoloNumero?.length() > 0)) {
            return;
        }

        // ottengo l'id del fascicolo:
        FascicoloOut fascicolo = fascicoloComuneModenaServiceClient.leggiFascicolo("", atto.fascicoloAnno.toString(), "${atto.classificaCodice}/${atto.fascicoloNumero}", utenteWsProtocollo, unitaProtocollazione, "", "")

        it.finmatica.atti.integrazioniws.comunemodena.fascicolo.EsitoOperazione esito = fascicoloComuneModenaServiceClient.fascicolaDocumento(fascicolo.id, idDocumento, "N", utenteWsProtocollo, unitaProtocollazione, "", "");
        if (!esito.isEsito()) {
            throw new AttiRuntimeException("Il webservice di fascicolazione ha ritornato un errore per il documento ${atto.estremiAtto}: " + esito.getErrore());
        }
    }
}
