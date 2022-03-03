package dizionari.atti

import it.finmatica.atti.dizionari.TipoAllegato
import it.finmatica.atti.dto.dizionari.TipoAllegatoDTO
import it.finmatica.atti.dto.documenti.DeliberaDTO
import it.finmatica.atti.dto.documenti.DeterminaDTO
import it.finmatica.atti.dto.documenti.PropostaDeliberaDTO
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class PopupSceltaTipoAllegatoViewModel {
	Window self
	def listaTipiAllegati
    def selectedRecord

	@NotifyChange(["listaTipiAllegati"])
	@Init
	init(
			@ContextParam(ContextType.COMPONENT) Window w,
			@ExecutionArgParam("tipologia") String tipologia, @ExecutionArgParam("id") Long id, @ExecutionArgParam("tipoAllegato") TipoAllegatoDTO tipoAllegato) {
		this.self = w
		selectedRecord = tipoAllegato

		listaTipiAllegati = TipoAllegato.createCriteria().list() {
			eq ("valido", true)
			or {
				isNull("tipologia")
				eq("tipologia", tipologia)
			}
			order("titolo","asc")
		}.toDTO()
	}

	@Command
	onSeleziona() {
		if (selectedRecord != null) {
			Events.postEvent(Events.ON_CLOSE, self, selectedRecord)
		} else {
			Messagebox.show("Selezionare uno dei tipi allegato nella lista", "Attenzione!", Messagebox.OK, Messagebox.EXCLAMATION)
		}
	}

	@Command
	onChiudi() {
        Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@NotifyChange(["listaTipiAllegati"])
	@Command
	onCerca() {
	}
}
