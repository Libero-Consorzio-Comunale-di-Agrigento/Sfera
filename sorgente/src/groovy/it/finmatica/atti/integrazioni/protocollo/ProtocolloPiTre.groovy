package it.finmatica.atti.integrazioni.protocollo

import com.fasterxml.jackson.databind.ObjectMapper
import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.AbstractProtocolloEsterno
import it.finmatica.atti.IDocumentaleEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.IProtocolloEsterno
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.commons.StrutturaOrganizzativaService
import it.finmatica.atti.commons.TokenIntegrazione
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.documenti.Allegato
import it.finmatica.atti.documenti.Certificato
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.DestinatarioNotifica
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IProtocollabile
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.pitre.PiTreField
import it.finmatica.atti.integrazioni.pitre.PiTreTemplate
import it.finmatica.atti.integrazioniws.pitre.PiTreClient
import it.finmatica.atti.integrazioniws.pitre.PiTreConfig
import it.finmatica.atti.integrazioniws.pitre.classes.*
import it.finmatica.atti.integrazioniws.pitre.enumeration.FilterCorrespondentType
import it.finmatica.atti.integrazioniws.pitre.enumeration.FilterProjectType
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubbService
import org.apache.log4j.Logger
import org.apache.commons.io.IOUtils
import org.apache.commons.codec.binary.Base64
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import it.finmatica.atti.IProtocolloEsterno.Fascicolo

import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * Integrazione del protocollo PiTre
 */
@Component("protocolloPiTre")
@Lazy
class ProtocolloPiTre extends AbstractProtocolloEsterno {

    private static final Logger log = Logger.getLogger(ProtocolloPiTre.class)
    private static final TIPO_DESTINATARI_NOTIFICHE = "PITRE"
    public static final RUBRICA_DEI_SERVIZI = "rubrica dei servizi"
    public static final RUBRICA_ENTE = "rubrica ente"
    public static final RUBRICA_PROVINCIALE = "rubrica provinciale"
    public static final RUBRICA_NAZIONALE = "rubrica nazionale"


    @Autowired IDocumentaleEsterno gestoreDocumentaleEsterno
    @Autowired TokenIntegrazioneService tokenIntegrazioneService
    @Autowired ProtocolloPiTreConfig protocolloPiTreConfig
    @Autowired IGestoreFile gestoreFile
    @Autowired SpringSecurityService springSecurityService
    @Autowired StrutturaOrganizzativaService strutturaOrganizzativaService
    @Autowired So4UnitaPubbService so4UnitaPubbService

    public static void main(String[] args) {
        testAlex()
    }

    @Override
    public String getZulFascicolo() {
        return "/commons/popupFascicoliPiTre.zul";
    }


    public void testAlex(){

        PiTreConfig config = new PiTreConfig()
        config.pathCertificate="C:\\Users\\AMarinelli\\Desktop\\xTrento\\client-t.ADScomTN-to-restpitre.tn.it.pfx"
        config.cacert_path="C:\\Program Files\\Java\\jdk1.8.0_201\\jre\\lib\\security\\cacerts"
        config.endPointUrl="https://ws-t.pitre.tn.it/RestServices"
        config.passwd="P1BCXeMquUQen4Ql"
        config.CodeApplication="ADS_COM_TN"
        config.CodeAdm="COM_TN"
        config.Username="ATTI_ADS_AUTOMATEDDATASYSTEM_01"
        config.CodeRole=""
        config.passwd_cacert="changeit";


        GetTokenRequest request= new GetTokenRequest()
        request.Username=config.Username
        request.CodeAdm=config.CodeAdm
        request.CodeApplication=config.CodeApplication
        request.CodeRole=config.CodeRole

        ProtocolloPiTre piTre=new ProtocolloPiTre();
        GetTokenResponse token = piTre.getToken(request,config)

        List<Project> fascicoli = cercaProject("","test",2021,Project.TYPE_GENERICO,config,token.Token,"")
        //cercaProject("6.3.1/2015/1","",Project.TYPE_GENERICO,config,token.Token)

        println "ciao"
    }


    boolean checkMostraControlliTemplate(String codiceCampo,String tipologia){
        boolean ret=false
        String idTemplate = protocolloPiTreConfig.getTemplateByTypo(tipologia)
        PiTreTemplate currentTemplate = PiTreTemplate.get(Integer.valueOf(idTemplate))
        if (currentTemplate) {
            for (PiTreField field : currentTemplate.fields) {
                if (field.codice.equals(codiceCampo) && field.richiesto) {
                    ret = true
                    break
                }
            }
        }
        return ret
    }

    List<String> loadValoriCampo(String codiceCampo, String tipologia){
        List<String> ret = new ArrayList<String>()
        String idTemplate = protocolloPiTreConfig.getTemplateByTypo(String.valueOf(tipologia))
        PiTreTemplate currentTemplate = PiTreTemplate.get(Integer.valueOf(idTemplate))
        if (currentTemplate) {
            for (PiTreField field : currentTemplate.fields) {
                if (field.codice.equals(codiceCampo)) {
                    String[] valori = field.valori.split("#")
                    ret = new ArrayList<>(Arrays.asList(valori))
                    break
                }
            }
        }
        return ret
    }

    String prendiToken(PiTreConfig config,So4UnitaPubb unitaProponente){
        So4UnitaPubb unitaSo4 = strutturaOrganizzativaService.getServizio(unitaProponente)
        String username=unitaSo4.codice
        String codeRole = config.CodeRole
        return prendiTokenByUsernameCodeRole(config,username,codeRole)
    }

    String prendiTokenByUsernameCodeRole(PiTreConfig config,String username,String codeRole){
        String ret=""
        GetTokenRequest request= new GetTokenRequest()
        request.Username=username
        request.CodeAdm=config.CodeAdm
        request.CodeApplication=config.CodeApplication
        request.CodeRole=codeRole
        GetTokenResponse response = getToken(request,config)
        if(response.Code==0){
            ret=response.Token
        } else {
            throw new Exception(response.Code + "-" +response.ErrorMessage)
        }
        return ret
    }

    So4UnitaPubb calcoloUnitaServizioIstituzionaleDelRedattore(IProtocollabile atto,Ad4Utente utenteRedattore){
        So4UnitaPubb unitaServizioIstituzionaleSingola = null
        if(utenteRedattore == null) {
            if (atto instanceof Determina) {
                utenteRedattore = atto.getSoggetto(TipoSoggetto.REDATTORE).utenteAd4
            }
            if (atto instanceof Delibera) {
                utenteRedattore = atto.propostaDelibera.getSoggetto(TipoSoggetto.REDATTORE).utenteAd4
            }
        }
        As4SoggettoCorrente soggettoCorrente = As4SoggettoCorrente.findByUtenteAd4(utenteRedattore)
        List<So4UnitaPubb> unitaServizioIstituzionale = so4UnitaPubbService.getUnitaPubbSoggetto(soggettoCorrente.id,springSecurityService.principal.amministrazione.codice,springSecurityService.principal.ottica().codice)
        if(unitaServizioIstituzionale!=null && unitaServizioIstituzionale.size()>=1) {
            unitaServizioIstituzionaleSingola=unitaServizioIstituzionale[0]
        }
        return unitaServizioIstituzionaleSingola
    }

/**
 *
 * @param codice
 * @param anno Anno del Fascicolo
 * @param descrizione  Inserire almeno una parola, non solo una parte, Fascicolo OK, icolo, ico, colo NO
 * @param projectType Inserire “P” per procedimentale, “G” per generale.
 * @param config
 * @param authToken
 *
 * Il titolario risulta articolato nelle seguenti sei macroaree:
 1. Organismi;
 2. Attività;
 3. Organizzazione, patrimonio e risorse strumentali;
 4. Risorse umane;
 5. Risorse finanziarie e gestione contabile;
 6. Sistema informativo e documentale.
 * @return
 */
    List<Project> cercaProject(String codice, String descrizione, Integer anno, String projectType,  PiTreConfig config, String authToken,String tipologia){
        String registro = protocolloPiTreConfig.getRegistroByTypo(tipologia)
        List<Project> ret=null
        GetActiveClassificationSchemeResponse titolarioAttivo = getActiveClassificationScheme(config,authToken)
        String idClassificationScheme = ""
        if(titolarioAttivo.ClassificationSchemes!=null && titolarioAttivo.ClassificationSchemes.size()>0) {
            idClassificationScheme = titolarioAttivo.ClassificationSchemes[0].Id
            SearchProjectRequest request = new SearchProjectRequest()
            request.Filters = new ArrayList<Filter>()
            request.Filters.add(new Filter(FilterProjectType.CLASSIFICATION_SCHEME, idClassificationScheme))
            request.Filters.add(new Filter(FilterProjectType.TYPE_PROJECT, projectType))
            request.Filters.add(new Filter(FilterProjectType.STATE, Project.STATE_OPEN))
            if(codice!=null && codice.length()>0){
                if(codice.indexOf("/")<0){
                    //cerco per codice classifica
                    request.Filters.add(new Filter(FilterProjectType.CLASSIFICATION_CODE, codice))
                    request.Filters.add(new Filter(FilterProjectType.REGISTER, registro))
                }else{
                    //cerco per codice fascicolo
                    request.Filters.add(new Filter(FilterProjectType.PROJECT_CODE, codice))
                }
            }
            if(descrizione!=null && descrizione.length()>3){
                request.Filters.add(new Filter(FilterProjectType.PROJECT_DESCRIPTION, descrizione))
            }
            if(anno>0){
                request.Filters.add(new Filter(FilterProjectType.YEAR, String.valueOf(anno)))
            }
            request.ElementsInPage=0
            request.PageNumber=0
            SearchProjectResponse response = searchProject(config, request, authToken)
            ret = response.Projects
        }
        return ret
    }


