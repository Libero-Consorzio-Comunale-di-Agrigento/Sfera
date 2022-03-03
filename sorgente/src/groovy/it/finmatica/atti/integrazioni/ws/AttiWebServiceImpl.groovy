package it.finmatica.atti.integrazioni.ws

import atti.ricerca.MascheraRicercaDocumento
import grails.compiler.GrailsCompileStatic
import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.documenti.IProposta
import it.finmatica.atti.integrazioni.documenti.AllegatiObbligatori
import org.springframework.transaction.annotation.Transactional
import groovy.transform.CompileDynamic
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.IDocumentaleEsterno
import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.IFileAllegato
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.commons.GestoreFileDataSource
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.DeliberaService
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.DeterminaService
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.ISoggettoDocumento
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.PropostaDeliberaService
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.DeterminaEsterna
import it.finmatica.atti.integrazioni.DocumentiEsterniService
import it.finmatica.atti.integrazioni.PropostaDeliberaEsterna
import it.finmatica.atti.integrazioni.ws.dati.*
import it.finmatica.gestioneiter.IGestoreCompetenze
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAzione
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgPulsante
import it.finmatica.gestioneiter.motore.WkfIter
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.so4.login.So4UserDetail
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

import javax.activation.DataHandler

@Transactional
@Component("attiWebService")
@GrailsCompileStatic
class AttiWebServiceImpl implements AttiWebService {

    private static final Logger log = Logger.getLogger(AttiWebServiceImpl.class)

    @Autowired
    PropostaDeliberaService propostaDeliberaService
    @Autowired
    DocumentiEsterniService documentiEsterniService
    @Autowired
    IDocumentaleEsterno     gestoreDocumentaleEsterno
    @Autowired
    SpringSecurityService   springSecurityService
    @Autowired
    IGestoreCompetenze      gestoreCompetenze
    @Autowired
    DeterminaService        determinaService
    @Autowired
    DeliberaService         deliberaService
    @Autowired
    WkfIterService          wkfIterService
    @Autowired
    IGestoreFile            gestoreFile

    /*
     * Metodi web-service esposti:
     */

    Documento numeraAtto (Soggetto operatore, String ente, Documento documento) {
        try {
            // verifico che questo metodo sia invocabile per il tipo di documento passato
            verificaValiditaTipoDocumento("numera", documento.tipo, [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO])

            // il webservice è protetto da basic-authentication. Normalmente viene fatto login con una utenza di servizio.
            // Con questo metodo "rifaccio" il login con l'operatore "vero e proprio" con cui risulteranno fatte le operazioni.
            login(operatore, ente)

            // eseguo le operazioni
            switch (documento.tipo) {
                case Determina.TIPO_OGGETTO:
                    return numeraDetermina(documento)

                case Delibera.TIPO_OGGETTO:
                    return numeraDelibera(documento)
            }
        } catch (Exception e) {
            log.error("Problemi durante l'operazione numeraAtto: ${e.getMessage()}", e);
            throw new AttiRuntimeException("Problemi durante l'operazione numeraAtto: ${e.message}", e)
        }
    }

    Documento creaProposta (Soggetto operatore, String ente, Documento proposta) {
        try {
            verificaValiditaTipoDocumento("creaProposta", proposta.tipo, [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO])

            // il webservice è protetto da basic-authentication. Normalmente viene fatto login con una utenza di servizio.
            // Con questo metodo "rifaccio" il login con l'operatore "vero e proprio" con cui risulteranno fatte le operazioni.
            login(operatore, ente)

            return creaPropostaAtto(proposta)

        } catch (Exception e) {
            log.error("Problemi durante l'operazione creaProposta: ${e.getMessage()}", e);
            throw new AttiRuntimeException("Problemi durante l'operazione creaProposta: ${e.message}", e)
        }
    }

    RisultatoRicerca ricercaDocumenti (Soggetto operatore, String ente, CampiRicerca campiRicerca, Integer pagina, Integer max) {
        try {
            // verifico che questo metodo sia invocabile per il tipo di documento passato
            verificaValiditaTipoDocumento("ricercaAtto", campiRicerca.tipo, [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO])

            // il webservice è protetto da basic-authentication. Normalmente viene fatto login con una utenza di servizio.
            // Con questo metodo "rifaccio" il login con l'operatore "vero e proprio" con cui risulteranno fatte le operazioni.
            login(operatore, ente)

            return ricerca(campiRicerca, pagina, max)
        } catch (Exception e) {
            log.error("Problemi durante l'operazione ricercaDocumenti: ${e.getMessage()}", e);
            throw new AttiRuntimeException("Problemi durante l'operazione ricercaDocumenti: ${e.message}", e)
        }
    }

