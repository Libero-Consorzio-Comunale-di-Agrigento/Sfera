package dizionari.odg

import grails.orm.PagedResultList
import it.finmatica.atti.dto.odg.dizionari.GruppoPoliticoComponenteDTOService
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import it.finmatica.atti.dto.odg.dizionari.GruppoPoliticoDTO
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class GruppoPoliticoDettaglioViewModel {

	// service
	//OrganoControlloRuoloDTOService organoControlloRuoloDTOService
	GruppoPoliticoComponenteDTOService gruppoPoliticoComponenteDTOService

	// dati
	GruppoPoliticoDTO 				gruppoPolitico

	// componenti
	Window self

	// stato
	int pageSize 	= 10
	int activePage	= 0
	int totalSize	= 0

	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("gruppoPolitico") GruppoPoliticoDTO gruppoPolitico)  {
		this.gruppoPolitico = gruppoPolitico

	}

}