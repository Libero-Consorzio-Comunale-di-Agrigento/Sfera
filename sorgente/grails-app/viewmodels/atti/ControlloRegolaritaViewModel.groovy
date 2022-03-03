package atti

import grails.orm.PagedResultList
import it.finmatica.atti.documenti.ControlloRegolarita
import it.finmatica.atti.dto.dizionari.ControlloRegolaritaDTOService
import it.finmatica.atti.dto.documenti.ControlloRegolaritaDTO
import it.finmatica.atti.dto.documenti.ControlloRegolaritaDocumentoDTO
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zul.Listitem
import org.zkoss.zul.Window

class ControlloRegolaritaViewModel {

	// services
	ControlloRegolaritaDTOService controlloRegolaritaDTOService

	// componenti
	Window self

	// ricerca
	String testoCerca  = ""

	// paginazione
	int activePage  = 0
	int pageSize 	= 30
	int totalSize 	= 100

	// dati
	List<ControlloRegolaritaDTO> 	listaDocumenti
	ControlloRegolaritaDTO			selected

	@Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w
		caricaLista()
	}

	@Command onRefresh() {
		caricaLista()
	}

	@Command onCerca() {
		activePage = 0
		caricaLista()
	}

	@Command onCrea() {
		Window w = Executions.createComponents("/commons/popupControlloRegolarita.zul", self, [id:-1])
		w.doModal()
		w.onClose {
			caricaLista()
		}
	}

	private void caricaLista() {
		PagedResultList lista = ControlloRegolarita.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			order ("dateCreated", "desc")
			fetchMode("tipoControlloRegolarita", 	FetchMode.JOIN)
			fetchMode("tipoRegistro", 				FetchMode.JOIN)
		}

		totalSize  = lista.totalCount
		listaDocumenti = lista.toDTO()

		BindUtils.postNotifyChange(null, null, this, "listaDocumenti")
		BindUtils.postNotifyChange(null, null, this, "totalSize")
		BindUtils.postNotifyChange(null, null, this, "activePage")
	}

	@Command onLink (@BindingParam("oggetto") ControlloRegolaritaDocumentoDTO oggetto) {
		if (oggetto.delibera) {
			Executions.createComponents("/atti/documenti/delibera.zul",  self, [id : oggetto.delibera.id]).doModal();
		} else if (oggetto.determina) {
			Executions.createComponents("/atti/documenti/determina.zul", self, [id : oggetto.determina.id]).doModal();
		}
	}
	@Command onModifica (@ContextParam(ContextType.COMPONENT) Listitem l) {
		Window w = Executions.createComponents("/commons/popupControlloRegolarita.zul", self, [id:l.value.id])
		w.doModal()
		w.onClose {
			caricaLista()
		}
	}
}