    Documento annullaAtto (Soggetto operatore, String ente, Documento documento) {
        try {
            // il webservice è protetto da basic-authentication. Normalmente viene fatto login con una utenza di servizio.
            // Con questo metodo "rifaccio" il login con l'operatore "vero e proprio" con cui risulteranno fatte le operazioni.
            login(operatore, ente)

            // eseguo l'operazione
            return annullaDetermina(documento)
        } catch (Exception e) {
            log.error("Problemi durante l'operazione annullaAtto: ${e.getMessage()}", e)
            throw new AttiRuntimeException("Problemi durante l'operazione annullaAtto: ${e.message}", e)
        }
    }

    Documento getDocumento (Soggetto operatore, String ente, long idDocumento) {
        try {
            String tipo = getTipo(idDocumento)

            // verifico che questo metodo sia invocabile per il tipo di documento passato
            verificaValiditaTipoDocumento("ricercaDocumento", tipo, [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO])

            // il webservice è protetto da basic-authentication. Normalmente viene fatto login con una utenza di servizio.
            // Con questo metodo "rifaccio" il login con l'operatore "vero e proprio" con cui risulteranno fatte le operazioni.
            login(operatore, ente)

            return ricercaDocumento(idDocumento, tipo)
        } catch (Exception e) {
            log.error("Problemi durante l'operazione getDocumento: ${e.getMessage()}", e)
            throw new AttiRuntimeException("Problemi durante l'operazione getDocumento: ${e.message}", e)
        }
    }

    RiferimentoFile getFile (Soggetto operatore, String ente, RiferimentoFile riferimentoFile) {
        try {
            if (riferimentoFile.tipoDocumento?.trim()?.length() == 0) {
                throw new AttiRuntimeException("Il campo 'tipoDocumento' non può essere vuoto ma deve contenere il tipo di documento di cui si vuole ottenere il file, ad esempio: 'DETERMINA', 'DELIBERA', 'ALLEGATO' etc.")
            }

            if (!(riferimentoFile.idDocumento > 0)) {
                throw new AttiRuntimeException("Il campo 'idDocumento' è obbligatorio e deve essere un numero intero positivo.")
            }

            // il webservice è protetto da basic-authentication. Normalmente viene fatto login con una utenza di servizio.
            // Con questo metodo "rifaccio" il login con l'operatore "vero e proprio" con cui risulteranno fatte le operazioni.
            login(operatore, ente)

            return getFilePrincipale(riferimentoFile)
        } catch (Exception e) {
            log.error("Problemi durante l'operazione getFile: ${e.getMessage()}", e)
            throw new AttiRuntimeException("Problemi durante l'operazione getFile: ${e.message}", e)
        }
    }

    /*
     * Metodi privati "che fanno il lavoro"
     */

    private RisultatoRicerca ricerca (CampiRicerca cp, Integer pagina, Integer max) {
        MascheraRicercaDocumento ricerca = new MascheraRicercaDocumento()
        ricerca.pageSize = max ?: Integer.MAX_VALUE
        ricerca.activePage = pagina ?: 0

        // ricerca sul singolo numero atto
        ricerca.annoAtto = (Long) cp.anno
        ricerca.numeroAtto = (Long) cp.numero
        ricerca.registroAtto = cp.registro

        ricerca.oggetto = cp.oggetto
        ricerca.tipoDocumento = cp.tipo
        ricerca.pagina((So4UserDetail) springSecurityService.principal)

        List<Documento> documenti = ricerca.listaDocumenti.collect { Map doc ->
            new Documento(numero: (Integer) doc.numeroAtto,
                          anno: (Integer) doc.annoAtto,
                          codiceRegistro: (String) doc.registroAtto,
                          numero2: (Integer) doc.numeroAtto2,
                          anno2: (Integer) doc.annoAtto2,
                          codiceRegistro2: (String) doc.registroAtto2,
                          dataNumero2: (Date) doc.dataNumero2,
                          annoProposta: (Integer) doc.annoProposta,
                          numeroProposta: (Integer) doc.numeroProposta,
                          oggetto: (String) doc.oggetto,
                          tipo: (String) doc.tipoDocumento,
                          stato: (String) doc.stato,
                          dataEsecutivita: (Date) doc.dataEsecutivita,
                          data: (Date) doc.dataAdozione,
                          id: (Long) ((doc.tipoDocumento == Delibera.TIPO_OGGETTO) ? doc.idDocumentoPrincipale : doc.idDocumento)
            )
        }

        return new RisultatoRicerca(documenti: documenti, totaleDocumenti: ricerca.totalSize)
    }

