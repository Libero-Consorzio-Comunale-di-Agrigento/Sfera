package dizionari.odg

import grails.orm.PagedResultList
import it.finmatica.atti.dto.odg.dizionari.*
import it.finmatica.atti.odg.dizionari.OrganoControlloComponente
import it.finmatica.atti.odg.dizionari.OrganoControlloRuolo
import it.finmatica.atti.odg.dizionari.TipoOrganoControllo
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class OrganoControlloDettaglioViewModel {

	// service
	OrganoControlloRuoloDTOService organoControlloRuoloDTOService
	OrganoControlloComponenteDTOService organoControlloComponenteDTOService

	// dati
	OrganoControlloDTO 				organoControllo
	List<OrganoControlloRuoloDTO> 	listaRuoli
	OrganoControlloRuoloDTO 		selectedRuolo
	List<TipoOrganoControlloDTO> 	listaTipi

	// componenti
	Window self

	// stato
	int pageSize 	= 10
	int activePage	= 0
	int totalSize	= 0

	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("organoControllo") OrganoControlloDTO organoControllo)  {
		this.organoControllo = organoControllo
		listaTipi = TipoOrganoControllo.list().toDTO()

		if (organoControllo.id>0)
			caricaListaRuoli()
	}

	@NotifyChange("listaRuoli")
	private void caricaListaRuoli() {
		PagedResultList lista = OrganoControlloRuolo.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
			eq ("organoControllo.id",organoControllo.id)
			order ("descrizione","asc")
		}

		totalSize = lista.totalCount
		listaRuoli = lista?.toDTO()

		BindUtils.postNotifyChange(null, null, this, "listaRuoli")
		BindUtils.postNotifyChange(null, null, this, "totalSize")
		BindUtils.postNotifyChange(null, null, this, "activePage")
	}

	@Command onPagina() {
		caricaListaRuoli()
	}

	@Command onModifica(@BindingParam("ruolo") OrganoControlloRuoloDTO ruolo) {
		boolean isNuovoRecord = (ruolo == null)

		if (organoControllo.id>0)  {
			Window w = Executions.createComponents("/dizionari/odg/organoControlloRuoloDettaglio.zul",self,[id: ((isNuovoRecord)?null:ruolo.id), organoControllo: organoControllo])

			w.onClose {
				caricaListaRuoli()
				BindUtils.postNotifyChange(null,null, this, "listaRuoli")
				BindUtils.postNotifyChange(null,null, this, "totalsize")
			}
			w.doModal()
		}
		else
		 Messagebox.show("Impossibile aggiungere un ruolo.\nBisogna prima salvare l'Organo di Controllo","Attenzione!", null, Messagebox.INFORMATION, null)
	}

	@NotifyChange(["listaRuoli","totalSize"])
	@Command onEliminaRuolo (@BindingParam("ruolo") OrganoControlloRuoloDTO ruolo) {
		if (ruolo.valido) {
			ruolo.valido = false;
			organoControlloRuoloDTOService.salva(ruolo, organoControllo)
			selectedRuolo=null

			List<OrganoControlloComponente>  listaComponenti = OrganoControlloComponente.createCriteria().list() {
				eq ("organoControllo.id", organoControllo.id)
				eq ("organoControlloRuolo.id", ruolo.id)
				fetchMode("organoControllo", FetchMode.JOIN)
				fetchMode("organoControlloRuolo", FetchMode.JOIN)
				fetchMode("componente", FetchMode.JOIN)
			}?.toDTO()

			listaComponenti.each { it->
				if (it.valido) {
					it.valido = false;
					organoControlloComponenteDTOService.salva(it, organoControllo);
				}
			}

			caricaListaRuoli();
		}
	}
}