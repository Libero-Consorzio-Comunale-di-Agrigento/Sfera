package it.finmatica.atti.impostazioni

import groovy.sql.Sql
import it.finmatica.atti.dto.impostazioni.ImpostazioneDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.gestioneiter.impostazioni.WkfImpostazione

import javax.sql.DataSource

class ImpostazioneService {

	DataSource dataSource

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
		// la vista IMPOSTAZIONI mostra solo gli enti presenti in WKF_IMPOSTAZIONI.ENTI_SO4: va registrato prima di scrivere le impostazioni
		WkfImpostazione wkfEntiSo4 = WkfImpostazione.findByCodiceAndEnte(Impostazioni.ENTI_SO4.toString(), ImpostazioniMap.ENTE_FALLBACK)
		if (wkfEntiSo4 == null) {
			new WkfImpostazione(ente: ImpostazioniMap.ENTE_FALLBACK
							  , codice: Impostazioni.ENTI_SO4.toString()
							  , valore: codiceEnte
							  , predefinito: "FINMATICA"
							  , descrizione: Impostazioni.ENTI_SO4.descrizione
							  , etichetta: Impostazioni.ENTI_SO4.etichetta).save()
		} else if (!wkfEntiSo4.valore.split(Impostazioni.SEPARATORE).contains(codiceEnte)) {
			wkfEntiSo4.valore += Impostazioni.SEPARATORE + codiceEnte
			wkfEntiSo4.save()
		}

		Impostazione entiSo4 = Impostazione.getImpostazione(Impostazioni.ENTI_SO4.toString(), ImpostazioniMap.ENTE_FALLBACK).get()
		if (entiSo4 == null) {
			scriviImpostazioneFallback(Impostazioni.ENTI_SO4, codiceEnte, codiceEnte)
		} else if (!entiSo4.valore?.split(Impostazioni.SEPARATORE)?.contains(codiceEnte)) {
			entiSo4.valore = entiSo4.valore ? entiSo4.valore + Impostazioni.SEPARATORE + codiceEnte : codiceEnte
			entiSo4.save()
		}

		// Aggiungo l'impostazione di aggiornamento in corso:
		Impostazione aggiornamentoInCorso = Impostazione.getImpostazione(Impostazioni.AGGIORNAMENTO_IN_CORSO.toString(), ImpostazioniMap.ENTE_FALLBACK).get()
		if (aggiornamentoInCorso == null) {
			scriviImpostazioneFallback(Impostazioni.AGGIORNAMENTO_IN_CORSO, "N", codiceEnte)
		}

		// prima "aggiorno le impostazioni" che vengono tutte scritte su db
		aggiornaImpostazioni(codiceEnte)

		// Aggiorno l'impostazione con le Risorse:
		// in installazione la vista IMPOSTAZIONI potrebbe non restituirle anche se già salvate: le cerco per id (sessione hibernate)
		Impostazione visMenu = Impostazione.getImpostazione(Impostazioni.VIS_MENU.toString(), codiceEnte).get() ?: Impostazione.get(new Impostazione(codice: Impostazioni.VIS_MENU.toString(), ente: codiceEnte))
		visMenu.risorsa = visMenu.predefinito.getBytes("UTF-8")
		visMenu.save()

		Impostazione otticaSo4 = Impostazione.getImpostazione(Impostazioni.OTTICA_SO4.toString(), codiceEnte).get() ?: Impostazione.get(new Impostazione(codice: Impostazioni.OTTICA_SO4.toString(), ente: codiceEnte))
		otticaSo4.valore = codiceOttica
		otticaSo4.save()
	}

	/**
	 * Scrive su AD4_REGISTRO un'impostazione di fallback (ente '*') sotto la chiave dell'ente installato,
	 * come lo script di migrazione 84: la vista IMPOSTAZIONI mostra solo le chiavi degli enti presenti in WKF_IMPOSTAZIONI.ENTI_SO4,
	 * mentre scrivendo tramite la vista il trigger creerebbe la chiave PRODUCTS/*.
	 */
	private void scriviImpostazioneFallback (Impostazioni i, String valore, String codiceEnte) {
		String chiave = "PRODUCTS/${codiceEnte}/AGSDE2/AGSDE2/IMPOSTAZIONI/${i.toString()}".toString()
		def stringhe = ['ENTE'           : ImpostazioniMap.ENTE_FALLBACK
					  , '(Predefinito)'  : i.predefinito
					  , 'CARATTERISTICHE': i.caratteristiche
					  , 'DESCRIZIONE'    : i.descrizione
					  , 'ETICHETTA'      : i.etichetta
					  , 'MODIFICABILE'   : i.modificabile ? 'Y' : 'N'
					  , 'VALORE'         : valore]

		Sql sql = new Sql(dataSource)
		for (def s : stringhe) {
			sql.call("begin ad4_registro_utility.scrivi_stringa(?, ?, ?, '', false); end;", [chiave, s.key, s.value])
		}
	}

	def aggiornaImpostazioni (String codiceEnte = null) {
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
		// in installazione ENTI_SO4 potrebbe non essere ancora visibile dalla vista IMPOSTAZIONI: uso l'ente passato
		def enti = []
		if (entiSo4 != null) {
			enti = entiSo4.valore.split(Impostazioni.SEPARATORE);
		} else if (codiceEnte) {
			enti = [codiceEnte]
		}

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
			// in installazione la vista IMPOSTAZIONI potrebbe non restituirla anche se già salvata: la cerco per id (sessione hibernate)
			aggiornamentoInCorso = Impostazione.get(new Impostazione(codice: Impostazioni.AGGIORNAMENTO_IN_CORSO.toString(), ente: ImpostazioniMap.ENTE_FALLBACK))
		}
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
