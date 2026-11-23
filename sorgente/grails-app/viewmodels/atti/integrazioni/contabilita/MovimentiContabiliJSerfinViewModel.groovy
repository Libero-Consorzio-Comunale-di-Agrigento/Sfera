package atti.integrazioni.contabilita

import it.finmatica.atti.contabilita.MovimentoContabile
import it.finmatica.atti.documenti.DocumentoFactory
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.impostazioni.CampiDocumento
import it.finmatica.atti.integrazioni.contabilita.IntegrazioneContabilitaJSerfin
import org.apache.log4j.Logger
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class MovimentiContabiliJSerfinViewModel {
    private static final Logger log = Logger.getLogger(MovimentiContabiliJSerfinViewModel.class)

    IntegrazioneContabilitaJSerfin  integrazioneContabilitaJSerfin

    MovimentoContabile movimento

    private long       idDocumento
    private String     tipoDocumento
    boolean            competenzaInModifica = false
    boolean            isRicercaAnagrafica = true //stabilisce se si può collegare un movimento ad un'anagrafica
    boolean            visualizzaTotali = false //stabilisce se visualizzare la somma dei movimenti contabili
    boolean            visualizzaCodiceGlobale = false //stabilisce se visualizzare il campo codiceGlobale
    String             codiceEsterno  //codice atto nell'applicativo contabilità. Da valutare se serve per J-Serfin

    BigDecimal totaleImpegni
    BigDecimal totaleAccertamenti
    BigDecimal totalePrenotazioni
    BigDecimal totaleSubImpegni
    BigDecimal totaleImpegniVariazioni
    BigDecimal totaleAccertamentiVariazioni
    BigDecimal totalePrenotazioniVariazioni
    BigDecimal totaleSubImpegniVariazioni
    BigDecimal totaleImpegniPrenotazioni

    //Mappa per contare il numero di impegni, accertamenti, ecc...e di conseguenza visualizzare le icone nel tab
    Map<String, Integer> conteggioPerTipo = [:]

    IDocumento atto

    def  listaMovimenti

    def campiProtetti
    String tipologia

    @Init
    init() {
        isRicercaAnagrafica = integrazioneContabilitaJSerfin.isRicercaAnagraficaAbilitata()
        visualizzaTotali = integrazioneContabilitaJSerfin.isVisualizzaTotali()
        visualizzaCodiceGlobale = integrazioneContabilitaJSerfin.isRicercaMovimentiCodiceGlobale()
    }


    @GlobalCommand("aggiornaAtto")
    @NotifyChange(["competenzaInModifica", "campiProtetti", "listaMovimenti", "listaPrenotazioni", "codiceEsterno", "numeroImpegni", "numeroAccertamenti", "numeroPrenotazioni", "numeroSubImpegni"])
    public void aggiornaAtto(@BindingParam("atto") IDocumento atto, @BindingParam("competenza") String competenza) {
        idDocumento = atto.id;
        tipoDocumento = atto.TIPO_OGGETTO;

        this.competenzaInModifica = "W".equals(competenza);
        aggiornaMovimentiContabili(atto)

        campiProtetti = CampiDocumento.getMappaCampi(atto?.campiProtetti)

        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "campiProtetti");
        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "competenzaInModifica");

        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "listaMovimenti");
    }

    private void aggiornaMovimentiContabili() {
        aggiornaMovimentiContabili(DocumentoFactory.getDocumento(idDocumento, tipoDocumento));
    }

    private void aggiornaMovimentiContabili(IDocumento atto) {
        log.info("aggiornaMovimentiContabili: id = " + atto.idDocumento)
        listaMovimenti = integrazioneContabilitaJSerfin.getMovimentiContabili(atto)
        if (integrazioneContabilitaJSerfin.visualizzaTotali) {
            aggiornaTotaliSpese(atto)
        }

        conteggioPerTipo = listaMovimenti?.countBy { it.tipoDettaglio } ?: [:]

        BindUtils.postNotifyChange(null, null, this, "listaMovimenti");
        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "listaMovimenti");
        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "numeroImpegni");
        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "numeroAccertamenti");
        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "numeroPrenotazioni");
        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "numeroSubImpegni");
    }

    int getNumeroImpegni() {
        conteggioPerTipo['IMPEGNO'] ?: 0
    }

    int getNumeroAccertamenti() {
        conteggioPerTipo['ACCERTAMENTO'] ?: 0
    }

    int getNumeroPrenotazioni() {
        conteggioPerTipo['PRENOTAZIONE'] ?: 0
    }

    int getNumeroSubImpegni() {
        conteggioPerTipo['SUBIMPEGNO'] ?: 0
    }

    @NotifyChange(["movimento", "listaMovimenti"])
    @Command onAggiungiMovimentoContabile(@BindingParam("tipologia") String tipologia) {
        Window w = Executions.createComponents("/atti/integrazioni/contabilita/popupSceltaMovimentiContabiliJSerfin.zul", null,
                [atto: DocumentoFactory.getDocumento(idDocumento, tipoDocumento), movimento: null, tipologia: tipologia, duplica: false])
        w.onClose { event ->
            aggiornaMovimentiContabili()
            BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "listaMovimenti");
        }
        w.doModal()
    }

    @NotifyChange(["movimento", "listaMovimenti"])
    @Command onModificaMovimento(@BindingParam("movimento") long idMovimento) {
        MovimentoContabile movimento = MovimentoContabile.get(idMovimento)
        Window w = Executions.createComponents("/atti/integrazioni/contabilita/popupSceltaMovimentiContabiliJSerfin.zul", null,
                [atto: DocumentoFactory.getDocumento(idDocumento, tipoDocumento), movimento: movimento, tipologia: null, duplica: false])
        w.onClose { event ->
            aggiornaMovimentiContabili()
            BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "listaMovimenti");
        }
        w.doModal()
    }

    @NotifyChange(["movimento", "listaMovimenti"])
    @Command
    onEliminaMovimento(@BindingParam("idMovimentoContabile") long idMovimentoContabile) {
        Messagebox.show("Eliminare il movimento contabile selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                new org.zkoss.zk.ui.event.EventListener() {
                    public void onEvent(Event e) {
                        if (Messagebox.ON_OK.equals(e.getName())) {
                            MovimentoContabile movimentoContabile = MovimentoContabile.get(idMovimentoContabile)

                            if (movimentoContabile) {
                                if (movimentoContabile.anagrafica) {
                                    //DA FAREverifico se l'anagrafica è collegata ad altri movimenti....se non lo è la elimina

                                }
                            }

                            if (integrazioneContabilitaJSerfin.isContabilitaIntegrata(movimentoContabile.documentoPrincipale)) {
                                log.info("Prima di eliminare impegno su JSerfin con id = " + movimentoContabile.id)
                                integrazioneContabilitaJSerfin.eliminaImpegno(movimentoContabile)
                                log.info("Dopo eliminazione impegno su JSerfin con id = " + movimentoContabile.id)
                            }
                            log.info("Eliminazione impegno su db Sfera con id = " + movimentoContabile.id)
                            MovimentoContabile.get(idMovimentoContabile).delete(flush: true)

                            aggiornaMovimentiContabili()
                            BindUtils.postNotifyChange(null, null, this, "movimento")
                            BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "listaMovimenti");
                        }
                    }
                })
    }

    private void aggiornaTotaliSpese(IDocumento atto) {
        totaleImpegni = integrazioneContabilitaJSerfin.totaleImporto(atto, "IMPEGNO")
        totaleAccertamenti = integrazioneContabilitaJSerfin.totaleImporto(atto, "ACCERTAMENTO")
        totalePrenotazioni = integrazioneContabilitaJSerfin.totaleImporto(atto, "PRENOTAZIONE")
        totaleSubImpegni = integrazioneContabilitaJSerfin.totaleImporto(atto, "SUBIMPEGNO")

        totaleImpegniVariazioni = integrazioneContabilitaJSerfin.totaleImporto(atto, "IMPEGNO_VARIAZIONE")
        totaleAccertamentiVariazioni = integrazioneContabilitaJSerfin.totaleImporto(atto, "ACCERTAMENTO_VARIAZIONE")
        totalePrenotazioniVariazioni = integrazioneContabilitaJSerfin.totaleImporto(atto, "PRENOTAZIONE_VARIAZIONE")
        totaleSubImpegniVariazioni = integrazioneContabilitaJSerfin.totaleImporto(atto, "SUBIMPEGNO_VARIAZIONE")

        totaleImpegniPrenotazioni = integrazioneContabilitaJSerfin.totaleImporto(atto, "IMPEGNO_PRENOTAZIONE")

        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "totaleImpegni")
        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "totaleAccertamenti")
        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "totalePrenotazioni")
        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "totaleSubImpegni")

        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "totaleImpegniVariazioni")
        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "totaleAccertamentiVariazioni")
        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "totalePrenotazioniVariazioni")
        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "totaleSubImpegniVariazioni")

        BindUtils.postNotifyChange("movimentiContabiliQueue", null, this, "totaleImpegniPrenotazioni")
    }
}