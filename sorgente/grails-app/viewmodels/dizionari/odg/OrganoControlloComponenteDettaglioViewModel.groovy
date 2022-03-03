package dizionari.odg

import afc.AfcAbstractRecord
import grails.orm.PagedResultList
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.dto.odg.dizionari.OrganoControlloComponenteDTO
import it.finmatica.atti.dto.odg.dizionari.OrganoControlloComponenteDTOService
import it.finmatica.atti.dto.odg.dizionari.OrganoControlloDTO
import it.finmatica.atti.dto.odg.dizionari.OrganoControlloRuoloDTO
import it.finmatica.atti.odg.dizionari.OrganoControlloComponente
import it.finmatica.atti.odg.dizionari.OrganoControlloRuolo
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zk.ui.event.OpenEvent
import org.zkoss.zk.ui.event.SelectEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class OrganoControlloComponenteDettaglioViewModel extends AfcAbstractRecord {

	// service
	OrganoControlloComponenteDTOService organoControlloComponenteDTOService

	String 		filtroSoggetti
	String 		componente
	String 		ruolo

	List<As4SoggettoCorrenteDTO> soggettiList
	int pageSize 	= 10
	int activePage 	= 0
	int	totalSize	= 0

	// dati
	OrganoControlloRuoloDTO 		organoControlloRuolo
	//OrganoControlloComponenteDTO 	organoControlloComponente
	OrganoControlloDTO 				organoControllo
	List<OrganoControlloRuoloDTO> 	listaRuoli
	//List<As4SoggettoCorrenteDTO> 	listaSoggetti

	@NotifyChange(["selectedRecord", "soggettiList", "totalSize"])
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id, @ExecutionArgParam("organoControllo") def organoControllo)  {
        this.self = w
		this.organoControllo = organoControllo

			// Inizializo le variabili di classe
		activePage = 0
		filtroSoggetti = ""

		soggettiList = loadSoggetti().toDTO()

		listaRuoli = OrganoControlloRuolo.createCriteria().list {
			eq("organoControllo.id",organoControllo.id)
			eq("valido",true)
			order ("descrizione","asc")
		}.toDTO()

		if (id != null) {
			selectedRecord = caricaOrganoControlloComponenteDto(id)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
			componente = (selectedRecord?.componente?.cognome?:"") + (selectedRecord?.componente?.nome?(" "+selectedRecord?.componente?.nome):"")
		} else {
			selectedRecord = new OrganoControlloComponenteDTO(valido:true)
			componente = ""
		}

    }

	private OrganoControlloComponenteDTO caricaOrganoControlloComponenteDto (Long id){
		OrganoControlloComponente organoControlloComponente = OrganoControlloComponente.createCriteria().get {
			eq("id", id)
			fetchMode("organoControllo", 		FetchMode.JOIN)
			fetchMode("organoControlloRuolo", 	FetchMode.JOIN)
			fetchMode("componente", 			FetchMode.JOIN)
		}
		return organoControlloComponente.toDTO()
	}

	//////////////////////////////////////////
	//				SOGGETTI				//
	//////////////////////////////////////////

	@NotifyChange(["soggettiList", "totalSize", "activePage"])
	@Command onChangingSoggetto(@ContextParam(ContextType.TRIGGER_EVENT) InputEvent event) {
		// onChanging può scattare anche subito dopo l'apertura del popup
		if (event.getValue() != componente){
			selectedRecord.componente = null
			activePage = 0
			filtroSoggetti = event.getValue()
			soggettiList = loadSoggetti().toDTO()
		}
	}

	@Command onChangeSoggetto(@ContextParam(ContextType.TRIGGER_EVENT) InputEvent event) {
		if (filtroSoggetti != "" && selectedRecord.componente == null){
			Messagebox.show("Soggetto non valido")
		}
	}

	@NotifyChange(["soggettiList", "totalSize", "activePage"])
	@Command onOpenSoggetto(@ContextParam(ContextType.TRIGGER_EVENT) OpenEvent event) {
		if (event.open){
			activePage = 0
			soggettiList = loadSoggetti().toDTO()
		}
	}

	@NotifyChange(["soggettiList", "totalSize"])
	@Command onPaginaSoggetto() {
		soggettiList = loadSoggetti().toDTO()
	}

	@NotifyChange(["selectedRecord", "componente"])
	@Command onSelectSoggetto(@ContextParam(ContextType.TRIGGER_EVENT)SelectEvent event, @BindingParam("target")Component target) {
		// SOLO se ho selezionato un solo item
		if (event.getSelectedItems()?.size() == 1 ){
			filtroSoggetti = ""
			selectedRecord.componente = event.getSelectedItems().toArray()[0].value
			componente = (selectedRecord?.componente?.cognome?:"") + (selectedRecord?.componente?.nome?(" "+selectedRecord?.componente?.nome):"")
			target?.close()
		}
	}

	private PagedResultList loadSoggetti () {
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

	//////////////////////////////////////////
	//				SALVATAGGIO				//
	//////////////////////////////////////////

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		boolean isNuovoComponente = (selectedRecord.id == null)
		def idOrganoComponente = organoControlloComponenteDTOService.salva(selectedRecord, organoControllo).id
		selectedRecord = caricaOrganoControlloComponenteDto(idOrganoComponente)
		if (isNuovoComponente){
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
		Messagebox.show("Modificare la validità del componente?", "Modifica validità",
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						super.getSelectedRecord().valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, OrganoControlloComponenteDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, OrganoControlloComponenteDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, OrganoControlloComponenteDettaglioViewModel.this, "datiModifica")
					}
				}
			}
		)
	}
}
