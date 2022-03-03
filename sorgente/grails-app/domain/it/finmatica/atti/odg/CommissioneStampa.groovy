package it.finmatica.atti.odg

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.documenti.ITipologia
import it.finmatica.atti.documenti.ITipologiaPubblicazione
import it.finmatica.atti.documenti.tipologie.TipoCertificato
import it.finmatica.atti.impostazioni.CaratteristicaTipologia
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.gestionetesti.reporter.GestioneTestiModello

/**
 * Rappresenta le stampe possibili per commissione
 *
 * @author mfrancesconi
 *
 */
class CommissioneStampa implements ITipologiaPubblicazione {

    public static final transient String CONVOCAZIONE = "CONVOCAZIONE"      // Stampa della CONVOCAZIONE da inviare tramite email dall'ODG.
    public static final transient String VERBALE      = "VERBALE"           // Stampa del VERBALE visibile dal Visualizzatore.
    public static final transient String DELIBERA     = "DELIBERA"
    // Modello della Delibera della stampa, mi serve un valore per non lasciare nulli (più facile da gestire dalla combo-box)

    String codice
    String titolo
    String descrizione

    CaratteristicaTipologia caratteristicaTipologia
    Long                    progressivoCfgIter

    GestioneTestiModello modelloTesto

    // indica se questa stampa è visibile o meno nel visualizzatore
    boolean usoNelVisualizzatore

    Commissione commissione
    static      belongsTo = [commissione: Commissione]

    boolean   valido = true
    Date      dateCreated
    Ad4Utente utenteIns
    Date      lastUpdated
    Ad4Utente utenteUpd

    static mapping = {
        table 'odg_commissioni_stampe'
        id column: "id_commissione_stampa"
        commissione column: "id_commissione"
        modelloTesto column: "id_modello"
        usoNelVisualizzatore type: 'yes_no'
        caratteristicaTipologia column: 'id_caratteristica_tipologia'

        dateCreated column: 'data_ins'
        utenteIns column: 'utente_ins'
        lastUpdated column: 'data_upd'
        utenteUpd column: 'utente_upd'
        valido type: 'yes_no'
    }

    static constraints = {
        descrizione nullable: true
        modelloTesto nullable: true
        progressivoCfgIter nullable: true
        caratteristicaTipologia nullable: true
        codice nullable: true, inList: [CommissioneStampa.CONVOCAZIONE
                                        , CommissioneStampa.VERBALE
                                        , CommissioneStampa.DELIBERA]
    }

    SpringSecurityService getSpringSecurityService () {
        return Holders.getApplicationContext().getBean("springSecurityService")
    }

    def beforeValidate () {
        utenteIns = utenteIns ?: getSpringSecurityService().currentUser
        utenteUpd = utenteUpd ?: getSpringSecurityService().currentUser
    }

    def beforeInsert () {
        utenteIns = utenteIns ?: getSpringSecurityService().currentUser
        utenteUpd = utenteUpd ?: getSpringSecurityService().currentUser
    }

    def beforeUpdate () {
        utenteUpd = utenteUpd ?: getSpringSecurityService().currentUser
    }

    @Override
    Long getProgressivoCfgIterPubblicazione () {
        return null
    }

    @Override
    Integer getGiorniPubblicazione () {
        return 15
    }

    @Override
    boolean isPubblicazione () {
        return true
    }

    @Override
    boolean isSecondaPubblicazione () {
        return false
    }

    @Override
    boolean isManuale () {
        return false
    }

    @Override
    boolean isPubblicaAllegati () {
        return false
    }

    @Override
    boolean isPubblicazioneFinoARevoca () {
        return false
    }
}