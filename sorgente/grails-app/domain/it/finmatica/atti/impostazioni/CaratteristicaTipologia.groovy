package it.finmatica.atti.impostazioni

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.so4.struttura.So4Amministrazione

/**
 * Rappresenta il dizionario dove vengono inserite le informazioni "tecniche" da registrare per ogni tipologia di atto.
 * Per ogni record bisogna stabilire l'oggetto di riferimento:
 * - PROPOSTA DELIBERA
 * - DETERMINA
 * - VISTO / PARERE
 */

class CaratteristicaTipologia {

    String         titolo
    String         descrizione
    String         layoutSoggetti
    WkfTipoOggetto tipoOggetto

    boolean valido = true
    Date    validoDal
    Date    validoAl

    So4Amministrazione ente
    Date               dateCreated
    Ad4Utente          utenteIns
    Date               lastUpdated
    Ad4Utente          utenteUpd

    List<CaratteristicaTipoSoggetto> caratteristicheTipiSoggetto;

    static hasMany = [caratteristicheTipiSoggetto: CaratteristicaTipoSoggetto]

    static mapping = {
        table 'caratteristiche_tipologie'
        id column: 'id_caratteristica_tipologia'
        descrizione length: 4000
        tipoOggetto column: 'tipo_oggetto'
        caratteristicheTipiSoggetto indexColumn: [name: "sequenza", type: Integer]

        valido type: 'yes_no'

        ente column: 'ente'
        dateCreated column: 'data_ins'
        utenteIns column: 'utente_ins'
        lastUpdated column: 'data_upd'
        utenteUpd column: 'utente_upd'
    }

    static constraints = {
        descrizione nullable: true
        validoAl nullable: true
    }

    SpringSecurityService getSpringSecurityService () {
        return Holders.getApplicationContext().getBean("springSecurityService")
    }

    def beforeValidate () {
        validoDal = validoDal ?: new Date()
        utenteIns = utenteIns ?: springSecurityService.currentUser
        utenteUpd = utenteUpd ?: springSecurityService.currentUser
        ente = ente ?: springSecurityService.principal.amministrazione
    }

    def beforeInsert () {
        validoAl = valido ? null : (validoAl ?: new Date())
        validoDal = new Date()
        utenteIns = utenteIns ?: springSecurityService.currentUser
        utenteUpd = utenteUpd ?: springSecurityService.currentUser
        ente = ente ?: springSecurityService.principal.amministrazione
    }

    def beforeUpdate () {
        validoAl = valido ? null : (validoAl ?: new Date())
        utenteUpd = utenteUpd ?: springSecurityService.currentUser
    }

    static hibernateFilters = {
        multiEnteFilter(condition: 'ente = :enteCorrente', type: 'string')
    }
}