    private Documento numeraDetermina (Documento documento) {
        // controllo che ci sia l'azione di numerazione:
        Determina determina = Determina.get(documento.id)

        if (determina == null) {
            throw new AttiRuntimeException("La determina con id '${documento.id}' non esiste.")
        }

        So4UserDetail userDetail = (So4UserDetail) springSecurityService.principal

        if (determina.ente.codice != userDetail.amm().codice) {
            throw new AttiRuntimeException(
                    "Non è possibile modificare la determina con id '${documento.id}' associata all'ente ${determina.ente.codice} con il login effettuato sull'ente ${userDetail.amm().codice}")
        }

        // se è già numerata, esco:
        if (determina.numeroDetermina > 0) {
            return toDocumento(determina)
        }

        // se l'utente non ha le competenze di modifica, do' errore:
        def competenze = gestoreCompetenze.getCompetenze(determina)
        if (!competenze.modifica) {
            throw new AttiRuntimeException(
                    "L'utente ${userDetail.username} non ha i diritti di modifica sulla Proposta di Determina ${determina.estremiAtto}.")
        }

        WkfAzione azione = getAzione(determina.iter, "determinaAction", "numeraDetermina")
        if (azione == null) {
            throw new AttiRuntimeException("Non è prevista la numerazione della determina nel nodo in cui si trova la proposta.")
        }

        determina = determinaService.numeraDetermina(determina)
        gestoreDocumentaleEsterno.salvaDocumento(determina)

        return toDocumento(determina)
    }

    private WkfAzione getAzione (WkfIter iter, String nomeBean, String nomeMetodo) {
        if (iter == null) {
            return null
        }

        for (WkfCfgPulsante pulsante : iter.stepCorrente.cfgStep.cfgPulsanti) {
            WkfAzione azione = pulsante.azioni.find { it.nomeBean == nomeBean && it.nomeMetodo == nomeMetodo }
            if (azione != null) {
                return azione
            }
        }

        return null
    }

    @CompileDynamic
    private Documento annullaDetermina (Documento documento) {

        Determina determina

        if (documento.id != null) {
            determina = Determina.get(documento.id)
        } else {
            determina = Determina.createCriteria().get() {
                eq("annoDetermina", documento.anno)
                eq("numeroDetermina", documento.numero)
                eq("registroDetermina.codice", documento.codiceRegistro)
            }?.toDTO()
        }

        if (determina == null) {
            if (documento.id != null) {
                throw new AttiRuntimeException("La determina con id '${documento.id}' non esiste.")
            } else {
                throw new AttiRuntimeException(
                        "La determina con '${documento.anno}' - '${documento.numero}'' - '${documento.codiceRegistro}' non esiste.")
            }
        }

        if (determina.ente.codice != springSecurityService.principal.amm().codice) {
            throw new AttiRuntimeException(
                    "Non è possibile modificare la determina con id '${documento.id}' associata all'ente ${determina.ente.codice} con il login effettuato sull'ente ${springSecurityService.principal.amm().codice}")
        }

        // se l'utente non ha le competenze di modifica, do' errore:
        def competenze = gestoreCompetenze.getCompetenze(determina)
        if (!competenze.modifica) {
            throw new AttiRuntimeException(
                    "L'utente ${springSecurityService.currentUser.nominativo} non ha i diritti di modifica sulla Proposta di Determina ${determina.estremiAtto}.")
        }

        determinaService.annullaDetermina(determina, null)

        return toDocumento(determina)
    }

    private Documento ricercaDocumento (long idDocumento, String tipo) {
        switch (tipo) {

            case Determina.TIPO_OGGETTO:
                Determina determina = Determina.get(idDocumento)
                if (determina == null) {
                    throw new AttiRuntimeException("Non ho trovato la Determina con id: ${idDocumento}")
                }
                return toDocumento(determina)

            case Delibera.TIPO_OGGETTO:
                Delibera delibera = Delibera.get(idDocumento)
                if (delibera == null) {
                    PropostaDelibera propostaDelibera = PropostaDelibera.get(idDocumento)
                    if (propostaDelibera == null) {
                        throw new AttiRuntimeException("Non ho trovato la Delibera con id: ${idDocumento}")
                    }

                    return toDocumento(propostaDelibera)
                }
                return toDocumento(delibera)
        }
    }

