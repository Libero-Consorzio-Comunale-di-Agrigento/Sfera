package it.finmatica.atti.odg.dizionari

import it.finmatica.ad4.autenticazione.Ad4Utente;
import java.util.Date;

/**
 * Rappresentano le tipologie di esiti possibili. Non è gestito da interfaccia
 *
 * @author mfrancesconi
 *
 */
class EsitoStandard {

	public static final transient String ADOTTATO		   = "ADOTTATO"
	public static final transient String RINVIO_UFFICIO    = "RINVIO_UFFICIO"
	public static final transient String DA_RATIFICARE     = "DA_RATIFICARE"
	public static final transient String PARZIALE          = "PARZIALE"
	public static final transient String NON_ADOTTATO      = "NON_ADOTTATO"
	public static final transient String INVIA_COMMISSIONE = "INVIA_COMMISSIONE"
	public static final transient String CONCLUSO		   = "CONCLUSO"

	String  codice
	String  titolo

	boolean creaDelibera   	= false
	boolean prossimaSeduta 	= false
	boolean determina 		= false

	static mapping = {
		table 		'odg_esiti_standard'
		id 			column: 'esito_standard', name:'codice', generator: 'assigned'
		codice 		column: "esito_standard"

		creaDelibera 	type: 'yes_no'
		prossimaSeduta 	type: 'yes_no'
		determina	 	type: 'yes_no'

		version false
	}
}
