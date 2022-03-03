package it.finmatica.atti.integrazioni

import grails.plugin.springsecurity.SpringSecurityService
import groovy.sql.Sql
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.documenti.*
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.l190.CasaDiVetroConfig
import it.finmatica.atti.integrazioniws.ads.l190.PubblicaAttoService
import it.finmatica.atti.integrazioniws.ads.l190.PubblicaAttoServiceName
import it.finmatica.atti.integrazioniws.ads.l190.WSPasswordEncrypter
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.apache.cxf.endpoint.Client
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.transport.http.HTTPConduit
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy

import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.sql.DataSource
import javax.xml.ws.BindingProvider
import javax.xml.ws.soap.SOAPBinding

class CasaDiVetroService {

    public static final String PUBBLICA_TESTO        = "TESTO"
    public static final String PUBBLICA_STAMPA_UNICA = "STAMPA_UNICA"
    public static final String PUBBLICA_NESSUNO      = "NESSUNO"

    SpringSecurityService springSecurityService
    StampaUnicaService    stampaUnicaService
    CasaDiVetroConfig     casaDiVetroConfig
    IGestoreFile          gestoreFile
    PubblicaAttoService l190ServiceClient

    private static String QUERY_CONTRATTI                    = '''select 1 from l190_contratti where cig = :cig and numero_proposta = :numero_proposta and anno_proposta = :anno_proposta'''
    //private static String QUERY_CONTRATTI_DATA_INIZIO_LAVORI = '''select 1 from l190_contratti where data_inizio is null and cig = :cig and numero_proposta = :numero_proposta and anno_proposta = :anno_proposta'''
    private static String QUERY_CONTRATTI_DATA_INIZIO_LAVORI = '''select 1 from l190_contratti where data_inizio is null and cig = :cig'''


    DataSource dataSource

    String login (String username) {
        return login(l190ServiceClient, username)
    }

    String login (PubblicaAttoService port, String username, String password = null) {
        if (password == null) {
            password = Ad4Utente.get(username).password;
        }

        String cryptedPwd = WSPasswordEncrypter.encrypt(password ?: "")
        String token = port.login(username ?: "", cryptedPwd)

        return token;
    }

    void logout (String token) {
        logout(l190ServiceClient, token)
    }

    void logout (PubblicaAttoService port, String token) {
        port.logout(token)
    }

    String getUrlDocumentoSePresente (def documento) {

        // se ancora il documento non è stato salvato, non ci provo neanche a fare il resto
        if (!(documento.id > 0)) {
            return null;
        }

        // se non sono integrato con la casa di vetro, non faccio niente:
        if (!Impostazioni.INTEGRAZIONE_CASA_DI_VETRO.abilitato) {
            return null;
        }

        // controllo che il documento sia in casa di vetro:
        def doc = documento.domainObject;
        String token = login(casaDiVetroConfig.getUtenteWebService());
        if (esisteAtto(doc, token)) {
            return getUrlDocumento(doc);
        }
    }

    String getUrlDocumento (IDocumento doc, String token) {
        String url = casaDiVetroConfig.getUrlCasaDiVetro() + "?token=${token}";
        IProposta proposta = (doc instanceof IAtto) ? doc.proposta : doc;

        url += "&annoProposta=${proposta.annoProposta}"
        url += "&numeroProposta=${proposta.numeroProposta}"
        url += "&registroProposta=${proposta.registroProposta.codice}"
        if (doc instanceof Delibera) {
            url += "&tipo=DELIBERA";
        } else if (doc instanceof PropostaDelibera) {
            url += "&tipo=PROPOSTA_DELIBERA";
        } else if (doc instanceof Determina) {
            if (doc.numeroDetermina > 0) {
                url += "&tipo=DETERMINA"
            } else {
                url += "&tipo=PROPOSTA_DETERMINA"
            }
        }

        String codici = getCodici(proposta)
        if (codici != null) {
            url += "&codiciSezioni=" + codici
        }

        return url;
    }

