package it.finmatica.atti.odg

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.odg.dizionari.Incarico
import it.finmatica.atti.odg.dizionari.RuoloPartecipante
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
/**
 * Rappresenta i convocati / partecipanti ad una seduta
 *
 * @author mfrancesconi
 *
 */
class SedutaPartecipante {

	static belongsTo = [seduta:Seduta]

	Seduta seduta

	CommissioneComponente 	commissioneComponente
	As4SoggettoCorrente 	componenteEsterno

	RuoloPartecipante 		ruoloPartecipante 	// ruolo che il partecipante ha nella commissione (presidente, segretario..)
	Incarico				incarico			// incarico del soggetto all'interno della commissione.
												// di default assume il valore del soggetto in commissioneComponente.

	boolean convocato 				= false
	Boolean presente			//	= false
	boolean assenteNonGiustificato 	= false
	boolean firmatario 				= false

	int sequenza				// sequenza di convocazione
	int sequenzaPartecipante	// sequenza del partecipante della seduta
	int sequenzaFirma			// sequenza di firma

	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd

	static mapping = {
		table 					'odg_sedute_partecipanti'
		id 						column: 'id_seduta_partecipante'
		seduta 					column: 'id_seduta'					, index: 'odgsedpar_odgsed_fk'
		commissioneComponente 	column: 'id_commissione_componente' , index: 'odgsedpar_odgcomcom_fk'
		ruoloPartecipante 		column: 'ruolo_partecipante' 		, index: 'odgsedpar_odgruopar_fk'
		componenteEsterno 		column: 'ni_componente_esterno'
		incarico				column: 'id_incarico'

		convocato 				type: 'yes_no'
		presente 				type: 'yes_no'
		assenteNonGiustificato 	type: 'yes_no'
		firmatario 				type: 'yes_no'

		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
	}

    static constraints = {
		commissioneComponente 	nullable: true
		ruoloPartecipante 		nullable: true
		componenteEsterno		nullable: true
		presente				nullable: true
		incarico				nullable: true
    }

	private SpringSecurityService getSpringSecurityService () {		return Holders.applicationContext.getBean("springSecurityService")    }

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
