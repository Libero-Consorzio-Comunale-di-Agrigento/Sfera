package dizionari.impostazioni

import afc.AfcAbstractRecord
import it.finmatica.gestionetesti.reporter.GestioneTestiTipoModello
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiTipoModelloDTO
import it.finmatica.gestionetesti.ui.dizionari.GestioneTestiTipoModelloDTOService
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.media.Media
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Filedownload
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class GestioneTestiTipoModelloDettaglioViewModel  extends AfcAbstractRecord {

	GestioneTestiTipoModelloDTO 	selectedRecord
	boolean fileGiaInserito

	// services
	GestioneTestiTipoModelloDTOService	gestioneTestiTipoModelloDTOService


	@NotifyChange(["selectedRecord", "fileGiaInserito"])
    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("codice") String codice) {
		this.self = w
		fileGiaInserito = false

		if (codice != null) {
			selectedRecord = caricaGestioneTestiTipoModelloDto(codice)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		} else {
			selectedRecord = new GestioneTestiTipoModelloDTO(valido:true)
		}
    }

	private GestioneTestiTipoModelloDTO caricaGestioneTestiTipoModelloDto (String codice){
		GestioneTestiTipoModello gestioneTestiTipoModello = GestioneTestiTipoModello.createCriteria().get {
			eq("codice", codice)
		}
		GestioneTestiTipoModelloDTO result = gestioneTestiTipoModello.toDTO()
		if (result.query != null) {
			fileGiaInserito	= true
			result.query	= null
		} else {
			fileGiaInserito = false
		}
		return result
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica", "fileGiaInserito"])
	@Command onUpload (@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
		Media media = event.media
		selectedRecord = gestioneTestiTipoModelloDTOService.salva(selectedRecord, media.stringData.bytes)
		aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
		aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		fileGiaInserito=true
	}

	@Command onDownload (@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
		Filedownload.save(gestioneTestiTipoModelloDTOService.getFileAllegato(selectedRecord.codice), "text/xml", selectedRecord.codice)
	}

	//Estendo i metodi abstract di AfcAbstractRecord

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		selectedRecord = gestioneTestiTipoModelloDTOService.salva(selectedRecord)
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
						GestioneTestiTipoModelloDettaglioViewModel.this.selectedRecord.valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, GestioneTestiTipoModelloDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, GestioneTestiTipoModelloDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, GestioneTestiTipoModelloDettaglioViewModel.this, "datiModifica")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)
	}
}
