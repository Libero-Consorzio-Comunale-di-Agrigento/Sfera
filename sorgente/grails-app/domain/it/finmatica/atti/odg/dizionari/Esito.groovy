package it.finmatica.atti.odg.dizionari

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.odg.Commissione
import it.finmatica.so4.struttura.So4Amministrazione

/**
 * Rappresenta gli esiti possibili che si possono assegnare ad una proposta discussa in commissione.
 *
 * Un esito potrà essere associato ad una commissione.
 * Se ciò accade allora l’esito sarà disponibile solo nelle sedute di questa commissione, se invece questo legame non viene definito allora l’esito sarà disponibile sempre.
 *
 * Se l’esito è di tipo “invia a commissione” allora l’esito sarà obbligatoriamente associato ad una commissione di partenza e di arrivo
 *
 * @author mfrancesconi
 *
 */
class Esito {

	String 	titolo
	String 	descrizione

	EsitoStandard	esitoStandard
	Commissione 	commissione
	Commissione 	commissioneArrivo
	Long 			progressivoCfgIter	// se valorizzato, ha precedenza sull'iter specificato in commissione.

	boolean notificaVerbalizzazione
	boolean testoAutomatico
	boolean gestioneEsecutivita
	int 	sequenza // indica l'ordine da mostrare nelle liste scelte

	boolean valido = true
	Date validoDal  // da valorizzare alla creazione del record
	Date validoAl   // deve essere valorizzato con la data di sistema quando valido = false
					// quando valido = true deve essere null

	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd
	TipoRegistro registroDelibera

	static mapping = {
		table 'odg_esiti'
		id column: 'id_esito'
		descrizione 		length: 4000

		esitoStandard 		column: 'esito_standard'
		commissione 		column: 'id_commissione'
		commissioneArrivo 	column: 'id_commissione_arrivo'

		notificaVerbalizzazione	type: 'yes_no'
		testoAutomatico 		type: 'yes_no'
		gestioneEsecutivita 	type: 'yes_no'
		valido 					type: 'yes_no'

		ente 				column: 'ente'
		dateCreated 		column: 'data_ins'
		utenteIns 			column: 'utente_ins'
		lastUpdated 		column: 'data_upd'
		utenteUpd 			column: 'utente_upd'
		registroDelibera	column: 'id_registro_delibera'
	}

    static constraints = {
		commissione 		nullable: true
		commissioneArrivo 	nullable: true
		descrizione 		nullable: true
		progressivoCfgIter	nullable: true
		validoAl 			nullable: true
		registroDelibera	nullable: true
    }

	private SpringSecurityService getSpringSecurityService () {
		return Holders.getApplicationContext().getBean("springSecurityService")
	}

	def beforeValidate () {
		validoDal = validoDal?:new Date()
		utenteIns = utenteIns?:springSecurityService.currentUser
		ente	  = ente?:springSecurityService.principal.amministrazione
		utenteUpd = utenteUpd?:springSecurityService.currentUser
	}

	def beforeInsert () {
		validoAl  = valido? null : (validoAl?:new Date())
		validoDal = new Date()
		utenteIns = utenteIns?:springSecurityService.currentUser
		utenteUpd = utenteUpd?:springSecurityService.currentUser
		ente	  = ente?:springSecurityService.principal.amministrazione
	}

	def beforeUpdate () {
		validoAl  = valido? null : (validoAl?:new Date())
		utenteUpd = springSecurityService.currentUser
	}

	static hibernateFilters = {
		multiEnteFilter (condition: 'ente = :enteCorrente', type:'string')
	}
}
