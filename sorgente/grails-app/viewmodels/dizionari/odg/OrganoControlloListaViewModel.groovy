package dizionari.odg

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dto.odg.dizionari.OrganoControlloDTO
import it.finmatica.atti.dto.odg.dizionari.OrganoControlloDTOService
import it.finmatica.atti.odg.dizionari.OrganoControllo
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Window

class OrganoControlloListaViewModel extends AfcAbstractGrid {

 	//service
	OrganoControlloDTOService organoControlloDTOService

	//componenti
	Window self

	//dati
	List <OrganoControlloDTO> 	listaOrganoControllo

	@Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w
		caricaListaOrganoControllo()
	}

	@NotifyChange(["listaOrganoControllo", "totalSize"])
	private void caricaListaOrganoControllo() {
		PagedResultList lista = OrganoControllo.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) eq ("valido",true)
			if (filtro != null){
				or{
					ilike("titolo", "%" + filtro + "%")
					ilike("descrizione", "%" + filtro + "%")
				}
			}
			order ("sequenza", "asc")
			fetchMode("tipo", FetchMode.JOIN)
		}
		totalSize  = lista.totalCount
		listaOrganoControllo = lista?.toDTO()
	}

	@NotifyChange(["listaOrganoControllo", "totalSize"])
	@Command onPagina() {
		caricaListaOrganoControllo()
	}

	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Window w = Executions.createComponents ("/dizionari/odg/indexOrganoControllo.zul", self, [id: (isNuovoRecord?null:selectedRecord.id)])
		w.onClose {
			caricaListaOrganoControllo()
			BindUtils.postNotifyChange(null, null, this, "listaOrganoControllo")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
		}
		w.doModal()
	}

	@NotifyChange(["listaOrganoControllo", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onRefresh () {
		filtro = null
		selectedRecord = null
		activePage = 0
		caricaListaOrganoControllo()
	}

	@NotifyChange(["listaOrganoControllo", "totalSize", "selectedRecord"])
	@Command onElimina () {
		organoControlloDTOService.elimina(selectedRecord)
		selectedRecord = null
		caricaListaOrganoControllo()
	}

	@NotifyChange(["visualizzaTutti", "listaOrganoControllo", "totalSize", "selectedRecord", "activePage"])
	@Command onVisualizzaTutti() {
		visualizzaTutti = !visualizzaTutti
		selectedRecord = null
		activePage = 0
		caricaListaOrganoControllo()
	}

	@NotifyChange(["listaOrganoControllo", "totalSize", "selectedRecord", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		selectedRecord = null
		activePage = 0
		caricaListaOrganoControllo()
	}

	@NotifyChange(["listaOrganoControllo", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onCancelFiltro() {
		onRefresh()
	}

}
