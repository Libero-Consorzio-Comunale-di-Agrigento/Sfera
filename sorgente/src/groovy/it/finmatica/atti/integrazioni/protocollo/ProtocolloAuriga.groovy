package it.finmatica.atti.integrazioni.protocollo

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.AbstractProtocolloEsterno
import it.finmatica.atti.IDocumentaleEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.commons.TokenIntegrazione
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.documenti.*
import it.finmatica.atti.dto.documenti.DatoAggiuntivoDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.integrazioniws.auriga.WSAddUd.WSAddUdService
import it.finmatica.atti.integrazioniws.auriga.WSAddUd.WSAddUdServiceLocator
import it.finmatica.atti.integrazioniws.auriga.WSAddUd.WSIAddUd
import it.finmatica.atti.integrazioniws.auriga.WSTrovaClassifTitolario.WSITrovaClassifTitolario
import it.finmatica.atti.integrazioniws.auriga.WSTrovaClassifTitolario.WSTrovaClassifTitolarioPortBindingStub
import it.finmatica.atti.integrazioniws.auriga.WSTrovaClassifTitolario.WSTrovaClassifTitolarioService
import it.finmatica.atti.integrazioniws.auriga.WSTrovaClassifTitolario.WSTrovaClassifTitolarioServiceLocator
import it.finmatica.atti.integrazioniws.auriga.WSTrovaDocFolder.WSITrovaDocFolder
import it.finmatica.atti.integrazioniws.auriga.WSTrovaDocFolder.WSTrovaDocFolderPortBindingStub
import it.finmatica.atti.integrazioniws.auriga.WSTrovaDocFolder.WSTrovaDocFolderService
import it.finmatica.atti.integrazioniws.auriga.WSTrovaDocFolder.WSTrovaDocFolderServiceLocator
import it.finmatica.atti.integrazioniws.auriga.classes.*
import it.finmatica.atti.integrazioniws.utility.Utility
import it.finmatica.atti.utility.InvioMail
import org.apache.axis.client.Stub
import org.apache.commons.io.IOUtils
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import javax.xml.bind.JAXBException
import javax.xml.rpc.ServiceException
import javax.xml.soap.SOAPException
import java.security.NoSuchAlgorithmException
import java.text.SimpleDateFormat

/**
 * Integrazione del protocollo e-Grammar - Auriga di Engineering.
 */
@Component("protocolloAuriga")
@Lazy
class ProtocolloAuriga extends AbstractProtocolloEsterno {

    private static final Logger log = Logger.getLogger(ProtocolloAuriga.class)

    @Autowired ProtocolloAurigaConfig protocolloAurigaConfig
    @Autowired IDocumentaleEsterno gestoreDocumentaleEsterno
    @Autowired TokenIntegrazioneService tokenIntegrazioneService
    @Autowired SpringSecurityService springSecurityService
    @Autowired IGestoreFile gestoreFile


    @Override
    public String getZulFascicolo() {
        return "/commons/popupFascicoliAuriga.zul";
    }

    String getUrlWebservice() {
        return protocolloAurigaConfig.getUrlWebService()
    }

    String getUtenteWsProtocollo() {
        return protocolloAurigaConfig.getUtenteWebService()
    }

