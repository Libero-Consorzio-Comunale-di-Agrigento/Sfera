package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dizionari.Email
import it.finmatica.atti.dto.dizionari.EmailDTO
import it.finmatica.atti.dto.dizionari.EmailDTOService
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Window

class EmailListaViewModel extends AfcAbstractGrid {

	// service
	EmailDTOService emailDTOService

	// componenti
	Window self

	// dati
	List<EmailDTO> listaEmail

    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w
		caricaListaEmail()
    }

	@NotifyChange(["listaEmail", "totalSize"])
	private void caricaListaEmail() {
		PagedResultList lista = Email.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) eq ("valido",true)
			if (filtro != null){
				or{
					ilike("ragioneSociale", "%" + filtro + "%")
					ilike("cognome", "%" + filtro + "%")
					ilike("nome", "%" + filtro + "%")
				}
			}
			order('ragioneSociale', 'asc')
			order('cognome', 		'asc')
			order('nome', 			'asc')
		}
		totalSize  = lista.totalCount
		listaEmail = lista.toDTO()
	}

	@NotifyChange(["listaEmail", "totalSize"])
	@Command onPagina() {
		caricaListaEmail()
	}

	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Window w = Executions.createComponents ("/dizionari/atti/emailDettaglio.zul", self, [id: (isNuovoRecord?null:selectedRecord.id)])
		w.onClose {
			caricaListaEmail()
			BindUtils.postNotifyChange(null, null, this, "listaEmail")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
		}
		w.doModal()
	}

	@NotifyChange(["listaEmail", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onRefresh () {
		filtro = null
		selectedRecord = null
		activePage = 0
		caricaListaEmail()
	}

	@NotifyChange(["listaEmail", "totalSize", "selectedRecord"])
	@Command onElimina () {
		emailDTOService.elimina(selectedRecord)
		selectedRecord = null
		caricaListaEmail()
	}

	@NotifyChange(["visualizzaTutti", "listaEmail", "totalSize", "selectedRecord", "activePage"])
	@Command onVisualizzaTutti() {
		visualizzaTutti = !visualizzaTutti
		selectedRecord = null
		activePage = 0
		caricaListaEmail()
	}

	@NotifyChange(["listaEmail", "totalSize", "selectedRecord", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		selectedRecord = null
		activePage = 0
		caricaListaEmail()
	}

	@NotifyChange(["listaEmail", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onCancelFiltro() {
		onRefresh()
	}

}
