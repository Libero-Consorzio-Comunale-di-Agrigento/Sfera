package it.finmatica.atti.dizionari

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.so4.struttura.So4Amministrazione

class ImpattoGenerazionale {
	String titolo
	String descrizione
	String frase

	boolean delibera = true
	boolean determina = true

	boolean valido = true
	Date 	validoDal
	Date 	validoAl

	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	  utenteIns
	Date 		lastUpdated
	Ad4Utente 	  utenteUpd



	static mapping = {
		table 		'impatti_generazionali'
		id 			column: 'id_impatto_generazionale'
		ente 		column: 'ente'

		descrizione	column: 'descrizione'
		descrizione	length: 4000
		titolo		column: 'titolo'
		titolo		length: 255
		frase		column: 'frase'
		frase		length: 4000
		delibera	type: 'yes_no'
		determina	type: 'yes_no'
		valido 		type: 	'yes_no'
		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
	}

	static constraints = {
		validoAl 			nullable: true
	}


	private SpringSecurityService getSpringSecurityService () {		return Holders.applicationContext.getBean("springSecurityService")    }

	def beforeValidate () {
		validoDal = validoDal?:new Date()
		utenteIns = utenteIns?:springSecurityService.currentUser
		ente	  = ente?:springSecurityService.principal.amministrazione
		utenteUpd = springSecurityService.currentUser
	}

	def beforeInsert () {
		validoAl = valido ? null : (validoAl?:new Date())
		validoDal = new Date()
		utenteIns = springSecurityService.currentUser
		utenteUpd = springSecurityService.currentUser
		ente	  = springSecurityService.principal.amministrazione
	}

	def beforeUpdate () {
		validoAl = valido ? null : (validoAl?:new Date())
		utenteUpd = springSecurityService.currentUser
	}

	static hibernateFilters = {
		multiEnteFilter (condition: 'ente = :enteCorrente', types: 'string')
	}
}
