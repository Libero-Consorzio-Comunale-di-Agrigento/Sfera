package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.documenti.tipologie.TipoCertificato
import it.finmatica.atti.dto.dizionari.TipoCertificatoDTOService
import it.finmatica.atti.dto.documenti.tipologie.TipoCertificatoDTO
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class TipoCertificatoListaViewModel extends AfcAbstractGrid{

	// services
	TipoCertificatoDTOService	tipoCertificatoDTOService

	// componenti
	Window self

	// dati
	List<TipoCertificatoDTO> 	listaTipoCertificato

    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w
		caricaListaTipoCertificato()
    }

	@NotifyChange(["listaTipoCertificato", "totalSize"])
	private void caricaListaTipoCertificato(String filterCondition = filtro) {
		PagedResultList lista = TipoCertificato.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) eq ("valido",true)
			if(filterCondition?:"" != "" ) {
				or{
					ilike("titolo","%${filterCondition}%")
					ilike("descrizione","%${filterCondition}%")
				}
			}
			order ("titolo", "asc")
			order ("descrizione","asc")
		}
		totalSize  = lista.totalCount
		listaTipoCertificato = lista.toDTO()
	}

	@NotifyChange(["listaTipoCertificato", "totalSize"])
	@Command onPagina() {
		caricaListaTipoCertificato()
	}

	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Long idTipoCertificato = isNuovoRecord? null: selectedRecord.id
		Window w = Executions.createComponents ("/dizionari/atti/tipoCertificatoDettaglio.zul", self, [id: idTipoCertificato])
		w.onClose {
			activePage 	= 0
			caricaListaTipoCertificato()
			BindUtils.postNotifyChange(null, null, this, "listaTipoCertificato")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
			BindUtils.postNotifyChange(null, null, this, "activePage")
		}
		w.doModal()
	}

	@NotifyChange(["listaTipoCertificato", "totalSize", "selectedRecord", "activePage"])
	@Command onRefresh () {
		activePage = 0
		caricaListaTipoCertificato()
		selectedRecord = null
	}

	//@NotifyChange(["listaTipoCertificato", "totalSize", "selectedRecord"])
	@Command onElimina () {
		Messagebox.show(Labels.getLabel("dizionario.cancellaRecordMessageBoxTesto"), Labels.getLabel("dizionario.cancellaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.EXCLAMATION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						//se è l'ultimo della pagina di visualizzazione decremento di uno la activePage
						if (listaTipoCertificato.size() == 1) {
							TipoCertificatoListaViewModel.this.activePage= TipoCertificatoListaViewModel.this.activePage==0?0:TipoCertificatoListaViewModel.this.activePage-1
						}

						tipoCertificatoDTOService.elimina(TipoCertificatoListaViewModel.this.selectedRecord)
						TipoCertificatoListaViewModel.this.selectedRecord = null
						TipoCertificatoListaViewModel.this.caricaListaTipoCertificato()
						BindUtils.postNotifyChange(null, null, TipoCertificatoListaViewModel.this, "activePage")
						BindUtils.postNotifyChange(null, null, TipoCertificatoListaViewModel.this, "listaTipoCertificato")
						BindUtils.postNotifyChange(null, null, TipoCertificatoListaViewModel.this, "totalSize")
						BindUtils.postNotifyChange(null, null, TipoCertificatoListaViewModel.this, "selectedRecord")
					}
				}
			}
		)
	}

	@NotifyChange(["listaTipoCertificato", "totalSize", "activePage","visualizzaTutti"])
	@Command onVisualizzaTutti() {
		activePage = 0
		visualizzaTutti = !visualizzaTutti
		caricaListaTipoCertificato()
	}

	@NotifyChange(["listaTipoCertificato", "totalSize", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		activePage = 0

		// Passa l'evento generato su onChanging del textbox filtro e ricarica i dati
		if (event instanceof InputEvent) {
			caricaListaTipoCertificato(event.value)
		} else {
			caricaListaTipoCertificato()
		}
	}

	@NotifyChange(["listaTipoCertificato", "totalSize", "filtro", "activePage"])
	@Command onCancelFiltro() {
		activePage = 0
		filtro = ""
		caricaListaTipoCertificato()
	}
}
