package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.documenti.tipologie.TipoVistoParere
import it.finmatica.atti.dto.documenti.tipologie.TipoVistoParereDTO
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Window

class TipoVistoParereListaViewModel extends AfcAbstractGrid {

	// service
	def tipoVistoParereDTOService

	// componenti
	Window self
	Window tipoVistoParereDettaglio

	// dati
	List<TipoVistoParereDTO> listaTipologiaVistoParere

	// stato
	int pageSize 	= AfcAbstractGrid.PAGE_SIZE_DEFAULT
	int activePage 	= 0
	int	totalSize	= 0

    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w

		caricaListaTipologiaVistoParere()
    }

	@NotifyChange(["listaTipologiaVistoParere", "totalSize"])
	private void caricaListaTipologiaVistoParere (String filterCondition = filtro) {
		PagedResultList lista = TipoVistoParere.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) eq ("valido",true)
			if(filterCondition?:"" != "" ){
				or{
					 ilike("titolo","%${filterCondition}%")
					 ilike("descrizione","%${filterCondition}%")
				}
			}
			order('descrizione', 'asc')
			fetchMode("cfgIter", FetchMode.JOIN)
		}

		totalSize  = lista.totalCount
		listaTipologiaVistoParere = lista.toDTO()
	}

	/*
	 * Implementazione dei metodi per AfcAbstractGrid
	 */

	@NotifyChange(["listaTipologiaVistoParere", "totalSize"])
	@Command onPagina() {
		caricaListaTipologiaVistoParere()
	}

	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Window w = Executions.createComponents ("/dizionari/atti/tipoVistoParereDettaglio.zul", self, [id: (isNuovoRecord?-1:selectedRecord.id)])
		w.onClose {
			caricaListaTipologiaVistoParere()
			BindUtils.postNotifyChange(null, null, this, "listaTipologiaVistoParere")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
		}
		w.doModal()
	}

	@NotifyChange(["listaTipologiaVistoParere", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onRefresh () {
		filtro = null
		selectedRecord = null
		activePage = 0
		caricaListaTipologiaVistoParere()
	}

	@NotifyChange(["listaTipologiaVistoParere", "totalSize", "selectedRecord"])
	@Command onElimina () {
		tipoVistoParereDTOService.elimina(selectedRecord)
		selectedRecord = null
		caricaListaTipologiaVistoParere()
	}

	@NotifyChange(["visualizzaTutti", "listaTipologiaVistoParere", "totalSize", "selectedRecord", "activePage"])
	@Command onVisualizzaTutti() {
		visualizzaTutti = !visualizzaTutti
		selectedRecord = null
		activePage = 0
		caricaListaTipologiaVistoParere()
	}

	@NotifyChange(["listaTipologiaVistoParere", "totalSize", "selectedRecord", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		selectedRecord = null
		activePage = 0
		caricaListaTipologiaVistoParere()
	}

	@NotifyChange(["listaTipologiaVistoParere", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onCancelFiltro() {
		onRefresh()
	}
}