    DatiAtto creaDatiAtto(IProtocollabile atto, boolean daProtocollare ){
        DatiAtto datiAtto = new DatiAtto()
        datiAtto.separatoreUffProduttore = ".";
        datiAtto.separatoreAssegnazioneInterna = ".";
        datiAtto.separatoreClassifica = ".";
        datiAtto.separatoreFascicolo = "/";
        if(daProtocollare) {
            datiAtto.CategoriaRegDaDare="PG"; // se c'e' questo PROTOCOLLA altrimenti registra solo
            datiAtto.CategoriaReg = protocolloAurigaConfig.getCategoriaProtocollare()
        }
        else {
            datiAtto.CategoriaReg= protocolloAurigaConfig.getCategoriaNonProtocollare()
        }
        datiAtto.TipoProvenienza="I" //Indica se unità documentaria in entrata (E), uscita (U) o interna (I)

        datiAtto.AssegnazioneInterna=atto.unitaProponente.codice
        datiAtto.UffProduttore=atto.unitaProponente.codice

        //STANDARD dell' ATTO
        datiAtto.Oggetto=atto.getOggetto()
        if(atto instanceof Determina){
            datiAtto.AnnoReg=atto.getAnnoDetermina()
            datiAtto.NumReg=atto.getNumeroDetermina()
            datiAtto.DataOraReg=atto.getDataNumeroDetermina()
            datiAtto.SiglaReg = atto.registroDetermina?.registroEsterno ?: atto.registroDetermina?.codice

        }
        if(atto instanceof Delibera){
            datiAtto.AnnoReg=atto.getAnnoDelibera()
            datiAtto.NumReg=atto.getNumeroDelibera()
            datiAtto.DataOraReg=atto.getDataNumeroDelibera()
            datiAtto.SiglaReg = atto.registroDelibera?.registroEsterno ?: atto.registroDelibera?.codice
        }

        //datiAtto.idFolder=1386627; // si puo' usare anche idFolder se servisse
        if(atto.classificaCodice!=null)
            datiAtto.Classifica=atto.classificaCodice
        if(atto.fascicoloNumero) {
            datiAtto.FascicoloAnno = atto.fascicoloAnno
            datiAtto.FascicoloNumero = atto.fascicoloNumero
        }

        datiAtto.riservato=atto.riservato

        // aggiungo il testo principale
        if (atto.testo != null) {
            datiAtto.filePrincipale=new FileAtto(atto.testo.nome,IOUtils.toByteArray(gestoreFile.getFile(atto, atto.testo)))
        }

        List<AllegatoAtto> allegatiAtto = new ArrayList<AllegatoAtto>()

        //aggiungo gli allegati dell'atto
        if(atto.allegati!=null){
            for (Allegato allegato in atto.allegati){

                for (FileAllegato fileAllegato in allegato.fileAllegati ) {
                    allegatiAtto.add(new AllegatoAtto(allegato.descrizione, new FileAtto(fileAllegato.nome, IOUtils.toByteArray(gestoreFile.getFile(allegato, fileAllegato)))))
                }
                if(allegato.riservato)
                    datiAtto.riservato=true

            }
        }

        if(atto instanceof Determina || atto instanceof Delibera ) {
            if (atto instanceof Delibera) {
                // Aggiungo gli allegati dei pareri della proposta:
                for (VistoParere visto : atto.propostaDelibera.visti) {
                    if (visto.valido) {
                        // aggiungo il testo del visto
                        if (visto.testo != null) {
                            allegatiAtto.add(new AllegatoAtto(visto.tipologia.titolo,new FileAtto(visto.testo.nome, IOUtils.toByteArray(gestoreFile.getFile(visto, visto.testo)))))
                        }
                        // aggiungo gli allegati del visto
                        for (Allegato allegato in visto.allegati) {

                            for (FileAllegato fileAllegato in allegato.fileAllegati) {
                                allegatiAtto.add(new AllegatoAtto(allegato.descrizione,new FileAtto(fileAllegato.nome, IOUtils.toByteArray(gestoreFile.getFile(allegato, fileAllegato)))))
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
                        allegatiAtto.add(new AllegatoAtto(visto.tipologia.titolo,new FileAtto(visto.testo.nome, IOUtils.toByteArray(gestoreFile.getFile(visto, visto.testo)))))
                    }
                    // aggiungo gli allegati del visto
                    for (Allegato allegato in visto.allegati) {

                        for (FileAllegato fileAllegato in allegato.fileAllegati) {
                            allegatiAtto.add(new AllegatoAtto(allegato.descrizione,new FileAtto(fileAllegato.nome, IOUtils.toByteArray(gestoreFile.getFile(allegato, fileAllegato)))))
                        }

                    }
                }
            }

            // Aggiungo gli allegati dei certificati:
            for (Certificato certificato : atto.certificati) {
                if (certificato.valido && certificato.testo != null) {
                    // aggiungo il testo del visto
                    if (certificato.testo != null) {
                        allegatiAtto.add(new AllegatoAtto(certificato.tipologia.titolo,new FileAtto(certificato.testo.nome, IOUtils.toByteArray(gestoreFile.getFile(certificato, certificato.testo)))))
                    }
                    for (Allegato allegato in certificato.allegati) {

                        for (FileAllegato fileAllegato in allegato.fileAllegati) {
                            allegatiAtto.add(new AllegatoAtto(allegato.descrizione,new FileAtto(fileAllegato.nome, IOUtils.toByteArray(gestoreFile.getFile(allegato, fileAllegato)))))
                        }

                    }
                }
            }
        }
        datiAtto.allegati = allegatiAtto as AllegatoAtto[]
        return datiAtto
    }

    @Override
    @Transactional
    void registra(IProtocollabile atto) {
        // ottengo il lock pessimistico per evitare doppie protocollazioni.
        atto.lock()

        // creo il token di protocollazione: se lo trovo ed ha successo, vuol dire che ho già protocollato:
        TokenIntegrazione token = tokenIntegrazioneService.beginTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO);
        if (token.isStatoSuccesso()) {
            // significa che ho già protocollato: prendo il numero di protocollo, lo assegno al documento ed esco:
            def map = Eval.me(token.dati);
            String idUDRitorno = map.idUDRitorno
            //memorizzo id di ritorno dal protocollo auriga
            if (idUDRitorno) {
                DatoAggiuntivoDTO datoAggiuntivoDTO = new DatoAggiuntivoDTO()
                datoAggiuntivoDTO.codice = TipoDatoAggiuntivo.ID_DOCUMENTO_AURIGA
                datoAggiuntivoDTO.valore = idUDRitorno
                atto.toDTO().addToDatiAggiuntivi(datoAggiuntivoDTO)
            }
            atto.save()

            // elimino il token: tutto è andato bene e verrà eliminato solo alla commit sull transaction principale
            tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
            return;
        }

        try {

            DatiAtto datiAtto = creaDatiAtto(atto,false)
            ResponseProtocollo response = protocolla(protocolloAurigaConfig.getCodiceApplicazione(), protocolloAurigaConfig.getUtenteWebService(), protocolloAurigaConfig.getPasswordWebService(), protocolloAurigaConfig.getUrlWebService(), datiAtto)

            if(response.result.WSResult.equals(BaseOutput_WS.OK)) {

                tokenIntegrazioneService.setTokenSuccess("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO,
                        "[idUDRitorno:${response.retProto.IdUD}]")

                String idUDRitorno = response.retProto.IdUD
                //memorizzo l'eventuale collegamento ad una pratica SURF
                if (idUDRitorno) {
                    DatoAggiuntivoDTO datoAggiuntivoDTO = new DatoAggiuntivoDTO()
                    datoAggiuntivoDTO.codice = TipoDatoAggiuntivo.ID_DOCUMENTO_AURIGA
                    datoAggiuntivoDTO.valore = idUDRitorno
                    atto.toDTO().addToDatiAggiuntivi(datoAggiuntivoDTO)
                }

                atto.save()

                // elimino il token: questo avverrà solo se la transazione "normale" di grails andrà a buon fine:
                tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
            }else{
                throw new Exception((response.result.WSError.ErrorNumber!=null?response.result.WSError.ErrorNumber:"")+"-"+response.result.WSError.ErrorMessage)
            }

        } catch (Exception e) {
            log.error("Errore nella chiamata alla registrazione: ${e.getMessage()}", e);
            InvioMail.inviaMailErrori("Errore in fase di registrazione atto su Auriga"
                    ,getDatiAtto(atto,e.getMessage()) + "\n Utente operazione: " + springSecurityService.currentUser?.id
                    ,e.getStackTrace().toString())
            // elimino il token (solo in caso di errore successivo la protocollazione) e interrompo la transazione del token (così si possono fare più tentativi)
            tokenIntegrazioneService.stopTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO);
            throw new AttiRuntimeException("Errore in fase di registrazione atto su Auriga: ${e.getMessage()}.", e);
        }
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

            //fascicola(atto);

            // elimino il token: tutto è andato bene e verrà eliminato solo alla commit sull transaction principale
            tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
            return;
        }

