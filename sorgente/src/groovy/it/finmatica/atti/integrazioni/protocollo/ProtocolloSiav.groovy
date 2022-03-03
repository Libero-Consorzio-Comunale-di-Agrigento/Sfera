package it.finmatica.atti.integrazioni.protocollo

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import it.finmatica.atti.AbstractProtocolloEsterno
import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.commons.TokenIntegrazione
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.dizionari.DatiAggiuntiviService
import it.finmatica.atti.documenti.Allegato
import it.finmatica.atti.documenti.Certificato
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.IProtocollabile
import it.finmatica.atti.documenti.TipoDatoAggiuntivo
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.documenti.viste.RicercaSiav
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioniws.siav.archiflow.card.ArrayOfField
import it.finmatica.atti.integrazioniws.siav.archiflow.card.AttachDocumentInput
import it.finmatica.atti.integrazioniws.siav.archiflow.card.Card
import it.finmatica.atti.integrazioniws.siav.archiflow.card.CardBundle
import it.finmatica.atti.integrazioniws.siav.archiflow.card.Document
import it.finmatica.atti.integrazioniws.siav.archiflow.card.ExternalAttachment
import it.finmatica.atti.integrazioniws.siav.archiflow.card.Field
import it.finmatica.atti.integrazioniws.siav.archiflow.card.ICardServiceContract
import it.finmatica.atti.integrazioniws.siav.archiflow.card.ICardServiceContractAttachDocumentByParamInvalidSessionFaultFaultFaultMessage
import it.finmatica.atti.integrazioniws.siav.archiflow.card.ICardServiceContractCreateCardArchiflowServiceExceptionDetailFaultFaultMessage
import it.finmatica.atti.integrazioniws.siav.archiflow.card.IdField
import it.finmatica.atti.integrazioniws.siav.archiflow.card.InsertParameters
import it.finmatica.atti.integrazioniws.siav.archiflow.login.ConnectionInfo
import it.finmatica.atti.integrazioniws.siav.archiflow.login.ILoginServiceContract
import it.finmatica.atti.integrazioniws.siav.archiflow.login.Language
import it.finmatica.atti.integrazioniws.siav.archiflow.login.ResultInfo
import it.finmatica.atti.integrazioniws.siav.archiflow.login.SessionInfo
import org.apache.commons.codec.binary.Base64
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import javax.sql.DataSource
import javax.xml.ws.Holder
import java.sql.Connection

/**
 * Created by czappavigna on 12/09/2018.
 */
@Component("protocolloSiav")
@Lazy
class ProtocolloSiav extends AbstractProtocolloEsterno {
    private static final Logger log = Logger.getLogger(ProtocolloSiav.class);

    @Autowired TokenIntegrazioneService tokenIntegrazioneService
    @Autowired DatiAggiuntiviService    datiAggiuntiviService
    @Autowired ProtocolloSiavConfig     protocolloSiavConfig
    @Autowired IGestoreFile             gestoreFile
    @Autowired DataSource               dataSource

    String getPasswordUtente() {
        return protocolloSiavConfig.getPasswordWebService()
    }

    String getUsername() {
        return protocolloSiavConfig.getUtenteWebService()
    }

    String getUrlWebservice() {
        return protocolloSiavConfig.getUrlWebService()
    }

    String getArchivio() {
        return protocolloSiavConfig.getArchivio()
    }

    @Override
    @Transactional
    void protocolla (IProtocollabile atto) {

        log.info("Protocollazione Siav");
        // ottengo il lock pessimistico per evitare doppie protocollazioni.
        atto.lock();

        // controllo che il documento non sia già protocollato
        if (atto.numeroProtocollo > 0) {
            throw new AttiRuntimeException("Il documento è già protocollato con numero: ${atto.numeroProtocollo} / ${atto.annoProtocollo}!");
        }

        // controllo se c'è un token di protocollazione precedente dovuto ad un errore dopo la protocollazione:
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
            return;
        }

        Holder<Card> outCard = new Holder<>(Card)
        String cardId = null;
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(ILoginServiceContract.class);
        factory.setAddress(urlWebservice + "/Login.svc");
        def loginServiceContract = (ILoginServiceContract) factory.create();

