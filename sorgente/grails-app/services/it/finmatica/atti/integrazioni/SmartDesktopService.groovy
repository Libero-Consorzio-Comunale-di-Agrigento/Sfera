package it.finmatica.atti.integrazioni

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.commons.StrutturaOrganizzativaService
import it.finmatica.atti.dizionari.Notifica
import it.finmatica.atti.dizionari.NotificaErrore
import it.finmatica.atti.documenti.*
import it.finmatica.atti.dto.documenti.viste.DocumentoStepDTO
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.jworklist.AbstractJWorklistDispatcher
import it.finmatica.atti.integrazioniws.ads.smartdesktop.*
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import org.springframework.transaction.annotation.Transactional

import javax.xml.datatype.DatatypeFactory
import javax.xml.datatype.XMLGregorianCalendar

class SmartDesktopService extends AbstractJWorklistDispatcher {

    // service
    StrutturaOrganizzativaService strutturaOrganizzativaService
    SpringSecurityService         springSecurityService

    // client smart desktop
    JWorklist smartDesktopClient
    AttiFirmaService attiFirmaService
    NotificheErroreService notificheErroreService

    /**
     * Invia una notifica sulla jworklist.
     *
     * Questa funzione richiede una nuova transazione perché va a scrivere anche sulla DestinatariNotificheAttività
     * in modo tale che tutta l'operazione sia "consistente": se scrivo sulla jworklist scrivo anche sulla DestinatariNotificheAttivita
     *
     * @param documento
     * @param tipoNotifica
     * @param oggetto
     * @param testo
     * @param utenti
     * @param priorita
     * @param stepCorrente
     */
    @Transactional
    void notifica (def documento, Notifica notifica, String oggetto, String testo, List<SoggettoNotifica> utenti, String priorita, String stepCorrente) {

        if (log.infoEnabled) {
            log.info("Invio la notifica oggetto: $oggetto, testo: $testo agli utenti: ${utenti.utente?.nominativo}")
        }

        // invio le notifiche ai singoli utenti.
        for (SoggettoNotifica soggettoNotifica : utenti) {
            String idRiferimento = getIdRiferimento(documento, notifica.tipoNotifica)
            // se ha già una notifica per questo id riferimento, non invio quella nuova:
            try {
                if (esisteNotificaJWorklist(idRiferimento, soggettoNotifica.utente) && !notificheErroreService.esisteErroreNotifica(idRiferimento)){
                    continue
                }
            } catch (Exception ex){
                log.error("Si è verificato un errore nella verifica della notifica con idRiferimento:$idRiferimento, utente:${soggettoNotifica.utente}. Procedo comunque con la creazione della notifica.", ex)
            }

            if (priorita != null && priorita.equals("")) {
                priorita = PRIORITA_NORMALE
            }

            List<String> idAttivitaCreate

            // Ogni tanto può succedere (ad es. a Belluno) che nel job notturno la notifica via jworklist non funzioni per qualche ragione (ad es. Read Timeout).
            // Siccome la notifica viene inviata nella funzione "afterCommit", dopo cioè la commit su agsde2 ma prima di quella su gdm,
            // per evitare problemi di sincronizzazione, faccio il catch di eventuali errori di invio della notifica e faccio un secondo tentativo di invio. Se anche questo non funzionasse, peccato:
            // meglio non avere la notifica in jworklist ma avere la situazione stabile e sincronizzata su db tra agsde2 e gdm.
            try {
                idAttivitaCreate = creaNotificaJWorklist(documento, idRiferimento, soggettoNotifica.utente, oggetto, testo, priorita, stepCorrente, notifica.tipoNotifica)
            } catch (Exception e) {
                log.warn("Si è verificato un errore nell'invio della notifica con idRiferimento:$idRiferimento, utente:${soggettoNotifica.utente}. Procedo con un secondo tentativo.", e)

                try {
                    idAttivitaCreate = creaNotificaJWorklist(documento, idRiferimento, soggettoNotifica.utente, oggetto, testo, priorita, stepCorrente, notifica.tipoNotifica)
                } catch (Exception e1) {
                    log.error("Si è verificato un secondo errore nell'invio della notifica con idRiferimento:$idRiferimento, utente:${soggettoNotifica.utente}. Non invio la notifica.", e)
                    idAttivitaCreate = null
                }
            }

            if (idAttivitaCreate == null){
                log.warn("Si è verificato un errore nell'invio della notifica, viene creata un errore notifica")
                notificheErroreService.creaErroreNotifica(NotificaErrore.OPERAZIONE_INVIO, notifica, idRiferimento, documento.iter.stepCorrente)
            }

            if (idAttivitaCreate != null && idAttivitaCreate.size() > 0) {
                soggettoNotifica.idAttivita = idAttivitaCreate[0]
                aggiungiNotificaJWorklist(idRiferimento, soggettoNotifica, documento, notifica)
            }
        }

        if (log.infoEnabled) {
            log.info("Notifiche (${utenti.idAttivita}) inviate agli utenti: ${utenti.utente?.nominativo}")
        }
    }

