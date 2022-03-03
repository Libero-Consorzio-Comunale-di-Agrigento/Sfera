package it.finmatica.atti.documenti

import it.finmatica.ad4.autenticazione.Ad4Utente;
import it.finmatica.atti.dizionari.TipoControlloRegolarita;
import it.finmatica.atti.dizionari.TipoRegistro;
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.gestionetesti.reporter.GestioneTestiModello;
import it.finmatica.so4.struttura.So4Amministrazione;

class ControlloRegolarita {

	public static final transient String STATO_REDAZIONE	= "REDAZIONE"
	public static final transient String STATO_CHIUSO    	= "CHIUSO"
	public static final transient String STATO_INVIATO    	= "INVIATO"
	
	public static final transient String MODELLO_TESTO		= "CONTROLLO_REGOLARITA";

	TipoControlloRegolarita tipoControlloRegolarita
	String 		 ambito
	TipoRegistro tipoRegistro
	Date	dataEsecutivitaDal
	Date	dataEsecutivitaAl
	Integer numeroProtocollo
	Integer annoProtocollo
	Integer attiDaEstrarre
	Integer totaleAtti
	boolean percentuale
	String 	stato
	String  criteriRicerca 

	// modelli testo
	GestioneTestiModello modelloTesto

	boolean valido = true
	Date 	validoDal
	Date 	validoAl
    Date    dataEstrazione

	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	  utenteIns
	Date 		lastUpdated
	Ad4Utente 	  utenteUpd

	static mapping = {
		table 					'controllo_regolarita'
		id 						column: 'id_controllo_regolarita'
		tipoControlloRegolarita	column: 'tipo_controllo_regolarita'
		ente 					column: 'ente'
		tipoRegistro			column: 'tipo_registro'
		ambito					inList: [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO]
		modelloTesto	 		column: 'id_modello_testo'

		valido 					type: 	'yes_no'
		percentuale 			type: 	'yes_no'
		criteriRicerca 			sqlType: 'Clob'
		dateCreated 			column: 'data_ins'
		utenteIns 				column: 'utente_ins'
		lastUpdated 			column: 'data_upd'
		utenteUpd 				column: 'utente_upd'
	}
	
    static constraints = {
		tipoRegistro 		 	nullable: true
		validoAl			 	nullable: true
		modelloTesto 			nullable: true
		numeroProtocollo		nullable: true
		annoProtocollo			nullable: true
		stato					nullable: true
		criteriRicerca			nullable: true
		totaleAtti				nullable: true
        dataEstrazione             nullable: true
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
