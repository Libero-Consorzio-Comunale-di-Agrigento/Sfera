package it.finmatica.atti.dizionari

import it.finmatica.ad4.autenticazione.Ad4Utente;
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.so4.struttura.So4Amministrazione;

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders

class Delega {
	As4SoggettoCorrente 	assessore
	So4Amministrazione 		ente
	String 					descrizioneAssessorato
	boolean 				valido = true
	Date 					validoDal
	Date 					validoAl
	Date 					dateCreated
	Ad4Utente 				utenteIns
	Date 					lastUpdated
	Ad4Utente 				utenteUpd
	int 					sequenza = 1
	Long 					idDelegaStorico

	static mapping = {
		table 		'deleghe'
		id 			column: 'id_delega'
		assessore 	column: 'ni_assessore'
		ente 		column: 'ente'
		valido 		type: 'yes_no'
		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
		idDelegaStorico column: 'id_delega_storico'
	}

	static constraints = {
		validoAl 		nullable: true
		idDelegaStorico nullable: true
	}

	private SpringSecurityService getSpringSecurityService () {		return Holders.applicationContext.getBean("springSecurityService")    }

	def beforeValidate () {
		validoDal = validoDal?:new Date()
		utenteIns = utenteIns?:springSecurityService.currentUser
		ente	  = ente?:springSecurityService.principal.amministrazione
		utenteUpd = springSecurityService.currentUser
	}

	def beforeInsert () {
		validoAl  = valido ? null : (validoAl?:new Date())
		validoDal = new Date()
		utenteIns = springSecurityService.currentUser
		utenteUpd = springSecurityService.currentUser
		ente	  = springSecurityService.principal.amministrazione
	}

	def beforeUpdate () {
		validoAl  = valido ? null : (validoAl?:new Date())
		utenteUpd = springSecurityService.currentUser
	}

	static hibernateFilters = {
		multiEnteFilter (condition: 'ente = :enteCorrente', types: 'string')
	}

}
