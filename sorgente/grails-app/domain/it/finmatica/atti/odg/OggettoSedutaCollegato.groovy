package it.finmatica.atti.odg

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.commons.AttiUtils

/**
 * Rappresenta un oggetto (proposta di delibera o determina) discusso in una seduta di una commissione collegata
 *
 * @author mfrancesconi
 *
 */
class OggettoSedutaCollegato {
	OggettoSeduta 		oggettoSeduta
	Seduta 				seduta
	int sequenzaConvocazione

	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd

	static mapping = {
		table 'odg_oggetti_seduta_collegati'
		id 				 		column: 'id_oggetto_seduta_coll'
		seduta 			 		column: 'id_seduta'			, index: 'odgoggsedcol_odgsed_fk'
		oggettoSeduta 		 		column: 'id_oggetto_seduta'			, index: 'odgoggsedcol_odgoggsed_fk'
		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
	}

	private SpringSecurityService getSpringSecurityService () {
		return Holders.applicationContext.getBean("springSecurityService")
	}

	def beforeValidate() {
		utenteIns	=	utenteIns?:springSecurityService.currentUser
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}

	def beforeInsert() {
		utenteIns	=	utenteIns?:springSecurityService.currentUser
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}

	def beforeUpdate() {
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}
}
