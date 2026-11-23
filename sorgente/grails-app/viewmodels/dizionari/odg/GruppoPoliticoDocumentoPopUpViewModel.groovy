package dizionari.odg

import it.finmatica.atti.dto.documenti.DeliberaDTO
import it.finmatica.atti.dto.documenti.PropostaDeliberaDTO
import it.finmatica.atti.dto.odg.dizionari.GruppoPoliticoComponenteDTO
import it.finmatica.atti.dto.odg.dizionari.GruppoPoliticoDTO
import it.finmatica.atti.odg.dizionari.GruppoPolitico
import it.finmatica.atti.odg.dizionari.GruppoPoliticoComponente
import org.apache.log4j.Logger
import org.hibernate.FetchMode
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class GruppoPoliticoDocumentoPopUpViewModel {

	private static final Logger log = Logger.getLogger(GruppoPoliticoDocumentoPopUpViewModel.class)

	// componenti
	Window self

	// dati
	GruppoPoliticoDTO gruppoPolitico
	List<GruppoPoliticoComponenteDTO>  listaComponentiGruppoPolitico

	// stato
	def documento
	String tipoDocumento
	List<GruppoPoliticoDTO> listaGruppiPolitici
	List<GruppoPoliticoComponenteDTO> listaComponentiSelezionati

	@NotifyChange(["listaGruppiPolitici"])
    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("documento") def documento) {
		this.self = w

		this.documento			= documento

		if(documento instanceof PropostaDeliberaDTO) {
			tipoDocumento = "propostaDelibera"
		} else if(documento instanceof DeliberaDTO) {
			tipoDocumento = "delibera"
		}

		caricaListaGruppiPolitici()
    }

	@NotifyChange(["listaComponentiGruppoPolitico"])
	@Command
	void onCambiaGruppoPolitico () {
		//carico la listaComponentiGruppoPolitico
		if(gruppoPolitico!=null && gruppoPolitico.id>0){
			listaComponentiGruppoPolitico = GruppoPoliticoComponente.createCriteria().list() {
				eq ("gruppoPolitico.id",		gruppoPolitico.id)

				createAlias("componente", "c")

				fetchMode("componente", FetchMode.JOIN)
				fetchMode("gruppoPolitico", FetchMode.JOIN)

				order("c.cognome", "asc")
				order("c.nome", "asc")
			}.toDTO()
		}

	}

	private void caricaListaGruppiPolitici () {
		listaGruppiPolitici= GruppoPolitico.createCriteria().list() {
			eq ("valido", true)
			order("titolo","asc")
		}.toDTO()
	}

	@NotifyChange("listaComponentiSelezionati")
	@Command onSalvaChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, listaComponentiSelezionati)

		onChiudi()
	}

	@Command onChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}