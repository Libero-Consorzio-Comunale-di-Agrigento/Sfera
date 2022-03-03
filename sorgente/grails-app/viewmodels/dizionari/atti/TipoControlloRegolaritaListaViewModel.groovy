package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dizionari.TipoControlloRegolarita
import it.finmatica.atti.dto.dizionari.TipoControlloRegolaritaDTO
import it.finmatica.atti.dto.dizionari.TipoControlloRegolaritaDTOService
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Window

class TipoControlloRegolaritaListaViewModel extends AfcAbstractGrid {

 	//service
	TipoControlloRegolaritaDTOService tipoControlloRegolaritaDTOService

	//componenti
	Window self

	//dati
	List <TipoControlloRegolaritaDTO> 	listaTipiControlloRegolarita

	@Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w
		caricaListaTipiControlloRegolarita()
	}

	@NotifyChange(["listaTipiControlloRegolarita", "totalSize"])
	private void caricaListaTipiControlloRegolarita() {
		PagedResultList lista = TipoControlloRegolarita.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) eq ("valido",true)
			if (filtro != null){
				or{
					ilike("titolo", "%" + filtro + "%")
				}
			}
			order ("sequenza", "asc")
		}
		totalSize  = lista.totalCount
		listaTipiControlloRegolarita = lista?.toDTO()
	}

	@NotifyChange(["listaTipiControlloRegolarita", "totalSize"])
	@Command onPagina() {
		caricaListaTipiControlloRegolarita()
	}

	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Window w = Executions.createComponents ("/dizionari/atti/tipoControlloRegolaritaDettaglio.zul", self, [id: (isNuovoRecord?null:selectedRecord.id)])
		w.onClose {
			caricaListaTipiControlloRegolarita()
			BindUtils.postNotifyChange(null, null, this, "listaTipiControlloRegolarita")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
		}
		w.doModal()
	}

	@NotifyChange(["listaTipiControlloRegolarita", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onRefresh () {
		filtro = null
		selectedRecord = null
		activePage = 0
		caricaListaTipiControlloRegolarita()
	}

	@NotifyChange(["listaTipiControlloRegolarita", "totalSize", "selectedRecord"])
	@Command onElimina () {
		tipoControlloRegolaritaDTOService.elimina(selectedRecord)
		selectedRecord = null
		caricaListaTipiControlloRegolarita()
	}

	@NotifyChange(["visualizzaTutti", "listaTipiControlloRegolarita", "totalSize", "selectedRecord", "activePage"])
	@Command onVisualizzaTutti() {
		visualizzaTutti = !visualizzaTutti
		selectedRecord = null
		activePage = 0
		caricaListaTipiControlloRegolarita()
	}

	@NotifyChange(["listaTipiControlloRegolarita", "totalSize", "selectedRecord", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		selectedRecord = null
		activePage = 0
		caricaListaTipiControlloRegolarita()
	}

	@NotifyChange(["listaTipiControlloRegolarita", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onCancelFiltro() {
		onRefresh()
	}

}
