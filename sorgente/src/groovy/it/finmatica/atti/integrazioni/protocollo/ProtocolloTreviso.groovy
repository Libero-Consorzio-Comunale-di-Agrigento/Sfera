package it.finmatica.atti.integrazioni.protocollo

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.AbstractProtocolloEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.IProtocolloEsterno
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.commons.TokenIntegrazione
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.documenti.Allegato
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.IDocumentoCollegato
import it.finmatica.atti.documenti.IProtocollabile
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.integrazioniws.ads.docarea.XmlProtocollazioneDOCArea
import it.finmatica.atti.integrazioniws.treviso.protocollo.prod.ARCHIVIAZIONEInput
import it.finmatica.atti.integrazioniws.treviso.protocollo.prod.ARCHIVIAZIONEResult
import it.finmatica.atti.integrazioniws.treviso.protocollo.prod.INSERIMENTOInput
import it.finmatica.atti.integrazioniws.treviso.protocollo.prod.INSERIMENTOResult
import it.finmatica.atti.integrazioniws.treviso.protocollo.prod.LOGINInput
import it.finmatica.atti.integrazioniws.treviso.protocollo.prod.LOGINResult
import it.finmatica.atti.integrazioniws.treviso.protocollo.prod.PROTOCOLLAZIONEInput
import it.finmatica.atti.integrazioniws.treviso.protocollo.prod.PROTOCOLLAZIONEResult
import it.finmatica.atti.integrazioniws.treviso.protocollo.prod.WSPROTOC
import it.finmatica.atti.integrazioniws.treviso.protocollo.prod.WSPROTOCPortType
import it.finmatica.gestionetesti.GestioneTestiService
import org.apache.commons.net.ftp.FTPClient
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Lazy
class ProtocolloTreviso extends AbstractProtocolloEsterno {

    private static final Logger log = Logger.getLogger(ProtocolloTreviso.class)

    @Autowired
    TokenIntegrazioneService tokenIntegrazioneService
    @Autowired
    ProtocolloTrevisoConfig protocolloTrevisoConfig
    @Autowired
    SpringSecurityService springSecurityService
    @Autowired
    GestioneTestiService gestioneTestiService
    @Autowired
    IProtocolloEsterno protocolloEsterno
    @Autowired
    IGestoreFile gestoreFile

    String getFtpServerUrl() {
        return protocolloTrevisoConfig.getFtpServerUrl()
    }

    String getFtpUser() {
        return protocolloTrevisoConfig.getFtpUser()
    }

    String getFtpPassword() {
        return protocolloTrevisoConfig.getFtpPassword()
    }

    String getFtpDirectory() {
        return protocolloTrevisoConfig.getFtpDirectory()
    }

    String getUrlWebservice() {
        return protocolloTrevisoConfig.getUrlWebService()
    }

    String getPasswordUtente() {
        return protocolloTrevisoConfig.getPasswordWebService()
    }

    String getCodiceEnte() {
        return protocolloTrevisoConfig.getCodiceEnte()
    }

    String getCodiceAoo() {
        return protocolloTrevisoConfig.getCodiceAoo()
    }

