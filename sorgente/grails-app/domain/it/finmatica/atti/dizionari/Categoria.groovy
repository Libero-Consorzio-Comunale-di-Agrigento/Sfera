package it.finmatica.atti.dizionari

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.so4.struttura.So4Amministrazione
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders

/**
 * Rappresenta i codici consip utilizzabili dalle determine
 * @author mfrancesconi
 *
 */
class Categoria {
	public static final transient TIPO_OGGETTO_DETERMINA			= "DETERMINA"
	public static final transient TIPO_OGGETTO_PROPOSTA_DELIBERA	= "PROPOSTA_DELIBERA"

	String 				codice
	String 				descrizione
	int 				sequenza = 1
	String 				tipoOggetto

	boolean 			valido = true
	boolean 			controlloCdv = true
	Date 				validoDal
	Date 				validoAl

	So4Amministrazione 	ente
	Date 				dateCreated
	Ad4Utente 	  		utenteIns
	Date 				lastUpdated
	Ad4Utente 	  		utenteUpd

	static mapping = {
		table 			'categorie'
		id 				column: 'id_categoria'
		ente 			column: 'ente'
		descrizione 	length: 4000
		valido 			type: 	'yes_no'
		controlloCdv 	type: 	'yes_no'
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
