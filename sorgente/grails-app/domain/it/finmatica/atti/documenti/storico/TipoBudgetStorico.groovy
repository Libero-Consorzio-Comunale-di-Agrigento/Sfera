package it.finmatica.atti.documenti.storico

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.dizionari.TipoBudget
import it.finmatica.so4.struttura.So4Amministrazione
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class TipoBudgetStorico {

    String          titolo
    int             anno
    String          tipo
    String          contoEconomico
    TipoBudget      tipoBudget
    String          motivazioni

    BigDecimal      importoIniziale
    BigDecimal      importoPrenotato
    BigDecimal      importoAutorizzato
    BigDecimal      importoDisponibile

    Ad4Utente 		utenteAd4
	So4UnitaPubb	unitaSo4

    boolean         valido = true
	boolean         attivo = true

    So4Amministrazione ente
    Date dateCreated
    Ad4Utente utenteIns
    Date lastUpdated
    Ad4Utente utenteUpd

    static mapping = {
		table 			'tipi_budget_storico'
		id 			 	column: 'id_tipo_budget_storico'
        tipoBudget      column: 'id_tipo_budget'
		utenteAd4  	 	column: 'utente'
		attivo			        type: 'yes_no'
        valido					type: 'yes_no'
        ente 					column: 'ente'
        dateCreated 			column: 'data_ins'
        utenteIns 				column: 'utente_ins'
        lastUpdated 			column: 'data_upd'
        utenteUpd 				column: 'utente_upd'

        columns {
			unitaSo4 {
				column name: 'unita_progr'
				column name: 'unita_dal'
				column name: 'unita_ottica'
			}
		}
	}

    static constraints = {
    }

    private SpringSecurityService getSpringSecurityService () {
        return Holders.applicationContext.getBean("springSecurityService")
    }

    def beforeValidate () {
        utenteIns = utenteIns ?: springSecurityService.currentUser
        utenteUpd = utenteUpd ?: springSecurityService.currentUser
        ente = ente ?: springSecurityService.principal.amministrazione
    }

    def beforeInsert () {
        utenteIns = springSecurityService.currentUser
        utenteUpd = springSecurityService.currentUser
        ente = springSecurityService.principal.amministrazione
    }

    def beforeUpdate () {
        utenteUpd = springSecurityService.currentUser
    }

}
