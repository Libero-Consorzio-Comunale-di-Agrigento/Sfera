package it.finmatica.atti.odg

import it.finmatica.ad4.autenticazione.Ad4Utente;
import it.finmatica.atti.dizionari.Notifica;
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
/**
 * Rappresenta una notifica inviata per una seduta o per un oggetto_seduta
 *
 * @author mfrancesconi
 *
 */
class SedutaNotifica {
	Notifica 		notifica
	Seduta 			seduta
	OggettoSeduta 	oggettoSeduta
	String 			indirizziEmail  // elenco di indirizzi separati da ,

	Date 		dataInvio
	Ad4Utente 	utenteInvio

	static mapping = {
		table 			'odg_sedute_notifiche'
		id 				column: 'id_seduta_notifica'
		notifica 		column: 'id_notifica', 			index: 'odgsednot_not_fk'
		seduta 			column: 'id_seduta', 			index: 'odgsednot_odgsed_fk'
		indirizziEmail 	sqlType: 'Clob'
		oggettoSeduta 	column: 'id_oggetto_seduta', 	index: 'odgsed_odgoggsed_fk'
		utenteInvio 	column: 'utente_invio'
	}

	static constraints = {
		oggettoSeduta nullable: true
	}

	private SpringSecurityService getSpringSecurityService () {		return Holders.applicationContext.getBean("springSecurityService")    }

	def beforeValidate() {
		utenteInvio	=	utenteInvio?:springSecurityService.currentUser
	}

	def beforeInsert() {
		utenteInvio	=	utenteInvio?:springSecurityService.currentUser
	}

	def beforeUpdate() {
		utenteInvio	=	utenteInvio?:springSecurityService.currentUser
	}

}
