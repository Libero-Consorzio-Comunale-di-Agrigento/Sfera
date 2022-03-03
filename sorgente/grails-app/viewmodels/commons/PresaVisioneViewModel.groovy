package commons

import it.finmatica.atti.integrazioni.SmartDesktopService
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PresaVisioneViewModel {

	SmartDesktopService smartDesktopService

	Window self

	List lista

	@NotifyChange("lista")
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @QueryParam("LISTA_ID") String LISTA_ID) {
		this.self = w
		String[] listaRiferimenti = LISTA_ID?.split("#")
		this.lista = smartDesktopService.presaVisione(listaRiferimenti)
	}

	@Command onChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
