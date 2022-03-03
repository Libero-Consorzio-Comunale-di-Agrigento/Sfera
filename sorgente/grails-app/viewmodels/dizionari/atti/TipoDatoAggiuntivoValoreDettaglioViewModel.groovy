package dizionari.atti

import afc.AfcAbstractRecord
import it.finmatica.atti.dizionari.DatiAggiuntiviService
import it.finmatica.atti.dizionari.TipoDatoAggiuntivoValore
import it.finmatica.atti.documenti.TipoDatoAggiuntivo
import it.finmatica.atti.dto.dizionari.TipoDatoAggiuntivoValoreDTO
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class TipoDatoAggiuntivoValoreDettaglioViewModel extends AfcAbstractRecord {

    TipoDatoAggiuntivoValoreDTO selectedRecord

    // services
    DatiAggiuntiviService datiAggiuntiviService

    TipoDatoAggiuntivo tipoDatoAggiuntivo
    List<TipoDatoAggiuntivo> listaTipiDatoAggiuntivo

    @NotifyChange(["selectedRecord"])
    @Init
    init (@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
        this.self = w
        listaTipiDatoAggiuntivo = TipoDatoAggiuntivo.listaDatiAggiuntivi

        if (id != null) {
            selectedRecord = caricaDatoAggiuntivoDto(id)
            tipoDatoAggiuntivo = TipoDatoAggiuntivo.getByCodice(selectedRecord.codice)
            aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
            aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
        } else {
            selectedRecord = new TipoDatoAggiuntivoValoreDTO(valido: true, sequenza: 1)
        }
    }

    private TipoDatoAggiuntivoValoreDTO caricaDatoAggiuntivoDto (Long idDatoAggiuntivo) {
        return TipoDatoAggiuntivoValore.get(idDatoAggiuntivo).toDTO()
    }

    @NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
    @Command
    onSalva () {
        selectedRecord.codice = tipoDatoAggiuntivo.codice
        selectedRecord = datiAggiuntiviService.salva(selectedRecord)
        aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
        aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
    }

    @NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
    @Command
    onSalvaChiudi () {
        onSalva()
        onChiudi()
    }

    @Command
    onSettaValido (@BindingParam("valido") boolean valido) {
        Messagebox.show(Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTesto", [valido ? "valido" : "non valido"].toArray()), Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTitolo"),
                Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                new org.zkoss.zk.ui.event.EventListener() {
                    @NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
                    void onEvent (Event e) {
                        if (Messagebox.ON_OK.equals(e.getName())) {
                            selectedRecord.valido = valido
                            onSalva()
                            BindUtils.postNotifyChange(null, null, TipoDatoAggiuntivoValoreDettaglioViewModel.this, "selectedRecord")
                            BindUtils.postNotifyChange(null, null, TipoDatoAggiuntivoValoreDettaglioViewModel.this, "datiCreazione")
                            BindUtils.postNotifyChange(null, null, TipoDatoAggiuntivoValoreDettaglioViewModel.this, "datiModifica")
                        }
                    }
                }
        )
    }
}