    private String getCodici (IProposta proposta) {
        String codicePubblicazione
        if (proposta instanceof PropostaDelibera) {
            codicePubblicazione = casaDiVetroConfig.getTipologia(proposta, casaDiVetroConfig.getSezioneDelibere())
        } else if (proposta instanceof Determina) {
            codicePubblicazione = casaDiVetroConfig.getTipologia(proposta, casaDiVetroConfig.getSezioneDetermine())
        }

        String codiciOggettiRicorrenti = proposta.oggettoRicorrente?.id ? casaDiVetroConfig.getOggettoRicorrente(proposta, null) : null;

        return codiciOggettiRicorrenti ?: casaDiVetroConfig.getCategoria(proposta) ?: codicePubblicazione
    }

    String getUrlDocumento (IDocumento doc) {
        // quando ottengo l'url del documento non faccio logout (altrimenti vanificherebbe l'utilità del token)
        String token = login(springSecurityService.currentUser?.id);
        return getUrlDocumento(doc, token);
    }

    boolean esisteAtto (def doc) {
        return esisteAtto(l190ServiceClient, doc);
    }

    boolean esisteAtto (def doc, String token) {
        return esisteAtto(l190ServiceClient, doc, token);
    }

    boolean esisteAtto (PubblicaAttoService port, def doc) {
        def proposta = (doc instanceof IAtto) ? doc.proposta : doc

        // se la proposta non è numerata ritorno false.
        if (!(proposta.numeroProposta > 0)) {
            return false
        }

        String token = login(port, casaDiVetroConfig.getUtenteWebService())
        try {
            return esisteAtto(port, doc, token)
        } finally {
            logout(port, token)
        }
    }

    boolean esisteAtto (PubblicaAttoService port, def doc, String token) {
        def proposta = (doc instanceof IAtto) ? doc.proposta : doc;
        // se la proposta non è numerata ritorno false.
        if (!(proposta.numeroProposta > 0)) {
            return false;
        }

        return port.esisteAtto(token, String.valueOf(proposta.getAnnoProposta()), String.valueOf(proposta.getNumeroProposta()),
                               proposta.getRegistroProposta()?.codice).booleanValue();
    }

    boolean verificaSezioni (def doc) {
        PubblicaAttoService port = l190ServiceClient
        String token = login(port, casaDiVetroConfig.getUtenteWebService())
        try {
            IProposta proposta = (doc instanceof IAtto) ? doc.proposta : doc;

            // se l'atto non è stato già portato sulla casa di vetro, non lo aggiorno.
            if (!esisteAtto(l190ServiceClient, proposta, token)) {
                return true;
            }

            String response = l190ServiceClient.warningSezioneContenuti(
                    token
                    , String.valueOf(proposta.annoProposta)
                    , String.valueOf(proposta.numeroProposta)
                    , proposta.registroProposta.codice
                    , getCodici(proposta)
            );
            return (response == null)
        } catch (Exception e) {
            log.error("Errore nella chiamata warningSezioneContenuti di un atto: ${e.getMessage()}", e)
            return false
        }

        finally {
            logout(port, token);
        }
    }

    void inserisci (def documento) {
        IProposta proposta = (documento instanceof IAtto) ? documento.proposta : documento;
        IAtto atto = (documento instanceof IAtto) ? documento : documento.atto;

        PubblicaAttoService port = l190ServiceClient
        String token = login(port, casaDiVetroConfig.getUtenteWebService())
        try {

            // se l'atto è stato già portato sulla casa di vetro, non lo reinserisco.
            if (esisteAtto(port, proposta)) {
                return;
            }

            String spesa = ""; // calcola la spesa sulla contabilità.
            String firmatario = (atto instanceof Determina) ? (proposta.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4?.nominativoSoggetto ?: "") : ""

            port.inserisci(
                    token
                    , String.valueOf(proposta.annoProposta)
                    , String.valueOf(proposta.numeroProposta)
                    , proposta.registroProposta.codice
                    , String.valueOf(atto?.annoAtto ?: "")
                    , String.valueOf(atto?.numeroAtto ?: "")
                    , atto?.registroAtto?.descrizione ?: ""
                    , (documento.oggetto.size() > Impostazioni.LUNGHEZZA_OGGETTO.valoreInt) ? documento.oggetto.substring(0,
                                                                                                                          Impostazioni.LUNGHEZZA_OGGETTO.valoreInt) : documento.oggetto
                    , proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.descrizione ?: ""
                    , "" // questo è il dirigente
                    , firmatario // questo è il firmatario
                    , proposta.tipologiaDocumento.titolo
                    , "" // documenti fascicolo ???
                    , spesa
                    , documento.hasProperty("codiceGara") ? (documento.codiceGara ?: "") : "" // cig
                    , proposta.tipologiaDocumento.descrizione    // tipo atto
                    , String.valueOf(proposta.id)    // id riferimento
                    , String.valueOf(proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.progr ?: "")
                    , String.valueOf(proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.dal?.format("dd/MM/yyyy") ?: "")
                    , proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.ottica?.codice ?: ""
            );

        } finally {
            logout(port, token);
        }
    }