    @Transactional
    void eliminaNotifica (long idNotificaDestinatarioAttivita) {
        log.debug("Eliminazione della notifica=" + idNotificaDestinatarioAttivita)
        DestinatarioNotificaAttivita notifica = DestinatarioNotificaAttivita.get(idNotificaDestinatarioAttivita)
        try {
            TaskResponse resp = smartDesktopClient.eliminaTask(Long.parseLong(notifica.idAttivita))
            if (!resp.getCodiceErrore().equals("0")) {
                log.warn("Problemi durante l'eliminazione della notifica con id_attivita= ${notifica.idAttivita}\n Errore: ${resp.getDescrizioneErrore()}")
            } else {
                notifica.delete()
            }
        } catch (Exception ex){
            log.warn("Problemi durante l'eliminazione della notifica ${notifica.idAttivita}", ex)
            def documento = getDocumento(notifica.idRiferimento)
            notificheErroreService.creaErroreNotifica(NotificaErrore.OPERAZIONE_ELIMINA, null, notifica.idRiferimento, documento.iter.stepCorrente)
        }
    }

    private List<String> creaNotificaJWorklist (IDocumentoIterabile documentoIterabile, String idRiferimento, Ad4Utente utente, String oggetto, String testoAttivita, String priorita, String stepCorrente, String tipoNotifica) {
        List<String> lista = new ArrayList()
        boolean hasRuoloFirma = (strutturaOrganizzativaService.utenteHasRuoloDaImpostazioni(utente.id, Impostazioni.OTTICA_SO4.toString(),
                                                                                            Impostazioni.RUOLO_SO4_FIRMA.toString()))
        String urlRiferimento = (hasRuoloFirma ? JWorklistConfig.getUrlJWorklist() + "/standalone.zul?operazione=DA_FIRMARE" : "")
        String urlRiferimentoDescrizione = (hasRuoloFirma ? "Documenti da firmare" : "")
        String urlEsecuzione = getUrlDocumento(documentoIterabile)
        String descrizioneUrlEsecuzione = "Visualizza il Documento"
        String tooltipUrlEsecuzione = "Visualizza il Documento"
        String utenteEsterno = utente.id
        Date dataAttivazione = new Date()
        Date dataArrivo = new Date()
        String livelloPriorita = priorita
        String note = getNote(documentoIterabile, tipoNotifica)
        String paramInitIter = getParamInitIterString(documentoIterabile)
        ArrayList listaParametriIter = getParamInitIter(documentoIterabile, idRiferimento)
        String descIter = stepCorrente
        Date scadenza = getScadenza(documentoIterabile, tipoNotifica)
        String descrizioneTipologia = getDescrizioneNotificaTipologia(documentoIterabile)
        String tipoFirma = (hasRuoloFirma && documentoIterabile.iter?.stepCorrente?.id) ? attiFirmaService.getTipoFirma(documentoIterabile) : null

        if (log.debugEnabled) {
            log.debug("Invio la notifica con idRiferimento=$idRiferimento all'utente: ${utente.id}")
        }

        Task task = creaTask(idRiferimento, oggetto, testoAttivita, urlRiferimento, urlRiferimentoDescrizione, urlEsecuzione,
                             descrizioneUrlEsecuzione,
                             tooltipUrlEsecuzione, scadenza, paramInitIter, descIter, dataAttivazione, utenteEsterno, dataArrivo, livelloPriorita,
                             note, listaParametriIter, descrizioneTipologia, tipoFirma, documentoIterabile.statoFirma == StatoFirma.FIRMATO_DA_SBLOCCARE)
        try {
            TaskResponse resp = smartDesktopClient.inserisciTask(task)
            // se c'è stato un problema nella comunicazione con il webservice, lo scrivo nei log.
            if (!resp.getCodiceErrore().equals("0")) {
                log.warn("Problemi durante la creazione della notifica per Documento n." + idRiferimento + " ed utente " + utenteEsterno + " non inserito. \n Errore: " + resp.getDescrizioneErrore())
                return null
            }
            if (log.debugEnabled) {
                log.debug("Notifica (${resp.getIdTask()}) con idRiferimento: $idRiferimento inviata all'utente: ${utente.id}")
            }

            if (resp.getIdTask() != null) {
                lista.add(resp.getIdTask().toString())
            }

            return lista
        } catch (Exception ex){
            log.error("Problemi durante la creazione della notifica per Documento n." + idRiferimento + " ed utente " + utenteEsterno + " non inserito. ", ex)
            return null
        }
    }

