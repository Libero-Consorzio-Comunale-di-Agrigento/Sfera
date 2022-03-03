package dizionari.odg

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dto.odg.dizionari.ImportoGettoneDTO
import it.finmatica.atti.dto.odg.dizionari.ImportoGettoneDTOService
import it.finmatica.atti.odg.dizionari.ImportoGettone
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class ImportoGettoneListaViewModel extends AfcAbstractGrid{

	// services
	ImportoGettoneDTOService	importoGettoneDTOService

	// componenti
	Window self

	// dati
	List<ImportoGettoneDTO> 	listaImportoGettone



    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w
		caricaListaImportoGettone()
    }

	@NotifyChange(["listaImportoGettone", "totalSize"])
	private void caricaListaImportoGettone(String filterCondition = filtro) {
		PagedResultList lista = ImportoGettone.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) {
				eq ("valido",true);

			}
			commissione{
				if(filterCondition?:"" != "" ){
					ilike("titolo","%${filterCondition}%")
				}
				order ("titolo", "asc")
			}
			order("validoDal","desc")


			fetchMode("commissione", FetchMode.JOIN)

		}
		totalSize  = lista.totalCount
		listaImportoGettone = lista.toDTO()

//		for (i in listaImportoGettone){
//			i.importoStampa = stampaImportoCorretto(i.importo)
//		}
	}

	private String stampaImportoCorretto(BigDecimal importo){
		String importoTesto = importo.toString().replace(".",",")
		def array = importoTesto.split(",")
		if(array.size() == 1) importoTesto = importoTesto + ",00"
		else if (array[1].length() == 0) importoTesto = array[0] + ",00"
		else if(array[1].length() == 1)importoTesto = array[0] + ","+ array[1].charAt(0) + "0"
		else if(array[1].length() >= 2)	importoTesto = array[0] + ","+ array[1].charAt(0) + array[1].charAt(1)
		return importoTesto
	}

	@NotifyChange(["listaImportoGettone", "totalSize"])
	@Command onPagina() {
		caricaListaImportoGettone()
	}


	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Long idImportoGettone = isNuovoRecord? null: selectedRecord.id
		Window w = Executions.createComponents ("/dizionari/odg/importoGettoneDettaglio.zul", self, [id: idImportoGettone])
		w.onClose {
			activePage 	= 0
			caricaListaImportoGettone()
			BindUtils.postNotifyChange(null, null, this, "listaImportoGettone")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
			BindUtils.postNotifyChange(null, null, this, "activePage")
		}
		w.doModal()
	}

	@NotifyChange(["listaImportoGettone", "totalSize", "selectedRecord", "activePage"])
	@Command onRefresh () {
		activePage = 0
		caricaListaImportoGettone()
		selectedRecord = null
	}

	//@NotifyChange(["listaImportoGettone", "totalSize", "selectedRecord"])
	@Command onElimina () {
		Messagebox.show(Labels.getLabel("dizionario.cancellaRecordMessageBoxTesto"), Labels.getLabel("dizionario.cancellaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.EXCLAMATION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						//se è l'ultimo della pagina di visualizzazione decremento di uno la activePage
						if(listaImportoGettone.size() == 1){
							ImportoGettoneListaViewModel.this.activePage= ImportoGettoneListaViewModel.this.activePage==0?0:ImportoGettoneListaViewModel.this.activePage-1
						}
						importoGettoneDTOService.elimina(ImportoGettoneListaViewModel.this.selectedRecord)
						ImportoGettoneListaViewModel.this.selectedRecord = null
						ImportoGettoneListaViewModel.this.caricaListaImportoGettone()
						BindUtils.postNotifyChange(null, null, ImportoGettoneListaViewModel.this, "activePage")
						BindUtils.postNotifyChange(null, null, ImportoGettoneListaViewModel.this, "listaImportoGettone")
						BindUtils.postNotifyChange(null, null, ImportoGettoneListaViewModel.this, "totalSize")
						BindUtils.postNotifyChange(null, null, ImportoGettoneListaViewModel.this, "selectedRecord")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)

	}

	@NotifyChange(["listaImportoGettone", "totalSize", "activePage","visualizzaTutti"])
	@Command onVisualizzaTutti() {
		activePage = 0
		visualizzaTutti = !visualizzaTutti
		caricaListaImportoGettone()
	}

	@NotifyChange(["listaImportoGettone", "totalSize", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		activePage = 0
//		Passa l'evento generato su onChanging del textbox filtro e ricarica i dati
		if(event instanceof InputEvent){
			caricaListaImportoGettone(event.value)
		}
		else{
			caricaListaImportoGettone()
		}
	}

	@NotifyChange(["listaImportoGettone", "totalSize", "filtro", "activePage"])
	@Command onCancelFiltro() {
		activePage = 0
		filtro = ""
		caricaListaImportoGettone()
	}
}
