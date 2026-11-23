package atti.documenti

import it.finmatica.atti.dizionari.CategoriaDelega
import it.finmatica.atti.dizionari.CategoriaDelegaTipologia
import it.finmatica.atti.dizionari.Delega
import it.finmatica.atti.dto.dizionari.DelegaDTO
import it.finmatica.atti.dto.documenti.PropostaDeliberaDTO
import org.hibernate.criterion.CriteriaSpecification
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.ExecutionArgParam
import org.zkoss.bind.annotation.Init
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zul.Window

class PopupSceltaDelegaViewModel {

	// componenti
	Window self

	// dati
	List<DelegaDTO> listaDeleghe
	DelegaDTO selectedRecord
	PropostaDeliberaDTO propostaDeliberaDTO

	String filtro=""

	// il pulsante "Togli Delega" ha senso solo quando la popup viene usata per gestire la delega principale (proposta di delibera / oggetto seduta),
	// non quando viene usata per aggiungere una delega "aggiuntiva"
	boolean mostraTogliDelega = true

	@Init init(@ContextParam(ContextType.COMPONENT) Window w,
			   @ExecutionArgParam("propostaDelibera") PropostaDeliberaDTO propostaDeliberaDto,
			   @ExecutionArgParam("mostraTogliDelega") Boolean mostraTogliDelegaParam) {
		this.self = w
		if ( propostaDeliberaDto!=null) {
			propostaDeliberaDTO=propostaDeliberaDto
		}
		if (mostraTogliDelegaParam != null) {
			mostraTogliDelega = mostraTogliDelegaParam
		}
		caricaListaSoggetti ()
	}

	private void caricaListaSoggetti (String filterCondition = filtro) {
		boolean checkCategorieCollegate = false

		List<Long> categorieDelega = new ArrayList<Long>()
		//qui devo inserire i soggetti da far vedere a seconda dei casi
		// 1 se la tipologia della proposta è in qualche categoria allora faccio vedere solo i soggetti che hanno quelle categorie
		// 2 se la tipologia non è in nessuna categoria mi comporto come ora

		if(propostaDeliberaDTO!=null){
			List<CategoriaDelegaTipologia> categoriaDelegaTipologiaList = CategoriaDelegaTipologia.findAllByIdTipoDelibera(propostaDeliberaDTO.tipologia.id)
			checkCategorieCollegate=(categoriaDelegaTipologiaList.size()>0?true:false)
			for (CategoriaDelegaTipologia categoriaDelegaTipologia in categoriaDelegaTipologiaList){
				CategoriaDelega categoriaDelega = CategoriaDelega.get(categoriaDelegaTipologia.idCategoriaDelega)
				if(categoriaDelega.valido) {
					categorieDelega.add(categoriaDelegaTipologia.idCategoriaDelega)
				}
			}
		}

		if(checkCategorieCollegate) {
			listaDeleghe = Delega.createCriteria().list() {
				createAlias ("assessore", 		"asse", 	CriteriaSpecification.INNER_JOIN)
				eq("valido", true)
				or {
					ilike("descrizioneAssessorato", "%"+filterCondition+"%")
					ilike("asse.denominazione", "%"+filterCondition+"%")
				}
				inList("idCategoriaDelega", categorieDelega.toArray())
				order("sequenza", "asc")
				order("descrizioneAssessorato", "asc")

			}.toDTO()
		} else {
			listaDeleghe = Delega.createCriteria().list() {
				createAlias ("assessore", 		"asse", 	CriteriaSpecification.INNER_JOIN)
				eq("valido", true)
				or {
					ilike("descrizioneAssessorato", "%"+filterCondition+"%")
					ilike("asse.denominazione", "%"+filterCondition+"%")
				}

				order("sequenza", "asc")
				order("descrizioneAssessorato", "asc")

			}.toDTO()
		}
		BindUtils.postNotifyChange(null, null, this, "listaDeleghe")
	}

	@Command onFiltro(@ContextParam(ContextType.TRIGGER_EVENT)Event event) {
		// Passa l'evento generato su onChanging del textbox filtro e ricarica i dati
		if (event instanceof InputEvent) {
			caricaListaSoggetti(event.value)
		} else {
			caricaListaSoggetti()
		}
	}

	@Command onSeleziona() {
		Events.postEvent(Events.ON_CLOSE, self, selectedRecord)
	}

	@Command onTogliDelega() {
		Events.postEvent(Events.ON_CLOSE, self, "eliminaDelega")
	}

	@Command onAnnulla () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
