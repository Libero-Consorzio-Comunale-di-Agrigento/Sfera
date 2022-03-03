package dizionari.impostazioni

import afc.AfcAbstractGrid
import it.finmatica.atti.admin.FunzioniAvanzateService
import it.finmatica.atti.dto.documenti.viste.RicercaUnitaDocumentoAttivoDTO
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class CambioUnitaDettaglioViewModel {

	// Services
	FunzioniAvanzateService funzioniAvanzateService

	// Componenti
	Window 				self

	// Dati
	So4UnitaPubbDTO		unitaSo4Vecchia
	List<So4UnitaPubbDTO> 	listaUnita
	So4UnitaPubbDTO	 	unitaSo4Nuova
    List<RicercaUnitaDocumentoAttivoDTO> 				listaDocumenti

	// Paginazione
	int pageSize 	= AfcAbstractGrid.PAGE_SIZE_DEFAULT
	int activePage 	= 0
	int	totalSize	= 0

	// Filtro in ricerca
	String 			filtro

    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("listaDocumenti") List<RicercaUnitaDocumentoAttivoDTO> listaDocumenti, @ExecutionArgParam("unitaSo4Vecchia") So4UnitaPubbDTO unitaSo4Vecchia) {
		this.self 				= w
		this.unitaSo4Vecchia 	= unitaSo4Vecchia
		this.listaDocumenti 	= listaDocumenti
		caricaListaUnita()
    }

	@NotifyChange(["listaUnita", "totalSize"])
	private void caricaListaUnita() {
		listaUnita = So4UnitaPubb.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if (filtro != null){
				or {
					ilike("descrizione", "%" + filtro + "%")
					ilike("codice", "%" + filtro + "%")
				}
			}
			eq("ottica.codice",Impostazioni.OTTICA_SO4.valore.toString())
			isNull("al")
			order("descrizione", "asc")
		}
		totalSize  = listaUnita.totalCount
        listaUnita = listaUnita.toDTO()
	}

	@NotifyChange(["listaUnita", "totalSize"])
	@Command onPagina() {
		caricaListaUnita()
	}

	@NotifyChange(["listaUnita", "totalSize", "unitaSo4Nuova", "activePage", "filtro"])
	@Command onRefresh () {
		filtro = null
		unitaSo4Nuova = null
		activePage = 0
		caricaListaUnita()
	}

	@NotifyChange(["listaUnita", "totalSize", "unitaSo4Nuova", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		unitaSo4Nuova = null
		activePage = 0
		caricaListaUnita()
	}

	//////////////////////////////////////////
	//				SALVATAGGIO				//
	//////////////////////////////////////////

	@Command onSalvaUnitaSelezionata () {
		Messagebox.show("Modificare i riferimenti all'unità \n\n $unitaSo4Vecchia.descrizione \n\n in riferimenti all'unità \n\n $unitaSo4Nuova.descrizione?", "Modifica validità",
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						funzioniAvanzateService.cambiaUnitaDocumenti (listaDocumenti, unitaSo4Vecchia, unitaSo4Nuova)
						onChiudi()
					}
				}
			}
		)
	}

	@Command onChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
