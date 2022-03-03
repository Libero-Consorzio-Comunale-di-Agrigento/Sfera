package it.finmatica.atti.integrazioni.protocollo

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.AbstractProtocolloEsterno
import it.finmatica.atti.IDocumentaleEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.commons.TokenIntegrazione
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.documenti.*
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.integrazioniws.ads.agspr.Movimento
import it.finmatica.atti.integrazioniws.ads.docarea.*
import org.apache.cxf.endpoint.Client
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.transport.http.HTTPConduit
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component("protocolloDocAreaDaImpostazioni")
@Lazy
class ProtocolloDOCAreaParametriDaImpostazioni extends AbstractProtocolloEsterno {

    private static final Logger log = Logger.getLogger(ProtocolloDOCAreaParametriDaImpostazioni.class)

    @Autowired
    private ProtocolloDOCAreaConfig protocolloDOCAreaConfig
    @Autowired
    private IDocumentaleEsterno gestoreDocumentaleEsterno
    @Autowired
    private TokenIntegrazioneService tokenIntegrazioneService
    @Autowired
    private SpringSecurityService springSecurityService
    @Autowired
    private IGestoreFile gestoreFile

    @Override
    @Transactional
    void protocolla(IProtocollabile atto) {
        // ottengo il lock pessimistico per evitare doppie protocollazioni.
        atto.lock();

        // controllo che il documento non sia già protocollato
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
            atto.save(failOnError: true, flush: true)

            // allineo il documento gdm:
            gestoreDocumentaleEsterno.salvaDocumento(atto);

            // elimino il token: tutto è andato bene e verrà eliminato solo alla commit sull transaction principale
            tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
            return;
        }

        ProtocollazioneRet protocollazioneRet = null;

        try {
            // vbg / vbg VBG_GIMIGNANO
            String username = protocolloDOCAreaConfig.getUtenteWebService()
            String password = protocolloDOCAreaConfig.getPasswordWebService()
            String codiceEnteLogin = protocolloDOCAreaConfig.getCodiceEnte()

            if (protocolloDOCAreaConfig.usaUtenteCorrente) {
                // devo prendere l'utente corrente di sessione e criptarne la password:
                username = springSecurityService.currentUser.nominativo;
                password = tokenIntegrazioneService.getTokenAutenticazioneAd4(username);
            }

            // preparo l'xml docarea:
            XmlProtocollazioneDOCArea xmlDocArea = xmlDocArea(atto);

            // preparo il client webservice
            DOCAREAProtoSoap port = getClient();
            String dst = login(port, codiceEnteLogin, username, password);

            // inserisco il documento principale
            InputStream inputStreamAllegato = gestoreFile.getFile(atto, atto.testo);
            long idAllegato = inserimento(port, username, dst, inputStreamAllegato);
            xmlDocArea.setAllegatoPrincipale(idAllegato, atto.testo.nome, "Documento Principale", "");

            // inserisco gli allegati al documento
            for (Allegato allegato : atto.allegati) {

                // salto gli allegati non più validi.
                if (!allegato.valido)
                    continue;

                int i = 1;
                for (FileAllegato file : allegato.fileAllegati) {
                    inputStreamAllegato = gestoreFile.getFile(allegato, file);
                    idAllegato = inserimento(port, username, dst, inputStreamAllegato);
                    xmlDocArea.addAllegato(idAllegato, file.nome, "Allegato n. " + (i++) + ": " + allegato.titolo, null);
                }
            }

            // inserisco il testo dei visti
            for (VistoParere v : atto.visti) {

                // salto i visti non più validi.
                if (!v.valido)
                    continue;

                inputStreamAllegato = gestoreFile.getFile(v, v.testo);
                idAllegato = inserimento(port, username, dst, inputStreamAllegato);
                xmlDocArea.addAllegato(idAllegato, v.testo.nome, v.tipologia.titolo, null);

                // aggiungo gli allegati dei visti:
                for (Allegato allegato : v.allegati) {

                    // salto gli allegati non più validi.
                    if (!allegato.valido)
                        continue;

                    int i = 1;
                    for (FileAllegato file : allegato.fileAllegati) {
                        inputStreamAllegato = gestoreFile.getFile(allegato, file);
                        idAllegato = inserimento(port, username, dst, inputStreamAllegato);
                        xmlDocArea.addAllegato(idAllegato, file.nome, "Allegato n. " + (i++) + ": " + allegato.titolo, null);
                    }
                }
            }

            // infine, tento la protocollazione:
            protocollazioneRet = protocollazione(port, username, dst, xmlDocArea);

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

        atto.save(failOnError: true, flush: true)

        // elimino il token: questo avverrà solo se la transazione "normale" di grails andrà a buon fine:
        tokenIntegrazioneService.endTokenTransaction("${atto.id}", TokenIntegrazione.TIPO_PROTOCOLLO)
    }

    private DOCAREAProtoSoap getClient() throws MalformedURLException {
        DOCAREAProto ss = new DOCAREAProto(new URL(protocolloDOCAreaConfig.getUrlWebService()));
        DOCAREAProtoSoap port = ss.getDOCAREAProto();
        return port;
    }

