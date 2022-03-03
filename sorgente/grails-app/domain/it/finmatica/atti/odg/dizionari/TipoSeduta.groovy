package it.finmatica.atti.odg.dizionari

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.so4.struttura.So4Amministrazione

/**
 * Rappresenta le possibili tipologie di una seduta (es: ordinaria, straordinaria,...)
 *
 * @author mfrancesconi
 */
class TipoSeduta {
	String	titolo
	String	descrizione
	int 	sequenza  // indica l'ordine da mostrare nelle liste scelte

	boolean valido = true
	Date 	validoDal  // da valorizzare alla creazione del record
	Date 	validoAl   // deve essere valorizzato con la data di sistema quando valido = false
					   // quando valido = true deve essere null

	So4Amministrazione ente
	Date 		dateCreated
	Date 		lastUpdated
	Ad4Utente 	utenteIns
	Ad4Utente 	utenteUpd

	static mapping = {
		table 				'odg_tipi_seduta'
		id 			column: 'id_tipo_seduta'
		ente 		column: 'ente'
		descrizione length: 4000
		valido 		type: 	'yes_no'
		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
	}

    static constraints = {
		descrizione nullable: true
		validoAl 	nullable: true
    }

	private SpringSecurityService getSpringSecurityService () {
		return Holders.applicationContext.getBean("springSecurityService")
	}

	def beforeValidate() {
		validoDal	= validoDal?:new Date()
		utenteIns	= utenteIns?:springSecurityService.currentUser
		utenteUpd	= utenteUpd?:springSecurityService.currentUser
		ente		= ente?:springSecurityService.principal.amministrazione
	}

	def beforeInsert() {
		validoAl	= valido ? null : (validoAl?:new Date())
		validoDal	= new Date()
		utenteIns	= utenteIns?:springSecurityService.currentUser
		utenteUpd	= utenteUpd?:springSecurityService.currentUser
		ente		= ente?:springSecurityService.principal.amministrazione
	}

	def beforeUpdate() {
		validoAl	=	valido ? null : (validoAl?:new Date())
		utenteUpd	=	springSecurityService.currentUser
	}

	static hibernateFilters = {
		multiEnteFilter (condition: 'ente = :enteCorrente', type: 'string')
	}
}
