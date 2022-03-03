package atti.actions.commons

import it.finmatica.atti.dizionari.CalendarioFestivitaService
import it.finmatica.atti.documenti.*
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione
import org.apache.log4j.Logger

/**
 * Contiene le azioni per la firma dei documenti
 */
class PubblicazioneAction {

	private static final Logger log = Logger.getLogger(PubblicazioneAction.class)

	DocumentoService		documentoService
	CalendarioFestivitaService calendarioFestivitaService

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Delibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO],
		nome		= "Pubblicazione Manuale?",
		descrizione = "Ritorna TRUE se la tipologia prevede la pubblicazione manuale.")
	boolean isPubblicazioneManuale (IAtto d) {
		return d.tipologiaDocumento.manuale
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Delibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO],
		nome		= "In seconda pubblicazione?",
		descrizione = "Ritorna TRUE se il documento è in seconda pubblicazione.")
	boolean isSecondaPubblicazione (IAtto d) {
		return (d.dataPubblicazione != null && d.dataFinePubblicazione != null)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Delibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO],
		nome		= "Documento da pubblicare?",
		descrizione = "Ritorna TRUE se il documento è da pubblicare (legge dalla tipologia).")
	boolean isDaPubblicare (IAtto d) {
		return documentoService.isDaPubblicare(d);
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Delibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO],
		nome		= "Documento con seconda pubblicazione?",
		descrizione = "Ritorna TRUE se il documento è da pubblicare in seconda pubblicazione (legge dalla tipologia).")
	boolean isDaPubblicare2 (IAtto d) {
		return documentoService.isDaPubblicare(d)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Delibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO],
		nome		= "Documento da pubblicare per la trasparenza?",
		descrizione = "Ritorna TRUE se il documento è da pubblicare per la trasparenza (legge dalla tipologia).")
	boolean isDaPubblicareTrasparenza (IAtto d) {
		return (d.tipologiaDocumento.pubblicazioneTrasparenza)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Pubblicazione terminata?",
		descrizione = "Ritorna TRUE se la data odierna è maggiore della data di fine pubblicazione")
	boolean isPubblicazioneFinita (IAtto d) {
		return documentoService.isPubblicazioneFinita(d);
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Pubblicazione fino a revoca?",
		descrizione = "Ritorna TRUE se la pubblicazione sul documento è segnata come fino a revoca.")
	boolean isPubblicazioneFinoARevoca (IAtto d) {
		return d.pubblicaRevoca
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Termina Pubblicazione",
		descrizione = "Imposta la data di fine pubblicazione in caso di pubblicazione fino a revoca.")
	def terminaPubblicazione (IAtto d) {

		// se ero in prima pubblicazione:
		if (d.pubblicaRevoca && d.dataPubblicazione != null && d.dataFinePubblicazione == null) {
			d.dataFinePubblicazione = new Date()
		} else if (d.pubblicaRevoca && d.dataPubblicazione2 != null && d.dataFinePubblicazione2 == null) {
			d.dataFinePubblicazione2 = new Date()
		}

		return d;
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Delibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO],
		nome		= "Attiva Pubblicazione (deve essere l'ultima dell'elenco)",
		descrizione	= "Attiva l'iter di pubblicazione se nella tipologia è scritto che deve essere pubblicata. Copia le competenze della proposta sulla delibera.")
	def attivaPubblicazione (IAtto d) {
		return documentoService.attivaPubblicazione(d)
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto	= [Delibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
			nome		= "Pubblicazione",
			descrizione	= "Inizia effettivamente il periodo di pubblicazione controllando e settando tutte le date necessarie, pubblica sull'albo se richiesto dalla configurazione")
	def pubblicazione (IPubblicabile d) {
		return documentoService.pubblicazione(d)
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto	= [Delibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
			nome		= "Pubblicazione il prossimo giorno festivo",
			descrizione	= "Inizia effettivamente il periodo di pubblicazione il prossimo giorno festivo controllando e settando tutte le date necessarie, pubblica sull'albo se richiesto dalla configurazione")
	def pubblicazioneFestiva (IPubblicabile d) {
		return documentoService.pubblicazione(d, calendarioFestivitaService.getProssimoGiornoFestivo(new Date()))
	}

	@Action(tipo		= TipoAzione.AUTOMATICA,
			tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
			nome		= "Pubblica su JMessi",
			descrizione	= "Pubblica la delibera sull'albo JMessi se nella tipologia è scritto che la delibera deve essere pubblicata e se è abilitata la pubblicazione ad un albo esterno.")
	def pubblicaAlboGdm (IPubblicabile d) {
		// controllo se devo pubblicare e se è abilitata la pubblicazione all'albo, se sì procedo, altrimenti no.
		if (!Impostazioni.INTEGRAZIONE_ALBO.isDisabilitato()) {
			return documentoService.pubblicazione (d)
		}

		return d
	}

	@Action(tipo		= TipoAzione.AUTOMATICA,
			tipiOggetto	= [SedutaStampa.TIPO_OGGETTO],
			nome		= "Pubblica la stampa sul visualizzatore",
			descrizione	= "Pubblica stampa della seduta sul visualizzatore")
	SedutaStampa pubblicaNelVisualizzatore (SedutaStampa sedutaStampa) {
		sedutaStampa.pubblicaVisualizzatore = true
		return sedutaStampa
	}

	@Action(tipo		= TipoAzione.AUTOMATICA,
			tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
			nome		= "Pubblica il documento sul visualizzatore",
			descrizione	= "Pubblica il documento sul visualizzatore")
	IAtto pubblicaVisualizzatore (IAtto d) {
		if (d.tipologiaDocumento.pubblicaVisualizzatore){
			d.pubblicaVisualizzatore = true
		}
		return d
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
			tipiOggetto	= [Delibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
			nome		= "Il documento è pubblicato all'albo?",
			descrizione = "Ritorna TRUE se il documento ha il numero dell'albo, FALSE altrimenti.")
	boolean isDocumentoPubblicatoAlbo (IPubblicabile d) {
		return (d.numeroAlbo > 0)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
			tipiOggetto	= [Delibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
			nome		= "Il documento non è pubblicato all'albo?",
			descrizione = "Ritorna TRUE se il documento NON ha il numero dell'albo, FALSE altrimenti.")
	boolean isNotDocumentoPubblicatoAlbo (IPubblicabile d) {
		return !isDocumentoPubblicatoAlbo(d)
	}
}