    private String login(DOCAREAProtoSoap port, String codiceEnteLogin, String username, String password) throws MalformedURLException {
        Client proxy = ClientProxy.getClient(port);
        HTTPConduit http = (HTTPConduit) proxy.getConduit();
        HTTPClientPolicy httpClientPolicy = http.getClient();
        if (httpClientPolicy == null) {
            httpClientPolicy = new HTTPClientPolicy();
            http.setClient(httpClientPolicy);
        }
        httpClientPolicy.setAllowChunking(false);

        // eseguo il login
        LoginRet loginRet = port.login(codiceEnteLogin, username, password);

        if (loginRet.getLngErrNumber() != 0) {
            log.error("Errore in fase di login al webservice di protocollazione: ${loginRet.getStrErrString()}.");
            throw new AttiRuntimeException("Errore in fase di login al webservice di protocollazione: ${loginRet.getStrErrString()}.");
        }

        return loginRet.getStrDST();
    }

    private long inserimento(DOCAREAProtoSoap port, String username, String dst, InputStream inputStream) throws FileNotFoundException {
        // aggiungo il testo dell'atto:
        addAllegato(port, inputStream);

        InserimentoRet inserimentoRet = port.inserimento(username, dst);

        if (inserimentoRet.getLngErrNumber() != 0) {
            log.error("Errore in fase di inserimento del file principale sul webservice di protocollazione: ${inserimentoRet.getStrErrString()}.");
            throw new AttiRuntimeException("Errore in fase di inserimento del file principale sul webservice di protocollazione: ${inserimentoRet.getStrErrString()}.");
        }

        return inserimentoRet.getLngDocID();
    }

    private ProtocollazioneRet protocollazione(DOCAREAProtoSoap port, String username, String dst, XmlProtocollazioneDOCArea xmlDocArea) throws FileNotFoundException, MalformedURLException {
        addAllegato(port, xmlDocArea.toXmlInputStream());
        ProtocollazioneRet protocollazioneRet = port.protocollazione(username, dst);

        if (protocollazioneRet.getLngErrNumber() != 0) {
            throw new AttiRuntimeException("Errore in fase di protocollazione via webservice: ${protocollazioneRet.getStrErrString()}.");
        }

        return protocollazioneRet;
    }

    private void addAllegato(DOCAREAProtoSoap port, InputStream inputStream) {
        Client proxy = ClientProxy.getClient(port);
        if (proxy.getOutInterceptors().size() == 0) {
            proxy.getOutInterceptors().add(new AllegatoInterceptor());
        }
        AllegatoInterceptor alle = (AllegatoInterceptor) proxy.getOutInterceptors().get(0);
        alle.setInputStream(inputStream);
    }

    private XmlProtocollazioneDOCArea xmlDocArea(IProtocollabile atto) {
        // preparo l'xml docarea:
        XmlProtocollazioneDOCArea xmlDocArea = new XmlProtocollazioneDOCArea()
        xmlDocArea.setOggetto(getOggetto(atto))
        xmlDocArea.setAmministrazione(springSecurityService.principal.amm().codice, springSecurityService.principal.amm().descrizione, springSecurityService.principal.amministrazione.soggetto.indirizzoWeb)
        xmlDocArea.setFlusso(getMovimento(atto))

        xmlDocArea.setAOO(protocolloDOCAreaConfig.getCodiceAoo())
        xmlDocArea.setNomeApplicativo(protocolloDOCAreaConfig.getCodiceApplicativo())
        // FIXME: NOME APPLICATIVO

        // nessuna uo mittente/destinataria: in questo modo viene impostata amm+aoo e basta sia come mitt che come dest.
        xmlDocArea.setUoDestinataria(protocolloDOCAreaConfig.getCodiceUnitaDestinataria(atto))
        xmlDocArea.setUoMittente(protocolloDOCAreaConfig.getCodiceUnita(atto))
        xmlDocArea.addParametroApplicativo("uo", protocolloDOCAreaConfig.getCodiceUnita(atto))
        // uo proponente.

        xmlDocArea.setClassifica(protocolloDOCAreaConfig.getCodiceClassifica(atto))

        if (protocolloDOCAreaConfig.getFascicoloNumero(atto) != null) {
            xmlDocArea.setFascicolo(protocolloDOCAreaConfig.getFascicoloNumero(atto)
                    , protocolloDOCAreaConfig.getFascicoloAnno(atto)
                    , protocolloDOCAreaConfig.getFascicoloOggetto(atto))
        }

        return xmlDocArea
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

    private String getOggetto(IAtto atto) {
        return "${atto.tipologiaDocumento.titolo} ${atto.numeroAtto} / ${atto.annoAtto} - ${atto.oggetto}"
    }

    private String getTipoDocumento(IProtocollabile atto) {
        if (atto instanceof Delibera) {
            return protocolloDOCAreaConfig.getCodiceTipoDocumentoDelibera()
        } else if (atto instanceof Determina) {
            return protocolloDOCAreaConfig.getCodiceTipoDocumentoDetermina()
        }

        throw new AttiRuntimeException("Nessun tipo documento specificato per il documento ${atto}")
    }
}