    void aggiorna (def documento) {
        IProposta proposta = (documento instanceof IAtto) ? documento.proposta : documento;
        IAtto atto = (documento instanceof IAtto) ? documento : documento.atto;

        PubblicaAttoService port = l190ServiceClient
        String token = login(port, casaDiVetroConfig.getUtenteWebService())
        try {

            // se l'atto non è stato già portato sulla casa di vetro, non lo aggiorno.
            if (!esisteAtto(port, proposta, token)) {
                return;
            }

            String spesa = ""; // da calcolare in base alla contabilità
            String firmatario = (atto instanceof Determina) ? (proposta.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4?.nominativoSoggetto ?: "") : ""

            port.aggiorna(
                    token
                    , String.valueOf(proposta.annoProposta)
                    , String.valueOf(proposta.numeroProposta)
                    , proposta.registroProposta.codice
                    , String.valueOf(atto?.annoAtto ?: "")
                    , String.valueOf(atto?.numeroAtto ?: "")
                    , atto?.registroAtto?.descrizione ?: ""
                    , (documento.oggetto.size() > Impostazioni.LUNGHEZZA_OGGETTO.valoreInt) ? documento.oggetto.substring(0,
                                                                                                                          Impostazioni.LUNGHEZZA_OGGETTO.valoreInt) : documento.oggetto
                    , proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.descrizione ?: ""
                    , "" // questo è il dirigente
                    , firmatario // questo è il firmatario
                    , proposta.tipologiaDocumento.titolo
                    , "" // documenti fascicolo ???
                    , spesa
                    , documento.hasProperty("codiceGara") ? (documento.codiceGara ?: "") : "" // cig
                    , proposta.tipologiaDocumento.descrizione    // tipo atto
                    , String.valueOf(proposta.id)    // id riferimento
                    , String.valueOf(proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.progr ?: "")
                    , String.valueOf(proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.dal?.format("dd/MM/yyyy") ?: "")
                    , proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.ottica?.codice ?: ""
            );

        } finally {
            logout(port, token);
        }
    }

    void pubblica (IAtto doc) {
        PubblicaAttoService port = l190ServiceClient
        String token = login(port, casaDiVetroConfig.getUtenteWebService())
        try {

            // se l'atto non è stato già portato sulla casa di vetro, non lo pubblico.
            if (!esisteAtto(port, doc.proposta, token)) {
                log.warn("NON pubblico l'atto ${doc.estremiAtto} (${doc}) perchè non è presente su L190!")
                return;
            }

            // pubblico gli allegati se richiesto.
            aggiungiAllegati(port, doc, token);

            log.info("Pubblico l'atto ${doc.estremiAtto} (${doc}) su L190!")

            String firmatario = (doc instanceof Determina) ? (doc.proposta.getSoggetto(
                    TipoSoggetto.DIRIGENTE)?.utenteAd4?.nominativoSoggetto ?: "") : ""

            port.pubblica(token
                          , String.valueOf(doc.proposta.annoProposta)
                          , String.valueOf(doc.proposta.numeroProposta)
                          , doc.proposta.registroProposta.codice
                          , String.valueOf(doc.annoAtto)
                          , String.valueOf(doc.numeroAtto)
                          , doc.registroAtto?.descrizione
                          , (doc.oggetto.size() > Impostazioni.LUNGHEZZA_OGGETTO.valoreInt) ? doc.oggetto.substring(0,
                                                                                                                    Impostazioni.LUNGHEZZA_OGGETTO.valoreInt) : doc.oggetto
                          , doc.proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.descrizione ?: ""
                          , "" // questo è il dirigente
                          , firmatario // questo è il firmatario
                          , doc.tipologiaDocumento.descrizione
                          , String.valueOf(doc.proposta.id) // id riferimento
                          , ""
                         , String.valueOf(doc.proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.progr ?: "")
                         , String.valueOf(doc.proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.dal?.format("dd/MM/yyyy") ?: "")
                         , doc.proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.ottica?.codice ?: ""
            );

        } finally {
            logout(port, token);
        }
    }

