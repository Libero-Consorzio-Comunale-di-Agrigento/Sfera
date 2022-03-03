package it.finmatica.atti.odg.dizionari

import it.finmatica.ad4.autenticazione.Ad4Utente;
import it.finmatica.so4.struttura.So4Amministrazione;

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders

/**
 * Rappresenta i possibili incarichi che un soggetto può assumere all'interno di una commissione.
 * Ad esempio: Sindaco, Assessore, Consigliere, etc.
 */
class Incarico {

	String titolo

	boolean valido = true

	So4Amministrazione ente
	Date 		dateCreated
	Date 		lastUpdated
	Ad4Utente 	utenteIns
	Ad4Utente 	utenteUpd

	static mapping = {
		table 	'odg_incarichi'
		id 		column: 'id_incarico'

		valido 		type: 	'yes_no'

		ente 		column: 'ente'
		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
	}

	private SpringSecurityService getSpringSecurityService () {		return Holders.applicationContext.getBean("springSecurityService")    }

	def beforeValidate () {
		utenteIns = utenteIns?:springSecurityService.currentUser
		ente	  = ente?:springSecurityService.principal.amministrazione
		utenteUpd = utenteUpd?:springSecurityService.currentUser
	}

	def beforeInsert () {
		utenteIns = utenteIns?:springSecurityService.currentUser
		utenteUpd = utenteUpd?:springSecurityService.currentUser
		ente	  = ente?:springSecurityService.principal.amministrazione
	}

	def beforeUpdate () {
		utenteUpd = springSecurityService.currentUser
	}

	static hibernateFilters = {
		multiEnteFilter (condition: 'ente = :enteCorrente', types: 'string')
	}
}
