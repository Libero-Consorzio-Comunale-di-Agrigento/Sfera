package it.finmatica.atti.integrazioni.protocollo

import grails.plugin.springsecurity.SpringSecurityService
import groovy.xml.StreamingMarkupBuilder
import it.finmatica.atti.AbstractProtocolloEsterno
import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.commons.TokenIntegrazione
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.documenti.Allegato
import it.finmatica.atti.documenti.Certificato
import it.finmatica.atti.documenti.IProtocollabile
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioniws.comunemodena.protocollo.AllegatoIn
import it.finmatica.atti.integrazioniws.comunemodena.protocollo.InserisciProtocolloEAnagraficheString
import it.finmatica.atti.integrazioniws.comunemodena.protocollo.InserisciProtocolloEAnagraficheStringResponse
import it.finmatica.atti.integrazioniws.comunemodena.protocollo.ProtocolloSoap
import org.apache.commons.codec.binary.Base64
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
@Component
@Lazy
class ProtocolloIrideString extends AbstractProtocolloEsterno {

    private static final Logger log = Logger.getLogger(ProtocolloIrideString.class)

    @Autowired ProtocolloIrideStringConfig protocolloIrideStringConfig
    @Autowired TokenIntegrazioneService tokenIntegrazioneService
    @Autowired SpringSecurityService    springSecurityService
    @Autowired IGestoreFile             gestoreFile

    @Autowired
    @Qualifier("protocolloIride")
    private ProtocolloSoap protocolloIride

    @Override
    void sincronizzaClassificazioniEFascicoli () {}

    @Override
    @Transactional
    void protocolla (IProtocollabile atto) {
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

            // elimino il token: tutto è andato bene e verrà eliminato solo alla commit sull transaction principale
            tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
            return
        }