    private Task creaTask (String idRiferimento, String oggetto, String testoAttivita, String urlRiferimento, String urlRiferimentoDescrizione,
                           String urlEsecuzione, String descrizioneUrlEsecuzione, String tooltipUrlEsecuzione, Date scadenza, String paramInitIter,
                           String descIter, Date dataAttivazione, String utenteEsterno, Date dataArrivo, String livelloPriorita, String note,
                           ArrayList listaParametriIter, String descrizioneTipologia, String tipoFirma, boolean daSbloccare) {
        GregorianCalendar c = new GregorianCalendar()
        c.setTime(dataAttivazione);
        XMLGregorianCalendar dataAtt = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

        c.setTime(dataArrivo);
        XMLGregorianCalendar dataArr = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);

        XMLGregorianCalendar dataScadenza = null
        if (scadenza != null) {
            c.setTime(scadenza);
            dataScadenza = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        }

        Task task = new Task()
        task.setApplicativoChiamante(JWorklistConfig.getApplicativoChiamante())
        task.setIdRiferimentoEsterno(idRiferimento)
        task.setDescrizione(oggetto)
        task.setTooltipDescrizione(testoAttivita)
        task.setUrlVisualizzazione(urlRiferimento)
        task.setDescrizioneUrlVisualizzazione(urlRiferimentoDescrizione)
        task.setUrlEsecuzione(urlEsecuzione)
        task.setDescrizioneUrlEsecuzione(descrizioneUrlEsecuzione)
        task.setTooltipDescrizione(tooltipUrlEsecuzione)
        task.setDataScadenza(dataScadenza)
        task.setParamInitIter(paramInitIter)
        task.setDescrizioneIter(descIter)
        task.setDataAttivazione(dataAtt)
        task.setUtente(utenteEsterno)
        task.setTipologia(TIPOLOGIA_ATTIVITA)
        task.setTipologiaDescrizione(descIter?.equals("Comunicazione") ? descIter : descrizioneTipologia)
        task.setDataArrivo(dataArr)
        task.setLivelloPriorita(livelloPriorita)
        task.setNote(note)

