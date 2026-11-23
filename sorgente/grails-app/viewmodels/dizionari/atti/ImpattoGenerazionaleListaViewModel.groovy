package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dizionari.ImpattoGenerazionale
import it.finmatica.atti.dto.dizionari.ImpattoGenerazionaleDTO
import it.finmatica.atti.dto.dizionari.ImpattoGenerazionaleDTOService
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class ImpattoGenerazionaleListaViewModel extends AfcAbstractGrid{

	// services
	ImpattoGenerazionaleDTOService	impattoGenerazionaleDTOService

	// componenti
	Window self

	// dati
	List<ImpattoGenerazionaleDTO> 	listaImpattoGenerazionale



    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w
		caricaListaImpattoGenerazionale()
    }

	@NotifyChange(["listaImpattoGenerazionale", "totalSize"])
	private void caricaListaImpattoGenerazionale(String filterCondition = filtro) {
		PagedResultList lista = ImpattoGenerazionale.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) eq ("valido",true)
			if(filterCondition?:"" != "" ) {
				or {
					ilike("titolo","%${filterCondition}%")
					ilike("descrizione","%${filterCondition}%")
				}
			}
			order ("titolo", "asc")
			order ("descrizione", "asc")
		}
		totalSize  = lista.totalCount
		listaImpattoGenerazionale = lista.toDTO()
	}

	@NotifyChange(["listaImpattoGenerazionale", "totalSize"])
	@Command onPagina() {
		caricaListaImpattoGenerazionale()
	}


	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Long idImpattoGenerazionale = isNuovoRecord? null: selectedRecord.id
		Window w = Executions.createComponents ("/dizionari/atti/impattoGenerazionaleDettaglio.zul", self, [id: idImpattoGenerazionale])
		w.onClose {
			activePage 	= 0
			caricaListaImpattoGenerazionale()
			BindUtils.postNotifyChange(null, null, this, "listaImpattoGenerazionale")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
			BindUtils.postNotifyChange(null, null, this, "activePage")
		}
		w.doModal()
	}

	@NotifyChange(["listaImpattoGenerazionale", "totalSize", "selectedRecord", "activePage"])
	@Command onRefresh () {
		activePage = 0
		caricaListaImpattoGenerazionale()
		selectedRecord = null
	}

	@Command onElimina () {
		Messagebox.show(Labels.getLabel("dizionario.cancellaRecordMessageBoxTesto"), Labels.getLabel("dizionario.cancellaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.EXCLAMATION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						//se è l'ultimo della pagina di visualizzazione decremento di uno la activePage
						if(listaImpattoGenerazionale.size() == 1){
							ImpattoGenerazionaleListaViewModel.this.activePage= ImpattoGenerazionaleListaViewModel.this.activePage==0?0:ImpattoGenerazionaleListaViewModel.this.activePage-1
						}
						ImpattoGenerazionaleDTOService.elimina(ImpattoGenerazionaleListaViewModel.this.selectedRecord)
						ImpattoGenerazionaleListaViewModel.this.selectedRecord = null
						ImpattoGenerazionaleListaViewModel.this.caricaListaImpattoGenerazionale()
						BindUtils.postNotifyChange(null, null, ImpattoGenerazionaleListaViewModel.this, "activePage")
						BindUtils.postNotifyChange(null, null, ImpattoGenerazionaleListaViewModel.this, "listaImpattoGenerazionale")
						BindUtils.postNotifyChange(null, null, ImpattoGenerazionaleListaViewModel.this, "totalSize")
						BindUtils.postNotifyChange(null, null, ImpattoGenerazionaleListaViewModel.this, "selectedRecord")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)

	}

	@NotifyChange(["listaImpattoGenerazionale", "totalSize", "activePage","visualizzaTutti"])
	@Command onVisualizzaTutti() {
		activePage = 0
		visualizzaTutti = !visualizzaTutti
		caricaListaImpattoGenerazionale()
	}

	@NotifyChange(["listaImpattoGenerazionale", "totalSize", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		activePage = 0
//		Passa l'evento generato su onChanging del textbox filtro e ricarica i dati
		if(event instanceof InputEvent){
			caricaListaImpattoGenerazionale(event.value)
		}
		else{
			caricaListaImpattoGenerazionale()
		}
	}

	@NotifyChange(["listaImpattoGenerazionale", "totalSize", "filtro", "activePage"])
	@Command onCancelFiltro() {
		activePage = 0
		filtro = ""
		caricaListaImpattoGenerazionale()
	}



}
