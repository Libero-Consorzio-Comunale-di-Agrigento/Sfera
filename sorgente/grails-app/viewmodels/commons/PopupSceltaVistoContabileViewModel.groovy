package commons

import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.competenze.VistoParereCompetenze
import it.finmatica.atti.dto.documenti.tipologie.TipoVistoParereDTO
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.Init
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupSceltaVistoContabileViewModel {



	// componenti
	Window self

	// dati
	List<TipoVistoParereDTO> listaVistiContabili
	TipoVistoParereDTO selectedRecord

	def utente

	@Init init(@ContextParam(ContextType.COMPONENT) 			Window w) {
		this.self = w
		caricaListaVistiContabili ()
	}

	private void caricaListaVistiContabili () {

		listaVistiContabili = VistoParereCompetenze.createCriteria().list() {
			projections {
				vistoParere {
					distinct("tipologia")
				}
			}

			AttiGestoreCompetenze.controllaCompetenze(delegate)
			vistoParere {
				tipologia {
					eq("valido", true)
					eq("contabile", true)
				}
			}
		}.toDTO()
		BindUtils.postNotifyChange(null, null, this, "listaVistiContabili")
	}

	@Command onSelezionaVistoContabile() {
		Events.postEvent(Events.ON_CLOSE, self, selectedRecord)
	}

	@Command onAnnulla () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
