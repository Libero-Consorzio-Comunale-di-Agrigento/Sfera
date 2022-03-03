package it.finmatica.atti.odg.dizionari

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.odg.Commissione
import it.finmatica.so4.struttura.So4Amministrazione
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders

class ImportoGettone {

	BigDecimal 		importo
	Commissione commissione

	boolean valido = true
	Date validoDal  // da valorizzare alla creazione del record
	Date validoAl   // deve essere valorizzato con la data di sistema quando valido = false
					// quando valido = true deve essere null

	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd

	static mapping = {
		table 		'odg_importi_gettone'
		id 			column: 'id_importo_gettone'

		commissione column: 'id_commissione'

		valido 		type:   'yes_no'
		ente 		column: 'ente'
		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
	}

    static constraints = {
		commissione 		nullable: true
		validoAl 			nullable: true
    }

	private SpringSecurityService getSpringSecurityService () {		return Holders.applicationContext.getBean("springSecurityService")    }

	def beforeValidate () {
//		validoDal = validoDal?:new Date()
		utenteIns = utenteIns?:springSecurityService.currentUser
		ente	  = ente?:springSecurityService.principal.amministrazione
		utenteUpd = utenteUpd?:springSecurityService.currentUser
	}

	def beforeInsert () {
//		validoAl  = valido? null : (validoAl?:new Date())
//		validoDal = new Date()
		utenteIns = utenteIns?:springSecurityService.currentUser
		utenteUpd = utenteUpd?:springSecurityService.currentUser
		ente	  = ente?:springSecurityService.principal.amministrazione
	}

	def beforeUpdate () {
//		validoAl  = valido? null : (validoAl?:new Date())
		utenteUpd = springSecurityService.currentUser
	}

	static hibernateFilters = {
		multiEnteFilter (condition: "ente = :enteCorrente", type:'string')
	}
}
