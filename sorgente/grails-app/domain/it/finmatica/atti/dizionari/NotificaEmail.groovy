package it.finmatica.atti.dizionari

import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders

/**
 */
class NotificaEmail {

	// indirizzo email a cui spedire la notifica.
	Email 			email

	// eventuale nome della funzione da invocare
	String 			funzione

	// eventuale unità a cui inviare la notifica
	So4UnitaPubb	unita

	// eventuale ruolo a cui inviare la notifica
	Ad4Ruolo 		ruolo

	// eventuale soggetto a cui inviare la notifica
	As4SoggettoCorrente soggetto

	boolean valido = true
	Date validoDal  // da valorizzare alla creazione del record
	Date validoAl   // deve essere valorizzato con la data di sistema quando valido = false
					// quando valido = true deve essere null
	Date dateCreated
	Ad4Utente utenteIns
	Date lastUpdated
	Ad4Utente utenteUpd

	Notifica notifica
	static belongsTo = [notifica : Notifica]

	static mapping = {
		table 		'notifiche_email'
		id 			column: 'id_notifica_email'
		notifica 	column: 'id_notifica'
		email 		column: 'id_email'

		soggetto	column: 'id_soggetto'
		ruolo		column: 'ruolo'
		columns {
			unita {
				column name: 'unita_progr'
				column name: 'unita_dal'
				column name: 'unita_ottica'
			}
		}

		valido 		type: 	'yes_no'
		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
	}

	static constraints = {
		validoAl 	nullable: true
		unita		nullable: true
		email		nullable: true
		ruolo		nullable: true
		soggetto	nullable: true
		funzione	nullable: true
	}

	private SpringSecurityService getSpringSecurityService () {		return Holders.applicationContext.getBean("springSecurityService")    }

	def beforeValidate() {
		validoDal 	= 	validoDal?:new Date()
		utenteIns	=	utenteIns?:springSecurityService.currentUser
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}

	def beforeInsert() {
		validoAl 	= 	valido?null:(validoAl?:new Date())
		validoDal 	= 	new Date()
		utenteIns	=	utenteIns?:springSecurityService.currentUser
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}

	def beforeUpdate() {
		validoAl 	= 	valido?null:(validoAl?:new Date())
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}
}
