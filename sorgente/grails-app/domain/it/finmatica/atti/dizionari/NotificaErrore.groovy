package it.finmatica.atti.dizionari

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.gestioneiter.motore.WkfStep
import it.finmatica.so4.struttura.So4Amministrazione
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

/**
 */
class NotificaErrore {

    public static final String OPERAZIONE_INVIO 	= "INVIO"
    public static final String OPERAZIONE_ELIMINA 	= "ELIMINA"

	boolean valido = true
	Date dateCreated
	Ad4Utente utenteIns
	Date lastUpdated
	Ad4Utente utenteUpd
    String idRiferimento
    So4Amministrazione ente
    String operazione
    WkfStep stepCorrente

	Notifica notifica
	static belongsTo = [notifica : Notifica]

	static mapping = {
		table 		'notifiche_errori'
		id 			column: 'id_notifica_errore'
		notifica 	column: 'id_notifica'
        ente 			column: 'ente'
        valido 		type: 	'yes_no'
		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
        idRiferimento column: 'id_riferimento'
        stepCorrente 	column: 'id_step_corrente'
	}

	static constraints = {
        notifica        nullable: true
        stepCorrente	nullable: true
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

    static hibernateFilters = {
        multiEnteFilter (condition: 'ente = :enteCorrente', type:'string')
    }

}
