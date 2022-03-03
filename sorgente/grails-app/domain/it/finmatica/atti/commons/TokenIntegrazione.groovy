package it.finmatica.atti.commons

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente

class TokenIntegrazione {

    public static final transient String STATO_IN_CORSO = "IN_CORSO"
    public static final transient String STATO_SUCCESSO = "SUCCESSO"
    public static final transient String STATO_ERRORE = "ERRORE"

    public static final transient String TIPO_PROTOCOLLO = "PROTOCOLLO"
    public static final transient String TIPO_ALBO = "ALBO"
    public static final transient String TIPO_LOCK_DOCUMENTO = "LOCK_DOCUMENTO"

    String idRiferimento
    String dati
    String tipo
    String stato // IN_CORSO, SUCCESSO, ERRORE

    String ente
    Date   dateCreated
    Date   lastUpdated
    Ad4Utente utenteUpd
    Ad4Utente utenteIns

    static constraints = {
        idRiferimento unique: 'tipo'
        dati nullable: true
    }

    static mapping = {
        table 'token_integrazioni'
        id column: 'id_token'
        ente column: 'ente'
        dateCreated column: 'data_ins'
        utenteIns column: 'utente_ins'
        lastUpdated column: 'data_upd'
        utenteUpd column: 'utente_upd'
    }

    private SpringSecurityService getSpringSecurityService () {
        return Holders.applicationContext.getBean("springSecurityService")
    }

    def beforeValidate () {
        utenteIns = utenteIns ?: springSecurityService.currentUser
        ente = ente ?: springSecurityService.principal.amministrazione.codice
        utenteUpd = springSecurityService.currentUser
    }

    def beforeInsert () {
        utenteIns = springSecurityService.currentUser
        utenteUpd = springSecurityService.currentUser
        ente = springSecurityService.principal.amministrazione.codice
    }

    def beforeUpdate () {
        utenteUpd = springSecurityService.currentUser
    }

    static hibernateFilters = {
        multiEnteFilter(condition: 'ente = :enteCorrente', types: 'string')
    }

    transient boolean isStatoSuccesso () {
        return (stato == TokenIntegrazione.STATO_SUCCESSO)
    }

    transient boolean isStatoErrore () {
        return (stato == TokenIntegrazione.STATO_ERRORE)
    }

    transient boolean isStatoInCorso () {
        return (stato == TokenIntegrazione.STATO_IN_CORSO)
    }
}
