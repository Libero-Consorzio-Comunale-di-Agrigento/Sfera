package atti.actions.commons

import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.util.GrailsNameUtils
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.commons.SoggettiAttoriService
import it.finmatica.atti.commons.StrutturaOrganizzativaService
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.storico.StoricoService
import it.finmatica.atti.documenti.tipologie.ParametroTipologia
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.gestioneiter.Attore
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione
import it.finmatica.gestioneiter.motore.WkfAttoreStep
import it.finmatica.so4.struttura.So4SuddivisioneStruttura
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

/**
 * Contiene le azioni per il calcolo degli attori della determina
 */
class AttoriAction {

	StrutturaOrganizzativaService 	strutturaOrganizzativaService
	SoggettiAttoriService			soggettiAttoriService
	SpringSecurityService 			springSecurityService
	VistoParereService 				vistoParereService
	AttiGestoreCompetenze 			gestoreCompetenze
	StoricoService					storicoService

	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
			tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO],
			nome		= "Tutti gli attori del flusso",
			descrizione = "Ritorna tutti gli attori del flusso")
	List<Attore> getAttoriFlusso (IDocumento documento) {
		return soggettiAttoriService.getListaAttoriFlusso(documento.iter).collect { new Attore(utenteAd4: it.utenteAd4, ruoloAd4: it.ruoloAd4, unitaSo4: it.unitaSo4) }
	}

	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
			tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO],
			nome		= "Tutti gli attori dei flussi anche conclusi",
			descrizione = "Ritorna tutti gli attori dei flussi anche conclusi")
	List<Attore> getAttoriFlussiStorico (IDocumento documento) {
		List iterStorico = storicoService.getIterStorico(documento);
		List<Attore> attori = getAttoriFlusso(documento)
		for (def iter : iterStorico) {
			soggettiAttoriService.getListaAttoriFlusso(iter).each {
				attori.add (new Attore(utenteAd4: it.utenteAd4, ruoloAd4: it.ruoloAd4, unitaSo4: it.unitaSo4))
			}
		}
		return attori
	}

	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Firmatario del Visto/Parere",
		descrizione = "Ritorna l'utente firmatario del visto con il codice specificato in tipologia.",
		codiciParametri 	 = ["CODICE_VISTO"],
		descrizioniParametri = ["Codice del visto."])
	Attore getFirmatarioVisto (IDocumento documento) {

		// Recupero del parametro dalla tipologia
		String codiceVisto = ParametroTipologia.getValoreParametro (documento.tipologiaDocumento, documento.iter.stepCorrente.cfgStep, "CODICE_VISTO")

		// cerco il visto del funzionario
		VistoParere vistoParere = vistoParereService.getVisto(documento, codiceVisto)

		return new Attore(utenteAd4: vistoParere.getSoggetto(TipoSoggetto.FIRMATARIO).utenteAd4)
	}

	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO],
		nome		= "Attori in lettura",
		descrizione = "Ritorna tutti gli attori che hanno il documento in lettura.")
	List<Attore> getAttoriLettura (IDocumento documento) {
		return gestoreCompetenze.getAttoriCompetenze(documento, true, false)
	}
	
	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
			tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
			nome		= "Il Dirigente della Proposta",
			descrizione = "Ritorna il dirigente della proposta.")
	Attore getDirigenteProposta (IAtto atto) {
		def utente = atto.proposta.getSoggetto (TipoSoggetto.DIRIGENTE)?.utenteAd4
		
		if (utente == null) {
			return null
		}
		
		return new Attore (utenteAd4: utente)
	}

	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO],
		nome		= "Attori in modifica",
		descrizione = "Ritorna tutti gli attori che hanno il documento in modifica.")
	List<Attore> getAttoriModifica (IDocumento documento) {
		return gestoreCompetenze.getAttoriCompetenze(documento, false, true);
	}

	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Utente corrente",
		descrizione = "Ritorna l'utente correntemente loggato." )
	Attore getUtenteCorrente (IDocumento documento) {
		return new Attore(utenteAd4: springSecurityService.currentUser);
	}

	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Ritorna gli utenti che hanno una notifica sulla jworklist.",
		descrizione = "Ritorna gli utenti che hanno una notifica sulla jworklist." )
	List<Attore> getUtentiConNotifica (IDocumento documento) {
		List<Attore> attori = []

		String propertyName = GrailsNameUtils.getPropertyName(GrailsHibernateUtil.unwrapIfProxy(documento).class)

		def utenti = DestinatarioNotificaAttivita.createCriteria().list {
			projections {
				groupProperty ("utente")
			}

			eq (propertyName, documento)
		}

		for (Ad4Utente u : utenti) {
			attori.add (new Attore(utenteAd4: u))
		}

		return attori
	}

	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
		tipiOggetto	= [VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO],
		nome		= "Ritorna l'attore in carico del documento principale.",
		descrizione = "Ritorna l'attore in carico del documento principale" )
	List<Attore> getAttoriInCaricoDocumentoPrincipale (def v) {
		List<Attore> attori = []

		for (WkfAttoreStep attoreStep : v.documentoPrincipale.iter.stepCorrente.attori) {
			attori.add (new Attore(utenteAd4: attoreStep.utenteAd4, unitaSo4: attoreStep.unitaSo4, ruoloAd4: attoreStep.ruoloAd4))
		}

		return attori
	}
	
	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO],
		nome		= "Ritorna gli attori che hanno in carico il documento.",
		descrizione = "Ritorna gli attori che hanno in carico il documento." )
	List<Attore> getAttoriInCarico (def documento) {
		List<Attore> attori = []

		for (WkfAttoreStep attoreStep : documento.iter.stepCorrente.attori) {
			attori.add (new Attore(utenteAd4: attoreStep.utenteAd4, unitaSo4: attoreStep.unitaSo4, ruoloAd4: attoreStep.ruoloAd4))
		}

		return attori
	}

	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
		tipiOggetto	= [VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO],
		nome		= "Ritorna gli attori con competenze sul documento principale.",
		descrizione = "Ritorna gli attori con competenze sul documento principale." )
	List<Attore> getCompetenzeDocumentoPrincipale (def v) {
		return gestoreCompetenze.getAttoriCompetenze(v.documentoPrincipale)
	}

	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
		nome		= "Ritorna l'unità proponente e le sue unità figlie",
		descrizione = "Calcola l'unità proponente e le sue unità figlie")
	List<Attore> getUnitaFiglieUoProponente (def d) {
		def attori = []

		So4UnitaPubb uoProponente = d.unitaProponente
		attori << new Attore (unitaSo4: uoProponente)

		List<So4UnitaPubb> uoFiglie = strutturaOrganizzativaService.getUnitaFiglieNLivello (uoProponente.progr, uoProponente.ottica.codice, uoProponente.dal)
		for (So4UnitaPubb uo : uoFiglie) {
			attori << new Attore (unitaSo4: uo)
		}

		return attori
	}

	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
		nome		= "Ritorna le unità del ramo SERVIZIO dell'unità proponente.",
		descrizione = "Ritorna le unità del ramo SERVIZIO dell'unità proponente.")
	List<Attore> getUnitaRamoServizioUoProponente (def d) {
		return getUnitaRamo(d.unitaProponente, Impostazioni.SO4_SUDDIVISIONE_SERVIZIO.valore)
	}

	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
		nome		= "Ritorna le unità del ramo AREA dell'unità proponente.",
		descrizione = "Ritorna le unità del ramo AREA dell'unità proponente.")
	List<Attore> getUnitaRamoAreaUoProponente (def d) {
		return getUnitaRamo(d.unitaProponente, Impostazioni.SO4_SUDDIVISIONE_AREA.valore)
	}

	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
		nome		= "Ritorna l'unità SERVIZIO dell'unità proponente.",
		descrizione = "Ritorna le unità SERVIZIO dell'unità proponente.")
	Attore getUnitaServizioUoProponente (def d) {
        So4SuddivisioneStruttura suddivisione = So4SuddivisioneStruttura.getSuddivisione(Impostazioni.SO4_SUDDIVISIONE_SERVIZIO.valore, Impostazioni.OTTICA_SO4.valore).get()
		return new Attore(unitaSo4: strutturaOrganizzativaService.getUnitaVertice(d.unitaProponente, suddivisione.id))
	}

	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
		nome		= "Ritorna l'unità AREA dell'unità proponente.",
		descrizione = "Ritorna l'unità AREA dell'unità proponente.")
	Attore getUnitaAreaUoProponente (def d) {
        So4SuddivisioneStruttura suddivisione = So4SuddivisioneStruttura.getSuddivisione(Impostazioni.SO4_SUDDIVISIONE_AREA.valore, Impostazioni.OTTICA_SO4.valore).get()
		return new Attore(unitaSo4: strutturaOrganizzativaService.getUnitaVertice(d.unitaProponente, suddivisione.id))
	}

	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
		tipiOggetto	= [Delibera.TIPO_OGGETTO],
		nome		= "Ritorna l'unità proponente del documento",
		descrizione = "Ritorna l'unità proponente del documento")
	Attore getUnitaProponenteDelibera (Delibera d) {
		return new Attore (unitaSo4: d.unitaProponente)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "L'utente ha fatto switch-user?",
		descrizione = "Ritorna TRUE solo se l'utente corrente ha effettuato SWITCH-USER")
	boolean isSwitchedUser (def d) {
		return SpringSecurityUtils.isSwitched()
	}

	private List<Attore> getUnitaRamo (So4UnitaPubb uoPartenza, String codiceSuddivisione) {
		List<So4UnitaPubb> uoFiglie = strutturaOrganizzativaService.getUnitaFiglieSuddivisione(uoPartenza, codiceSuddivisione)

		def attori = []
		for (So4UnitaPubb uo : uoFiglie) {
			attori << new Attore (unitaSo4: uo)
		}

		return attori
	}

	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
			tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
			nome		= "Ritorna l'unità proponente e tutte le unità del suo ramo",
			descrizione = "Calcola l'unità proponente e tutte le unità del suo ramo")
	public List<Attore> getUnitaEPadri(def d){
		So4UnitaPubb unita = d.unitaProponente;

		def attori = []
		while (unita != null) {
			attori << new Attore(unitaSo4: unita)
			unita = unita.getUnitaPubbPadre()
		}

		return attori;
	}


	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
			tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
			nome		= "Ritorna la prima unità tra l'unità proponente e le unità del suo ramo che ha almeno un soggetto con ruolo definito nelle impostazioni",
			descrizione = "Calcola la prima unità tra l'unità proponente e le unità del suo ramo che ha almeno un soggetto con ruolo definito nelle impostazioni")
	public List<Attore> getUnitaEPadriConUtente(def d){
		So4UnitaPubb unita = d.unitaProponente;
		String ruolo = Impostazioni.RUOLO_SO4_CALCOLO_STRUTTURA.valore
		while (unita != null) {
			if (strutturaOrganizzativaService.getComponentiConRuoloInUnita(ruolo, unita.progr, unita.ottica.codice).size() > 0) {
				return [new Attore(unitaSo4: unita)]
			}
			unita = unita.getUnitaPubbPadre()
		}

		return [];
	}
}
