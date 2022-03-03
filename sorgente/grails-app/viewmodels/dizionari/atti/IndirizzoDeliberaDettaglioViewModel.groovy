package dizionari.atti

import afc.AfcAbstractRecord
import it.finmatica.atti.dizionari.IndirizzoDelibera
import it.finmatica.atti.dto.dizionari.IndirizzoDeliberaDTO
import it.finmatica.atti.dto.dizionari.IndirizzoDeliberaDTOService
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class IndirizzoDeliberaDettaglioViewModel extends AfcAbstractRecord {

	IndirizzoDeliberaDTO 	selectedRecord


	// services
	IndirizzoDeliberaDTOService	indirizzoDeliberaDTOService


	@NotifyChange(["selectedRecord"])
    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w


		if (id != null) {
			selectedRecord = caricaIndirizzoDeliberaDto(id)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		} else {
			selectedRecord = new IndirizzoDeliberaDTO(valido:true)
		}
    }

	private IndirizzoDeliberaDTO caricaIndirizzoDeliberaDto (Long idIndirizzoDelibera){
		IndirizzoDelibera indirizzoDelibera = IndirizzoDelibera.createCriteria().get {
			eq("id", idIndirizzoDelibera)
		}
		return indirizzoDelibera.toDTO()
	}





	//Estendo i metodi abstract di AfcAbstractRecord

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		selectedRecord = indirizzoDeliberaDTOService.salva(selectedRecord)
		aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
		aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalvaChiudi() {
		onSalva()
		onChiudi ()
	}


	@Command onSettaValido(@BindingParam("valido") boolean valido) {
		Messagebox.show(Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTesto",[valido?"valido":"non valido"].toArray()), Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						selectedRecord.valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, IndirizzoDeliberaDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, IndirizzoDeliberaDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, IndirizzoDeliberaDettaglioViewModel.this, "datiModifica")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)
	}


}