    private Documento numeraDelibera (Documento documento) {
        // controllo che ci sia l'azione di numerazione:
        Delibera delibera = Delibera.get(documento.id)

        if (delibera == null) {
            throw new AttiRuntimeException("La delibera con id '${documento.id}' non esiste.")
        }


        So4UserDetail userDetail = (So4UserDetail) springSecurityService.principal

        if (delibera.ente.codice != userDetail.amm().codice) {
            throw new AttiRuntimeException(
                    "Non è possibile modificare la delibera con id '${documento.id}' associata all'ente ${delibera.ente.codice} con il login effettuato sull'ente ${userDetail.amm().codice}")
        }

        // se è già numerata, esco:
        if (delibera.numeroDelibera > 0) {
            return toDocumento(delibera)
        }

        // se l'utente non ha le competenze di modifica, do' errore:
        def competenze = gestoreCompetenze.getCompetenze(delibera)
        if (!competenze.modifica) {
            throw new AttiRuntimeException(
                    "L'utente ${userDetail.username} non ha i diritti di modifica sulla Delibera ${delibera.estremiAtto}.")
        }

        WkfAzione azione = getAzione(delibera.iter, "deliberaAction", "numeraDelibera")
        if (azione == null) {
            throw new AttiRuntimeException("Non è prevista la numerazione della delibera nel nodo in cui si trova la proposta.")
        }

        delibera = deliberaService.numeraDelibera(delibera)
        gestoreDocumentaleEsterno.salvaDocumento(delibera)

        return toDocumento(delibera)
    }

    private void login (Soggetto operatore, String ente) {
        AttiUtils.eseguiAutenticazione(operatore.utenteAd4, ente)
    }

    private void verificaValiditaTipoDocumento (String nomeMetodo, def tipo, List<String> tipiDocumentoValidi) {
        if (!tipiDocumentoValidi.contains(tipo)) {
            throw new AttiRuntimeException(
                    "Non è possibile eseguire il metodo ${nomeMetodo} su un documento di tipo: ${tipo}, i tipi documento accettati sono: ${tipiDocumentoValidi}")
        }
    }

    private String getTipo (long id) {
        if (Determina.get(id) != null) {
            return Determina.TIPO_OGGETTO
        } else if (Delibera.get(id) != null) {
            return Delibera.TIPO_OGGETTO
        } else if (it.finmatica.atti.documenti.Allegato.get(id) != null) {
            return it.finmatica.atti.documenti.Allegato.TIPO_OGGETTO
        } else if (it.finmatica.atti.documenti.Certificato.get(id) != null) {
            return it.finmatica.atti.documenti.Certificato.TIPO_OGGETTO
        } else if (PropostaDelibera.get(id) != null) {
            return Delibera.TIPO_OGGETTO
        } else if (it.finmatica.atti.documenti.VistoParere.get(id)) {
            return it.finmatica.atti.documenti.VistoParere.TIPO_OGGETTO
        }

        throw new AttiRuntimeException("Non ho trovato il tipo documento con id ${id}")
    }

    private Documento toDocumento (Determina determina) {
        Documento documento = new Documento(id: determina.id, oggetto: determina.oggetto)

        documento.codiceTipologia = determina.tipologia.codiceEsterno

        if (determina.numeroProposta > 0) {
            documento.tipo = Determina.TIPO_OGGETTO
            documento.numeroProposta = determina.numeroProposta
            documento.annoProposta = determina.annoProposta
            documento.dataProposta = determina.dataNumeroProposta
        }

        if (determina.numeroDetermina > 0) {
            documento.tipo = Determina.TIPO_OGGETTO
            documento.numero = determina.numeroDetermina
            documento.anno = determina.annoDetermina
            documento.codiceRegistro = determina.registroDetermina.codice
            documento.data = determina.dataNumeroDetermina
            documento.dataEsecutivita = determina.dataEsecutivita
        }

        if (determina.numeroDetermina2) {
            documento.numero2 = determina.numeroDetermina2
            documento.anno2 = determina.annoDetermina2
            documento.codiceRegistro2 = determina.registroDetermina2.codice
            documento.dataNumero2 = determina.dataNumeroDetermina2
        }

        if (determina.numeroProtocollo > 0) {
            documento.numeroProtocollo = determina.numeroProtocollo
            documento.dataProtocollo = determina.dataNumeroProtocollo
            documento.annoProtocollo = determina.annoProtocollo
        }

        documento.dataInizioPubblicazione = determina.dataPubblicazione
        documento.dataFinePubblicazione = determina.dataFinePubblicazione
        documento.numeroAlbo = determina.numeroAlbo
        documento.annoAlbo = determina.annoAlbo
        documento.stato = determina.stato
        documento.idRiferimento = determina.idDocumentoEsterno

        documento.redattore = creaSoggetto(determina.getSoggetto(TipoSoggetto.REDATTORE))
        documento.dirigente = creaSoggetto(determina.getSoggetto(TipoSoggetto.DIRIGENTE))
        documento.funzionario = creaSoggetto(determina.getSoggetto(TipoSoggetto.FUNZIONARIO))
        documento.unitaProponente = creaSoggetto(determina.getSoggetto(TipoSoggetto.UO_PROPONENTE))?.unita

        if (determina.classificaCodice?.trim()?.length() > 0) {
            documento.classificazione = new Classificazione(codice: determina.classificaCodice, descrizione: determina.classificaDescrizione, dataValidita: determina.classificaDal)
        }

        if (determina.fascicoloNumero?.trim()?.length() > 0) {
            documento.fascicolo = new Fascicolo(numero: determina.fascicoloNumero, anno: determina.fascicoloAnno, oggetto: determina.fascicoloOggetto)
        }

        // Testo
        documento.testo = creaRiferimentoFile(Determina.TIPO_OGGETTO, determina, determina.testo)

        // Allegati
        documento.allegati = getAllegati(determina)

        // Stampa Unica
        Allegato stampaUnica = creaAllegatoStampaUnica(determina, determina.stampaUnica, determina.TIPO_OGGETTO)
        if (stampaUnica != null){
            documento.allegati.add(stampaUnica)
        }

        // VistiPareri
        documento.vistiPareri = getVistiPareri(determina)
        // Certificati
        documento.certificati = getListaCertificati(determina)
        // Soggetti
        documento.soggetti = getListaSoggetti(determina)

        return documento
    }

