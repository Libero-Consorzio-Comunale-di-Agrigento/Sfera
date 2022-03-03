package atti.actions.propostadelibera

import it.finmatica.atti.documenti.Determina;
import it.finmatica.atti.documenti.StatoOdg;
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.documenti.tipologie.ParametroTipologia
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.odg.OggettoSeduta;
import it.finmatica.atti.odg.dizionari.EsitoStandard;
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione



class PropostaDeliberaCondizioniAction {

	/*	*******
	 *	PULSANTI
	 */

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
		nome		= "La Proposta ha un esito?",
		descrizione = "Ritorna TRUE se l'esito della proposta è diverso da null e la proposta ha l'esito confermato.")
	public boolean isPropostaConEsito(PropostaDelibera d) {
		// se oggettoSeduta == null è perché sono appena entrato in odg e non ho alcun oggettoseduta da controllare.
		// l'esito deve anche essere confermato perché il flusso si sblocchi!
		return (d.oggettoSeduta?.esito != null && d.oggettoSeduta?.confermaEsito)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
		nome		= "La Proposta ha esito ADOTTATO?",
		descrizione = "Ritorna TRUE se l'esito della proposta è ADOTTATO")
	public boolean isEsitoPropostaPositivo(PropostaDelibera d) {
		return (d.oggettoSeduta.esito.esitoStandard.codice == EsitoStandard.ADOTTATO)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
		nome		= "La Proposta ha esito NON ADOTTATO?",
		descrizione = "Ritorna TRUE se l'esito della proposta è NON ADOTTATO")
	public boolean isEsitoPropostaNegativo(PropostaDelibera d) {
		return (d.oggettoSeduta.esito.esitoStandard.codice == EsitoStandard.NON_ADOTTATO)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
		nome		= "La Proposta ha esito PARZIALE?",
		descrizione = "Ritorna TRUE se l'esito della proposta è PARZIALE")
	public boolean isEsitoPropostaParziale(PropostaDelibera d) {
		return (d.oggettoSeduta.esito.esitoStandard.codice == EsitoStandard.PARZIALE)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
		nome		= "La Proposta ha esito RINVIO_UFFICIO?",
		descrizione = "Ritorna TRUE se l'esito della proposta è RINVIO_UFFICIO")
	public boolean isEsitoPropostaRinvio(PropostaDelibera d) {
		return (d.oggettoSeduta.esito.esitoStandard.codice == EsitoStandard.RINVIO_UFFICIO)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
		nome		= "La Proposta ha esito DA_RATIFICARE?",
		descrizione = "Ritorna TRUE se l'esito della proposta è DA_RATIFICARE")
	public boolean isEsitoPropostaDaRatificare(PropostaDelibera d) {
		return (d.oggettoSeduta.esito.esitoStandard.codice == EsitoStandard.DA_RATIFICARE)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
		nome		= "La Proposta ha esito INVIA_COMMISSIONE?",
		descrizione = "Ritorna TRUE se l'esito della proposta è INVIA_COMMISSIONE")
	public boolean isEsitoPropostaInviaCommissione(PropostaDelibera d) {
		return (d.oggettoSeduta.esito.esitoStandard.codice == EsitoStandard.INVIA_COMMISSIONE)
	}

	@Action(tipo		= TipoAzione.CONDIZIONE,
			tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
			nome		= "La Proposta di Delibera è COMPLETO?",
			descrizione = "Ritorna TRUE se la determina è numerata e se ha lo stato per l'odg a INIZIALE oppure se lo stato per l'odg è COMPLETO ed è stata già inserita in odg.")
	public boolean isPropostaDeliberaStatoDaCompletare(PropostaDelibera d) {
		return ((d.numeroProposta > 0 && d.annoProposta > 0 && d.statoOdg == StatoOdg.INIZIALE) || 	// FIXME: perchè questo?
				(d.statoOdg == StatoOdg.COMPLETO && OggettoSeduta.findByPropostaDelibera(d) == null))	// FIXME: non basta il controllo sullo stato?
	}

	@Action(tipo		= TipoAzione.CONDIZIONE,
			tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
			nome		= "PropostaDelibera setta stato completa",
			descrizione = "Controlla che una propostaDelibera sia nello stato da completare")
	public boolean isPropostaDeliberaStatoCompleta(PropostaDelibera d) {
		return (d.statoOdg == StatoOdg.DA_COMPLETARE)
	}

	@Action(tipo		= TipoAzione.CONDIZIONE,
			tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
			nome		= "Proposta predisposizione odg?",
			descrizione = "Controlla che una propostaDelibera sia già numerata e da mandare in odg")
	public boolean isPropostaDeliberaPredisposizioneODG (PropostaDelibera d) {
		return (d.numeroProposta > 0 && d.annoProposta > 0 && d.statoOdg == StatoOdg.INIZIALE)
	}

	@Action(tipo		= TipoAzione.CONDIZIONE,
			tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
			nome		= "PropostaDelibera numerata?",
			descrizione = "Controlla che una propostaDelibera sia già numerata")
	public boolean isPropostaDeliberaNumerata (PropostaDelibera d) {
		return (d.numeroProposta > 0 && d.annoProposta > 0)
	}

	@Action(tipo		= TipoAzione.CONDIZIONE,
			tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
			nome		= "Proposta numerata?",
			descrizione = "Controlla che una proposta propostaDelibera sia già numerata")
	public boolean isPropostaPropostaDeliberaNumerata (PropostaDelibera d) {
		return (d.numeroProposta > 0 && d.annoProposta > 0)
	}

	@Action(tipo		= TipoAzione.CONDIZIONE,
			tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
			nome		= "PropostaDelibera non numerata?",
			descrizione = "Controlla che una propostaDelibera non sia già numerata")
	public boolean isNotPropostaDeliberaNumerata (PropostaDelibera d) {
		return !isPropostaDeliberaNumerata(d)
	}

	@Action(tipo		= TipoAzione.CONDIZIONE,
			tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
			nome		= "Proposta non numerata",
			descrizione = "Controlla che una proposta propostaDelibera non sia già numerata")
	public boolean isNotPropostaPropostaDeliberaNumerata (PropostaDelibera d) {
		return !isPropostaPropostaDeliberaNumerata(d)
	}


	/*  *********
	 *	TIPOLOGIA
	 */
	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
		nome		= "Ha Impegno di Spesa?",
		descrizione	= "Ritorna TRUE se la proposta di delibera ha un visto contabile.")
	public boolean haImpegnoSpesa (PropostaDelibera d) {

		long vistiContabili = VistoParere.createCriteria ().get () {
			projections {
				rowCount()
			}
			eq ("valido", true)
			eq ("propostaDelibera", d)
			tipologia {
				eq ("contabile", true)
			}
		}

		return (vistiContabili > 0);
	}

	@Action(tipo		= TipoAzione.CONDIZIONE,
			tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
			nome		= "PropostaDelibera con Parere dei Revisori dei Conti?",
			descrizione = "Ritorna TRUE se per la propostaDelibera è attivo il flag del Parere dei Revisori dei Conti")
	public boolean isPropostaParereRevisoriConti (PropostaDelibera d) {
		return d.parereRevisoriConti
	}
}
