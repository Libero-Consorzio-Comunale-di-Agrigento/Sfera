package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dto.dizionari.TipoBudgetDTOService
import it.finmatica.atti.dizionari.TipoBudget
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window


class TipoBudgetListaViewModel extends AfcAbstractGrid {

    // services
    TipoBudgetDTOService tipoBudgetDTOService

    // componenti
    Window self

    // dati
    List listaTipiBudget

    @Init
    init (@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w
        caricaListaTipoBudget()
    }

    @NotifyChange(["listaTipiBudget", "totalSize"])
    private void caricaListaTipoBudget (String filterCondition = filtro) {
        PagedResultList lista = TipoBudget.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
            if (!visualizzaTutti) {
                eq("valido", true)
            }
            if (filterCondition ?: "" != "") {
                ilike ("titolo", "%" + filterCondition + "%")
            }
            order("titolo", "asc")
        }
        totalSize = lista.totalCount
        listaTipiBudget = lista?.toDTO(["unitaSo4", "utenteAd4"])
    }

    @NotifyChange(["listaTipiBudget", "totalSize"])
    @Command
    onPagina () {
        caricaListaTipoBudget()
    }

    @Command
    onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
        Long idTipoBudget = isNuovoRecord ? null : selectedRecord.id
        Window w = Executions.createComponents("/dizionari/atti/tipoBudgetDettaglio.zul", self, [id: idTipoBudget])
        w.onClose {
            activePage = 0
            caricaListaTipoBudget()
            BindUtils.postNotifyChange(null, null, this, "listaTipiBudget")
            BindUtils.postNotifyChange(null, null, this, "totalSize")
            BindUtils.postNotifyChange(null, null, this, "activePage")
        }
        w.doModal()
    }

    @NotifyChange(["listaTipiBudget", "totalSize", "selectedRecord", "activePage"])
    @Command
    onRefresh () {
        activePage = 0
        caricaListaTipoBudget()
        selectedRecord = null
    }

    @Command
    onElimina () {
        Messagebox.show("Tipo Budget", "Tipo Budget",
                Messagebox.OK | Messagebox.CANCEL, Messagebox.EXCLAMATION,
                new org.zkoss.zk.ui.event.EventListener() {
                    public void onEvent(Event e){
                        if(Messagebox.ON_OK.equals(e.getName())) {
                            //se è l'ultimo della pagina di visualizzazione decremento di uno la activePage
                            tipoBudgetDTOService.elimina(TipoBudgetListaViewModel.this.selectedRecord)
                            TipoBudgetListaViewModel.this.selectedRecord = null
                            TipoBudgetListaViewModel.this.caricaListaTipoBudget()
                            BindUtils.postNotifyChange(null, null, TipoBudgetListaViewModel.this, "activePage")
                            BindUtils.postNotifyChange(null, null, TipoBudgetListaViewModel.this, "totalSize")
                            BindUtils.postNotifyChange(null, null, TipoBudgetListaViewModel.this, "listaTipiBudget")
                            BindUtils.postNotifyChange(null, null, TipoBudgetListaViewModel.this, "selectedRecord")
                        } else if(Messagebox.ON_CANCEL.equals(e.getName())) {
                            //Cancel is clicked
                        }
                    }
                }
        )
    }

    @NotifyChange(["listaTipiBudget", "totalSize", "activePage", "visualizzaTutti"])
    @Command
    onVisualizzaTutti () {
        activePage = 0
        visualizzaTutti = !visualizzaTutti
        caricaListaTipoBudget()
    }

    @NotifyChange(["listaTipiBudget", "totalSize", "activePage"])
    @Command
    onFiltro (@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
        activePage = 0
        // Passa l'evento generato su onChanging del textbox filtro e ricarica i dati
        if (event instanceof InputEvent) {
            caricaListaTipoBudget(event.value)
        } else {
            caricaListaTipoBudget()
        }
    }

    @NotifyChange(["listaTipiBudget", "totalSize", "filtro", "activePage"])
    @Command
    onCancelFiltro () {
        activePage = 0
        filtro = ""
        caricaListaTipoBudget()
    }

    @Command
    public void onImportExcel() {
        Window w = Executions.createComponents("/dizionari/atti/excelImport.zul", self, [:])
        w.position = "top_center"
        w.doModal()
        w.onClose {
            caricaListaTipoBudget()
            BindUtils.postNotifyChange(null, null, null, "activePage")
            BindUtils.postNotifyChange(null, null, null, "totalSize")
            BindUtils.postNotifyChange(null, null, null, "listaTipiBudget")
            BindUtils.postNotifyChange(null, null, null, "selectedRecord")
        }

    }
}
