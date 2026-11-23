package it.finmatica.atti.odg

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente

/**
 * Rappresenta le commissioni collegate alla commissione principale
 *
 * @author mfrancesconi
 *
 */
class CommissioneCollegata {
    Commissione 		commissione
    Commissione         commissioneCollegata

    static      belongsTo = [commissione: Commissione]

    boolean   valido = true
    Date 	validoDal
    Date 	validoAl

    Date      dateCreated
    Ad4Utente utenteIns
    Date      lastUpdated
    Ad4Utente utenteUpd

    static mapping = {
        table 'odg_commissioni_collegate'
        id column: "id_commissione_collegata"
        commissione column: "id_commissione"
        commissioneCollegata column: "id_collegata"

        dateCreated column: 'data_ins'
        utenteIns column: 'utente_ins'
        lastUpdated column: 'data_upd'
        utenteUpd column: 'utente_upd'
        valido type: 'yes_no'
    }

    static constraints = {
        validoAl 			nullable: true
    }

    SpringSecurityService getSpringSecurityService () {
        return Holders.getApplicationContext().getBean("springSecurityService")
    }

    def beforeValidate () {
        validoDal 	= 	validoDal?:new Date()
        utenteIns = utenteIns ?: getSpringSecurityService().currentUser
        utenteUpd = utenteUpd ?: getSpringSecurityService().currentUser
    }

    def beforeInsert () {
        validoAl 	= 	valido?null:(validoAl?:new Date())
        validoDal 	= 	new Date()
        utenteIns = utenteIns ?: getSpringSecurityService().currentUser
        utenteUpd = utenteUpd ?: getSpringSecurityService().currentUser
    }

    def beforeUpdate () {
        validoAl 	= 	valido?null:(validoAl?:new Date())
        utenteUpd = utenteUpd ?: getSpringSecurityService().currentUser
    }
}