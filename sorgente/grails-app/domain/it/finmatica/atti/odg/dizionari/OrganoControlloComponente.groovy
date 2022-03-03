package it.finmatica.atti.odg.dizionari

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.as4.As4SoggettoCorrente
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
/**
 * Rappresenta i componenti di un organo di controllo. Per ogni componente è possibile associare un ruolo
 *
 * @author mfrancesconi
 *
 */
class OrganoControlloComponente {
	OrganoControllo		 organoControllo
	OrganoControlloRuolo organoControlloRuolo
	As4SoggettoCorrente  componente

	boolean valido = true
	Date 	validoDal
	Date	validoAl

	Date 		dateCreated
	Date 		lastUpdated
	Ad4Utente 	utenteIns
	Ad4Utente	utenteUpd

	static mapping = {
		table 				'organi_controllo_componenti'
		id 					 column: 'id_organo_controllo_componente'
		organoControllo 	 column: 'id_organo_controllo', 			index: 'orgconcom_orgcon_fk'
		organoControlloRuolo column: 'id_organo_controllo_ruolo', 		index: 'orgconcom_orgconruo_fk'
		componente 			 column: 'ni_componente'

		valido 		type: 'yes_no'
		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
	}

	static constraints = {
		organoControlloRuolo nullable: true
		validoAl 			 nullable: true
	}

	private SpringSecurityService getSpringSecurityService () {		return Holders.applicationContext.getBean("springSecurityService")    }

	def beforeValidate () {
		validoDal = validoDal?:new Date()
		utenteIns = utenteIns?:springSecurityService.currentUser
		utenteUpd = utenteUpd?:springSecurityService.currentUser
	}

	def beforeInsert () {
		validoAl  = valido? null : (validoAl?:new Date())
		validoDal = new Date()
		utenteIns = utenteIns?:springSecurityService.currentUser
		utenteUpd = utenteUpd?:springSecurityService.currentUser
	}

	def beforeUpdate () {
		validoAl  = valido? null : (validoAl?:new Date())
		utenteUpd = springSecurityService.currentUser
	}
}
