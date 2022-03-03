package commons

import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class PopupSceltaModelloTestoViewModel {
	Window self
	List<GestioneTestiModelloDTO> listaModelliTesto
	def gestioneTestiModelloDTOService
	def selectedRecord

	@NotifyChange(["listaModelliTesto"])
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("tipoOggetto") String tipoOggetto ) {
		this.self = w
		listaModelliTesto = gestioneTestiModelloDTOService.getListaModelli(tipoOggetto)
	}

	@Command onSeleziona() {
		if(selectedRecord != null){
			Events.postEvent(Events.ON_CLOSE, self, selectedRecord)
		} else {
			Messagebox.show("Selezionare uno dei modelli testo presenti nella lista", "Attenzione!", Messagebox.OK , Messagebox.EXCLAMATION)
		}
	}

	@Command onChiudi() {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
