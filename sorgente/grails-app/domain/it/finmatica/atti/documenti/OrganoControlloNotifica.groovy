package it.finmatica.atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders

import it.finmatica.ad4.autenticazione.Ad4Utente;
import it.finmatica.atti.commons.FileAllegato;
import it.finmatica.atti.dizionari.TipoRegistro;
import it.finmatica.atti.odg.dizionari.TipoOrganoControllo;
import it.finmatica.so4.struttura.So4Amministrazione;

class OrganoControlloNotifica {

	public static final transient String STATO_ANTEPRIMA	= "ANTEPRIMA"
	public static final transient String STATO_INVIATA    	= "INVIATA"

	public static final transient String AMBITO_DETERMINA	= "DETERMINA"
	public static final transient String AMBITO_DELIBERA   	= "DELIBERA"

	public static final transient String MODELLO_TESTO		= "ORGANI_CONTROLLO_NOTIFICA";


	TipoOrganoControllo tipoOrganoControllo
	String 		 ambito
	TipoRegistro tipoRegistro
	Date	dataPubblicazioneDal
	Date	dataPubblicazioneAl
	Date	dataAdozioneDal
	Date	dataAdozioneAl
	Integer numeroProtocollo
	Integer annoProtocollo
	String 	stato

	FileAllegato testo

	boolean valido = true
	Date 	validoDal
	Date 	validoAl

	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	  utenteIns
	Date 		lastUpdated
	Ad4Utente 	  utenteUpd

	static mapping = {
		table 					'organi_controllo_notifiche'
		id 						column: 'id_organo_controllo_notifica'
		tipoOrganoControllo		column: 'tipo_organo_controllo'
		ente 					column: 'ente'
		tipoRegistro			column: 'tipo_registro'
		ambito					inList: [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO]
		testo 			column: 'id_file_allegato'

		valido 		type: 	'yes_no'
		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
	}

    static constraints = {
		tipoRegistro 		 	nullable: true
		dataPubblicazioneDal	nullable: true
        dataPubblicazioneAl  	nullable: true
		dataAdozioneDal      	nullable: true
		dataAdozioneAl       	nullable: true
		validoAl			 	nullable: true
		testo 			 	 	nullable: true
		numeroProtocollo		nullable: true
		annoProtocollo			nullable: true
		stato					nullable: true
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