    List<Correspondent> cercaCorrespondent(String codice,boolean exact, String descrizione, String correspondentType, boolean typeGroupOffices, boolean typeGroupUsers, boolean typeGroupRoles, String registryOrRF, PiTreConfig config, String authToken) {
        List<Correspondent> ret = null

        SearchCorrespondentsRequest request = new SearchCorrespondentsRequest()
        request.Filters = new ArrayList<Filter>()
        if (descrizione != null && descrizione.length() > 0) {
            request.Filters.add(new Filter(FilterCorrespondentType.DESCRIPTION, descrizione))
        }
        if (codice != null && codice.length() > 0) {
            if(exact)
                request.Filters.add(new Filter(FilterCorrespondentType.EXACT_CODE, codice))
            else
                request.Filters.add(new Filter(FilterCorrespondentType.CODE, codice))
        }
        request.Filters.add(new Filter(FilterCorrespondentType.TYPE, correspondentType))

        if(typeGroupOffices)
            request.Filters.add(new Filter(FilterCorrespondentType.OFFICES, "TRUE"))
        if(typeGroupUsers)
            request.Filters.add(new Filter(FilterCorrespondentType.USERS, "TRUE"))
        if(typeGroupRoles)
            request.Filters.add(new Filter(FilterCorrespondentType.ROLES, "TRUE"))

        request.Filters.add(new Filter("COMMON_ADDRESSBOOK", "TRUE"))
        if(registryOrRF!=null && registryOrRF.length()>0){
            request.Filters.add(new Filter(FilterCorrespondentType.REGISTRY_OR_RF, registryOrRF))
        }
        request.Filters.add(new Filter(FilterCorrespondentType.EXTRACT_DETAILS,"TRUE"))

        SearchCorrespondentsResponse response = searchCorrespondent(config, request, authToken)
        ret = response.Correspondents
        return ret
    }

    @Override
    @Transactional
    void protocolla(IProtocollabile atto) {
        // ottengo il lock pessimistico per evitare doppie protocollazioni.
        atto.lock()
        if (atto.numeroProtocollo > 0) {
            throw new AttiRuntimeException("Il documento è già protocollato!");
        }

        if (atto.idApplicativoEsterno!=null && atto.idApplicativoEsterno > 0) {
            log.info("idApplicativoEsterno="+atto.idApplicativoEsterno)
            return
        }

        // controllo campi obbligatori

        if(atto instanceof Determina){
            if( (atto.idFascicoloEsterno==null || atto.idFascicoloEsterno.length()==0)){
                throw new AttiRuntimeException("E' obbligatorio indicare un fascicolo!");
            }
        }

        if(atto instanceof Delibera){
            if( (atto.propostaDelibera.idFascicoloEsterno==null || atto.propostaDelibera.idFascicoloEsterno.length()==0)){
                throw new AttiRuntimeException("E' obbligatorio indicare un fascicolo!");
            }
        }

        if(checkMostraControlliTemplate(PiTreField.MACROCATEGORIA,String.valueOf(atto.getTipologiaDocumento().id)) && (atto.macrocategoria==null || atto.macrocategoria.length()==0) ){
            throw new AttiRuntimeException("E' obbligatorio selezionare una Macrocategoria!");
        }



        // creo il token di protocollazione: se lo trovo ed ha successo, vuol dire che ho già protocollato:
        TokenIntegrazione token = tokenIntegrazioneService.beginTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO);
        if (token.isStatoSuccesso()) {
            // significa che ho già protocollato: prendo il numero di protocollo, lo assegno al documento ed esco:
            def map = Eval.me(token.dati);

            if(map.idApplicativoProtocollo!=null && map.idApplicativoProtocollo.length()>0){
                // è stato creato il documento di tipo G da non protocollare
                atto.idApplicativoEsterno=map.idApplicativoProtocollo
            } // ##83638
            if(map.numero!=null && map.numero>0) {
                atto.numeroProtocollo = map.numero;
                atto.annoProtocollo = map.anno;
                atto.dataNumeroProtocollo = Date.parse("dd/MM/yyyy", map.data);
            }


            atto.save()

            // allineo il documento gdm:
            gestoreDocumentaleEsterno.salvaDocumento(atto);

            //fascicola(atto);

            // elimino il token: tutto è andato bene e verrà eliminato solo alla commit sull transaction principale
            tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
            return;
        }

