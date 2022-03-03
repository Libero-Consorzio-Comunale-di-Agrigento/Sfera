package commons

import it.finmatica.atti.documenti.tipologie.TipoDelibera
import it.finmatica.atti.documenti.tipologie.TipoDetermina
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupCambiaTipologiaViewModel {

		//componenti
		Window self

		//dati
		def listaTipologie
		def selectedRecord

		//stato
		Class tipoDocumento

		@Init init(@ContextParam(ContextType.COMPONENT) Window w,  @ExecutionArgParam("documento") String doc) {
			this.self = w
			if(doc == "determina")
				tipoDocumento = TipoDetermina
			else if (doc == "propostaDelibera")
				tipoDocumento = TipoDelibera
			caricaListaTipologie ()
		}

		private void caricaListaTipologie () {
			listaTipologie = tipoDocumento.createCriteria().list() {
				eq("valido",true)
				order("titolo","asc")
			}.toDTO()
			BindUtils.postNotifyChange(null, null, this, "listaTipologie")
		}

		@Command onAnnulla() {
			Events.postEvent(Events.ON_CLOSE, self, null)
		}

		@Command onScegli() {
			Events.postEvent(Events.ON_CLOSE, self, selectedRecord)
		}
}
