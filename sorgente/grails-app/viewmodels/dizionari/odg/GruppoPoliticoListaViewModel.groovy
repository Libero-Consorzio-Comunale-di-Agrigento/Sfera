package dizionari.odg

import afc.AfcAbstractGrid
import grails.orm.PagedResultList
import it.finmatica.atti.dto.odg.dizionari.GruppoPoliticoDTO
import it.finmatica.atti.dto.odg.dizionari.GruppoPoliticoDTOService
import it.finmatica.atti.odg.dizionari.GruppoPolitico
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Window

class GruppoPoliticoListaViewModel extends AfcAbstractGrid {

 	//service
	GruppoPoliticoDTOService gruppoPoliticoDTOService

	//componenti
	Window self

	//dati
	List <GruppoPoliticoDTO> 	listaGruppoPolitico

	@Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w
		caricaListaGruppiPolitici()
	}

	@NotifyChange(["listaGruppoPolitico", "totalSize"])
	private void caricaListaGruppiPolitici() {
		PagedResultList lista = GruppoPolitico.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			if(!visualizzaTutti) eq ("valido",true)
			if (filtro != null){
				or{
					ilike("titolo", "%" + filtro + "%")
					ilike("descrizione", "%" + filtro + "%")
				}
			}
			order ("sequenza", "asc")
		}
		totalSize  = lista.totalCount
		listaGruppoPolitico = lista?.toDTO()
	}

	@NotifyChange(["listaGruppoPolitico", "totalSize"])
	@Command onPagina() {
		caricaListaGruppiPolitici()
	}

	@Command onModifica (@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
		Window w = Executions.createComponents ("/dizionari/odg/indexGruppoPolitico.zul", self, [id: (isNuovoRecord?null:selectedRecord.id)])
		w.onClose {
			caricaListaGruppiPolitici()
			BindUtils.postNotifyChange(null, null, this, "listaGruppoPolitico")
			BindUtils.postNotifyChange(null, null, this, "totalSize")
		}
		w.doModal()
	}

	@NotifyChange(["listaGruppoPolitico", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onRefresh () {
		filtro = null
		selectedRecord = null
		activePage = 0
		caricaListaGruppiPolitici()
	}

	@NotifyChange(["listaGruppoPolitico", "totalSize", "selectedRecord"])
	@Command onElimina () {
		/*gruppoPoliticoDTOService.elimina(selectedRecord)
		selectedRecord = null
		caricaListaGruppiPolitici()*/
	}

	@NotifyChange(["visualizzaTutti", "listaGruppoPolitico", "totalSize", "selectedRecord", "activePage"])
	@Command onVisualizzaTutti() {
		visualizzaTutti = !visualizzaTutti
		selectedRecord = null
		activePage = 0
		caricaListaGruppiPolitici()
	}

	@NotifyChange(["listaGruppoPolitico", "totalSize", "selectedRecord", "activePage"])
	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		selectedRecord = null
		activePage = 0
		caricaListaGruppiPolitici()
	}

	@NotifyChange(["listaGruppoPolitico", "totalSize", "selectedRecord", "activePage", "filtro"])
	@Command onCancelFiltro() {
		onRefresh()
	}

}
