package it.finmatica.atti.dizionari

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.so4.struttura.So4Amministrazione

/**
 * Rappresenta le categorie delle deleghe ( Consiglieri, Assessori )
 * @author amarinelli
 *
 */
class CategoriaDelega {

	String 				titolo
	String 				descrizione

	boolean 			valido = true

	So4Amministrazione 	ente
	Date 				dateCreated
	Ad4Utente 	  		utenteIns
	Date 				lastUpdated
	Ad4Utente 	  		utenteUpd
	Date validoAl
	Date validoDal

	static mapping = {
		table 			'categorie_deleghe'
		id 				column: 'id_categoria_delega'
		ente 			column: 'ente'
		descrizione 	length: 4000
		valido 			type: 	'yes_no'
		dateCreated 	column: 'data_ins'
		utenteIns 		column: 'utente_ins'
		lastUpdated 	column: 'data_upd'
		utenteUpd 		column: 'utente_upd'
	}

	static constraints = {
		descrizione nullable: true
		validoAl 	nullable: true
	}

	private SpringSecurityService getSpringSecurityService () {		return Holders.applicationContext.getBean("springSecurityService")    }

	def beforeValidate () {
		validoDal 	= 	validoDal?:new Date()
		utenteIns = utenteIns?:springSecurityService.currentUser
		ente	  = ente?:springSecurityService.principal.amministrazione
		utenteUpd = springSecurityService.currentUser
	}

	def beforeInsert () {
		validoAl 	= 	valido?null:(validoAl?:new Date())
		validoDal 	= 	new Date()
		utenteIns = springSecurityService.currentUser
		utenteUpd = springSecurityService.currentUser
		ente	  = springSecurityService.principal.amministrazione
	}

	def beforeUpdate () {
		validoAl 	= 	valido?null:(validoAl?:new Date())
		utenteUpd = springSecurityService.currentUser
	}

	static hibernateFilters = {
		multiEnteFilter (condition: 'ente = :enteCorrente', types: 'string')
	}
}
