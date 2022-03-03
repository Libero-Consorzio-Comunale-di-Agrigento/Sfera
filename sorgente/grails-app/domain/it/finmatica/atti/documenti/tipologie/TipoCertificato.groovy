package it.finmatica.atti.documenti.tipologie

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.documenti.ITipologia
import it.finmatica.atti.impostazioni.CaratteristicaTipologia
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.so4.struttura.So4Amministrazione

class TipoCertificato implements ITipologia {
	public static final transient String CERT_DETE = "CERT_DETE"
	public static final transient String CERT_DELI = "CERT_DELI"

	String titolo
	String descrizione
    String descrizioneNotifica

	// progressivo dell'iter del certificato
	Long progressivoCfgIter
	CaratteristicaTipologia caratteristicaTipologia
	GestioneTestiModello modelloTesto

	boolean valido = true
	Date validoDal  // da valorizzare alla creazione del record
	Date validoAl   // deve essere valorizzato con la data di sistema quando valido = false
					// quando valido = true deve essere null
	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd

	static mapping = {
		table 				'tipi_certificato'
		id 			column: 'id_tipo_certificato'
		descrizione length: 4000

		progressivoCfgIter		column: 'progressivo_cfg_iter'

		modelloTesto			column: 'id_modello_testo'
		caratteristicaTipologia	column: 'id_caratteristica_tipologia'

		ente 		column: 'ente'
		valido 		type:   'yes_no'
		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
	}

	static constraints = {
		progressivoCfgIter	nullable: true
		descrizione			nullable: true
		validoAl			nullable: true
		modelloTesto		nullable: true
		caratteristicaTipologia nullable:true
        descrizioneNotifica nullable: true
    }

	public transient WkfCfgIter getCfgIter () {
		return WkfCfgIter.getIterIstanziabile (progressivoCfgIter).get()
	}

	private SpringSecurityService getSpringSecurityService () {
		return Holders.applicationContext.getBean("springSecurityService")
	}

	def beforeValidate () {
		validoDal = validoDal?:new Date()
		utenteIns = utenteIns?:springSecurityService.currentUser
		ente	  = ente?:springSecurityService.principal.amministrazione
		utenteUpd = springSecurityService.currentUser
	}

	def beforeInsert () {
		validoAl  = valido ? null : (validoAl?:new Date())
		validoDal = new Date()
		utenteIns = utenteIns?:springSecurityService.currentUser
		utenteUpd = utenteUpd?:springSecurityService.currentUser
		ente	  = ente?:springSecurityService.principal.amministrazione
	}

	def beforeUpdate () {
		validoAl  = valido ? null : (validoAl?:new Date())
		utenteUpd = springSecurityService.currentUser
	}

	static hibernateFilters = {
		multiEnteFilter (condition: 'ente = :enteCorrente', types: 'string')
	}

	static namedQueries = {
		inUsoPerModelloTesto { long idModelloTesto ->
			eq ("modelloTesto.id", idModelloTesto)
			eq ("valido", true)
		}

		inUsoPerCaratteristicaTipologia { long idCaratteristica ->
			eq ("caratteristicaTipologia.id", idCaratteristica)
			eq ("valido", true)
		}
	}
}
