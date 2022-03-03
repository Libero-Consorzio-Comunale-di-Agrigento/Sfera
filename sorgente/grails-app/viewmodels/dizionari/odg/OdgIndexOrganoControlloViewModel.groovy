package dizionari.odg

import afc.AfcAbstractRecord
import it.finmatica.atti.dto.odg.dizionari.*
import it.finmatica.atti.odg.dizionari.OrganoControllo
import it.finmatica.atti.odg.dizionari.OrganoControlloComponente
import it.finmatica.atti.odg.dizionari.OrganoControlloRuolo
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class OdgIndexOrganoControlloViewModel extends AfcAbstractRecord {

	// services
	OrganoControlloDTOService 			organoControlloDTOService
	OrganoControlloRuoloDTOService 		organoControlloRuoloDTOService
	OrganoControlloComponenteDTOService organoControlloComponenteDTOService

	@NotifyChange(["selectedRecord", "soggettiList", "totalSize"])
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		 this.self = w

		 if (id != null) {
			 selectedRecord = caricaOrganoControlloDto(id)
			 aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			 aggiornaDatiModifica (selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		 } else {
			 selectedRecord = new OrganoControlloDTO(valido:true, sequenza:1)
		 }
	}

	private OrganoControlloDTO caricaOrganoControlloDto (Long idOrganoControllo){
		 OrganoControllo organo = OrganoControllo.createCriteria().get {
			 eq("id", idOrganoControllo)
			 fetchMode("tipo", FetchMode.JOIN)
		 }
		 return organo.toDTO()
	}

	//////////////////////////////////////////
	//				SALVATAGGIO				//
	//////////////////////////////////////////

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		if (checkDuplicati()) {
			boolean isNuovoOrganoControllo = (selectedRecord.id == null)
			def idOrganoControllo = organoControlloDTOService.salva(selectedRecord).id
			selectedRecord = caricaOrganoControlloDto(idOrganoControllo)
			if (isNuovoOrganoControllo){
				aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			}
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		} else
			Messagebox.show("Impossibilie salvare l'Organo di Controllo, Tipo Organo di Controllo già utilizzato!!!")

	}

	private boolean checkDuplicati() {
		List<OrganoControllo> duplicati = OrganoControllo.createCriteria().list() {
			if (selectedRecord.id != null) {
				ne ("id", selectedRecord.id)
			}
			eq("tipo.codice", selectedRecord.tipo.codice)
			eq("valido", true)
		};
		return (duplicati.size()>=1 ? false : true)
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalvaChiudi() {
		onSalva()
		onChiudi()
	}

	@Command onSettaValido(@BindingParam("valido") boolean valido) {
		if (checkDuplicati()) {
			Messagebox.show("Modificare la validità dell'organo di controllo?", "Modifica validità",
				Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					public void onEvent(Event e){
						if(Messagebox.ON_OK.equals(e.getName())) {
							super.getSelectedRecord().valido = valido
							onSalva()
							BindUtils.postNotifyChange(null, null, OdgIndexOrganoControlloViewModel.this, "selectedRecord")
							BindUtils.postNotifyChange(null, null, OdgIndexOrganoControlloViewModel.this, "datiCreazione")
							BindUtils.postNotifyChange(null, null, OdgIndexOrganoControlloViewModel.this, "datiModifica")
						} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
							//Cancel is clicked
						}
					}
				}
			)
		} else
			Messagebox.show("Impossibilie ripristinare l'Organo di Controllo, Tipo Organo di Controllo già esistente!!!")
	}

	@Command onRinnovaOrgano() {
		List<OrganoControlloRuoloDTO> listaRuoli = OrganoControlloRuolo.createCriteria().list() {
			eq ("organoControllo.id",selectedRecord.id)
		}?.toDTO();
		listaRuoli.each {
			OrganoControlloRuoloDTO ruolo = it;
			if (ruolo.valido) {
				ruolo.valido = false;
				organoControlloRuoloDTOService.salva(ruolo, selectedRecord);
			}
		}

		List<OrganoControlloComponenteDTO>  listaComponenti = OrganoControlloComponente.createCriteria().list() {
			eq ("organoControllo.id", selectedRecord.id)
			fetchMode("organoControllo", FetchMode.JOIN)
		}?.toDTO()

		listaComponenti.each {
			OrganoControlloComponenteDTO componente = it;
			if (componente.valido) {
				componente.valido = false;
				organoControlloComponenteDTOService.salva(componente, selectedRecord)
			}
		}

		onChiudi ();
	}

}
