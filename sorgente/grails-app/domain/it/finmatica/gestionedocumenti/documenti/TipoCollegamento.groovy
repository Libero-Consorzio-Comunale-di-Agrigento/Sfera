package it.finmatica.gestionedocumenti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.so4.struttura.So4Amministrazione

class TipoCollegamento {

    // definito da java per identificare il tipo di collegamento
	String codice

	String descrizione
	String commento

	So4Amministrazione ente
	boolean            valido = true
	Date               dateCreated
	Ad4Utente          utenteIns
	Date               lastUpdated
	Ad4Utente          utenteUpd

	static mapping = {
		table 		'gdo_tipi_collegamento'
        id          column: 'id_tipo_collegamento'
		codice 		column: 'tipo_collegamento',  index: 'gdo_tico_idx'
		commento 	length: 4000
		version		false

		ente  column: 'ente'
		dateCreated 	column: 'data_ins'
		utenteIns 		column: 'utente_ins'
		lastUpdated 	column: 'data_upd'
		utenteUpd 		column: 'utente_upd'
		valido          type: 'yes_no'
	}

	static constraints = {
		commento    nullable: true
        codice      unique: 'ente'
	}

	SpringSecurityService getSpringSecurityService () {
		return Holders.getApplicationContext().getBean("springSecurityService")
	}

	def beforeValidate () {
		utenteIns	= utenteIns?:getSpringSecurityService().currentUser
		utenteUpd	= utenteUpd?:getSpringSecurityService().currentUser
		ente        = ente?:springSecurityService.principal.amministrazione
	}

	def beforeInsert () {
		utenteIns	= utenteIns?:getSpringSecurityService().currentUser
		utenteUpd	= utenteUpd?:getSpringSecurityService().currentUser
		ente        = ente?:springSecurityService.principal.amministrazione
	}

	def beforeUpdate () {
		utenteUpd	=	utenteUpd?:getSpringSecurityService().currentUser
	}
}
