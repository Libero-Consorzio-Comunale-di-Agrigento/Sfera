package dizionari.odg

import afc.AfcAbstractRecord
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.atti.dto.odg.dizionari.ImportoGettoneDTO
import it.finmatica.atti.dto.odg.dizionari.ImportoGettoneDTOService
import it.finmatica.atti.odg.Commissione
import it.finmatica.atti.odg.dizionari.ImportoGettone
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class ImportoGettoneDettaglioViewModel extends AfcAbstractRecord {

	ImportoGettoneDTO 	selectedRecord
	List<CommissioneDTO> listaCommissione
	String importoTesto

	// services
	ImportoGettoneDTOService	importoGettoneDTOService


	@NotifyChange(["selectedRecord","importoTesto"])
    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w


		if (id != null) {
			selectedRecord = caricaImportoGettoneDto(id)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)

			importoTesto = selectedRecord.importo.toString().replace(".", ",")
			stampaImportoCorretto()
		} else {
			selectedRecord = new ImportoGettoneDTO(valido:true)
		}

		listaCommissione = Commissione.createCriteria().list(){
			order("titolo", "asc")
		}.toDTO()
    }

	private ImportoGettoneDTO caricaImportoGettoneDto (Long idImportoGettone){
		ImportoGettone importoGettone = ImportoGettone.createCriteria().get {
			eq("id", idImportoGettone)
		}
		return importoGettone.toDTO()
	}


	private stampaImportoCorretto(){
		def array = importoTesto.split(",")
		if(array.size() == 1) importoTesto = importoTesto + ",00"
		else if (array[1].length() == 0) importoTesto = array[0] + ",00"
		else if(array[1].length() == 1)importoTesto = array[0] + ","+ array[1].charAt(0) + "0"
		else if(array[1].length() >= 2)	importoTesto = array[0] + ","+ array[1].charAt(0) + array[1].charAt(1)

	}


	//Estendo i metodi abstract di AfcAbstractRecord

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica", "importoTesto"])
	@Command onSalva () {
		selectedRecord.importo = Double.parseDouble(importoTesto.replace(',', '.'))
		selectedRecord = importoGettoneDTOService.salva(selectedRecord)
		importoTesto = selectedRecord.importo.toString().replace(".", ",")
		stampaImportoCorretto()
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
						BindUtils.postNotifyChange(null, null, ImportoGettoneDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, ImportoGettoneDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, ImportoGettoneDettaglioViewModel.this, "datiModifica")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)
	}


}
