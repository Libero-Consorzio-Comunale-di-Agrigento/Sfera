package it.finmatica.atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.odg.dizionari.GruppoPolitico

class GruppoPoliticoDocumento {

    GruppoPolitico gruppoPolitico
    PropostaDelibera propostaDelibera
    Delibera delibera
    As4SoggettoCorrente componente

    String 					descrizione
    String                  titolo

    boolean valido = true
    Date 	validoDal
    Date 	validoAl

    Date 		dateCreated
    Ad4Utente   utenteIns
    Date 		lastUpdated
    Ad4Utente 	utenteUpd

    static mapping = {
        table 		'gruppi_politici_documenti'
        id 			column: 'id_gruppo_politico_documento'
        descrizione length: 4000
        componente 	column: 'ni_componente'

        gruppoPolitico column: 'id_gruppo_politico', index: 'grupoldoc_grupol_fk'
        propostaDelibera	column: 'id_proposta_delibera',	index: 'grupoldoc_prodel_fk'
        delibera 	column: 'id_delibera',	index: 'grupoldoc_del_fk'

        valido 		type: 	'yes_no'
        dateCreated column: 'data_ins'
        utenteIns 	column: 'utente_ins'
        lastUpdated column: 'data_upd'
        utenteUpd 	column: 'utente_upd'
    }

    static constraints = {
        descrizione nullable: true
        validoAl 	nullable: true
        delibera    nullable: true
        propostaDelibera nullable: true

        componente validator: { val, obj ->
            def existing = GruppoPoliticoDocumento.createCriteria().get {
                eq("componente", val)
                if (obj.propostaDelibera) {
                    eq("propostaDelibera", obj.propostaDelibera)
                    isNull("delibera") // deve essere nella stessa condizione
                } else if (obj.delibera) {
                    eq("delibera", obj.delibera)
                    isNull("propostaDelibera")
                }
                if (obj.id) {
                    ne("id", obj.id) // escludi se stesso in update
                }
            }

            return !existing ?: "componente.duplicato.per.proposta.o.delibera"
        }
    }

    private SpringSecurityService getSpringSecurityService () {		return Holders.applicationContext.getBean("springSecurityService")    }

    def beforeValidate () {
        validoDal = validoDal?:new Date()
        utenteIns = utenteIns?:springSecurityService.currentUser
        utenteUpd = utenteUpd?:springSecurityService.currentUser
    }

    def beforeInsert () {
        validoAl  = valido? null : (validoAl?:new Date())
        validoDal = new Date()
        utenteIns = utenteIns?:springSecurityService.currentUser
        utenteUpd = utenteUpd?:springSecurityService.currentUser
    }

    def beforeUpdate () {
        validoAl  = valido? null : (validoAl?:new Date())
        utenteUpd = springSecurityService.currentUser
    }


}
