package it.finmatica.atti.odg

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.odg.dizionari.Incarico
import it.finmatica.atti.odg.dizionari.RuoloPartecipante
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
/**
 * Rappresenta un componente di una commissione
 *
 * @author mfrancesconi
 *
 */

class CommissioneComponente {
	Commissione 		commissione
	As4SoggettoCorrente	componente
	RuoloPartecipante 	ruoloPartecipante  // indica il ruolo che il componente ha nella commissione
	boolean 			firmatario = false

	Incarico			incarico;

	int sequenzaFirma
	int sequenza

	boolean valido = true
	Date 	validoDal
	Date 	validoAl

	Date 		dateCreated
	Ad4Utente 	  utenteIns
	Date 		lastUpdated
	Ad4Utente 	  utenteUpd

	static belongsTo = [commissione: Commissione]

	private SpringSecurityService getSpringSecurityService () {		return Holders.applicationContext.getBean("springSecurityService")    }

	static mapping = {
		table 'odg_commissioni_componenti'

		id 					column: 'id_commissione_componente'
		commissione 		column: 'id_commissione', 			index: 'odgcomcom_odgcom_fk'
		componente 			column: 'ni_componente'
		ruoloPartecipante 	column: 'ruolo_partecipante'
		incarico			column: 'id_incarico'

		firmatario 	type: 'yes_no'
		valido 		type: 'yes_no'

		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
	}

	static constraints = {
		ruoloPartecipante 	nullable: true
		validoAl 			nullable: true
		incarico 			nullable: true
	}

	def beforeValidate() {
		validoDal 	= 	validoDal?:new Date()
		utenteIns	=	utenteIns?:springSecurityService.currentUser
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}

	def beforeInsert() {
		validoAl 	= 	valido?null:(validoAl?:new Date())
		validoDal 	= 	new Date()
		utenteIns	=	utenteIns?:springSecurityService.currentUser
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}

	def beforeUpdate() {
		validoAl 	= 	valido?null:(validoAl?:new Date())
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}
}
