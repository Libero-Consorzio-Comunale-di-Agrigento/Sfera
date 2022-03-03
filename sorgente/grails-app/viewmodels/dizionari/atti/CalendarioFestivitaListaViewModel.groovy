package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dizionari.CalendarioFestivita
import it.finmatica.atti.dto.dizionari.CalendarioFestivitaDTO
import it.finmatica.atti.dizionari.CalendarioFestivitaService

import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Window

class CalendarioFestivitaListaViewModel extends AfcAbstractGrid {

	// services
	CalendarioFestivitaService calendarioFestivitaService

	// componenti
	Window self

	// dati
	List<CalendarioFestivitaDTO> 	listaCalendarioFestivita
    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w
        caricaListaCalendarioFestivita()
    }

	@NotifyChange(["listaCalendarioFestivita", "totalSize"])
	private void caricaListaCalendarioFestivita() {
		PagedResultList lista = CalendarioFestivita.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) eq ("valido",true)
			if (filtro!= null && filtro != ""){
				or{
					eq("giorno", Integer.parseInt(filtro))
                    eq("mese", Integer.parseInt(filtro))
					ilike("anno", "%" + filtro + "%")
					ilike("descrizione", "%" + filtro + "%")
				}
			}
			order ('descrizione', 'asc')
		}
		totalSize  = lista.totalCount
		listaCalendarioFestivita = lista.toDTO()
	}

	@NotifyChange(["listaCalendarioFestivita", "totalSize"])
	@Command onPagina() {
		caricaListaCalendarioFestivita()
	}

	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Window w = Executions.createComponents ("/dizionari/atti/calendarioFestivitaDettaglio.zul", self, [id: (isNuovoRecord?null:selectedRecord.id)])
		w.onClose {
			caricaListaCalendarioFestivita()
			BindUtils.postNotifyChange(null, null, this, "listaCalendarioFestivita")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
		}
		w.doModal()
	}

	@NotifyChange(["listaCalendarioFestivita", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onRefresh () {
		filtro = null
		selectedRecord = null
		activePage = 0
		caricaListaCalendarioFestivita()
	}

	@NotifyChange(["listaCalendarioFestivita", "totalSize", "selectedRecord"])
	@Command onElimina () {
		calendarioFestivitaService.elimina(selectedRecord)
		selectedRecord = null
		caricaListaCalendarioFestivita()
	}

	@NotifyChange(["visualizzaTutti", "listaCalendarioFestivita", "totalSize", "selectedRecord", "activePage"])
	@Command onVisualizzaTutti() {
		visualizzaTutti = !visualizzaTutti
		selectedRecord = null
		activePage = 0
		caricaListaCalendarioFestivita()
	}

	@NotifyChange(["listaCalendarioFestivita", "totalSize", "selectedRecord", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		selectedRecord = null
		activePage = 0
		caricaListaCalendarioFestivita()
	}

	@NotifyChange(["listaCalendarioFestivita", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onCancelFiltro() {
		onRefresh()
	}

}