    @Override
    @Transactional
    void protocolla(IProtocollabile atto) {
        log.info("Ottengo il lock dell'atto.")
        // ottengo il lock pessimistico per evitare doppie protocollazioni.
        atto.lock();

        // controllo che il documento non sia gi?? protocollato
        if (atto.numeroProtocollo > 0) {
            throw new AttiRuntimeException("Il documento ?? gi?? protocollato con numero: ${atto.numeroProtocollo} / ${atto.annoProtocollo}!");
        }

        // controllo se c'?? un token di protocollazione precedente dovuto ad un errore dopo la protocollazione:
        TokenIntegrazione token = tokenIntegrazioneService.getToken("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
        if (token != null && token.isStatoSuccesso()) {
            // significa che ho gi?? protocollato: prendo il numero di protocollo, lo assegno al documento ed esco:
            def map = Eval.me(token.dati);
            atto.numeroProtocollo = map.numero;
            atto.annoProtocollo = map.anno;
            atto.dataNumeroProtocollo = Date.parse("dd/MM/yyyy", map.data);
            atto.save()

            // elimino il token: tutto ?? andato bene e verr?? eliminato solo alla commit sull transaction principale
            tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
            return;
        }

        String username = getUtente(atto);

        // preparo l'xml docarea:
        log.info("Preparo l'xml docarea")
        XmlProtocollazioneDOCArea xmlDocArea = creaXmlProtocollazione(atto, codiceEnte, codiceAoo);

        // mi collego al webservice
        log.info("Creo il webservice")
        WSPROTOC ss = new WSPROTOC(new URL(urlWebservice));
        WSPROTOCPortType service = ss.getWSPROTOCHttpSoap11Endpoint();

        // Faccio login sul webservice e inserisco i files.
        LOGINResult loginResult = loginEInserimentoFile(service, atto, xmlDocArea, codiceEnte, username, passwordUtente, true);

        String urlXmlProtocollazione = caricaFileSuFtp(loginResult.dst, atto.id, "dati_protocollazione.xml", xmlDocArea.toXmlInputStream());

        // creo il token di protocollazione: se lo trovo ed ha successo, vuol dire che ho gi?? protocollato:
        token = tokenIntegrazioneService.beginTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO);
        if (token.isStatoSuccesso()) {
            // significa che ho gi?? protocollato: prendo il numero di protocollo, lo assegno al documento ed esco:
            def map = Eval.me(token.dati);
            atto.numeroProtocollo = map.numero;
            atto.annoProtocollo = map.anno;
            atto.dataNumeroProtocollo = Date.parse("dd/MM/yyyy", map.data);
            atto.save()

            // elimino il token: tutto ?? andato bene e verr?? eliminato solo alla commit sull transaction principale
            tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
            return;
        }

        PROTOCOLLAZIONEResult protocollazioneResult = null;
        try {
            log.info("Eseguo la protocollazione webservice.")
            protocollazioneResult = service.protocollazione(new PROTOCOLLAZIONEInput(link: urlXmlProtocollazione, dst: loginResult.dst));
            log.debug("protocollazioneRet: ${protocollazioneResult.numpg}/${protocollazioneResult.annopg}")

            if (protocollazioneResult.errnbr != 0) {
                throw new Exception("Errore in fase di protocollazione via webservice: ${protocollazioneResult.errstring}.");
            }
        } catch (Exception e) {
            log.error("Errore nella chiamata alla protocollazione webservice: ${e.getMessage()}", e);
            // elimino il token e interrompo la transazione del token (cos?? si possono fare pi?? tentativi)
            tokenIntegrazioneService.stopTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO);
            throw new AttiRuntimeException("Errore in fase di protocollazione via webservice: ${e.getMessage()}.", e);
        }
        log.info("Protocollazione Webservice effettuata sul documento ${atto.id}: ${protocollazioneResult.numpg}/${protocollazioneResult.annopg} in data ${protocollazioneResult.datapg}")

        // la prima cosa che faccio dopo la protocollazione ?? salvare il record su db:
        tokenIntegrazioneService.setTokenSuccess("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO, "[numero:${protocollazioneResult.numpg}, anno:${protocollazioneResult.annopg}, data:'${protocollazioneResult.datapg}']");

        atto.numeroProtocollo = protocollazioneResult.numpg;
        atto.annoProtocollo = protocollazioneResult.annopg;
        atto.dataNumeroProtocollo = Date.parse("dd/MM/yyyy", protocollazioneResult.datapg);

        atto.save()

        // elimino il token: questo avverr?? solo se la transazione "normale" di grails andr?? a buon fine:
        tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
    }

