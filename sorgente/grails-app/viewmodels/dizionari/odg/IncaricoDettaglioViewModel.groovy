package dizionari.odg

import afc.AfcAbstractRecord
import it.finmatica.atti.dto.odg.dizionari.IncaricoDTO
import it.finmatica.atti.odg.dizionari.Incarico
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class IncaricoDettaglioViewModel extends AfcAbstractRecord {

	@NotifyChange("selectedRecord")
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w

		if (id != null) {
			selectedRecord = caricaIncaricoDto(id)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica (selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		} else {
			selectedRecord = new IncaricoDTO(valido:true)
		}
	}

	private IncaricoDTO caricaIncaricoDto (Long idIncarico) {
		return Incarico.get(idIncarico)?.toDTO(["utenteIns", "utenteUpd"])
	}

	//////////////////////////////////////////
	//				SALVATAGGIO				//
	//////////////////////////////////////////

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		boolean isNuovoIncarico = (selectedRecord.id == null)

		// mi permetto di salvare l'incarico direttamente qui dentro senza fare un service transazionale perché è
		// talmente banale che non ha senso fare un service.
		Incarico incarico = selectedRecord.domainObject?:new Incarico ();
		incarico.titolo = selectedRecord.titolo;
		incarico.valido = selectedRecord.valido;
		incarico.save();

		selectedRecord = incarico.toDTO(["utenteIns", "utenteUpd"]);

		if (isNuovoIncarico) {
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
		}
		aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalvaChiudi() {
		onSalva()
		onChiudi()
	}

	@Command onSettaValido(@BindingParam("valido") boolean valido) {
		Messagebox.show("Modificare la validità del voto?", "Modifica validità",
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						super.getSelectedRecord().valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, IncaricoDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, IncaricoDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, IncaricoDettaglioViewModel.this, "datiModifica")
					}
				}
			}
		)
	}
}
