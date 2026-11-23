package it.finmatica.atti.integrazioni.protocollo

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import integrazione.siav.classes.CreateCardResponse
import integrazione.siav.classes.LogoutResponse
import integrazione.siav.classes.SessionInfo
import integrazione.siav.utily.Utils
import it.finmatica.atti.AbstractProtocolloEsterno
import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.commons.TokenIntegrazione
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.dizionari.DatiAggiuntiviService
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.viste.RicercaSiav
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.protocollo.siav.ExternalAttachment
import org.apache.commons.codec.binary.Base64
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import javax.sql.DataSource
import java.sql.Connection

/**
 * Created by czappavigna on 12/09/2018.
 */
@Component("protocolloSiavRest")
@Lazy
class ProtocolloSiavRest extends AbstractProtocolloEsterno {
    private static final Logger log = Logger.getLogger(ProtocolloSiavRest.class);

    @Autowired TokenIntegrazioneService tokenIntegrazioneService
    @Autowired DatiAggiuntiviService    datiAggiuntiviService
    @Autowired ProtocolloSiavRestConfig     protocolloSiavRestConfig
    @Autowired IGestoreFile             gestoreFile
    @Autowired DataSource               dataSource

    String getPasswordUtente() {
        return protocolloSiavRestConfig.getPasswordWebService()
    }

    String getUsername() {
        return protocolloSiavRestConfig.getUtenteWebService()
    }

    String getUrlWebservice() {
        return protocolloSiavRestConfig.getUrlWebService()
    }

    String getArchivio() {
        return protocolloSiavRestConfig.getArchivio()
    }

    String getDocumentTypeId() {
        return protocolloSiavRestConfig.getDocumentTypeId()
    }

