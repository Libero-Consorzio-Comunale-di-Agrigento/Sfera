package it.finmatica.atti.odg

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.dizionari.Delega

class OggettoSedutaDelega {

	OggettoSeduta 	oggettoSeduta
	Delega 			delega
	int 			sequenza

	Date 		dateCreated
	Ad4Utente 	utenteIns

	static belongsTo = [oggettoSeduta: OggettoSeduta]

	static mapping = {
		table 				'odg_oggetti_seduta_deleghe'
		id 					column: 'id_oggetto_seduta_delega'
		oggettoSeduta 		column: 'id_oggetto_seduta', index: 'odgoggseddel_oggsed_fk'
		delega 				column: 'id_delega', 		 index: 'odgoggseddel_del_fk'
		dateCreated 		column: 'data_ins'
		utenteIns 			column: 'utente_ins'
	}

	static constraints = {
	}

	private SpringSecurityService getSpringSecurityService () {
		return Holders.applicationContext.getBean("springSecurityService")
	}

	def beforeValidate () {
		utenteIns = utenteIns ?: springSecurityService.currentUser
	}

	def beforeInsert () {
		utenteIns = springSecurityService.currentUser
	}
}
