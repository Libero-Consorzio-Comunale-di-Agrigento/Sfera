package it.finmatica.atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.dizionari.TipoBudget
import it.finmatica.atti.dizionari.TipoDatoAggiuntivoValore
import it.finmatica.so4.struttura.So4Amministrazione

/**
 * Rappresenta il budget associato alla determina
 * @author czappavigna
 *
 */
class Budget {

    Determina determina
    PropostaDelibera propostaDelibera

    TipoBudget  tipoBudget
    BigDecimal  importo
    boolean     approvato
    boolean     annullato
    int         sequenza
    boolean     valido
    String      contoEconomico
    String      codiceProgetto
    String      codiceFornitore
    Date dataInizioValidita
    Date dataFineValidita

    So4Amministrazione ente
    Date dateCreated
    Ad4Utente utenteIns
    Date lastUpdated
    Ad4Utente utenteUpd

    static mapping = {
        table 'budget'
        id          column: 'id_budget'
        determina   column: 'id_determina'
        propostaDelibera column: 'id_proposta_delibera'
        tipoBudget  column: 'id_tipo_budget'
        approvato   type: 'yes_no'
        annullato   type: 'yes_no'
        valido      type: 'yes_no'
        ente        column: 'ente'
        dateCreated column: 'data_ins'
        utenteIns   column: 'utente_ins'
        lastUpdated column: 'data_upd'
        utenteUpd   column: 'utente_upd'
        determina   column: 'id_determina'
        tipoBudget column: 'id_tipo_budget'
        dataInizioValidita column: 'data_inizio_validita'
        dataFineValidita column: 'data_fine_validita'
        codiceFornitore column: 'codice_fornitore'
    }

    static constraints = {
        determina        nullable: true
        propostaDelibera nullable: true
        contoEconomico   nullable: true
        codiceProgetto   nullable: true
        codiceFornitore  nullable: true
        dataInizioValidita  nullable: true
        dataFineValidita  nullable: true
    }

    private SpringSecurityService getSpringSecurityService () {
        return Holders.getApplicationContext().getBean("springSecurityService")
    }

    def beforeValidate () {
        utenteIns = utenteIns ?: springSecurityService.currentUser
        utenteUpd = springSecurityService.currentUser
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

    public IProposta getProposta(){
        determina?:propostaDelibera
    }

    public IAtto getAtto(){
        determina?:Delibera.findByPropostaDelibera(propostaDelibera)
    }

}