    @Override
    @Transactional
    void protocolla (IProtocollabile atto) {
        log.info("Protocollazione Siav Rest");
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

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        try {
            // Eseguo la login
            integrazione.siav.classes.SessionInfo sessionInfo=login()
            log.info("Effettuato login rest - id_connessione =  "+ sessionInfo.SessionId);
            try{
                //protocollo l'Atto
                integrazione.siav.classes.CreateCardResponse createCardResponse= protocollaAtto(sessionInfo.SessionId,atto)
                if(createCardResponse.oCardRet!=null && createCardResponse.oCardRet.CardId!=null){
                    log.info("Chiamata InserisciScheda rest CardId =  "+ createCardResponse.oCardRet.CardId);

                    // scrivi il risultato su db
                    scriviProtocolloDb (createCardResponse.oCardRet, atto);
                    //aggiungi gli allegati
                    aggiungiAllegati(sessionInfo.SessionId, createCardResponse.oCardRet.CardId, atto)
                    //effettua il logout
                    integrazione.siav.classes.LogoutResponse responseLogout= logout(sessionInfo)
                    log.info("Effettuato logout  rest - id_connessione =  "+ sessionInfo.SessionId);
                }
            }catch (Exception ex){
                throw new AttiRuntimeException("Errore in fase di protocollazione via webservice rest: ${ex.getMessage()}.", ex);
            }

            // elimino il token: questo avverrà solo se la transazione "normale" di grails andrà a buon fine:
            tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
        }
        catch (Exception e) {
            // elimino il token e interrompo la transazione del token (così si possono fare più tentativi)
            tokenIntegrazioneService.stopTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO);

            log.error("Errore nella chiamata alla protocollazione webservice rest: ${e.getMessage()}", e);
            throw new AttiRuntimeException("Errore in fase di protocollazione via webservice rest: ${e.getMessage()}.", e);
        }
    }

    SessionInfo login() throws Exception{
        integrazione.siav.classes.SessionInfo ret=null
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        integrazione.siav.classes.Login login = new integrazione.siav.classes.Login();
        login.strUser = getUsername();
        login.strPassword = getPasswordUtente()
        integrazione.siav.classes.ConnectionInfo oConnectionInfo = new integrazione.siav.classes.ConnectionInfo();
        oConnectionInfo.dateFormat = "dd/MM/yyyy";
        oConnectionInfo.workflowDomain = "SIAV";
        login.oConnectionInfo = oConnectionInfo;

        String postData = objectMapper.writeValueAsString(login)
        String jsonResponse=  restCall(postData,getUrlWebservice(),"/login.svc/json/Login","POST")
        integrazione.siav.classes.LoginResponse loginResponse = objectMapper.readValue(jsonResponse, integrazione.siav.classes.LoginResponse.class);
        ret=loginResponse.oSessionInfo
        return ret
    }

    private CreateCardResponse protocollaAtto(String strSessionId,IAtto atto) throws Exception{
        CreateCardResponse ret=null
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        integrazione.siav.classes.CardBundle scheda = creaScheda(strSessionId,atto)
        integrazione.siav.classes.CreateCard createCard = new integrazione.siav.classes.CreateCard()
        integrazione.siav.classes.InsertParameters insertParameters        = new integrazione.siav.classes.InsertParameters();
        insertParameters.oCard=scheda
        createCard.oInsertParameters=insertParameters
        createCard.strSessionId=strSessionId
        String postData = objectMapper.writeValueAsString(createCard);
        String jsonResponse=  restCall(postData,getUrlWebservice(),"/Card.svc/json/CreateCard","PUT")
        ret= objectMapper.readValue(jsonResponse, integrazione.siav.classes.CreateCardResponse.class);
        return ret
    }

    LogoutResponse logout(SessionInfo sessionInfo) throws Exception{
        LogoutResponse ret=null
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        integrazione.siav.classes.Logout logout = new integrazione.siav.classes.Logout();
        sessionInfo.Language=null
        sessionInfo.LoginType=null
        logout.oSessionInfo=sessionInfo;
        String postData = objectMapper.writeValueAsString(logout)
        String jsonResponse=  restCall(postData,getUrlWebservice(),"/login.svc/json/Logout","POST")
        ret= objectMapper.readValue(jsonResponse, integrazione.siav.classes.LogoutResponse.class);
        return ret
    }



    void allegaFile(String sessionId, String cardId, IDocumentoEsterno documento, FileAllegato fileAllegato) throws Exception{
        String nomeFile 		= normalizeNomeFile(fileAllegato.nome)
        String extension 		= getEstensione(nomeFile)
        String mimeType 		= fileAllegato.contentType
        String content= Base64.encodeBase64String(gestoreFile.getFile(documento, fileAllegato).getBytes())
        String name = getNomeFile(nomeFile)

        String postData ="<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:tem=\"http://tempuri.org/\" xmlns:siav=\"http://schemas.datacontract.org/2004/07/SIAV.Archiflow.Entities.Card.ValueObject\">\n" +
                "   <soapenv:Header/>\n" +
                "   <soapenv:Body>\n" +
                "      <tem:AttachExternalDocument>\n" +
                "         <tem:strSessionId>[SESSION_ID]</tem:strSessionId>\n" +
                "         <tem:oCardId>[CARD_ID]</tem:oCardId>\n" +
                "         <tem:oAttachment>\n" +
                "            <siav:Name>[NAME]</siav:Name>\n" +
                "            <siav:Note>[NOTE]</siav:Note>\n" +
                "            <siav:SecurityUserId>0</siav:SecurityUserId>\n" +
                "            <siav:Content>[CONTENT]</siav:Content>\n" +
                "            <siav:ContentType>[CONTENT_TYPE]</siav:ContentType>\n" +
                "            <siav:Extension>[EXTENSION]</siav:Extension>\n" +
                "         </tem:oAttachment>\n" +
                "      </tem:AttachExternalDocument>\n" +
                "   </soapenv:Body>\n" +
                "</soapenv:Envelope>"

        postData=postData.replace("[SESSION_ID]",sessionId)
        postData=postData.replace("[CARD_ID]",cardId)
        postData=postData.replace("[NAME]",name)

        if (documento.riservato) {
            postData=postData.replace("[NOTE]","RISERVATO_")
        } else {
            postData=postData.replace("[NOTE]","")
        }
        postData=postData.replace("[CONTENT]",content)
        postData=postData.replace("[CONTENT_TYPE]",mimeType)
        postData=postData.replace("[EXTENSION]",extension)

        String jsonResponse=  restCall(postData,getUrlWebservice(),"/Card.svc","POST",true)
        log.info("Allegafile ="+ name + "->OK")
    }

    public integrazione.siav.classes.AttachDocumentByParamResponse allegaFileExt(String sessionId, String cardId, IDocumentoEsterno documento, FileAllegato fileAllegato) throws Exception{
        String nomeFile 		= normalizeNomeFile(fileAllegato.nome)
        String estensione 		= getEstensione(nomeFile)
        String mimeType 		= fileAllegato.contentType

        integrazione.siav.classes.AttachDocumentByParamResponse ret=null

        integrazione.siav.classes.AttachDocumentByParam attachDocumentByParam = new integrazione.siav.classes.AttachDocumentByParam()
        integrazione.siav.classes.AttachDocumentInput attachDocumentInput = new integrazione.siav.classes.AttachDocumentInput();
        attachDocumentInput.CardId=cardId;

        ExternalAttachment attach = new ExternalAttachment();
        attach.__type="ExternalAttachment:#SIAV.Archiflow.Entities.Card.ValueObject";
        attach.Name=getNomeFile(nomeFile)
        attach.Extension=estensione
        attach.Content=Base64.encodeBase64String(gestoreFile.getFile(documento, fileAllegato).getBytes())
        //attach.ContentType=mimeType
        attach.Note=" "
        attach.SecurityUserId=0
        // se riservato o no
        if (documento.riservato) {
            attach.Note="RISERVATO_"
        }

        attachDocumentInput.Attachment=attach
        //attachDocumentInput.FailIfExists=true;
        attachDocumentInput.Circular=false;
        attachDocumentInput.SessionInfo = new SessionInfo()
        attachDocumentInput.SessionInfo.SessionId=sessionId;
        attachDocumentByParam.paramIn=attachDocumentInput

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)

        String postData = objectMapper.writeValueAsString(attachDocumentByParam);

        String jsonResponse=  restCall(postData,getUrlWebservice(),"/Card.svc/json/AttachDocumentByParam","POST")

        ret= objectMapper.readValue(jsonResponse, integrazione.siav.classes.AttachDocumentByParamResponse.class);

        return ret
    }


    private void scriviProtocolloDb(integrazione.siav.classes.Card card, IProtocollabile documento) throws Exception {
        String progressivoAnnuo = card.Indexes[0].FieldValue
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

    private integrazione.siav.classes.CardBundle creaScheda(String connId, IAtto doc) throws IOException, Exception {
        integrazione.siav.classes.CardBundle scheda = new integrazione.siav.classes.CardBundle();
        scheda.ArchiveId= Short.parseShort(getArchivio())
        scheda.DocumentTypeId= Short.parseShort(getDocumentTypeId())

        Connection conn = dataSource.connection

        List<integrazione.siav.classes.Field> indici = new ArrayList<integrazione.siav.classes.Field>();
        indici.add(new integrazione.siav.classes.Field(0, 	""));  //numero di protocollo che il sistema deve valorizzare
        indici.add(new integrazione.siav.classes.Field (24, 	doc.oggetto));                              //"Oggetto"
        indici.add(new integrazione.siav.classes.Field (4,	getSettore(doc, conn)));                    // "Settore-Struttura Destinatario"
        indici.add(new integrazione.siav.classes.Field (9, getMittente(doc, conn)));                   //"Mittente"
        indici.add(new integrazione.siav.classes.Field (14, getTipoDocumento(doc, conn)));              //"Tipo Documento"
        indici.add(new integrazione.siav.classes.Field (15, "FL FILE"));                                //"Supporto"
        indici.add(new integrazione.siav.classes.Field (20, doc.proposta.numeroProposta.toString()));   //"Riferimento ADS"
        indici.add(new integrazione.siav.classes.Field (21, doc.numeroAtto.toString()));                //"Numero Registro"
        indici.add(new integrazione.siav.classes.Field (22, doc.annoAtto.toString()));                  //"Anno Registro"

        if (doc.dataAtto != null) {
            indici.add(new integrazione.siav.classes.Field(23, doc.dataAtto.format("dd/MM/yyyy")))  //"Data Adozione"
        }

        scheda.Indexes=indici
        scheda.HasDocument=true
        scheda.MainDocument = creaDocumento(doc);

        return scheda;
    }

    private integrazione.siav.classes.Document creaDocumento(IAtto doc) throws IOException, Exception {
        String nomeAllegato 	= doc.testo.nome;
        log.debug("File allegato rest: "+nomeAllegato);

        String estensione 		= getEstensione(nomeAllegato);

        integrazione.siav.classes.Document documento = new integrazione.siav.classes.Document();
        documento.DocumentTitle=getNomeFile(normalizeNomeFile(nomeAllegato))
        documento.DocumentExtension=estensione
        documento.ContentType=doc.testo.contentType
        documento.OriginalFileName=nomeAllegato
        documento.Version=0
        documento.Content=Base64.encodeBase64String(gestoreFile.getFile(doc, doc.testo).getBytes());

        return documento;
    }

    void aggiungiAllegati (String sessionId, String cardId, IAtto atto) {
        if (atto.stampaUnica != null) {
            allegaFileExt(sessionId, cardId, atto, atto.stampaUnica)
        }

        // aggiungo gli allegati se l'impostazione lo richiede.
        for (Allegato allegato : atto.allegati) {
            if (allegato.valido) {
                for (FileAllegato fileAllegato : allegato.fileAllegati) {
                    if (fileAllegato != null) {
                        allegaFileExt(sessionId, cardId, allegato, fileAllegato)
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
                allegaFileExt(sessionId, cardId, visto, visto.testo)
            }

            for (Allegato allegato : visto.allegati) {
                if (allegato.valido) {
                    for (FileAllegato fileAllegato : allegato.fileAllegati) {
                        if (fileAllegato != null) {
                            if (fileAllegato != null) {
                                allegaFileExt(sessionId, cardId, allegato, fileAllegato)
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
                allegaFileExt(sessionId, cardId, certificato, certificato.testo)
            }
        }
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

    String restCall(String postData, String endPointUrl, String action, String method ) throws Exception{
        return restCall(postData,endPointUrl,action,method,false)
    }

    String restCall(String postData, String endPointUrl, String action, String method, boolean soap ) throws Exception{
        String ret=null
        String jsonLog = postData.replaceAll("\"Content\"\\s*:\\s*\"[^\"]*\"","\"Content\":\"\"")
        log.info("[REQ]"+ action + "="+jsonLog);
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(endPointUrl+action).openConnection();
            connection.setRequestMethod(method);
            connection.setDoOutput(true);
            if(soap){
                connection.addRequestProperty("Content-Type","text/xml;charset=UTF-8");
                connection.addRequestProperty("SOAPAction","http://tempuri.org/ICardServiceContract/AttachExternalDocument");
            } else {
                connection.addRequestProperty("Content-Type","application/json");
            }

            OutputStreamWriter wr = new OutputStreamWriter(connection.getOutputStream());
            wr.write(postData);
            wr.flush();

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                InputStream is = connection.getInputStream();
                ret = Utils.getStringFromIS(is);
                String jsonRetLog = ret.replaceAll("\"Content\"\\s*:\\s*\"[^\"]*\"","\"Content\":\"\"")
                log.debug("[RESP]"+ action + "="+jsonRetLog);
            } else {
                InputStream err =connection.getErrorStream();
                ret = Utils.getStringFromIS(err);
                String jsonErrLog = ret.replaceAll("\"Content\"\\s*:\\s*\"[^\"]*\"","\"Content\":\"\"")
                log.error("[ERR] "+action+"rest="+jsonErrLog);
                throw  new Exception("Errore nella chiamata al servizio " +action + "_rest="+jsonErrLog)
            }
        }catch (Exception ex){
            ex.printStackTrace();
            throw new Exception(ex);
        }
        return ret
    }
}

