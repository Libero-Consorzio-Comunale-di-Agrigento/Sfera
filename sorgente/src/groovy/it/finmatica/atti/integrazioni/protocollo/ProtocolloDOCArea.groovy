package it.finmatica.atti.integrazioni.protocollo

import grails.plugin.springsecurity.SpringSecurityService
import groovy.sql.Sql
import it.finmatica.atti.AbstractProtocolloEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.IProtocolloEsterno
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.IProtocolloEsterno.Classifica
import it.finmatica.atti.IProtocolloEsterno.Documento
import it.finmatica.atti.IProtocolloEsterno.Fascicolo
import it.finmatica.atti.commons.TokenIntegrazione
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.documenti.*
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.integrazioniws.ads.agspr.Movimento
import it.finmatica.atti.integrazioniws.ads.docarea.*
import it.finmatica.docer.atti.anagrafiche.DatiRicercaDocumento
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.apache.cxf.endpoint.Client
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.transport.http.HTTPConduit
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

import javax.sql.DataSource

class ProtocolloDOCArea extends AbstractProtocolloEsterno {

    private static final Logger log = Logger.getLogger(ProtocolloDOCArea.class)

    @Autowired
    DataSource dataSource
    @Autowired
    SpringSecurityService springSecurityService
    @Autowired
    TokenIntegrazioneService tokenIntegrazioneService
    @Autowired
    IProtocolloEsterno protocolloEsterno
    @Autowired
    IGestoreFile gestoreFile
    @Autowired
    ProtocolloDOCAreaConfig protocolloDOCAreaConfig

    @Override
    void sincronizzaClassificazioniEFascicoli() {
        protocolloEsterno.sincronizzaClassificazioniEFascicoli()
    }

    @Override
    void fascicola(IFascicolabile atto) {
        protocolloEsterno.fascicola(atto)
    }

