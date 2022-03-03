package gestionetesti.ui.funzionalita

import afc.AfcAbstractGrid
import com.github.sardine.Sardine
import grails.orm.PagedResultList
import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.gestionetesti.lock.GestioneTestiDettaglioLock
import it.finmatica.gestionetesti.lock.dto.GestioneTestiDettaglioLockDTO
import org.apache.http.HttpStatus
import org.hibernate.FetchMode
import org.zkoss.bind.annotation.*
import org.zkoss.zul.Window

class LockDocumentiListaViewModel {

	// Paginazione
	int pageSize 	= AfcAbstractGrid.PAGE_SIZE_DEFAULT
	int activePage 	= 0
	int	totalSize	= 0

	// componenti
	Window self

	// dati
	GestioneTestiDettaglioLockDTO			selectedRecord
	List<GestioneTestiDettaglioLockDTO>		listaTestiLock = []

	// services
	GestioneTestiService  gestioneTestiService
	SpringSecurityService springSecurityService

	@Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		activePage 	= 0
		totalSize	= 0
		this.self = w
		caricaListaLock()
	}

	@NotifyChange(["listaTestiLock", "totalSize"])
	private void caricaListaLock() {
		PagedResultList lista = GestioneTestiDettaglioLock.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			isNull("dataFineLock")
			order ("dataInizioLock", "asc")
			fetchMode("utenteInizioLock", FetchMode.JOIN)
		}
		totalSize  = lista.totalCount
		listaTestiLock = lista.toDTO()
    }


	@NotifyChange(["listaTestiLock", "totalSize"])
	@Command onPagina() {
		caricaListaLock()
	}

	@NotifyChange(["listaTestiLock", "selectedRecord", "activePage", "totalSize"])
	@Command onRefresh () {
		selectedRecord = null
		activePage = 0
		caricaListaLock()
	}

	@NotifyChange(["listaTestiLock", "selectedRecord", "totalSize"])
	@Command onUnlock () {
		// sblocco il testo con l'utente che lo ha lockato.
		try {
			gestioneTestiService.unlock(selectedRecord.lock.idRiferimentoTesto, selectedRecord.utenteInizioLock.domainObject)
		} catch (RuntimeException e) {
			// se ricevo errore di testo non trovato, significa che il file non è presente su webdav, allora procedo con l'unlock normale.
			if (e.cause instanceof Sardine && e.cause.getStatusCode() == HttpStatus.SC_NOT_FOUND) {
				gestioneTestiService.eliminaLock(selectedRecord.lock.idRiferimentoTesto, selectedRecord.utenteInizioLock.domainObject)
			} else {
				throw e
			}
		}
		selectedRecord = null
		caricaListaLock()
	}

}
