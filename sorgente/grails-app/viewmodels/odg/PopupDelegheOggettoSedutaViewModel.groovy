package odg

import it.finmatica.atti.dto.dizionari.DelegaDTO
import it.finmatica.atti.dto.odg.OggettoSedutaDTO
import it.finmatica.atti.dto.odg.OggettoSedutaDelegaDTO
import it.finmatica.atti.dto.odg.OggettoSedutaDelegaDTOService
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.BindingParam
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.ExecutionArgParam
import org.zkoss.bind.annotation.Init
import org.zkoss.zhtml.Messagebox
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.EventListener
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupDelegheOggettoSedutaViewModel {

	// servizi
	OggettoSedutaDelegaDTOService oggettoSedutaDelegaDTOService

	// componenti
	Window self

	// dati
	OggettoSedutaDTO oggettoSeduta
	List<OggettoSedutaDelegaDTO> listaAltreDeleghe

	@Init
	init (@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("oggettoSeduta") OggettoSedutaDTO oggettoSedutaDto) {
		this.self 			= w
		this.oggettoSeduta 	= oggettoSedutaDto
		caricaListaAltreDeleghe()
	}

	private void caricaListaAltreDeleghe () {
		listaAltreDeleghe = oggettoSedutaDelegaDTOService.getListaAltreDeleghe(oggettoSeduta.domainObject)
		BindUtils.postNotifyChange(null, null, this, "listaAltreDeleghe")
	}

	@Command onAggiungiDelega () {
		Window w = Executions.createComponents("/atti/documenti/popupSceltaDelega.zul", self, [propostaDelibera: oggettoSeduta.propostaDelibera, mostraTogliDelega: false])
		w.onClose { event ->
			if (event?.data != null && !(event.data instanceof String)) {
				DelegaDTO delegaSelezionata = event.data
				if (oggettoSeduta.delega?.id == delegaSelezionata.id) {
					Messagebox.show("La delega selezionata è già impostata come delega principale dell'oggetto seduta", "Attenzione!", Messagebox.OK, Messagebox.EXCLAMATION)
				} else if (listaAltreDeleghe*.delega*.id.contains(delegaSelezionata.id)) {
					Messagebox.show("La delega selezionata è già stata aggiunta", "Attenzione!", Messagebox.OK, Messagebox.EXCLAMATION)
				} else {
					oggettoSedutaDelegaDTOService.aggiungiDelega(oggettoSeduta, delegaSelezionata)
					caricaListaAltreDeleghe()
				}
			}
		}
		w.doModal()
	}

	@Command
	void onEliminaDelega (@BindingParam("altraDelega") OggettoSedutaDelegaDTO altraDelega) {
		Messagebox.show("Eliminare la delega selezionata?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new EventListener() {
				void onEvent (Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						oggettoSedutaDelegaDTOService.eliminaDelega(altraDelega.id)
						caricaListaAltreDeleghe()
					}
				}
			})
	}

	@Command onChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
