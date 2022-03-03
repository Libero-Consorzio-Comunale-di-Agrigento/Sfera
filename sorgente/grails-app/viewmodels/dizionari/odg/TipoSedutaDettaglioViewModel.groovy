package dizionari.odg

import afc.AfcAbstractRecord
import it.finmatica.atti.dto.odg.dizionari.TipoSedutaDTO
import it.finmatica.atti.dto.odg.dizionari.TipoSedutaDTOService
import it.finmatica.atti.odg.dizionari.TipoSeduta
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class TipoSedutaDettaglioViewModel  extends AfcAbstractRecord {

	// service
	TipoSedutaDTOService tipoSedutaDTOService

    @NotifyChange(["selectedRecord", "totalSize"])
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w

		if (id != null) {
			selectedRecord = caricaTipoSedutaDto(id)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		} else {
			selectedRecord = new TipoSedutaDTO(valido:true, sequenza:1)
		}
	}

	private TipoSedutaDTO caricaTipoSedutaDto (Long idTipoSeduta){
		TipoSeduta tipoSeduta = TipoSeduta.createCriteria().get {
			eq("id", idTipoSeduta)
			fetchMode("utenteIns", FetchMode.JOIN)
			fetchMode("utenteUpd", FetchMode.JOIN)
		}
		return tipoSeduta.toDTO()
	}

	//////////////////////////////////////////
	//				SALVATAGGIO				//
	//////////////////////////////////////////

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		boolean isNuovatipoSeduta = (selectedRecord.id == null)
		def idTipoSeduta = tipoSedutaDTOService.salva(selectedRecord).id
		selectedRecord = caricaTipoSedutaDto(idTipoSeduta)
		if (isNuovatipoSeduta){
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
		Messagebox.show("Modificare la validità del Tipo Seduta?", "Modifica validità",
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						super.getSelectedRecord().valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, TipoSedutaDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, TipoSedutaDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, TipoSedutaDettaglioViewModel.this, "datiModifica")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)
	}

}
