package it.finmatica.atti.documenti.viste

import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.documenti.ricerca.RicercaDocumento;
import it.finmatica.gestioneiter.motore.WkfStep
import it.finmatica.so4.struttura.So4Amministrazione
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class RicercaParere extends RicercaDocumento {
	
	String esito;
	Ad4Utente utenteFirmatario

	static mapping = {
		// mapping per questa domain
		table 			'ricerca_parere'

		// mapping comuni per la RicercaDocumento
		version false
		id 		generator: 'assigned', name: 'idDocumento', column: 'id_documento'

		ente 	column: 'ente'
		step	column: 'id_step'

		compLettura         type: 'yes_no'
		compModifica        type: 'yes_no'
		compCancellazione	type: 'yes_no'

		riservato 			type: 'yes_no'
		daInviareCorteConti	type: 'yes_no'
		conImpegnoSpesa		type: 'yes_no'
		attoConcluso		type: 'yes_no'

		utenteFirmatario	column: 'utente_firmatario'

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
		}

		utenteSoggetto 		column: 'utente_soggetto'
		stepUtente 			column: 'step_utente'
		stepRuolo  			column: 'step_ruolo'
		categoria			column: 'id_categoria'
		testo				column: 'id_testo'
		allegato			column: 'id_file_allegato'
        tipoBudget          column: 'id_tipo_budget'

		// campi aggiunti per la seconda numerazione: http://svi-redmine/issues/22205
		registroAtto2  		column: 'registro_atto_2'
		annoAtto2			column: 'anno_atto_2'
		numeroAtto2			column: 'numero_atto_2'
		dataNumeroAtto2		column: 'data_numero_atto_2'
	}

	static hibernateFilters = {
		multiEnteFilter (condition: 'ente = :enteCorrente', types: 'string')
	}
}