        try {
            String unitaProtocollante=atto.getUnitaProponente().codice
            PiTreConfig config = new PiTreConfig()
            if(protocolloPiTreConfig.piTreConfig==null){
                config.pathCertificate=protocolloPiTreConfig.getPathCertificato()
                config.cacert_path=protocolloPiTreConfig.getPathCacert()
                config.endPointUrl=protocolloPiTreConfig.getUrlWebService()
                config.passwd=protocolloPiTreConfig.getPswdCertificato()
                config.CodeApplication=protocolloPiTreConfig.getCodeApplication()
                config.CodeAdm=protocolloPiTreConfig.getCodeAdm()
                //config.Username=protocolloPiTreConfig.getUnitaUsername(unitaProtocollante)
                if(protocolloPiTreConfig.getCodeRole().equals( "-"))
                    config.CodeRole=""
                else
                    config.CodeRole=protocolloPiTreConfig.getCodeRole()
                config.passwd_cacert=protocolloPiTreConfig.getPswdCacert()
                config.getSSLContext()
                protocolloPiTreConfig.piTreConfig=config
            }

            //CreateDocumentAndAddInProjectResponse response = creaProtocollo(protocolloPiTreConfig, atto)
            ProtocolPredisposedResponse response = creaProtocolloScomposto(protocolloPiTreConfig,atto)

            if(response.Code==0) {
                atto.idApplicativoEsterno=response.Document.Id   // #83638  scrivo sempre idApplicativoEsterno
                if(response.Document.ProtocolNumber!=null && response.Document.ProtocolNumber.length()>0) {
                    tokenIntegrazioneService.setTokenSuccess("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO,
                            "[numero:${Integer.parseInt(response.Document.ProtocolNumber)}, anno:${Integer.parseInt(response.Document.ProtocolYear)}, data:'${new Date().format("dd/MM/yyyy")}']")

                    atto.numeroProtocollo = Integer.parseInt(response.Document.ProtocolNumber)
                    atto.annoProtocollo = Integer.parseInt(response.Document.ProtocolYear)
                    atto.dataNumeroProtocollo = new Date() //volendo c'è anche questa  -> response.Document.ProtocolDate

                }else{
                    tokenIntegrazioneService.setTokenSuccess("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO,"[idApplicativoProtocollo:"+response.Document.Id + "]")

                }

                atto.save()

                // elimino il token: questo avverrà solo se la transazione "normale" di grails andrà a buon fine:
                tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
            }else{
                throw new Exception("[P4] "+response.Code + ":"+ response.ErrorMessage)
            }

        } catch (Exception e) {
            log.error("Errore nella chiamata alla protocollazione: ${e.getMessage()}", e);
            // elimino il token (solo in caso di errore successivo la protocollazione) e interrompo la transazione del token (così si possono fare più tentativi)
            tokenIntegrazioneService.stopTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO);
            throw new AttiRuntimeException("[P4] Errore in fase di protocollazione: ${e.getMessage()}.", e);
        }
    }

    CreateProjectResponse creaFascicolo(String codice,String descrizione,boolean riservato, So4UnitaPubb unitaProponente, Ad4Utente utenteRedattore){
        CreateProjectResponse ret=null
        if( (codice!=null && codice.length()>0 )
                || (descrizione!=null && descrizione.length()>4)){
            PiTreConfig config = new PiTreConfig()
            if(protocolloPiTreConfig.piTreConfig==null){
                config.pathCertificate=protocolloPiTreConfig.getPathCertificato()
                config.cacert_path=protocolloPiTreConfig.getPathCacert()
                config.endPointUrl=protocolloPiTreConfig.getUrlWebService()
                config.passwd=protocolloPiTreConfig.getPswdCertificato()
                config.CodeApplication=protocolloPiTreConfig.getCodeApplication()
                config.CodeAdm=protocolloPiTreConfig.getCodeAdm()
                //config.Username=protocolloPiTreConfig.getUnitaUsername(unitaProtocollante)
                if(protocolloPiTreConfig.getCodeRole().equals( "-")) {
                    config.CodeRole = ""
                }else {
                    config.CodeRole = protocolloPiTreConfig.getCodeRole()
                }
                config.passwd_cacert=protocolloPiTreConfig.getPswdCacert()
                Date prima = new Date()
                config.getSSLContext()
                Date dopo = new Date()
                log.info("TIME=" + (dopo.getTime() - prima.getTime()).toString())
                protocolloPiTreConfig.piTreConfig=config
            }
            //anche qui inserire il servizio TODO
            //tra tutte le unità dell'utente quella istituzionale #83636
            So4UnitaPubb unitaServizioIstituzionale =calcoloUnitaServizioIstituzionaleDelRedattore(null,utenteRedattore)
            String authToken = prendiToken(protocolloPiTreConfig.piTreConfig, unitaServizioIstituzionale)

            GetActiveClassificationSchemeResponse titolarioAttivo = getActiveClassificationScheme(protocolloPiTreConfig.piTreConfig, authToken)
            ClassificationScheme classificationScheme
            if (titolarioAttivo.ClassificationSchemes != null && titolarioAttivo.ClassificationSchemes.size() > 0) {
                classificationScheme = titolarioAttivo.ClassificationSchemes[0]
            }

            CreateProjectRequest request = new CreateProjectRequest()
            Project project = new Project()
            project.ClassificationScheme = classificationScheme
            project.CodeNodeClassification = codice
            project.Description = descrizione
            project.Private = riservato
            request.Project = project
            ret = createProject(protocolloPiTreConfig.piTreConfig, request, authToken)

        }
        return ret
    }

    @Transactional
    List<Fascicolo> getListaFascicoli(String codice, String descrizione, int anno,So4UnitaPubb unitaProponente, String tipologia, Ad4Utente utenteRedattore) {
        List<Fascicolo> ret = new ArrayList<Fascicolo>()

        if( (codice!=null && codice.length()>0 )
                || (descrizione!=null && descrizione.length()>3)
                || (descrizione!=null && descrizione.length()>3 && anno>0)){

            PiTreConfig config = new PiTreConfig()
            if(protocolloPiTreConfig.piTreConfig==null){
                config.pathCertificate=protocolloPiTreConfig.getPathCertificato()
                config.cacert_path=protocolloPiTreConfig.getPathCacert()
                config.endPointUrl=protocolloPiTreConfig.getUrlWebService()
                config.passwd=protocolloPiTreConfig.getPswdCertificato()
                config.CodeApplication=protocolloPiTreConfig.getCodeApplication()
                config.CodeAdm=protocolloPiTreConfig.getCodeAdm()
                //config.Username=protocolloPiTreConfig.getUnitaUsername(unitaProtocollante)
                if(protocolloPiTreConfig.getCodeRole().equals( "-"))
                    config.CodeRole=""
                else
                    config.CodeRole=protocolloPiTreConfig.getCodeRole()
                config.passwd_cacert=protocolloPiTreConfig.getPswdCacert()
                Date prima = new Date()
                config.getSSLContext()
                Date dopo = new Date()
                log.info("TIME=" + (dopo.getTime() - prima.getTime()).toString())
                protocolloPiTreConfig.piTreConfig=config
            }
            //al posto dell'unità proponente indicare il servizio #83636
            So4UnitaPubb unitaServizioIstituzionale =calcoloUnitaServizioIstituzionaleDelRedattore(null,utenteRedattore)
            String authToken = prendiToken(protocolloPiTreConfig.piTreConfig, unitaServizioIstituzionale)
            List<Project> projectList = cercaProject(codice, descrizione, anno, Project.TYPE_PROCEDIMENTALE, protocolloPiTreConfig.piTreConfig, authToken, tipologia)
            for (Project project in projectList) {
                Fascicolo fascicolo = new Fascicolo()
                fascicolo.numero = project.Code
                fascicolo.riservato = project.Private
                fascicolo.oggetto = project.Description
                fascicolo.riservato = project.Private
                fascicolo.sub = project.Id
                ret.add(fascicolo)
            }
        }
        return ret
    }

    /*As4SoggettoCorrente getRedattore(IDocumentoIterabile documentoIterabile){
        As4SoggettoCorrente ret=null
        Ad4Utente utente=null
        if (documentoIterabile instanceof IAtto) {
            utente = documentoIterabile.proposta.getSoggetto(TipoSoggetto.REDATTORE)?.utenteAd4
        } else {
            utente = documentoIterabile.getSoggetto(TipoSoggetto.REDATTORE)?.utenteAd4
        }
        if(utente) {
            ret = As4SoggettoCorrente.findByUtenteAd4(utente)
        }
        return retDeliberaViewModel
    }*/

    @Transactional
    void inviaTrasmetti(IProtocollabile atto){
        So4UnitaPubb unitaProponente = atto.unitaProponente
        PiTreConfig config = new PiTreConfig()
        if(protocolloPiTreConfig.piTreConfig==null){
            config.pathCertificate=protocolloPiTreConfig.getPathCertificato()
            config.cacert_path=protocolloPiTreConfig.getPathCacert()
            config.endPointUrl=protocolloPiTreConfig.getUrlWebService()
            config.passwd=protocolloPiTreConfig.getPswdCertificato()
            config.CodeApplication=protocolloPiTreConfig.getCodeApplication()
            config.CodeAdm=protocolloPiTreConfig.getCodeAdm()
            //config.Username=protocolloPiTreConfig.getUnitaUsername(unitaProtocollante)
            if(protocolloPiTreConfig.getCodeRole().equals( "-"))
                config.CodeRole=""
            else
                config.CodeRole=protocolloPiTreConfig.getCodeRole()
            config.passwd_cacert=protocolloPiTreConfig.getPswdCacert()
            Date prima = new Date()
            config.getSSLContext()
            Date dopo = new Date()
            log.info("TIME=" + (dopo.getTime() - prima.getTime()).toString())
            protocolloPiTreConfig.piTreConfig=config
        }
        String authToken = prendiToken(protocolloPiTreConfig.piTreConfig,unitaProponente)
        So4UnitaPubb servizio = strutturaOrganizzativaService.getServizio(unitaProponente)

        //invioMail
        if(atto instanceof Determina){
            try {
                SendDocumentAdvancedRequest requestInvio = new SendDocumentAdvancedRequest()
                requestInvio.IdDocument = atto.idApplicativoEsterno
                requestInvio.CodeRegister = protocolloPiTreConfig.getRfeByCodiceUnita(servizio.codice)
                SendDocumentAdvancedResponse sendDocumentAdvancedResponse = sendDocumentAdvanced(protocolloPiTreConfig.piTreConfig, requestInvio, authToken)
            }catch (Throwable th){
                println th.message
            }
        }

        //trasmettiNotifica Mittente
        try {
            ExecuteTransmissionDocumentRequest requestTrasmetti = new ExecuteTransmissionDocumentRequest()
            requestTrasmetti.IdDocument = atto.idApplicativoEsterno
            requestTrasmetti.TransmissionReason = "COMPETENZA"
            Correspondent receiver = new Correspondent()
            receiver.Code = protocolloPiTreConfig.getUsernameByCodiceUnita(servizio.codice)
            requestTrasmetti.Receiver = receiver
            requestTrasmetti.Notify = true
            ExecuteTransmissionDocumentResponse executeTransmissionDocumentResponse = executeTransmissionDocument(protocolloPiTreConfig.piTreConfig, requestTrasmetti, authToken)
        }catch (Throwable th){
            println th.message
        }

        if(atto instanceof Determina){
            for(DestinatarioNotifica destinatarioNotifica in atto.destinatariNotifiche){
                if(destinatarioNotifica!=null && destinatarioNotifica.tipoDestinatario.equals(TIPO_DESTINATARI_NOTIFICHE)){
                    if(destinatarioNotifica.idSoggettoEsterno!=null
                            && destinatarioNotifica.idSoggettoEsterno.length()>0
                            ){
                        //trasmettiNotifica Destinatario Interno
                        try {
                            ExecuteTransmissionDocumentRequest requestTrasmettiDestinatario = new ExecuteTransmissionDocumentRequest()
                            requestTrasmettiDestinatario.IdDocument = atto.idApplicativoEsterno
                            requestTrasmettiDestinatario.TransmissionReason = "COMPETENZA"
                            Correspondent receiverDestinatario = new Correspondent()
                            receiverDestinatario.Id = destinatarioNotifica.idSoggettoEsterno
                            requestTrasmettiDestinatario.Receiver = receiverDestinatario
                            requestTrasmettiDestinatario.Notify = true
                            ExecuteTransmissionDocumentResponse executeTransmissionDocumentDestinatarioResponse = executeTransmissionDocument(protocolloPiTreConfig.piTreConfig, requestTrasmettiDestinatario, authToken)
                        }catch(Throwable th){
                            println th.message
                        }
                    }
                }
            }
        }
        if(atto instanceof Delibera){
            for(DestinatarioNotifica destinatarioNotifica in atto.propostaDelibera.destinatariNotifiche){
                if(destinatarioNotifica!=null && destinatarioNotifica.tipoDestinatario.equals(TIPO_DESTINATARI_NOTIFICHE)){
                    if(destinatarioNotifica.idSoggettoEsterno!=null
                            && destinatarioNotifica.idSoggettoEsterno.length()>0
                            ){
                        //trasmettiNotifica Destinatario Interno
                        try {
                            ExecuteTransmissionDocumentRequest requestTrasmettiDestinatario = new ExecuteTransmissionDocumentRequest()
                            requestTrasmettiDestinatario.IdDocument = atto.idApplicativoEsterno
                            requestTrasmettiDestinatario.TransmissionReason = "COMPETENZA"
                            Correspondent receiverDestinatario = new Correspondent()
                            receiverDestinatario.Id = destinatarioNotifica.idSoggettoEsterno
                            requestTrasmettiDestinatario.Receiver = receiverDestinatario
                            requestTrasmettiDestinatario.Notify = true
                            ExecuteTransmissionDocumentResponse executeTransmissionDocumentDestinatarioResponse = executeTransmissionDocument(protocolloPiTreConfig.piTreConfig, requestTrasmettiDestinatario, authToken)
                        }catch(Throwable th){
                            println th.message
                        }
                    }
                }
            }
        }



    }

    @Transactional
    List<Correspondent> getListaCorrispondenti(String tipo, boolean typeGroupOffices, boolean typeGroupUsers, boolean typeGroupRoles, String codice, String denominazione, So4UnitaPubb unitaProponente, CodiceDescrizione rubrica) {
        List<Correspondent> ret = new ArrayList<Correspondent>()
        String RF=""
        if( (denominazione!=null && denominazione.length()>=3 ) || (codice!=null && codice.length()>=3) ){
            PiTreConfig config = new PiTreConfig()
            if(protocolloPiTreConfig.piTreConfig==null){
                config.pathCertificate=protocolloPiTreConfig.getPathCertificato()
                config.cacert_path=protocolloPiTreConfig.getPathCacert()
                config.endPointUrl=protocolloPiTreConfig.getUrlWebService()
                config.passwd=protocolloPiTreConfig.getPswdCertificato()
                config.CodeApplication=protocolloPiTreConfig.getCodeApplication()
                config.CodeAdm=protocolloPiTreConfig.getCodeAdm()
                //config.Username=protocolloPiTreConfig.getUnitaUsername(unitaProtocollante)
                if(protocolloPiTreConfig.getCodeRole().equals( "-")) {
                    config.CodeRole = ""
                } else {
                    config.CodeRole = protocolloPiTreConfig.getCodeRole()
                }
                config.passwd_cacert=protocolloPiTreConfig.getPswdCacert()
                Date prima = new Date()
                config.getSSLContext()
                Date dopo = new Date()
                log.info("TIME=" + (dopo.getTime() - prima.getTime()).toString())
                protocolloPiTreConfig.piTreConfig=config
            }
            String authToken = prendiToken(protocolloPiTreConfig.piTreConfig,unitaProponente)

            //String RF = protocolloPiTreConfig.getRfeByCodiceUnita(servizio.codice)

            if(rubrica.descrizione.equals(ProtocolloPiTre.RUBRICA_DEI_SERVIZI)){
                So4UnitaPubb servizio = strutturaOrganizzativaService.getServizio(unitaProponente)
                RF = (tipo.equals("INTERNAL")?"": protocolloPiTreConfig.getRfeByCodiceUnita(servizio.codice))
            } else {
                RF=rubrica.codice
            }


            ret = cercaCorrespondent(codice,false,denominazione,tipo,typeGroupOffices,typeGroupUsers,typeGroupRoles,RF,protocolloPiTreConfig.piTreConfig,authToken)
        }
        return ret
    }



    ///*****************************///


    CreateDocumentResponse creaDocumento(ProtocolloPiTreConfig mapping,IProtocollabile atto,String authTokenCreazione,So4UnitaPubb unitaServizioIstituzionaleDelRedattore){
        String registro=protocolloPiTreConfig.getRegistroByTypo(String.valueOf(atto.getTipologiaDocumento().id))
        CreateDocumentResponse ret
        So4UnitaPubb unitaProtocollante=atto.getUnitaProponente()
        //So4UnitaPubb servizioProponente = strutturaOrganizzativaService.getServizio(unitaProtocollante)
        //String authToken = prendiToken(mapping.piTreConfig,unitaProtocollante)

        So4UnitaPubb servizioUnitaIstituzionaleDelRedattore = strutturaOrganizzativaService.getServizio(unitaServizioIstituzionaleDelRedattore)
        CreateDocumentRequest request = new CreateDocumentRequest()

        request.CodeRegister=registro
        So4UnitaPubb servizio = strutturaOrganizzativaService.getServizio(unitaProtocollante)
        request.CodeRF= protocolloPiTreConfig.getRfeByCodiceUnita(servizioUnitaIstituzionaleDelRedattore.codice)

        DocumentShort nuovoDocumento= new DocumentShort()

        nuovoDocumento.Object=atto.oggetto
        nuovoDocumento.Predisposed = true
        nuovoDocumento.DocumentType=Document.TIPO_DOCUMENTO_IN_PARTENZA   // il movimento è sempre in PARTENZA

        //Inserire Sender con unità proponente servizio associata
        String unitaProponente=protocolloPiTreConfig.getUsernameByCodiceUnita(servizio.codice)
        List<Correspondent> senders = cercaCorrespondent(unitaProponente,true,"",Correspondent.TYPE_INTERNAL,true,true,true, null,mapping.piTreConfig,authTokenCreazione)
        if(senders!=null && senders.size()>0){
            nuovoDocumento.Sender=senders[0]
        }

        try{
            String idTemplate = protocolloPiTreConfig.getTemplateByTypo(String.valueOf(atto.getTipologiaDocumento().id))
            PiTreTemplate currentTemplate = PiTreTemplate.get(Integer.valueOf(idTemplate))
            if(currentTemplate!=null){
                nuovoDocumento.Recipients = new ArrayList<Correspondent>(    )
                if(currentTemplate.protocolla){    //#83638
                    //Ai destinatari aggiungo sempre l'ufficio proponente, di base il movimento è INTERNO
                    nuovoDocumento.Recipients.add(nuovoDocumento.Sender)
                    //nuovoDocumento.DocumentType=Document.TIPO_DOCUMENTO_INTERNO
                }else{
                    nuovoDocumento.DocumentType=Document.TIPO_DOCUMENTO_NON_PROTOCOLLATO
                }
                //Aggiungo i destinatari sempre #83638


                if(atto instanceof Determina && atto.destinatariNotifiche?.size()>0){
                        //Se ci sono destinatari il movimento diventa in partenza
                        //nuovoDocumento.DocumentType=Document.TIPO_DOCUMENTO_IN_PARTENZA
                        for(DestinatarioNotifica destinatarioNotifica in atto.destinatariNotifiche){
                            if(destinatarioNotifica!=null && destinatarioNotifica.tipoDestinatario.equals(TIPO_DESTINATARI_NOTIFICHE)){
                                if(destinatarioNotifica.idSoggettoEsterno!=null && destinatarioNotifica.idSoggettoEsterno.length()>0){
                                    Correspondent destinatario = new Correspondent()
                                    destinatario.Id=destinatarioNotifica.idSoggettoEsterno
                                    destinatario.Type=destinatarioNotifica.tipoSoggettoEsterno
                                    destinatario.CorrespondentType=destinatarioNotifica.tipoCorrispondenteEsterno
                                    nuovoDocumento.Recipients.add(destinatario)
                                }
                            }
                        }
                }
                if(atto instanceof Delibera && atto.propostaDelibera.destinatariNotifiche.size()>0){
                    for(DestinatarioNotifica destinatarioNotifica in atto.propostaDelibera.destinatariNotifiche){
                        if(destinatarioNotifica!=null && destinatarioNotifica.tipoDestinatario.equals(TIPO_DESTINATARI_NOTIFICHE)){
                            if(destinatarioNotifica.idSoggettoEsterno!=null && destinatarioNotifica.idSoggettoEsterno.length()>0){
                                Correspondent destinatario = new Correspondent()
                                destinatario.Id=destinatarioNotifica.idSoggettoEsterno
                                destinatario.Type=destinatarioNotifica.tipoSoggettoEsterno
                                destinatario.CorrespondentType=destinatarioNotifica.tipoCorrispondenteEsterno
                                nuovoDocumento.Recipients.add(destinatario)
                            }
                        }
                    }
                }

                nuovoDocumento.Template=elaboraTemplate(atto,currentTemplate,registro,request.CodeRF)
            } else{
                nuovoDocumento.DocumentType=Document.TIPO_DOCUMENTO_NON_PROTOCOLLATO
            }
        }catch (Exception ex){
            log.info("Template non esistente o non caricato correttamente")
        }

        nuovoDocumento.PrivateDocument=atto.riservato

        request.Document=nuovoDocumento
        ret =createDocument(mapping.piTreConfig,request,authTokenCreazione)
        return ret
    }

    public static TemplateShort elaboraTemplate(IProtocollabile atto, PiTreTemplate currentTemplate,String registro,String codeRegisterOrRF){
        TemplateShort ret = null
        String retString= currentTemplate.modello
        retString=retString.replace("[REGISTRO]",registro)
        retString=retString.replace("[CODE_REGISTER_OR_RF]",codeRegisterOrRF==null?"":codeRegisterOrRF)
        for (PiTreField field: currentTemplate.fields){
            String valore=""
            if(field.codice.equals(PiTreField.ATTO_SOGGETTO_CONTROLLO)){
                if(atto.attoSoggettoControllo) {
                    valore="SI"
                }else{
                    valore="NO"
                }
            }
            if(field.codice.equals(PiTreField.MACROCATEGORIA)){
                valore=atto.macrocategoria
            }
            if(field.codice.equals(PiTreField.NUMERO_ATTO)){
                if(atto instanceof Delibera || atto instanceof Determina){
                    valore=atto.getNumero()
                }
            }
            if(field.codice.equals(PiTreField.DATA_SEDUTA)){
                if (atto instanceof Delibera ){
                    valore=atto.getDataAtto().format("dd/MM/yyyy")
                }
            }
            retString=retString.replace("["+field.codice+"]",valore)
        }
        log.info("TemplateShort= "+retString)
        ObjectMapper objectMapper = new ObjectMapper()
        ret = objectMapper.readValue(retString, TemplateShort.class)
        return ret
    }

    GenericResponse uploadFile(ProtocolloPiTreConfig mapping,String authToken, IProtocollabile atto, String idDocument){
        GenericResponse ret = new GenericResponse()
        ret.ResultMessage="OK"
        ret.Code=0

        if (atto.testo != null) {
            UploadFileToDocumentRequest requestFilePrincipale = new UploadFileToDocumentRequest()
            File tempFile = new File()
            tempFile = new File()
            tempFile.Content = Base64.encodeBase64String(IOUtils.toByteArray(gestoreFile.getFile(atto, atto.testo)))
            tempFile.Name = atto.testo.nome
            requestFilePrincipale.IdDocument=idDocument
            requestFilePrincipale.Description="File Principale"
            requestFilePrincipale.CreateAttachment=false
            requestFilePrincipale.File=tempFile

            GenericResponse responseFilePrincipale= uploadFileToDocument(mapping.piTreConfig,requestFilePrincipale,authToken)
            if(responseFilePrincipale.Code==0){

                List<File> allegatiAtto = new ArrayList<File>()
                //aggiungo gli allegati dell'atto
                if (atto.allegati != null) {
                    for (Allegato allegato in atto.allegati) {

                        for (FileAllegato fileAllegato in allegato.fileAllegati) {
                            File tempAllegato = new File()
                            tempAllegato.Name = fileAllegato.nome
                            tempAllegato.Content = Base64.encodeBase64String(IOUtils.toByteArray(gestoreFile.getFile(allegato, fileAllegato)))
                            tempAllegato.Description = allegato.titolo
                            allegatiAtto.add(tempAllegato)

                        }
                    }
                }

                if (atto instanceof Determina || atto instanceof Delibera) {
                    if (atto instanceof Delibera) {
                        // Aggiungo gli allegati dei pareri della proposta:
                        for (VistoParere visto : atto.propostaDelibera.visti) {
                            if (visto.valido) {
                                // aggiungo il testo del visto
                                if (visto.testo != null) {
                                    File tempAllegato = new File()
                                    tempAllegato.Name = visto.testo.nome
                                    tempAllegato.Content = Base64.encodeBase64String(IOUtils.toByteArray(gestoreFile.getFile(visto, visto.testo)))
                                    tempAllegato.Description = visto.tipologia.titolo
                                    allegatiAtto.add(tempAllegato)
                                }
                                // aggiungo gli allegati del visto
                                for (Allegato allegato in visto.allegati) {

                                    for (FileAllegato fileAllegato in allegato.fileAllegati) {
                                        File tempAllegato = new File()
                                        tempAllegato.Name = fileAllegato.nome
                                        tempAllegato.Content = Base64.encodeBase64String(IOUtils.toByteArray(gestoreFile.getFile(allegato, fileAllegato)))
                                        tempAllegato.Description = allegato.descrizione
                                        allegatiAtto.add(tempAllegato)
                                    }

                                }
                            }
                        }
                    }

                    // Aggiungo gli allegati dei visti:
                    for (VistoParere visto : atto.visti) {
                        if (visto.valido) {
                            // aggiungo il testo del visto
                            if (visto.testo != null) {
                                File tempAllegato = new File()
                                tempAllegato.Name = visto.testo.nome
                                tempAllegato.Content = Base64.encodeBase64String(IOUtils.toByteArray(gestoreFile.getFile(visto, visto.testo)))
                                tempAllegato.Description = visto.tipologia.titolo
                                allegatiAtto.add(tempAllegato)
                            }
                            // aggiungo gli allegati del visto
                            for (Allegato allegato in visto.allegati) {

                                for (FileAllegato fileAllegato in allegato.fileAllegati) {
                                    File tempAllegato = new File()
                                    tempAllegato.Name = fileAllegato.nome
                                    tempAllegato.Content = Base64.encodeBase64String(IOUtils.toByteArray(gestoreFile.getFile(allegato, fileAllegato)))
                                    tempAllegato.Description = allegato.descrizione
                                    allegatiAtto.add(tempAllegato)
                                }

                            }
                        }
                    }

                    // Aggiungo gli allegati dei certificati:
                    for (Certificato certificato : atto.certificati) {
                        if (certificato.valido && certificato.testo != null) {
                            // aggiungo il testo del visto
                            if (certificato.testo != null) {
                                File tempAllegato = new File()
                                tempAllegato.Name = certificato.testo.nome
                                tempAllegato.Content = Base64.encodeBase64String(IOUtils.toByteArray(gestoreFile.getFile(certificato, certificato.testo)))
                                tempAllegato.Description = certificato.tipologia.titolo
                                allegatiAtto.add(tempAllegato)
                            }
                            for (Allegato allegato in certificato.allegati) {

                                for (FileAllegato fileAllegato in allegato.fileAllegati) {
                                    File tempAllegato = new File()
                                    tempAllegato.Name = fileAllegato.nome
                                    tempAllegato.Content = Base64.encodeBase64String(IOUtils.toByteArray(gestoreFile.getFile(allegato, fileAllegato)))
                                    tempAllegato.Description = allegato.descrizione
                                    allegatiAtto.add(tempAllegato)
                                }

                            }
                        }
                    }
                }

                for(File tmpFileAllegato in allegatiAtto){
                    UploadFileToDocumentRequest requestAllegato = new UploadFileToDocumentRequest()
                    requestAllegato.IdDocument=idDocument
                    requestAllegato.CreateAttachment=true
                    requestAllegato.Description="File Allegato"
                    requestAllegato.File=tmpFileAllegato

                    GenericResponse responseAllegato =  uploadFileToDocument(mapping.piTreConfig,requestAllegato,authToken)
                    if(responseAllegato.Code!=0){
                        ret.Code = responseAllegato.Code
                        ret.ErrorMessage = "Errore Allegato ("+ requestAllegato.File.Name+"): " +responseAllegato.ErrorMessage
                        break;
                    }
                }

            }else{
                ret.Code=responseFilePrincipale.Code
                ret.ErrorMessage="Errore uplodaFilePrincipale:"+ responseFilePrincipale.ErrorMessage
            }
        }
        return ret
    }



    ProtocolPredisposedResponse creaProtocolloScomposto(ProtocolloPiTreConfig mapping,IProtocollabile atto ) {
        log.info("[WS] Protocollazione inizio")
        long start = System.currentTimeMillis()
        ProtocolPredisposedResponse ret = new ProtocolPredisposedResponse()
        So4UnitaPubb unitaServizioIstituzionaleSingola = calcoloUnitaServizioIstituzionaleDelRedattore(atto, null)
        So4UnitaPubb unitaProtocollante = atto.getUnitaProponente()
        //CHECK unitaServizioIstituzionaleSingola se nullo sparare errore
        String authToken=null
        if (unitaServizioIstituzionaleSingola != null) {
        authToken = prendiToken(mapping.piTreConfig, unitaServizioIstituzionaleSingola)
            // Creo il documento con predisposed=true
            CreateDocumentResponse createDocumentResponse = creaDocumento(mapping, atto,authToken,unitaServizioIstituzionaleSingola)
            if (createDocumentResponse.Code == 0 && createDocumentResponse.Document != null && createDocumentResponse.Document.Id != null && createDocumentResponse.Document.Id.length() > 0) {
                //Procedo all inserimento del file principale ed eventuali allegati
                String idDocument = createDocumentResponse.Document.Id
                //salvare determina e delibera
                //atto.idApplicativoEsterno=idDocument
                GenericResponse uploadFileToDocument = uploadFile(mapping, authToken, atto, idDocument)
                if (uploadFileToDocument.Code == 0) {
                    //Procedo all inserimento in fascicolo principale
                    AddDocInProjectRequest requestAddDocInProject = new AddDocInProjectRequest()
                    requestAddDocInProject.IdDocument = idDocument
                    //distinguere tra Delibera e Detemrina
                    String idFascicoloEsternoPrincipale = ""
                    if (atto instanceof Determina) {
                        idFascicoloEsternoPrincipale = atto.idFascicoloEsterno
                    }
                    if (atto instanceof Delibera) {
                        idFascicoloEsternoPrincipale = atto.propostaDelibera.idFascicoloEsterno
                    }
                    requestAddDocInProject.IdProject = idFascicoloEsternoPrincipale

                    //fascicolazione principale
                    GenericResponse addDocInProjectResponse = addDocInProject(mapping.piTreConfig, requestAddDocInProject, authToken)

                    if (addDocInProjectResponse.Code == 0) {
                        //inserimento del Fascicolo Secondario se nella tipologia è specificato
                        // e se diverso dal fascicolo principale altrimenti da errore
                        String idFascicoloEsternoDaTipologia = null
                        if (atto instanceof Determina) {
                            idFascicoloEsternoDaTipologia = atto.getTipologiaDocumento().idFascicoloEsterno
                        }
                        if (atto instanceof Delibera) {
                            idFascicoloEsternoDaTipologia = atto.propostaDelibera.getTipologiaDocumento().idFascicoloEsterno
                        }

                        if (idFascicoloEsternoDaTipologia != null && !idFascicoloEsternoDaTipologia.equals(idFascicoloEsternoPrincipale)) {
                            //Procedo all inserimento in fascicolo secondario
                            AddDocInProjectRequest requestAddDocInProject2 = new AddDocInProjectRequest()
                            requestAddDocInProject2.IdDocument = idDocument
                            requestAddDocInProject2.IdProject = idFascicoloEsternoDaTipologia
                            // ARIMODIFICA SENZA SUPERUSER
                            //String authTokenFascicoloTipologia = prendiTokenByUsernameCodeRole(mapping.piTreConfig, mapping.getSuperUserFascicoloTipologia(), mapping.getSuperCodeRoleFascicoloTipologia())
                            GenericResponse addDocInProjectResponse2 = addDocInProject(mapping.piTreConfig, requestAddDocInProject2, authToken)
                            if (addDocInProjectResponse2.Code != 0) {
                                ret.Code = addDocInProjectResponse2.Code
                                ret.ErrorMessage = "addDocInProject2: " + addDocInProjectResponse2.ErrorMessage
                                return ret
                            }
                        }
                        //Effettuo la protocollazione
                        if (!createDocumentResponse.Document.DocumentType.equals(Document.TIPO_DOCUMENTO_NON_PROTOCOLLATO)) {
                            //se è un tipo documento da protocollare procedo con il predisposed
                            ProtocolPredisposedRequest predisposedRequest = new ProtocolPredisposedRequest()
                            predisposedRequest.IdDocument = idDocument
                            predisposedRequest.CodeRegister = protocolloPiTreConfig.getRegistroByTypo(String.valueOf(atto.getTipologiaDocumento().id))

                            So4UnitaPubb servizio = strutturaOrganizzativaService.getServizio(unitaServizioIstituzionaleSingola)
                            predisposedRequest.CodeRF = protocolloPiTreConfig.getRfeByCodiceUnita(servizio.codice)
                            ret = protocolPredisposed(mapping.piTreConfig, predisposedRequest, authToken)
                        } else {
                            //altrimenti se non è da protocollare ritorno  il predisposed con il documento dentro senza numero e anno
                            ret.Code = 0
                            ret.ErrorMessage = null
                            ret.Document = createDocumentResponse.Document
                        }
                    } else {
                        ret.Code = addDocInProjectResponse.Code
                        ret.ErrorMessage = "addDocInProject: " + addDocInProjectResponse.ErrorMessage
                    }
                } else {
                    ret.Code = uploadFileToDocument.Code
                    ret.ErrorMessage = "uploadFileToDocument: " + createDocumentResponse.ErrorMessage
                }
            } else {
                ret.Code = createDocumentResponse.Code
                ret.ErrorMessage = "createDocument: " + createDocumentResponse.ErrorMessage
            }
        } else {
            ret.Code = -1
            ret.ErrorMessage = "L'utente redattore non ha unità istituzionale assegnata!"
        }
        long delay = System.currentTimeMillis() - start
        log.info("[WS] Protocollazione fine (" + delay + ")")
        return ret
    }

    /**
     * getActiveClassificationScheme
     *
     * @param config
     * @param authToken
     * @return
     */
    GetActiveClassificationSchemeResponse getActiveClassificationScheme(PiTreConfig config,String authToken){
        log.info("[WS] getActiveClassificationScheme inizio")
        long start = System.currentTimeMillis()
        GetActiveClassificationSchemeResponse ret
        String response=PiTreClient.routeRestRequest(config,"GetActiveClassificationScheme",GetActiveClassificationSchemeRequest.METHOD,null,authToken,null)
        ObjectMapper objectMapper = new ObjectMapper()
        if(response.indexOf("ClassificationSchemes")>-1)
            ret = objectMapper.readValue(response, GetActiveClassificationSchemeResponse.class)
        else{
            GetClassificationSchemeByIdResponse getClassificationSchemeByIdResponse = objectMapper.readValue(response, GetClassificationSchemeByIdResponse.class)
            ret = new GetActiveClassificationSchemeResponse()
            ret.ErrorMessage=getClassificationSchemeByIdResponse.ErrorMessage
            ret.Code=getClassificationSchemeByIdResponse.Code
            ret.ClassificationSchemes=new ArrayList<ClassificationScheme>();
            if(getClassificationSchemeByIdResponse.ClassificationScheme!=null)
                ret.ClassificationSchemes.add(getClassificationSchemeByIdResponse.ClassificationScheme)
        }
        long delay = System.currentTimeMillis() - start
        log.info("[WS] getActiveClassificationScheme fine ("+ delay + ")")
        return ret
    }

    /**
     * GetToken
     *
     * @param request
     * @param config
     * @return
     */
    GetTokenResponse getToken(GetTokenRequest request, PiTreConfig config){
        log.info("[WS] getToken inizio")
        long start = System.currentTimeMillis()
        GetTokenResponse ret
        ObjectMapper objectMapper = new ObjectMapper()
        String postData=objectMapper.writeValueAsString(request)
        String response=PiTreClient.routeRestRequest(config,"GetToken",GetTokenRequest.METHOD,postData,null,null)
        ret = objectMapper.readValue(response, GetTokenResponse.class)
        long delay = System.currentTimeMillis() - start
        log.info("[WS] getToken fine ("+ delay + ")")
        return ret
    }

