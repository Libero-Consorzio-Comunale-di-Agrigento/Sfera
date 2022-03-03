package atti.actions.certificato

import it.finmatica.atti.documenti.Certificato
import it.finmatica.atti.documenti.CertificatoService
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IAtto
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione

class CertificatoCondizioniAction {
	
	CertificatoService certificatoService
	
	@Action(tipo	= TipoAzione.CONDIZIONE,
    	tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
    	nome		= "Il certificato di Pubblicazione è concluso oppure non necessario?",
    	descrizione = "Ritorna TRUE se il certificato di Pubblicazione è in stato concluso oppure se non è richiesto dalla tipologia.")
	boolean isCertPubbConcluso (IAtto d) {
		if (d.tipologiaDocumento.tipoCertPubb != null) {
			return certificatoService.isCertificatoPresenteEConcluso (d, d.tipologiaDocumento.tipoCertPubb, Certificato.CERTIFICATO_PUBBLICAZIONE)
		}
		
		// se non ho il certificato di avvenuta pubblicazione da aspettare, ritorno true.
		return true;
	}
	
	@Action(tipo	= TipoAzione.CONDIZIONE,
			tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
			nome		= "Il certificato di Seconda Pubblicazione è concluso oppure non necessario?",
			descrizione = "Ritorna TRUE se il certificato di Seconda Pubblicazione è in stato concluso oppure se non è richiesto dalla tipologia.")
	boolean isCertPubb2Concluso (IAtto d) {
		if (d.tipologiaDocumento.tipoCertPubb2 != null) {
			return certificatoService.isCertificatoPresenteEConcluso (d, d.tipologiaDocumento.tipoCertPubb2, Certificato.CERTIFICATO_PUBBLICAZIONE, true)
		}
		
		// se non ho il certificato di avvenuta pubblicazione da aspettare, ritorno true.
		return true;
	}
	
	@Action(tipo	= TipoAzione.CONDIZIONE,
			tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
			nome		= "Il certificato di avvenuta pubblicazione è concluso oppure non necessario?",
			descrizione = "Ritorna TRUE se il certificato di Avvenuta Pubblicazione è in stato concluso oppure se non è richiesto dalla tipologia.")
	boolean isCertAvvPubbConcluso (IAtto d) {
		if (d.tipologiaDocumento.tipoCertAvvPubb != null) {
			return certificatoService.isCertificatoPresenteEConcluso (d, d.tipologiaDocumento.tipoCertAvvPubb, Certificato.CERTIFICATO_AVVENUTA_PUBBLICAZIONE)
		}
		
		// se non ho il certificato di avvenuta pubblicazione da aspettare, ritorno true.
		return true;
	}
	
	@Action(tipo		= TipoAzione.CONDIZIONE,
			tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
			nome		= "Il certificato di avvenuta seconda pubblicazione è concluso oppure non necessario?",
			descrizione = "Ritorna TRUE se il certificato di Avvenuta Seconda Pubblicazione è in stato concluso oppure se non è richiesto dalla tipologia.")
	boolean isCertAvvPubb2Concluso (IAtto d) {
		if (d.tipologiaDocumento.tipoCertAvvPubb2 != null) {
			return certificatoService.isCertificatoPresenteEConcluso (d, d.tipologiaDocumento.tipoCertAvvPubb2, Certificato.CERTIFICATO_AVVENUTA_PUBBLICAZIONE, true)
		}
		
		// se non ho il certificato di avvenuta pubblicazione da aspettare, ritorno true.
		return true;
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Delibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO],
		nome		= "Il certificato di Immediata Eseguibilità è concluso oppure non necessario?",
		descrizione = "Ritorna TRUE se il certificato di Immediata Eseguibilità è in stato concluso oppure se non è richiesto dalla tipologia.")
	boolean isCertImmediataEseguibilitaConcluso (Delibera d) {
		if (d.tipologiaDocumento.tipoCertImmEseg != null) {
			return certificatoService.isCertificatoPresenteEConcluso (d, d.tipologiaDocumento.tipoCertImmEseg, Certificato.CERTIFICATO_IMMEDIATA_ESEGUIBILITA)
		}
		
		// se non ho il certificato di immediata eseguibilità da aspettare, ritorno true.
		return true;
	}
	
	// http://svi-redmine/issues/15770
	@Action(tipo		= TipoAzione.CONDIZIONE,
			tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
			nome		= "Il certificato di Esecutività è concluso oppure non necessario?",
			descrizione = "Ritorna TRUE se il certificato di Esecutività è presente e in stato concluso oppure se non è richiesto dalla tipologia.")
	boolean isCertEsecutivita (IAtto atto) {
		// Il Certificato di Esecutività è particolare: se è richiesto, parte N giorni dalla pubblicazione, quindi se non è presente, va "aspettato".
		if (atto.tipologiaDocumento.tipoCertEsec != null) {
			return certificatoService.isCertificatoPresenteEConcluso(atto, atto.tipologiaDocumento.tipoCertEsec, Certificato.CERTIFICATO_ESECUTIVITA);
		}
		
		// se non ho il certificato di esecutività da aspettare, ritorno true.
		return true;
	}
	
	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Con certificato di Avvenuta Pubblicazione?",
		descrizione = "Ritorna TRUE se la tipologia richiede il certificato di Avvenuta Pubblicazione.")
	boolean conCertAvvPubb (IAtto d) {
		return (d.tipologiaDocumento.tipoCertAvvPubb != null)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Con certificato di Pubblicazione?",
		descrizione = "Ritorna TRUE se la tipologia richiede il certificato di Pubblicazione.")
	boolean conCertPubb (IAtto d) {
		return (d.tipologiaDocumento.tipoCertPubb != null)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Con certificato di Avvenuta Seconda Pubblicazione?",
		descrizione = "Ritorna TRUE se la tipologia richiede il certificato di Avvenuta Seconda Pubblicazione.")
	boolean conCertAvvPubb2 (IAtto d) {
		return (d.tipologiaDocumento.tipoCertAvvPubb2 != null)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Con certificato di Seconda Pubblicazione?",
		descrizione = "Ritorna TRUE se la tipologia richiede il certificato di Seconda Pubblicazione.")
	boolean conCertPubb2 (IAtto d) {
		return (d.tipologiaDocumento.tipoCertPubb2 != null)
	}
	
	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Delibera.TIPO_OGGETTO],
		nome		= "Con certificato di Immediata Eseguibilità?",
		descrizione = "Ritorna TRUE se la tipologia richiede il certificato di Immediata Eseguibilità.")
	boolean conCertImmediataEseguibilita (Delibera d) {
		return (d.tipologiaDocumento.tipoCertImmEseg != null)
	}
	
	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Con certificato di Esecutività?",
		descrizione = "Ritorna TRUE se la tipologia richiede il certificato di Esecutività.")
	boolean conCertEsecutivita (IAtto d) {
		return (d.tipologiaDocumento.tipoCertEsec != null)
	}
}
