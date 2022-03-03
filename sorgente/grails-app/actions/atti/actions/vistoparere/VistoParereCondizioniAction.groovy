package atti.actions.vistoparere

import atti.documenti.VistoViewModel
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.tipologie.ParametroTipologia
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione

class VistoParereCondizioniAction {

	VistoParereService vistoParereService

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "Il visto ha esito FAVOREVOLE?",
		descrizione = "Ritorna TRUE se l'esito è FAVOREVOLE")
	public boolean isEsitoFavorevole (VistoParere vistoParere) {
		return (vistoParere.esito == EsitoVisto.FAVOREVOLE)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "Il visto ha esito CONTRARIO?",
		descrizione = "Ritorna TRUE se l'esito è CONTRARIO")
	public boolean isEsitoContrario (VistoParere vistoParere) {
		return (vistoParere.esito == EsitoVisto.CONTRARIO)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "Il Visto ha esito NON_APPOSTO?",
		descrizione = "Ritorna TRUE se l'esito è NON APPOSTO")
	public boolean isEsitoNonApposto (VistoParere vistoParere) {
		return (vistoParere.esito == EsitoVisto.NON_APPOSTO)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "Il Visto ha esito DA_VALUTARE?",
		descrizione = "Ritorna TRUE se l'esito è DA VALUTARE")
	public boolean isEsitoDaValutare (VistoParere vistoParere) {
		return (vistoParere.esito == EsitoVisto.DA_VALUTARE)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "Il Visto ha esito RIMANDA INDIETRO?",
		descrizione = "Ritorna TRUE se l'esito è RIMANDA INDIETRO")
	public boolean isEsitoRimandaIndietro (VistoParere vistoParere) {
		return (vistoParere.esito == EsitoVisto.RIMANDA_INDIETRO)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "Il visto ha un esito?",
		descrizione = "Ritorna TRUE se il visto ha un esito FAVOREVOLE, CONTRARIO, o NON APPOSTO. Ritorna FALSE se è DA_VALUTARE o RIMANDA_INDIETRO.")
	public boolean isEsitoPresente (VistoParere vistoParere) {
		return (vistoParere.esito != null && vistoParere.esito != EsitoVisto.DA_VALUTARE && vistoParere.esito != EsitoVisto.RIMANDA_INDIETRO)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE_VISIBILITA,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "L'unità destinataria è cambiata?",
		descrizione = "Ritorna TRUE se l'unità è stata cambiata in interfaccia")
	public boolean isUnitaDestinatariaCambiata (VistoParere vistoParere, VistoViewModel viewModel) {
		return (vistoParere.unitaSo4?.progr != viewModel.soggetti[TipoSoggetto.UO_DESTINATARIA]?.unita?.progr)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE_VISIBILITA,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "L'unità destinataria non è cambiata?",
		descrizione = "Ritorna TRUE se l'unità non è stata cambiata in interfaccia")
	public boolean isUnitaDestinatariaUguale (VistoParere vistoParere, VistoViewModel viewModel) {
		return !isUnitaDestinatariaCambiata(vistoParere, viewModel)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "Con passaggio in redazione unità?",
		descrizione = "Ritorna TRUE se la tipologia del visto indica che deve passare in redazione all'unità")
	public boolean conPassaggioRedazioneUnita (VistoParere vistoParere) {
		return (vistoParere.tipologia.conRedazioneUnita)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "Con passaggio in redazione al Dirigente?",
		descrizione = "Ritorna TRUE se la tipologia del visto indica che deve passare in redazione al dirigente")
	public boolean conPassaggioRedazioneDirigente (VistoParere vistoParere) {
		return (vistoParere.tipologia.conRedazioneDirigente)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "Con firma del Dirigente?",
		descrizione = "Ritorna TRUE se la tipologia del visto indica che deve passare in firma al dirigente")
	public boolean conFirmaDirigente (VistoParere vistoParere) {
		return (vistoParere.tipologia.conFirma)
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "Il visto è Contabile?",
		descrizione = "Ritorna TRUE se la tipologia del visto indica che il visto è contabile.")
	public boolean isContabile (VistoParere vistoParere) {
		return (vistoParere.tipologia.contabile)
	}

	/*	*****
	 *	VISTI
	 */

	@Action(tipo		= TipoAzione.CONDIZIONE,
			tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
			nome		= "Esistono visti DA PROCESSARE?",
			descrizione = "Ritorna true se esiste almeno un visto con il CODICE_VISTO specificato in stato DA_PROCESSARE.",
			codiciParametri = ["CODICE_VISTO"],
			descrizioniParametri = ["Codice del visto/parere da verificare"])
	public boolean esistonoVistiDaProcessare (def d) {
		String codiceVisto = getParametro (d, "CODICE_VISTO");
		return vistoParereService.esisteAlmenoUnVisto(d.visti, codiceVisto, StatoDocumento.DA_PROCESSARE);
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Tutti i visti con il codice in tipologia sono conclusi?",
		descrizione = "Ritorna true se tutti i visti con il CODICE_VISTO specificato sono in stato CONCLUSO.",
		codiciParametri = ["CODICE_VISTO"],
		descrizioniParametri = ["Codice del visto/parere da verificare"])
	public boolean isTuttiVistiConclusi (def d) {
		String codiceVisto = getParametro (d, "CODICE_VISTO");
		return vistoParereService.tuttiVistiSono(d.visti, codiceVisto, StatoDocumento.CONCLUSO);
	}

	@Action(tipo = TipoAzione.CONDIZIONE
		, tipiOggetto	= [Delibera.TIPO_OGGETTO]
		, nome		= "Tutti i visti sono conclusi?"
		, descrizione = "Ritorna true se tutti i visti sono in stato CONCLUSO oppure se non ci sono visti validi da aspettare.")
	public boolean isTuttiVistiConclusiSenzaCodice (def d) {

		int contaValidi = 0;
		for (def v : d.visti) {
			if (v.valido) {
				contaValidi ++;
			}
		}

		if (contaValidi == 0) {
			return true;
		}

		return vistoParereService.tuttiVistiSono(d.visti, null, StatoDocumento.CONCLUSO);
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Esiste un visto contabile DA PROCESSARE?",
		descrizione = "Ritorna true se esiste un visto che ha contabile = true nella tipologia e in stato DA PROCESSARE.")
	public boolean esisteVistoContabileDaProcessare (def d) {
		return vistoParereService.esisteAlmenoUnVisto(d.visti, null, StatoDocumento.DA_PROCESSARE, null, true);
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Non esiste un visto contabile DA PROCESSARE?",
		descrizione = "Ritorna true se non esiste un visto che ha contabile = true nella tipologia e in stato DA PROCESSARE.")
	public boolean nonEsisteVistoContabileDaProcessare (def d) {
		return !(esisteVistoContabileDaProcessare(d));
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Tutti i Visti sono Favorevoli?",
		descrizione = "Ritorna TRUE se tutti i visti sono in stato CONCLUSO e hanno esito FAVOREVOLE.",
		codiciParametri = ["CODICE_VISTO"],
		descrizioniParametri = ["Codice del visto/parere da verificare"])
	public boolean isTuttiVistiFavorevoli (def d) {
		String codiceVisto = getParametro (d, "CODICE_VISTO");
		return vistoParereService.tuttiVistiSono(d.visti, codiceVisto, StatoDocumento.CONCLUSO, [EsitoVisto.FAVOREVOLE, EsitoVisto.FAVOREVOLE_CON_PRESCRIZIONI]);
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Esiste un visto NON APPOSTO?",
		descrizione = "Ritorna TRUE se esiste almeno un visto con esito NON APPOSTO e stato CONCLUSO.",
		codiciParametri = ["CODICE_VISTO"],
		descrizioniParametri = ["Codice del visto/parere da verificare"])
	public boolean esisteVistoNonApposto (def d) {
		String codiceVisto = getParametro (d, "CODICE_VISTO");
		return vistoParereService.esisteAlmenoUnVisto(d.visti, codiceVisto, StatoDocumento.CONCLUSO, EsitoVisto.NON_APPOSTO);
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Esiste un visto con esito RIMANDA INDIETRO?",
		descrizione = "Ritorna TRUE se esiste almeno un visto con esito RIMANDA_INDIETRO e stato CONCLUSO.",
		codiciParametri = ["CODICE_VISTO"],
		descrizioniParametri = ["Codice del visto/parere da verificare"])
	public boolean esisteVistoRimandaIndietro (def d) {
		String codiceVisto = getParametro (d, "CODICE_VISTO");
		return vistoParereService.esisteAlmenoUnVisto(d.visti, codiceVisto, StatoDocumento.CONCLUSO, EsitoVisto.RIMANDA_INDIETRO);
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Esiste un visto CONTRARIO?",
		descrizione = "Ritorna TRUE se esiste almeno un visto con esito CONTRARIO e stato CONCLUSO.",
		codiciParametri = ["CODICE_VISTO"],
		descrizioniParametri = ["Codice del visto/parere da verificare"])
	public boolean esisteVistoContrario (def d) {
		String codiceVisto = getParametro (d, "CODICE_VISTO");
		return vistoParereService.esisteAlmenoUnVisto(d.visti, codiceVisto, StatoDocumento.CONCLUSO, EsitoVisto.CONTRARIO);
	}
	
	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Non esiste un visto CONTRARIO? (anche non concluso)",
		descrizione = "Ritorna TRUE se NON esiste almeno un visto con esito CONTRARIO.",
		codiciParametri = ["CODICE_VISTO"],
		descrizioniParametri = ["Codice del visto/parere da verificare"])
	public boolean nonEsisteUnVistoContrario (def d) {
		String codiceVisto = getParametro (d, "CODICE_VISTO");
		return !(vistoParereService.esisteAlmenoUnVisto(d.visti, codiceVisto, null, EsitoVisto.CONTRARIO));
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Ritorna TRUE se il firmatario del visto è lo stesso della proposta.",
		descrizione	= "Se il firmatario del visto è NULL, scatena un'eccezione. Se il visto non è presente, ritorna FALSE.",
		codiciParametri = ["CODICE_VISTO"],
		descrizioniParametri = ["Codice del visto/parere da verificare"])
	public boolean isFirmatarioVistoUgualeFirmatarioProposta (IProposta proposta) {
		// Recupero del parametro dalla tipologia
		String codiceVisto = ParametroTipologia.getValoreParametro (proposta.tipologiaDocumento, proposta.iter.stepCorrente.cfgStep, "CODICE_VISTO")

		// cerco il visto richiesto (mi aspetto di trovarne uno solo)
		VistoParere visto = vistoParereService.getVisto(proposta, codiceVisto);

		// se non trovo il visto, ritorno false.
		if (visto == null) {
			return false;
		}

		// se non ho il firmatario del visto, do errore
		Ad4Utente firmatarioVisto = visto.getSoggetto(TipoSoggetto.FIRMATARIO)?.utenteAd4;
		if (firmatarioVisto == null) {
			throw new AttiRuntimeException ("Non è possibile proseguire: il firmatario del visto ${visto.tipologia.titolo} è vuoto.")
		}

		// se non ho il firmatario della proposta, do errore
		Ad4Utente firmatarioProposta = proposta.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4;
		if (firmatarioProposta == null) {
			throw new AttiRuntimeException ("Non è possibile proseguire: manca il firmatario del documento principale.")
		}

		// ritorno TRUE se il firmatario del visto coincide con quello della proposta.
		return (firmatarioVisto.id == firmatarioProposta.id)
	}

	/*
	 * Condizione di sblocco
	 */
	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Tutti i visti sono favorevoli o uno è non favorevole.",
		descrizione = "Ritorna TRUE se tutti i visti con CODICE_VISTO hanno esito FAVOREVOLE oppure se ce ne è uno CONTRARIO, NON APPOSTO o RIMANDA INDIETRO",
		codiciParametri = ["CODICE_VISTO"],
		descrizioniParametri = ["Codice del visto/parere da verificare"])
	public boolean isTuttiVistiFavorevoliOUnoContrario (def d) {
		// lento e inefficiente ma facile da leggere:
		return esisteVistoContrario(d) || esisteVistoNonApposto(d) || esisteVistoRimandaIndietro(d) || isTuttiVistiFavorevoli(d);
	}
	
	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Esiste un visto FAVOREVOLE CON PRESCRIZIONI?",
		descrizione = "Ritorna TRUE se esiste almeno un visto con esito FAVOREVOLE CON PRESCRIZIONI e stato CONCLUSO.",
		codiciParametri = ["CODICE_VISTO"],
		descrizioniParametri = ["Codice del visto/parere da verificare"])
	public boolean esisteVistoFavorevoleConPrescrizioni (def d) {
		String codiceVisto = getParametro (d, "CODICE_VISTO");
		return vistoParereService.esisteAlmenoUnVisto(d.visti, codiceVisto, StatoDocumento.CONCLUSO, EsitoVisto.FAVOREVOLE_CON_PRESCRIZIONI);
	}
	
	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "Il Visto ha esito FAVOREVOLE CON PRESCRIZIONI?",
		descrizione = "Ritorna TRUE se l'esito è FAVOREVOLE CON PRESCRIZIONI")
	public boolean isEsitoFavorevoleConPrescrizioni (VistoParere vistoParere) {
		return (vistoParere.esito == EsitoVisto.FAVOREVOLE_CON_PRESCRIZIONI)
	}
	
	/*
	 * Metodi di utilità
	 */
	public String getParametro (IDocumento d, String codice) {
		return ParametroTipologia.getValoreParametro (d.tipologiaDocumento, d.iter.stepCorrente.cfgStep, codice)
	}
}
