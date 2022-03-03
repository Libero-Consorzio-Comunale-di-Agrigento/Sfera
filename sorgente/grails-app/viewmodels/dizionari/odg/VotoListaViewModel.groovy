package dizionari.odg

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dto.odg.dizionari.VotoDTO
import it.finmatica.atti.dto.odg.dizionari.VotoDTOService
import it.finmatica.atti.odg.dizionari.Voto
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Window

class VotoListaViewModel extends AfcAbstractGrid {

	// servizi
	VotoDTOService votoDTOService

	// componenti
	Window self

	// dati
	List<VotoDTO> listaVoto

	@Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w
		caricaListaVoto()
	}

	@NotifyChange(["listaVoto","totalSize"])
	private void caricaListaVoto(){
	        PagedResultList lista = Voto.createCriteria().list(max: pageSize, offset: pageSize * activePage){
			if(!visualizzaTutti) eq ("valido",true)
			if (filtro != null){
				or{
					ilike("descrizione", "%" + filtro + "%")
					ilike("valore", "%" + filtro + "%")
				}
			}
			order('sequenza','asc')
			order('descrizione','asc')
			order('valore','asc')
		}

		totalSize  = lista.totalCount
        listaVoto = lista.toDTO()
	}

	@NotifyChange(["listaVoto", "totalSize"])
	@Command onPagina() {
		caricaListaVoto()
	}

	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Window w = Executions.createComponents("/dizionari/odg/votoDettaglio.zul", self, [id : ((isNuovoRecord)?null:selectedRecord.id), lista: listaVoto])

		w.onClose {
			caricaListaVoto()
			BindUtils.postNotifyChange(null, null, this, "listaVoto")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
		}
		w.doModal()
	}

	@NotifyChange(["listaVoto", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onRefresh () {
		filtro = null
		selectedRecord = null
		activePage = 0
		caricaListaVoto()
	}

	@NotifyChange(["listaVoto", "totalSize", "selectedRecord"])
	@Command onElimina () {
		votoDTOService.elimina(selectedRecord)
		selectedRecord = null
		caricaListaVoto()
	}

	@NotifyChange(["visualizzaTutti", "listaVoto", "totalSize", "selectedRecord", "activePage"])
	@Command onVisualizzaTutti() {
		visualizzaTutti = !visualizzaTutti
		selectedRecord = null
		activePage = 0
		caricaListaVoto()
	}

	@NotifyChange(["listaVoto", "totalSize", "selectedRecord", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		selectedRecord = null
		activePage = 0
		caricaListaVoto()
	}

	@NotifyChange(["listaVoto", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onCancelFiltro() {
		onRefresh()
	}

}
