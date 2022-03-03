package it.finmatica.atti.odg

import it.finmatica.ad4.autenticazione.Ad4Utente;
import it.finmatica.atti.odg.dizionari.RuoloPartecipante;
import it.finmatica.atti.odg.dizionari.Voto;
import it.finmatica.so4.struttura.So4Amministrazione;
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders

/**
 * Rappresenta i partecipanti alla discussione di un singolo oggetto (proposta di delibera o determina)
 *
 * @author mfrancesconi
 *0
 */
class OggettoPartecipante {
	
	OggettoSeduta 		oggettoSeduta
	SedutaPartecipante 	sedutaPartecipante
	RuoloPartecipante 	ruoloPartecipante
	
	Voto 	voto
	Boolean presente 				//= false
	boolean assenteNonGiustificato 	= false
	boolean firmatario 				= false

	int sequenzaFirma
	int sequenza

	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	  utenteIns
	Date 		lastUpdated
	Ad4Utente 	  utenteUpd

	static mapping = {
		table 'odg_oggetti_partecipanti'
		id 					column: 'id_oggetto_partecipante'
		oggettoSeduta		column: 'id_oggetto_seduta', 		index: 'odgoggpar_odgoggsed_fk'
		sedutaPartecipante 	column: 'id_seduta_partecipante', 	index: 'odgoggpar_odgsedpar_fk'
		voto 				column: 'id_voto'
		ruoloPartecipante 	column: 'ruolo_partecipante'

		presente 				type: 'yes_no'
		assenteNonGiustificato 	type: 'yes_no'
		firmatario 				type: 'yes_no'

		ente		column: 'ente'
		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
	}

    static constraints = {
		ruoloPartecipante 	nullable:true
		voto 				nullable:true
		presente 			nullable:true
    }

	private SpringSecurityService getSpringSecurityService () {		return Holders.applicationContext.getBean("springSecurityService")    }

	def beforeValidate() {
		utenteIns	=	utenteIns?:springSecurityService.currentUser
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
		ente		=	ente?:springSecurityService.principal.amministrazione
	}

	def beforeInsert() {
		utenteIns	=	utenteIns?:springSecurityService.currentUser
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
		ente		=	ente?:springSecurityService.principal.amministrazione
	}

	def beforeUpdate() {
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}
}
