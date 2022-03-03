package dizionari.atti

import afc.AfcAbstractRecord
import it.finmatica.atti.dizionari.EsitoControlloRegolarita
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.dto.dizionari.EsitoControlloRegolaritaDTO
import it.finmatica.atti.dto.dizionari.EsitoControlloRegolaritaDTOService
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class EsitoControlloRegolaritaDettaglioViewModel extends AfcAbstractRecord {

    // service
	EsitoControlloRegolaritaDTOService esitoControlloRegolaritaDTOService
	List<String> listaAmbiti = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO];
	
	// dati

	// stato
	boolean	inModifica   = false

	@NotifyChange(["selectedRecord",  "listaEsitoStandard", "listaCommissione", "totalSize"])
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w

		if (id != null) {
			selectedRecord = caricaEsitoControlloRegolaritaDto(id)

			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica (selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		} else {
			selectedRecord = new EsitoControlloRegolarita(valido:true, sequenza:1)
		}

	}

	private EsitoControlloRegolaritaDTO caricaEsitoControlloRegolaritaDto (Long idEsito){
		EsitoControlloRegolarita esitoControlloRegolarita = EsitoControlloRegolarita.createCriteria().get {
			eq("id", idEsito)
			
			fetchMode("utenteIns", 			FetchMode.JOIN)
			fetchMode("utenteUpd", 			FetchMode.JOIN)
		}
		return esitoControlloRegolarita.toDTO()
	}

	//////////////////////////////////////////
	//				SALVATAGGIO				//
	//////////////////////////////////////////

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		boolean isNuovoEsito = (selectedRecord.id == null)
		if (isNuovoEsito){
			selectedRecord = selectedRecord.toDTO()
		}
		def idEsito = esitoControlloRegolaritaDTOService.salva(selectedRecord).id
		selectedRecord = caricaEsitoControlloRegolaritaDto(idEsito)
		if (isNuovoEsito){
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
		Messagebox.show("Modificare la validità dell'esito?", "Modifica validità",
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						super.getSelectedRecord().valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, EsitoControlloRegolaritaDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, EsitoControlloRegolaritaDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, EsitoControlloRegolaritaDettaglioViewModel.this, "datiModifica")
					}
				}
			}
		)
	}

}
