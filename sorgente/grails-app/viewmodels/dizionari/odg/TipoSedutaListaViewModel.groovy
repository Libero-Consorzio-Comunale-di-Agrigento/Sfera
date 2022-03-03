package dizionari.odg

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dto.odg.dizionari.TipoSedutaDTO
import it.finmatica.atti.dto.odg.dizionari.TipoSedutaDTOService
import it.finmatica.atti.odg.dizionari.TipoSeduta
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Window

class TipoSedutaListaViewModel extends AfcAbstractGrid {

	// service
	TipoSedutaDTOService tipoSedutaDTOService

	// componenti
	Window self

	// dati
	List<TipoSedutaDTO> listaTipoSeduta

    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w
		caricaListaTipoSeduta()
    }

	@NotifyChange(["listaTipoSeduta","totalSize"])
	private void caricaListaTipoSeduta() {
		PagedResultList lista = TipoSeduta.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) eq ("valido",true)
			if (filtro != null){
				or{
					ilike("descrizione", "%" + filtro + "%")
					ilike("titolo", "%" + filtro + "%")
				}
			}
			order ("sequenza","asc")
			order ("titolo","asc")
			order ("descrizione","asc")
		}
		totalSize  = lista.totalCount
		listaTipoSeduta = lista?.toDTO()
	}

	@NotifyChange(["listaTipoSeduta", "totalSize"])
	@Command onPagina() {
		caricaListaTipoSeduta()
	}

	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Window w = Executions.createComponents ("/dizionari/odg/tipoSedutaDettaglio.zul", self, [id: (isNuovoRecord?null:selectedRecord.id)])
		w.onClose {
			caricaListaTipoSeduta()
			BindUtils.postNotifyChange(null,null, this, "listaTipoSeduta")
			BindUtils.postNotifyChange(null,null, this, "totalsize")
		}
		w.doModal()
	}

	@NotifyChange(["listaTipoSeduta", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onRefresh () {
		filtro = null
		selectedRecord = null
		activePage = 0
		caricaListaTipoSeduta()
	}

	@NotifyChange(["listaTipoSeduta", "totalSize", "selectedRecord"])
	@Command onElimina () {
		tipoSedutaDTOService.elimina(selectedRecord)
		selectedRecord = null
		caricaListaTipoSeduta()
	}

	@NotifyChange(["visualizzaTutti", "listaTipoSeduta", "totalSize", "selectedRecord", "activePage"])
	@Command onVisualizzaTutti() {
		visualizzaTutti = !visualizzaTutti
		selectedRecord = null
		activePage = 0
		caricaListaTipoSeduta()
	}

	@NotifyChange(["listaTipoSeduta", "totalSize", "selectedRecord", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		selectedRecord = null
		activePage = 0
		caricaListaTipoSeduta()
	}

	@NotifyChange(["listaTipoSeduta", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onCancelFiltro() {
		onRefresh()
	}

}
