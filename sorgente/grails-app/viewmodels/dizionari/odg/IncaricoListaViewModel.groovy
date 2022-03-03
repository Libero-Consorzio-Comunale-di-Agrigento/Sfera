package dizionari.odg

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dto.odg.dizionari.IncaricoDTO
import it.finmatica.atti.odg.dizionari.Incarico
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Window

class IncaricoListaViewModel extends AfcAbstractGrid {

	// componenti
	Window self

	// dati
	List<IncaricoDTO> listaIncarichi

	@Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w
		caricaListaIncarico()
	}

	@NotifyChange(["listaIncarichi","totalSize"])
	private void caricaListaIncarico(){
	        PagedResultList lista = Incarico.createCriteria().list(max: pageSize, offset: pageSize * activePage){
			if (!visualizzaTutti) {
				eq ("valido", true)
			}

			if (filtro != null) {
				or {
					ilike("descrizione",	"%" + filtro + "%")
					ilike("valore", 		"%" + filtro + "%")
				}
			}

			order('titolo',	'asc')
		}

		totalSize  		= lista.totalCount
        listaIncarichi 	= lista.toDTO()
	}

	@NotifyChange(["listaIncarichi", "totalSize"])
	@Command onPagina() {
		caricaListaIncarico()
	}

	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Window w = Executions.createComponents("/dizionari/odg/incaricoDettaglio.zul", self, [id : ((isNuovoRecord)?null:selectedRecord.id), lista: listaIncarichi])

		w.onClose {
			caricaListaIncarico()
			BindUtils.postNotifyChange(null, null, this, "listaIncarichi")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
		}
		w.doModal()
	}

	@NotifyChange(["listaIncarichi", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onRefresh () {
		filtro = null
		selectedRecord = null
		activePage = 0
		caricaListaIncarico()
	}

	@NotifyChange(["listaIncarichi", "totalSize", "selectedRecord"])
	@Command onElimina () {
		selectedRecord.domainObject?.delete()
		selectedRecord = null
		caricaListaIncarico()
	}

	@NotifyChange(["visualizzaTutti", "listaIncarichi", "totalSize", "selectedRecord", "activePage"])
	@Command onVisualizzaTutti() {
		visualizzaTutti = !visualizzaTutti
		selectedRecord = null
		activePage = 0
		caricaListaIncarico()
	}

	@NotifyChange(["listaIncarichi", "totalSize", "selectedRecord", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		selectedRecord = null
		activePage = 0
		caricaListaIncarico()
	}

	@NotifyChange(["listaIncarichi", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onCancelFiltro() {
		onRefresh()
	}

}
