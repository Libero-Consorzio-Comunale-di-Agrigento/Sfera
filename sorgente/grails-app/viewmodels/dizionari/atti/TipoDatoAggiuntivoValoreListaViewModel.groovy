package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dizionari.DatiAggiuntiviService
import it.finmatica.atti.dizionari.TipoDatoAggiuntivoValore
import it.finmatica.atti.documenti.DatoAggiuntivo
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.dto.dizionari.TipoDatoAggiuntivoValoreDTO
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class TipoDatoAggiuntivoValoreListaViewModel extends AfcAbstractGrid {

    // services
    DatiAggiuntiviService datiAggiuntiviService

    // componenti
    Window self

    // dati
    List<TipoDatoAggiuntivoValoreDTO> listaDatoAggiuntivo

    @Init
    init (@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w
        caricaListaDatoAggiuntivo()
    }

    @NotifyChange(["listaDatoAggiuntivo", "totalSize"])
    private void caricaListaDatoAggiuntivo (String filterCondition = filtro) {
        PagedResultList lista = TipoDatoAggiuntivoValore.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
            if (!visualizzaTutti) {
                eq("valido", true)
            }
            if (filterCondition ?: "" != "") {
                ilike("codice", "%${filterCondition}%")
            }
            order("sequenza", "asc")
            order("codice", "asc")
        }
        totalSize = lista.totalCount
        listaDatoAggiuntivo = lista.toDTO()
    }

    @NotifyChange(["listaDatoAggiuntivo", "totalSize"])
    @Command
    onPagina () {
        caricaListaDatoAggiuntivo()
    }

    @Command
    onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
        Long idDatoAggiuntivo = isNuovoRecord ? null : selectedRecord.id
        Window w = Executions.createComponents("/dizionari/atti/tipoDatoAggiuntivoValoreDettaglio.zul", self, [id: idDatoAggiuntivo])
        w.onClose {
            activePage = 0
            caricaListaDatoAggiuntivo()
            BindUtils.postNotifyChange(null, null, this, "listaDatoAggiuntivo")
            BindUtils.postNotifyChange(null, null, this, "totalSize")
            BindUtils.postNotifyChange(null, null, this, "activePage")
        }
        w.doModal()
    }

    @NotifyChange(["listaDatoAggiuntivo", "totalSize", "selectedRecord", "activePage"])
    @Command
    onRefresh () {
        activePage = 0
        caricaListaDatoAggiuntivo()
        selectedRecord = null
    }

    @Command
    onElimina () {
        Messagebox.show(Labels.getLabel("dizionario.cancellaRecordMessageBoxTesto"), Labels.getLabel("dizionario.cancellaRecordMessageBoxTitolo"),
            Messagebox.OK | Messagebox.CANCEL, Messagebox.EXCLAMATION,
            new org.zkoss.zk.ui.event.EventListener() {
                void onEvent (Event e) {
                    if (Messagebox.ON_OK.equals(e.getName())) {
                        // se è l'ultimo della pagina di visualizzazione decremento di uno la activePage
                        if (listaDatoAggiuntivo.size() == 1) {
                            TipoDatoAggiuntivoValoreListaViewModel.this.activePage = TipoDatoAggiuntivoValoreListaViewModel.this.activePage == 0 ? 0 : TipoDatoAggiuntivoValoreListaViewModel.this.activePage - 1
                        }
                        datiAggiuntiviService.elimina(TipoDatoAggiuntivoValoreListaViewModel.this.selectedRecord)
                        TipoDatoAggiuntivoValoreListaViewModel.this.selectedRecord = null
                        TipoDatoAggiuntivoValoreListaViewModel.this.caricaListaDatoAggiuntivo()
                        BindUtils.postNotifyChange(null, null, TipoDatoAggiuntivoValoreListaViewModel.this, "activePage")
                        BindUtils.postNotifyChange(null, null, TipoDatoAggiuntivoValoreListaViewModel.this, "listaDatoAggiuntivo")
                        BindUtils.postNotifyChange(null, null, TipoDatoAggiuntivoValoreListaViewModel.this, "totalSize")
                        BindUtils.postNotifyChange(null, null, TipoDatoAggiuntivoValoreListaViewModel.this, "selectedRecord")
                    }
                }
            }
        )
    }

    @NotifyChange(["listaDatoAggiuntivo", "totalSize", "activePage", "visualizzaTutti"])
    @Command
    onVisualizzaTutti () {
        activePage = 0
        visualizzaTutti = !visualizzaTutti
        caricaListaDatoAggiuntivo()
    }

    @NotifyChange(["listaDatoAggiuntivo", "totalSize", "activePage"])
    @Command
    onFiltro (@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
        activePage = 0
        // Passa l'evento generato su onChanging del textbox filtro e ricarica i dati
        if (event instanceof InputEvent) {
            caricaListaDatoAggiuntivo(event.value)
        } else {
            caricaListaDatoAggiuntivo()
        }
    }

    @NotifyChange(["listaDatoAggiuntivo", "totalSize", "filtro", "activePage"])
    @Command
    onCancelFiltro () {
        activePage = 0
        filtro = ""
        caricaListaDatoAggiuntivo()
    }
}
