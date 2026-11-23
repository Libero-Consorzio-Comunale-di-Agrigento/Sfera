package commons

import it.finmatica.atti.IProtocolloEsterno
import it.finmatica.atti.IProtocolloEsterno.Fascicolo
import it.finmatica.atti.impostazioni.Impostazioni
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupFascicoliViewModel {
	// beans
	IProtocolloEsterno protocolloEsterno

	// componenti
	Window self

	// dati
	List<Fascicolo>	listaFascicoli
	Fascicolo		selectedFascicolo
	String 			classificaCodice
	String 			classificaDescrizione
	boolean			abilitaCreaFascicolo
	String			codiceUoProponente
	Date 			classificaDal

	// stato
	String filtro 	= ""

	@NotifyChange(['listaFascicoli','abilitaCreaFascicolo'])
	@Init init(@ContextParam(ContextType.COMPONENT) 		Window w
			 , @ExecutionArgParam("classificaCodice") 		String classificaCodice
			 , @ExecutionArgParam("classificaDescrizione") 	String classificaDescrizione
			 , @ExecutionArgParam("classificaDal") 			Date classificaDal
			 , @ExecutionArgParam("codiceUoProponente") 	String codiceUoProponente) {
		this.self = w
		this.classificaDescrizione 	= classificaDescrizione
		this.classificaCodice 		= classificaCodice
		this.codiceUoProponente 	= codiceUoProponente
		this.classificaDal			= classificaDal
		listaFascicoli 				= protocolloEsterno.getListaFascicoli(filtro, classificaCodice, classificaDal, codiceUoProponente)
		abilitaCreaFascicolo		= Impostazioni.PROTOCOLLO_CREA_FASCICOLO.abilitato
	}

	@NotifyChange('listaFascicoli')
	@Command onCerca() {
		listaFascicoli = protocolloEsterno.getListaFascicoli(filtro, classificaCodice, classificaDal, codiceUoProponente)
	}

	@Command onSalva() {
		Events.postEvent(Events.ON_CLOSE, self, selectedFascicolo)
	}

	@Command onAnnulla () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@Command onSvuotaClassifica () {
		this.classificaCodice 		= "";
		this.classificaDescrizione 	= "";
		this.classificaDal			= null;
		onCerca();
		BindUtils.postNotifyChange(null, null, this, "classificaCodice")
		BindUtils.postNotifyChange(null, null, this, "classificaDescrizione")
		BindUtils.postNotifyChange(null, null, this, "classificaDal")
	}

	@NotifyChange([ 'classificaCodice', 'classificaDescrizione','classificaDal'])
	@Command onAggiornaClassifica () {
		if (selectedFascicolo == null) {
			return;
		}

		this.classificaCodice 		= selectedFascicolo.classifica.codice;
		this.classificaDescrizione 	= selectedFascicolo.classifica.descrizione;
		this.classificaDal			= selectedFascicolo.classifica.dal
	}

	@Command onCreaFascicolo () {
		if (Impostazioni.DOCER.abilitato) {
			Window w = Executions.createComponents("/commons/popupCreaFascicolo.zul", self, [classificaCodice: this.classificaCodice, listaFascicoli: this.listaFascicoli])
			w.onClose {
				onCerca()
			}
			w.doModal()
		}
	}
}
