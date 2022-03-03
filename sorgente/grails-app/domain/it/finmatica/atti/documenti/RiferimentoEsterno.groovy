package it.finmatica.atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.so4.struttura.So4Amministrazione

/**
 * Rappresenta un riferimento a un documento presente su un documentale esterno.
 */
class RiferimentoEsterno implements IDocumentoEsterno {

    public static final String DOCUMENTALE_GDM = "GDM"

    Long   idDocumentoEsterno
    String codiceDocumentaleEsterno
    String tipoDocumento
    String titolo
    boolean valido = true

    So4Amministrazione ente
    Ad4Utente   utenteIns
    Ad4Utente 	utenteUpd
    Date 		dateCreated
    Date 		lastUpdated

    static mapping = {
        table 'riferimenti_esterni'
        id column: 'id_riferimento_esterno'
        titolo length: 4000
        valido type: 'yes_no'

        ente 		column: 'ente'
        dateCreated column: 'data_ins'
        utenteIns 	column: 'utente_ins'
        lastUpdated column: 'data_upd'
        utenteUpd 	column: 'utente_upd'
    }

    static constraints = {
        codiceDocumentaleEsterno inList: [DOCUMENTALE_GDM]
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