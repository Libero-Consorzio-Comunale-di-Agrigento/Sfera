package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.dizionari.TipoRegistroDTOService
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class TipoRegistroListaViewModel extends AfcAbstractGrid  {
	// services
	TipoRegistroDTOService	tipoRegistroDTOService

	// componenti
	Window self

	// dati
	List<TipoRegistroDTO> 	listaTipoRegistroDto


	@Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w
		caricaListaTipoRegistro()
	}

	@NotifyChange(["listaTipoRegistroDto", "totalSize"])
	private void caricaListaTipoRegistro(String filterCondition = filtro) {
		PagedResultList lista = TipoRegistro.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) eq ("valido",true)
			if(filterCondition?:"" != "" ){
				or{
					 ilike("codice","%${filterCondition}%")
				}
			}
			order ("codice", "asc")
		}
		totalSize  = lista.totalCount
		listaTipoRegistroDto = lista.toDTO()
	}

	@NotifyChange(["listaTipoRegistroDto", "totalSize"])
	@Command onPagina() {
		caricaListaTipoRegistro()
	}

	@NotifyChange(["listaTipoRegistroDto", "totalSize", "selectedRecord", "activePage"])
	@Command onRefresh () {
		activePage = 0
		caricaListaTipoRegistro()
		selectedRecord = null
	}


	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		String codice = isNuovoRecord? null :selectedRecord.codice
		Window w = Executions.createComponents ("/dizionari/atti/tipoRegistroDettaglio.zul", self, [codice: codice])
		w.onClose {
			activePage 	= 0
			caricaListaTipoRegistro()
			BindUtils.postNotifyChange(null, null, this, "listaTipoRegistroDto")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
			BindUtils.postNotifyChange(null, null, this, "activePage")
		}
		w.doModal()
	}


	@Command onElimina () {
		Messagebox.show(Labels.getLabel("dizionario.cancellaRecordMessageBoxTesto"), Labels.getLabel("dizionario.cancellaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.EXCLAMATION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						//se è l'ultimo della pagina di visualizzazione decremento di uno la activePage
						if(listaTipoRegistroDto.size() == 1){
							TipoRegistroListaViewModel.this.activePage= TipoRegistroListaViewModel.this.activePage==0?0:TipoRegistroListaViewModel.this.activePage-1
						}
						tipoRegistroDTOService.elimina(TipoRegistroListaViewModel.this.selectedRecord)
						TipoRegistroListaViewModel.this.selectedRecord = null
						TipoRegistroListaViewModel.this.caricaListaTipoRegistro()
						BindUtils.postNotifyChange(null, null, TipoRegistroListaViewModel.this, "activePage")
						BindUtils.postNotifyChange(null, null, TipoRegistroListaViewModel.this, "listaTipoRegistroDto")
						BindUtils.postNotifyChange(null, null, TipoRegistroListaViewModel.this, "totalSize")
						BindUtils.postNotifyChange(null, null, TipoRegistroListaViewModel.this, "selectedRecord")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)

	}

	@NotifyChange(["listaTipoRegistroDto", "totalSize", "activePage","visualizzaTutti"])
	@Command onVisualizzaTutti() {
		activePage = 0
		visualizzaTutti = !visualizzaTutti
		caricaListaTipoRegistro()
	}

	@NotifyChange(["listaTipoRegistroDto", "totalSize", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		activePage = 0
//		Passa l'evento generato su onChanging del textbox filtro e ricarica i dati
		if(event instanceof InputEvent){
			caricaListaTipoRegistro(event.value)
		}
		else{
			caricaListaTipoRegistro()
		}

	}


	@NotifyChange(["listaTipoRegistroDto", "totalSize", "filtro", "activePage"])
	@Command onCancelFiltro() {
		activePage = 0
		filtro = null
		caricaListaTipoRegistro()
	}


}
