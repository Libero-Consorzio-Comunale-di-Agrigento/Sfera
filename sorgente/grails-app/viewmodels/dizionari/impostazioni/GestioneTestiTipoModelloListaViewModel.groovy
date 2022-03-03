package dizionari.impostazioni

import afc.AfcAbstractGrid
import it.finmatica.gestionetesti.reporter.GestioneTestiTipoModello
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiTipoModelloDTO
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class GestioneTestiTipoModelloListaViewModel  extends AfcAbstractGrid{

	// services
	def	gestioneTestiTipoModelloDTOService

	// componenti
	Window self

	// dati
	List<GestioneTestiTipoModelloDTO> 	listaGestioneTestiTipoModello



    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w
		caricaListaGestioneTestiTipoModello()
    }

	@NotifyChange(["listaGestioneTestiTipoModello", "totalSize"])
	private void caricaListaGestioneTestiTipoModello(String filterCondition = filtro) {
		def lista = GestioneTestiTipoModello.createCriteria().list() {
			projections{
				property("codice")
				property("descrizione")
				property("valido")
				property("version")
			}
			if(!visualizzaTutti) eq ("valido",true)
			if(filterCondition?:"" != "" ) {
				or{
					ilike("codice","%${filterCondition}%")
					ilike("descrizione","%${filterCondition}%")
				}
			}
			order ("codice", "asc")
			order ("descrizione","asc")
			maxResults(pageSize)
			firstResult(activePage*pageSize)
		}
		totalSize  = GestioneTestiTipoModello.createCriteria().get() {
			projections{
				rowCount()
			}
			if(!visualizzaTutti) eq ("valido",true)
			if(filterCondition?:"" != "" ) {
				or{
					ilike("codice","%${filterCondition}%")
					ilike("descrizione","%${filterCondition}%")
				}
			}
			order ("codice", "asc")
			order ("descrizione","asc")
		}
		List<GestioneTestiTipoModelloDTO>	 listaDto = new ArrayList<GestioneTestiTipoModelloDTO>()
		for(int i; i < lista.size(); i++){
			GestioneTestiTipoModelloDTO elDto = new GestioneTestiTipoModelloDTO()
			elDto.codice = lista[i][0]
			elDto.descrizione = lista[i][1]
			elDto.valido = lista[i][2]
			elDto.version = lista[i][3]
			listaDto.add(elDto)
		}
		listaGestioneTestiTipoModello = listaDto
	}

	@NotifyChange(["listaGestioneTestiTipoModello", "totalSize"])
	@Command onPagina() {
		caricaListaGestioneTestiTipoModello()
	}


	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		String idGestioneTestiTipoModello = isNuovoRecord? null: selectedRecord.codice
		Window w = Executions.createComponents ("/dizionari/impostazioni/gestioneTestiTipoModelloDettaglio.zul", self, [codice: idGestioneTestiTipoModello])
		w.onClose {
			activePage 	= 0
			caricaListaGestioneTestiTipoModello()
			BindUtils.postNotifyChange(null, null, this, "listaGestioneTestiTipoModello")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
			BindUtils.postNotifyChange(null, null, this, "activePage")
		}
		w.doModal()
	}

	@NotifyChange(["listaGestioneTestiTipoModello", "totalSize", "selectedRecord", "activePage"])
	@Command onRefresh () {
		activePage = 0
		caricaListaGestioneTestiTipoModello()
		selectedRecord = null
	}

	//@NotifyChange(["listaGestioneTestiTipoModello", "totalSize", "selectedRecord"])
	@Command onElimina () {
		Messagebox.show(Labels.getLabel("dizionario.cancellaRecordMessageBoxTesto"), Labels.getLabel("dizionario.cancellaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.EXCLAMATION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						//se è l'ultimo della pagina di visualizzazione decremento di uno la activePage
						if(listaGestioneTestiTipoModello.size() == 1){
							GestioneTestiTipoModelloListaViewModel.this.activePage= GestioneTestiTipoModelloListaViewModel.this.activePage==0?0:GestioneTestiTipoModelloListaViewModel.this.activePage-1
						}
						gestioneTestiTipoModelloDTOService.elimina(GestioneTestiTipoModelloListaViewModel.this.selectedRecord)
						GestioneTestiTipoModelloListaViewModel.this.selectedRecord = null
						GestioneTestiTipoModelloListaViewModel.this.caricaListaGestioneTestiTipoModello()
						BindUtils.postNotifyChange(null, null, GestioneTestiTipoModelloListaViewModel.this, "activePage")
						BindUtils.postNotifyChange(null, null, GestioneTestiTipoModelloListaViewModel.this, "listaGestioneTestiTipoModello")
						BindUtils.postNotifyChange(null, null, GestioneTestiTipoModelloListaViewModel.this, "totalSize")
						BindUtils.postNotifyChange(null, null, GestioneTestiTipoModelloListaViewModel.this, "selectedRecord")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)

	}

	@NotifyChange(["listaGestioneTestiTipoModello", "totalSize", "activePage","visualizzaTutti"])
	@Command onVisualizzaTutti() {
		activePage = 0
		visualizzaTutti = !visualizzaTutti
		caricaListaGestioneTestiTipoModello()
	}

	@NotifyChange(["listaGestioneTestiTipoModello", "totalSize", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		activePage = 0
//		Passa l'evento generato su onChanging del textbox filtro e ricarica i dati
		if(event instanceof InputEvent){
			caricaListaGestioneTestiTipoModello(event.value)
		}
		else{
			caricaListaGestioneTestiTipoModello()
		}
	}

	@NotifyChange(["listaGestioneTestiTipoModello", "totalSize", "filtro", "activePage"])
	@Command onCancelFiltro() {
		activePage = 0
		filtro = ""
		caricaListaGestioneTestiTipoModello()
	}



}