    @CompileDynamic
    private RiferimentoFile creaRiferimentoFile (String tipoDocumento, def documento, IFileAllegato fileAllegato) {
        if (fileAllegato == null) {
            return null
        }

        return new RiferimentoFile(idDocumento: documento.id, tipoDocumento: tipoDocumento, nome: fileAllegato.nome, idFile: fileAllegato.id)
    }

    private Documento toDocumento (Delibera delibera) {
        Documento documento = new Documento(id: delibera.id, oggetto: delibera.oggetto)

        if (delibera.propostaDelibera != null) {
            documento.tipo = Delibera.TIPO_OGGETTO
            documento.numeroProposta = delibera.propostaDelibera.numeroProposta
            documento.annoProposta = delibera.propostaDelibera.annoProposta
            documento.dataProposta = delibera.propostaDelibera.dataNumeroProposta
        }

        if (delibera.numeroDelibera > 0) {
            documento.tipo = Delibera.TIPO_OGGETTO
            documento.numero = delibera.numeroDelibera
            documento.anno = delibera.annoDelibera
            documento.data = delibera.dataAdozione
            documento.dataEsecutivita = delibera.dataEsecutivita
        }

        if (delibera.numeroProtocollo > 0) {
            documento.numeroProtocollo = delibera.numeroProtocollo
            documento.dataProtocollo = delibera.dataNumeroProtocollo
            documento.annoProtocollo = delibera.annoProtocollo
        }

        documento.dataInizioPubblicazione = delibera.dataPubblicazione
        documento.dataFinePubblicazione = delibera.dataFinePubblicazione
        documento.numeroAlbo = delibera.numeroAlbo
        documento.annoAlbo = delibera.annoAlbo
        documento.stato = delibera.stato

        documento.redattore = creaSoggetto(delibera.propostaDelibera.getSoggetto(TipoSoggetto.REDATTORE))
        documento.dirigente = creaSoggetto(delibera.propostaDelibera.getSoggetto(TipoSoggetto.DIRIGENTE))
        documento.funzionario = creaSoggetto(delibera.propostaDelibera.getSoggetto(TipoSoggetto.FUNZIONARIO))
        documento.unitaProponente = creaSoggetto(delibera.propostaDelibera.getSoggetto(TipoSoggetto.UO_PROPONENTE))?.unita

        if (delibera.classificaCodice?.trim()?.length() > 0) {
            documento.classificazione = new Classificazione(codice: delibera.classificaCodice, descrizione: delibera.classificaDescrizione, dataValidita: delibera.classificaDal)
        }

        if (delibera.fascicoloNumero?.trim()?.length() > 0) {
            documento.fascicolo = new Fascicolo(numero: delibera.fascicoloNumero, anno: delibera.fascicoloAnno, oggetto: delibera.fascicoloOggetto)
        }

        // Testo
        documento.testo = creaRiferimentoFile(Delibera.TIPO_OGGETTO, delibera, delibera.testo)


        // Allegati
        documento.allegati = getAllegati(delibera)
        // Stampa Unica
        Allegato stampaUnica = creaAllegatoStampaUnica(delibera, delibera.stampaUnica, delibera.TIPO_OGGETTO)
        if (stampaUnica != null){
            documento.allegati.add(stampaUnica)
        }
        // VistiPareri
        documento.vistiPareri = getVistiPareri(delibera)
        // Certificati
        documento.certificati = getListaCertificati(delibera)
        // Soggetti
        documento.soggetti = getListaSoggetti(delibera)

        return documento
    }

