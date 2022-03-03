package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dizionari.IndirizzoDelibera
import it.finmatica.atti.dto.dizionari.IndirizzoDeliberaDTO
import it.finmatica.atti.dto.dizionari.IndirizzoDeliberaDTOService
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class IndirizzoDeliberaListaViewModel extends AfcAbstractGrid{

	// services
	IndirizzoDeliberaDTOService	indirizzoDeliberaDTOService

	// componenti
	Window self

	// dati
	List<IndirizzoDeliberaDTO> 	listaIndirizzoDelibera



    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w
		caricaListaIndirizzoDelibera()
    }

	@NotifyChange(["listaIndirizzoDelibera", "totalSize"])
	private void caricaListaIndirizzoDelibera(String filterCondition = filtro) {
		PagedResultList lista = IndirizzoDelibera.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) eq ("valido",true)
			if(filterCondition?:"" != "" ) {
				ilike("titolo","%${filterCondition}%")
				ilike("descrizione","%${filterCondition}%")
			}
			order ("titolo", "asc")
			order ("descrizione","asc")
		}
		totalSize  = lista.totalCount
		listaIndirizzoDelibera = lista.toDTO()
	}

	@NotifyChange(["listaIndirizzoDelibera", "totalSize"])
	@Command onPagina() {
		caricaListaIndirizzoDelibera()
	}


	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Long idIndirizzoDelibera = isNuovoRecord? null: selectedRecord.id
		Window w = Executions.createComponents ("/dizionari/atti/indirizzoDeliberaDettaglio.zul", self, [id: idIndirizzoDelibera])
		w.onClose {
			activePage 	= 0
			caricaListaIndirizzoDelibera()
			BindUtils.postNotifyChange(null, null, this, "listaIndirizzoDelibera")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
			BindUtils.postNotifyChange(null, null, this, "activePage")
		}
		w.doModal()
	}

	@NotifyChange(["listaIndirizzoDelibera", "totalSize", "selectedRecord", "activePage"])
	@Command onRefresh () {
		activePage = 0
		caricaListaIndirizzoDelibera()
		selectedRecord = null
	}

	//@NotifyChange(["listaIndirizzoDelibera", "totalSize", "selectedRecord"])
	@Command onElimina () {
		Messagebox.show(Labels.getLabel("dizionario.cancellaRecordMessageBoxTesto"), Labels.getLabel("dizionario.cancellaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.EXCLAMATION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						//se è l'ultimo della pagina di visualizzazione decremento di uno la activePage
						if(listaIndirizzoDelibera.size() == 1){
							IndirizzoDeliberaListaViewModel.this.activePage= IndirizzoDeliberaListaViewModel.this.activePage==0?0:IndirizzoDeliberaListaViewModel.this.activePage-1
						}
						indirizzoDeliberaDTOService.elimina(IndirizzoDeliberaListaViewModel.this.selectedRecord)
						IndirizzoDeliberaListaViewModel.this.selectedRecord = null
						IndirizzoDeliberaListaViewModel.this.caricaListaIndirizzoDelibera()
						BindUtils.postNotifyChange(null, null, IndirizzoDeliberaListaViewModel.this, "activePage")
						BindUtils.postNotifyChange(null, null, IndirizzoDeliberaListaViewModel.this, "listaIndirizzoDelibera")
						BindUtils.postNotifyChange(null, null, IndirizzoDeliberaListaViewModel.this, "totalSize")
						BindUtils.postNotifyChange(null, null, IndirizzoDeliberaListaViewModel.this, "selectedRecord")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)

	}

	@NotifyChange(["listaIndirizzoDelibera", "totalSize", "activePage","visualizzaTutti"])
	@Command onVisualizzaTutti() {
		activePage = 0
		visualizzaTutti = !visualizzaTutti
		caricaListaIndirizzoDelibera()
	}

	@NotifyChange(["listaIndirizzoDelibera", "totalSize", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		activePage = 0
//		Passa l'evento generato su onChanging del textbox filtro e ricarica i dati
		if(event instanceof InputEvent){
			caricaListaIndirizzoDelibera(event.value)
		}
		else{
			caricaListaIndirizzoDelibera()
		}
	}

	@NotifyChange(["listaIndirizzoDelibera", "totalSize", "filtro", "activePage"])
	@Command onCancelFiltro() {
		activePage = 0
		filtro = ""
		caricaListaIndirizzoDelibera()
	}



}
