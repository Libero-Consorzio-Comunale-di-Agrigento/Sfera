package it.finmatica.atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.dizionari.TipoDatoAggiuntivoValore

/**
 * Rappresenta i Dati Aggiuntivi ai documenti.
 * @author esasdelli
 *
 */
class DatoAggiuntivo {

    // Collegamento ai vari documenti:
    Determina determina
    Delibera delibera
    PropostaDelibera propostaDelibera

    // codice del datoAggiuntivo
    String codice
    String valore
    TipoDatoAggiuntivoValore valoreTipoDato

    Date dateCreated
    Ad4Utente utenteIns
    Date lastUpdated
    Ad4Utente utenteUpd

    static mapping = {
        table 'dati_aggiuntivi'
        id column: 'id_dato_aggiuntivo'
        dateCreated column: 'data_ins'
        utenteIns column: 'utente_ins'
        lastUpdated column: 'data_upd'
        utenteUpd column: 'utente_upd'
        determina column: 'id_determina'
        delibera column: 'id_delibera'
        propostaDelibera column: 'id_proposta_delibera'
        valoreTipoDato column: 'id_tipo_dato_aggiuntivo_valore'
    }

    static constraints = {
        determina nullable: true
        delibera nullable: true
        propostaDelibera nullable: true
        valore nullable:true
        valoreTipoDato nullable: true
    }

    private SpringSecurityService getSpringSecurityService () {
        return Holders.getApplicationContext().getBean("springSecurityService")
    }

    def beforeValidate () {
        utenteIns = utenteIns ?: springSecurityService.currentUser
        utenteUpd = springSecurityService.currentUser
    }

    def beforeInsert () {
        utenteIns = springSecurityService.currentUser
        utenteUpd = springSecurityService.currentUser
    }

    def beforeUpdate () {
        utenteUpd = springSecurityService.currentUser
    }
}
