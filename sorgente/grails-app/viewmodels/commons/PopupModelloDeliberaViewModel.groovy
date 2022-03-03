package commons

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.DeliberaService
import it.finmatica.atti.odg.CommissioneStampa
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.so4.login.So4UserDetail
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupModelloDeliberaViewModel {

    // service
	SpringSecurityService springSecurityService
	DeliberaService deliberaService

	// componenti
	Window self
	So4UserDetail utente

	// dati
	def	listaModelli
	def	selectedModello

	// stato

	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("idCommissione") Long idCommissione) {
		this.self = w
		CommissioneStampa stampa = CommissioneStampa.createCriteria().get() {
			eq ('commissione.id', idCommissione)
			modelloTesto {
				like('tipoModello.codice', Delibera.TIPO_OGGETTO+"%")
			}
		}
		selectedModello = stampa?.modelloTesto?.toDTO();
		utente 		 	= springSecurityService.principal;
		listaModelli	= [new GestioneTestiModelloDTO(id:-1, nome:"-- usa modello specificato in tipologia --", descrizione:"Per ogni delibera, utilizza il modello testo specificato in tipologia.")] + deliberaService.getListaModelliTestoAbilitati(utente)
	}

	@Command onSalva() {
		Events.postEvent(Events.ON_CLOSE, self, [esito: "OK", modello:selectedModello])
	}

	@Command onAnnulla () {
		Events.postEvent(Events.ON_CLOSE, self, [esito:"ANNULLA"])
	}
}
