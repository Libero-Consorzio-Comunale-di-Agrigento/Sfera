package it.finmatica.atti.integrazioni.protocollo

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.AbstractProtocolloEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.IProtocolloEsterno
import it.finmatica.atti.IProtocolloEsterno.Classifica
import it.finmatica.atti.IProtocolloEsterno.Documento
import it.finmatica.atti.IProtocolloEsterno.Fascicolo
import it.finmatica.atti.commons.TokenIntegrazione
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.documenti.*
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.docer.DocErConfig
import it.finmatica.docer.atti.anagrafiche.*
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import javax.sql.DataSource
import java.sql.Connection
import java.text.SimpleDateFormat

@Component("protocolloEsternoDocer")
@Lazy
class ProtocolloDocEr extends AbstractProtocolloEsterno {

    private static final Logger log = Logger.getLogger(ProtocolloDocEr.class)

    @Autowired SpringSecurityService springSecurityService
    @Autowired @Qualifier("dataSource_gdm") DataSource dataSource_gdm
    @Autowired TokenIntegrazioneService tokenIntegrazioneService
    @Autowired IProtocolloEsterno protocolloEsterno
    @Autowired IGestoreFile gestoreFile
    @Autowired DocErConfig docErConfig

    @Override
    void sincronizzaClassificazioniEFascicoli() {}

