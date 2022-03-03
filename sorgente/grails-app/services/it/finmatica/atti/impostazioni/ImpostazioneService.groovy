package it.finmatica.atti.impostazioni

import it.finmatica.atti.dto.impostazioni.ImpostazioneDTO
import it.finmatica.atti.exceptions.AttiRuntimeException

class ImpostazioneService {

	ImpostazioneDTO salva(ImpostazioneDTO impostazioneDto) {
		Impostazione impostazione = Impostazione.getImpostazione (impostazioneDto.codice, impostazioneDto.ente).get()?:new Impostazione()
		if (impostazione.version != impostazioneDto.version) {
			throw new AttiRuntimeException(AttiRuntimeException.ERRORE_MODIFICA_CONCORRENTE)
		}

		impostazione.valore = impostazioneDto.valore
		impostazione.save ()

		return 	impostazione.toDTO()
	}

	void installaImpostazioniEnte (String codiceEnte, String codiceOttica) {
		Impostazione entiSo4 = Impostazione.getImpostazione(Impostazioni.ENTI_SO4.toString(), ImpostazioniMap.ENTE_FALLBACK).get()
		if (entiSo4 == null) {
			entiSo4 = new Impostazione ()
			entiSo4.codice          = Impostazioni.ENTI_SO4.toString()
			entiSo4.descrizione     = Impostazioni.ENTI_SO4.descrizione
			entiSo4.etichetta       = Impostazioni.ENTI_SO4.etichetta
			entiSo4.predefinito     = Impostazioni.ENTI_SO4.predefinito
			entiSo4.caratteristiche = Impostazioni.ENTI_SO4.caratteristiche
			entiSo4.modificabile    = Impostazioni.ENTI_SO4.modificabile
			entiSo4.ente			= ImpostazioniMap.ENTE_FALLBACK
		}
		if (entiSo4.valore?.length() > 0) {
			if (!entiSo4.valore.split(Impostazioni.SEPARATORE).contains(codiceEnte)) {
				entiSo4.valore += Impostazioni.SEPARATORE+codiceEnte
			}
		} else {
			entiSo4.valore = codiceEnte
		}
		entiSo4.save()

		// Aggiungo l'impostazione di aggiornamento in corso:
		Impostazione aggiornamentoInCorso = Impostazione.getImpostazione(Impostazioni.AGGIORNAMENTO_IN_CORSO.toString(), ImpostazioniMap.ENTE_FALLBACK).get()
		if (aggiornamentoInCorso == null) {
			aggiornamentoInCorso = new Impostazione ()
			aggiornamentoInCorso.codice          = Impostazioni.AGGIORNAMENTO_IN_CORSO.toString()
			aggiornamentoInCorso.descrizione     = Impostazioni.AGGIORNAMENTO_IN_CORSO.descrizione
			aggiornamentoInCorso.etichetta       = Impostazioni.AGGIORNAMENTO_IN_CORSO.etichetta
			aggiornamentoInCorso.predefinito     = Impostazioni.AGGIORNAMENTO_IN_CORSO.predefinito
			aggiornamentoInCorso.caratteristiche = Impostazioni.AGGIORNAMENTO_IN_CORSO.caratteristiche
			aggiornamentoInCorso.modificabile    = Impostazioni.AGGIORNAMENTO_IN_CORSO.modificabile
			aggiornamentoInCorso.ente			 = ImpostazioniMap.ENTE_FALLBACK
			aggiornamentoInCorso.valore 		 = "N"
			aggiornamentoInCorso.save()
		}

		// prima "aggiorno le impostazioni" che vengono tutte scritte su db
		aggiornaImpostazioni()

		// Aggiorno l'impostazione con le Risorse:
		Impostazione visMenu = Impostazione.getImpostazione(Impostazioni.VIS_MENU.toString(), codiceEnte).get()
		visMenu.risorsa = visMenu.predefinito.getBytes("UTF-8")
		visMenu.save()

		Impostazione otticaSo4 = Impostazione.getImpostazione(Impostazioni.OTTICA_SO4.toString(), codiceEnte).get()
		otticaSo4.valore = codiceOttica
		otticaSo4.save()
	}

	def aggiornaImpostazioni () {
		// prima elimino quelle che non ci sono più:
		def impostazioni = Impostazione.list()
		for (Impostazione i : impostazioni) {
			try {
				if (Impostazioni.valueOf(i.codice) == null) {
					log.info ("elimino l'impostazione: ${i.ente}: ${i.codice}")
					i.delete();
				}
			} catch (IllegalArgumentException e) {
				log.info ("elimino l'impostazione: ${i.ente}: ${i.codice}")
				i.delete();
			}
		}

		// poi controllo l'impostazione base "ENTI_SO4":
		Impostazione entiSo4 = Impostazione.getImpostazione(Impostazioni.ENTI_SO4.toString(), ImpostazioniMap.ENTE_FALLBACK).get();

		// aggiorno le impostazioni per ogni ente:
		def enti = entiSo4.valore.split(Impostazioni.SEPARATORE);

		for (String ente : enti) {
			for (Impostazioni i : Impostazioni.values()) {
				if (i == Impostazioni.ENTI_SO4 || i == Impostazioni.AGGIORNAMENTO_IN_CORSO)
					continue;

				upsert (i, ente);
			}

			// Il menù del visualizzatore NON va aggiornato!
//			Impostazione i = Impostazione.getImpostazione(Impostazioni.VIS_MENU.toString(), ente).findByRisorsaIsNotNull();
//			if (i != null) {
//				i.risorsa = i.predefinito.bytes;
//				i.save()
//			}
		}

		// Aggiungo l'impostazione di aggiornamento in corso:
		Impostazione aggiornamentoInCorso = Impostazione.getImpostazione(Impostazioni.AGGIORNAMENTO_IN_CORSO.toString(), ImpostazioniMap.ENTE_FALLBACK).get();
		if (aggiornamentoInCorso == null) {
			aggiornamentoInCorso = new Impostazione ();
			aggiornamentoInCorso.codice          = Impostazioni.AGGIORNAMENTO_IN_CORSO.toString()
			aggiornamentoInCorso.descrizione     = Impostazioni.AGGIORNAMENTO_IN_CORSO.descrizione
			aggiornamentoInCorso.etichetta       = Impostazioni.AGGIORNAMENTO_IN_CORSO.etichetta
			aggiornamentoInCorso.predefinito     = Impostazioni.AGGIORNAMENTO_IN_CORSO.predefinito
			aggiornamentoInCorso.caratteristiche = Impostazioni.AGGIORNAMENTO_IN_CORSO.caratteristiche
			aggiornamentoInCorso.modificabile    = Impostazioni.AGGIORNAMENTO_IN_CORSO.modificabile
			aggiornamentoInCorso.ente			 = ImpostazioniMap.ENTE_FALLBACK
			aggiornamentoInCorso.valore 		 = "N";
			aggiornamentoInCorso.save();
		}

		Impostazioni.map.refresh();
	}

	private Impostazione upsert (Impostazioni i, String codiceEnte) {
		Impostazione impostazione = Impostazione.getImpostazione(i.toString(), codiceEnte).get()

		if (impostazione == null) {
			impostazione 		= new Impostazione ();
			impostazione.codice = i.toString()
			impostazione.valore = i.predefinito
			impostazione.ente	= codiceEnte;
		}

		impostazione.descrizione     = i.descrizione
		impostazione.etichetta       = i.etichetta
		impostazione.predefinito     = i.predefinito
		impostazione.caratteristiche = i.caratteristiche
		impostazione.modificabile    = i.modificabile
		impostazione.save();

		return impostazione;
	}
}
