package atti.documenti

import it.finmatica.atti.dizionari.Delega
import it.finmatica.atti.dto.dizionari.DelegaDTO
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.Init
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zul.Window

class PopupSceltaDelegaViewModel {

	// componenti
	Window self

	// dati
	List<DelegaDTO> listaDeleghe
	DelegaDTO selectedRecord

	String filtro=""

	@Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w
		caricaListaSoggetti ()
	}

	private void caricaListaSoggetti (String filterCondition = filtro) {
		listaDeleghe = Delega.createCriteria().list() {
			ilike("descrizioneAssessorato", "%"+filterCondition+"%")

			eq("valido", true)
			order("descrizioneAssessorato", "asc")

			fetchMode("assessore", 			 FetchMode.JOIN)
			fetchMode("assessore.utenteAd4", FetchMode.JOIN)
		}.toDTO()
		BindUtils.postNotifyChange(null, null, this, "listaDeleghe")
	}

	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		// Passa l'evento generato su onChanging del textbox filtro e ricarica i dati
		if (event instanceof InputEvent) {
			caricaListaSoggetti(event.value)
		} else {
			caricaListaSoggetti()
		}
	}

	@Command onSeleziona() {
		Events.postEvent(Events.ON_CLOSE, self, selectedRecord)
	}

	@Command onTogliDelega() {
		Events.postEvent(Events.ON_CLOSE, self, "eliminaDelega")
	}

	@Command onAnnulla () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