    private Documento toDocumento (PropostaDelibera propostaDelibera) {
        Documento documento = new Documento(id: propostaDelibera.id, oggetto: propostaDelibera.oggetto)

        documento.tipo = Delibera.TIPO_OGGETTO
        documento.numeroProposta = propostaDelibera.numeroProposta
        documento.annoProposta = propostaDelibera.annoProposta
        documento.dataProposta = propostaDelibera.dataNumeroProposta

        documento.stato = propostaDelibera.stato

        documento.redattore = creaSoggetto(propostaDelibera.getSoggetto(TipoSoggetto.REDATTORE))
        documento.dirigente = creaSoggetto(propostaDelibera.getSoggetto(TipoSoggetto.DIRIGENTE))
        documento.funzionario = creaSoggetto(propostaDelibera.getSoggetto(TipoSoggetto.FUNZIONARIO))
        documento.unitaProponente = creaSoggetto(propostaDelibera.getSoggetto(TipoSoggetto.UO_PROPONENTE))?.unita

        // Testo
        documento.testo = creaRiferimentoFile(Delibera.TIPO_OGGETTO, propostaDelibera, propostaDelibera.testo)

        // Allegati
        documento.allegati = getAllegati(propostaDelibera)

        // Stampa Unica
        Allegato stampaUnica = creaAllegatoStampaUnica(propostaDelibera, propostaDelibera.stampaUnica, propostaDelibera.TIPO_OGGETTO)
        if (stampaUnica != null){
            documento.allegati.add(stampaUnica)
        }

        // VistiPareri
        documento.vistiPareri = getVistiPareri(propostaDelibera)
        // Soggetti
        documento.soggetti = getListaSoggetti(propostaDelibera)

        return documento
    }

    private RiferimentoFile getFilePrincipale (RiferimentoFile riferimentoFile) {
        FileAllegato fileAllegato = FileAllegato.get(riferimentoFile.idFile)
        if (fileAllegato == null) {
            throw new AttiRuntimeException("Non ho trovato il file con id ${riferimentoFile.idFile}")
        }

        def documento = getDoc(riferimentoFile.idDocumento)
        RiferimentoFile file = new RiferimentoFile()
        file.idDocumento = riferimentoFile.idDocumento
        file.idFile = fileAllegato.id.longValue()
        file.nome = fileAllegato.nome
        file.tipoDocumento = getTipo(riferimentoFile.idDocumento)
        file.file = new DataHandler(new GestoreFileDataSource(documento, fileAllegato))
        return file
    }

    private IDocumentoEsterno getDoc (long idDocumento) {
        Determina dete = Determina.get(idDocumento)
        if (dete != null) {
            return dete
        }

        Delibera deli = Delibera.get(idDocumento)
        if (deli != null) {
            return deli
        }

        PropostaDelibera propostaDelibera = PropostaDelibera.get(idDocumento)
        if (propostaDelibera != null) {
            return propostaDelibera
        }

        it.finmatica.atti.documenti.Allegato alle = it.finmatica.atti.documenti.Allegato.get(idDocumento)
        if (alle != null) {
            return alle
        }

        it.finmatica.atti.documenti.Certificato cert = it.finmatica.atti.documenti.Certificato.get(idDocumento)
        if (cert != null) {
            return cert
        }

        it.finmatica.atti.documenti.VistoParere visto = it.finmatica.atti.documenti.VistoParere.get(idDocumento)
        if (visto != null) {
            return visto
        }

        throw new AttiRuntimeException("Documento con id ${idDocumento} non trovato.")
    }

    private List<Allegato> getAllegati (IDocumento documento) {
        if (documento.allegati == null) {
            return []
        }

        List<Allegato> lista = []
        for (it.finmatica.atti.documenti.Allegato a : documento.allegati) {
            if (!a.valido) {
                continue
            }

            Allegato allegato = new Allegato()
            allegato.tipo = a.tipoAllegato?.codiceEsterno
            allegato.titoloTipologia = a.tipoAllegato?.titolo

            allegato.titolo = a.titolo
            allegato.id = (Integer) a.id
            allegato.riferimentiFile = []

            for (FileAllegato f : a.fileAllegati) {
                if (!a.valido) {
                    continue
                }

                allegato.riferimentiFile << creaRiferimentoFile(it.finmatica.atti.documenti.Allegato.TIPO_OGGETTO, a, f)
            }

            if (allegato.riferimentiFile.size() > 0) {
                lista << allegato
            }
        }
        return lista
    }

