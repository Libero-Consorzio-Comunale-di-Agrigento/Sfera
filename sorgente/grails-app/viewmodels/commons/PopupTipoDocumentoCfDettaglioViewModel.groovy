package commons

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.cf.integrazione.IAttiIntegrazioneServiceCf
import it.finmatica.atti.documenti.tipologie.TipoDocumentoCf

import it.finmatica.atti.dto.documenti.tipologie.TipoDocumentoCfDTO
import it.finmatica.atti.dto.documenti.tipologie.TipoDocumentoCfDTOService
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupTipoDocumentoCfDettaglioViewModel {

	// service
	TipoDocumentoCfDTOService 	tipoDocumentoCfDTOService
	IAttiIntegrazioneServiceCf	attiCfIntegrazioneService
	SpringSecurityService 		springSecurityService

	// dati
	TipoDocumentoCfDTO selectedRecord
	Window self

	int pageSize 	= 10
	int activePage 	= 0
	int	totalSize	= 0

	List<it.finmatica.atti.cf.integrazione.TipoDocumentoCf> listaTipoDocumentoCf
	List<TipoDocumentoCf> listaTipoDoc

	@NotifyChange(["selectedRecord", "listaTipoDocumentoCf", "valoreTipiDocumento"])
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("documento") def doc, @ExecutionArgParam("tipoDocumento") String tipoDoc, @ExecutionArgParam("lista") List<TipoDocumentoCfDTO> listaTipiDocumentoCf) {
		this.self = w

		if (tipoDoc == "tipoDetermina") {
			selectedRecord = new TipoDocumentoCfDTO()
			selectedRecord.tipoDetermina = doc
		} else if(tipoDoc == "tipoDelibera") {
			selectedRecord = new TipoDocumentoCfDTO()
			selectedRecord.tipoDelibera = doc
		}

		listaTipoDoc = listaTipiDocumentoCf?:[]

		// inizializzo la lista della combobox
		listaTipoDocumentoCf = caricaListaTipoDocumentoCf()
	}

	// metodi per il calcolo delle combobox
	private List<it.finmatica.atti.cf.integrazione.TipoDocumentoCf> caricaListaTipoDocumentoCf() {
        List<String> codiciDaEscludere = listaTipoDoc*.cfTipoDocumentoCodice
		return attiCfIntegrazioneService.getTipiDocumento(springSecurityService.principal.amministrazione.codice).findAll { !codiciDaEscludere.contains(it.codice) }.sort { it.titolo }
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onInserisci () {
		if (selectedRecord != null) {
			selectedRecord = tipoDocumentoCfDTOService.salva(selectedRecord)
			onChiudi()
		}
	}

	@Command onChiudi() {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
