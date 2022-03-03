package afc

import it.finmatica.as4.As4SoggettoCorrente
import org.zkoss.bind.annotation.BindingParam
import org.zkoss.bind.annotation.Command
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

abstract class AfcAbstractRecord implements IAfcRecord {

	Window 		self

	String 		datiCreazione
	String 		datiModifica

	def 		selectedRecord

	protected void aggiornaDatiCreazione (String idUtenteIns, Date dataCreazione) {
		As4SoggettoCorrente utenteCreazione = As4SoggettoCorrente.createCriteria().get{
			eq("utenteAd4.id", idUtenteIns)
		}
		if (utenteCreazione != null)
			datiCreazione = "Documento creato da " + utenteCreazione.nome +" "+ utenteCreazione.cognome +" il " + dataCreazione?.format("dd/MM/yyyy")
		else
			datiCreazione = "Documento creato il " + dataCreazione?.format("dd/MM/yyyy")
	}

	protected void aggiornaDatiModifica (String idUtenteMod, Date dataModifica) {
		As4SoggettoCorrente utenteModifica  = As4SoggettoCorrente.createCriteria().get{
			eq("utenteAd4.id", idUtenteMod)
		}

		if (utenteModifica != null)
			datiModifica = "Ultima modifica effettuata da " + utenteModifica.nome +" "+ utenteModifica.cognome +" il " + dataModifica?.format("dd/MM/yyyy")
		else
			datiModifica = "Ultima modifica effettuata il " + dataModifica?.format("dd/MM/yyyy")
	}

	@Command onChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@Command abstract onSalva();
	@Command abstract onSalvaChiudi();
	@Command abstract onSettaValido(@BindingParam("valido") boolean valido);

}
