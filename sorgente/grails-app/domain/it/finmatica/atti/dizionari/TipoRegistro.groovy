package it.finmatica.atti.dizionari

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.so4.struttura.So4Amministrazione

/**
 * Rappresenta le tipologie di registro che si possono utilizzare per la numerazione delle proposte e degli atti
 *
 * @author mfrancesconi
 *
 */
class TipoRegistro {

	String id
	String codice
	String descrizione

	// indica se deve rinnovarsi automaticamente ogni anno
	boolean automatico

	// indica se il registro annuale va chiuso automaticamente al rinnovo automatico del nuovo anno.
	boolean chiusuraAutomatica

	String registroEsterno // codice da usare per integrazione (es contabilità)

	// FIXME: questi dati non so se servono ancora (alla luce della relazione Registro->TipoRegistro)
	boolean valido = true
	Date validoDal  // da valorizzare alla creazione del record
	Date validoAl   // deve essere valorizzato con la data di sistema quando valido = false
					// quando valido = true deve essere null

	boolean delibera  		= true
	boolean determina 		= true

	// indica se il registro deve essere disponibile per la ricerca nel visualizzatore.
	boolean visualizzatore 	= true
	boolean paginaUnica = true

	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd

	static mapping = {
		table 					'tipi_registro'
		id 				column: 'tipo_registro', name: 'codice', generator: 'assigned'
		codice			column: 'tipo_registro'

		registroEsterno column: 'registro_esterno'

		descrizione 		length: 4000
		valido 				type:   'yes_no'
		automatico			type:   'yes_no'
		chiusuraAutomatica	type:   'yes_no'

		delibera		type:   'yes_no'
		determina		type:   'yes_no'
		visualizzatore  type:   'yes_no'
		paginaUnica		type:   'yes_no'

		ente 			column: 'ente'
		dateCreated 	column: 'data_ins'
		utenteIns 		column: 'utente_ins'
		lastUpdated 	column: 'data_upd'
		utenteUpd 		column: 'utente_upd'
	}

    static constraints = {
		descrizione 		nullable: true
		validoAl 			nullable: true
		registroEsterno		nullable: true
    }

	private SpringSecurityService getSpringSecurityService () {
		return Holders.applicationContext.getBean("springSecurityService")
	}

	def beforeValidate() {
		validoDal 	= 	validoDal?:new Date()
		utenteIns	=	utenteIns?:springSecurityService.currentUser
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
		ente		=	ente?:springSecurityService.principal.amministrazione
	}

	def beforeInsert() {
		validoAl 	= 	valido?null:(validoAl?:new Date())
		validoDal 	= 	new Date()
		utenteIns	=	utenteIns?:springSecurityService.currentUser
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
		ente		=	ente?:springSecurityService.principal.amministrazione
	}

	def beforeUpdate() {
		validoAl 	= 	valido?null:(validoAl?:new Date())
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}

	static hibernateFilters = {
		multiEnteFilter (condition: 'ente = :enteCorrente', type:'string')
	}
}
