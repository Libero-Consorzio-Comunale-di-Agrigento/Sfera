package it.finmatica.atti.odg.dizionari

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.as4.As4SoggettoCorrente

/**
 * Rappresenta i componenti di un Gruppo Politico. Per ogni componente è possibile associare un ruolo
 *
 * @author amarinelli
 *
 */
class GruppoPoliticoComponente {
	GruppoPolitico		 gruppoPolitico
	As4SoggettoCorrente  componente

	boolean valido = true
	Date 	validoDal
	Date	validoAl

	Date 		dateCreated
	Date 		lastUpdated
	Ad4Utente 	utenteIns
	Ad4Utente	utenteUpd

	static mapping = {
		table 				'gruppi_politici_componenti'
		id 					 column: 'id_gruppo_politico_componente'
		gruppoPolitico 	 column: 'id_gruppo_politico', 			index: 'GRUPOL_GRUPOLCOM_FK'
		componente 			 column: 'ni_componente'

		valido 		type: 'yes_no'
		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
	}

	static constraints = {
		validoAl 			 nullable: true
		componente unique: ['gruppoPolitico']
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
