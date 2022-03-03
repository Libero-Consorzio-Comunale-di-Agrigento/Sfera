package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.documenti.tipologie.TipoDelibera
import it.finmatica.atti.dto.documenti.tipologie.TipoDeliberaDTO
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Window

class TipoDeliberaListaViewModel extends AfcAbstractGrid {

	// service
	def tipoDeliberaDTOService

	// componenti
	Window self
	Window tipoDeliberaDettaglio

	// dati
	List<TipoDeliberaDTO> listaTipologiaDelibera

    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w

		caricaListaTipologiaDelibera()
    }

	@NotifyChange(["listaTipologiaDelibera", "totalSize"])
	private void caricaListaTipologiaDelibera (String filterCondition = filtro) {
		PagedResultList lista = TipoDelibera.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) eq ("valido",true)
			if(filterCondition?:"" != "" ){
				or{
					 ilike("titolo","%${filterCondition}%")
					 ilike("descrizione","%${filterCondition}%")
				}
			}
			order('titolo', 'asc')
		}
		totalSize  = lista.totalCount
		listaTipologiaDelibera = lista.toDTO()
	}

	/*
	 * Implementazione dei metodi per AfcAbstractGrid
	 */

	@NotifyChange(["listaTipologiaDelibera", "totalSize"])
	@Command onPagina() {
		caricaListaTipologiaDelibera()
	}

	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Window w = Executions.createComponents ("/dizionari/atti/tipoDeliberaDettaglio.zul", self, [id: (isNuovoRecord?-1:selectedRecord.id)])
		w.onClose {
			caricaListaTipologiaDelibera()
			BindUtils.postNotifyChange(null, null, this, "listaTipologiaDelibera")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
		}
		w.doModal()
	}

	@NotifyChange(["listaTipologiaDelibera", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onRefresh () {
		filtro = null
		selectedRecord = null
		activePage = 0
		caricaListaTipologiaDelibera()
	}

	@NotifyChange(["listaTipologiaDelibera", "totalSize", "selectedRecord"])
	@Command onElimina () {
		tipoDeliberaDTOService.elimina(selectedRecord)
		selectedRecord = null
		caricaListaTipologiaDelibera()
	}

	@NotifyChange(["visualizzaTutti", "listaTipologiaDelibera", "totalSize", "selectedRecord", "activePage"])
	@Command onVisualizzaTutti() {
		visualizzaTutti = !visualizzaTutti
		selectedRecord = null
		activePage = 0
		caricaListaTipologiaDelibera()
	}

	@NotifyChange(["listaTipologiaDelibera", "totalSize", "selectedRecord", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		selectedRecord = null
		activePage = 0
		caricaListaTipologiaDelibera()
	}

	@NotifyChange(["listaTipologiaDelibera", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onCancelFiltro() {
		onRefresh()
	}
}
