package it.finmatica.atti.odg.dizionari

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.so4.struttura.So4Amministrazione
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
/**
 * Rappresenta i possibili ruoli che un utente può avere durante una seduta.
 * Valori possibili:
 * S = Segretario
 * P = Presidente
 * C = Scrutatore
 * VS = Vice Segretario
 * VP = Vice Presidente
 * I  = Invitato
 Per questa domainClass non è prevista una interfaccia di gestione
 *
 * @author mfrancesconi
 *
 */
class RuoloPartecipante {

	public static final transient String CODICE_PRESIDENTE 	 	= "P"
	public static final transient String CODICE_SEGRETARIO  	= "S"
	public static final transient String CODICE_VICE_PRESIDENTE	= "VP"
	public static final transient String CODICE_VICE_SEGRETARIO = "VS"
	public static final transient String CODICE_SCRUTATORE  	= "C"
	public static final transient String CODICE_INVITATO 		= "I"
	
	public static final transient String CODICE_DIRETTORE_AMMINISTRATIVO = TipoSoggetto.DIRETTORE_AMMINISTRATIVO
	public static final transient String CODICE_DIRETTORE_SANITARIO 	 = TipoSoggetto.DIRETTORE_SANITARIO
	public static final transient String CODICE_DIRETTORE_GENERALE 		 = TipoSoggetto.DIRETTORE_GENERALE
    public static final transient String CODICE_DIRETTORE_SOCIO_SANITARIO= TipoSoggetto.DIRETTORE_SOCIO_SANITARIO

	String codice
	String descrizione

	boolean valido = true
	Date 	validoDal  // da valorizzare alla creazione del record
	Date 	validoAl   // deve essere valorizzato con la data di sistema quando valido = false
					   // quando valido = true deve essere null

	So4Amministrazione ente
	Date 		dateCreated
	Date 		lastUpdated
	Ad4Utente 	utenteIns
	Ad4Utente 	utenteUpd

	static mapping = {
		table 	'odg_ruoli_partecipanti'
		id 		column: 'ruolo_partecipante', name: 'codice', generator: 'assigned', type: 'string'
		codice 	column: "ruolo_partecipante"

		descrizione length: 4000
		valido 		type: 	'yes_no'

		ente 		column: 'ente'
		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'

		version false
	}

    static constraints = {
		descrizione nullable: true
		validoAl 	nullable: true
    }

	private SpringSecurityService getSpringSecurityService () {		return Holders.applicationContext.getBean("springSecurityService")    }

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
		multiEnteFilter (condition: 'ente = :enteCorrente', types: 'string')
	}
}
