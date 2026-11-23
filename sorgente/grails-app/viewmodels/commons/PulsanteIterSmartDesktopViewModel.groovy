package commons

import it.finmatica.atti.AbstractViewModel
import it.finmatica.atti.documenti.*
import it.finmatica.atti.integrazioni.SmartDesktopService
import it.finmatica.atti.integrazioni.jworklist.JWorklistDispatcher
import it.finmatica.dto.DTO
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgPulsante
import it.finmatica.gestioneiter.motore.WkfIterService
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PulsanteIterSmartDesktopViewModel extends AbstractViewModel<IDocumento> {

	SmartDesktopService smartDesktopService
	JWorklistDispatcher jworklistDispatcher
	WkfIterService wkfIterService

	Window self

	String titolo
	WkfCfgPulsante wkfCfgPulsante
	IDocumento documento
	IDocumento documentoPrincipale
	boolean pulsantePremuto = false

	String idRiferimento
	String note = ""    //campo note inserito dall'utente
	String oggetto     //oggetto dell'atto (solo in visualizzazione)
	String estremiAtto //estremi dell'atto (solo in visualizzazione)

	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @QueryParam("ID_RIFERIMENTO") String riferimento, @QueryParam("ID_CFG_PULSANTE") long idCfgPulsante) {
		this.self = w
		wkfCfgPulsante = WkfCfgPulsante.get(idCfgPulsante)
		documento = jworklistDispatcher.getDocumento(riferimento)
		idRiferimento = riferimento

		titolo = wkfCfgPulsante.etichetta

		if ((documento instanceof Determina) || (documento instanceof Delibera)) {
			oggetto = documento.oggetto
			estremiAtto = documento.estremiAtto
		}
		else {
			if ((documento instanceof PropostaDelibera)) {
				oggetto = documento.oggetto
				if (documento.numeroProposta) {
					estremiAtto = documento.numeroProposta + " / " + documento.annoProposta + " " + documento.registroProposta?.descrizione
				}
				else {
					estremiAtto = "Proposta non numerata"
				}
			}
			else {
				if ((documento instanceof VistoParere)) {
					documentoPrincipale = documento.documentoPrincipale
					if (documentoPrincipale) {
						oggetto = documentoPrincipale.oggetto
						if ((documentoPrincipale instanceof PropostaDelibera)) {
							if (documentoPrincipale.numeroProposta) {
								estremiAtto = documentoPrincipale.numeroProposta + " / " + documentoPrincipale.annoProposta + " " + documentoPrincipale.registroProposta?.descrizione
							}
							else {
								estremiAtto = "Proposta non numerata"
							}
						}
						else {
							estremiAtto = documentoPrincipale.estremiAtto
						}
					}
				}
			}
		}
	}

	@NotifyChange(["pulsantePremuto"])
	@Command onEsegui () {
		if (!pulsantePremuto) {
			smartDesktopService.eseguiPulsanteIter(this, idRiferimento, wkfCfgPulsante.id)
			pulsantePremuto = true
		}
		//Clients.evalJavaScript('''window.parent.location.reload();''') //javascript per aggiornare la scrivania
	}

	@Command onChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@Override
	IDocumento getDocumentoIterabile(boolean controllaConcorrenza) {
		return null
	}

	@Override
	void aggiornaMaschera(IDocumento documentoIterabile) {

	}

	@Override
	void aggiornaDocumentoIterabile(IDocumento documento) {
		if (note) {
			String testoNote = new Date().format("dd/MM/yyyy HH:mm:ss")+": "+note
			if (documento.note) {
				documento.note = documento.note+"\n\n"+ testoNote
			}
			else {
				documento.note = testoNote
			}
		}
	}

	@Override
	WkfCfgIter getCfgIter() {
		return null
	}

	@Override
	Collection<String> validaMaschera() {
		return null
	}

	@Override
	DTO getDocumentoDTO() {
		return null
	}
}
