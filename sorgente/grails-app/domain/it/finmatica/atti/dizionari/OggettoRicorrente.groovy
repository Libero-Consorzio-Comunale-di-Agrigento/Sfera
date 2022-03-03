package it.finmatica.atti.dizionari

import it.finmatica.ad4.autenticazione.Ad4Utente;
import it.finmatica.so4.struttura.So4Amministrazione;

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders

class OggettoRicorrente {
	public static String TIPO_AFFIDAMENTO_IN_HOUSE = "AFFIDAMENTO IN HOUSE"

	String 	oggetto

	boolean delibera = true
	boolean determina = true

	boolean valido = true
	Date 	validoDal
	Date 	validoAl
	String 	codice

	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	  utenteIns
	Date 		lastUpdated
	Ad4Utente 	  utenteUpd

	// campi introdotti per l'attività #24611
	boolean cigObbligatorio = false
	String servizioFornitura
	String tipo
	String norma
	String modalita

	static mapping = {
		table 		'oggetti_ricorrenti'
		id 			column: 'id_oggetto_ricorrente'
		ente 		column: 'ente'
		oggetto 	length: 4000
		codice		column: 'codice'
		delibera	type: 'yes_no'
		determina	type: 'yes_no'
		valido 		type: 	'yes_no'
		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
		cigObbligatorio type: 'yes_no'
	}

	static constraints = {
		validoAl 			nullable: true
		servizioFornitura 	nullable: true
		tipo				nullable: true
		norma				nullable: true
		modalita			nullable: true
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
