package odg

import it.finmatica.atti.dto.odg.dizionari.EsitoDTO
import it.finmatica.atti.dto.odg.dizionari.EsitoDTOService
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupAssegnaEsitoViewModel {

  	// service
	EsitoDTOService esitoDTOService;

	// componenti
	Window self

	// dati
	List<EsitoDTO> listaEsito
	EsitoDTO selectedEsito
	String note

	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("idCommissione")long idCommissione) {
		this.self = w
		caricaListaEsito (idCommissione);
	}

	@NotifyChange(["listaEsito"])
	private void caricaListaEsito (long idCommissione) {
		listaEsito = esitoDTOService.getListaEsiti (null, idCommissione)
	}

	@Command onAssegna() {
		Events.postEvent(Events.ON_CLOSE, self, [selectedEsito:selectedEsito, note:note])
	}

	@Command onChiudi() {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