    @Override
    @Transactional
    void fascicola(IFascicolabile atto) {
        log.debug("Invocazione di Fascicolazione sul sistema Doc/Er")
        // ottengo il lock pessimistico per evitare doppie fascicolazioni.
        atto.lock();

        // controllo se c'è un token di fascicolazione precedente dovuto ad un errore dopo la fascicolazione:
        TokenIntegrazione token = tokenIntegrazioneService.getToken("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
        if (token != null && token.isStatoSuccesso()) {
            // significa che ho già fascicolato: prendo il codice classifica fascicolo anno e numero, lo assegno al documento ed esco:
            def map = Eval.me(token.dati)
            atto.classificaCodice = map.classificaCodice
            atto.fascicoloAnno = map.fascicoloAnno
            atto.fascicoloNumero = map.fascicoloNumero
            atto.save()

            // elimino il token: tutto è andato bene e verrà eliminato solo alla commit sull transaction principale
            tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
            return;
        }

        log.debug("Eseguo la Fascicolazione")

        // creo il token di fascicolazione: se lo trovo ed ha successo, vuol dire che ho già fascicolato:
        token = tokenIntegrazioneService.beginTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
        if (token.isStatoSuccesso()) {
            // significa che ho già fascicolato: prendo il codice e descrizione della classifica, lo assegno al documento ed esco:
            def map = Eval.me(token.dati)
            atto.classificaCodice = map.classificaCodice
            atto.fascicoloAnno = map.fascicoloAnno
            atto.fascicoloNumero = map.fascicoloNumero
            atto.save()

            // elimino il token: tutto è andato bene e verrà eliminato solo alla commit sull transaction principale
            tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
            return;
        }

        log.debug("Invocazione del WS per la fascicolazione su Doc/Er")

        try {
            Connection conn = dataSource_gdm.connection;
            GestioneAnagrafiche g = new GestioneAnagrafiche(conn,
                    docErConfig.getUtenteWebservice(),
                    docErConfig.getUrlWsdlAutenticazione(),
                    docErConfig.getUrlWsdl(),
                    docErConfig.getUrlWsdlFascicolazione(),
                    docErConfig.getUrlWsdlProtocollazione())

            String cod_ente = docErConfig.getCodiceEnte()
            String cod_aoo = docErConfig.getCodiceAoo()
            String codice_classifica = atto.classificaCodice
            String anno_fascicolo = atto.fascicoloAnno
            String progr_fascicolo = atto.fascicoloNumero

            // XML per la Fascicolazione
            DatiFascicolazione df = new DatiFascicolazione();
            df.setAnno_fascicolo(anno_fascicolo);
            df.setCod_aoo(cod_aoo);
            df.setCod_ente(cod_ente);
            df.setCodice_classifica(codice_classifica);
            df.setId_documento(atto.idDocumentoEsterno.toString());
            df.setProgr_fascicolo(progr_fascicolo);
            df.setUri_doc("");

            String xmlResult = g.fascicolaById(atto.idDocumentoEsterno.toString(), df.getInputXMLFascicolazione())
            def xml = new XmlSlurper().parseText(xmlResult)
            String esito = xml.codice.text()
            if (!esito.equalsIgnoreCase("0")) {
                String errore = xml.descrizione.text()
                throw new Exception("Esito di fascicolazione: ${errore}.");
            }

            log.debug("fascicolazione: Classifica:${atto.classificaCodice}, Fascicolo Anno:${atto.fascicoloAnno}, Fascicolo Progressivo:${atto.fascicoloNumero}")

        } catch (Exception e) {
            log.error("Errore nella chiamata alla fascicolazione webservice su Doc/Er: ${e.getMessage()}", e)
            // elimino il token e interrompo la transazione del token (così si possono fare più tentativi)
            tokenIntegrazioneService.stopTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
            throw new AttiRuntimeException("Errore in fase di fascicolazione su Doc/Er: ${e.getMessage()}.", e)
        }

        log.info("Fascicolazione Webservice su Doc/Er effettuata sul documento ${atto.id}")

        // la prima cosa che faccio dopo la fascicolazione è salvare il record su db:
        tokenIntegrazioneService.setTokenSuccess("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO, "[Classifica: ${atto.classificaCodice}, Fascicolo Anno: ${atto.fascicoloAnno}, Fascicolo Progressivo: ${atto.fascicoloNumero}]")

        // elimino il token: questo avverrà solo se la transazione "normale" di grails andrà a buon fine:
        tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
    }

    @Override
    @Transactional
    void protocolla(IProtocollabile atto) {
        log.debug("Invocazione di Protocollazione sul sistema Doc/Er")
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd")
        // ottengo il lock pessimistico per evitare doppie protocollazioni.
        atto.lock();

        // controllo che il documento non sia già protocollato
        if (atto.numeroProtocollo > 0) {
            throw new AttiRuntimeException("Il documento è già protocollato con numero: ${atto.numeroProtocollo} / ${atto.annoProtocollo}!")
        }

        // controllo se c'è un token di protocollazione precedente dovuto ad un errore dopo la protocollazione:
        TokenIntegrazione token = tokenIntegrazioneService.getToken("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
        if (token != null && token.isStatoSuccesso()) {
            // significa che ho già protocollato: prendo il numero di protocollo, lo assegno al documento ed esco:
            def map = Eval.me(token.dati);
            atto.numeroProtocollo = map.numero;
            atto.annoProtocollo = map.anno;
            atto.dataNumeroProtocollo = sdf.parse(map.data);
            atto.save()

            // elimino il token: tutto è andato bene e verrà eliminato solo alla commit sull transaction principale
            tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
            return;
        }

        log.debug("Eseguo la Protocollazione")

        // creo il token di protocollazione: se lo trovo ed ha successo, vuol dire che ho già protocollato:
        token = tokenIntegrazioneService.beginTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO);
        if (token.isStatoSuccesso()) {
            // significa che ho già protocollato: prendo il numero di protocollo, lo assegno al documento ed esco:
            def map = Eval.me(token.dati);
            atto.numeroProtocollo = map.numero;
            atto.annoProtocollo = map.anno;
            atto.dataNumeroProtocollo = sdf.parse(map.data);
            atto.save()

            // elimino il token: tutto è andato bene e verrà eliminato solo alla commit sull transaction principale
            tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
            return;
        }

        log.debug("Invocazione del WS per la protocollazione su Doc/Er")

        String numeroProtocollo, annoProtocollo, dataNumeroProtocollo

        try {
            Connection conn = dataSource_gdm.connection;
            GestioneAnagrafiche g = new GestioneAnagrafiche(conn,
                    docErConfig.getUtenteWebservice(),
                    docErConfig.getUrlWsdlAutenticazione(),
                    docErConfig.getUrlWsdl(),
                    docErConfig.getUrlWsdlFascicolazione(),
                    docErConfig.getUrlWsdlProtocollazione())
            // Oggetto
            String oggettoDoc = atto.oggetto
            // Tipo Richiesta Interna
            String tipo_richiesta = "I"
            // Firma
            String stato_firma = getTipoFirma(atto)
            // Persona
            String id_persona, nome_persona, cognome_persona, mail_persona, indirizzo_persona
            As4SoggettoCorrente soggetto = getSoggettoFirmatario(atto)

            if (soggetto == null && stato_firma.equals("F")) {
                throw new AttiRuntimeException("Errore in fase di protocollazione - Soggetto Firmatario nullo o non valido")
            }

            if (soggetto != null) {
                id_persona = soggetto.id
                nome_persona = soggetto.nome
                cognome_persona = soggetto.cognome
                mail_persona = soggetto.indirizzoWeb
                indirizzo_persona = springSecurityService.principal.amministrazione.soggetto.indirizzoResidenza
                if (indirizzo_persona == null)
                    throw new AttiRuntimeException("Errore in fase di protocollazione - Indirizzo Postale nullo o non valido")
                else
                    indirizzo_persona = indirizzo_persona + " " + (springSecurityService.principal.amministrazione.soggetto.comuneResidenza ?: "") + " " + (springSecurityService.principal.amministrazione.soggetto.provinciaResidenza ?: "")
            }

            //Amministrazione
            String denominzazione_amm = atto.ente.soggetto.cognome + (atto.ente.soggetto.nome ?: "")
            String codice_amm = atto.ente.codice
            //Unità organizzativa
            String denominzazione_uo = atto.getUnitaProponente().codice
            String identificativo_uo = atto.getUnitaProponente().descrizione
            //AOO
            String denominazione_aoo = docErConfig.getDenominazioneAoo()
            String codice_aoo = docErConfig.getCodiceAoo()
            //Fascicolo Primario
            String codice_amm_fasc, codice_aoo_fasc, classifica_fasc, anno_fasc, progressivo_fasc
            if (atto.classificaCodice != null) {
                codice_amm_fasc = docErConfig.getCodiceEnte()
                codice_aoo_fasc = docErConfig.getCodiceAoo()
                classifica_fasc = atto.classificaCodice
                anno_fasc = atto.fascicoloAnno
                progressivo_fasc = atto.fascicoloNumero
            }

            // XML per la protocolazione
            DatiProtocollazione dp = new DatiProtocollazione()
            dp.setAnno_fasc(anno_fasc)
            dp.setClassifica_fasc(classifica_fasc)
            dp.setCodice_amm(codice_amm)
            dp.setCodice_amm_fasc(codice_amm_fasc)
            dp.setCodice_aoo(codice_aoo)
            dp.setCodice_aoo_fasc(codice_aoo_fasc)
            dp.setCognome_persona(cognome_persona)
            dp.setDenominazione_aoo(denominazione_aoo)
            dp.setDenominzazione_amm(denominzazione_amm)
            dp.setDenominzazione_uo(denominzazione_uo)
            dp.setId_persona(id_persona)
            dp.setIdentificativo_uo(identificativo_uo)
            dp.setIndirizzo_persona(indirizzo_persona)
            dp.setMail_persona(mail_persona)
            dp.setNome_persona(nome_persona)
            dp.setOggettoDoc(oggettoDoc)
            dp.setProgressivo_fasc(progressivo_fasc)
            dp.setStato_firma(stato_firma)
            dp.setTipo_richiesta(tipo_richiesta)

            // Protocollazione
            String xmlResult = g.protocollaById(atto.idDocumentoEsterno.toString(), dp.getInputXMLProtocollazione())
            def xml = new XmlSlurper().parseText(xmlResult)
            String esito = xml.codice.text()
            if (!esito.equalsIgnoreCase("0")) {
                String errore = xml.descrizione.text()
                throw new Exception("Errore in fase di protocollazione via webservice su Doc/Er: ${errore}.");
            }

            numeroProtocollo = xml.dati_protocollo.NUM_PG.text()
            annoProtocollo = xml.dati_protocollo.ANNO_PG.text()
            String dt = xml.dati_protocollo.DATA_PG.text()
            dataNumeroProtocollo = dt.substring(0, dt.indexOf("T"))

            log.debug("protocollazione: ${numeroProtocollo}/${annoProtocollo} e data di protocollazione ${dataNumeroProtocollo}")
        } catch (Exception e) {
            log.error("Errore nella chiamata alla protocollazione webservice su Doc/Er: ${e.getMessage()}", e)
            // elimino il token e interrompo la transazione del token (così si possono fare più tentativi)
            tokenIntegrazioneService.stopTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
            throw new AttiRuntimeException("Errore in fase di protocollazione su Doc/Er: ${e.getMessage()}.", e)
        }

        log.info("Protocollazione Webservice su Doc/Er effettuata sul documento ${atto.id}: ${numeroProtocollo}/${annoProtocollo} in data ${dataNumeroProtocollo}")

        // la prima cosa che faccio dopo la protocollazione è salvare il record su db:
        tokenIntegrazioneService.setTokenSuccess("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO, "[numero:${numeroProtocollo}, anno:${annoProtocollo}, data:'${dataNumeroProtocollo}']")

        atto.numeroProtocollo = (numeroProtocollo) ? Integer.parseInt(numeroProtocollo) : null
        atto.annoProtocollo = (annoProtocollo) ? Integer.parseInt(annoProtocollo) : null
        atto.dataNumeroProtocollo = (dataNumeroProtocollo) ? sdf.parse(dataNumeroProtocollo) : null

        atto.save()

        // elimino il token: questo avverrà solo se la transazione "normale" di grails andrà a buon fine:
        tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
    }

    @Override
    @Transactional(readOnly = true)
    List<Classifica> getListaClassificazioni(String filtro, String codiceUoProponente) {
        log.debug("Doc-Er - Ricerca Titolario - getListaClassificazioni( filtro:${filtro} )")

//		if (filtro.equals(""))
//		 return []

        GestioneAnagrafiche g = new GestioneAnagrafiche(docErConfig.getUtenteWebservice(),
                docErConfig.getUrlWsdlAutenticazione(),
                docErConfig.getUrlWsdl(),
                docErConfig.getUrlWsdlFascicolazione(),
                docErConfig.getUrlWsdlProtocollazione());

        //Ricerca classificazione va per CODICE
        String result = g.ricercaTitolario(filtro, "", docErConfig.getCodiceEnte(), docErConfig.getCodiceAoo())

        def xml = new XmlSlurper().parseText(result)

        def listaClassificazioni = xml.ROW.collect {
            new Classifica(codice: it.CLASSIFICA.toString(), descrizione: it.DES_TITOLARIO.toString(), dal: null)
        }

        log.debug("Doc-Er - Ricerca Titolario - Risulato della ricerca XML:" + result)

        listaClassificazioni = listaClassificazioni.sort { it.codice }
        listaClassificazioni = listaClassificazioni.sort { it.descrizione }

        return listaClassificazioni;
    }

    @Override
    @Transactional(readOnly = true)
    List<Fascicolo> getListaFascicoli(String filtro, String codiceClassifica, Date classificaDal, String codiceUoProponente) {

        log.debug("Doc-Er - Ricerca Fascicoli - getListaFascicoli( filtro:${filtro}, codiceClassifica:${codiceClassifica}")

//		if(codiceClassifica==null || (codiceClassifica!=null && codiceClassifica.equals("")))
//			return []

        GestioneAnagrafiche g = new GestioneAnagrafiche(docErConfig.getUtenteWebservice(),
                docErConfig.getUrlWsdlAutenticazione(),
                docErConfig.getUrlWsdl(),
                docErConfig.getUrlWsdlFascicolazione(),
                docErConfig.getUrlWsdlProtocollazione());

        String result = g.ricercaFascicolo("", "", filtro, codiceClassifica, docErConfig.getCodiceEnte(), docErConfig.getCodiceAoo())

        def xml = new XmlSlurper().parseText(result)

        def listaFascicoli = xml.ROW.collect {
            new Fascicolo(classifica: new Classifica(codice: it.CLASSIFICA.toString(), descrizione: "", dal: null)
                    , anno: Integer.parseInt(it.ANNO_FASCICOLO.toString())
                    , numero: it.PROGR_FASCICOLO.toString()
                    , oggetto: it.DES_FASCICOLO.toString())
        }

        log.debug("Doc-Er - Ricerca Titolario - Risulato della ricerca XML:" + result)

        listaFascicoli = listaFascicoli.sort { it.anno }
        listaFascicoli = listaFascicoli.sort { it.numero }
        listaFascicoli = listaFascicoli.sort { it.oggetto }

        return listaFascicoli
    }


    @Override
    @Transactional
    void creaFascicolo(String numero, String anno, String descrizione, String parent_progressivo, String classifica) {
        log.debug("Doc-Er - Creazione Fascicolo sul sistema Doc/Er")

        try {
            Connection conn = dataSource_gdm.connection;
            GestioneAnagrafiche g = new GestioneAnagrafiche(conn,
                    docErConfig.getUtenteWebservice(),
                    docErConfig.getUrlWsdlAutenticazione(),
                    docErConfig.getUrlWsdl(),
                    docErConfig.getUrlWsdlFascicolazione(),
                    docErConfig.getUrlWsdlProtocollazione())

            String result = g.creaFascicolo(springSecurityService.currentUser.nominativo,
                    numero,
                    anno,
                    parent_progressivo,
                    classifica,
                    docErConfig.getCodiceAoo(),
                    docErConfig.getCodiceEnte(),
                    descrizione)

            log.debug("Doc-Er - Creazione Fascicolo - Risulato: ${result}")

        } catch (Exception e) {
            log.error("Errore nella chiamata alla crea fascicolo webservice su Doc/Er: ${e.getMessage()}", e)
            throw new AttiRuntimeException("Errore in fase di creazione di un fascicolo su Doc/Er.", e)
        }
    }

    @Override
    @Transactional(readOnly = true)
    List<Documento> getListaDocumenti(DatiRicercaDocumento datiRicerca) {

        log.debug("Doc-Er - Ricerca Documenti - ricercaDocumenti()")

        GestioneDocumenti g = new GestioneDocumenti(docErConfig.getUtenteWebservice(),
                docErConfig.getUrlWsdlAutenticazione(),
                docErConfig.getUrlWsdl())

        String result = g.ricercaDocumenti(datiRicerca)

        def xml = new XmlSlurper().parseText(result)

        def listaDocumenti = xml.ROW.collect {
            new Documento(docNum: it.DOCNUM.text()
                    , typeId: it.TYPE_ID.text()
                    , statoArchivistico: it.STATO_ARCHIVISTICO.text()
                    , docName: it.DOCNAME.text())
        }

        log.debug("Doc-Er - Ricerca Documenti - Risulato della ricerca XML:${result}")

        return listaDocumenti
    }


    @Override
    @Transactional(readOnly = true)
    InputStream downloadFile(String docNum) {

        log.debug("Doc-Er - Download File Documento - downloadFile( docNum:${docNum} )")

        GestioneDocumenti g = new GestioneDocumenti(docErConfig.getUtenteWebservice(),
                docErConfig.getUrlWsdlAutenticazione(),
                docErConfig.getUrlWsdl())


        return g.downloadFile(docNum)
    }

    private As4SoggettoCorrente getSoggettoFirmatario(IAtto atto) {
        Firmatario firmatario
        As4SoggettoCorrente soggetto = null

        def f = atto?.firmatari?.findAll { it.firmato }?.sort { it.sequenza }
        if (f.size() > 0) {
            firmatario = f.first()
            soggetto = As4SoggettoCorrente.findByUtenteAd4(firmatario.firmatario)
        } else
            soggetto = As4SoggettoCorrente.findByUtenteAd4(atto.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4)

        log.debug "Calcolo del soggetto primo firmatario della sequenza per il documento ${atto.id}"

        return soggetto
    }

    private String getTipoFirma(IAtto atto) {
        String firma = "NF"

        switch (atto.statoFirma) {
            case null:
            case "":
            case StatoFirma.DA_NON_FIRMARE:
                firma = "NF"
                break

            case StatoFirma.FIRMATO:
            case StatoFirma.FIRMATO_DA_SBLOCCARE:
                firma = "FD"
                break

            case StatoFirma.IN_FIRMA:
            case StatoFirma.DA_FIRMARE:
                firma = "F"
                break
        }
        log.debug "Calcolo del tipo di firma ${firma}"
        return firma
    }
}
