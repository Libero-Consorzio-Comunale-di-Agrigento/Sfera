package dizionari.odg

import afc.AfcAbstractRecord
import it.finmatica.atti.dto.odg.dizionari.VotoDTO
import it.finmatica.atti.dto.odg.dizionari.VotoDTOService
import it.finmatica.atti.odg.dizionari.Voto
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class VotoDettaglioViewModel extends AfcAbstractRecord {

	// servizi
	VotoDTOService votoDTOService

	// stato
	List<VotoDTO> listaVoto
	List listaVotiStandard = [Voto.VOTO_FAVOREVOLE, Voto.VOTO_CONTRARIO,Voto.VOTO_ASTENUTO]

	@NotifyChange("selectedRecord")
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id, @ExecutionArgParam("lista") List<VotoDTO> lista) {
		this.self = w

		listaVoto = lista

		if (id != null) {
			selectedRecord = caricaVotoDto(id)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		}
		else {
			selectedRecord = new VotoDTO(valido:true, sequenza:1)
		}
	}

	private VotoDTO caricaVotoDto (Long idVoto){
		Voto voto = Voto.createCriteria().get {
			eq("id", idVoto)
			fetchMode("utenteIns", FetchMode.JOIN)
			fetchMode("utenteUpd", FetchMode.JOIN)
		}
		return voto?.toDTO()
	}

	//////////////////////////////////////////
	//				SALVATAGGIO				//
	//////////////////////////////////////////

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		boolean isNuovoVoto = (selectedRecord.id == null)
		def idVoto = votoDTOService.salva(selectedRecord).id
		selectedRecord = caricaVotoDto(idVoto)

		if (selectedRecord.predefinito)
		checkPredefinito()

		if (isNuovoVoto){
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
		}
		aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
	}

	private void checkPredefinito() {
		VotoDTO votoPredefinito = listaVoto.find {it.predefinito==true}
		if(votoPredefinito != null && votoPredefinito.id!=selectedRecord.id){
			votoPredefinito.predefinito = false
			votoPredefinito = votoDTOService.salva(votoPredefinito)
		}
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
						BindUtils.postNotifyChange(null, null, VotoDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, VotoDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, VotoDettaglioViewModel.this, "datiModifica")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)
	}
}
