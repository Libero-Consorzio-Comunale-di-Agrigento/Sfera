package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dizionari.Delega
import it.finmatica.atti.dto.dizionari.DelegaDTO
import it.finmatica.atti.dto.dizionari.DelegaDTOService
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Window

class DelegaListaViewModel extends AfcAbstractGrid {

	// services
	DelegaDTOService	delegaDTOService

	// componenti
	Window self

	// dati
	List<DelegaDTO> 	listaDeleghe
    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w
		caricaListaDeleghe()
    }

	@NotifyChange(["listaDeleghe", "totalSize"])
	private void caricaListaDeleghe() {
		PagedResultList lista = Delega.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) eq ("valido",true)
			if (filtro != null){
				or{
					ilike("descrizioneAssessorato", "%" + filtro + "%")
					assessore{
						ilike("cognome", "%" + filtro + "%")
					}
					assessore{
						ilike("nome", "%" + filtro + "%")
					}
				}
			}
			assessore{
				order ("cognome", "asc")
			}
		}
		totalSize  = lista.totalCount
		listaDeleghe = lista.toDTO()
	}

	@NotifyChange(["listaDeleghe", "totalSize"])
	@Command onPagina() {
		caricaListaDeleghe()
	}

	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Window w = Executions.createComponents ("/dizionari/atti/delegaDettaglio.zul", self, [id: (isNuovoRecord?null:selectedRecord.id)])
		w.onClose {
			caricaListaDeleghe()
			BindUtils.postNotifyChange(null, null, this, "listaDeleghe")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
		}
		w.doModal()
	}

	@NotifyChange(["listaDeleghe", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onRefresh () {
		filtro = null
		selectedRecord = null
		activePage = 0
		caricaListaDeleghe()
	}

	@NotifyChange(["listaDeleghe", "totalSize", "selectedRecord"])
	@Command onElimina () {
		delegaDTOService.elimina(selectedRecord)
		selectedRecord = null
		caricaListaDeleghe()
	}

	@NotifyChange(["visualizzaTutti", "listaDeleghe", "totalSize", "selectedRecord", "activePage"])
	@Command onVisualizzaTutti() {
		visualizzaTutti = !visualizzaTutti
		selectedRecord = null
		activePage = 0
		caricaListaDeleghe()
	}

	@NotifyChange(["listaDeleghe", "totalSize", "selectedRecord", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		selectedRecord = null
		activePage = 0
		caricaListaDeleghe()
	}

	@NotifyChange(["listaDeleghe", "totalSize", "selectedRecord", "activePage", "filtro"])
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
