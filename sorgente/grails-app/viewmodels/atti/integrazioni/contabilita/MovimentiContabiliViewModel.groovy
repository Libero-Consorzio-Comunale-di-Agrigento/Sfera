package atti.integrazioni.contabilita

import it.finmatica.atti.IntegrazioneContabilita
import it.finmatica.atti.contabilita.MovimentoContabile
import it.finmatica.atti.contabilita.MovimentoContabileInterno
import it.finmatica.atti.contabilita.MovimentoContabileInternoService
import it.finmatica.atti.dizionari.DatiAggiuntiviService
import it.finmatica.atti.documenti.DatoAggiuntivo
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.DocumentoFactory
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.TipoDatoAggiuntivo
import it.finmatica.atti.dto.contabilita.MovimentoContabileInternoDTO
import it.finmatica.atti.impostazioni.CampiDocumento
import it.finmatica.atti.impostazioni.MappingIntegrazione
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.AfterCompose
import org.zkoss.bind.annotation.BindingParam
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.GlobalCommand
import org.zkoss.bind.annotation.Init
import org.zkoss.bind.annotation.NotifyChange
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class MovimentiContabiliViewModel {

    IntegrazioneContabilita          integrazioneContabilita
    DatiAggiuntiviService            datiAggiuntiviService
    MovimentoContabileInternoService movimentoContabileInternoService

    def listaMovimenti
    def campiProtetti
    def listaCapitoli
    def listaSoggetti
    def listaMovimentiInterni

            boolean            competenzaInModifica = false
            MovimentoContabile movimento
    private long               idDocumento
    private String             tipoDocumento
            String             codiceEsterno

    String  gestioneInterna          = 'N'
    boolean visTabMovContabili       = true
    boolean visTabMovInterni         = false
    boolean gestioneMovimentiInterni = true

    // componenti
    @Wire("#popupModificaMovimentoContabile")
    Window popupModificaMovimentoContabile

    @Init
    init() {
        gestioneInterna = MappingIntegrazione.getValoreEsterno("CONTABILITA_ASCOT", "GESTIONE_INTERNA").trim()
        if (gestioneInterna == 'Y') {
            visTabMovContabili = false
            visTabMovInterni = true
        }
    }

    @AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, false);
    }

    @GlobalCommand("aggiornaAtto")
    @NotifyChange(["competenzaInModifica", "campiProtetti", "listaMovimenti", "codiceEsterno", "listaMovimentiInterni", "listaCapitoli", "listaSoggetti"])
    public void aggiornaAtto(@BindingParam("atto") IDocumento atto, @BindingParam("competenza") String competenza) {
        idDocumento = atto.id;
        tipoDocumento = atto.TIPO_OGGETTO;

        this.competenzaInModifica = "W".equals(competenza);
        aggiornaMovimentiContabili(atto)
        caricaListaMovimentiInterni(atto)
        campiProtetti = CampiDocumento.getMappaCampi(atto?.campiProtetti)

        try {
            DatoAggiuntivo datoAggiuntivo = datiAggiuntiviService.getDatoAggiuntivo((atto instanceof IAtto ? atto.proposta : atto), TipoDatoAggiuntivo.CONTABILITA_ASCOT)
            if (datoAggiuntivo != null && datoAggiuntivo?.valore?.contains("#")) {
                codiceEsterno = datoAggiuntivo?.valore?.replace("#", "/");
            }
        } catch (Exception ex) {
            log.error(ex)
        }

        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "campiProtetti");
        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "competenzaInModifica");
    }

    private void aggiornaMovimentiContabili() {
        aggiornaMovimentiContabili(DocumentoFactory.getDocumento(idDocumento, tipoDocumento));
    }

    private void aggiornaMovimentiContabili(IDocumento atto) {
        listaMovimenti = integrazioneContabilita.getMovimentiContabili(atto);

        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "listaMovimenti");
        BindUtils.postNotifyChange(null, null, this, "conDocumentiContabili");
    }

    @NotifyChange(["movimento", "listaMovimenti"])
    @Command
    onSalvaMovimentoContabile() {

        if (movimento.importo == null ||
                movimento.codice == null ||
                movimento.azione == null ||
                movimento.capitolo == null) {

            Clients.showNotification("Non è possibile salvare il movimento: non tutti i campi obbligatori sono compilati.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 3000, true)
            return;
        }

        movimento.idDocumento = idDocumento;
        movimento.tipoDocumento = tipoDocumento;
        movimento.save();
        movimento = null;
        popupModificaMovimentoContabile.visible = false;

        aggiornaMovimentiContabili();

        Clients.showNotification("Movimento salvato con successo.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true)
    }

    @Command
    onChiudiMovimentoContabile() {
        popupModificaMovimentoContabile.visible = false;
    }

    @NotifyChange(["movimento", "listaMovimenti"])
    @Command
    onAggiungiMovimentoContabile() {
        movimento = new MovimentoContabile();
        // imposto come default il valore di spesa.
        // questo lo faccio per forzare l'aggiornamento del campo "tipo" di cui "entrata" è un alias.
        // se non lo facessi e l'utente da interfaccia non scegliesse nulla, allora il campo "tipo" avrebbe ancora valore null.
        // in questo modo invece lo forzo ad avere il campo "USCITA" di default.
        movimento.entrata = false;
        movimento.discard();
        popupModificaMovimentoContabile.doModal();
    }

    @NotifyChange(["movimento", "listaMovimenti"])
    @Command
    onModificaMovimentoContabile(@BindingParam("idMovimentoContabile") long idMovimentoContabile) {
        movimento = MovimentoContabile.get(idMovimentoContabile);
        movimento.discard();
        popupModificaMovimentoContabile.doModal();
    }

    @Command
    onEliminaMovimentoContabile(@BindingParam("idMovimentoContabile") long idMovimentoContabile) {
        Messagebox.show("Eliminare il movimento contabile selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                new org.zkoss.zk.ui.event.EventListener() {
                    public void onEvent(Event e) {
                        if (Messagebox.ON_OK.equals(e.getName())) {
                            MovimentoContabile.get(idMovimentoContabile).delete();
                            MovimentiContabiliViewModel.this.aggiornaMovimentiContabili();
                        }
                    }
                })
    }

    /*
    * Metodi per la gestione dei movimenti contabili interni
    */

    private void caricaListaMovimentiInterni() {
        caricaListaMovimentiInterni(DocumentoFactory.getDocumento(idDocumento, tipoDocumento));
    }

    @NotifyChange("listaMovimentiInterni")
    private void caricaListaMovimentiInterni(IDocumento atto) {

        listaMovimentiInterni = MovimentoContabileInterno.createCriteria().list() {
            if (atto instanceof Determina) {
                eq("determina.id", atto.id)
            } else {
                eq("propostaDelibera.id", atto.id)
            }
        }.toDTO()

        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "listaMovimentiInterni");
        BindUtils.postNotifyChange(null, null, this, "conDocumentiContabili");
    }

    @NotifyChange("listaMovimentiInterni")
    @Command
    onEliminaMovimentoInterno(@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("movimento") MovimentoContabileInternoDTO movimentoContabileInterno) {
        Messagebox.show("Eliminare il movimento selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                new org.zkoss.zk.ui.event.EventListener() {
                    void onEvent(Event e) {
                        if (Messagebox.ON_OK.equals(e.getName())) {
                            movimentoContabileInternoService.elimina(movimentoContabileInterno)
                            MovimentiContabiliViewModel.this.caricaListaMovimentiInterni();
                            BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "listaMovimentiInterni");
                        }
                    }
                })
    }

    @NotifyChange("listaMovimentiInterni")
    @Command
    onAggiungiMovimentoInterno() {
        Window w = Executions.createComponents("/atti/integrazioni/contabilita/popupSceltaMovimentiContabiliInterni.zul", null,
                [atto: DocumentoFactory.getDocumento(idDocumento, tipoDocumento)])
        w.onClose { event ->
            caricaListaMovimentiInterni()
            BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "listaMovimentiInterni");
        }
        w.doModal()
    }

    @NotifyChange("listaMovimentiInterni")
    @Command
    onModificaMovimentoInterno(@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("movimento") MovimentoContabileInternoDTO movimentoContabileInterno) {
        Window w = Executions.createComponents("/atti/integrazioni/contabilita/popupSceltaMovimentiContabiliInterni.zul", null,
                [atto: DocumentoFactory.getDocumento(idDocumento, tipoDocumento), movimento: movimentoContabileInterno])
        w.onClose { e ->
            caricaListaMovimentiInterni()
            BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "listaMovimentiInterni");
        }
        w.doModal()
    }
}