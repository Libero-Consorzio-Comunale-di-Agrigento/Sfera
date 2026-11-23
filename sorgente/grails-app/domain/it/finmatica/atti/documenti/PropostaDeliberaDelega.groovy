package it.finmatica.atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.dizionari.Delega

class PropostaDeliberaDelega {

	PropostaDelibera 	propostaDelibera
	Delega 				delega
	int 				sequenza

	Date 		dateCreated
	Ad4Utente 	utenteIns

	static belongsTo = [propostaDelibera: PropostaDelibera]

	static mapping = {
		table 				'proposte_delibera_deleghe'
		id 					column: 'id_proposta_delibera_delega'
		propostaDelibera 	column: 'id_proposta_delibera', index: 'prodeldel_prodel_fk'
		delega 				column: 'id_delega', 			 index: 'prodeldel_del_fk'
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