        /** SEZIONE DETTAGLIO */
        List<TaskDettaglio> taskDettaglioArrayList = new ArrayList<TaskDettaglio>()
        List<TaskAllegato> taskAllegatoArrayList = new ArrayList<TaskAllegato>()
        TaskDettaglio taskDettaglio = new TaskDettaglio()
        if (descIter != null && !descIter.equals("")) {
            taskDettaglio.setNomeDettaglio("Stato documento:")
            taskDettaglio.setValoreDettaglio(descIter)
            taskDettaglio.setUrl(0)
            taskDettaglioArrayList.add(taskDettaglio)
        }
        if (scadenza != null && !scadenza.equals("")) {
            taskDettaglio = new TaskDettaglio()
            taskDettaglio.setNomeDettaglio("Scadenza:")
            taskDettaglio.setValoreDettaglio((scadenza != null) ? scadenza.format("dd/MM/yyyy") : "")
            taskDettaglio.setUrl(0)
            taskDettaglioArrayList.add(taskDettaglio)
        }
        for (def parametro in listaParametriIter) {
            if (parametro.value != null && !parametro.value.equals("")) {
                if (parametro.key == "Allegato") {
                    TaskAllegato taskAllegato = new TaskAllegato()
                    taskAllegato.nomeFile   = parametro.value
                    taskAllegato.etichetta  = parametro.value
                    taskAllegato.priorita   = parametro.priorita
                    taskAllegato.url        = JWorklistConfig.getUrlJWorklist()+ "/smartDesktop/"+ parametro.url
                    taskAllegato.firmato    = parametro.firmato ? 1 : 0
                    taskAllegatoArrayList.add(taskAllegato)
                }
                else {
                    taskDettaglio = new TaskDettaglio()
                    taskDettaglio.setNomeDettaglio(parametro.key)
                    taskDettaglio.setValoreDettaglio((parametro.value != null) ? parametro.value : "")
                    taskDettaglio.setUrl(0)
                    taskDettaglioArrayList.add(taskDettaglio)
                }
            }
        }
        task.taskAllegato = taskAllegatoArrayList
        task.taskDettaglio = taskDettaglioArrayList

        /** SEZIONE ALLEGATI */
        /*
        List<TaskAllegato> taskAllegatoList = new ArrayList<TaskAllegato>()
        TaskAllegato taskAllegato = new TaskAllegato()
        taskAllegato.setEtichetta("")
        taskAllegato.setNomeFile("")
        taskAllegato.setMimetype("")
        taskAllegato.setTooltip("")
        taskAllegato.setUrl()
        taskAllegatoList.add(taskAllegato)
        task.taskAllegato=taskAllegatoList*/

        /** SEZIONE BOTTONI */
        List<TaskBottone> taskBottoneArrayList = new ArrayList<TaskBottone>()
        if (tipoFirma != null && !daSbloccare) {
            TaskBottone taskBottone = new TaskBottone()
            taskBottone.setTooltip("Firma i documenti selezionati")
            taskBottone.setEtichetta("Firma")
            taskBottone.setIcona("fa fa-pencil")
            taskBottone.setIdentificativoRiferimento(idRiferimento)
            taskBottone.setMultiplo(1)
            taskBottone.setTipologia("FORM")
            taskBottone.setUrlAzione(JWorklistConfig.getUrlJWorklist()+ "/smartDesktop/"+tipoFirma+"?LISTA_ID=XXXX")
            taskBottoneArrayList.add(taskBottone)
        }

        if (!idRiferimento.startsWith("NOTIFICA_") && daSbloccare) {
            TaskBottone taskBottone = new TaskBottone()
            taskBottone.setTooltip("Completa firma per i documenti selezionati")
            taskBottone.setEtichetta("Completa Firma")
            taskBottone.setIcona("fa fa-unlock")
            taskBottone.setIdentificativoRiferimento(idRiferimento)
            taskBottone.setMultiplo(1)
            taskBottone.setTipologia("FORM")
            taskBottone.setUrlAzione(JWorklistConfig.getUrlJWorklist()+ "/smartDesktop/completaFirma?LISTA_ID=XXXX")
            taskBottoneArrayList.add(taskBottone)
        }
        if (idRiferimento.startsWith("NOTIFICA_")) {
            TaskBottone taskBottone = new TaskBottone()
            taskBottone.setTooltip("Presa visione")
            taskBottone.setEtichetta("Presa visione")
            taskBottone.setIcona("fa fa-check")
            taskBottone.setIdentificativoRiferimento(idRiferimento)
            taskBottone.setMultiplo(1)
            taskBottone.setTipologia("FORM")
            taskBottone.setUrlAzione(JWorklistConfig.getUrlJWorklist()+ "/smartDesktop/presaVisione?LISTA_ID=XXXX")
            taskBottoneArrayList.add(taskBottone)
        }
        task.taskBottone = taskBottoneArrayList

