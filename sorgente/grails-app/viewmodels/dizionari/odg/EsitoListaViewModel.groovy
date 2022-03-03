package dizionari.odg

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dto.odg.dizionari.EsitoDTO
import it.finmatica.atti.dto.odg.dizionari.EsitoDTOService
import it.finmatica.atti.odg.dizionari.Esito
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Window

class EsitoListaViewModel extends AfcAbstractGrid {

	//service
	EsitoDTOService esitoDTOService

	//componenti
	Window self

	//dati
	List<EsitoDTO> listaEsito

    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w
		caricaListaEsito()
    }

	@NotifyChange(["listaEsito", "totalSize"])
	private void caricaListaEsito() {
		PagedResultList lista = Esito.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) eq ("valido",true)
			if (filtro != null){
				or{
					ilike("titolo", "%" + filtro + "%")
					ilike("descrizione", "%" + filtro + "%")

				}
			}
			fetchMode("commissione", FetchMode.JOIN)
			fetchMode("esitoStandard", FetchMode.JOIN)
			order('titolo', 'asc')
			order('descrizione', 'asc')
		}
		totalSize  = lista.totalCount
		listaEsito = lista?.toDTO()
	}

	@NotifyChange(["listaEsito", "totalSize"])
	@Command onPagina() {
		caricaListaEsito()
	}

	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Window w = Executions.createComponents ("/dizionari/odg/esitoDettaglio.zul", self, [id: (isNuovoRecord?null:selectedRecord.id)])
		w.onClose {
			caricaListaEsito()
			BindUtils.postNotifyChange(null, null, this, "listaEsito")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
		}
		w.doModal()
	}

	@NotifyChange(["listaEsito", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onRefresh () {
		filtro = null
		selectedRecord = null
		activePage = 0
		caricaListaEsito()
	}

	@NotifyChange(["listaEsito", "totalSize", "selectedRecord"])
	@Command onElimina () {
		esitoDTOService.elimina(selectedRecord)
		selectedRecord = null
		caricaListaEsito()
	}

	@NotifyChange(["visualizzaTutti", "listaEsito", "totalSize", "selectedRecord", "activePage"])
	@Command onVisualizzaTutti() {
		visualizzaTutti = !visualizzaTutti
		selectedRecord = null
		activePage = 0
		caricaListaEsito()
	}

	@NotifyChange(["listaEsito", "totalSize", "selectedRecord", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		selectedRecord = null
		activePage = 0
		caricaListaEsito()
	}

	@NotifyChange(["listaEsito", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onCancelFiltro() {
		onRefresh()
	}

}
