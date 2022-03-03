package dizionari.odg

import grails.orm.PagedResultList
import it.finmatica.atti.dto.odg.dizionari.OrganoControlloComponenteDTO
import it.finmatica.atti.dto.odg.dizionari.OrganoControlloComponenteDTOService
import it.finmatica.atti.dto.odg.dizionari.OrganoControlloDTO
import it.finmatica.atti.odg.dizionari.OrganoControlloComponente
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zul.Window

class OrganoControlloComponenteListaViewModel {

	// service
	OrganoControlloComponenteDTOService organoControlloComponenteDTOService

	// componenti
	Window self

	// dati
	OrganoControlloDTO 					organoControllo
	List<OrganoControlloComponenteDTO>  listaComponenti
	OrganoControlloComponenteDTO 		selectedComponente

	// stato
	int pageSize 	= 10
	int activePage	= 0
	int totalSize	= 0

    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("organoControllo") OrganoControlloDTO organoControllo)  {
        this.self = w
		this.organoControllo = organoControllo
		caricaListaComponenti()
    }

	@Command onPaging () {
		caricaListaComponenti();

		BindUtils.postNotifyChange(null,null, this, "listaComponenti")
		BindUtils.postNotifyChange(null,null, this, "totalsize")
	}

	@NotifyChange(["listaComponenti","totalSize"])
	private void caricaListaComponenti() {
		PagedResultList lista = OrganoControlloComponente.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			createAlias("componente", 		"componente")

			eq ("organoControllo.id",		organoControllo.id)

			order ("componente.cognome",	"asc")
			order ("componente.nome",		"asc")

			fetchMode("organoControllo", 		FetchMode.JOIN)
			fetchMode("organoControlloRuolo", 	FetchMode.JOIN)
			fetchMode("componente", 			FetchMode.JOIN)
		}

		totalSize 		= lista.totalCount
		listaComponenti = lista.toDTO()
	}

	@Command onModifica(@BindingParam("componente") OrganoControlloComponenteDTO componente) {
		boolean isNuovoRecord = (componente == null)
		Window w = Executions.createComponents("/dizionari/odg/organoControlloComponenteDettaglio.zul",self,[id: ((isNuovoRecord)?null:componente.id), organoControllo: organoControllo])
		w.onClose {
			caricaListaComponenti()
			BindUtils.postNotifyChange(null,null, this, "listaComponenti")
			BindUtils.postNotifyChange(null,null, this, "totalsize")
		}
		w.doModal()
	}

	@NotifyChange(["listaComponenti","totalSize"])
	@Command onEliminaComponente (@BindingParam("componente") OrganoControlloComponenteDTO componente) {
		if (componente.valido) {
			componente.valido = false
			selectedComponente = null
			organoControlloComponenteDTOService.salva(componente, organoControllo)
			caricaListaComponenti()
		}
	}
}