    private String getUtente(IAtto atto) {
        return (atto instanceof Determina ? "AP_DETE-" : "AP_DELI-") + springSecurityService.currentUser.nominativo.toUpperCase();
    }

    void archiviazioneAtto(IAtto atto) {
        String username = getUtente(atto);

        // controllo se il documento ha un visto contabile, se s??, allora sar?? la firma di quello a chiudere definitivamente il documento
        // (cio?? a mettere "2" come tipoArchiviazione) altrimenti lascio "1" che vuol dire che il documento rimarr?? "aperto" per modifiche
        // sul protocollo di treviso.
        // String tipoArchiviazione = ((atto.proposta.visti.find { it.valido == true && it.tipologia.contabile == true} != null) ? "1" : "2");
        // modificato il metodo di archiviazione: a chiudere il documento sar?? l'invio di un certificato.
        String tipoArchiviazione = "1";

        // preparo l'xml docarea:
        XmlProtocollazioneDOCArea xmlDocArea = creaXmlProtocollazione(atto, codiceEnte, codiceAoo);

        // mi collego al webservice
        WSPROTOC ss = new WSPROTOC(new URL(urlWebservice));
        WSPROTOCPortType service = ss.getWSPROTOCHttpSoap11Endpoint();

        // Faccio login sul webservice e inserisco i files.
        LOGINResult loginResult = loginEInserimentoFile(service, atto, xmlDocArea, codiceEnte, username, passwordUtente);

        // Carico l'xml di protocollazione sull'ftp e lancio l'archiviazione.
        String urlXmlProtocollazione = caricaFileSuFtp(loginResult.dst, atto.id, "dati_protocollazione.xml", xmlDocArea.toXmlInputStream());
        ARCHIVIAZIONEResult archiviazioneResult = service.archiviazione(new ARCHIVIAZIONEInput(dst: loginResult.dst, link: urlXmlProtocollazione, tipoarch: tipoArchiviazione));

        if (archiviazioneResult.errnbr > 0) {
            log.error("Errore in fase di archiviazione del documento: ${archiviazioneResult.errstring}.");
            throw new AttiRuntimeException("Errore in fase di archiviazione del documento: ${archiviazioneResult.errstring}.");
        }
    }

    void archiviazioneDocumentoCollegato(IDocumentoCollegato documento, String tipoArchiviazione = "1") {
        IAtto atto = documento.documentoPrincipale;

        String username = getUtente(atto);

        // preparo l'xml docarea:
        XmlProtocollazioneDOCArea xmlDocArea = creaXmlProtocollazione(atto, codiceEnte, codiceAoo);

        // mi collego al webservice
        WSPROTOC ss = new WSPROTOC(new URL(urlWebservice));
        WSPROTOCPortType service = ss.getWSPROTOCHttpSoap11Endpoint();

        // Faccio login sul webservice e inserisco i files.
        LOGINResult loginResult = loginEInserimentoFile(service, documento, xmlDocArea, codiceEnte, username, passwordUtente);

        // Carico l'xml di protocollazione sull'ftp e lancio l'archiviazione.
        String urlXmlProtocollazione = caricaFileSuFtp(loginResult.dst, atto.id, "dati_protocollazione.xml", xmlDocArea.toXmlInputStream());
        log.debug("urlXmlProtocollazione: " + urlXmlProtocollazione)
        ARCHIVIAZIONEResult archiviazioneResult = service.archiviazione(new ARCHIVIAZIONEInput(dst: loginResult.dst, link: urlXmlProtocollazione, tipoarch: tipoArchiviazione));

        if (archiviazioneResult.errnbr > 0) {
            log.error("Errore in fase di archiviazione del documento: ${archiviazioneResult.errstring}.");
            throw new AttiRuntimeException("Errore in fase di archiviazione del documento: ${archiviazioneResult.errstring}.");
        }
    }