    private Allegato creaAllegatoStampaUnica(IDocumento documento, FileAllegato stampaUnica, String tipoOggetto) {
        if (stampaUnica != null) {
            it.finmatica.atti.integrazioni.ws.dati.Allegato allegato = new it.finmatica.atti.integrazioni.ws.dati.Allegato()
            allegato.tipo = "STAMPA_UNICA"
            allegato.titoloTipologia = "STAMPA_UNICA"

            allegato.titolo = "Stampa Unica"
            allegato.id = (Integer) stampaUnica.id
            allegato.riferimentiFile = []

            allegato.riferimentiFile << creaRiferimentoFile(tipoOggetto, documento, stampaUnica)

            if (allegato.riferimentiFile.size() > 0) {
                return allegato
            }
        }
        return null
    }

    @CompileDynamic
    private List<VistoParere> getVistiPareri (IDocumento atto) {
        List<VistoParere> lista = []
        List<it.finmatica.atti.documenti.VistoParere> visti = it.finmatica.atti.documenti.VistoParere.createCriteria().list {
            if (atto instanceof Determina) {
                eq("determina", atto)
            } else if (atto instanceof Delibera) {
                or {
                    eq("propostaDelibera", atto.proposta)
                    eq("delibera", atto)
                }
            } else if (atto instanceof PropostaDelibera) {
                eq("propostaDelibera", atto)
            } else {
                eq("id", -1)
            }

            eq("valido", true)
            eq("stato", StatoDocumento.CONCLUSO)

            isNotNull("testo")
            tipologia {
                order("sequenzaStampaUnica", "asc")
            }
        }

        // se il documento è una delibera, allora verifico se ha dei pareri. Se ne ha, metto quelli in stampa unica.
        List<it.finmatica.atti.documenti.VistoParere> pareriDelibera = visti.findAll { it.delibera != null }
        if (pareriDelibera.size() > 0) {
            visti = pareriDelibera
        }

        for (it.finmatica.atti.documenti.VistoParere v : visti) {
            lista << getVistoParere(v)
        }

        return lista
    }

    private VistoParere getVistoParere (it.finmatica.atti.documenti.VistoParere vistoParere) {
        VistoParere visto = new VistoParere()
        visto.allegati = []

        visto.firmatario = creaSoggetto(vistoParere.getSoggetto(TipoSoggetto.FIRMATARIO))
        visto.unita = creaSoggetto(vistoParere.getSoggetto(TipoSoggetto.UO_DESTINATARIA))

        if (vistoParere.testo != null) {
            visto.testo = new RiferimentoFile(nome: vistoParere.testo.nome, idDocumento: vistoParere.id, idFile: vistoParere.testo.id)
        }

        visto.allegati = getAllegati(vistoParere)

        return visto
    }

    @CompileDynamic
    private List<Certificato> getListaCertificati (IAtto atto) {
        def ordineCertificati = [[tipo: it.finmatica.atti.documenti.Certificato.CERTIFICATO_IMMEDIATA_ESEGUIBILITA, secondaPubblicazione: false]
                                 , [tipo: it.finmatica.atti.documenti.Certificato.CERTIFICATO_PUBBLICAZIONE, secondaPubblicazione: false]
                                 , [tipo: it.finmatica.atti.documenti.Certificato.CERTIFICATO_ESECUTIVITA, secondaPubblicazione: false]
                                 , [tipo: it.finmatica.atti.documenti.Certificato.CERTIFICATO_AVVENUTA_PUBBLICAZIONE, secondaPubblicazione: false]
                                 , [tipo: it.finmatica.atti.documenti.Certificato.CERTIFICATO_IMMEDIATA_ESEGUIBILITA, secondaPubblicazione: true]
                                 , [tipo: it.finmatica.atti.documenti.Certificato.CERTIFICATO_PUBBLICAZIONE, secondaPubblicazione: true]
                                 , [tipo: it.finmatica.atti.documenti.Certificato.CERTIFICATO_ESECUTIVITA, secondaPubblicazione: true]
                                 , [tipo: it.finmatica.atti.documenti.Certificato.CERTIFICATO_AVVENUTA_PUBBLICAZIONE, secondaPubblicazione: true]]

        // seleziono solo quelli validi.
        for (it.finmatica.atti.documenti.Certificato certificato : atto.certificati) {
            if (!certificato.valido) {
                continue
            }

            // uso il tipo di certificato e la seconda pubblicazione come "chiave" di ordineCertificati e ad esso assegno il certificato corrente.
            ordineCertificati.find({ certificato.tipo == it.tipo && certificato.secondaPubblicazione == it.secondaPubblicazione })?.certificato = certificato
        }

        def certificati = ordineCertificati.findAll { it.certificato != null }.certificato

        List<Certificato> listaCertificati = []
        for (it.finmatica.atti.documenti.Certificato c : certificati) {
            if (c.testo != null && c.stato == StatoDocumento.CONCLUSO) {
                RiferimentoFile rf = new RiferimentoFile(nome: c.testo.nome, idDocumento: c.id, idFile: c.testo.id)
                listaCertificati.add(new Certificato(firmatario: creaSoggetto(c.getSoggetto(TipoSoggetto.FIRMATARIO)), testo: rf, tipo: c.tipo))
            }
        }
        return listaCertificati
    }