        ConnectionInfo connectionInfo = new ConnectionInfo()
        connectionInfo.dateFormat = "dd/mm/yyyy"
        connectionInfo.setLanguage(Language.ITALIAN)
        connectionInfo.workflowDomain= "SIAV"
        Holder<ResultInfo> resultInfo = new Holder<ResultInfo>();
        Holder<SessionInfo> sessionInfo = new Holder<SessionInfo>();
        String idConn = null;

        try {

            loginServiceContract.login(username, passwordUtente, connectionInfo, resultInfo, sessionInfo);
            idConn = sessionInfo.value.getSessionId()

            log.info("Effettuato login - id_connessione =  "+ idConn);

            InsertParameters insertParameters = new InsertParameters()
            insertParameters.setOCard(creaScheda(idConn, atto));

            JaxWsProxyFactoryBean cardFactory = new JaxWsProxyFactoryBean();
            cardFactory.setServiceClass(ICardServiceContract.class);
            cardFactory.setAddress(urlWebservice + "/Card.svc");
            def cardServiceContract = (ICardServiceContract) cardFactory.create();

            cardServiceContract.createCard(idConn, insertParameters, resultInfo, outCard)
            cardId = outCard.value.getCardId()
            log.info("Chiamata InserisciScheda terminata - id_connessione =  "+ idConn);
            log.info("Chiamata InserisciScheda CardId =  "+ cardId);

            // scrivi il risultato su db
            scriviProtocolloDb (outCard.value, atto);

            try {
                if (cardId != null) {
                    aggiungiAllegati(idConn, cardId, atto)
                }
            }
            catch (ICardServiceContractAttachDocumentByParamInvalidSessionFaultFaultFaultMessage e) {
                log.error("Errore nella chiamata inserimento allegati di protocollo via webservice: ${e.getFaultInfo().message}", e);
            }
            catch (Exception e){
                log.error("Errore nella chiamata inserimento allegati di protocollo via webservice: ${e.getMessage()}", e);
            }

            loginServiceContract.logout(sessionInfo.value);
            log.info("Effettuato logout - id_connessione =  "+ idConn);

            // elimino il token: questo avverrà solo se la transazione "normale" di grails andrà a buon fine:
            tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)

        } catch (ICardServiceContractCreateCardArchiflowServiceExceptionDetailFaultFaultMessage e){
            // elimino il token e interrompo la transazione del token (così si possono fare più tentativi)
            tokenIntegrazioneService.stopTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO);
            log.error("Errore nella chiamata alla protocollazione webservice: ${e.getFaultInfo().message}", e);
            log.error("Errore nella chiamata alla protocollazione webservice: ${e.getMessage()}", e);
        }
        catch (Exception e) {
            // elimino il token e interrompo la transazione del token (così si possono fare più tentativi)
            tokenIntegrazioneService.stopTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO);

            log.error("Errore nella chiamata alla protocollazione webservice: ${e.getMessage()}", e);
            throw new AttiRuntimeException("Errore in fase di protocollazione via webservice: ${e.getMessage()}.", e);
        }
    }

    private void scriviProtocolloDb(Card p, IProtocollabile documento) throws Exception {
        String progressivoAnnuo = p?.getIndexes().getField().get(0).fieldValue
        log.debug("ProgressivoAnnuo: " + progressivoAnnuo)
        String[] campi = progressivoAnnuo?.split("/");
        String anno = "20" + campi[1]
        String numero = campi[0]
            log.info("Anno protocollo =  " + anno);
            log.info("Numero protocollo =  " + numero);

        // la prima cosa che faccio dopo la protocollazione è salvare il record su db:
        tokenIntegrazioneService.setTokenSuccess("${documento.id}", TokenIntegrazione.TIPO_PROTOCOLLO, "[numero:${numero}, anno:${anno}, data:'${new Date().format('dd/MM/yyyy')}']");

            documento.dataNumeroProtocollo = new Date();
            documento.annoProtocollo = Integer.parseInt(anno);
            documento.numeroProtocollo = Integer.parseInt(numero);

            documento.save()
        }

    private CardBundle creaScheda(String connId, IAtto doc) throws IOException, Exception {
        CardBundle scheda = new CardBundle();
        scheda.setArchiveId(Short.parseShort(archivio))
        scheda.setDocumentTypeId(Short.parseShort("4"))

        Connection conn = dataSource.connection
        ArrayOfField indici = new ArrayOfField()
        indici.field.add(creaIndice (IdField.IF_REFERENCE, 	""));                                   //numero di protocollo che il sistema deve valorizzare
        indici.field.add(creaIndice (IdField.IF_OBJ, 	doc.oggetto));                              //"Oggetto"
        indici.field.add(creaIndice (IdField.IF_KEY_11,	getSettore(doc, conn)));                    // "Settore-Struttura Destinatario"
        indici.field.add(creaIndice (IdField.IF_KEY_21, getMittente(doc, conn)));                   //"Mittente"
        indici.field.add(creaIndice (IdField.IF_KEY_31, getTipoDocumento(doc, conn)));              //"Tipo Documento"
        indici.field.add(creaIndice (IdField.IF_KEY_32, "FL FILE"));                                //"Supporto"
        indici.field.add(creaIndice (IdField.IF_KEY_42, doc.proposta.numeroProposta.toString()));   //"Riferimento ADS"
        indici.field.add(creaIndice (IdField.IF_KEY_43, doc.numeroAtto.toString()));                //"Numero Registro"
        indici.field.add(creaIndice (IdField.IF_KEY_44, doc.annoAtto.toString()));                  //"Anno Registro"

        if (doc.dataAtto != null) {
            indici.field.add(creaIndice(IdField.IF_KEY_45, doc.dataAtto.format("dd/MM/yyyy")))  //"Data Adozione"
        }

        scheda.setIndexes(indici);
        scheda.setHasDocument(true);
        scheda.mainDocument = creaDocumento(doc);

        return scheda;
    }

    private Document creaDocumento(IAtto doc) throws IOException, Exception {
        String nomeAllegato 	= doc.testo.nome;
        log.debug("File allegato: "+nomeAllegato);

        String estensione 		= getEstensione(nomeAllegato);

        Document documento = new Document();
        documento.setDocumentTitle(getNomeFile(normalizeNomeFile(nomeAllegato)));
        documento.setDocumentExtension(estensione);
        documento.setContentType(doc.testo.contentType)
        documento.setOriginalFileName(nomeAllegato)
        documento.setVersion(0)
        documento.setContent(Base64.encodeBase64String(gestoreFile.getFile(doc, doc.testo).getBytes()));

        return documento;
    }

    void aggiungiAllegati (String sessionId, String cardId, IAtto atto) {
        if (atto.stampaUnica != null) {
            creaAllegato(sessionId, cardId, atto, atto.stampaUnica)
        }

        // aggiungo gli allegati se l'impostazione lo richiede.
        for (Allegato allegato : atto.allegati) {
            if (allegato.valido) {
                for (FileAllegato fileAllegato : allegato.fileAllegati) {
                    if (fileAllegato != null) {
                        creaAllegato(sessionId, cardId, allegato, fileAllegato)
                    }
                }
            }
        }

        def vistiPareriAtto = atto.visti.findAll{it.valido == true}
        def vistiPareriProposta = (atto instanceof Delibera) ? atto.proposta.visti.findAll{it.valido == true} : []
        def vistiPareri = (vistiPareriProposta ?: []) + (vistiPareriAtto ?: [])

        for (def vistoParere : vistiPareriProposta){
            if (vistiPareriAtto.findAll{ it.tipologia.codice == vistoParere.tipologia.codice}.size() > 0){
                vistiPareri.remove(vistoParere)
            }
        }

        for (VistoParere visto : vistiPareri) {
            if (!visto.valido) {
                continue
            }

            if (visto.testo != null) {
                creaAllegato(sessionId, cardId, visto, visto.testo)
            }

            for (Allegato allegato : visto.allegati) {
                if (allegato.valido) {
                    for (FileAllegato fileAllegato : allegato.fileAllegati) {
                        if (fileAllegato != null) {
                            if (fileAllegato != null) {
                                creaAllegato(sessionId, cardId, allegato, fileAllegato)
                            }
                        }
                    }
                }
            }
        }
        for (Certificato certificato : atto.certificati) {
            if (!certificato.valido) {
                continue
            }

            if (certificato.testo != null) {
                creaAllegato(sessionId, cardId, certificato, certificato.testo)
            }
        }
    }

    private void creaAllegato(String sessionId, String cardId, IDocumentoEsterno documento, FileAllegato fileAllegato) throws IOException, Exception {
        String nomeFile 		= normalizeNomeFile(fileAllegato.nome)
        String estensione 		= getEstensione(nomeFile)
        String mimeType 		= fileAllegato.contentType

        ExternalAttachment attach = new ExternalAttachment();
        attach.setName(getNomeFile(nomeFile));
        attach.setExtension(estensione)
        attach.setContent(Base64.encodeBase64String(gestoreFile.getFile(documento, fileAllegato).getBytes()))

        // se riservato o no
        if (documento.riservato) {
            attach.setNote("RISERVATO_")
        }
        JaxWsProxyFactoryBean cardFactory = new JaxWsProxyFactoryBean();
        cardFactory.setServiceClass(ICardServiceContract.class);
        cardFactory.setAddress(urlWebservice + "/Card.svc");
        def cardServiceContract = (ICardServiceContract) cardFactory.create();
        cardFactory.setUsername(getUsername())
        cardFactory.setPassword(getPasswordUtente())
        cardServiceContract.attachDocument(sessionId, cardId, attach, false, false)
    }

    private Field creaIndice (IdField idField, String valore) {
        Field indice = new Field();
        indice.setFieldId(idField)
        indice.setFieldValue(valore);
        return indice;
    }

    private String getMittente(IAtto doc, Connection conn) {
        if (doc instanceof Delibera){
            Sql sql = new Sql(conn)
            String mittente = ""
            sql.eachRow("SELECT MITTENTE FROM MAPPING_PROTOCOLLO_SIAV WHERE TIPO_PROPOSTA = ${doc.proposta.tipologia.tipoRegistroDelibera.codice}") { row ->
                mittente = row.MITTENTE
            }
            return mittente

        } else {
            RicercaSiav elemento = RicercaSiav.findByCodiceStrutturaAndCodiceSiav(doc.proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.progr, datiAggiuntiviService.getDatoAggiuntivo(doc.proposta, TipoDatoAggiuntivo.PROTOCOLLO_SIAV_STRUTTURA)?.valore)
            return elemento.descrizione;
        }
    }

    private String getSettore(IAtto doc, Connection conn) {
        RicercaSiav elemento = RicercaSiav.findByCodiceStrutturaAndCodiceSiav(doc.proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.progr, datiAggiuntiviService.getDatoAggiuntivo(doc.proposta, TipoDatoAggiuntivo.PROTOCOLLO_SIAV_STRUTTURA)?.valore)
        return elemento?.descrizione;
    }

    private String getTipoDocumento(IAtto doc, Connection conn) {
        String codiceRegistro = doc.registroAtto?.codice

        if (codiceRegistro == null) {
            if (doc instanceof Delibera){
                    codiceRegistro = doc.propostaDelibera.tipologia.tipoRegistroDelibera.codice
            } else if (doc instanceof Determina) {
                codiceRegistro = doc.tipologia.tipoRegistro.codice
            } else {
                throw new AttiRuntimeException("Tipo di documento (${doc.class.name}) non riconosciuto.")
            }
        }

        if (codiceRegistro == null) {
            throw new AttiRuntimeException('Non ho il codiceRegistro per protocollare.')
        }

        Sql sql = new Sql(conn)
        List<GroovyRowResult> rows = sql.rows('SELECT TIPO_DOCUMENTO FROM MAPPING_PROTOCOLLO_SIAV WHERE TIPO_PROPOSTA = ?', [codiceRegistro])
        if (rows.size() == 0) {
            throw new AttiRuntimeException("Non ho trovato il tipo di documento per il tipo registro ${codiceRegistro}")
        }
        return rows[0].TIPO_DOCUMENTO
    }

    private String getEstensione (String nomeFile) {
        return nomeFile.substring(nomeFile.lastIndexOf(".")+1).toLowerCase();
    }

    /**
     * Elimina dal nome del file tutte le estensioni .p7m tranne una.
     * Ad esempio, "determina 123.doc.p7m.p7m" -> "determina 123.doc.p7m"
     *
     * @param nomeFile	il nome del file da normalizzare
     * @return			il nome del file normalizzato
     */
    private String normalizeNomeFile (String nomeFile) {
        return nomeFile.replaceAll("(\\.p7m)+", "\\.p7m"); // rimuovo tutte le occorrenze di .p7m tranne una.
    }

    private String getNomeFile (String nomeFile) {
        return nomeFile.substring(0, nomeFile.lastIndexOf("."));
    }

}

