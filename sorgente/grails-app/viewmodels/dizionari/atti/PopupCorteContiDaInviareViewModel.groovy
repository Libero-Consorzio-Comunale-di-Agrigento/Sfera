package dizionari.atti

import it.finmatica.atti.exceptions.AttiRuntimeException
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupCorteContiDaInviareViewModel {

	// componenti
	Window self

	// dati
	boolean daInviareCorteConti
	Date dataInvioCorteConti

	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("daInviareCorteConti")boolean daInviareCorteConti, @ExecutionArgParam("dataInvioCorteConti")Date dataInvioCorteConti) {
		this.self = w
		this.daInviareCorteConti = daInviareCorteConti
		this.dataInvioCorteConti = dataInvioCorteConti
	}

	@Command onSalva() {
		if ( !dataInvioCorteConti ){
			throw new AttiRuntimeException ("Attenzione: valorizzare i campi obbligatori")
		}
		Events.postEvent(Events.ON_CLOSE, self, [daInviareCorteConti:daInviareCorteConti, dataInvioCorteConti:dataInvioCorteConti])
	}

	@Command onChiudi() {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