        try {

            DatiAtto datiAtto = creaDatiAtto(atto,true)
            ResponseProtocollo response = protocolla(protocolloAurigaConfig.getCodiceApplicazione(), protocolloAurigaConfig.getUtenteWebService(), protocolloAurigaConfig.getPasswordWebService(), protocolloAurigaConfig.getUrlWebService(), datiAtto)

            if(response.result.WSResult.equals(BaseOutput_WS.OK)) {

                tokenIntegrazioneService.setTokenSuccess("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO,
                        "[numero:${response.retProto.RegistrazioneDataUD.NumReg}, anno:${response.retProto.RegistrazioneDataUD.AnnoReg}, data:'${new Date().format("dd/MM/yyyy")}']")

                atto.numeroProtocollo = response.retProto.RegistrazioneDataUD.NumReg
                atto.annoProtocollo = response.retProto.RegistrazioneDataUD.AnnoReg
                atto.dataNumeroProtocollo = new Date()

                atto.save()

                // elimino il token: questo avverrà solo se la transazione "normale" di grails andrà a buon fine:
                tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
            }else{
                throw new Exception((response.result.WSError.ErrorNumber!=null?response.result.WSError.ErrorNumber:"")+"-"+response.result.WSError.ErrorMessage)
            }

        } catch (Exception e) {
            log.error("Errore nella chiamata alla protocollazione: ${e.getMessage()}", e);
            // elimino il token (solo in caso di errore successivo la protocollazione) e interrompo la transazione del token (così si possono fare più tentativi)
            InvioMail.inviaMailErrori("Errore in fase di protocollazione atto su Auriga"
                    ,getDatiAtto(atto,e.getMessage()) + "\n Utente operazione: " + springSecurityService.currentUser?.id
                    ,e.getStackTrace().toString())
            tokenIntegrazioneService.stopTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO);
            throw new AttiRuntimeException("Errore in fase di protocollazione atto su Auriga: ${e.getMessage()}.", e);
        }
    }

    static String getDatiAtto(def atto,String message){
        String ret=""
        ret="Id: " + atto.id
        if (atto instanceof Determina) {
            ret+="\n Tipologia: "+ atto.tipologia.titolo
            ret+="\n Anno Proposta: "+ atto.annoProposta
            ret+="\n Numero Proposta: "+ atto.numeroProposta
        } else if (atto instanceof Delibera) {
            ret+="\n Tipologia: "+ atto.propostaDelibera.tipologia.titolo
            ret+="\n Anno Proposta: "+ atto.propostaDelibera.annoProposta
            ret+="\n Numero Proposta: "+ atto.propostaDelibera.numeroProposta
        }
        if(message!=null && message.length()>0){
            ret+="\n Dettaglio Errore: "+ message
        }
        return ret;
    }

    @Override
    List<Classifica> getListaClassificazioni(String filtro, String codiceUoProponente) {
        ResponseTitolario ret = ricercaClassifiche(protocolloAurigaConfig.getCodiceApplicazione(), protocolloAurigaConfig.getUtenteWebService(), protocolloAurigaConfig.getPasswordWebService(), protocolloAurigaConfig.getUrlWebserviceTitolario(), filtro,"",".");

        def classifiche = [];
        if(ret.result.WSResult.equals(BaseOutput_WS.OK)){
            if (ret.lista?.Classifica) {
                for (int i = 0; i < ret.lista.Classifica.length; i++) {
                    ClassificaType classifica = ret.lista.Classifica[i];
                    classifiche << new Classifica(dal: getDateFromString(classifica.DataInizioValidita), codice: classifica.Indice, descrizione: classifica.DescrizioneEstesa);
                }
            }
        }

        return classifiche;
    }

    @Override
    List<Fascicolo> getListaFascicoliAnno(int anno, String filtro, String codiceClassifica, Date classificaDal, String codiceUoProponente) {
        String utenteCorrente=(springSecurityService.currentUser!=null?springSecurityService.currentUser.nominativo:protocolloAurigaConfig.getUtenteWebService())
      //  String passwordCorrente= (springSecurityService.currentUser!=null?"":protocolloAurigaConfig.getPasswordWebService())
        String passwordCorrente = ""
        if (protocolloAurigaConfig.isPasswordUnica()) {
            passwordCorrente = protocolloAurigaConfig.getPasswordUnica()
        }
        else {
            passwordCorrente= (springSecurityService.currentUser!=null?"":protocolloAurigaConfig.getPasswordWebService())
        }
        ResponseTitolario ret = ricercaFascicoli(protocolloAurigaConfig.getCodiceApplicazione(), utenteCorrente, passwordCorrente, protocolloAurigaConfig.getUrlWebserviceFascicolo(), codiceClassifica, filtro, anno,".");
        def listaFascicoli = [];
        if(ret.result?.WSResult.equals(BaseOutput_WS.OK)){
            if (ret.lista?.Riga) {
                for(int i=0;i<ret.lista.Riga.length;i++){
                    Riga riga=ret.lista.Riga[i];

                    //classifiche << new Classifica(dal: getDateFromString(classifica.DataInizioValidita), codice: classifica.Identificativo, descrizione: classifica.DescrizioneEstesa);
                    //2021 VIII.4 N° 8
                    String[] fascicolo=riga.Colonna[3].value.split(" ");
                    listaFascicoli << new Fascicolo(classifica: new Classifica(dal: classificaDal, codice: codiceClassifica, descrizione: '')
                            , anno: Integer.valueOf(fascicolo[0].toString())
                            , numero: (fascicolo.length>2?fascicolo[3].toString():"")
                            , oggetto: riga.Colonna[2].value);
                }
            }
        }

        return listaFascicoli;
    }

    @Override
    void fascicola(IFascicolabile fascicolabile) {

    }

    public Date getDateFromString(String dataString){
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ITALIAN);
        return formatter.parse(dataString);
    }

    //Metodi per integrazione con Auriga
    static ResponseTitolario ricercaClassifiche(String codiceApplicazione, String username, String password, String urlEndpoint, String codiceClassifica,String descrizioneClassifica,String separatore) throws IOException, ServiceException, NoSuchAlgorithmException, JAXBException, SOAPException {
        ResponseTitolario ret = new ResponseTitolario()

        if (separatore==null || separatore.length()==0)
            separatore="."
        WSTrovaClassifTitolarioService service = new WSTrovaClassifTitolarioServiceLocator()
        WSITrovaClassifTitolario port = service.getWSTrovaClassifTitolarioPort(new URL(urlEndpoint))
        it.finmatica.atti.integrazioniws.auriga.WSTrovaClassifTitolario.Service service1 = new it.finmatica.atti.integrazioniws.auriga.WSTrovaClassifTitolario.Service()
        service1.setUserName(username)
        service1.setPassword(password)
        service1.setCodApplicazione(codiceApplicazione)
        service1.setIstanzaApplicazione("")

        TrovaClassifTitolario trovaClassifiche = new TrovaClassifTitolario()
        if(codiceClassifica!=null && codiceClassifica.length()>0)
            trovaClassifiche.IndiceClassificazione = LivelloGerarchiaType.getLivelli(codiceClassifica, separatore)
        if(descrizioneClassifica!=null && descrizioneClassifica.length()>0)
            trovaClassifiche.DesClassificazione=descrizioneClassifica
        trovaClassifiche.SoloClassifAttive = 1;
        String xmlInput = Utility.getXmlStringFromObject(trovaClassifiche)
        log.info("ricercaClassifiche:codiceApplicazione:" + codiceApplicazione)
        log.info("ricercaClassifiche:username:" + username)
       // log.info("ricercaClassifiche:password:" + password)
        log.info("ricercaClassifiche:urlEndpoint:" + urlEndpoint)
        log.info("ricercaClassifiche:codiceClassifica:" + codiceClassifica)
        log.info("ricercaClassifiche:descrizioneClassifica:" + descrizioneClassifica)
        log.info("ricercaClassifiche:separatore:" + separatore)
        log.info("ricercaClassifiche:xmlInput:" + xmlInput)
        service1.setXml(xmlInput)
        service1.setHash(Utility.encryptSHA1(xmlInput))
        it.finmatica.atti.integrazioniws.auriga.WSTrovaClassifTitolario.ServiceResponse responseService = port.serviceOperation(service1)
        String xmlResponse = Utility.getStringFromB64(responseService.getServiceReturn())
        BaseOutput_WS response = (BaseOutput_WS) Utility.getObjectFromXml(xmlResponse, BaseOutput_WS.class)
        ret.result = response
        if (response.WSResult.equals(BaseOutput_WS.OK)) {
            String xmlRet = Utility.getStringAttachment((WSTrovaClassifTitolarioPortBindingStub) port)
            xmlRet = Utility.decodeXmlHtml(xmlRet)
            Lista classifiche = (Lista) Utility.getObjectFromXml(xmlRet, Lista.class)
            ret.lista = classifiche
        }
        return ret
    }

    static ResponseTitolario ricercaDocumento(String codiceApplicazione, String username, String password, String urlEndpoint, Integer idDoc) throws IOException, ServiceException, NoSuchAlgorithmException, JAXBException, SOAPException {
        ResponseTitolario ret = new ResponseTitolario()
        WSTrovaDocFolderService service = new WSTrovaDocFolderServiceLocator()
        WSITrovaDocFolder port = service.getWSTrovaDocFolderPort(new URL(urlEndpoint))
        it.finmatica.atti.integrazioniws.auriga.WSTrovaDocFolder.Service service1 = new it.finmatica.atti.integrazioniws.auriga.WSTrovaDocFolder.Service()
        service1.setUserName(username)
        service1.setPassword(password)
        service1.setCodApplicazione(codiceApplicazione)
        service1.setIstanzaApplicazione("")

        TrovaDocFolder tdf = new TrovaDocFolder()
        FiltriPrincipaliType fp = new FiltriPrincipaliType()
        fp.TipoOggettiDaCercare = "D"
        FiltroFullTextType filtroFullText= new FiltroFullTextType()
        filtroFullText.FlagTutteLeParole="1"
        filtroFullText.ListaParole="Sfera"
        fp.FiltroFullText=filtroFullText
        tdf.FiltriPrincipali=fp
        tdf.LimitaEstrazioneAlCampo = [1, 2, 3, 4, 7, 16]
        String xmlInput = Utility.getXmlStringFromObject(tdf)
        log.info("ricercaDocumento:codiceApplicazione:" + codiceApplicazione)
        log.info("ricercaDocumento:username:" + username)
        log.info("ricercaDocumento:password:" + password)
        log.info("ricercaDocumento:urlEndpoint:" + urlEndpoint)
        log.info("ricercaDocumento:idDoc:" + idDoc)
        log.info("ricercaDocumento:xmlInput:" + xmlInput)
        service1.setXml(xmlInput)
        service1.setHash(Utility.encryptSHA1(xmlInput))
        it.finmatica.atti.integrazioniws.auriga.WSTrovaDocFolder.ServiceResponse responseService = port.serviceOperation(service1)
        String xmlResponse = Utility.getStringFromB64(responseService.getServiceReturn())
        BaseOutput_WS response = (BaseOutput_WS) Utility.getObjectFromXml(xmlResponse, BaseOutput_WS.class)
        ret.result = response
        if (response.WSResult.equals(BaseOutput_WS.OK)) {
            String xmlRet = Utility.getStringAttachment((WSTrovaDocFolderPortBindingStub) port)
            xmlRet = Utility.decodeXmlHtml(xmlRet)
            Lista fascicoli = (Lista) Utility.getObjectFromXml(xmlRet, Lista.class)
            ret.lista = fascicoli
        }
        return ret
    }

    static ResponseTitolario ricercaFascicoli(String codiceApplicazione, String username, String password, String urlEndpoint, String codiceClassifica, String codiceFascicoli, Integer annoFascicolo,String separatore) throws IOException, ServiceException, NoSuchAlgorithmException, JAXBException, SOAPException {
        ResponseTitolario ret = new ResponseTitolario()
        String separatoreFascicoli = "/"
        if (separatore==null || separatore.length()==0)
            separatore="."
        WSTrovaDocFolderService service = new WSTrovaDocFolderServiceLocator()
        WSITrovaDocFolder port = service.getWSTrovaDocFolderPort(new URL(urlEndpoint))
        it.finmatica.atti.integrazioniws.auriga.WSTrovaDocFolder.Service service1 = new it.finmatica.atti.integrazioniws.auriga.WSTrovaDocFolder.Service()
        service1.setUserName(username)
        //service1.setUserName('d.corchia')  //utente ambiente di test Parma
        service1.setPassword(password)
        service1.setCodApplicazione(codiceApplicazione)
        service1.setIstanzaApplicazione("")

        TrovaDocFolder tdf = new TrovaDocFolder()
        FiltriPrincipaliType fp = new FiltriPrincipaliType()
        fp.TipoOggettiDaCercare = "F"
        ClassifUAType classSearch = new ClassifUAType()
        if(annoFascicolo>0)
            classSearch.AnnoAperturaUA = annoFascicolo
        classSearch.LivelloClassificazione = LivelloGerarchiaType.getLivelli(codiceClassifica, separatore)
        if(codiceFascicoli!=null && codiceFascicoli.length()>0) {
            String[] splitFascicoli=codiceFascicoli.split("["+separatoreFascicoli+"]")
            if(splitFascicoli.length>0)
                classSearch.NroProgrUA=Integer.valueOf(splitFascicoli[0])
            if(splitFascicoli.length>1)
                classSearch.NroSottofasc=Integer.valueOf(splitFascicoli[1])
        }
        fp.ClassifUA = classSearch
        tdf.FiltriPrincipali = fp
        tdf.LimitaEstrazioneAlCampo = [1, 2, 3, 4, 7, 16, 31]
        String xmlInput = Utility.getXmlStringFromObject(tdf)
        log.info("ricercaFascicoli:codiceApplicazione:" + codiceApplicazione)
        log.info("ricercaFascicoli:username:" + username)
        //log.info("ricercaFascicoli:password:" + password)
        log.info("ricercaFascicoli:urlEndpoint:" + urlEndpoint)
        log.info("ricercaFascicoli:codiceClassifica:" + codiceClassifica)
        log.info("ricercaFascicoli:codiceFascicoli:" + codiceFascicoli)
        log.info("ricercaFascicoli:annoFascicolo:" + annoFascicolo)
        log.info("ricercaFascicoli:separatore:" + separatore)
        log.info("ricercaFascicoli:xmlInput" + xmlInput)
        service1.setXml(xmlInput)
        service1.setHash(Utility.encryptSHA1(xmlInput))
        it.finmatica.atti.integrazioniws.auriga.WSTrovaDocFolder.ServiceResponse responseService = port.serviceOperation(service1)
        String xmlResponse = Utility.getStringFromB64(responseService.getServiceReturn())
        BaseOutput_WS response = (BaseOutput_WS) Utility.getObjectFromXml(xmlResponse, BaseOutput_WS.class)
        ret.result = response
        if (response.WSResult.equals(BaseOutput_WS.OK)) {
            String xmlRet = Utility.getStringAttachment((WSTrovaDocFolderPortBindingStub) port)
            xmlRet = Utility.decodeXmlHtml(xmlRet)

            Lista responseTrovaDocFolder = (Lista) Utility.getObjectFromXml(xmlRet, Lista.class)
            ret.lista = responseTrovaDocFolder
            if(responseTrovaDocFolder.Riga?.length>0)
                ret.lista.Riga=filtroFascicoli(responseTrovaDocFolder.Riga)
        }
        return ret
    }


    static Riga[] filtroFascicoli(Riga[] fascicoli){
        List<Riga> righe = new ArrayList<Riga>();
        for (int i=0;i<fascicoli.length;i++){
            Riga riga = fascicoli[i];
            if(checkColonna(riga.Colonna)){
                righe.add(riga);
            }
        }
        Riga[] fascicoliBuoni = new Riga[righe.size()];
        int i=0;
        for (Riga riga : righe){
            fascicoliBuoni[i]=riga;
            i++;
        }
        return fascicoliBuoni;
    }


    static boolean checkColonna(ColonnaType[] colonne){
        for (int i=0;i<colonne.length;i++){
            ColonnaType colonna = colonne[i];

            if(colonna.Nro==16 && colonna.value!=null && colonna.value.length()>0){
                return false;
            }
            if(colonna.Nro==31 && colonna.value.equals("4")){
                return true;
            }
        }
        return false;
    }

    static ResponseProtocollo protocolla(String codiceApplicazione, String username, String password, String urlEndpoint, DatiAtto atto ) throws IOException, ServiceException, NoSuchAlgorithmException, JAXBException, SOAPException {
        ResponseProtocollo ret = new ResponseProtocollo()
        int contaFile = 0
        WSAddUdService service = new WSAddUdServiceLocator()
        WSIAddUd port = service.getWSAddUdPort(new URL(urlEndpoint))
        it.finmatica.atti.integrazioniws.auriga.WSAddUd.Service service1 = new it.finmatica.atti.integrazioniws.auriga.WSAddUd.Service()
        service1.setUserName(username)
        service1.setPassword(password)
        service1.setCodApplicazione(codiceApplicazione)
        service1.setIstanzaApplicazione("")

        NewUD proto = new NewUD()
        if(atto.riservato)
            proto.LivelloRiservatezzaInterno=1
        RegistrazioneNumerazioneType registrazioneData = new RegistrazioneNumerazioneType()
        registrazioneData.CategoriaReg = atto.CategoriaReg
        registrazioneData.SiglaReg = atto.SiglaReg
        registrazioneData.AnnoReg = atto.AnnoReg
        registrazioneData.NumReg = atto.NumReg
        registrazioneData.DataOraReg = atto.DataOraReg
        UserType utente = new UserType()
        utente.UsernameInterna=username
        registrazioneData.UserReg =utente
        proto.RegistrazioneData = registrazioneData

        if (atto.CategoriaRegDaDare != null && atto.CategoriaRegDaDare.length() > 0) {
            RegistrazioneDaDareType registrazioneDaDare = new RegistrazioneDaDareType()
            registrazioneDaDare.CategoriaReg = atto.CategoriaRegDaDare
            proto.RegistrazioneDaDare = registrazioneDaDare
        }

        proto.OggettoUD = atto.Oggetto
        proto.TipoProvenienza = atto.TipoProvenienza
        DatiProduzioneType datiProduzione = new DatiProduzioneType()
        UOType uffProduttore = new UOType()
        uffProduttore.LivelloUO = LivelloGerarchiaType.getLivelliUnita(atto.UffProduttore)
        datiProduzione.UffProduttore = uffProduttore
        proto.DatiProduzione=datiProduzione
        AssegnazioneInternaType assegnazioneInterna = new AssegnazioneInternaType()
        UOType uffAssegnazioneInterna = new UOType()
        uffAssegnazioneInterna.LivelloUO = LivelloGerarchiaType.getLivelliUnita(atto.AssegnazioneInterna)
        assegnazioneInterna.UO = uffAssegnazioneInterna
        assegnazioneInterna.FlagPerConoscenza="0"; // dicono che è fisso
        proto.AssegnazioneInterna = assegnazioneInterna
        if(atto.idFolder>0 || atto.Classifica!=null || atto.FascicoloAnno>0) {
            CollocazioneClassificazioneUDType collocazioneClassificazioneUD = new CollocazioneClassificazioneUDType()
            ClassifFascicoloType classifFascicolo = new ClassifFascicoloType()
            if(atto.idFolder>0) {
                EstremiXIdentificazioneFolderNoLibType folderCustomer = new EstremiXIdentificazioneFolderNoLibType()
                folderCustomer.IdFolder = atto.idFolder
                classifFascicolo.FolderCustom = folderCustomer
            }
            if(atto.Classifica!=null){
                ClassifUAType classifica= new ClassifUAType()
                classifica.LivelloClassificazione=LivelloGerarchiaType.getLivelli(atto.Classifica,atto.separatoreClassifica)
                if(atto.FascicoloAnno>0){
                    classifica.AnnoAperturaUA=atto.FascicoloAnno
                    String[] fascicolo=atto.FascicoloNumero.split("["+atto.separatoreFascicolo+"]")
                    if(fascicolo!=null && fascicolo.length>0){
                        classifica.NroProgrUA=Integer.valueOf(fascicolo[0])
                        if(fascicolo.length>1){
                            classifica.NroSottofasc=Integer.valueOf(fascicolo[1])
                        }
                    }
                }
                classifFascicolo.ClassifUA=classifica
            }
            collocazioneClassificazioneUD.ClassifFascicolo = classifFascicolo
            proto.CollocazioneClassificazioneUD = collocazioneClassificazioneUD
        }
        if (atto.filePrincipale != null) {
            Utility.setAttachment((Stub) port, atto.filePrincipale.is)
            contaFile++
            VersioneElettronicaType versioneElettronica = new VersioneElettronicaType()
            versioneElettronica.NomeFile=atto.filePrincipale.nomeFile
            versioneElettronica.NroAttachmentAssociato=contaFile
            proto.VersioneElettronica=versioneElettronica
        }


        if(atto.allegati!=null && atto.allegati.length>0){
            proto.AllegatoUD= new AllegatoUDType[atto.allegati.length]
            for (int i=0;i<atto.allegati.length;i++) {
                AllegatoAtto allegatoAtto = atto.allegati[i];
                AllegatoUDType allegato = new AllegatoUDType()

                FileAtto fileAtto = allegatoAtto.fileAllegato
                contaFile++
                VersioneElettronicaType versioneElettronica = new VersioneElettronicaType();
                versioneElettronica.NomeFile = fileAtto.nomeFile;
                versioneElettronica.NroAttachmentAssociato = contaFile;
                allegato.VersioneElettronica = versioneElettronica;
                allegato.DesAllegato =allegatoAtto.descrizione;
                proto.AllegatoUD[i] = allegato;
                Utility.setAttachment((Stub) port, fileAtto.is);
            }
        }

        String xmlInput = Utility.getXmlStringFromObject(proto)


        log.info("protocolla:codiceApplicazione:" + codiceApplicazione)
        log.info("protocolla:username:" + username)
      //  log.info("protocolla:password:" + password)
        log.info("protocolla:urlEndpoint:" + urlEndpoint)
        log.info("protocolla:xmlInput" + xmlInput)
        service1.setXml(xmlInput)
        service1.setHash(Utility.encryptSHA1(xmlInput))

        it.finmatica.atti.integrazioniws.auriga.WSAddUd.ServiceResponse responseService = port.serviceOperation(service1)
        String xmlResponse = Utility.getStringFromB64(responseService.getServiceReturn())
        log.info("protocolla - response:" + xmlResponse)
        BaseOutput_WS response = (BaseOutput_WS) Utility.getObjectFromXml(xmlResponse, BaseOutput_WS.class)
        ret.result = response
        if (response.WSResult.equals(BaseOutput_WS.OK)) {
            String xmlProto = Utility.getStringAttachment((Stub) port)
            xmlProto=Utility.decodeXmlHtml(xmlProto)
            Output_UD protoRet = (Output_UD) Utility.getObjectFromXml(xmlProto, Output_UD.class)
            System.out.println(protoRet.IdUD)
            ret.retProto=protoRet
        }
        return ret
    }
}
