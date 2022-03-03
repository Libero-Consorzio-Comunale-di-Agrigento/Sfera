package it.finmatica.atti.dizionari

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.so4.struttura.So4Amministrazione

/**
 * Rappresenta i aggiuntivi
 * @author czappavigna
 *
 */
class TipoDatoAggiuntivoValore {

	// il codice del tipo di dato aggiuntivo
	String 	codice

	// la descrizione del valore di questo dato aggiuntivo
	String 	descrizione

	// il codice del tipo di oggetto per cui questo valore è disponibile.
	String 	tipoOggetto

	int 	sequenza = 1

	boolean valido = true
	Date 	validoDal
	Date 	validoAl

	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	  utenteIns
	Date 		lastUpdated
	Ad4Utente 	  utenteUpd

	static mapping = {
		table 		'tipi_dati_aggiuntivi_valori'
		id 			column: 'id_tipo_dato_aggiuntivo_valore'
		ente 		column: 'ente'
		descrizione length: 4000
		valido 		type: 	'yes_no'
		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
	}

	static constraints = {
		descrizione nullable: true
		validoAl 	nullable: true
	}

	private SpringSecurityService getSpringSecurityService () {
		return Holders.getApplicationContext().getBean("springSecurityService")
	}

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