    void archiviazioneVisti(IAtto atto) {
        for (VistoParere v : atto.visti) {
            if (!v.valido) {
                continue
            }

            archiviazioneDocumentoCollegato(v)
        }
    }

    private LOGINResult loginEInserimentoFile(WSPROTOCPortType service, IDocumento documento, XmlProtocollazioneDOCArea xmlDocArea, String codiceEnte, String username, String password, boolean visti = false) {
        IAtto atto = (documento instanceof IAtto ? documento : documento.documentoPrincipale);

        log.info("Eseguo il login sul webservice di protocollazione")
        LOGINResult loginResult = service.login(new LOGINInput(codente: codiceEnte, username: username, password: password));

        log.debug("Eseguo il login sul webservice di protocollazione con l'utente: ${username}");
        if (loginResult.errnbr > 0) {
            log.error("Errore in fase di login al webservice di protocollazione: ${loginResult.errstring}.");
            throw new AttiRuntimeException("Errore in fase di login al webservice di protocollazione: ${loginResult.errstring}.");
        }

        // aggiungo il testo principale dell'atto:
        log.info("Inserisco l'allegato principale")
        InputStream allegatoPrincipale = gestoreFile.getFile(documento, documento.testo);

        // se ?? ancora modificabile, allora ?? la "prima passata", quindi rigenero la stampa in pdf.
        String nomeFile = documento.testo.nome;
        if (documento.testo.isModificabile()) {
            allegatoPrincipale = gestioneTestiService.stampaUnione(allegatoPrincipale, new String(documento.modelloTesto.tipoModello.query), [id: documento.id], GestioneTestiService.FORMATO_PDF, true);
            nomeFile = documento.testo.nomePdf;
        }

        String urlDocumento = caricaFileSuFtp(loginResult.dst, documento.testo.id, nomeFile, allegatoPrincipale);
        log.debug("urlDocumento: " + urlDocumento)
        INSERIMENTOResult inserimentoResult = service.inserimento(new INSERIMENTOInput(dst: loginResult.dst, username: username, link: urlDocumento));

        if (inserimentoResult.errnbr > 0) {
            log.error("Errore in fase di inserimento del file principale sul webservice di protocollazione: ${inserimentoResult.errstring}.");
            throw new AttiRuntimeException("Errore in fase di inserimento del file principale sul webservice di protocollazione: ${inserimentoResult.errstring}.");
        }

        if (documento instanceof IAtto || documento.testo?.isFirmato()) {
            xmlDocArea.setAllegatoPrincipale(inserimentoResult.docid, nomeFile, "Documento Principale", getTipoDocumento(atto));
        } else {
            xmlDocArea.addAllegato(inserimentoResult.docid, nomeFile, documento.tipologiaDocumento?.descrizione, null);
        }

        // Aggiungo gli allegati secondari.
        if (documento.hasProperty("allegati")) {
            log.debug("Inserisco gli allegati secondari.")
            for (Allegato allegato : documento.allegati?.findAll { it.valido == true }) {
                int i = 1;
                for (FileAllegato file : allegato.fileAllegati) {
                    urlDocumento = caricaFileSuFtp(loginResult.dst, file.id, file.nome, gestoreFile.getFile(allegato, file));
                    inserimentoResult = service.inserimento(new INSERIMENTOInput(dst: loginResult.dst, username: username, link: urlDocumento));

                    if (inserimentoResult.errnbr > 0) {
                        log.error("Errore in fase di inserimento del file principale sul webservice di protocollazione: ${inserimentoResult.errstring}.");
                        throw new AttiRuntimeException("Errore in fase di inserimento del file principale sul webservice di protocollazione: ${inserimentoResult.errstring}.");
                    }
                    xmlDocArea.addAllegato(inserimentoResult.docid, file.nome, "Allegato n. " + (i++) + ": " + allegato.titolo, null);
                }
            }
        }

        if (documento instanceof IAtto && visti) {
            for (VistoParere v : documento.visti?.findAll {
                it.valido == true && it.stato == it.finmatica.atti.documenti.StatoDocumento.CONCLUSO
            }) {
                log.info("Inserisco il testo del visto")
                String nomeVisto = v.testo.nome;
                InputStream visto = gestoreFile.getFile(v, v.testo);
                if (v.testo.isModificabile()) {
                    visto = gestioneTestiService.stampaUnione(visto, new String(v.modelloTesto.tipoModello.query), [id: v.id], GestioneTestiService.FORMATO_PDF, true);
                    nomeVisto = v.testo.nomePdf;
                }

                urlDocumento = caricaFileSuFtp(loginResult.dst, v.testo.id, nomeVisto, visto);
                inserimentoResult = service.inserimento(new INSERIMENTOInput(dst: loginResult.dst, username: username, link: urlDocumento));
                xmlDocArea.addAllegato(inserimentoResult.docid, nomeVisto, v.tipologiaDocumento?.descrizione, null);
            }
        }

        return loginResult;
    }

