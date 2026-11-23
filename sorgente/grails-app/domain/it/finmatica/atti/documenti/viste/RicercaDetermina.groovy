package it.finmatica.atti.documenti.viste

import it.finmatica.atti.dizionari.OggettoRicorrente
import it.finmatica.atti.documenti.ricerca.RicercaDocumento
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class RicercaDetermina extends RicercaDocumento {

	Long idTipoAllegatoDetermina
	Long idTipoAllegatoVisto
	// campo aggiunto per la ricerca dell'oggetto ricorrente: http://svi-redmine/issues/22462
	OggettoRicorrente oggettoRicorrente

	//campi richiesti da integrazione contabilita' ats pavia: https://redmine.finmatica.it/issues/69066
	boolean     inEvidenza = false
	Date 	    dataInEvidenzaDa
	Date 	    dataInEvidenzaA

	//campo richiesto da AO Croce Carle: https://redmine.finmatica.it/issues/90151
	BigDecimal  importoTotale

	//campi richiesti in ricerca servizio
	So4UnitaPubb	uoServizio
	String 			uoServizioDescrizione
	String 			uoServizioCodice

	static mapping = {
		// mapping per questa domain
		table 			'ricerca_determina'

		// mapping comuni per la RicercaDocumento
		version false
		id 		generator: 'assigned', name: 'idDocumento', column: 'id_documento'

		ente 	column: 'ente'
		step	column: 'id_step'

		compLettura          type: 'yes_no'
		compModifica         type: 'yes_no'
		compCancellazione	 type: 'yes_no'

		riservato 			 type: 'yes_no'
		controlloFunzionario type: 'yes_no'
		daInviareCorteConti	 type: 'yes_no'
		conImpegnoSpesa		 type: 'yes_no'
		attoConcluso		 type: 'yes_no'

		compUtente column: 'comp_utente'
		compRuolo  column: 'comp_ruolo'
		columns {
			compUnita {
				column name: 'comp_unita_progr'
				column name: 'comp_unita_dal'
				column name: 'comp_unita_ottica'
			}

			uoProponente {
				column name: 'uo_proponente_progr'
				column name: 'uo_proponente_dal'
				column name: 'uo_proponente_ottica'
			}

			stepUnita {
				column name: 'step_unita_progr'
				column name: 'step_unita_dal'
				column name: 'step_unita_ottica'
			}

			uoServizio {
				column name: 'uo_servizio_progr'
				column name: 'uo_servizio_dal'
				column name: 'uo_servizio_ottica'
			}
		}

		utenteSoggetto 		column: 'utente_soggetto'
		stepUtente 			column: 'step_utente'
		stepRuolo  			column: 'step_ruolo'
		categoria			column: 'id_categoria'
		testo				column: 'id_testo'
		allegato			column: 'id_file_allegato'
		impattoGenerazionale column: 'id_impatto_generazionale'

		// campi aggiunti per la seconda numerazione: http://svi-redmine/issues/22205
		registroAtto2  		column: 'registro_atto_2'
		annoAtto2			column: 'anno_atto_2'
		numeroAtto2			column: 'numero_atto_2'
		// campo aggiunto per la ricerca dell'oggetto ricorrente: http://svi-redmine/issues/22462
		oggettoRicorrente 	column: 'id_oggetto_ricorrente'
		dataNumeroAtto2		column: 'data_numero_atto_2'
        tipoBudget          column: 'id_tipo_budget'

		//campi aggiunti per ricerca dati contabili Ats-pavia: https://redmine.finmatica.it/issues/69066
		inEvidenza          type: 'yes_no'
		dataInEvidenzaDa    column: 'data_in_evidenza_da'
		dataInEvidenzaA     column: 'data_in_evidenza_a'
		importoTotale       column: 'importo_totale', scale: 2
	}

	static hibernateFilters = {
		multiEnteFilter (condition: 'ente = :enteCorrente', types: 'string')
	}
}