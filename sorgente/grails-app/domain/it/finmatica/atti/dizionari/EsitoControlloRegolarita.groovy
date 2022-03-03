package it.finmatica.atti.dizionari

import it.finmatica.ad4.autenticazione.Ad4Utente;
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.so4.struttura.So4Amministrazione;

/**
 * Rappresenta gli esiti possibili che si possono assegnare ad un atto in fase di controllo di regolarità.
 *
 * @author czappavigna
 *
 */
class EsitoControlloRegolarita {

	String 	titolo
	String 	descrizione

	String ambito

	int 	sequenza // indica l'ordine da mostrare nelle liste scelte

	boolean valido = true

	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd

	static mapping = {
		table 'tipi_esiti_controllo_reg'
		id column: 'id_esito_controllo_reg'
		descrizione length: 4000

		valido 		type: 'yes_no'

		ente 		column: 'ente'
		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
	}

    static constraints = {
		descrizione 		nullable: true
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
		multiEnteFilter (condition: 'ente = :enteCorrente', type:'string')
	}
}
