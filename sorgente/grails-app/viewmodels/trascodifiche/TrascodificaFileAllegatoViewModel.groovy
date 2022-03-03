package trascodifiche

import it.finmatica.atti.trascodifiche.TrascodificaService
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.Init
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class TrascodificaFileAllegatoViewModel {

	// services
	TrascodificaService trascodificaService

	// componenti
	Window self

    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w

    }

	@Command onLanciaTrascodifica() {

		lanciaTrascoFileAllegati ()
	}

	private void lanciaTrascoFileAllegati () {
		while (trascodificaService.importaFileAllegato ()) {

		}
		Messagebox.show("La trascodifica dei file allegati è avvenuta con successo");
	}

}
