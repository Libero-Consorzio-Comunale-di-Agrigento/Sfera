package atti

import grails.orm.PagedResultList
import it.finmatica.atti.documenti.OrganoControlloNotifica
import it.finmatica.atti.dto.documenti.OrganoControlloNotificaDTO
import it.finmatica.atti.dto.documenti.OrganoControlloNotificaDocumentoDTO
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zul.Listitem
import org.zkoss.zul.Window

class NotificheOrganiDiControlloViewModel {

	// services

	// componenti
	Window self

	// ricerca
	String testoCerca  = ""

	// paginazione
	int activePage  = 0
	int pageSize 	= 30
	int totalSize 	= 100

	// dati
	List<OrganoControlloNotificaDTO> 	listaNotifiche
	OrganoControlloNotificaDTO			selected

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
		Window w = Executions.createComponents("/commons/popupNotificheOrganiDiControllo.zul", self, [id:-1])
		w.doModal()
		w.onClose {
			caricaLista()
		}
	}

	private void caricaLista() {
		PagedResultList lista = OrganoControlloNotifica.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			order ("dateCreated", "desc")
			fetchMode("tipoOrganoControllo", 	FetchMode.JOIN)
			fetchMode("tipoRegistro", 			FetchMode.JOIN)
		}

		totalSize  = lista.totalCount
		listaNotifiche = lista?.toDTO()

		BindUtils.postNotifyChange(null, null, this, "listaNotifiche")
		BindUtils.postNotifyChange(null, null, this, "totalSize")
		BindUtils.postNotifyChange(null, null, this, "activePage")
	}

	@Command onLink (@BindingParam("oggetto") OrganoControlloNotificaDocumentoDTO oggetto) {
		if (oggetto.delibera) {
			Executions.createComponents("/atti/documenti/delibera.zul",  self, [id : oggetto.delibera.id]).doModal();
		} else if (oggetto.determina) {
			Executions.createComponents("/atti/documenti/determina.zul", self, [id : oggetto.determina.id]).doModal();
		}
	}
	@Command onModifica (@ContextParam(ContextType.COMPONENT) Listitem l) {
		Window w = Executions.createComponents("/commons/popupNotificheOrganiDiControllo.zul", self, [id:l.value.id])
		w.doModal()
		w.onClose {
			caricaLista()
		}
	}
}
