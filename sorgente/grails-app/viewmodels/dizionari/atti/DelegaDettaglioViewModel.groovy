package dizionari.atti

import afc.AfcAbstractRecord
import grails.orm.PagedResultList
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.dizionari.Delega
import it.finmatica.atti.dto.dizionari.DelegaDTO
import it.finmatica.atti.dto.dizionari.DelegaDTOService
import it.finmatica.atti.exceptions.AttiRuntimeException
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zk.ui.event.OpenEvent
import org.zkoss.zk.ui.event.SelectEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class DelegaDettaglioViewModel extends AfcAbstractRecord {

	String 		filtroSoggetti
	String 		assessore
	boolean 	modificabile = false

	List<As4SoggettoCorrenteDTO> soggettiList
	List<DelegaDTO> storico
	int pageSize 	= 10
	int activePage 	= 0
	int	totalSize	= 0

	// services
	DelegaDTOService	delegaDTOService


	@NotifyChange(["selectedRecord", "soggettiList", "totalSize", "storico"])
    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w

		// Inizializo le variabili di classe
		activePage = 0
		filtroSoggetti = ""

	//	soggettiList = loadSoggetti().toDTO()

		if (id != null) {
			selectedRecord = caricaDelegaDto(id)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
			assessore = (selectedRecord?.assessore?.cognome?:"") + (selectedRecord?.assessore?.nome?(" "+selectedRecord?.assessore?.nome):"")
			caricaStorico()
		} else {
			selectedRecord = new DelegaDTO(valido:true, sequenza:1)
			assessore = ""
			modificabile = true
			storico = null
		}
    }

	private DelegaDTO caricaDelegaDto (Long idDelega){
		Delega delega = Delega.createCriteria().get {
			eq("id", idDelega)
			fetchMode("assessore", FetchMode.JOIN)
			fetchMode("utenteIns", FetchMode.JOIN)
			fetchMode("utenteUpd", FetchMode.JOIN)
		}
		return delega.toDTO()
	}

	private void caricaStorico (){
		if (selectedRecord.idDelegaStorico > 0) {
			storico = Delega.findAllByIdDelegaStorico(selectedRecord.idDelegaStorico,  [sort:"validoAl", order:"desc"])?.toDTO() + Delega.findById(selectedRecord.idDelegaStorico)?.toDTO();
		}
		else {
			storico = Delega.findAllByIdDelegaStorico(selectedRecord.id, [sort:"validoAl", order:"desc"])?.toDTO() + Delega.findById(selectedRecord.id)?.toDTO();
		}
		if (storico.size() == 1) {
			storico = null;
		}

	}

	//////////////////////////////////////////
	//				SOGGETTI				//
	//////////////////////////////////////////


	@Command onChangeSoggetto(@ContextParam(ContextType.TRIGGER_EVENT) InputEvent event) {
		if (filtroSoggetti != "" && selectedRecord.assessore == null){
			Messagebox.show("Soggetto non valido")
		}
	}

	@Command
	void onCercaSoggetto () {
		Window w = Executions.createComponents("/commons/popupRicercaSoggetti.zul", self, [id: -1])
		w.onClose { Event event ->
			if (event.data != null) {
				this.selectedRecord.assessore = event.data;
				this.assessore = (this.selectedRecord?.assessore?.cognome?:"") + (this.selectedRecord?.assessore?.nome?(" "+this.selectedRecord?.assessore?.nome):"")
				BindUtils.postNotifyChange(null, null, this, "selectedRecord")
				BindUtils.postNotifyChange(null, null, this, "assessore")
			}
		}
		w.doModal()
	}

	private PagedResultList loadSoggetti () {
		if (filtroSoggetti != "") {
			PagedResultList elencoSoggetti = As4SoggettoCorrente.createCriteria().list( max:pageSize, offset: pageSize * activePage ){
				or {
					ilike ("cognome", 		"%"+filtroSoggetti+"%")
					ilike ("nome", 			"%"+filtroSoggetti+"%")
					ilike ("denominazione", "%"+filtroSoggetti+"%")
				}

				order ("cognome", 	"asc")
				order ("nome", 		"asc")

				fetchMode("utenteAd4", FetchMode.JOIN)
			}
			totalSize = elencoSoggetti.totalCount

			return elencoSoggetti
		}
		else
			return null

	}

	//////////////////////////////////////////
	//				SALVATAGGIO				//
	//////////////////////////////////////////

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		boolean isNuovaDelega = (selectedRecord.id == null)
		def idDelega = delegaDTOService.salva(selectedRecord).id
		selectedRecord = caricaDelegaDto(idDelega)
		if (isNuovaDelega){
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
		Messagebox.show("Modificare la validità della delega?", "Modifica validità",
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						// prima di attivare una delega occorre verificare che questa non sia una delega storicizzata, se è storicizzata si può attivare soltanto l'ultima
						if (valido && Delega.findByIdDelegaStorico(super.getSelectedRecord()?.id) != null) {
							throw new AttiRuntimeException(Labels.getLabel("label.delega.errore_abilitazione"))
						}
						else if (valido && super.getSelectedRecord()?.idDelegaStorico > 0 && ! super.getSelectedRecord().id?.equals(Delega.findAllByIdDelegaStorico(super.getSelectedRecord().idDelegaStorico,  [sort:"validoAl", order:"desc"]).first()?.id)) {
							throw new AttiRuntimeException(Labels.getLabel("label.delega.errore_abilitazione"))
						}

						super.getSelectedRecord().valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, DelegaDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, DelegaDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, DelegaDettaglioViewModel.this, "datiModifica")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)
	}

	@NotifyChange(["selectedRecord", "modificabile"])
	@Command onModifica() {
		modificabile = true;
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica", "modificabile"])
	@Command onStoricizza () {
		selectedRecord = new DelegaDTO(valido:true, sequenza:1, idDelegaStorico: selectedRecord.id, assessore: selectedRecord.assessore, descrizioneAssessorato: selectedRecord.descrizioneAssessorato)
		modificabile = true
		storico = null
	}
}
