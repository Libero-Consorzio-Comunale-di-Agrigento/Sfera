package dizionari.odg

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.atti.dto.odg.dizionari.CommissioneDTOService
import it.finmatica.atti.odg.Commissione
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Window

class CommissioneListaViewModel extends AfcAbstractGrid {

	//service
	CommissioneDTOService commissioneDTOService

	//componenti
	Window self

	//dati
	List<CommissioneDTO> 	listaCommissione

	@Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w
		caricaListaCommissione()
	}

	@NotifyChange(["listaCommissione", "totalSize"])
	private void caricaListaCommissione() {
		PagedResultList lista = Commissione.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) eq ("valido",true)
			if (filtro != null){
				or{
					ilike("descrizione", "%" + filtro + "%")
					ilike("titolo", "%" + filtro + "%")
				}
			}
			fetchMode("ruoloCompetenze", FetchMode.JOIN)
			order('titolo', 'asc')
			order('descrizione', 'asc')
		}
		totalSize  = lista.totalCount
		listaCommissione = lista?.toDTO()
	}

	@NotifyChange(["listaCommissione", "totalSize"])
	@Command onPagina() {
		caricaListaCommissione()
	}

	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Window w = Executions.createComponents ("/dizionari/odg/commissioneDettaglio.zul", self, [id: (isNuovoRecord?null:selectedRecord.id)])
		w.onClose {
			caricaListaCommissione()
			BindUtils.postNotifyChange(null, null, this, "listaCommissione")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
		}
		w.doModal()
	}

	@NotifyChange(["listaCommissione", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onRefresh () {
		filtro = null
		selectedRecord = null
		activePage = 0
		caricaListaCommissione()
	}

	@NotifyChange(["listaCommissione", "totalSize", "selectedRecord"])
	@Command onElimina () {
		commissioneDTOService.elimina(selectedRecord)
		selectedRecord = null
		caricaListaCommissione()
	}

	@NotifyChange(["visualizzaTutti", "listaCommissione", "totalSize", "selectedRecord", "activePage"])
	@Command onVisualizzaTutti() {
		visualizzaTutti = !visualizzaTutti
		selectedRecord = null
		activePage = 0
		caricaListaCommissione()
	}

	@NotifyChange(["listaCommissione", "totalSize", "selectedRecord", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		selectedRecord = null
		activePage = 0
		caricaListaCommissione()
	}

	@NotifyChange(["listaCommissione", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onCancelFiltro() {
		onRefresh()
	}

}
