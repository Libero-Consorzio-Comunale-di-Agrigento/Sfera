package it.finmatica.atti.odg.dizionari

import it.finmatica.ad4.autenticazione.Ad4Utente;

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders

/**
 * Rappresenta i possibili ruoli che i componenti hanno all'interno di un organo di controllo.
 * Esempio: capigruppo del partito XXX
 *
 * @author mfrancesconi
 *
 */
class OrganoControlloRuolo {

	String titolo
	String descrizione

	OrganoControllo organoControllo

	boolean valido = true
	Date 	validoDal
	Date	validoAl

	Date 		dateCreated
	Date 		lastUpdated
	Ad4Utente 	utenteIns
	Ad4Utente	utenteUpd

	static mapping = {
		table 			'organi_controllo_ruoli'
		id 				column: 'id_organo_controllo_ruolo'
		organoControllo column: 'id_organo_controllo'
		descrizione 	length: 4000

		valido 		type:   'yes_no'

		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
	}

	static constraints = {
		descrizione nullable: true
		validoAl	nullable: true
	}

	private SpringSecurityService getSpringSecurityService () {		return Holders.applicationContext.getBean("springSecurityService")    }

	def beforeValidate () {
		validoDal = validoDal?:new Date()
		utenteIns = utenteIns?:springSecurityService.currentUser
		utenteUpd = utenteUpd?:springSecurityService.currentUser
	}

	def beforeInsert () {
		validoAl  = valido? null : (validoAl?:new Date())
		validoDal = new Date()
		utenteIns = utenteIns?:springSecurityService.currentUser
		utenteUpd = utenteUpd?:springSecurityService.currentUser
	}

	def beforeUpdate () {
		validoAl  = valido? null : (validoAl?:new Date())
		utenteUpd = springSecurityService.currentUser
	}
}
