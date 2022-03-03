package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dizionari.Categoria
import it.finmatica.atti.dto.dizionari.CategoriaDTO
import it.finmatica.atti.dto.dizionari.CategoriaDTOService
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class CategoriaPropostaDeliberaListaViewModel extends AfcAbstractGrid{

	// services
	CategoriaDTOService	categoriaDTOService

	// componenti
	Window self

	// dati
	List<CategoriaDTO> 	listaCategoria



    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w
		caricaListaCategoria()
    }

	@NotifyChange(["listaCategoria", "totalSize"])
	private void caricaListaCategoria(String filterCondition = filtro) {
		PagedResultList lista = Categoria.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) eq ("valido",true)
			if(filterCondition?:"" != "" ) ilike("codice","%${filterCondition}%")
			eq("tipoOggetto", Categoria.TIPO_OGGETTO_PROPOSTA_DELIBERA)
			order ("sequenza", "asc")
			order ("codice","asc")
		}
		totalSize  = lista.totalCount
		listaCategoria = lista.toDTO()
	}

	@NotifyChange(["listaCategoria", "totalSize"])
	@Command onPagina() {
		caricaListaCategoria()
	}


	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Long idCategoria = isNuovoRecord? null: selectedRecord.id
		Window w = Executions.createComponents ("/dizionari/atti/categoriaPropostaDeliberaDettaglio.zul", self, [id: idCategoria])
		w.onClose {
			activePage 	= 0
			caricaListaCategoria()
			BindUtils.postNotifyChange(null, null, this, "listaCategoria")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
			BindUtils.postNotifyChange(null, null, this, "activePage")
		}
		w.doModal()
	}

	@NotifyChange(["listaCategoria", "totalSize", "selectedRecord", "activePage"])
	@Command onRefresh () {
		activePage = 0
		caricaListaCategoria()
		selectedRecord = null
	}

	//@NotifyChange(["listaCategoria", "totalSize", "selectedRecord"])
	@Command onElimina () {
		Messagebox.show(Labels.getLabel("dizionario.cancellaRecordMessageBoxTesto"), Labels.getLabel("dizionario.cancellaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.EXCLAMATION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						//se è l'ultimo della pagina di visualizzazione decremento di uno la activePage
						if(listaCategoria.size() == 1){
							CategoriaPropostaDeliberaListaViewModel.this.activePage= CategoriaPropostaDeliberaListaViewModel.this.activePage==0?0:CategoriaPropostaDeliberaListaViewModel.this.activePage-1
						}
						categoriaDTOService.elimina(CategoriaPropostaDeliberaListaViewModel.this.selectedRecord)
						CategoriaPropostaDeliberaListaViewModel.this.selectedRecord = null
						CategoriaPropostaDeliberaListaViewModel.this.caricaListaCategoria()
						BindUtils.postNotifyChange(null, null, CategoriaPropostaDeliberaListaViewModel.this, "activePage")
						BindUtils.postNotifyChange(null, null, CategoriaPropostaDeliberaListaViewModel.this, "listaCategoria")
						BindUtils.postNotifyChange(null, null, CategoriaPropostaDeliberaListaViewModel.this, "totalSize")
						BindUtils.postNotifyChange(null, null, CategoriaPropostaDeliberaListaViewModel.this, "selectedRecord")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)

	}

	@NotifyChange(["listaCategoria", "totalSize", "activePage","visualizzaTutti"])
	@Command onVisualizzaTutti() {
		activePage = 0
		visualizzaTutti = !visualizzaTutti
		caricaListaCategoria()
	}

	@NotifyChange(["listaCategoria", "totalSize", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		activePage = 0
//		Passa l'evento generato su onChanging del textbox filtro e ricarica i dati
		if(event instanceof InputEvent){
			caricaListaCategoria(event.value)
		}
		else{
			caricaListaCategoria()
		}
	}

	@NotifyChange(["listaCategoria", "totalSize", "filtro", "activePage"])
	@Command onCancelFiltro() {
		activePage = 0
		filtro = ""
		caricaListaCategoria()
	}



}
