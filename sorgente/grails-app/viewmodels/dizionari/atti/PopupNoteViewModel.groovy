package dizionari.atti

import it.finmatica.atti.dto.dizionari.EsitoControlloRegolaritaDTO
import it.finmatica.atti.dto.dizionari.EsitoControlloRegolaritaDTOService
import it.finmatica.atti.exceptions.AttiRuntimeException
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupNoteViewModel {

	// componenti
	Window self

	// dati
	String note
	String label
	String title

	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("note")String note, @ExecutionArgParam("title")String title, @ExecutionArgParam("label")String label) {
		this.self = w
		this.note = note
		this.title = title
		this.label = label
	}

	@Command onSalva() {
		if ( !note ){
			throw new AttiRuntimeException ("Attenzione: il campo ${label} non può essere vuoto!")
		}
		Events.postEvent(Events.ON_CLOSE, self, [note:note])
	}

	@Command onChiudi() {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
