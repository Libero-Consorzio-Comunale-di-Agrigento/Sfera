package it.finmatica.atti.impostazioni

import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAttore
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.so4.struttura.So4Amministrazione

/**
 * @author czappavigna
 *
 */
class RegolaCampo implements Serializable {
	private static final long serialVersionUID = 1L;

	// il tipo oggetto a cui questi campi/blocchi riferiscono
	WkfTipoOggetto tipoOggetto

	// il nome del blocco di campi (ad es. DATI_PROTOCOLLO può contenere vari dati come NUMERO_PROTOCOLLO, ANNO_PROTOCOLLO, ma da un certo cliente può contenere anche il campo UTENTE_FIRMATARIO)
	String blocco

	// il codice che identifica il campo sullo .zul (può essere anche un "tab" o qualsiasi altra cosa prevista dalla maschera)
	String campo

	WkfAttore wkfAttore

	boolean visibile 		= false
	boolean modificabile 	= false
	boolean invertiRegola 	= false
	boolean valido 			= true

	So4Amministrazione 	ente
	Ad4Utente 			utenteIns
	Ad4Utente 			utenteUpd
	Date 				dateCreated
	Date				lastUpdated
	Long 				version

	static mapping = {
		table 			'regole_campi'
		id 				column: 'id_regola_campo'

		valido 			type: 	'yes_no'
		visibile 		type: 	'yes_no'
		modificabile 	type: 	'yes_no'
		invertiRegola 	type: 	'yes_no'

		wkfAttore 		column: 'id_attore'
		tipoOggetto 	column: "tipo_oggetto"

		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
		ente 		column: 'ente'

	}

	static constraints = {
		ente	nullable: true
		campo	nullable: true
	}

	def beforeValidate () {
		utenteIns = utenteIns?:Holders.applicationContext.getBean('springSecurityService').currentUser
		utenteUpd = Holders.applicationContext.getBean('springSecurityService').currentUser
		ente	  = ente?:Holders.applicationContext.getBean('springSecurityService').principal.amministrazione
	}

	def beforeInsert () {
		utenteIns = Holders.applicationContext.getBean('springSecurityService').currentUser
		ente	  = Holders.applicationContext.getBean('springSecurityService').principal.amministrazione
	}

	def beforeUpdate () {
		utenteUpd = Holders.applicationContext.getBean('springSecurityService').currentUser
	}

	static hibernateFilters = {
		multiEnteFilter (condition: 'ente = :enteCorrente', types: 'string')
	}

}
