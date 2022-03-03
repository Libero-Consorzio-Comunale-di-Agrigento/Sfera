package atti.actions.commons

import it.finmatica.atti.documenti.Certificato
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.IProposta;
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.StatoConservazione
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.StatoOdg
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione


/**
 * Contiene le azioni sugli stati documento comuni a tutti i documenti
 *
 */
class StatoDocumentoAction {

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Il documento è ESECUTIVO?",
		descrizione	= "Ritorna TRUE se lo stato del documento è ESECUTIVO")
	public boolean isStatoEsecutivo (IDocumento d) {
		return (d.stato == StatoDocumento.ESECUTIVO)
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
		nome		= "Imposta il documento come CONCLUSO",
		descrizione	= "Imposta stato = CONCLUSO sul documento")
	public def setStatoConcluso (IDocumento d) {
		d.stato = StatoDocumento.CONCLUSO
		return d
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
		nome		= "Il documento è CONCLUSO?",
		descrizione	= "Ritorna TRUE se lo stato del documento è CONCLUSO")
	public boolean isStatoConcluso (IDocumento d) {
		return (d.stato == StatoDocumento.CONCLUSO)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Il documento è NON_ESECUTIVO?",
		descrizione	= "Ritorna TRUE se lo stato del documento è NON_ESECUTIVO")
	public boolean isStatoNonEsecutivo (IDocumento d) {
		return (d.stato == StatoDocumento.NON_ESECUTIVO)
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Certificato.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
		nome		= "Imposta il documento come ANNULLATO",
		descrizione	= "Imposta stato = ANNULLATO sul documento")
	public def setStatoAnnullato (IDocumento d) {
		d.stato = StatoDocumento.ANNULLATO
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto	= [Delibera.TIPO_OGGETTO],
			nome		= "Imposta il documento come in ATTESA ESECUTIVITA' MANUALE",
			descrizione	= "Imposta stato = ATTESA_ESECUTIVITA_MANUALE sul documento")
	public def setStatoAttesaEsecutivitaManuale (Delibera d) {
		// do' la possibilità di inserire la data di esecutività manuale solo se l'atto non è già esecutivo.
		// questo serve in particolare nei flussi in cui è possibile inserire la data di esecutività manuale
		// in qualsiasi momento e quindi anche quando l'atto è PUBBLICATO (quindi lo stato sarebbe PUBBLICATO e non farebbe vedere il pulsante in maschera)
		if (d.dataEsecutivita == null) {
			d.stato = StatoDocumento.ATTESA_ESECUTIVITA_MANUALE
		}
		return d
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
			tipiOggetto	= [Delibera.TIPO_OGGETTO],
			nome		= "Ritorna TRUE se la data di esecutività manuale è minore o uguale alla data odierna",
			descrizione	= "Ritorna TRUE se la data di esecutività manuale è minore o uguale alla data odierna")
	public boolean isDataEsecutivitaManualePrecedente (Delibera d) {
		// se il documento è già esecutivo, non posso impostare la data di esecutività manuale
		if (d.dataEsecutivita != null) {
			return false
		}

		if (d.dataEsecutivitaManuale == null) {
			return false
		}

		// se la data di esecutività manuale è nel futuro, ritorno false (sarà il job notturno a rendere esecutivo il documento)
		if (d.dataEsecutivitaManuale.clearTime().after(new Date().clearTime())) {
			return false
		}

		// la data di esecutività manuale è oggi o già passata
		return true
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
			tipiOggetto	= [Delibera.TIPO_OGGETTO],
			nome		= "Il documento è in stato di ATTESA ESECUTIVITA' MANUALE?",
			descrizione	= "Ritorna TRUE se lo stato del documento è ATTESA ESECUTIVITA' MANUALE")
	public boolean isStatoAttesaEsecutivitaManuale (IDocumento d) {
		return (d.stato == StatoDocumento.ATTESA_ESECUTIVITA_MANUALE)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
		nome		= "Il documento è ANNULLATO?",
		descrizione	= "Ritorna TRUE se lo stato del documento è ANNULLATO")
	public boolean isStatoAnnullato (IDocumento d) {
		return (d.stato == StatoDocumento.ANNULLATO)
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Certificato.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
		nome		= "Imposta il documento come PROCESSATO",
		descrizione	= "Imposta stato = PROCESSATO sul documento")
	public def setStatoProcessato (IDocumento d) {
		d.stato = StatoDocumento.PROCESSATO
		return d
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Certificato.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
		nome		= "Il documento è DA_PROCESSARE?",
		descrizione	= "Ritorna TRUE se lo stato del documento è DA_PROCESSARE")
	public boolean isStatoProcessato (IDocumento d) {
		return (d.stato == StatoDocumento.DA_PROCESSARE)
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Certificato.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
		nome		= "Imposta il documento come DA_PROCESSARE",
		descrizione	= "Imposta stato = DA_PROCESSARE sul documento")
	public def setStatoDaProcessare (IDocumento d) {
		d.stato = StatoDocumento.DA_PROCESSARE
		return d
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Certificato.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
		nome		= "Il documento è PROCESSATO?",
		descrizione	= "Ritorna TRUE se lo stato del documento è PROCESSATO")
	public boolean isStatoDaProcessare (IDocumento d) {
		return (d.stato == StatoDocumento.PROCESSATO)
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Imposta il documento come DA_CONSERVARE",
		descrizione	= "Imposta stato_conservazione = DA_CONSERVARE sul documento")
	public def setStatoConservazioneDaConservare (IAtto d) {
		if (d.tipologiaDocumento.conservazioneSostitutiva) {
			d.statoConservazione = StatoConservazione.DA_CONSERVARE
		}
		return d
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Il documento è DA_CONSERVARE?",
		descrizione	= "Ritorna TRUE se lo stato_conservazione del documento è DA_CONSERVARE")
	public boolean isStatoConservazioneDaConservare (IAtto d) {
		return (d.statoConservazione == StatoConservazione.DA_CONSERVARE)
	}

	/*
	 * Azioni per l'ordine del giorno
	 */

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Imposta il documento come ODG IN ISTRUTTORIA",
		descrizione	= "Cambia lo stato_odg del documento in IN ISTRUTTORIA")
	public def setStatoOdgInIstruttoria(IProposta d) {
		StatoOdg.mandaInSegreteriaIstruttoriaInCorso(d);
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Imposta il documento come ODG COMPLETO",
		descrizione	= "Cambia lo stato_odg del documento in COMPLETO")
	public def setStatoOdgCompleto (IProposta d) {
		StatoOdg.mandaInOdg(d);
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Imposta il documento come ODG CONCLUSO",
		descrizione	= "Cambia lo stato_odg del documento in CONCLUSO")
	public def setStatoOdgConcluso (IProposta d) {
		StatoOdg.concludiOdg(d);
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Imposta il documento come ODG DA_COMPLETARE",
		descrizione	= "Cambia lo stato_odg del documento in DA_COMPLETARE")
	public def setStatoOdgDaCompletare (IProposta d) {
		StatoOdg.mandaInSegreteria(d);
		return d
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Il documento è ODG COMPLETO?",
		descrizione	= "Ritorna TRUE se lo stato_odg del documento è COMPLETO")
	public boolean isStatoOdgCompleto (IProposta d) {
		return (d.statoOdg == StatoOdg.COMPLETO)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Il documento è ODG CONCLUSO?",
		descrizione	= "Ritorna TRUE se lo stato_odg del documento è CONCLUSO")
	public boolean isStatoOdgConcluso (IProposta d) {
		return (d.statoOdg == StatoOdg.CONCLUSO)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Il documento è ODG DA_COMPLETARE?",
		descrizione	= "Ritorna TRUE se lo stato_odg del documento è DA_COMPLETARE")
	public boolean isStatoOdgDaCompletare (IProposta d) {
		return (d.statoOdg == StatoOdg.DA_COMPLETARE)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
		nome		= "Il documento è stato salvato?",
		descrizione	= "Ritorna TRUE se il documento è stato salvato")
	public boolean isDocumentoSalvato (IProposta d) {
		return (d.id >= 0)
	}
}