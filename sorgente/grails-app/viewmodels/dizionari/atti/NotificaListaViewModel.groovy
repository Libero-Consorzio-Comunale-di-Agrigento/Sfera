package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dizionari.Notifica
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.dto.dizionari.NotificaDTO
import it.finmatica.atti.dto.dizionari.NotificaDTOService
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Window

class NotificaListaViewModel extends AfcAbstractGrid {

	// service
	NotificaDTOService notificaDTOService

	// componenti
	Window self

	// dati
	List<NotificaDTO> listaNotifica

	@Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w
		caricaListaNotifica()
	}

	@NotifyChange(["listaNotifica", "totalSize"])
	private void caricaListaNotifica() {
		PagedResultList lista = Notifica.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if (!visualizzaTutti)
				eq ("valido", true)

			if (filtro != null) {
				ilike("oggetto", "%" + filtro + "%")
			}

			// Mostro solo le notifiche gestibili da interfaccia:
			'in' ("tipoNotifica", TipoNotifica.lista.codice)

			fetchMode("commissione", FetchMode.JOIN)
			order ('oggetto', 'asc')
		}
		totalSize  		= lista.totalCount
		listaNotifica 	= lista.toDTO()
	}

	@NotifyChange(["listaNotifica", "totalSize"])
	@Command onPagina() {
		caricaListaNotifica()
	}

	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Window w = Executions.createComponents ("/dizionari/atti/notificaDettaglio.zul", self, [id: (isNuovoRecord?null:selectedRecord.id)])
		w.onClose {
			caricaListaNotifica()
			BindUtils.postNotifyChange(null, null, this, "listaNotifica")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
		}
		w.doModal()
	}

	@NotifyChange(["listaNotifica", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onRefresh () {
		filtro = null
		selectedRecord = null
		activePage = 0
		caricaListaNotifica()
	}

	@NotifyChange(["listaNotifica", "totalSize", "selectedRecord"])
	@Command onElimina () {
		notificaDTOService.elimina(selectedRecord)
		selectedRecord = null
		caricaListaNotifica()
	}

	@NotifyChange(["visualizzaTutti", "listaNotifica", "totalSize", "selectedRecord", "activePage"])
	@Command onVisualizzaTutti() {
		visualizzaTutti = !visualizzaTutti
		selectedRecord = null
		activePage = 0
		caricaListaNotifica()
	}

	@NotifyChange(["listaNotifica", "totalSize", "selectedRecord", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		selectedRecord = null
		activePage = 0
		caricaListaNotifica()
	}

	@NotifyChange(["listaNotifica", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onCancelFiltro() {
		onRefresh()
	}

}
