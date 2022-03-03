package commons

import it.finmatica.atti.IProtocolloEsterno
import it.finmatica.atti.IProtocolloEsterno.Classifica
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupClassificazioniViewModel {

	// beans
	IProtocolloEsterno 	protocolloEsterno

	// componenti
	Window self

	// dati
	List<Classifica> listaClassificazioni
	Classifica 		selectedClassificazione
	String			codiceUoProponente

	// stato
	String filtro = ""

	@NotifyChange('listaClassificazioni')
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("codiceUoProponente") String codiceUoProponente) {
		this.self = w;
		this.codiceUoProponente = codiceUoProponente;
		listaClassificazioni = protocolloEsterno.getListaClassificazioni(filtro, codiceUoProponente)?.sort {it.codice}
	}

	@NotifyChange('listaClassificazioni')
	@Command onCerca() {
		listaClassificazioni = protocolloEsterno.getListaClassificazioni(filtro, codiceUoProponente)?.sort {it.codice}
	}

	@Command onSalva() {
		Events.postEvent(Events.ON_CLOSE, self, selectedClassificazione)
	}

	@Command onAnnulla () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
