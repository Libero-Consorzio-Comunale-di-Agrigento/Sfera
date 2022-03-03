package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dizionari.RegistroUnita
import it.finmatica.atti.dto.dizionari.RegistroUnitaDTO
import it.finmatica.atti.dto.dizionari.RegistroUnitaDTOService
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class RegistroUnitaListaViewModel extends AfcAbstractGrid  {
	// services
	RegistroUnitaDTOService	registroUnitaDTOService

	// componenti
	Window self

	// dati
	List<RegistroUnitaDTO> 	listaRegistroUnitaDto

	@Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w
		caricaListaRegistroUnita()
	}

	@NotifyChange(["listaRegistroUnitaDto", "totalSize"])
	private void caricaListaRegistroUnita(String filterCondition = filtro) {
		PagedResultList lista = RegistroUnita.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) eq ("valido",true)
			if(filterCondition?:"" != "" ){
				or{
					tipoRegistro{
						ilike("descrizione","%${filterCondition}%")
					}
					unitaSo4{
						ilike("descrizione","%${filterCondition}%")
					}
				}
			}
			tipoRegistro{
				order ("descrizione", "asc")
			}
			unitaSo4{
				order ("descrizione", "asc")
			}
			fetchMode("tipoRegistro", FetchMode.JOIN)
			fetchMode("unitaSo4", FetchMode.JOIN)
			fetchMode("caratteristica", FetchMode.JOIN)
		}
		totalSize  = lista.totalCount
		listaRegistroUnitaDto = lista.toDTO()
	}

	@NotifyChange(["listaRegistroUnitaDto", "totalSize"])
	@Command onPagina() {
		caricaListaRegistroUnita()
	}

	@NotifyChange(["listaRegistroUnitaDto", "totalSize", "selectedRecord", "activePage"])
	@Command onRefresh () {
		activePage = 0
		caricaListaRegistroUnita()
		selectedRecord = null
	}


	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Long idRegUnita = isNuovoRecord? null :selectedRecord.id
		Window w = Executions.createComponents ("/dizionari/atti/registroUnitaDettaglio.zul", self, [id: idRegUnita])
		w.onClose {
			activePage 	= 0
			caricaListaRegistroUnita()
			BindUtils.postNotifyChange(null, null, this, "listaRegistroUnitaDto")
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
						if(listaRegistroUnitaDto.size() == 1){
							RegistroUnitaListaViewModel.this.activePage= RegistroUnitaListaViewModel.this.activePage==0?0:RegistroUnitaListaViewModel.this.activePage-1
						}
						registroUnitaDTOService.elimina(RegistroUnitaListaViewModel.this.selectedRecord)
						RegistroUnitaListaViewModel.this.selectedRecord = null
						RegistroUnitaListaViewModel.this.caricaListaRegistroUnita()
						BindUtils.postNotifyChange(null, null, RegistroUnitaListaViewModel.this, "activePage")
						BindUtils.postNotifyChange(null, null, RegistroUnitaListaViewModel.this, "listaRegistroUnitaDto")
						BindUtils.postNotifyChange(null, null, RegistroUnitaListaViewModel.this, "totalSize")
						BindUtils.postNotifyChange(null, null, RegistroUnitaListaViewModel.this, "selectedRecord")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)

	}

	@NotifyChange(["listaRegistroUnitaDto", "totalSize", "activePage","visualizzaTutti"])
	@Command onVisualizzaTutti() {
		activePage = 0
		visualizzaTutti = !visualizzaTutti
		caricaListaRegistroUnita()
	}

	@NotifyChange(["listaRegistroUnitaDto", "totalSize", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		activePage = 0
//		Passa l'evento generato su onChanging del textbox filtro e ricarica i dati
		if(event instanceof InputEvent){
			caricaListaRegistroUnita(event.value)
		}
		else{
			caricaListaRegistroUnita()
		}

	}


	@NotifyChange(["listaRegistroUnitaDto", "totalSize", "filtro", "activePage"])
	@Command onCancelFiltro() {
		activePage = 0
		filtro = null
		caricaListaRegistroUnita()
	}


}
