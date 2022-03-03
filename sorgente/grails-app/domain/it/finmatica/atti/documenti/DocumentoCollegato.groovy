package it.finmatica.atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.so4.struttura.So4Amministrazione

class DocumentoCollegato {

	public static final transient String OPERAZIONE_ANNULLA	= 'ANNULLA'
	public static final transient String OPERAZIONE_INTEGRA	= 'INTEGRA'
	public static final transient String OPERAZIONE_COLLEGA	= 'COLLEGA'

	String 	operazione 	// tipo di collegamento: annullamento, integrazione o collegamento

	PropostaDelibera propostaDeliberaPrincipale
	Delibera deliberaPrincipale
	Determina determinaPrincipale

	Determina determinaCollegata
	Delibera  deliberaCollegata

	RiferimentoEsterno riferimentoEsternoCollegato

	static belongsTo = [determinaPrincipale : Determina, propostaDeliberaPrincipale : PropostaDelibera, deliberaPrincipale: Delibera]

	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd

	static mapping = {
		table				'documenti_collegati'
		id 					column: 'id_documento_collegato'

		determinaPrincipale			column: 'id_determina_principale', 		index: 'detprinc_det_fk'
		propostaDeliberaPrincipale 	column: 'id_proposta_delibera_princ', 	index: 'prdelprinc_det_fk'
		deliberaPrincipale 			column: 'id_delibera_principale', 		index: 'delprinc_det_fk'

		determinaCollegata	column: 'id_determina_collegata', 	index: 'detcol_det_fk'
		deliberaCollegata   column: 'id_delibera_collegata', 	index: 'delcol_det_fk'
        riferimentoEsternoCollegato   column: 'id_riferimento_est_coll', 	index: 'rifestcol_rifest_fk'

		ente 		column: 'ente'
		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
	}

    static constraints = {
        riferimentoEsternoCollegato	nullable: true
		determinaPrincipale			nullable: true
		propostaDeliberaPrincipale	nullable: true
		deliberaPrincipale			nullable: true

		determinaCollegata			nullable: true
		deliberaCollegata           nullable: true

		operazione 	inList: [ DocumentoCollegato.OPERAZIONE_ANNULLA
							, DocumentoCollegato.OPERAZIONE_INTEGRA
							, DocumentoCollegato.OPERAZIONE_COLLEGA]
    }

	private SpringSecurityService getSpringSecurityService () {
        return Holders.grailsApplication.mainContext.getBean("springSecurityService")
    }

	def beforeValidate () {
		utenteIns = utenteIns?:springSecurityService.currentUser
		utenteUpd = utenteUpd?:springSecurityService.currentUser
		ente	  = ente?:springSecurityService.principal.amministrazione
	}

	def beforeInsert () {
		utenteIns = springSecurityService.currentUser
		utenteUpd = springSecurityService.currentUser
		ente	  = springSecurityService.principal.amministrazione
	}

	def beforeUpdate () {
		utenteUpd = springSecurityService.currentUser
	}

	static hibernateFilters = {
		multiEnteFilter (condition: 'ente = :enteCorrente', types: 'string')
	}
}