        return task
    }

    public ArrayList presaVisione(String[] listaRiferimenti){
        def result = new ArrayList()
        int notificheCancellate = 0
        for (String riferimento in listaRiferimenti) {
            if (esisteNotificaJWorklist(riferimento, springSecurityService.currentUser)){
                List<DestinatarioNotificaAttivita> notifiche = getNotificheDaEliminare(riferimento, springSecurityService.currentUser)
                for (DestinatarioNotificaAttivita notificaAttivita : notifiche){
                    try {
                        eliminaNotifica(notificaAttivita.id)
                        notificheCancellate ++
                    } catch (Exception e) {
                        log.warn(e)

                    }
                }
            }
        }
        if (notificheCancellate > 0 ) {
            result.add([descrizione: "Operazione effettuata", result: true])
        }
        else if (notificheCancellate == 0 ) {
            result.add([descrizione: "Operazione non effettuata", result: false])
        }
        return result
    }

    public ArrayList completaFirma(String[] listaRiferimenti) {
        def result = new ArrayList()
        Collection<DocumentoStepDTO> documentiDaSbloccare = []
        for (String riferimento in listaRiferimenti) {
            def doc = getDocumento(riferimento)
            String descrizione = getDescrizione(doc)
            if (esisteNotificaJWorklist(riferimento, springSecurityService.currentUser)){
                DocumentoStepDTO documento = new DocumentoStepDTO(idDocumento: doc.idDocumento, tipoOggetto: doc.tipoOggetto)
                documentiDaSbloccare.add(documento)
                result.add([descrizione: descrizione, result: true])
            }
            else {
                result.add([descrizione: descrizione, result: false])
            }
        }

        attiFirmaService.sbloccaDocumentiFirmati(documentiDaSbloccare, springSecurityService.currentUser.nominativo)
        return result
    }

    public String getDescrizione(def doc) {
        String descrizione = getDescrizioneTipologia(doc);
        descrizione += (doc.tipologiaDocumento != null) ? ": "+doc.tipologiaDocumento.titolo : ""
        if (doc instanceof IDocumentoCollegato) {
            doc = doc.documentoPrincipale
        }
        descrizione += (doc instanceof IAtto && doc.numeroAtto > 0)         ? ", Atto n. "      + doc.numeroAtto+"/"+doc.annoAtto: ""
        descrizione += (doc instanceof IProposta && doc.numeroProposta > 0) ? ", Proposta n. "  + doc.numeroProposta+"/"+doc.annoProposta: ""
        descrizione += (doc.oggetto != null) ? " - "+doc.oggetto : ""
        return descrizione
    }

    public String getDescrizioneTipologia(def doc) {
        String descrizione = WkfTipoOggetto.get(doc.tipoOggetto)? WkfTipoOggetto.get(doc.tipoOggetto)?.nome : doc.tipoOggetto
        if (doc.tipoOggetto == VistoParere.TIPO_OGGETTO){
            return "Visto"
        }
        else if (doc.tipoOggetto == VistoParere.TIPO_OGGETTO_PARERE){
            return "Parere"
        }

        return descrizione
    }

    public String getDescrizioneNotificaTipologia(def doc){
        String descrizione = doc.tipoOggetto == Delibera.TIPO_OGGETTO ? doc.tipologiaDocumento?.descrizioneNotificaDelibera : doc.tipologiaDocumento?.descrizioneNotifica
        if (descrizione == null) {
            return getDescrizioneTipologia(doc)
        }
        return descrizione
    }
}