    @Override
    @Transactional
    void protocolla(IProtocollabile atto) {
        // ottengo il lock pessimistico per evitare doppie protocollazioni.
        atto.lock()

        // controllo che il documento non sia già protocollato
        if (atto.numeroProtocollo > 0) {
            throw new AttiRuntimeException("Il documento è già protocollato con numero: ${atto.numeroProtocollo} / ${atto.annoProtocollo}!");
        }

        // controllo se c'è un token di protocollazione precedente dovuto ad un errore dopo la protocollazione:
        TokenIntegrazione token = tokenIntegrazioneService.getToken("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
        if (token != null && token.isStatoSuccesso()) {
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

        String username = protocolloDOCAreaConfig.getUtenteWebService()
        String password = protocolloDOCAreaConfig.getPasswordWebService()
        String codiceEnte = protocolloDOCAreaConfig.getCodiceEnte()
        String codiceAoo = protocolloDOCAreaConfig.getCodiceAoo()

        if (protocolloDOCAreaConfig.usaUtenteCorrente) {
            // devo prendere l'utente corrente di sessione e criptarne la password:
            username = springSecurityService.currentUser.nominativo
            password = tokenIntegrazioneService.getTokenAutenticazioneAd4(username)
        }

        // preparo l'xml docarea:
        XmlProtocollazioneDOCArea xmlDocArea = new XmlProtocollazioneDOCArea();
        xmlDocArea.setOggetto(atto.oggetto);
        xmlDocArea.setAmministrazione(codiceEnte, springSecurityService.principal.amm().descrizione, springSecurityService.principal.amministrazione.soggetto.indirizzoWeb);
        xmlDocArea.setFlusso(getMovimento(atto));

        xmlDocArea.setAOO(codiceAoo);
        xmlDocArea.setNomeApplicativo("AGSPR");        // FIXME: NOME APPLICATIVO

        // nessuna uo mittente/destinataria: in questo modo viene impostata amm+aoo e basta sia come mitt che come dest.
        xmlDocArea.setUoDestinataria(getCodiceUnitaWs(atto.getUnitaProponente()))
        xmlDocArea.setUoMittente(getCodiceUnitaWs(atto.getUnitaProponente()))
        xmlDocArea.addParametroApplicativo("uo", getCodiceUnitaWs(atto.getUnitaProponente()));    // uo proponente.

        xmlDocArea.setClassifica(atto.classificaCodice)
        xmlDocArea.setFascicolo(atto.fascicoloNumero, (atto.fascicoloAnno > 0) ? Integer.toString(atto.fascicoloAnno) : null, atto.fascicoloOggetto)

        // preparo il client webservice
        log.debug("Mi collego al webservice: ${protocolloDOCAreaConfig.getUrlWebService()}")
        DOCAREAProto ss = new DOCAREAProto(new URL(protocolloDOCAreaConfig.getUrlWebService()));
        DOCAREAProtoSoap port = ss.getDOCAREAProtoSoap();
        Client proxy = ClientProxy.getClient(port);
        HTTPConduit http = (HTTPConduit) proxy.getConduit();
        HTTPClientPolicy httpClientPolicy = http.getClient();
        if (httpClientPolicy == null) {
            httpClientPolicy = new HTTPClientPolicy();
            http.setClient(httpClientPolicy);
        }
        httpClientPolicy.setAllowChunking(false);

        // eseguo il login
        log.debug("Eseguo il login sul webservice di protocollazione con l'utente: ${username}");
        LoginRet loginRet = port.login(codiceEnte, username, password);

        if (loginRet.getLngErrNumber() != 0) {
            log.error("Errore in fase di login al webservice di protocollazione: ${loginRet.getStrErrString()}.");
            throw new AttiRuntimeException("Errore in fase di login al webservice di protocollazione: ${loginRet.getStrErrString()}.");
        }
        String dst = loginRet.getStrDST();

        AllegatoInterceptor allegatoInterceptor = new AllegatoInterceptor();
        proxy.getOutInterceptors().add(allegatoInterceptor);

        // aggiungo il testo dell'atto:
        log.debug("Inserisco l'allegato principale.")
        InputStream allegatoPrincipale = gestoreFile.getFile(atto, atto.testo);
        allegatoInterceptor.setInputStream(allegatoPrincipale);
        InserimentoRet inserimentoRet;
        inserimentoRet = port.inserimento(username, dst);

        if (inserimentoRet.getLngErrNumber() != 0) {
            log.error("Errore in fase di inserimento del file principale sul webservice di protocollazione: ${inserimentoRet.getStrErrString()}.");
            throw new AttiRuntimeException("Errore in fase di inserimento del file principale sul webservice di protocollazione: ${inserimentoRet.getStrErrString()}.");
        }
        xmlDocArea.setAllegatoPrincipale(inserimentoRet.getLngDocID(), atto.testo.nome, "Documento Principale", getTipoDocumento(atto));

        log.debug("Inserisco gli allegati secondari.")

        // aggiungo gli allegati al documento:
        for (Allegato allegato : atto.allegati) {
            int i = 1;
            for (FileAllegato file : allegato.fileAllegati) {
                allegatoInterceptor.setInputStream(gestoreFile.getFile(allegato, file));
                inserimentoRet = port.inserimento(username, dst);

                if (inserimentoRet.getLngErrNumber() != 0) {
                    log.error("Errore in fase di inserimento del file principale sul webservice di protocollazione: ${inserimentoRet.getStrErrString()}.");
                    throw new AttiRuntimeException("Errore in fase di inserimento del file principale sul webservice di protocollazione: ${inserimentoRet.getStrErrString()}.");
                }
                xmlDocArea.addAllegato(inserimentoRet.getLngDocID(), file.nome, "Allegato n. " + (i++) + ": " + allegato.titolo, null);
            }
        }

        // eseguo la protocollazione:
        allegatoInterceptor.setInputStream(xmlDocArea.toXmlInputStream());

        // creo il token di protocollazione: se lo trovo ed ha successo, vuol dire che ho già protocollato:
        token = tokenIntegrazioneService.beginTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO);
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

        ProtocollazioneRet protocollazioneRet = null;
        try {
            protocollazioneRet = port.protocollazione(username, dst);
            log.debug("protocollazioneRet: ${protocollazioneRet.getLngNumPG()}/${protocollazioneRet.getLngAnnoPG()}")

            if (protocollazioneRet.getLngErrNumber() != 0) {
                throw new Exception("Errore in fase di protocollazione via webservice: ${protocollazioneRet.getStrErrString()}.");
            }
        } catch (Exception e) {
            log.error("Errore nella chiamata alla protocollazione webservice: ${e.getMessage()}", e);
            // elimino il token e interrompo la transazione del token (così si possono fare più tentativi)
            tokenIntegrazioneService.stopTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO);
            throw new AttiRuntimeException("Errore in fase di protocollazione via webservice: ${e.getMessage()}.", e);
        }
        log.info("Protocollazione Webservice effettuata sul documento ${atto.id}: ${protocollazioneRet.getLngNumPG()}/${protocollazioneRet.getLngAnnoPG()} in data ${protocollazioneRet.getStrDataPG()}")

        // la prima cosa che faccio dopo la protocollazione è salvare il record su db:
        tokenIntegrazioneService.setTokenSuccess("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO, "[numero:${protocollazioneRet.getLngNumPG()}, anno:${protocollazioneRet.getLngAnnoPG()}, data:'${protocollazioneRet.getStrDataPG()}']");

        atto.numeroProtocollo = protocollazioneRet.getLngNumPG();
        atto.annoProtocollo = protocollazioneRet.getLngAnnoPG();
        atto.dataNumeroProtocollo = Date.parse("dd/MM/yyyy", protocollazioneRet.getStrDataPG());

        atto.save()

        // elimino il token: questo avverrà solo se la transazione "normale" di grails andrà a buon fine:
        tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
    }

    String getMovimento(IProtocollabile protocollabile) {
        switch (protocollabile.movimento) {
            case IProtocollabile.Movimento.PARTENZA:
                return XmlProtocollazioneDOCArea.FLUSSO_USCITA

            case IProtocollabile.Movimento.ARRIVO:
                return XmlProtocollazioneDOCArea.FLUSSO_ENTRATA

            case IProtocollabile.Movimento.INTERNO:
            default:
                return XmlProtocollazioneDOCArea.FLUSSO_INTERNO
        }
    }

    private String getTipoDocumento(IProtocollabile atto) {
        if (atto instanceof Delibera) {
            return protocolloDOCAreaConfig.getCodiceTipoDocumentoDelibera()
        } else if (atto instanceof Determina) {
            return protocolloDOCAreaConfig.getCodiceTipoDocumentoDetermina()
        }

        throw new AttiRuntimeException("Nessun tipo documento specificato per il documento ${atto}")
    }

    @Override
    List<Classifica> getListaClassificazioni(String filtro, String codiceUoProponente) {
        return protocolloEsterno.getListaClassificazioni(filtro, codiceUoProponente);
    }

    @Override
    List<Fascicolo> getListaFascicoli(String filtro, String codiceClassifica, Date classificaDal, String codiceUoProponente) {
        return protocolloEsterno.getListaFascicoli(filtro, codiceClassifica, classificaDal, codiceUoProponente);
    }

    @Override
    void creaFascicolo(String numero, String anno, String descrizione, String parent_progressivo, String classifica) {
        protocolloEsterno.creaFascicolo(numero, anno, descrizione, parent_progressivo, classifica)
    }

    @Override
    List<Documento> getListaDocumenti(DatiRicercaDocumento datiRicerca) {
        return protocolloEsterno.getListaDocumenti(datiRicerca)
    }

    @Override
    InputStream downloadFile(String docNum) {
        return protocolloEsterno.downloadFile(docNum)
    }

    @Transactional(readOnly = true)
    String getCodiceUnitaWs(So4UnitaPubb so4unitaPubb) {
        String query = """select unita from uo_so4_docarea where progr_unita_organizzativa = :progr and rownum = 1""";

        Sql sql = new Sql(dataSource);
        def rows = sql.rows(query, [progr: so4unitaPubb.progr]);
        for (def row : rows) {
            return row.unita;
        }

        throw new AttiRuntimeException("Non è possibile protocollare: non è stata trovata l'unità GS4 corrispondente all'unità SO4: ${so4unitaPubb.progr}.");
    }
}