    private XmlProtocollazioneDOCArea creaXmlProtocollazione(IDocumento documento, String codiceEnte, String codiceAoo) {
        IAtto atto = (documento instanceof IAtto ? documento : documento.documentoPrincipale);

        XmlProtocollazioneDOCArea xmlDocArea = new XmlProtocollazioneDOCArea();
        xmlDocArea.setOggetto(atto.oggetto);
        xmlDocArea.setAmministrazione(codiceEnte, springSecurityService.principal.amm().descrizione, springSecurityService.principal.amministrazione.soggetto.indirizzoWeb);
        xmlDocArea.setFlusso(XmlProtocollazioneDOCArea.FLUSSO_USCITA);
        xmlDocArea.setNumeroRegistrazione("${atto.registroAtto.codice}/${atto.numeroAtto}/${atto.annoAtto}")
        xmlDocArea.setDataRegistrazione(atto.dataAtto.format("dd/MM/yyyy"))
        xmlDocArea.setAOO(codiceAoo)
        xmlDocArea.setNomeApplicativo("PROTV")

        // nessuna uo mittente/destinataria: in questo modo viene impostata amm+aoo e basta sia come mitt che come dest.
        xmlDocArea.setUoMittente(atto.getUnitaProponente().codice);
//		xmlDocArea.setUoMittente("U_RBIL");
        xmlDocArea.setDescrizioneRegistro(atto.registroAtto.descrizione);

        // FIXME: Va gestito anche il valore H (per documenti non contabili?)
        xmlDocArea.addParametroApplicativo("tipodocumento", (atto instanceof Determina ? "M" : "C"));

        return xmlDocArea;
    }

    private String caricaFileSuFtp(String dst, long idFile, String filename, InputStream is) {
        FTPClient ftp = new FTPClient();
        String directory = dst;
        String file = "${idFile}_${filename}";
        try {
            int reply;
            ftp.connect(ftpServerUrl);
            ftp.login(ftpUser, ftpPassword);
            ftp.mkd(directory);
            ftp.cwd(directory);
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();
            ftp.storeFile(file, new BufferedInputStream(is));
            ftp.logout();
            return directory + "/" + file;
        } finally {
            if (ftp.isConnected()) {
                try {
                    ftp.disconnect();
                } catch (Exception ioe) {
                    log.error(ioe);
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (Exception ioe) {
                    log.error(ioe);
                }
            }
        }

        return filename;
    }

    private String getTipoDocumento(IAtto atto) {
        if (atto instanceof Delibera) {
            return protocolloTrevisoConfig.getCodiceTipoDocumentoDelibera(atto.tipologiaDocumento?.id);
        } else if (atto instanceof Determina) {
            return protocolloTrevisoConfig.getCodiceTipoDocumentoDetermina();
        }

        throw new AttiRuntimeException("Nessun tipo documento specificato per il documento ${atto}");
    }
}
