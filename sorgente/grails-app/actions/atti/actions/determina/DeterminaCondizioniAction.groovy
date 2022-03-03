package atti.actions.determina

import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione

class DeterminaCondizioniAction {

	/*	*******
	 *	stato del documento
	 */
	@Action(tipo		= TipoAzione.CONDIZIONE,
			tipiOggetto	= [Determina.TIPO_OGGETTO],
			nome		= "Determina numerata?",
			descrizione = "Controlla che una determina sia già numerata")
	boolean isDeterminaNumerata (Determina d) {
		return (d.numeroDetermina > 0 && d.annoDetermina > 0)
	}

	@Action(tipo		= TipoAzione.CONDIZIONE,
			tipiOggetto	= [Determina.TIPO_OGGETTO],
			nome		= "La Determina ha il secondo numero?",
			descrizione = "Ritorna true se la determina ha il secondo numero, false altrimenti.")
	boolean isDeterminaNumerata2 (Determina d) {
		return (d.numeroDetermina2 > 0 && d.annoDetermina2 > 0)
	}

	@Action(tipo		= TipoAzione.CONDIZIONE,
			tipiOggetto	= [Determina.TIPO_OGGETTO],
			nome		= "Proposta numerata?",
			descrizione = "Controlla che una proposta determina sia già numerata")
	boolean isPropostaDeterminaNumerata (Determina d) {
		return (d.numeroProposta > 0 && d.annoProposta > 0)
	}

	@Action(tipo		= TipoAzione.CONDIZIONE,
			tipiOggetto	= [Determina.TIPO_OGGETTO],
			nome		= "La Determina Non ha il secondo numero?",
			descrizione = "Ritorna true se la determina non ha il secondo numero, false altrimenti.")
	boolean isNotDeterminaNumerata2 (Determina d) {
		return !isDeterminaNumerata2(d)
	}

	@Action(tipo		= TipoAzione.CONDIZIONE,
			tipiOggetto	= [Determina.TIPO_OGGETTO],
			nome		= "Determina non numerata?",
			descrizione = "Controlla che una determina non sia già numerata")
	boolean isNotDeterminaNumerata (Determina d) {
		return !isDeterminaNumerata(d)
	}

	@Action(tipo		= TipoAzione.CONDIZIONE,
			tipiOggetto	= [Determina.TIPO_OGGETTO],
			nome		= "Proposta non numerata",
			descrizione = "Controlla che una proposta determina non sia già numerata")
	boolean isNotPropostaDeterminaNumerata (Determina d) {
		return !isPropostaDeterminaNumerata(d)
	}

	/*  *********
	 *	TIPOLOGIA
	 */
	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO],
		nome		= "Ha Impegno di Spesa?",
		descrizione	= "Ritorna TRUE se la proposta di determina ha un visto contabile.")
	boolean haImpegnoSpesa (Determina d) {


		long vistiContabili = VistoParere.createCriteria ().get () {
			projections {
				rowCount()
			}
			eq ("valido", true)
			eq ("determina", d)
			tipologia {
				eq ("contabile", true)
			}
		}

		return (vistiContabili > 0);
	}
}
