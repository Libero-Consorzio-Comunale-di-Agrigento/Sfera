package dizionari.atti

import afc.AfcAbstractRecord
import it.finmatica.atti.dizionari.TipoControlloRegolarita
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.dto.dizionari.TipoControlloRegolaritaDTO
import it.finmatica.atti.dto.dizionari.TipoControlloRegolaritaDTOService
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class TipoControlloRegolaritaDettaglioViewModel extends AfcAbstractRecord {

	TipoControlloRegolaritaDTO 	selectedRecord


	// services
	TipoControlloRegolaritaDTOService	tipoControlloRegolaritaDTOService
	def listaTipologie = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO]


	@NotifyChange(["selectedRecord"])
    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w
		if (id != null) {
			selectedRecord = caricaTipoControlloRegolaritaDto(id)
		} else {
			selectedRecord = new TipoControlloRegolaritaDTO(valido:true, sequenza:1)
		}
    }

	private TipoControlloRegolaritaDTO caricaTipoControlloRegolaritaDto (Long idTipoControlloRegolarita){
		TipoControlloRegolarita tipoControlloRegolarita = TipoControlloRegolarita.createCriteria().get {
			eq("id", idTipoControlloRegolarita)
		}
		return tipoControlloRegolarita.toDTO()
	}


	//Estendo i metodi abstract di AfcAbstractRecord

	@NotifyChange(["selectedRecord"])
	@Command onSalva () {
		if (checkDuplicati()) {
			selectedRecord = tipoControlloRegolaritaDTOService.salva(selectedRecord)
		}
		else {
			Messagebox.show("Impossibile salvare il Tipo di Controllo Regolarità, numero di sequenza già utilizzato!!!")
		}
	}

	@NotifyChange(["selectedRecord"])
	@Command onSalvaChiudi() {
		onSalva()
		onChiudi ()
	}

	private boolean checkDuplicati() {
		List<TipoControlloRegolarita> duplicati = TipoControlloRegolarita.createCriteria().list() {
			if (selectedRecord.id != null) {
				ne ("id", selectedRecord.id)
			}
			eq("ambito", selectedRecord.ambito)
			eq("valido", true)
			eq("sequenza", selectedRecord.sequenza)
		};
		return (duplicati.size()>=1 ? false : true)
	}


	@Command onSettaValido(@BindingParam("valido") boolean valido) {
		Messagebox.show(Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTesto",[valido?"valido":"non valido"].toArray()), Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				@NotifyChange(["selectedRecord"])
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						selectedRecord.valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, TipoControlloRegolaritaDettaglioViewModel.this, "selectedRecord")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)
	}


}
