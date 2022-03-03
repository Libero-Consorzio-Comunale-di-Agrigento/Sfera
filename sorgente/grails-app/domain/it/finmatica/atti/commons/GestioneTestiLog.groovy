package it.finmatica.atti.commons

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente

class GestioneTestiLog {

    enum Stato {
        SUCCESSO, ERRORE
    }

    enum Operazione {
        CONVERSIONE_PDF
    }

    Long idDocumento

    String stato
    String operazione
    String applicativo
    String nomeFile
    String estremiDocumento
    String errore

    Date dataFineElaborazione

    Date dateCreated
    Ad4Utente utenteIns
    Date lastUpdated
    Ad4Utente utenteUpd

    static mapping = {

        table 'gte_log_view'
        id column: 'id_gte_log'
        idDocumento column: 'id_documento'
        errore sqlType: 'Clob'

        dateCreated column: 'data_ins'
        utenteIns column: 'utente_ins'
        lastUpdated column: 'data_upd'
        utenteUpd column: 'utente_upd'

        version false
    }

    static constraints = {

        errore nullable: true
        nomeFile nullable: true
        estremiDocumento nullable: true
        dataFineElaborazione nullable: true
        stato nullable: true
    }

    private SpringSecurityService getSpringSecurityService() {
        return Holders.applicationContext.getBean("springSecurityService")
    }

    def beforeValidate() {
        utenteIns = utenteIns ?: springSecurityService.currentUser
        utenteUpd = springSecurityService.currentUser
    }

    def beforeInsert() {
        utenteIns = springSecurityService.currentUser
        utenteUpd = springSecurityService.currentUser
    }

    def beforeUpdate() {
        utenteUpd = springSecurityService.currentUser
    }

    static hibernateFilters = {
        multiEnteFilter(condition: "ente = :enteCorrente and valido = 'Y'", types: 'string')
    }

}