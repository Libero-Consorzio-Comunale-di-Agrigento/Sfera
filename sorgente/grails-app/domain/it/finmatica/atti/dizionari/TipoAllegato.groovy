package it.finmatica.atti.dizionari

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.gestionetesti.reporter.GestioneTestiModello;
import it.finmatica.so4.struttura.So4Amministrazione;

import java.util.Date;

class TipoAllegato {
	String 				titolo
	String 				descrizione
	boolean 			pubblicaCasaDiVetro = true
	boolean 			pubblicaAlbo		= true
	boolean 			pubblicaVisualizzatore = true
	So4Amministrazione 	ente
	boolean 			valido = true
	Date 				validoDal  // da valorizzare alla creazione del record
	Date 				validoAl   // deve essere valorizzato con la data di sistema quando valido = false
									// quando valido = true deve essere null
	Date 				dateCreated
	Ad4Utente 			utenteIns
	Date 				lastUpdated
	Ad4Utente 			utenteUpd

	String 				codice	// identifica il "tipo" di allegato: OMISSIS, SCHEDA_CONTABILE, ALLEGATO (per intendere un ALLEGATO GENERICO), FRONTESPIZIO etc.
	String 				codiceEsterno	// codice univoco che identifica la tipologia di allegato per integrazioni con altri applicativi (come il codice della tipologia di determina)
	GestioneTestiModello modelloTesto
	boolean 			modificabile = false // indica se il testo è editabile con edita-testo
	boolean				modificaCampi = true
	String 				tipologia
	StatoFirma 			statoFirma
	boolean				stampaUnica = false

	static mapping = {
		table 			'tipi_allegato'
		id 				column: 'id_tipo_allegato'
		ente 			column: 'ente'
		modelloTesto	column: 'id_modello_testo'
		valido 			type: 'yes_no'
		dateCreated 	column: 'data_ins'
		utenteIns 		column: 'utente_ins'
		lastUpdated 	column: 'data_upd'
		utenteUpd 		column: 'utente_upd'

		pubblicaAlbo		type: 'yes_no'
		pubblicaCasaDiVetro	type: 'yes_no'
		modificabile		type: 'yes_no'
		modificaCampi		type: 'yes_no'
		stampaUnica			type: 'yes_no'
		pubblicaVisualizzatore type: 'yes_no'
	}

	static constraints = {
		codiceEsterno	nullable: true
		descrizione		nullable: true
		validoAl 		nullable: true
		codice			nullable: true
		modelloTesto	nullable: true
		tipologia		nullable: true
		statoFirma		nullable: true
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
