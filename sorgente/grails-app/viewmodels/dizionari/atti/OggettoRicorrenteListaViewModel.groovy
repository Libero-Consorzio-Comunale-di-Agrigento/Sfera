package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dizionari.OggettoRicorrente
import it.finmatica.atti.dto.dizionari.OggettoRicorrenteDTO
import it.finmatica.atti.dto.dizionari.OggettoRicorrenteDTOService
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class OggettoRicorrenteListaViewModel extends AfcAbstractGrid{

	// services
	OggettoRicorrenteDTOService	oggettoRicorrenteDTOService

	// componenti
	Window self

	// dati
	List<OggettoRicorrenteDTO> 	listaOggettoRicorrente



    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w
		caricaListaOggettoRicorrente()
    }

	@NotifyChange(["listaOggettoRicorrente", "totalSize"])
	private void caricaListaOggettoRicorrente(String filterCondition = filtro) {
		PagedResultList lista = OggettoRicorrente.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) eq ("valido",true)
			if(filterCondition?:"" != "" ) {
				or {
					ilike("oggetto","%${filterCondition}%")
					ilike("codice","%${filterCondition}%")
				}
			}
			order ("codice", "asc")
			order ("oggetto", "asc")
		}
		totalSize  = lista.totalCount
		listaOggettoRicorrente = lista.toDTO()
	}

	@NotifyChange(["listaOggettoRicorrente", "totalSize"])
	@Command onPagina() {
		caricaListaOggettoRicorrente()
	}


	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Long idOggettoRicorrente = isNuovoRecord? null: selectedRecord.id
		Window w = Executions.createComponents ("/dizionari/atti/oggettoRicorrenteDettaglio.zul", self, [id: idOggettoRicorrente])
		w.onClose {
			activePage 	= 0
			caricaListaOggettoRicorrente()
			BindUtils.postNotifyChange(null, null, this, "listaOggettoRicorrente")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
			BindUtils.postNotifyChange(null, null, this, "activePage")
		}
		w.doModal()
	}

	@NotifyChange(["listaOggettoRicorrente", "totalSize", "selectedRecord", "activePage"])
	@Command onRefresh () {
		activePage = 0
		caricaListaOggettoRicorrente()
		selectedRecord = null
	}

	//@NotifyChange(["listaOggettoRicorrente", "totalSize", "selectedRecord"])
	@Command onElimina () {
		Messagebox.show(Labels.getLabel("dizionario.cancellaRecordMessageBoxTesto"), Labels.getLabel("dizionario.cancellaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.EXCLAMATION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						//se è l'ultimo della pagina di visualizzazione decremento di uno la activePage
						if(listaOggettoRicorrente.size() == 1){
							OggettoRicorrenteListaViewModel.this.activePage= OggettoRicorrenteListaViewModel.this.activePage==0?0:OggettoRicorrenteListaViewModel.this.activePage-1
						}
						oggettoRicorrenteDTOService.elimina(OggettoRicorrenteListaViewModel.this.selectedRecord)
						OggettoRicorrenteListaViewModel.this.selectedRecord = null
						OggettoRicorrenteListaViewModel.this.caricaListaOggettoRicorrente()
						BindUtils.postNotifyChange(null, null, OggettoRicorrenteListaViewModel.this, "activePage")
						BindUtils.postNotifyChange(null, null, OggettoRicorrenteListaViewModel.this, "listaOggettoRicorrente")
						BindUtils.postNotifyChange(null, null, OggettoRicorrenteListaViewModel.this, "totalSize")
						BindUtils.postNotifyChange(null, null, OggettoRicorrenteListaViewModel.this, "selectedRecord")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)

	}

	@NotifyChange(["listaOggettoRicorrente", "totalSize", "activePage","visualizzaTutti"])
	@Command onVisualizzaTutti() {
		activePage = 0
		visualizzaTutti = !visualizzaTutti
		caricaListaOggettoRicorrente()
	}

	@NotifyChange(["listaOggettoRicorrente", "totalSize", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		activePage = 0
//		Passa l'evento generato su onChanging del textbox filtro e ricarica i dati
		if(event instanceof InputEvent){
			caricaListaOggettoRicorrente(event.value)
		}
		else{
			caricaListaOggettoRicorrente()
		}
	}

	@NotifyChange(["listaOggettoRicorrente", "totalSize", "filtro", "activePage"])
	@Command onCancelFiltro() {
		activePage = 0
		filtro = ""
		caricaListaOggettoRicorrente()
	}



}
