package dizionari.odg

import afc.AfcAbstractRecord
import it.finmatica.atti.dto.odg.dizionari.GruppoPoliticoDTO
import it.finmatica.atti.dto.odg.dizionari.GruppoPoliticoDTOService
import it.finmatica.atti.odg.dizionari.GruppoPolitico
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class OdgIndexGruppoPoliticoViewModel extends AfcAbstractRecord {

	// services
	GruppoPoliticoDTOService gruppoPoliticoDTOService

	@NotifyChange(["selectedRecord", "soggettiList", "totalSize"])
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		 this.self = w

		 if (id != null) {
			 selectedRecord = caricaGruppoPoliticoDto(id)
			 aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			 aggiornaDatiModifica (selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		 } else {
			 selectedRecord = new GruppoPoliticoDTO(valido:true, sequenza:1)
		 }
	}

	private GruppoPoliticoDTO caricaGruppoPoliticoDto (Long idGruppoPolitico){
		 GruppoPolitico gruppoPolitico = GruppoPolitico.createCriteria().get {
			 eq("id", idGruppoPolitico)
		 }
		 return gruppoPolitico.toDTO()
	}

	//////////////////////////////////////////
	//				SALVATAGGIO				//
	//////////////////////////////////////////

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		if (checkDuplicati()) {
			boolean isNuovoGruppoPolitico = (selectedRecord.id == null)
			def idGruppoPolitico = gruppoPoliticoDTOService.salva(selectedRecord).id
			selectedRecord = caricaGruppoPoliticoDto(idGruppoPolitico)
			if (isNuovoGruppoPolitico){
				aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			}
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		} else
			Messagebox.show("Impossibilie salvare il Gruppo Politico, titolo esistente")

	}

	private boolean checkDuplicati() {
		List<GruppoPolitico> duplicati = GruppoPolitico.createCriteria().list() {
			if (selectedRecord.id != null) {
				ne ("id", selectedRecord.id)
			}
			eq("titolo", selectedRecord.titolo)
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
			Messagebox.show("Modificare la validità del gruppo politico?", "Modifica validità",
				Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					public void onEvent(Event e){
						if(Messagebox.ON_OK.equals(e.getName())) {
							super.getSelectedRecord().valido = valido
							onSalva()
							BindUtils.postNotifyChange(null, null, OdgIndexGruppoPoliticoViewModel.this, "selectedRecord")
							BindUtils.postNotifyChange(null, null, OdgIndexGruppoPoliticoViewModel.this, "datiCreazione")
							BindUtils.postNotifyChange(null, null, OdgIndexGruppoPoliticoViewModel.this, "datiModifica")
						} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
							//Cancel is clicked
						}
					}
				}
			)
		} else
			Messagebox.show("Impossibilie ripristinare il Gruppo Politico, Titolo già esistente !!!")
	}


}
