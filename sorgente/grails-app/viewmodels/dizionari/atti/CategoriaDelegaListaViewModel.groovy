package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dizionari.CategoriaDelega
import it.finmatica.atti.dto.dizionari.CategoriaDelegaDTOService
import it.finmatica.atti.dto.dizionari.CategoriaDelegaDTO
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Window

class CategoriaDelegaListaViewModel extends AfcAbstractGrid {

	// services
	CategoriaDelegaDTOService categoriaDelegaDTOService

	// componenti
	Window self

	// dati
	List<CategoriaDelegaDTO> 	listaCategorieDeleghe
    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w
		caricaListaCategorieDeleghe()
    }

	@NotifyChange(["listaCategorieDeleghe", "totalSize"])
	private void caricaListaCategorieDeleghe() {
		PagedResultList lista = CategoriaDelega.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) eq ("valido",true)
			if (filtro != null){
				or{
					ilike("descrizione", "%" + filtro + "%")
				}
			}
		}
		totalSize  = lista.totalCount
		listaCategorieDeleghe = lista.toDTO()
	}

	@NotifyChange(["listaCategorieDeleghe", "totalSize"])
	@Command onPagina() {
		caricaListaCategorieDeleghe()
	}

	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Window w = Executions.createComponents ("/dizionari/atti/categoriaDelegaDettaglio.zul", self, [id: (isNuovoRecord?null:selectedRecord.id)])
		w.onClose {
			caricaListaCategorieDeleghe()
			BindUtils.postNotifyChange(null, null, this, "listaCategorieDeleghe")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
		}
		w.doModal()
	}

	@NotifyChange(["listaCategorieDeleghe", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onRefresh () {
		filtro = null
		selectedRecord = null
		activePage = 0
		caricaListaCategorieDeleghe()
	}

	@NotifyChange(["listaCategorieDeleghe", "totalSize", "selectedRecord"])
	@Command onElimina () {
		categoriaDelegaDTOService.elimina(selectedRecord)
		selectedRecord = null
		caricaListaCategorieDeleghe()
	}

	@NotifyChange(["visualizzaTutti", "listaCategorieDeleghe", "totalSize", "selectedRecord", "activePage"])
	@Command onVisualizzaTutti() {
		visualizzaTutti = !visualizzaTutti
		selectedRecord = null
		activePage = 0
		caricaListaCategorieDeleghe()
	}

	@NotifyChange(["listaCategorieDeleghe", "totalSize", "selectedRecord", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		selectedRecord = null
		activePage = 0
		caricaListaCategorieDeleghe()
	}

	@NotifyChange(["listaCategorieDeleghe", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onCancelFiltro() {
		onRefresh()
	}

//	Commentata per evitare che la query parta 2 volte sul command method "onPagina"
//	@NotifyChange(["listaDeleghe", "totalSize"])
//	public void setActivePage (int activePage) {
//		super.setActivePage(activePage)
//		caricaListaDeleghe()
//	}

}
