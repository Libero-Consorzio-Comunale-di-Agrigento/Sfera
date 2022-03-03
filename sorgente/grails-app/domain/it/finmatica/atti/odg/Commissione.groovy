package it.finmatica.atti.odg

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.so4.struttura.So4Amministrazione

/**
 * Rappresenta una commissione dell'odg
 * @author mfrancesconi
 *
 */
class Commissione {

	String titolo
	String descrizione

	TipoRegistro tipoRegistroSeduta		// registro da utilizzare per la numerazione delle sedute
	TipoRegistro tipoRegistro 			// registro da utilizzare per la numerazione delle delibere
	Long 	 	 progressivoCfgIter 	// progressivo dell'iter da associare alle delibere create
	Ad4Ruolo 	 ruoloCompetenze		// indica il ruolo SO4 che ha diritto di gestione sulla seduta di questa commissione
	Ad4Ruolo 	 ruoloVisualizza		// indica il ruolo SO4 che ha diritto di visualizzare tutti i dati delle sedute su AGSVIS

	boolean secondaConvocazione = false // indica se la commissione prevede una seconda convocazione
	boolean sedutaPubblica		= true  // indica se la seduta è pubblica o no. Se è pubblica e se è pubblicaWeb=true, allora anche gli utenti anonimi potranno visualizzare la seduta dal visualizzatore.
	boolean pubblicaWeb 		= true  // indica se le sedute della commissione devono essere visibili dal visualizzatore
	boolean ruoliObbligatori    = true  // indica se i ruoli PRESIDENTE e SEGRETARIO siano obbligatori
	boolean votoPresidente      = true  // indica se il PRESIDENTE deve poter votare
	boolean controlloFirmatari  = true  // indica se bloccare l'attivazione se non sono presenti tutti i firmatari dell'atto

	boolean valido = true
	Date 	validoDal
	Date 	validoAl

	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd

	private SpringSecurityService getSpringSecurityService () {
		return Holders.applicationContext.getBean("springSecurityService")
	}

	static hasMany = [componenti: CommissioneComponente, stampe: CommissioneStampa]

	static mapping = {
		table 		'odg_commissioni'
		id			 column: 'id_commissione'
		descrizione  length: 4000

		tipoRegistro 		column: 'id_tipo_registro'
		tipoRegistroSeduta 	column: 'id_tipo_registro_seduta'
		ruoloCompetenze 	column: 'ruolo_competenze', index: 'odgcom_ruocom_fk'
		ruoloVisualizza		column: 'ruolo_visualizza'

		secondaConvocazione type: 'yes_no'
		sedutaPubblica 		type: 'yes_no'
		pubblicaWeb 		type: 'yes_no'
		valido 				type: 'yes_no'
		ruoliObbligatori 	type: 'yes_no'
		votoPresidente      type: 'yes_no'
		controlloFirmatari  type: 'yes_no'

		ente 		column: 'ente'
		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
	}

    static constraints = {
		descrizione 		nullable: true
		validoAl 			nullable: true
		ruoloVisualizza		nullable: true
		progressivoCfgIter	nullable: true
    }

	def beforeValidate() {
		componenti*.beforeValidate()
		stampe*.beforeValidate()
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
	
	static namedQueries = {
		inUsoPerModelloTesto { long idModelloTesto ->
			stampe {
				eq ("modelloTesto.id", idModelloTesto)
			}
			eq ("valido", true)
		}
	}
}
