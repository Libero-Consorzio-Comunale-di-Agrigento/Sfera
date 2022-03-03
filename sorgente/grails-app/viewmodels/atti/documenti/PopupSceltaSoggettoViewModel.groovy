package atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.impostazioni.CaratteristicaTipologiaService
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.zk.SoggettoDocumento
import it.finmatica.dto.DTO
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.apache.log4j.Logger
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zhtml.Messagebox
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Window

class PopupSceltaSoggettoViewModel {

	public static final Logger log = Logger.getLogger(PopupSceltaSoggettoViewModel.class);

	// service
	SpringSecurityService springSecurityService
	CaratteristicaTipologiaService caratteristicaTipologiaService

	// componenti
	Window self

	// dati
    List<SoggettoDocumento> listaSoggetti
	SoggettoDocumento selectedRecord
	def documentoDto
	Map<String, SoggettoDocumento> soggetti
	long idCaratteristicaTipologia
	String tipoSoggetto

	// stato
	String categoriaSoggetto
	String labelTitolo
	String labelColonna1
	String labelColonna2
	String labelNessunRisultato

	boolean eliminaSoggetto

	@NotifyChange("listaUnita")
	@Init init(@ContextParam(ContextType.COMPONENT) 			    Window w
               , @ExecutionArgParam("idCaratteristicaTipologia") 	long idCaratteristicaTipologia
               , @ExecutionArgParam("documento") 					DTO<?> documentoDto        // la domain class di partenza
               , @ExecutionArgParam("soggetti") 					Map<String, SoggettoDocumento> soggetti
               , @ExecutionArgParam("tipoSoggetto") 				String tipoSoggetto
               , @ExecutionArgParam("categoriaSoggetto") 			String categoriaSoggetto) {
		this.self = w
		this.categoriaSoggetto = categoriaSoggetto
		this.documentoDto = documentoDto
		this.soggetti = soggetti
		this.tipoSoggetto = tipoSoggetto
		this.idCaratteristicaTipologia = idCaratteristicaTipologia

		labelTitolo 		 = Labels.getLabel("atti.documenti.popupSceltaSoggetto.${tipoSoggetto}.titolo")
		labelColonna1 		 = Labels.getLabel("atti.documenti.popupSceltaSoggetto.${tipoSoggetto}.colonna1")
		labelColonna2 		 = Labels.getLabel("atti.documenti.popupSceltaSoggetto.${tipoSoggetto}.colonna2")
		labelNessunRisultato = Labels.getLabel("atti.documenti.popupSceltaSoggetto.${tipoSoggetto}.nessunRisultato")

		listaSoggetti = caratteristicaTipologiaService.calcolaListaSoggetti (idCaratteristicaTipologia, documentoDto.domainObject, soggetti, tipoSoggetto)
		listaSoggetti = listaSoggetti.sort { it.descrizione }

		def warnings = listaSoggetti.findAll { it.warnMessage != null }.warnMessage

		BindUtils.postNotifyChange(null, null, this, "listaSoggetti")
		if (warnings.size() > 0) {
			Clients.showNotification("Attenzione! Ci sono incongruenze di dati in Struttura Organizzativa:\n"+warnings.join("\n"), Clients.NOTIFICATION_TYPE_WARNING, self, "before_center", 5000, true);
		}

		// abilitazione pulsante elimina soggetto
		if (soggetti[tipoSoggetto] && tipoSoggetto.equals(TipoSoggetto.INCARICATO)) {
			eliminaSoggetto = true
		}

	}

	@Command onEliminaSoggetto () {
		Messagebox.show("Sei sicuro di voler eliminare il soggetto "+labelColonna1+" già inserito?", "Conferma cancellazione.",
				Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					public void onEvent(Event e) {
						if (Messagebox.ON_OK.equals(e.getName())) {
							soggetti[tipoSoggetto] = [:]
							Events.postEvent(Events.ON_CLOSE, self, soggetti)
						}
					}
				}
		)
	}

	@Command onSelezionaSoggetto () {
		// se ho annullato la modifica, non faccio niente:
		if (selectedRecord == null)
			return

		// se ho deselezionato il soggetto, lo svuoto
		if (soggetti[tipoSoggetto] == null) {
			soggetti[tipoSoggetto] = [:]
		}

		// se ho cambiato utente o unità, allora aggiorno il soggetto:
		if (soggetti[tipoSoggetto].utente?.id  != selectedRecord.utente?.id || soggetti[tipoSoggetto].unita?.progr != selectedRecord.unita?.progr) {
			soggetti[tipoSoggetto] = new SoggettoDocumento(selectedRecord)

			caratteristicaTipologiaService.aggiornaSoggetti(idCaratteristicaTipologia, documentoDto.domainObject, soggetti, tipoSoggetto)
		}

		Events.postEvent(Events.ON_CLOSE, self, selectedRecord)
	}

	@Command onAnnulla () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}