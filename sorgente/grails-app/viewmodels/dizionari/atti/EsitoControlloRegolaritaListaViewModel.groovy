package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dizionari.EsitoControlloRegolarita
import it.finmatica.atti.dto.dizionari.EsitoControlloRegolaritaDTO
import it.finmatica.atti.dto.dizionari.EsitoControlloRegolaritaDTOService
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Window

class EsitoControlloRegolaritaListaViewModel extends AfcAbstractGrid {

	//service
	EsitoControlloRegolaritaDTOService esitoControlloRegolaritaDTOService

	//componenti
	Window self

	//dati
	List<EsitoControlloRegolaritaDTO> listaEsito

    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w
		caricaListaEsitoControlloRegolarita()
    }

	@NotifyChange(["listaEsito", "totalSize"])
	private void caricaListaEsitoControlloRegolarita() {
		PagedResultList lista = EsitoControlloRegolarita.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) eq ("valido",true)
			if (filtro != null){
				or{
					ilike("titolo", "%" + filtro + "%")
					ilike("descrizione", "%" + filtro + "%")

				}
			}
			order('titolo', 'asc')
			order('descrizione', 'asc')
		}
		totalSize  = lista.totalCount
		listaEsito = lista?.toDTO()
	}

	@NotifyChange(["listaEsito", "totalSize"])
	@Command onPagina() {
		caricaListaEsitoControlloRegolarita()
	}

	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Window w = Executions.createComponents ("/dizionari/atti/esitoControlloRegolaritaDettaglio.zul", self, [id: (isNuovoRecord?null:selectedRecord.id)])
		w.onClose {
			caricaListaEsitoControlloRegolarita()
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
		caricaListaEsitoControlloRegolarita()
	}

	@NotifyChange(["listaEsito", "totalSize", "selectedRecord"])
	@Command onElimina () {
		esitoControlloRegolaritaDTOService.elimina(selectedRecord)
		selectedRecord = null
		caricaListaEsitoControlloRegolarita()
	}

	@NotifyChange(["visualizzaTutti", "listaEsito", "totalSize", "selectedRecord", "activePage"])
	@Command onVisualizzaTutti() {
		visualizzaTutti = !visualizzaTutti
		selectedRecord = null
		activePage = 0
		caricaListaEsitoControlloRegolarita()
	}

	@NotifyChange(["listaEsito", "totalSize", "selectedRecord", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		selectedRecord = null
		activePage = 0
		caricaListaEsitoControlloRegolarita()
	}

	@NotifyChange(["listaEsito", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onCancelFiltro() {
		onRefresh()
	}

}
