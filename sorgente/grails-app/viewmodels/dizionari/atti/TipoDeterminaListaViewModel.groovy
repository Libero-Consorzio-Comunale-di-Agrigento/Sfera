package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.documenti.tipologie.TipoDetermina
import it.finmatica.atti.dto.documenti.tipologie.TipoDeterminaDTO
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.ListModelList
import org.zkoss.zul.Window

class TipoDeterminaListaViewModel extends AfcAbstractGrid {

	// service
	def tipoDeterminaDTOService

	// componenti
	Window self
	Window tipoDeterminaDettaglio

	// dati
	ListModelList<TipoDeterminaDTO> listaTipologiaDetermina

	def lista = []

    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w

		caricaListaTipologiaDetermina()
    }

	private void caricaListaTipologiaDetermina (String filterCondition = filtro) {
		PagedResultList lista = TipoDetermina.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
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
		listaTipologiaDetermina = new ListModelList<TipoDeterminaDTO>(lista.toDTO());

		BindUtils.postNotifyChange(null, null, this, "totalSize")
		BindUtils.postNotifyChange(null, null, this, "listaTipologiaDetermina")
	}

	/*
	 * Implementazione dei metodi per AfcAbstractGrid
	 */

	@NotifyChange(["listaTipologiaDetermina", "totalSize"])
	@Command onPagina() {
		caricaListaTipologiaDetermina()
	}

	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Window w = Executions.createComponents ("/dizionari/atti/tipoDeterminaDettaglio.zul", self, [id: (isNuovoRecord?-1:selectedRecord.id)])
		w.onClose {
			caricaListaTipologiaDetermina()
			BindUtils.postNotifyChange(null, null, this, "listaTipologiaDetermina")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
		}
		w.doModal()
	}

	@NotifyChange(["listaTipologiaDetermina", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onRefresh () {
		filtro = null
		selectedRecord = null
		activePage = 0
		caricaListaTipologiaDetermina()
	}

	@NotifyChange(["listaTipologiaDetermina", "totalSize", "selectedRecord"])
	@Command onElimina () {
		tipoDeterminaDTOService.elimina(selectedRecord)
		selectedRecord = null
		caricaListaTipologiaDetermina()
	}

	@NotifyChange(["visualizzaTutti", "listaTipologiaDetermina", "totalSize", "selectedRecord", "activePage"])
	@Command onVisualizzaTutti() {
		visualizzaTutti = !visualizzaTutti
		selectedRecord = null
		activePage = 0
		caricaListaTipologiaDetermina()
	}

	@NotifyChange(["listaTipologiaDetermina", "totalSize", "selectedRecord", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		selectedRecord = null
		activePage = 0
		caricaListaTipologiaDetermina()
	}

	@NotifyChange(["listaTipologiaDetermina", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onCancelFiltro() {
		onRefresh()
	}
}
