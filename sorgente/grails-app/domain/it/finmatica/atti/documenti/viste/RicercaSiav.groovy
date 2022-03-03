package it.finmatica.atti.documenti.viste

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.documenti.ricerca.RicercaDocumento

class RicercaSiav implements Serializable {
	
	String codiceStruttura
	String descrizione
	String codiceSiav

	static mapping = {
		// mapping per questa domain
		table 				'GALILEO_SETTORI_LINK'
		id 					composite: ['codiceStruttura', 'codiceSiav']
		version 			false
		codiceStruttura		column: 'COD_ADS'
		codiceSiav			column: 'COD_SIAV'
	}

	static hibernateFilters = {
	}
}
