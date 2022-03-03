package dizionari.atti

import it.finmatica.atti.dto.dizionari.EsitoControlloRegolaritaDTO
import it.finmatica.atti.dto.dizionari.EsitoControlloRegolaritaDTOService
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupAssegnaEsitoControlloRegolaritaViewModel {

  	// service
	EsitoControlloRegolaritaDTOService esitoControlloRegolaritaDTOService;

	// componenti
	Window self

	// dati
	List<EsitoControlloRegolaritaDTO> listaEsito
	EsitoControlloRegolaritaDTO selectedEsito
	String note

	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("ambito")String ambito, @ExecutionArgParam("idEsito")Long idEsito, @ExecutionArgParam("note")String note) {
		this.self = w
		this.note = note
		caricaListaEsito (ambito);
		if (idEsito != null) selectedEsito = listaEsito.find {it.id == idEsito};
	}

	@NotifyChange(["listaEsito"])
	private void caricaListaEsito (String ambito) {
		listaEsito = esitoControlloRegolaritaDTOService.getListaEsiti (ambito)
	}

	@Command onAssegna() {
		Events.postEvent(Events.ON_CLOSE, self, [selectedEsito:selectedEsito, note:note])
	}

	@Command onChiudi() {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
