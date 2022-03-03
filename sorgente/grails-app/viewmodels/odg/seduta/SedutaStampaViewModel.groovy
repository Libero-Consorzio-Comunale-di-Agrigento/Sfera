package odg.seduta

import it.finmatica.atti.AbstractViewModel
import it.finmatica.atti.commons.FileAllegatoStorico
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.documenti.DocumentoFactory
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.NotificheService
import it.finmatica.atti.documenti.beans.AttiFileDownloader
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.storico.SedutaStampaStoricoService
import it.finmatica.atti.dto.documenti.DestinatarioNotificaDTO
import it.finmatica.atti.dto.documenti.DestinatarioNotificaDTOService
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.atti.dto.odg.CommissioneStampaDTO
import it.finmatica.atti.dto.odg.SedutaDTO
import it.finmatica.atti.dto.odg.SedutaStampaDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.CaratteristicaTipologiaService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.odg.CommissioneStampa
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.atti.zk.documenti.ViewModelSoggetti
import it.finmatica.dto.DTO
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class SedutaStampaViewModel extends AbstractViewModel<SedutaStampa> implements ViewModelSoggetti {

    private Window popupCambiaTipologia
    boolean lockPermanente

    DestinatarioNotificaDTOService destinatarioNotificaDTOService
    CaratteristicaTipologiaService caratteristicaTipologiaService
    SedutaStampaStoricoService sedutaStampaStoricoService
    AttiFileDownloader attiFileDownloader
    AttiGestoreCompetenze gestoreCompetenze
    NotificheService notificheService
    TokenIntegrazioneService tokenIntegrazioneService

    private String tipoStampa

    List storico
    SedutaStampaDTO sedutaStampa
    List<CommissioneStampaDTO> listaTipologie
    def competenze = [lettura: true, modifica: true, cancellazione: true]
    def campiProtetti = [:]
    boolean isNotificaPresente = false
    boolean mostraArchiviazioni = true
    boolean classifica_obb = Impostazioni.PROTOCOLLO_CLASSIFICA_OBBL.abilitato
    boolean fascicolo_obb = Impostazioni.PROTOCOLLO_FASCICOLO_OBBL.abilitato

    // dati per la gestione dei destinatari:
    List listaDestinatari

    // gestione edita testo
    boolean testoLockato = false

    @Init
    void init(
            @ContextParam(ContextType.COMPONENT) Window w,
            @ExecutionArgParam("seduta") SedutaDTO seduta, @ExecutionArgParam("tipo") String tipoStampa,
            @ExecutionArgParam("id") Long idSedutaStampa,
            @ExecutionArgParam("idDocumentoEsterno") Long idDocumentoEsterno) {
        this.self = w
        this.tipoStampa = tipoStampa

        if (idSedutaStampa > 0) {
            aggiornaMaschera(SedutaStampa.get(idSedutaStampa))
        } else if (idDocumentoEsterno > 0) {
            aggiornaMaschera(SedutaStampa.findByIdDocumentoEsterno(idDocumentoEsterno))
        } else {
            sedutaStampa = new SedutaStampaDTO(id: -1, seduta: seduta)
        }

        if (this.tipoStampa == null) {
            this.tipoStampa = sedutaStampa.commissioneStampa.codice
        }

        if (this.sedutaStampa.iter?.id > 0) {
            aggiornaPulsanti()
        }

        // gestione dei destinatari interni / esterni:
        refreshListaDestinatari()
    }

    /*
     * Gestione tipologia
     */

    @AfterCompose
    void afterCompose(@SelectorParam("#popupCambiaTipologia") Window popupTipologia) {
        this.popupCambiaTipologia = popupTipologia
        if (sedutaStampa.id > 0) {
            return
        }

        caricaListaTipologie(sedutaStampa.seduta.commissione, this.tipoStampa)
        if (listaTipologie.size() == 1) {
            this.sedutaStampa.tipologia = listaTipologie[0]
            onSelectTipologia()
        }

        if (sedutaStampa.tipologia == null) {
            popupCambiaTipologia.doModal()
        }
    }

    @Command
    void onSelectTipologia() {
        soggetti = caratteristicaTipologiaService.calcolaSoggetti(new SedutaStampa(seduta: sedutaStampa.seduta.domainObject),
                sedutaStampa.commissioneStampa.caratteristicaTipologia.domainObject)
        sedutaStampa.giorniPubblicazione = sedutaStampa.commissioneStampa.domainObject.giorniPubblicazione
        aggiornaPulsanti()
        popupCambiaTipologia.setVisible(false)
        self.invalidate()
        BindUtils.postNotifyChange(null, null, this, "sedutaStampa")
    }

    @Command
    void onChiudiPopup() {
        onChiudi()
        popupCambiaTipologia.setVisible(false)
    }

    private void caricaListaTipologie(CommissioneDTO commissione, String tipoStampa) {
        listaTipologie =
                CommissioneStampa.findAllByValidoAndCommissioneAndCodiceAndProgressivoCfgIterIsNotNullAndModelloTestoIsNotNullAndCaratteristicaTipologiaIsNotNull(
                        true, commissione.domainObject, tipoStampa,
                        [sort: "titolo", order: "asc", fetch: [caratteristicaTipologia: "eager", modelloTesto: "eager"]]).toDTO()
        if (listaTipologie.size() == 0) {
            Clients.showNotification("Non sono presenti Stampe di Commissione configurate per il tipo di stampa ${tipoStampa}. Configurarne almeno una nei Dizionari -> Ordine del Giorno -> Commissioni", Clients.NOTIFICATION_TYPE_ERROR, null, "middle_center", 8000, true)
        }
        BindUtils.postNotifyChange(null, null, this, "listaTipologie")
    }

    /*
	 * Gestione Applet e download Testo
	 */

    @NotifyChange(["testoLockato"])
    @Command
    void editaTesto() {
        testoLockato = gestioneTesti.editaTesto(getDocumentoDTO())
    }

    @Command
    void onEliminaTesto() {
        gestioneTesti.eliminaTesto(getDocumentoDTO(), this)
    }

    @Command
    void onDownloadTesto() {
        IDocumento documento = getDocumentoIterabile(false)
        attiFileDownloader.downloadFileAllegato(documento, documento.testo)
    }

    /*
     * Gestione Soggetti
     */

    @Command
    void onSceltaSoggetto(
            @BindingParam("tipoSoggetto") String tipoSoggetto,
            @BindingParam("categoriaSoggetto") String categoriaSoggetto) {
        Window w = Executions.createComponents("/atti/documenti/popupSceltaSoggetto.zul", self,
                [idCaratteristicaTipologia: sedutaStampa.tipologia.caratteristicaTipologia.id
                 , documento              : sedutaStampa
                 , soggetti               : soggetti
                 , tipoSoggetto           : tipoSoggetto
                 , categoriaSoggetto      : categoriaSoggetto])
        w.onClose { event ->
            // se ho annullato la modifica, non faccio niente:
            if (event.data == null) {
                return
            }

            // altrimenti aggiorno i soggetti.
            BindUtils.postNotifyChange(null, null, this, "soggetti")
        }
        w.doModal()
    }

    private void calcolaSoggetti(@BindingParam("tipoSoggetto") String tipoSoggetto) {
        caratteristicaTipologiaService.aggiornaSoggetti(sedutaStampa.tipologia.caratteristicaTipologia.id, sedutaStampa.domainObject, soggetti,
                tipoSoggetto)
        BindUtils.postNotifyChange(null, null, this, "soggetti")
    }

    /*
	 * Gestione popup protocollo
	 */

    @Command
    void apriClassificazione() {
        Window w = Executions.createComponents("/commons/popupClassificazioni.zul", self,
                [codiceUoProponente: soggetti[TipoSoggetto.REDATTORE].unita.codice])
        w.onClose { event ->
            if (event.data) {
                if (event.data.codice != sedutaStampa.classificaCodice) {
                    sedutaStampa.fascicoloAnno = 0
                    sedutaStampa.fascicoloNumero = null
                    sedutaStampa.fascicoloOggetto = null
                }
                sedutaStampa.classificaCodice = event.data.codice
                sedutaStampa.classificaDescrizione = event.data.descrizione
                sedutaStampa.classificaDal = event.data.dal
                BindUtils.postNotifyChange(null, null, this, "sedutaStampa")
            }
        }
        w.doModal()
    }

    @Command
    void apriFascicoli() {
        Window w = Executions.createComponents("/commons/popupFascicoli.zul", self,
                [classificaCodice: sedutaStampa.classificaCodice, classificaDescrizione: sedutaStampa.classificaDescrizione, classificaDal: sedutaStampa.classificaDal, codiceUoProponente: soggetti
                [TipoSoggetto.REDATTORE].unita.codice])
        w.onClose { event ->
            if (event.data) {
                // se ho cambiato la classificazione, la riaggiorno
                if (event.data.classifica.codice != sedutaStampa.classificaCodice) {
                    sedutaStampa.classificaCodice = event.data.classifica.codice
                    sedutaStampa.classificaDescrizione = event.data.classifica.descrizione
                    sedutaStampa.classificaDal = event.data.classifica.dal
                }
                sedutaStampa.fascicoloAnno = event.data.anno
                sedutaStampa.fascicoloNumero = event.data.numero
                sedutaStampa.fascicoloOggetto = event.data.oggetto
                BindUtils.postNotifyChange(null, null, this, "sedutaStampa")
            }
        }
        w.doModal()
    }

    /*
     * Gestione storico
     */

    @Command
    void onDownloadTestoStorico(
            @BindingParam("tipoOggetto") String tipoOggetto,
            @BindingParam("id") Long id, @BindingParam("idFileAllegato") Long idFileAllegato) {
        FileAllegatoStorico f = FileAllegatoStorico.get(idFileAllegato)
        attiFileDownloader.downloadFileAllegato(DocumentoFactory.getDocumentoStorico(id, tipoOggetto), f, true)
    }

    private void caricaStorico() {
        storico = sedutaStampaStoricoService.caricaStorico(sedutaStampa)
        BindUtils.postNotifyChange(null, null, this, "storico")
    }

    /*
	 * Gestione Destinatari
	 */

    @Command
    void onAggiungiDestinatario() {
        Window w = Executions.createComponents("/commons/popupRicercaSoggetti.zul", self, [ricercaSuSfera: true])
        w.onClose { Event event ->
            if (event.data != null) {
                // dentro "data" ci può essere o un As4SoggettoCorrenteDTO oppure una EmailDTO
                destinatarioNotificaDTOService.salvaDestinatario(sedutaStampa, event.data)
                this.refreshListaDestinatari()

                BindUtils.postNotifyChange(null, null, this, "soggetto")
            }
        }
        w.doModal()
    }

    @Command
    void onEliminaDestinatario(@ContextParam(ContextType.TRIGGER_EVENT) Event event
                               , @BindingParam("destinatario") DestinatarioNotificaDTO destinatario) {
        Messagebox.show("Eliminare il destinatario selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION) { Event e ->
            if (Messagebox.ON_OK.equals(e.getName())) {
                destinatarioNotificaDTOService.eliminaDestinatarioNotifica(destinatario)
                this.refreshListaDestinatari()
                sedutaStampa.version = sedutaStampa.domainObject.version
            }
        }
    }

    private void refreshListaDestinatari() {
        listaDestinatari = destinatarioNotificaDTOService.getListaDestinatari(sedutaStampa)
        BindUtils.postNotifyChange(null, null, this, "listaDestinatari")
    }

    /*
     * Gestione Maschera
     */

    @Command
    onPresaVisione() {
        notificheService.eliminaNotifica(sedutaStampa.domainObject, springSecurityService.currentUser)
        isNotificaPresente = false
        BindUtils.postNotifyChange(null, null, this, "isNotificaPresente")
        onChiudi()
    }

    @Command
    void onChiudi() {
        // se devo rilasciare il lock sul testo, lo rilascio.
        gestioneTesti.uploadEUnlockTesto(sedutaStampa, lockPermanente);
        tokenIntegrazioneService.unlockDocumento(sedutaStampa.domainObject)
        Events.postEvent(Events.ON_CLOSE, self, null)
    }

    @Override
    SedutaStampa getDocumentoIterabile(boolean controllaConcorrenza) {
        if (sedutaStampa.id > 0) {
            SedutaStampa domainObject = sedutaStampa.domainObject
            if (controllaConcorrenza && sedutaStampa?.version >= 0 && domainObject.version != sedutaStampa?.version) {
                throw new AttiRuntimeException(
                        "Attenzione: un altro utente ha modificato il documento su cui si sta lavorando. Impossibile continuare. \n (dto.version=${sedutaStampa.version}!=domain.version=${domainObject.version})")
            }

            return domainObject
        }

        return new SedutaStampa()
    }

    @Override
    void aggiornaMaschera(SedutaStampa sedutaStampa) {
        this.sedutaStampa = sedutaStampa.toDTO(
                ["seduta.commissione", "commissioneStampa.caratteristicaTipologia", "commissioneStampa.modelloTesto", "fileDocumenti", "iter.stepCorrente.cfgStep"])

        soggetti = caratteristicaTipologiaService.calcolaSoggettiDto(sedutaStampa)
        competenze = gestoreCompetenze.getCompetenze(sedutaStampa)

        isNotificaPresente = notificheService.isNotificaPresente(sedutaStampa, springSecurityService.currentUser)

        caricaStorico()

        // destinatari
        refreshListaDestinatari()

        BindUtils.postNotifyChange(null, null, this, "sedutaStampa")
        BindUtils.postNotifyChange(null, null, this, "isNotificaPresente")
        BindUtils.postNotifyChange(null, null, this, "soggetti")
        BindUtils.postNotifyChange(null, null, this, "competenze")
    }

    @Override
    void aggiornaDocumentoIterabile(SedutaStampa sedutaStampa) {

        sedutaStampa.seduta = this.sedutaStampa.seduta.domainObject
        sedutaStampa.commissioneStampa = this.sedutaStampa.commissioneStampa.domainObject
        sedutaStampa.note = this.sedutaStampa.note
        sedutaStampa.giorniPubblicazione = this.sedutaStampa.giorniPubblicazione

        // dati di protocollo
        sedutaStampa.fascicoloAnno = this.sedutaStampa.fascicoloAnno
        sedutaStampa.fascicoloNumero = this.sedutaStampa.fascicoloNumero
        sedutaStampa.fascicoloOggetto = this.sedutaStampa.fascicoloOggetto
        sedutaStampa.classificaCodice = this.sedutaStampa.classificaCodice
        sedutaStampa.classificaDal = this.sedutaStampa.classificaDal
        sedutaStampa.classificaDescrizione = this.sedutaStampa.classificaDescrizione

        sedutaStampa.save()

        caratteristicaTipologiaService.salvaSoggettiModificati(sedutaStampa, soggetti)
    }

    @Override
    WkfCfgIter getCfgIter() {
        return WkfCfgIter.getIterIstanziabile(sedutaStampa?.tipologia?.progressivoCfgIter ?: ((long) -1)).get()
    }

    @Override
    Collection<String> validaMaschera() {
        return []
    }

    @Override
    DTO<SedutaStampa> getDocumentoDTO() {
        return sedutaStampa
    }

    @NotifyChange(["lockPermanente"])
    @Command onToggleLockPermanente () {
        lockPermanente = !lockPermanente;
    }

}
