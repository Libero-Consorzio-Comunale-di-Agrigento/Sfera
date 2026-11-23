package dizionari.atti

import afc.AfcAbstractRecord
import it.finmatica.atti.dizionari.ImpattoGenerazionale
import it.finmatica.atti.dto.dizionari.ImpattoGenerazionaleDTO
import it.finmatica.atti.dto.dizionari.ImpattoGenerazionaleDTOService
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class ImpattoGenerazionaleDettaglioViewModel extends AfcAbstractRecord {

	ImpattoGenerazionaleDTO 	selectedRecord
	// services
	ImpattoGenerazionaleDTOService	impattoGenerazionaleDTOService


	@NotifyChange(["selectedRecord"])
    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w

		if (id != null) {
			selectedRecord = caricaImpattoGenerazionaleDto(id)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		} else {
			selectedRecord = new ImpattoGenerazionaleDTO(valido:true)
		}
    }

	private ImpattoGenerazionaleDTO caricaImpattoGenerazionaleDto (Long idImpattoGenerazionale){
		ImpattoGenerazionale impattoGenerazionale = ImpattoGenerazionale.createCriteria().get {
			eq("id", idImpattoGenerazionale)
		}
		return impattoGenerazionale.toDTO()
	}





	//Estendo i metodi abstract di AfcAbstractRecord

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		selectedRecord = impattoGenerazionaleDTOService.salva(selectedRecord)
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
						BindUtils.postNotifyChange(null, null, ImpattoGenerazionaleDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, ImpattoGenerazionaleDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, ImpattoGenerazionaleDettaglioViewModel.this, "datiModifica")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)
	}

}
