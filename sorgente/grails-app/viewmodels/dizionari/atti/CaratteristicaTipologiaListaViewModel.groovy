package dizionari.atti

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dto.documenti.tipologie.CaratteristicaTipologiaDTOService
import it.finmatica.atti.dto.impostazioni.CaratteristicaTipologiaDTO
import it.finmatica.atti.impostazioni.CaratteristicaTipologia
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Window

class CaratteristicaTipologiaListaViewModel extends AfcAbstractGrid {

	// services
	CaratteristicaTipologiaDTOService	caratteristicaTipologiaDTOService

	// componenti
	Window self

	// dati
	List<CaratteristicaTipologiaDTO> 	listaCaratteristiche

    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w
		caricaListaCaratteristiche()
    }

	@NotifyChange(["listaCaratteristiche", "totalSize"])
	private void caricaListaCaratteristiche() {
		PagedResultList lista = CaratteristicaTipologia.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if (!visualizzaTutti) {
				eq ("valido", true)
			}
			
			tipoOggetto {
				order ("nome", "asc")
			}
			if (filtro != null) {
				ilike("titolo", "%" + filtro + "%")
			}

			order ("titolo", "asc")
			
			fetchMode("tipoOggetto", FetchMode.JOIN)
		}
		totalSize  = lista.totalCount
		listaCaratteristiche = lista.toDTO()
	}

	@NotifyChange(["listaCaratteristiche", "totalSize"])
	@Command onPagina() {
		caricaListaCaratteristiche()
	}

	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Window w = Executions.createComponents ("/dizionari/atti/caratteristicaTipologiaDettaglio.zul", self, [id: (isNuovoRecord?null:selectedRecord.id)])
		w.onClose {
			caricaListaCaratteristiche()
			BindUtils.postNotifyChange(null, null, this, "listaCaratteristiche")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
		}
		w.doModal()
	}

	@NotifyChange(["listaCaratteristiche", "totalSize", "selectedRecord", "activePage"])
	@Command onRefresh () {
		filtro = null
		selectedRecord = null
		activePage = 0
		caricaListaCaratteristiche()
	}

	@NotifyChange(["listaCaratteristiche", "totalSize", "selectedRecord"])
	@Command onElimina () {
		caratteristicaTipologiaDTOService.elimina(selectedRecord)
		selectedRecord = null
		caricaListaCaratteristiche()
	}

	@NotifyChange(["visualizzaTutti", "listaCaratteristiche", "totalSize"])
	@Command onVisualizzaTutti() {
		visualizzaTutti = !visualizzaTutti
		selectedRecord = null
		activePage = 0
		caricaListaCaratteristiche()
	}

	@NotifyChange(["listaCaratteristiche", "totalSize", "selectedRecord", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		selectedRecord = null
		activePage = 0
		caricaListaCaratteristiche()
	}

	@NotifyChange(["listaCaratteristiche", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onCancelFiltro() {
		onRefresh()
	}
}