    private List<Soggetto> getListaSoggetti (IDocumento documento) {
        List<Soggetto> lista = []
        for (ISoggettoDocumento soggettoDocumento : documento.getSoggetti()) {
            lista.add(creaSoggetto(soggettoDocumento))
        }

        if (documento instanceof Delibera) {
            lista.addAll(getListaSoggetti(((Delibera) documento).propostaDelibera))
        }

        return lista
    }

    private Soggetto creaSoggetto (ISoggettoDocumento soggettoDocumento) {
        if (soggettoDocumento == null) {
            return null
        }

        UnitaOrganizzativa uo = null
        if (soggettoDocumento.unitaSo4 != null) {
            uo = new UnitaOrganizzativa(progressivo: soggettoDocumento.unitaSo4.progr
                                        , codiceOttica: soggettoDocumento.unitaSo4.ottica.codice
                                        , dal: soggettoDocumento.unitaSo4.dal
                                        , codice: soggettoDocumento.unitaSo4.codice
                                        , descrizione: soggettoDocumento.unitaSo4.descrizione)
        }

        if (soggettoDocumento.utenteAd4 != null) {
            As4SoggettoCorrente s = As4SoggettoCorrente.findByUtenteAd4(soggettoDocumento.utenteAd4)
            return new Soggetto(tipo: soggettoDocumento.tipoSoggetto.codice
                                , niAs4: s.id.toString()
                                , nome: s.nome
                                , cognome: s.cognome
                                , utenteAd4: soggettoDocumento.utenteAd4.nominativo
                                , codiceFiscale: s.codiceFiscale
                                , unita: uo)
        } else {
            return new Soggetto(tipo: soggettoDocumento.tipoSoggetto.codice, unita: uo)
        }
    }

    @CompileDynamic
    private Documento creaPropostaAtto (Documento proposta) {
        if (!(proposta.codiceTipologia?.length() > 0)) {
            throw new AttiRuntimeException("Non è possibile creare una proposta senza aver specificato il 'codiceTipologia'.")
        }

        switch (proposta.tipo) {
            case Determina.TIPO_OGGETTO:
                Determina determina = documentiEsterniService.creaDetermina(proposta)
                DeterminaEsterna determinaEsterna = documentiEsterniService.creaDeterminaEsterna(DocumentiEsterniService.APPLICATIVO_ESTERNO_WEBSERVICE, proposta)
                determinaEsterna.idDetermina = determina.id
                determinaEsterna.statoAcquisizione = DocumentiEsterniService.ELABORATO
                determinaEsterna.save()

                WkfCfgIter cfgIter = WkfCfgIter.getIterIstanziabile(determina.tipologia.progressivoCfgIter).get()
                wkfIterService.istanziaIter(cfgIter, determina)
                determinaService.numeraProposta(determina)
                wkfIterService.sbloccaDocumento(determina)
                return toDocumento(determina)

            case Delibera.TIPO_OGGETTO:
                PropostaDelibera propostaDelibera = documentiEsterniService.creaPropostaDelibera(proposta)
                propostaDeliberaEsterna.idPropostaDelibera = propostaDelibera.id
                propostaDeliberaEsterna.statoAcquisizione = DocumentiEsterniService.ELABORATO
                propostaDeliberaEsterna.save()

                WkfCfgIter cfgIter = (WkfCfgIter)WkfCfgIter.getIterIstanziabile(propostaDelibera.tipologia.progressivoCfgIter).get()
                wkfIterService.istanziaIter(cfgIter, propostaDelibera)
                propostaDeliberaService.numeraProposta(propostaDelibera)
                wkfIterService.sbloccaDocumento(propostaDelibera)
                return toDocumento(propostaDelibera)
        }
    }
}
