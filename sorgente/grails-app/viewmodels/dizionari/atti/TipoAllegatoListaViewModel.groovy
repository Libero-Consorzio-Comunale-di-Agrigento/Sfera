package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dizionari.TipoAllegato
import it.finmatica.atti.dto.dizionari.TipoAllegatoDTO
import it.finmatica.atti.dto.dizionari.TipoAllegatoDTOService
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.documenti.AllegatiObbligatori
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class TipoAllegatoListaViewModel extends AfcAbstractGrid{

	// services
	TipoAllegatoDTOService	tipoAllegatoDTOService

	// componenti
	Window self

	// dati
	List<TipoAllegatoDTO> 	listaTipoAllegato



    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w
		caricaListaTipoAllegato()
    }

	@NotifyChange(["listaTipoAllegato", "totalSize"])
	private void caricaListaTipoAllegato(String filterCondition = filtro) {
		PagedResultList lista = TipoAllegato.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) eq ("valido",true)
			if(filterCondition?:"" != "" ) ilike("titolo","%${filterCondition}%")
			order ("titolo", "asc")
			order ("descrizione","asc")
		}
		totalSize  = lista.totalCount
		listaTipoAllegato = lista.toDTO()
	}

	@NotifyChange(["listaTipoAllegato", "totalSize"])
	@Command onPagina() {
		caricaListaTipoAllegato()
	}


	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Long idTipoAllegato = isNuovoRecord? null: selectedRecord.id
		Window w = Executions.createComponents ("/dizionari/atti/tipoAllegatoDettaglio.zul", self, [id: idTipoAllegato])
		w.onClose {
			activePage 	= 0
			caricaListaTipoAllegato()
			BindUtils.postNotifyChange(null, null, this, "listaTipoAllegato")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
			BindUtils.postNotifyChange(null, null, this, "activePage")
		}
		w.doModal()
	}

	@NotifyChange(["listaTipoAllegato", "totalSize", "selectedRecord", "activePage"])
	@Command onRefresh () {
		activePage = 0
		caricaListaTipoAllegato()
		selectedRecord = null
	}

	//@NotifyChange(["listaTipoAllegato", "totalSize", "selectedRecord"])
	@Command onElimina () {
		if (MappingIntegrazione.countByCategoriaAndValoreEsterno(AllegatiObbligatori.MAPPING_CATEGORIA, selectedRecord.id.toString())> 0){
			throw new AttiRuntimeException("Tipo Allegato utilizzato come allegato obbligatorio da una Tipologia o da una Categoria, operazione annullata!")
		}
		Messagebox.show(Labels.getLabel("dizionario.cancellaRecordMessageBoxTesto"), Labels.getLabel("dizionario.cancellaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.EXCLAMATION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						//se è l'ultimo della pagina di visualizzazione decremento di uno la activePage
						if(listaTipoAllegato.size() == 1){
							TipoAllegatoListaViewModel.this.activePage= TipoAllegatoListaViewModel.this.activePage==0?0:TipoAllegatoListaViewModel.this.activePage-1
						}
						tipoAllegatoDTOService.elimina(TipoAllegatoListaViewModel.this.selectedRecord)
						TipoAllegatoListaViewModel.this.selectedRecord = null
						TipoAllegatoListaViewModel.this.caricaListaTipoAllegato()
						BindUtils.postNotifyChange(null, null, TipoAllegatoListaViewModel.this, "activePage")
						BindUtils.postNotifyChange(null, null, TipoAllegatoListaViewModel.this, "listaTipoAllegato")
						BindUtils.postNotifyChange(null, null, TipoAllegatoListaViewModel.this, "totalSize")
						BindUtils.postNotifyChange(null, null, TipoAllegatoListaViewModel.this, "selectedRecord")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)

	}

	@NotifyChange(["listaTipoAllegato", "totalSize", "activePage","visualizzaTutti"])
	@Command onVisualizzaTutti() {
		activePage = 0
		visualizzaTutti = !visualizzaTutti
		caricaListaTipoAllegato()
	}

	@NotifyChange(["listaTipoAllegato", "totalSize", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		activePage = 0
//		Passa l'evento generato su onChanging del textbox filtro e ricarica i dati
		if(event instanceof InputEvent){
			caricaListaTipoAllegato(event.value)
		}
		else{
			caricaListaTipoAllegato()
		}
	}

	@NotifyChange(["listaTipoAllegato", "totalSize", "filtro", "activePage"])
	@Command onCancelFiltro() {
		activePage = 0
		filtro = ""
		caricaListaTipoAllegato()
	}



}
