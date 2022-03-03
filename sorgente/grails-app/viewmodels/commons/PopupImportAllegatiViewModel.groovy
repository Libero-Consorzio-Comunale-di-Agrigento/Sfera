package commons

import it.finmatica.atti.dto.documenti.AllegatoDTO
import it.finmatica.atti.impostazioni.Impostazioni
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Window

class PopupImportAllegatiViewModel {

	// componenti
	Window self

	// dati
	String urlPopup
	String targetOrigin

	def allegatoDTOService
	AllegatoDTO allegato
	def listaDocumenti
	def gestoreDocumentaleEsterno

	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("allegato") AllegatoDTO allegato) {
		this.self = w
		this.allegato = allegato
		urlPopup = Impostazioni.IMPORT_ALLEGATO_GDM_URL.valore+"/jdms/common/ServletImportAllegatiDoc?rw=Q"
		targetOrigin = Impostazioni.IMPORT_ALLEGATO_GDM_URL.valore
	}

	@Command onSalva () {
		Clients.evalJavaScript("importaDocumenti();");
	}

	@Command onImportaDocumenti (@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		List<String> lista

		if (event.data) {
			lista= event.data
		}

	    if(lista!=null && lista.size()>0){
			if(allegato.titolo==null)
				allegato.titolo = "Allegato"
			allegato = gestoreDocumentaleEsterno?.importAllegati(allegato, lista)
		}
		onChiudi()
	}

	@Command onChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, allegato)
	}

}
