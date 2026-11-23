package it.finmatica.atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.so4.struttura.So4Amministrazione

// Registro transiente del numero assegnato alla Determina in una TX indipendente (REQUIRES_NEW).
// Esiste solo tra il momento in cui il numero viene committato e il momento in cui la outer TX
// lo applica sulla Determina. Se la outer TX fa rollback, questo record sopravvive e viene
// riutilizzato al ciclo successivo (evita gap aggiuntivi nel registro e garantisce che il
// numero già assegnato venga applicato senza ri-numerare).
class DeterminaNumero {

    Long        determinaId
    int         numeroDetermina
    int         annoDetermina
    Date        dataNumeroDetermina
    TipoRegistro registroDetermina
    Date        dataOperazione = new Date()
    So4Amministrazione ente

    static constraints = {
        determinaId  unique: true
        registroDetermina nullable: false
    }

    static mapping = {
        table           'determine_numeri'
        id                column: 'id_determina_numero'
        determinaId       column: 'id_determina'
        registroDetermina column: 'registro_determina'
        ente 			  column: 'ente'
        version 	false
    }

    private SpringSecurityService getSpringSecurityService () {
        return Holders.applicationContext.getBean("springSecurityService")
    }

    def beforeValidate () {
        ente = ente ?: springSecurityService.principal.amministrazione
    }

    static hibernateFilters = {
        multiEnteFilter(condition: "ente = :enteCorrente", types: 'string')
    }
}
