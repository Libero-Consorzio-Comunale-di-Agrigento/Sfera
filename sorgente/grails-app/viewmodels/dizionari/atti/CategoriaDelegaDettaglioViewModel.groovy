package dizionari.atti

import afc.AfcAbstractRecord
import grails.orm.PagedResultList
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.dizionari.CategoriaDelega
import it.finmatica.atti.dizionari.CategoriaDelegaTipologia
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.tipologie.TipoDelibera
import it.finmatica.atti.dto.dizionari.CategoriaDelegaDTOService
import it.finmatica.atti.dto.dizionari.CategoriaDelegaDTO
import it.finmatica.atti.dto.dizionari.CategoriaDelegaTipologiaDTOService
import it.finmatica.atti.dto.documenti.tipologie.TipoDeliberaDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class CategoriaDelegaDettaglioViewModel extends AfcAbstractRecord {

	String 		filtroSoggetti
	boolean 	modificabile = false
	List<TipoDelibera> listaTipologieCollegate = new ArrayList<TipoDelibera>()
	String titolo
	String descrizione

	int pageSize 	= 10
	int activePage 	= 0
	int	totalSize	= 0

	// services
	CategoriaDelegaDTOService categoriaDelegaDTOService
	CategoriaDelegaTipologiaDTOService categoriaDelegaTipologiaDTOService


	@NotifyChange(["selectedRecord", "totalSize"])
    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w

		// Inizializo le variabili di classe
		activePage = 0
		filtroSoggetti = ""

	//	soggettiList = loadSoggetti().toDTO()

		if (id != null) {
			selectedRecord = caricaCategoriaDelegaDto(id)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
			titolo = (selectedRecord?.titolo?selectedRecord.titolo:"")
			descrizione = (selectedRecord?.descrizione?selectedRecord.descrizione:"")
			refreshTipologie(id)

		} else {
			selectedRecord = new CategoriaDelegaDTO(valido:true)
			titolo=""
			descrizione=""
			modificabile = true
		}
    }

	private void refreshTipologie(Long id){
		listaTipologieCollegate.clear()
		List<CategoriaDelegaTipologia> listaCategorie = CategoriaDelegaTipologia.findAllByIdCategoriaDelega(id)
		for(CategoriaDelegaTipologia categoriaDelegaTipologia in listaCategorie){
			if(categoriaDelegaTipologia.idTipoDelibera!=null){
				TipoDelibera tipoDelibera= TipoDelibera.get(categoriaDelegaTipologia.idTipoDelibera)
				listaTipologieCollegate.add(tipoDelibera)
			}
		}
		BindUtils.postNotifyChange(null, null, this, "listaTipologieCollegate")
	}

	private CategoriaDelegaDTO caricaCategoriaDelegaDto (Long idCategoriaDelega){
		CategoriaDelega categoriaDelega = CategoriaDelega.createCriteria().get {
			eq("id", idCategoriaDelega)
		}
		return categoriaDelega.toDTO()
	}

	@Command onAggiungiTipologiaDelibera() {
		Window w = Executions.createComponents("/commons/popupCambiaTipologia.zul", self, [documento: "propostaDelibera"])
		w.onClose { event ->
			if (event.data != null) {
				TipoDeliberaDTO tipoDeliberaDTO = event.data

				CategoriaDelegaTipologia check = CategoriaDelegaTipologia.findByIdCategoriaDelegaAndIdTipoDelibera(selectedRecord.id,tipoDeliberaDTO.id)
				if(!check) {
					CategoriaDelegaTipologia categoriaDelegaTipologia = new CategoriaDelegaTipologia()
					categoriaDelegaTipologia.idCategoriaDelega = selectedRecord.id
					categoriaDelegaTipologia.idTipoDelibera = tipoDeliberaDTO.id
					categoriaDelegaTipologiaDTOService.salva(categoriaDelegaTipologia.toDTO())
					refreshTipologie(selectedRecord.id)
					BindUtils.postNotifyChange(null, null, this, "listaTipologieCollegate")
				}
			}
		}
		w.doModal()
	}

	@Command onEliminaTipologiaDelibera(@BindingParam("tipoDelibera") TipoDelibera tipologia) {
		org.zkoss.zhtml.Messagebox.show("Vuoi cancellare la tipologia collegata ?", "Attenzione",
				org.zkoss.zhtml.Messagebox.OK | org.zkoss.zhtml.Messagebox.CANCEL, org.zkoss.zhtml.Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					public void onEvent(Event e) {
						if (org.zkoss.zhtml.Messagebox.ON_OK.equals(e.getName())) {

							CategoriaDelegaTipologia temp = CategoriaDelegaTipologia.findByIdCategoriaDelegaAndIdTipoDelibera(selectedRecord.id,tipologia.id)
							categoriaDelegaTipologiaDTOService.elimina(temp.toDTO())
							refreshTipologie(selectedRecord.id)
							BindUtils.postNotifyChange(null, null, this, "listaTipologieCollegate")
						}
					}
				});
	}

	//////////////////////////////////////////
	//				SALVATAGGIO				//
	//////////////////////////////////////////

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		boolean isNuovaCategoriaDelega = (selectedRecord.id == null)
		def idCategoriaDelega = categoriaDelegaDTOService.salva(selectedRecord).id
		selectedRecord = caricaCategoriaDelegaDto(idCategoriaDelega)
		if (isNuovaCategoriaDelega){
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
		Messagebox.show("Modificare la validità della categoria delega?", "Modifica validità",
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						// prima di attivare una delega occorre verificare che questa non sia una delega storicizzata, se è storicizzata si può attivare soltanto l'ultima

						super.getSelectedRecord().valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, CategoriaDelegaDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, CategoriaDelegaDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, CategoriaDelegaDettaglioViewModel.this, "datiModifica")
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

}
