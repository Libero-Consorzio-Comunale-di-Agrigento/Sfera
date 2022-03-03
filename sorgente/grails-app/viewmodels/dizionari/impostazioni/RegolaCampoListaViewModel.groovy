package dizionari.impostazioni

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.impostazioni.RegolaCampo
import it.finmatica.atti.impostazioni.RegolaCampoService
import it.finmatica.atti.dto.impostazioni.RegolaCampoDTO
import org.hibernate.criterion.CriteriaSpecification
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window


class RegolaCampoListaViewModel extends AfcAbstractGrid {

    // services
    RegolaCampoService regolaCampoService

    // componenti
    Window self

    // dati
    List listaRegolaCampo

    @Init
    init (@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w
        caricaListaRegolaCampo()
    }

    @NotifyChange(["listaRegolaCampo", "totalSize"])
    private void caricaListaRegolaCampo (String filterCondition = filtro) {
        PagedResultList lista = RegolaCampo.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
            createAlias ("tipoOggetto", "to", CriteriaSpecification.LEFT_JOIN)
            createAlias ("wkfAttore", "attore", CriteriaSpecification.LEFT_JOIN)
            if (!visualizzaTutti) {
                eq("valido", true)
            }
            if (filterCondition ?: "" != "") {
            }
            order("to.codice", "asc")
            order("blocco", "asc")
            order("campo", "asc")
        }
        totalSize = lista.totalCount
        listaRegolaCampo = lista.toDTO()
    }

    @NotifyChange(["listaDatoAggiuntivo", "totalSize"])
    @Command
    onPagina () {
        caricaListaRegolaCampo()
    }

    @Command
    onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
        Long idRegolaCampo = isNuovoRecord ? null : selectedRecord.id
        Window w = Executions.createComponents("/dizionari/impostazioni/regolaCampoDettaglio.zul", self, [id: idRegolaCampo])
        w.onClose {
            activePage = 0
            caricaListaRegolaCampo()
            BindUtils.postNotifyChange(null, null, this, "listaRegolaCampo")
            BindUtils.postNotifyChange(null, null, this, "totalSize")
            BindUtils.postNotifyChange(null, null, this, "activePage")
        }
        w.doModal()
    }

    @NotifyChange(["listaRegolaCampo", "totalSize", "selectedRecord", "activePage"])
    @Command
    onRefresh () {
        activePage = 0
        caricaListaRegolaCampo()
        selectedRecord = null
    }

    @Command
    onElimina () {
        Messagebox.show("Regola Visibilità Campo", "Regola Visibilità Campo",
                Messagebox.OK | Messagebox.CANCEL, Messagebox.EXCLAMATION,
                new org.zkoss.zk.ui.event.EventListener() {
                    public void onEvent(Event e){
                        if(Messagebox.ON_OK.equals(e.getName())) {
                            //se è l'ultimo della pagina di visualizzazione decremento di uno la activePage
                            regolaCampoService.elimina(RegolaCampoListaViewModel.this.selectedRecord)
                            RegolaCampoListaViewModel.this.selectedRecord = null
                            RegolaCampoListaViewModel.this.caricaListaRegolaCampo()
                            BindUtils.postNotifyChange(null, null, RegolaCampoListaViewModel.this, "activePage")
                            BindUtils.postNotifyChange(null, null, RegolaCampoListaViewModel.this, "totalSize")
                            BindUtils.postNotifyChange(null, null, RegolaCampoListaViewModel.this, "listaRegolaCampo")
                            BindUtils.postNotifyChange(null, null, RegolaCampoListaViewModel.this, "selectedRecord")
                        } else if(Messagebox.ON_CANCEL.equals(e.getName())) {
                            //Cancel is clicked
                        }
                    }
                }
        )
    }

    @NotifyChange(["listaRegolaCampo", "totalSize", "activePage", "visualizzaTutti"])
    @Command
    onVisualizzaTutti () {
        activePage = 0
        visualizzaTutti = !visualizzaTutti
        caricaListaRegolaCampo()
    }

    @NotifyChange(["listaRegolaCampo", "totalSize", "activePage"])
    @Command
    onFiltro (@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
        activePage = 0
        // Passa l'evento generato su onChanging del textbox filtro e ricarica i dati
        if (event instanceof InputEvent) {
            caricaListaRegolaCampo(event.value)
        } else {
            caricaListaRegolaCampo()
        }
    }

    @NotifyChange(["listaRegolaCampo", "totalSize", "filtro", "activePage"])
    @Command
    onCancelFiltro () {
        activePage = 0
        filtro = ""
        caricaListaRegolaCampo()
    }
}
