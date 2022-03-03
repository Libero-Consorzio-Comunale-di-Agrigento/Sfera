package atti.actions.certificato
import it.finmatica.atti.documenti.Certificato
import it.finmatica.atti.documenti.CertificatoService
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IAtto
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione
import it.finmatica.gestioneiter.motore.WkfIterService
import atti.documenti.CertificatoViewModel

class CertificatoAction {

	CertificatoService certificatoService
	WkfIterService wkfIterService

	@Action(tipo		= TipoAzione.PULSANTE,
			tipiOggetto = [Certificato.TIPO_OGGETTO],
			nome		= "Salva il certificato di pubblicazione",
			descrizione	= "Salva il certificato di pubblicazione: aggiunge il certificato alla determina e lo salva")
	public Certificato salva (Certificato certificato, CertificatoViewModel v) {
		certificato.save(failOnError: true)
		return certificato
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto = [Certificato.TIPO_OGGETTO],
		nome		= "Sblocca l'iter dell'atto principale",
		descrizione = "Sblocca l'iter dell'atto principale sia esso una determina o una delibera")
	public Certificato sbloccaAttoPrincipale (Certificato certificato) {
		wkfIterService.sbloccaDocumento(certificato.documentoPrincipale)
		return certificato
	}

	/*
	 * Certificati di Pubblicazione
	 */
	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Crea Certificato Pubblicazione",
		descrizione = "Crea e richiede il certificato di pubblicazione se richiesto dalla tipologia. Se viene creato")
	public def creaCertificatoPubblicazione (IAtto d) {
		if (d.tipologiaDocumento.pubblicazione && d.tipologiaDocumento.tipoCertPubb != null) {
			certificatoService.creaCertificato (d, d.tipologiaDocumento.tipoCertPubb, Certificato.CERTIFICATO_PUBBLICAZIONE, false);
		}
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Crea Certificato Avvenuta Pubblicazione",
		descrizione = "Crea e richiede il certificato di avvenuta pubblicazione se richiesto dalla tipologia")
	public def creaCertificatoAvvenutaPubblicazione (IAtto d) {
		if (d.tipologiaDocumento.pubblicazione && d.tipologiaDocumento.tipoCertAvvPubb != null) {
			certificatoService.creaCertificato (d, d.tipologiaDocumento.tipoCertAvvPubb, Certificato.CERTIFICATO_AVVENUTA_PUBBLICAZIONE, false)
		}
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Crea Certificato Seconda Pubblicazione",
		descrizione = "Crea e richiede il certificato di seconda pubblicazione se richiesto dalla tipologia")
	public def creaCertificatoPubblicazione2 (IAtto d) {
		if (d.tipologiaDocumento.pubblicazione && d.tipologiaDocumento.tipoCertPubb2 != null) {
			certificatoService.creaCertificato (d, d.tipologiaDocumento.tipoCertPubb2, Certificato.CERTIFICATO_PUBBLICAZIONE, true)
		}
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Crea Certificato Avvenuta Seconda Pubblicazione",
		descrizione = "Crea e richiede il certificato di avvenuta seconda pubblicazione se richiesto dalla tipologia")
	public def creaCertificatoAvvenutaPubblicazione2 (IAtto d) {
		if (d.tipologiaDocumento.pubblicazione && d.tipologiaDocumento.tipoCertAvvPubb2 != null) {
			certificatoService.creaCertificato (d, d.tipologiaDocumento.tipoCertAvvPubb2, Certificato.CERTIFICATO_AVVENUTA_PUBBLICAZIONE, true)
		}
		return d
	}
	
	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Delibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO],
		nome		= "Crea Certificato di Immediata Eseguibilita",
		descrizione = "Crea e richiede il certificato di immediata eseguibilità se configurato in tipologia.")
	public def creaCertificatoImmediataEseguibilita (IAtto d) {
		if (d.eseguibilitaImmediata && d.tipologiaDocumento.tipoCertImmEseg != null) {
			certificatoService.creaCertificato (d, d.tipologiaDocumento.tipoCertImmEseg, Certificato.CERTIFICATO_IMMEDIATA_ESEGUIBILITA, false)
		}
		return d
	}
}
