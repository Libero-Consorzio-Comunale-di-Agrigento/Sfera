package odg

import it.finmatica.atti.dto.odg.dizionari.VotoDTO
import it.finmatica.atti.odg.dizionari.Voto
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupAssegnaVotiViewModel {

   //service

	//componenti
	Window self

	//dati
	List<VotoDTO> listaVoti
	VotoDTO selectedVoto

	// stato

	@Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w
		caricaListaVoti()
	}

	@NotifyChange(["listaEsito"])
	private void caricaListaVoti() {
		listaVoti = Voto.createCriteria().list() {
			eq ("valido",true)
			order('sequenza', 'asc')
		}.toDTO()
	}

	@Command onAssegna() {
		Events.postEvent(Events.ON_CLOSE, self, selectedVoto)
	}

	@Command onChiudi() {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

}
