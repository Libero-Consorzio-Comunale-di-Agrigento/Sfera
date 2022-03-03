package atti.documenti

import it.finmatica.atti.AbstractViewModel
import it.finmatica.atti.IntegrazioneContabilita
import it.finmatica.atti.commons.FileAllegatoStorico
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.beans.AttiFileDownloader
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.dto.documenti.*
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.*
import it.finmatica.dto.DTO
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class VistoViewModel extends AbstractViewModel<VistoParere> {

    // services
    CaratteristicaTipologiaService caratteristicaTipologiaService
    IntegrazioneContabilita        integrazioneContabilita
    VistoParereDTOService          vistoParereDTOService
    DocumentoDTOService            documentoDTOService
    VistoParereService             vistoParereService
    AllegatoDTOService             allegatoDTOService
    AttiFileDownloader             attiFileDownloader
    AttiGestoreCompetenze          gestoreCompetenze
    NotificheService               notificheService
    RegolaCampoService             regolaCampoService
    TokenIntegrazioneService       tokenIntegrazioneService

    // componenti
    Window popupCambiaTipologia

    Map<String, it.finmatica.atti.zk.SoggettoDocumento> soggetti = [:]

    // dati
    VistoParereDTO visto
    def            listaTipologie
    String         proponente
    def            storico
    def            proposta
    def            atto
    def            listaAllegati

    // stato
    String  posizioneFlusso
    def     campiProtetti
    boolean vistoFirmato = false
    def     competenze
    boolean isNotificaPresente
    boolean testoLockato
    boolean lockPermanente
    boolean firmaRemotaAbilitata

    // gestione contabilità
    boolean conDocumentiContabili = false
    boolean contabilitaAbilitata  = false
    String  zulContabilita

    // gestione delle note di trasmissione
    def     noteTrasmissionePrecedenti
    boolean attorePrecedente
    boolean mostraNoteTrasmissionePrecedenti
    boolean mostraNote    = true
    boolean mostraStorico = true

    // indica se è bloccato da un altro utente
    boolean isLocked = false

    // indica se il documento deve essere comunque aperto in lettura (delegato)
    boolean forzaCompetenzeLettura

    @NotifyChange(["visto"])
    @Init
    void init (
            @ContextParam(ContextType.COMPONENT) Window w,
            @ExecutionArgParam("id") long idVisto,
            @ExecutionArgParam("documento") def d, @ExecutionArgParam("idDocumentoEsterno") Long idDocumentoEsterno, @ExecutionArgParam("competenzeLettura") Boolean competenzeLettura) {
        this.self = w

        firmaRemotaAbilitata = Impostazioni.FIRMA_REMOTA.abilitato;
        forzaCompetenzeLettura = competenzeLettura

        if (idVisto != null) {
            visto = new VistoParereDTO([id: idVisto])
            visto.setDocumentoPrincipale(d);
            // al momento non è possibile aggiungere visti direttamente da Determine o Delibere ma solo da Proposte di Determina e Proposte di Delibera.
            proposta = d;
            atto = null;
            visto.esito = EsitoVisto.NON_APPOSTO;
            visto.stato = StatoDocumento.DA_PROCESSARE;
        } else {
            VistoParere v = VistoParere.findByIdDocumentoEsterno(idDocumentoEsterno);
            visto = v.toDTO(["propostaDelibera", "determina", "delibera"])
            proposta = v.proposta.toDTO();
            atto = v.atto?.toDTO();

            idVisto = visto.id
        }

        if (idVisto > 0) {
            aggiornaMaschera(VistoParere.get(idVisto))
        } else {
            visto = new VistoParereDTO(id: -1, esito: EsitoVisto.DA_VALUTARE, stato: StatoDocumento.DA_PROCESSARE)
            visto.setDocumentoPrincipale(d);
            competenze = [lettura: true, modifica: true, cancellazione: true]
        }

        if (visto.tipologia == null) {
            listaTipologie = vistoParereService.getListaTipologiePossibili(visto.documentoPrincipale.domainObject)?.toDTO()
        }

        // aggiorno gli allegati:
        refreshListaAllegati();

        // aggiorno i pulsanti solo se ho l'iter del visto.
        if (visto.iter != null) {
            aggiornaPulsanti()
        }
    }

    @Command
    onApriAtto () {
        String pathAtto = ""
        if (atto instanceof DeterminaDTO) {
            pathAtto = "/atti/documenti/determina.zul"
        } else if (atto instanceof DeliberaDTO) {
            pathAtto = "/atti/documenti/delibera.zul"
        } else {
            return;
        }

        Window w = Executions.createComponents(pathAtto, self, [id: atto.id])
        w.doModal()
    }

    @Command
    onApriTestoAtto () {
        def documento = atto?.getDomainObject();
        attiFileDownloader.downloadFileAllegato(documento, documento.testo)
    }

    @Command
    onApriProposta () {
        String pathAtto = ""
        if (proposta instanceof DeterminaDTO) {
            pathAtto = "/atti/documenti/determina.zul"
        } else if (proposta instanceof PropostaDeliberaDTO) {
            pathAtto = "/atti/documenti/propostaDelibera.zul"
        } else {
            return;
        }

        Window w = Executions.createComponents(pathAtto, self, [id: proposta.id])
        w.doModal()
    }

    @Command
    onApriTestoProposta () {
        def documento = proposta.getDomainObject();
        attiFileDownloader.downloadFileAllegato(documento, documento.testo)
    }

    @AfterCompose
    void afterCompose (@SelectorParam("#popupCambiaTipologia") Window popupTipologia) {
        if (visto.tipologia == null) {
            this.popupCambiaTipologia = popupTipologia
            popupCambiaTipologia.doModal()
        }
    }

    @NotifyChange("visto")
    @Command
    onSelectTipologia () {
        // calcolo i soggetti del documento:
        soggetti = caratteristicaTipologiaService.calcolaSoggetti(
                visto.domainObject ?: new VistoParere(documentoPrincipale: visto.documentoPrincipale.domainObject, tipologia: visto.tipologia.domainObject),
                visto.tipologia.caratteristicaTipologia.domainObject)
        visto.modelloTesto = visto.tipologia.modelloTesto

        aggiornaPulsanti()
        self.invalidate()

        BindUtils.postNotifyChange(null, null, this, "soggetti")
        Events.postEvent(new Event(Events.ON_CLOSE, this.popupCambiaTipologia, null))
    }

    /*
     *  Presa Visione
     */

    @Command
    onPresaVisione () {
        notificheService.eliminaNotifica(visto.domainObject, springSecurityService.currentUser)
        isNotificaPresente = false
        BindUtils.postNotifyChange(null, null, this, "isNotificaPresente")
        onChiudi()
    }

    /*
     * Gestione Applet Testo
     */

    @NotifyChange(["testoLockato"])
    @Command
    editaTesto () {
        testoLockato = gestioneTesti.editaTesto(visto);
    }

    @Command
    onEliminaTesto () {
        gestioneTesti.eliminaTesto(visto, this)
    }

    @Command
    onDownloadTesto () {
        VistoParere d = visto.domainObject
        attiFileDownloader.downloadFileAllegato(d, d.testo)
    }

    @Command
    onDownloadTestoStorico (
            @BindingParam("tipoOggetto") String tipoOggetto, @BindingParam("id") Long id, @BindingParam("idFileAllegato") Long idFileAllegato) {
        FileAllegatoStorico f = FileAllegatoStorico.get(idFileAllegato)
        attiFileDownloader.downloadFileAllegato(DocumentoFactory.getDocumentoStorico(id, tipoOggetto), f, true)
    }

    @Command
    onSceltaSoggetto (@BindingParam("tipoSoggetto") String tipoSoggetto, @BindingParam("categoriaSoggetto") String categoriaSoggetto) {
        Window w = Executions.createComponents("/atti/documenti/popupSceltaSoggetto.zul", self,
                                               [idCaratteristicaTipologia: visto.tipologia.caratteristicaTipologia.id
                                                , documento              : visto
                                                , soggetti               : soggetti
                                                , tipoSoggetto           : tipoSoggetto
                                                , categoriaSoggetto      : categoriaSoggetto])
        w.onClose { event ->
            // se ho annullato la modifica, non faccio niente:
            if (event.data == null) {
                return
            };

            // altrimenti aggiorno i soggetti.
            BindUtils.postNotifyChange(null, null, this, "soggetti");
            self.invalidate()
        }
        w.doModal()
    }

    private void calcolaSoggetti (String tipoSoggetto) {
        caratteristicaTipologiaService.aggiornaSoggetti(visto.tipologia.caratteristicaTipologia.id, visto.domainObject ?: new VistoParere(
                documentoPrincipale: visto.documentoPrincipale.domainObject), soggetti, tipoSoggetto)

        BindUtils.postNotifyChange(null, null, this, "soggetti")
    }

    /*
     * Gestione allegati
     */

    @Command
    onModificaAllegato (
            @ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("nuovo") boolean nuovo, @BindingParam("selected") def selected) {
        // succede quando un utente fa "doppio click" sulla tabella vuota.
        if (!nuovo && selected == null) {
            return;
        }

        Window w = Executions.createComponents("/atti/documenti/allegato.zul", self, [id: (nuovo ? -1 : selected.id), documento: visto, competenzeLettura: forzaCompetenzeLettura])
        w.onClose {
            if (!(visto.idDocumentoEsterno > 0)) {
                // potrei aver aggiornato la determina, quindi ne riprendo i numeri di versione e idDocumentoEsterno.
                VistoParere v = visto.domainObject;
                visto.version = v.version;
                visto.idDocumentoEsterno = v.idDocumentoEsterno;
            }
            refreshListaAllegati()
        }
        w.doModal()
    }

    @Command
    onEliminaAllegato (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("allegato") AllegatoDTO allegato) {
        Messagebox.show("Eliminare l'allegato selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                        new org.zkoss.zk.ui.event.EventListener() {
                            void onEvent (Event e) {
                                if (Messagebox.ON_OK.equals(e.getName())) {
                                    allegatoDTOService.elimina(allegato, visto)
                                    visto.version = visto.domainObject.version;
                                    VistoViewModel.this.refreshListaAllegati()
                                }
                            }
                        }
        )
    }

    private void refreshListaAllegati () {
        listaAllegati = Allegato.createCriteria().list {
            eq("vistoParere.id", visto.id)
            order("sequenza", "asc")
            order("titolo", "asc")
        }.toDTO()
        BindUtils.postNotifyChange(null, null, this, "listaAllegati")
    }

    private void aggiornaNoteTrasmissionePrecedenti () {
        def result = documentoDTOService.getNoteTrasmissionePrecedenti(visto)
        noteTrasmissionePrecedenti = result.noteTrasmissionePrecedenti
        attorePrecedente = result.attorePrecedente
        mostraNoteTrasmissionePrecedenti = result.mostraNoteTrasmissionePrecedenti

        BindUtils.postNotifyChange(null, null, this, "mostraNoteTrasmissionePrecedenti")
        BindUtils.postNotifyChange(null, null, this, "noteTrasmissionePrecedenti")
        BindUtils.postNotifyChange(null, null, this, "attorePrecedente")
    }

    /*
     * Gestione della contabilità
     */

    @Command
    void onAggiornaContabilita () {
        aggiornaContabilita(visto.domainObject)
    }

    void aggiornaContabilita (VistoParere vp) {
        if (vp != null) {
            integrazioneContabilita.aggiornaMaschera(vp.documentoPrincipale, (competenze.modifica && !(campiProtetti.CONTABILITA && vp.documentoPrincipale.tipologiaDocumento.scritturaMovimentiContabili)))
        }
    }

    /*
     * Gestione dello storico:
     */

    private void caricaStorico () {
        storico = vistoParereDTOService.caricaStorico(visto);

        BindUtils.postNotifyChange(null, null, this, "storico")
    }

    @NotifyChange(["visto"])
    @Command
    void onSalva () {
        if (soggetti[TipoSoggetto.UO_DESTINATARIA] == null || soggetti[TipoSoggetto.FIRMATARIO] == null) {
            throw new AttiRuntimeException("Attenzione<br />Unità destinataria e firmatario sono obbligatori")
        }

        VistoParere.withTransaction {
            VistoParere vistoParere = getDocumentoIterabile(false)
            aggiornaDocumentoIterabile(vistoParere)
            vistoParere.save()
            aggiornaMaschera(vistoParere)
        }
    }

    @Command
    void onChiudi () {
        // se devo rilasciare il lock sul testo, lo rilascio.
        gestioneTesti.uploadEUnlockTesto(visto, lockPermanente)
        tokenIntegrazioneService.unlockDocumento(visto.domainObject)
        Events.postEvent(Events.ON_CLOSE, self, null)
    }

    @Override
    WkfCfgIter getCfgIter () {
        return WkfCfgIter.getIterIstanziabile(visto?.tipologia?.progressivoCfgIter ?: -1).get()
    }

    DTO<VistoParere> getDocumentoDTO () {
        return visto
    }

    VistoParere getDocumentoIterabile (boolean controllaConcorrenza) {
        if (visto?.id > 0) {
            VistoParere domainObject = visto.getDomainObject()
            if (controllaConcorrenza && visto?.version >= 0 && domainObject.version != visto?.version) {
                throw new AttiRuntimeException(
                        "Attenzione: un altro utente ha modificato il documento su cui si sta lavorando. Impossibile continuare. \n (dto.version=${visto.version}!=domain.version=${domainObject.version})")
            }

            return domainObject
        }

        return new VistoParere()
    }

    Collection<String> validaMaschera () {
        return []
    }

    void aggiornaDocumentoIterabile (VistoParere v) {
        boolean inCreazione = !(v.id > 0)
        // salvo e sblocco il testo
        gestioneTesti.uploadEUnlockTesto(v)

        // se sono in creazione, devo associare anche il documento principale e copiarne le competenze
        if (inCreazione) {
            v.documentoPrincipale = visto.documentoPrincipale.domainObject
        }

        v.tipologia = visto.tipologia?.domainObject
        v.modelloTesto = visto.tipologia?.modelloTesto?.domainObject
        v.esito = visto.esito
        v.stato = visto.stato
        v.note = visto.note
        v.noteTrasmissione = visto.noteTrasmissione

        caratteristicaTipologiaService.salvaSoggettiModificati(v, soggetti)

        if (inCreazione) {
            vistoParereService.copiaCompetenzeDocumentoPrincipale(v)
        }
    }

    void aggiornaMaschera (VistoParere v) {
        // per prima cosa controllo che l'utente abbia le competenze in lettura sul documento
        competenze = gestoreCompetenze.getCompetenze(v, true)
        competenze.lettura = competenze.lettura ?: forzaCompetenzeLettura
        if (!competenze.lettura) {
            visto = null
            throw new AttiRuntimeException(
                    "L'utente ${springSecurityService.principal.username} non ha i diritti di lettura sul documento con id ${v.id}")
        }

        if (v.statoFirma == StatoFirma.IN_FIRMA || v.statoFirma == StatoFirma.FIRMATO_DA_SBLOCCARE || v.statoFirma == StatoFirma.FIRMATO) {
            competenze.modifica = false
            competenze.cancellazione = false
        }

        isLocked = tokenIntegrazioneService.isLocked(v)

        visto = v.toDTO(["tipologia.caratteristicaTipologia", "tipologia.modelloTesto", "testo", "modelloTesto", "iter.stepCorrente.cfgStep.titolo"]);
        proposta = v.proposta?.toDTO();
        atto = v.atto?.toDTO();

        proponente = v.proposta?.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.descrizione;

        // aggiorno i dati del lock sul testo:
        testoLockato = gestioneTesti.isTestoLockato(v)

        // calcolo i campi che devo proteggere in lettura
        campiProtetti = CampiDocumento.getMappaCampi(visto.campiProtetti)

        posizioneFlusso = v.iter?.stepCorrente?.cfgStep?.nome

        // calcolo i vari soggetti della determina/delibera
        soggetti = caratteristicaTipologiaService.calcolaSoggettiDto(v);

        // carico lo storico
        caricaStorico()

        // aggiorno le note di trasmissioni dello step precedente
        aggiornaNoteTrasmissionePrecedenti()

        // controllo se la determina è firmata o no
        vistoFirmato = (v.testo?.isFirmato())

        // gestione contabilità
        contabilitaAbilitata = integrazioneContabilita.isAbilitata(v)
        if (contabilitaAbilitata) {
            conDocumentiContabili = integrazioneContabilita.isConDocumentiContabili(v)
            zulContabilita = integrazioneContabilita.getZul(v)
            aggiornaContabilita(v)

            BindUtils.postNotifyChange(null, null, this, "conDocumentiContabili")
            BindUtils.postNotifyChange(null, null, this, "zulContabilita")
        }

        // verifico presenza notifiche
        isNotificaPresente = notificheService.isNotificaPresente(v, springSecurityService.currentUser)

        refreshListaAllegati();

		mostraNote 	  = regolaCampoService.isBloccoVisibile(v, v.tipoOggetto, "NOTE")
		mostraStorico = regolaCampoService.isBloccoVisibile(v, v.tipoOggetto, "STORICO")

        BindUtils.postNotifyChange(null, null, this, "testoLockato")
        BindUtils.postNotifyChange(null, null, this, "visto")
        BindUtils.postNotifyChange(null, null, this, "vistoFirmato")
        BindUtils.postNotifyChange(null, null, this, "campiProtetti")
        BindUtils.postNotifyChange(null, null, this, "soggetti")
        BindUtils.postNotifyChange(null, null, this, "posizioneFlusso")
        BindUtils.postNotifyChange(null, null, this, "proponente")
        BindUtils.postNotifyChange(null, null, this, "soggetti")
        BindUtils.postNotifyChange(null, null, this, "isNotificaPresente")
        BindUtils.postNotifyChange(null, null, this, "contabilitaAbilitata")
    }
}