    void aggiungiAllegati (PubblicaAttoService port, def doc) {
        String token = login(casaDiVetroConfig.getUtenteWebService())
        try {
            aggiungiAllegati(port, doc, token);
        } finally {
            logout(port, token);
        }
    }

    void aggiungiAllegati (PubblicaAttoService port, def atto, String token) {
        // se l'atto non è stato già portato sulla casa di vetro, non lo pubblico.
        if (!esisteAtto(port, atto.proposta)) {
            return;
        }

        // Pubblico il file richiesto a seconda delle impostazioni:
        switch (Impostazioni.CASA_DI_VETRO_FILE_PRINCIPALE.valore) {
            case CasaDiVetroService.PUBBLICA_TESTO:
                // invio il testo se il documento non è riservato oppure lo è ma l'impostazione dice di mandarli comunque.
                if (!atto.riservato || Impostazioni.CASA_DI_VETRO_RISERVATI.abilitato) {
                    aggiungiDocumentoPrincipale(port, token, atto, atto.testo);
                }
                break;

            case CasaDiVetroService.PUBBLICA_STAMPA_UNICA:
                // se non ho la stampa unica, tento di generarla.
                if (atto.stampaUnica == null) {
                    stampaUnicaService.stampaUnica(atto);
                }

                // potrebbe comunque succedere che la stampa unica non venga generata (ad es tutti i file che dovrebbero andarci sono riservati)
                if (atto.stampaUnica != null) {
                    aggiungiDocumentoPrincipale(port, token, atto, atto.stampaUnica);
                }
                break;

            // nessuno:
            case CasaDiVetroService.PUBBLICA_NESSUNO:
            default:
                break;
        }

        // aggiungo gli allegati se l'impostazione lo richiede.
        if (Impostazioni.CASA_DI_VETRO_PUBBLICA_ALLEGATI.abilitato) {
            for (Allegato allegato : atto.allegati) {
                if (allegato.valido && (!allegato.riservato || Impostazioni.CASA_DI_VETRO_RISERVATI.abilitato) && allegato.pubblicaCasaDiVetro) {
                    for (FileAllegato fileAllegato : allegato.fileAllegati) {
                        aggiungiFileAllegato(port, token, atto, allegato, fileAllegato, false);
                    }
                }
            }
        }

        // Feature #14460: viene aggiunto il testo del visto o gli allegati se le impostazioni lo richiedono
        if (Impostazioni.CASA_DI_VETRO_PUBBLICA_TESTO_VISTO.abilitato || Impostazioni.CASA_DI_VETRO_PUBBLICA_ALLEGATI_VISTO.abilitato) {
            def vistiPareriAtto = atto.visti.findAll{it.valido == true}
            def vistiPareriProposta = (atto instanceof Delibera) ? atto.proposta.visti.findAll{it.valido == true} : []
            def vistiPareri = (vistiPareriProposta ?: []) + (vistiPareriAtto ?: [])

            for (def vistoParere : vistiPareriProposta){
                if (vistiPareriAtto.findAll{ it.tipologia.codice == vistoParere.tipologia.codice}.size() > 0){
                    vistiPareri.remove(vistoParere)
                }
            }

            for (VistoParere visto : vistiPareri) {

                log.debug("Verifico il visto ${visto.id} per l'atto: ${atto.getEstremiAtto()} per l'invio in casa di vetro")
                if (Impostazioni.CASA_DI_VETRO_PUBBLICA_TESTO_VISTO.abilitato && visto.testo != null) {
                    aggiungiFileAllegato(port, token, atto, visto, visto.testo, false);
                }

                if (Impostazioni.CASA_DI_VETRO_PUBBLICA_ALLEGATI_VISTO.abilitato) {
                    for (Allegato allegato : visto.allegati) {
                        if (allegato.valido && (!allegato.riservato || Impostazioni.CASA_DI_VETRO_RISERVATI.abilitato)) {
                            for (FileAllegato fileAllegato : allegato.fileAllegati) {
                                if (fileAllegato != null) {
                                    aggiungiFileAllegato(port, token, atto, allegato, fileAllegato, false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    void elimina (IAtto atto) {
        // se la proposta non è numerata, non faccio nulla ed esco.
        if (!(atto.proposta.numeroProposta > 0)) {
            return;
        }
        PubblicaAttoService port = l190ServiceClient
        String token = login(port, casaDiVetroConfig.getUtenteWebService());
        try {
            // se l'atto non è stato già portato sulla casa di vetro, non lo pubblico.
            if (!esisteAtto(port, atto.proposta)) {
                log.warn("NON elimino l'atto ${atto.estremiAtto} (${atto}) perchè non è presente su L190!")
                return;
            }

            port.elimina(token, String.valueOf(atto.proposta.annoProposta), String.valueOf(atto.proposta.numeroProposta),
                         atto.proposta.registroProposta.codice);
        } finally {
            logout(port, token);
        }
    }

    private void aggiungiDocumentoPrincipale (PubblicaAttoService port, String token, IAtto atto, FileAllegato file) {
        aggiungiFileAllegato(port, token, atto, atto, file, true)
    }

    private void aggiungiFileAllegato (PubblicaAttoService port, token, IAtto atto, IDocumentoEsterno documentoEsterno, FileAllegato file, boolean filePrincipale) {
        log.info("Aggiungo l'allegato ${file.nome} in casa di vetro per l'atto: ${atto.getEstremiAtto()}")

        File temp = File.createTempFile("l190", "l190")
        InputStream inputStream
        OutputStream outputStream
        try {
            inputStream = gestoreFile.getFile(documentoEsterno, file)
            outputStream = temp.newOutputStream()
            IOUtils.copyLarge(inputStream, outputStream)
            IOUtils.closeQuietly((OutputStream) outputStream)
            DataHandler dataHandler = new DataHandler(new FileDataSource(temp))

            port.aggiungiAllegato(token
                                  , String.valueOf(atto.proposta.annoProposta ?: "")
                                  , String.valueOf(atto.proposta.numeroProposta ?: "")
                                  , String.valueOf(atto.proposta.registroProposta?.codice ?: "")
                                  , filePrincipale ? "Y" : "N"
                                  , dataHandler
                                  , file.nome
                                  , file.contentType)

        } finally {
            FileUtils.deleteQuietly(temp)
            IOUtils.closeQuietly((InputStream) inputStream)
            IOUtils.closeQuietly((OutputStream) outputStream)
        }
    }

    boolean esisteContratto (String cig, Long numero_proposta, Long anno_proposta) {
        Sql sql = new Sql(dataSource);
        return sql.rows(QUERY_CONTRATTI, [cig: cig, numero_proposta: numero_proposta, anno_proposta: anno_proposta]).size() > 0
    }

    boolean esisteContrattoSenzaDataInizio (String cig, Long numero_proposta, Long anno_proposta) {
        Sql sql = new Sql(dataSource);
        return sql.rows(QUERY_CONTRATTI_DATA_INIZIO_LAVORI, [cig: cig, numero_proposta: numero_proposta, anno_proposta: anno_proposta]).size() > 0
    }
}
