package dizionari.atti

import afc.AfcAbstractRecord
import it.finmatica.atti.dizionari.Email
import it.finmatica.atti.dto.dizionari.EmailDTO
import it.finmatica.atti.dto.dizionari.EmailDTOService
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class EmailDettaglioViewModel extends AfcAbstractRecord {

	// services
	EmailDTOService emailDTOService

	@NotifyChange(["selectedRecord", "totalSize"])
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w

		if (id != null) {
			selectedRecord = caricaEmailDto(id)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		} else {
			selectedRecord = new EmailDTO(valido:true)
		}
	}

	private EmailDTO caricaEmailDto (Long idEmail){
		Email email = Email.createCriteria().get {
			eq("id", idEmail)
			fetchMode("utenteIns", FetchMode.JOIN)
			fetchMode("utenteUpd", FetchMode.JOIN)
		}
		return email.toDTO()
	}

	//////////////////////////////////////////
	//				SALVATAGGIO				//
	//////////////////////////////////////////

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		boolean isNuovaEmail = (selectedRecord.id == null)
		def idEmail = emailDTOService.salva(selectedRecord).id
		selectedRecord = caricaEmailDto(idEmail)
		if (isNuovaEmail){
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
		Messagebox.show("Modificare la validità della email?", "Modifica validità",
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						super.getSelectedRecord().valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, EmailDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, EmailDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, EmailDettaglioViewModel.this, "datiModifica")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)
	}

}