        try {
            String xmlProtIn = creaXmlProtocollazione(atto)

            // invoco il ws di protocollazione
            InserisciProtocolloEAnagraficheStringResponse response = protocolloIride.inserisciProtocolloString(
                    new InserisciProtocolloEAnagraficheString(protocolloInStr: xmlProtIn, codiceAmministrazione: protocolloIrideStringConfig.getEnte(springSecurityService.principal.amministrazione.codice), codiceAOO: protocolloIrideStringConfig.getAoo()))
            log.info("Messaggio XML ritornato dal Webservice di protocollazione IRIDE: ${response.inserisciProtocolloEAnagraficheStringResult}")

            ProtocolloIrideStringResponse protocollo = new ProtocolloIrideStringResponse(response.inserisciProtocolloEAnagraficheStringResult)
            if (!protocollo.successo) {
                throw new Exception("Il webservice di protocollazione ha ritornato un errore: ${protocollo.messaggio}")
            }

            // la prima cosa che faccio dopo la protocollazione è salvare il record su db:
            tokenIntegrazioneService.setTokenSuccess("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO,
                                                     "[numero:${protocollo.numero}, anno:${protocollo.anno}, data:'${protocollo.data.format("dd/MM/yyyy")}', idDocumento:${protocollo.idDocumento}]")

            // poi salvo il n. protocollato sul documento.
            atto.numeroProtocollo = protocollo.numero
            atto.annoProtocollo = protocollo.anno
            atto.dataNumeroProtocollo = protocollo.data

            atto.save()

            // elimino il token: questo avverrà solo se la transazione "normale" di grails andrà a buon fine:
            tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)

        } catch (Exception e) {
            log.error("Errore nella chiamata alla protocollazione: ${e.getMessage()}", e);
            // elimino il token (solo in caso di errore successivo la protocollazione) e interrompo la transazione del token (così si possono fare più tentativi)
            tokenIntegrazioneService.stopTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO);
            throw new AttiRuntimeException("Errore in fase di protocollazione: ${e.getMessage()}.", e);
        }
    }

    String creaXmlProtocollazione (IProtocollabile atto) {
        return new StreamingMarkupBuilder().bind { builder ->
            ProtoIn {
                Data(new SimpleDateFormat("dd/MM/yyyy").format(new Date()))
                delegate.Classifica(protocolloIrideStringConfig.getCodiceClassifica(atto.tipologiaDocumento.id))
                TipoDocumento(protocolloIrideStringConfig.getTipoDocumento(atto.tipologiaDocumento.id))
                String prefisso  = protocolloIrideStringConfig.getPrefissoOggetto(atto.tipologiaDocumento.id)
                prefisso = prefisso?.replaceAll("\\[UNITA_PROPONENTE\\]", atto.getSoggetto(TipoSoggetto.UO_PROPONENTE).unitaSo4.descrizione)
                Oggetto((prefisso + " " + atto.oggetto).trim())
                Origine("I")
                AggiornaAnagrafiche("N")
                MittenteInterno(protocolloIrideStringConfig.getUnitaProtocollo(atto.getSoggetto(TipoSoggetto.UO_PROPONENTE).unitaSo4))
                InCaricoA(protocolloIrideStringConfig.getUnitaInCaricoA(atto.getSoggetto(TipoSoggetto.UO_PROPONENTE).unitaSo4))
                Utente(protocolloIrideStringConfig.getUtenteWebservice())
                Ruolo(protocolloIrideStringConfig.getRuolo())
                Allegati {

                    // aggiungo il testo principale
                    if (atto.testo != null) {
                        this.creaAllegato(builder, atto, atto.testo, "Documento Principale")
                    }

                    // aggiungo gli allegati del documento
                    this.creaAllegati(builder, atto.allegati, "Documento Principale")

                    // Aggiungo gli allegati dei visti:
                    for (VistoParere visto : atto.visti) {
                        if (visto.valido) {
                            // aggiungo il testo del visto
                            if (visto.testo != null) {
                                this.creaAllegato(builder, visto, visto.testo, "Testo del Visto ${visto.tipologia.titolo}")
                            }
                            // aggiungo gli allegati del visto
                            this.creaAllegati(builder, visto.allegati, visto.tipologia.titolo)
                        }
                    }

                    // Aggiungo gli allegati dei visti:
                    for (Certificato certificato : atto.certificati) {
                        if (certificato.valido && certificato.testo != null) {
                            // aggiungo il testo del visto
                            this.creaAllegato(builder, certificato, certificato.testo, "Testo del Certificato")
                        }
                    }
                }
            }
        }.toString()
    }

    private void creaAllegati (def builder, Collection<Allegato> allegati, String commento) {
        // aggiungo gli allegati del documento al protocollo:
        for (Allegato allegato : allegati) {
            if (allegato.valido) {
                for (FileAllegato file : allegato.fileAllegati) {
                    if (file.valido) {
                        creaAllegato(builder, allegato, file, "Allegato n. ${allegato.sequenza} del ${commento}")
                    }
                }
            }
        }
    }

    private AllegatoIn creaAllegato (def builder, IDocumentoEsterno documento, FileAllegato allegato, String commento) {
        builder.Allegato {
            TipoFile(getEstensione(allegato.nome))
            ContentType(allegato.contentType)
            Image(Base64.encodeBase64String(IOUtils.toByteArray(gestoreFile.getFile(documento, allegato))))
            NomeAllegato(allegato.nome)
            Commento(commento)
        }
    }

    /*
Esempio di risposta con successo:

<?xml version="1.0" encoding="utf-8"?>
<ProtocolloOut>
  <IdDocumento>1340613</IdDocumento>
  <AnnoProtocollo>2018</AnnoProtocollo>
  <NumeroProtocollo>36</NumeroProtocollo>
  <DataProtocollo>2018-01-31T13:12:28.0000000</DataProtocollo>
  <Messaggio>Inserimento Protocollo eseguito con successo, senza Avvio Iter</Messaggio>
  <Registri>
    <Registro>
      <TipoRegistro />
      <AnnoRegistro>0</AnnoRegistro>
      <NumeroRegistro>0</NumeroRegistro>
    </Registro>
  </Registri>
</ProtocolloOut>

Esempio di risposta fallita:

<?xml version="1.0" encoding="utf-8"?>
<ProtocolloOut>
  <IdDocumento>0</IdDocumento>
  <AnnoProtocollo>0</AnnoProtocollo>
  <NumeroProtocollo>0</NumeroProtocollo>
  <DataProtocollo>0001-01-01T00:00:00.0000000</DataProtocollo>
  <Messaggio>IrideDll2Net.CProtocollo.Inizializza--&gt; Proprietà UTENTE non valida - Funzione:PF_ControlloProprietàObbligatorie (CProtocollo) - Funzione:PF_ControlloProprietàObbligatorie (CProtocollo)</Messaggio>
  <Registri />
  <Errore>IrideDll2Net.CProtocollo.Inizializza--&gt; Proprietà UTENTE non valida - Funzione:PF_ControlloProprietàObbligatorie (CProtocollo) - Funzione:PF_ControlloProprietàObbligatorie (CProtocollo)</Errore>
</ProtocolloOut>
     */
    static class ProtocolloIrideStringResponse {
        final String messaggio
        final long idDocumento
        final boolean successo
        final int anno
        final int numero
        final Date data

        ProtocolloIrideStringResponse (String xmlInput) {
            def xml = new XmlSlurper().parseText(xmlInput)
            idDocumento = Long.parseLong(xml.IdDocumento.text())
            successo = idDocumento > 0
            messaggio = xml.Messaggio.text()
            anno = Integer.parseInt(xml.AnnoProtocollo.text())
            numero = Integer.parseInt(xml.NumeroProtocollo.text())
            if (successo) {
                data = new Date().parse("yyyy-MM-dd'T'HH:mm:ss.S", xml.DataProtocollo.text())
            } else {
                data = null
            }
        }
    }

    private String getEstensione (String nomeFile) {
        return nomeFile.substring(nomeFile.indexOf(".")+1).toLowerCase();
    }
}
