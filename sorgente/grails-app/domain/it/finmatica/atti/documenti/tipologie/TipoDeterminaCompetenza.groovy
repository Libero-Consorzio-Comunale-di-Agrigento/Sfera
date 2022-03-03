package it.finmatica.atti.documenti.tipologie

import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class TipoDeterminaCompetenza {

	TipoDetermina tipoDetermina

	boolean lettura			= false
	boolean modifica		= false
	boolean cancellazione	= false

	Ad4Utente utenteAd4
	Ad4Ruolo ruoloAd4
	So4UnitaPubb unitaSo4

	String titolo

	static mapping = {
		table 			'tipi_determina_competenze'
		id				column: 'id_tipi_determina_competenze'
		tipoDetermina	column: 'id_tipo_determina', index: 'tipdetcom_tipdet_fk'

		lettura			type: 'yes_no'
		modifica		type: 'yes_no'
		cancellazione	type: 'yes_no'
		utenteAd4		column: 'utente'
		ruoloAd4		column: 'ruolo'
		columns {
			unitaSo4 {
				column name: 'unita_progr'
				column name: 'unita_dal'
				column name: 'unita_ottica'
			}
		}
	}

	static constraints = {
		unitaSo4		nullable: true
		utenteAd4 		nullable: true
		ruoloAd4		nullable: true
	}
}
