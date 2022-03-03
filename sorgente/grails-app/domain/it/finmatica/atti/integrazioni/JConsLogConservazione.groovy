package it.finmatica.atti.integrazioni

import it.finmatica.atti.IDocumentoEsterno

class JConsLogConservazione implements IDocumentoEsterno {

    Long idDocumentoEsterno
    String log
    String stato
    Date dataInizio
    Date dataFine
    String esito
    Long idTransazione

    String descrizione
    String idSistemaConservazione
    String nome

    static mapping = {
        table 'jcons_v_log_conservazione'
        id column: 'id_documento', generator: 'assigned'
        idDocumentoEsterno column: 'id_documento_rif'
        version false
        esito column: 'esito_conservazione'
        stato column: 'stato_conservazione'
    }

    static constraints = {
        idSistemaConservazione  nullable: true
        idDocumentoEsterno      nullable: true
        log             nullable: true
        stato           nullable: true
        dataInizio      nullable: true
        dataFine        nullable: true
        esito           nullable: true
        idTransazione   nullable: true
        descrizione     nullable: true
        nome            nullable: true
    }

    static namedQueries = {
        getLast { Long idDocumentoEsterno ->
            eq ("idDocumentoEsterno", idDocumentoEsterno)
            maxResults (1)
            order ("id", "desc")
        }
    }
}
