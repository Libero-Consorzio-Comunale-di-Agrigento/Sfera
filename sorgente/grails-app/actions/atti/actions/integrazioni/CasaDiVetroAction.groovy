package atti.actions.integrazioni;

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.AbstractViewModel
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.CasaDiVetroService
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione
import it.finmatica.zkutils.SuccessHandler

import org.zkoss.zk.ui.Executions

class CasaDiVetroAction {

	SuccessHandler			successHandler;
	SpringSecurityService 	springSecurityService
	CasaDiVetroService 		casaDiVetroService

	@Action(tipo	= TipoAzione.CLIENT,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Apre la maschera della casa di vetro",
		descrizione	= "Apre la maschera della casa di vetro")
	public void apriCasaDiVetro (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {

		if (!Impostazioni.INTEGRAZIONE_CASA_DI_VETRO.abilitato) {
			return;
		}

		// se la proposta/atto è già in casa di vetro, allora la apro solamente
		// altrimenti la inserisco:
		def doc = viewModel.getDocumentoIterabile(false)
		casaDiVetroService.inserisci(doc)

		String url = casaDiVetroService.getUrlDocumento(doc)

		// salto l'invalidate della maschera:
		successHandler.saltaInvalidate()

		log.debug ("URL per casa di vetro: ${url}")
		Executions.getCurrent().sendRedirect(url, "_blank")
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
			nome		= "Aggiorna i dati della proposta/atto in Casa di Vetro",
			descrizione	= "Aggiorna i dati della proposta/atto in Casa di Vetro")
	public def aggiorna (def documento) {
		if (!Impostazioni.INTEGRAZIONE_CASA_DI_VETRO.abilitato) {
			return documento;
		}
		casaDiVetroService.aggiorna(documento);
		return documento;
	}

		@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Pubblica l'atto nella casa di vetro",
		descrizione	= "Pubblica l'atto nella casa di vetro")
	public def pubblica(def documento) {
		if (!Impostazioni.INTEGRAZIONE_CASA_DI_VETRO.abilitato) {
			return documento;
		}

		casaDiVetroService.pubblica(documento);

		return documento;
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "La casa di vetro è abilitata?",
		descrizione	= "Ritorna TRUE se l'integrazione con la casa di vetro è abilitata. False altrimenti.")
	public boolean isCasaDiVetroAbilitata (def documento) {
		return Impostazioni.INTEGRAZIONE_CASA_DI_VETRO.abilitato;
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "L'atto è presente sulla casa di vetro?",
		descrizione	= "L'atto è presente sulla casa di vetro?")
	public boolean isAttoPresente (def documento) {
		if (!Impostazioni.INTEGRAZIONE_CASA_DI_VETRO.abilitato) {
			return false;
		}

		return casaDiVetroService.esisteAtto(documento);
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "L'atto non è presente sulla casa di vetro?",
		descrizione	= "L'atto non è presente sulla casa di vetro?")
	public boolean isNotAttoPresente (def documento) {
		if (!Impostazioni.INTEGRAZIONE_CASA_DI_VETRO.abilitato) {
			return false;
		}

		return !isAttoPresente(documento);
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Controlla la presenza dell'atto sulla casa di vetro",
		descrizione	= "Controlla la presenza dell'atto sulla casa di vetro se è richiesta dalla tipologia. Se non presente, interrompe l'esecuzione.")
	public def controllaAttoPresenteObbligatorio (def documento) {
		if (Impostazioni.INTEGRAZIONE_CASA_DI_VETRO.abilitato) {
			if (documento.tipologia.pubblicazioneTrasparenza && !isAttoPresente(documento)) {
				throw new AttiRuntimeException("L'atto non è presente sulla casa di vetro")
			}
		}

		return documento;
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
			nome		= "Controlla che siano state compilate tutte le sezioni dell'atto sulla casa di vetro",
			descrizione	= "Controlla che siano state compilate tutte le sezioni dell'atto sulla casa di vetro. Se non sono presenti, interrompe l'esecuzione.")
	public def controllaSezioniAtto (def documento) {
		if (Impostazioni.INTEGRAZIONE_CASA_DI_VETRO.abilitato) {
			if (documento.categoria == null || documento.categoria?.controlloCdv) {  
				if (!casaDiVetroService.verificaSezioni(documento)) {
					throw new AttiRuntimeException("Esistono delle sezioni che non sono state compilate sulla casa di vetro")
				}
			}	
		}

		return documento;
	}
}
