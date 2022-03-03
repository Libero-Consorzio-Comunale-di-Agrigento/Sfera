package commons

import afc.AfcAbstractGrid
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupUnitaOrganizzativaViewModel extends AfcAbstractGrid {

	// service
	def springSecurityService

	// componenti
	Window self

	// dati
	List<So4UnitaPubbDTO> listaUnita

	// stato


	@NotifyChange("listaUnita")
	@Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w
	}

	private void caricaListaUnita () {
		def list = So4UnitaPubb.allaData(new Date()).perOttica(springSecurityService.principal.ottica().codice) {
			ilike ("descrizione", "%"+filtro+"%")
			maxResults(pageSize)
			firstResult(pageSize * activePage)
		}

		listaUnita = list.toDTO()
		totalSize  = So4UnitaPubb.allaData(new Date()).perOttica(springSecurityService.principal.ottica().codice).count() {
			ilike ("descrizione", "%"+filtro+"%")
		}

		BindUtils.postNotifyChange(null, null, this, "listaUnita")
		BindUtils.postNotifyChange(null, null, this, "totalSize")
	}

	@Command onSelezionaUnita () {
		Events.postEvent(Events.ON_CLOSE, self, [unita: selectedRecord])
	}

	@Command onAnnulla () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	/*
	 * Implementazione metodi AfcAbstractGrid
	 */

	@NotifyChange(["listaUnita", "totalSize"])
	@Command onPagina() {
		caricaListaUnita()
	}

	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		throw new AttiRuntimeException ("Non è possibile modificare Unità Organizzative.")
	}

	@NotifyChange(["listaUnita", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onRefresh () {
		filtro = null
		selectedRecord = null
		activePage = 0
		caricaListaUnita()
	}

	@NotifyChange(["listaUnita", "totalSize", "selectedRecord"])
	@Command onElimina () {
		throw new AttiRuntimeException ("Non è possibile eliminare Unità Organizzative.")
	}

	@NotifyChange(["visualizzaTutti", "listaUnita", "totalSize", "selectedRecord", "activePage"])
	@Command onVisualizzaTutti() {
		visualizzaTutti = !visualizzaTutti
		selectedRecord = null
		activePage = 0
		caricaListaUnita()
	}

	@NotifyChange(["listaUnita", "totalSize", "selectedRecord", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		selectedRecord = null
		activePage = 0
		caricaListaUnita()
	}

	@NotifyChange(["listaUnita", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onCancelFiltro() {
		onRefresh()
	}

}