/**
 * Recupera Fascicolo
 *
 * @param config
 * @param classificationSchemeId
 * @param codeProject
 * @param authToken
 * @return
 */
    GetProjectResponse getProject(PiTreConfig config,String classificationSchemeId,String codeProject,String authToken){
        GetProjectResponse ret
        List<CodeValue> parametri = new ArrayList<CodeValue>();
        parametri.add(new CodeValue("classificationSchemeId",classificationSchemeId))
        parametri.add(new CodeValue("codeProject",codeProject))
        String response=PiTreClient.routeRestRequest(config,"GetProject",GetProjectRequest.METHOD,null,authToken,parametri)
        ObjectMapper objectMapper = new ObjectMapper()
        ret = objectMapper.readValue(response, GetProjectResponse.class)
        return ret
    }

    /**
     * Crea Fascicolo
     *
     * @param config
     * @param request
     * @param authToken
     * @return
     */
    CreateProjectResponse createProject(PiTreConfig config, CreateProjectRequest request, String authToken){
        log.info("[WS] createProject inizio")
        long start = System.currentTimeMillis()
        CreateProjectResponse ret
        ObjectMapper objectMapper = new ObjectMapper();
        String postData= objectMapper.writeValueAsString(request)
        String response=PiTreClient.routeRestRequest(config,"CreateProject", CreateProjectRequest.METHOD,postData,authToken,null)
        ret = objectMapper.readValue(response, CreateProjectResponse.class)
        long delay = System.currentTimeMillis() - start
        log.info("[WS] createProject fine ("+ delay + ")")
        return ret
    }
    /**
     * Creare un documento non protocollato (G)
     * DocumentType    String      Obbligatorio. Indica la tipologia del documento. Inserire il valore “G”.
     * Object          String      Obbligatorio. Oggetto del documento.
     * Note            Note[]      Opzionale. Possibilità di inserire una o più note nel documento.
     * Template        Template    Opzionale. Tipologia del documento che si vuole associare al documento con i relativi valori.
     * MainDocument    File        Opzionale. Documento principale.

     * Creare un documento protocollato in entrata (A)
     * DocumentType    String          Obbligatorio. Indica la tipologia del documento. Inserire il valore “A”.
     * Object          String          Obbligatorio. Oggetto del documento.
     * Note            Note[]          Opzionale. Possibilità di inserire una o più note nel documento.
     * Template        Template        Opzionale. Tipologia del documento che si vuole associare al documento con i relativi valori.
     * Sender          Correspondent   Obbligatorio. Indica il mittente del protocollo.
     * MultipleSenders Correspondent[] Opzionale. Lista di mittenti aggiuntivi.
     * MeansOfSending  String          Opzionale. Mezzo di spedizione.
     * ProtocolSender  String          Opzionale. Protocollo mittente.
     * ArrivalDate     String          Opzionale. Data di arrivo.
     * ArrivalTime     String          Opzionale. Ora di arrivo.
     * DataProtocolSender  String      Opzionale. Data protocollo mittente.
     * MainDocument    File            Opzionale. Documento principale.

     * Creare un documento protocollato in uscita (P)
     * DocumentType    String          Obbligatorio. Indica la tipologia del documento. Inserire il valore “P”.
     * Object          String          Obbligatorio. Oggetto del documento.
     * Note            Note[]          Opzionale. Possibilità di inserire una o più note nel documento.
     * Template        Template        Opzionale. Tipologia del documento che si vuole associare al documento con i relativi valori.
     * Sender          Correspondent   Opzionale. Indica il mittente del protocollo.
     * Recipients      Correspondent[] Obbligatorio. Destinatari del protocollo, inserirne almeno uno di tipo interno o esterno.
     * RecipientsCC    Correspondent[] Opzionale. Destinatari in conoscenza del protocollo di tipo interno o esterno.
     * MainDocument    File            Opzionale. Documento principale.
     *
     * Creare un documento protocollato interno (I)
     * DocumentType    String          Obbligatorio. Indica la tipologia del documento. Inserire il valore “I”.
     * Object          String          Obbligatorio. Oggetto del documento.
     * Note            Note[]          Opzionale. Possibilità di inserire una o più note nel documento.
     * Template        Template        Opzionale. Tipologia del documento che si vuole associare al documento con i relativi valori.
     * Sender          Correspondent   Obbligatorio. Indica il mittente del protocollo.
     * Recipients      Correspondent[] Obbligatorio. Destinatari del protocollo, inserirne almeno uno di tipo interno.
     * RecipientsCC    Correspondent[] Opzionale. Destinatari in conoscenza del protocollo di tipo interno.
     * MainDocument    File            Opzionale. Documento principale.
     *
     * @param config
     * @param request
     * @param authToken
     * @return
     */
    CreateDocumentResponse createDocument(PiTreConfig config, CreateDocumentRequest request,String authToken){
        log.info("[WS] createDocument inizio")
        long start = System.currentTimeMillis()
        CreateDocumentResponse ret
        ObjectMapper objectMapper = new ObjectMapper();
        String postData= objectMapper.writeValueAsString(request)
        String response=PiTreClient.routeRestRequest(config,"CreateDocument", CreateDocumentRequest.METHOD,postData,authToken,null)
        ret = objectMapper.readValue(response, CreateDocumentResponse.class)
        long delay = System.currentTimeMillis() - start
        log.info("[WS] createDocument fine ("+ delay + ")")
        return ret
    }

    /**
     * Creare un documento non protocollato (G)
     * DocumentType    String      Obbligatorio. Indica la tipologia del documento. Inserire il valore “G”.
     * Object          String      Obbligatorio. Oggetto del documento.
     * Note            Note[]      Opzionale. Possibilità di inserire una o più note nel documento.
     * Template        Template    Opzionale. Tipologia del documento che si vuole associare al documento con i relativi valori.
     * MainDocument    File        Opzionale. Documento principale.

     * Creare un documento protocollato in entrata (A)
     * DocumentType    String          Obbligatorio. Indica la tipologia del documento. Inserire il valore “A”.
     * Object          String          Obbligatorio. Oggetto del documento.
     * Note            Note[]          Opzionale. Possibilità di inserire una o più note nel documento.
     * Template        Template        Opzionale. Tipologia del documento che si vuole associare al documento con i relativi valori.
     * Sender          Correspondent   Obbligatorio. Indica il mittente del protocollo.
     * MultipleSenders Correspondent[] Opzionale. Lista di mittenti aggiuntivi.
     * MeansOfSending  String          Opzionale. Mezzo di spedizione.
     * ProtocolSender  String          Opzionale. Protocollo mittente.
     * ArrivalDate     String          Opzionale. Data di arrivo.
     * ArrivalTime     String          Opzionale. Ora di arrivo.
     * DataProtocolSender  String      Opzionale. Data protocollo mittente.
     * MainDocument    File            Opzionale. Documento principale.

     * Creare un documento protocollato in uscita (P)
     * DocumentType    String          Obbligatorio. Indica la tipologia del documento. Inserire il valore “P”.
     * Object          String          Obbligatorio. Oggetto del documento.
     * Note            Note[]          Opzionale. Possibilità di inserire una o più note nel documento.
     * Template        Template        Opzionale. Tipologia del documento che si vuole associare al documento con i relativi valori.
     * Sender          Correspondent   Opzionale. Indica il mittente del protocollo.
     * Recipients      Correspondent[] Obbligatorio. Destinatari del protocollo, inserirne almeno uno di tipo interno o esterno.
     * RecipientsCC    Correspondent[] Opzionale. Destinatari in conoscenza del protocollo di tipo interno o esterno.
     * MainDocument    File            Opzionale. Documento principale.
     *
     * Creare un documento protocollato interno (I)
     * DocumentType    String          Obbligatorio. Indica la tipologia del documento. Inserire il valore “I”.
     * Object          String          Obbligatorio. Oggetto del documento.
     * Note            Note[]          Opzionale. Possibilità di inserire una o più note nel documento.
     * Template        Template        Opzionale. Tipologia del documento che si vuole associare al documento con i relativi valori.
     * Sender          Correspondent   Obbligatorio. Indica il mittente del protocollo.
     * Recipients      Correspondent[] Obbligatorio. Destinatari del protocollo, inserirne almeno uno di tipo interno.
     * RecipientsCC    Correspondent[] Opzionale. Destinatari in conoscenza del protocollo di tipo interno.
     * MainDocument    File            Opzionale. Documento principale.
     *
     * @param config
     * @param request
     * @param authToken
     * @return
     */
    CreateDocumentAndAddInProjectResponse createDocumentAndAddInProject(PiTreConfig config, CreateDocumentAndAddInProjectRequest request,String authToken){
        CreateDocumentAndAddInProjectResponse ret
        ObjectMapper objectMapper = new ObjectMapper();
        String postData= objectMapper.writeValueAsString(request)
        String response=PiTreClient.routeRestRequest(config,"CreateDocumentAndAddInProject", CreateDocumentAndAddInProjectRequest.METHOD,postData,authToken,null)
        ret = objectMapper.readValue(response, CreateDocumentAndAddInProjectResponse.class)
        return ret
    }

    /**
     *  Per effettuare l’inserimento del documento in un sottofascicolo,
     * bisogna inserire nella richiesta nel parametro in ingresso CodeProject il codice del fascicolo,
     * inserendo quindi i caratteri separatori “//”, e di seguito la descrizione del sottofascicolo.
     * Un esempio di stringa accettabile può essere “6.2-2014//SottoFasc1”.
     * Per inserire il documento in un sottofascicolo non di primo livello bisogna inserire nel parametro
     * tutto il percorso per arrivare alla cartella desiderata.
     * Esempio “6.2-2014//SottoFasc1//SottoFasc2”.
     *
     * @param config
     * @param request
     * @param authToken
     * @return
     */
    GenericResponse addDocInProject(PiTreConfig config, AddDocInProjectRequest request, String authToken){
        log.info("[WS] addDocInProject inizio")
        long start = System.currentTimeMillis()
        GenericResponse ret
        ObjectMapper objectMapper = new ObjectMapper();
        String postData= objectMapper.writeValueAsString(request)
        String response=PiTreClient.routeRestRequest(config,"AddDocInProject", AddDocInProjectRequest.METHOD,postData,authToken,null)
        ret = objectMapper.readValue(response, GenericResponse.class)
        long delay = System.currentTimeMillis() - start
        log.info("[WS] addDocInProject fine ("+ delay + ")")
        return ret
    }

    private SearchProjectResponse searchProject(PiTreConfig config, SearchProjectRequest request, String authToken){
        log.info("[WS] searchProject inizio")
        long start = System.currentTimeMillis()
        SearchProjectResponse ret
        ObjectMapper objectMapper = new ObjectMapper();
        String postData= objectMapper.writeValueAsString(request)
        String response=PiTreClient.routeRestRequest(config,"SearchProjects", SearchProjectRequest.METHOD,postData,authToken,null)
        ret = objectMapper.readValue(response, SearchProjectResponse.class)
        long delay = System.currentTimeMillis() - start
        log.info("[WS] searchProject fine ("+ delay + ")")
        return ret
    }

    private SearchCorrespondentsResponse searchCorrespondent(PiTreConfig config, SearchCorrespondentsRequest request, String authToken){
        log.info("[WS] searchCorrespondent inizio")
        long start = System.currentTimeMillis()
        SearchCorrespondentsResponse ret
        ObjectMapper objectMapper = new ObjectMapper();
        String postData= objectMapper.writeValueAsString(request)
        String response=PiTreClient.routeRestRequest(config,"SearchCorrespondents", SearchCorrespondentsRequest.METHOD,postData,authToken,null)
        ret = objectMapper.readValue(response, SearchCorrespondentsResponse.class)
        long delay = System.currentTimeMillis() - start
        log.info("[WS] searchCorrespondent fine ("+ delay + ")")
        return ret
    }

    private GetRegisterOrRFResponse getRegisterOrRFResponse(PiTreConfig config, String codeRegister,String idRegister, String authToken){
        log.info("[WS] getRegisterOrRFResponse inizio")
        long start = System.currentTimeMillis()
        GetRegisterOrRFResponse ret
        ObjectMapper objectMapper = new ObjectMapper();
        List<CodeValue> parametri = new ArrayList<CodeValue>()
        if(codeRegister!=null && codeRegister.length()>0)
            parametri.add(new CodeValue("codeRegister",codeRegister))
        if(idRegister!=null && idRegister.length()>0)
            parametri.add(new CodeValue("idRegister",idRegister))
        String response=PiTreClient.routeRestRequest(config,"GetRegisterOrRF",GetRegisterOrRFRequest.METHOD,null,authToken,parametri)
        ret = objectMapper.readValue(response, GetRegisterOrRFResponse.class)
        long delay = System.currentTimeMillis() - start
        log.info("[WS] getRegisterOrRFResponse fine ("+ delay + ")")
        return ret
    }

    private GenericResponse  uploadFileToDocument(PiTreConfig config, UploadFileToDocumentRequest request, String authToken){
        log.info("[WS] uploadFileToDocument inizio")
        long start = System.currentTimeMillis()
        GenericResponse ret
        ObjectMapper objectMapper = new ObjectMapper()
        String postData= objectMapper.writeValueAsString(request)
        String response=PiTreClient.routeRestRequest(config,"UploadFileToDocument",UploadFileToDocumentRequest.METHOD,postData,authToken,null)
        ret = objectMapper.readValue(response, GenericResponse.class)
        long delay = System.currentTimeMillis() - start
        log.info("[WS] uploadFileToDocument fine ("+ delay + ")")
        return ret
    }

    private ProtocolPredisposedResponse  protocolPredisposed(PiTreConfig config, ProtocolPredisposedRequest request, String authToken){
        log.info("[WS] protocolPredisposed inizio")
        long start = System.currentTimeMillis()
        ProtocolPredisposedResponse ret
        ObjectMapper objectMapper = new ObjectMapper()
        String postData= objectMapper.writeValueAsString(request)
        String response=PiTreClient.routeRestRequest(config,"ProtocolPredisposed",ProtocolPredisposedRequest.METHOD,postData,authToken,null)
        ret = objectMapper.readValue(response, ProtocolPredisposedResponse.class)
        long delay = System.currentTimeMillis() - start
        log.info("[WS] protocolPredisposed fine ("+ delay + ")")
        return ret
    }

    private GenericResponse sendDocument(PiTreConfig config, SendDocumentRequest request, String authToken){
        log.info("[WS] sendDocument inizio")
        long start = System.currentTimeMillis()
        GenericResponse ret
        ObjectMapper objectMapper = new ObjectMapper()
        String postData= objectMapper.writeValueAsString(request)
        String response=PiTreClient.routeRestRequest(config,"SendDocument",SendDocumentRequest.METHOD,postData,authToken,null)
        ret = objectMapper.readValue(response, GenericResponse.class)
        long delay = System.currentTimeMillis() - start
        log.info("[WS] sendDocument fine ("+ delay + ")")
        return ret
    }

    private SendDocumentAdvancedResponse sendDocumentAdvanced(PiTreConfig config, SendDocumentAdvancedRequest request, String authToken){
        log.info("[WS] sendDocumentAdvanced inizio")
        long start = System.currentTimeMillis()
        SendDocumentAdvancedResponse ret
        ObjectMapper objectMapper = new ObjectMapper()
        String postData= objectMapper.writeValueAsString(request)
        String response=PiTreClient.routeRestRequest(config,"SendDocumentAdvanced",SendDocumentAdvancedRequest.METHOD,postData,authToken,null)
        ret = objectMapper.readValue(response, SendDocumentAdvancedResponse.class)
        long delay = System.currentTimeMillis() - start
        log.info("[WS] sendDocumentAdvanced fine ("+ delay + ")")
        return ret
    }

    private ExecuteTransmissionDocumentResponse executeTransmissionDocument(PiTreConfig config, ExecuteTransmissionDocumentRequest request, String authToken){
        log.info("[WS] executeTransmissionDocument inizio")
        long start = System.currentTimeMillis()
        ExecuteTransmissionDocumentResponse ret
        ObjectMapper objectMapper = new ObjectMapper()
        String postData= objectMapper.writeValueAsString(request)
        String response=PiTreClient.routeRestRequest(config,"ExecuteTransmissionDocument",ExecuteTransmissionDocumentRequest.METHOD,postData,authToken,null)
        ret = objectMapper.readValue(response, ExecuteTransmissionDocumentResponse.class)
        long delay = System.currentTimeMillis() - start
        log.info("[WS] executeTransmissionDocument fine ("+ delay + ")")
        return ret
    }

    static int estraiAnnoDaFascicoloPiTre(String numeroFascicolo){
        int ret=0
        Pattern pattern = Pattern.compile("/(\\d{4})/")
        Matcher matcher = pattern.matcher(numeroFascicolo)
        if (matcher.find()) {
            try{ ret=Integer.valueOf(matcher.group(1))  }catch (Exception ex){}
        }
        return ret
    }


    /*
    {
	"Filters": [
		{
			"Name": "CLASSIFICATION_SCHEME",
			"Value": "1576173"
		},
		{
			"Name": "PROJECT_CODE",
			"Value": "6.3.1/2015/1"
		},
		{
			"Name": "PROJECT_NUMBER",
			"Value": "1"
		}
	]
}
     */

}


