package dizionari.odg

import grails.orm.PagedResultList
import it.finmatica.atti.dto.odg.dizionari.GruppoPoliticoComponenteDTO
import it.finmatica.atti.dto.odg.dizionari.GruppoPoliticoDTO
import it.finmatica.atti.dto.odg.dizionari.GruppoPoliticoComponenteDTOService
import it.finmatica.atti.odg.dizionari.GruppoPoliticoComponente
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.ListModelList
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class GruppoPoliticoComponenteListaViewModel {

	// service
	GruppoPoliticoComponenteDTOService gruppoPoliticoComponenteDTOService

	// componenti
	Window self

	// dati
	GruppoPoliticoDTO 					gruppoPolitico
	ListModelList<GruppoPoliticoComponenteDTO> listaComponenti = new ListModelList<>();
	GruppoPoliticoComponenteDTO 		selectedComponente

	// stato
	int pageSize 	= 10
	int activePage	= 0
	int totalSize	= 0

    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("gruppoPolitico") GruppoPoliticoDTO gruppoPolitico)  {
        this.self = w
		this.gruppoPolitico = gruppoPolitico
		caricaListaComponenti()
    }

	@Command onPaging () {
		caricaListaComponenti();

		BindUtils.postNotifyChange(null,null, this, "listaComponenti")
		BindUtils.postNotifyChange(null,null, this, "totalsize")
	}

	@NotifyChange(["listaComponenti","totalSize"])
	private void caricaListaComponenti() {
		PagedResultList lista = GruppoPoliticoComponente.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			createAlias("componente", 		"componente")

			eq ("gruppoPolitico.id",		gruppoPolitico.id)

			order("id", "asc")

			fetchMode("gruppoPolitico", 		FetchMode.JOIN)
			fetchMode("componente", 			FetchMode.JOIN)
		}

		totalSize 		= lista.totalCount
		listaComponenti = lista.toDTO()
	}

	@Command onModifica(@BindingParam("componente") GruppoPoliticoComponenteDTO componente) {
		boolean isNuovoRecord = (componente == null)
		Window w = Executions.createComponents("/dizionari/odg/gruppoPoliticoComponenteDettaglio.zul",self,[id: ((isNuovoRecord)?null:componente.id), gruppoPolitico: gruppoPolitico])
		w.onClose {
			caricaListaComponenti()
			BindUtils.postNotifyChange(null,null, this, "listaComponenti")
			BindUtils.postNotifyChange(null,null, this, "totalsize")
		}
		w.doModal()
	}

	@NotifyChange(["listaComponenti","totalSize"])
	@Command onEliminaComponente (@BindingParam("componente") GruppoPoliticoComponenteDTO componente) {

		Messagebox.show("Eliminare il componente?", "Elimina componente gruppo politico",
				Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					public void onEvent(Event e){
						if(Messagebox.ON_OK.equals(e.getName())) {
								selectedComponente = null
								gruppoPoliticoComponenteDTOService.elimina(componente)
								listaComponenti.remove(componente)
							totalSize--
						}
					}
				})
		/*caricaListaComponenti()
		BindUtils.postNotifyChange(null,null, this, "listaComponenti")*/
		BindUtils.postNotifyChange(null,null, this, "totalsize")
	}
}
