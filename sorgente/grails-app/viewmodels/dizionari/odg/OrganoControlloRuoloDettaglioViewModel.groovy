package dizionari.odg

import afc.AfcAbstractRecord
import it.finmatica.atti.dto.odg.dizionari.OrganoControlloDTO
import it.finmatica.atti.dto.odg.dizionari.OrganoControlloRuoloDTO
import it.finmatica.atti.dto.odg.dizionari.OrganoControlloRuoloDTOService
import it.finmatica.atti.odg.dizionari.OrganoControlloRuolo
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class OrganoControlloRuoloDettaglioViewModel extends AfcAbstractRecord {

	// service
	OrganoControlloRuoloDTOService organoControlloRuoloDTOService

	// dati
	OrganoControlloRuoloDTO organoControlloRuolo
	OrganoControlloDTO 		organoControllo

	@NotifyChange(["selectedRecord", "soggettiList", "totalSize"])
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id, @ExecutionArgParam("organoControllo") OrganoControlloDTO organoControllo)  {
		this.self = w

		this.organoControllo = organoControllo

		if (id != null) {
			selectedRecord = caricaOrganoControlloRuoloDto(id)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		} else {
			selectedRecord = new OrganoControlloRuoloDTO(valido:true)
		}
	}

	private OrganoControlloRuoloDTO caricaOrganoControlloRuoloDto (Long idOrganoControlloRuolo){
		OrganoControlloRuolo organoControlloRuolo = OrganoControlloRuolo.createCriteria().get {
			eq("id", idOrganoControlloRuolo)
			fetchMode("organoControllo", FetchMode.JOIN)
			fetchMode("utenteIns", FetchMode.JOIN)
			fetchMode("utenteUpd", FetchMode.JOIN)
		}
		return organoControlloRuolo.toDTO()
	}

	//////////////////////////////////////////
	//				SALVATAGGIO				//
	//////////////////////////////////////////

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		boolean isNuovoRuolo = (selectedRecord.id == null)
		def idOrganoControlloRuolo = organoControlloRuoloDTOService.salva(selectedRecord, organoControllo).id
		selectedRecord = caricaOrganoControlloRuoloDto(idOrganoControlloRuolo)
		if (isNuovoRuolo){
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
		Messagebox.show("Modificare la validità del ruolo?", "Modifica validità",
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						super.getSelectedRecord().valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, OrganoControlloRuoloDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, OrganoControlloRuoloDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, OrganoControlloRuoloDettaglioViewModel.this, "datiModifica")
					}
				}
			}
		)
	}
}
