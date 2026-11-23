package atti.integrazioni.contabilita

import it.finmatica.atti.contabilita.MovimentoContabile
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.DocumentoFactory
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.impostazioni.CampiDocumento
import it.finmatica.atti.integrazioni.contabilita.IntegrazioneContabilitaAtsPavia
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class MovimentiContabiliAtsPaviaViewModel {

    IntegrazioneContabilitaAtsPavia integrazioneContabilitaAtsPavia

    Determina determina

    def listaMovimenti
    def campiProtetti

    boolean            competenzaInModifica = false
    MovimentoContabile movimento
    private long       idDocumento
    private String     tipoDocumento
    String             codiceEsterno

    boolean attoConImpegnoSpesa = false
    boolean attoConVariazione = false
    boolean attoTrovaCopertura = false

    // componenti
    @Wire("#popupModificaMovimentoContabile")
    Window popupModificaMovimentoContabile

    @Init
    init() {

    }

    @AfterCompose
    public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, false);
    }

    @GlobalCommand("aggiornaAtto")
    @NotifyChange(["competenzaInModifica", "campiProtetti", "listaMovimenti", "codiceEsterno", "listaCapitoli", "listaSoggetti"])
    public void aggiornaAtto(@BindingParam("atto") IDocumento atto, @BindingParam("competenza") String competenza) {
        //metodo richiamato quando si accede la prima volta al folder contabilità o dopo ogni salvataggio dell'atto
        idDocumento = atto.id;
        tipoDocumento = atto.TIPO_OGGETTO;

        //in base al mapping integrazioni stabilisco la tipologia di atto (e quindi i campi da mostrare a video)
        attoConImpegnoSpesa = integrazioneContabilitaAtsPavia.isAttoConImpegnoSpesa(atto.tipologiaDocumento)
        if (!(attoConImpegnoSpesa)) {
            attoConVariazione = integrazioneContabilitaAtsPavia.isAttoConVariazione(atto.tipologiaDocumento)

            if (!(attoConVariazione)) {
                attoTrovaCopertura = integrazioneContabilitaAtsPavia.isAttoTrovaCopertura(atto.tipologiaDocumento)
            }
        }

        this.competenzaInModifica = "W".equals(competenza);
        if (this.competenzaInModifica) {
            // calcolo i campi che devo proteggere in lettura
            campiProtetti = CampiDocumento.getMappaCampi(atto.campiProtetti)
        }

        aggiornaMovimentiContabili(atto)

        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "competenzaInModifica");
        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "attoConImpegnoSpesa");
        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "attoConVariazione");
        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "attoTrovaCopertura");
    }

    private void aggiornaMovimentiContabili() {
        aggiornaMovimentiContabili(DocumentoFactory.getDocumento(idDocumento, tipoDocumento));
    }

    private void aggiornaMovimentiContabili(IDocumento atto) {
        listaMovimenti = integrazioneContabilitaAtsPavia.getMovimentiContabili(atto);

        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "listaMovimenti");
    }

    @Command
    onChiudiMovimentoContabile() {
        popupModificaMovimentoContabile.visible = false;
    }

    @NotifyChange(["movimento", "listaMovimenti"])
    @Command onAggiungiMovimentoContabile(@BindingParam("tipologia") String tipologia) {
        Window w = Executions.createComponents("/atti/integrazioni/contabilita/popupSceltaMovimentiContabiliAtsPavia.zul", null,
                [atto: DocumentoFactory.getDocumento(idDocumento, tipoDocumento),
                 movimento: null,
                 attoConImpegnoSpesa: this.attoConImpegnoSpesa,
                 attoConVariazione: this.attoConVariazione,
                 attoTrovaCopertura: this.attoTrovaCopertura])
        w.onClose { event ->
            aggiornaMovimentiContabili()
            BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "listaMovimenti");
        }
        w.doModal()
    }

    @NotifyChange(["movimento", "listaMovimenti"])
    @Command
    onModificaMovimentoContabile(@BindingParam("idMovimentoContabile") long idMovimentoContabile) {
        movimento = MovimentoContabile.get(idMovimentoContabile);
        movimento.discard();

        Window w = Executions.createComponents("/atti/integrazioni/contabilita/popupSceltaMovimentiContabiliAtsPavia.zul", null,
                [atto: DocumentoFactory.getDocumento(idDocumento, tipoDocumento),
                 movimento: movimento,
                 attoConImpegnoSpesa: attoConImpegnoSpesa,
                 attoConVariazione: attoConVariazione,
                 attoTrovaCopertura: attoTrovaCopertura])
        w.onClose { event ->
            aggiornaMovimentiContabili()
            BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "listaMovimenti");
        }
        w.doModal()

    }

    @NotifyChange(["listaMovimenti"])
    @Command
    onEliminaMovimentoContabile(@BindingParam("idMovimentoContabile") long idMovimentoContabile) {
        Messagebox.show("Eliminare il movimento contabile selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                new org.zkoss.zk.ui.event.EventListener() {
                    public void onEvent(Event e) {
                        if (Messagebox.ON_OK.equals(e.getName())) {
                            MovimentoContabile.get(idMovimentoContabile).delete();
                            MovimentiContabiliAtsPaviaViewModel.this.aggiornaMovimentiContabili();

                            BindUtils.postNotifyChange(null, null, this, "listaMovimenti")
                        }
                    }
                })
    }
}