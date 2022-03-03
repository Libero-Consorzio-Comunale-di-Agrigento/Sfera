package it.finmatica.atti.documenti.tipologie

import it.finmatica.ad4.autenticazione.Ad4Utente
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.atti.cf.integrazione.ITipoDocumentoCf
import it.finmatica.so4.struttura.So4Amministrazione

class TipoDocumentoCf {

	TipoDetermina tipoDetermina
	TipoDelibera tipoDelibera

	String cfTipoDocumentoCodice
	String cfTipoDocumentoEnte

	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd

	static mapping = {
		table				'tipi_documento_cf'
		id					column: 'id_tipo_documento_cf'
		tipoDetermina		column: 'id_tipo_determina',	index: 'tipdetcf_tipdet_fk'
		tipoDelibera		column: 'id_tipo_delibera',		index: 'tipdelcf_tipdel_fk'

		ente 				column: 'ente'
		dateCreated 		column: 'data_ins'
		utenteIns 			column: 'utente_ins'
		lastUpdated 		column: 'data_upd'
		utenteUpd 			column: 'utente_upd'
	}

    static constraints = {
		tipoDetermina		nullable: true
		tipoDelibera		nullable: true
    }

	private SpringSecurityService getSpringSecurityService () {		return Holders.applicationContext.getBean("springSecurityService")    }

	def beforeValidate () {
		utenteIns = utenteIns?:springSecurityService.currentUser
		ente	  = ente?:springSecurityService.principal.amministrazione
		utenteUpd = springSecurityService.currentUser
		cfTipoDocumentoEnte	  = cfTipoDocumentoEnte?:springSecurityService.principal.amministrazione?.codice
	}

	def beforeInsert () {
		utenteIns = utenteIns?:springSecurityService.currentUser
		utenteUpd = utenteUpd?:springSecurityService.currentUser
		ente	  = ente?:springSecurityService.principal.amministrazione
	}

	def beforeUpdate () {
		utenteUpd = springSecurityService.currentUser
	}

	static hibernateFilters = {
		multiEnteFilter (condition: 'ente = :enteCorrente', types: 'string')
	}

	static List<ITipoDocumentoCf> getTipiDocumento (TipoDetermina tipoDetermina) {
		return TipoDocumentoCf.findAllByTipoDeterminaAndCfTipoDocumentoCodiceIsNotNull(tipoDetermina).collect { new it.finmatica.atti.cf.integrazione.TipoDocumentoCf(codice:it.cfTipoDocumentoCodice, codiceEnte:it.ente.codice) }
	}

	static List<ITipoDocumentoCf> getTipiDocumento (TipoDelibera tipoDelibera) {
		return TipoDocumentoCf.findAllByTipoDeliberaAndCfTipoDocumentoCodiceIsNotNull(tipoDelibera).collect { new it.finmatica.atti.cf.integrazione.TipoDocumentoCf(codice:it.cfTipoDocumentoCodice, codiceEnte:it.ente.codice) }
	}
}
