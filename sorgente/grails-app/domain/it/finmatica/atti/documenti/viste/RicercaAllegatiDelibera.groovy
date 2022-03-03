package it.finmatica.atti.documenti.viste

import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.so4.struttura.So4Amministrazione
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb


class RicercaAllegatiDelibera {

    long   posizione
    Long   idDocumento
    String tipoDocumento
    Long   idFileEsterno
    Long   idFileAllegato
    Long   annoDelibera
    Long   numeroDelibera
    Date   dataNumeroDelibera

    String oggetto
    String codiceRegistro
    String registro
    String nome
    Long   codiceEsito
    String esito

    So4Amministrazione ente

    // dati per le competenze
    Ad4Utente    compUtente
    So4UnitaPubb compUnita
    Ad4Ruolo     compRuolo
    boolean      compLettura
    boolean      compModifica
    boolean      compCancellazione

    static mapping = {
        // mapping per questa domain
        table 'DELIBERE_ALLEGATI_VIEW'

        // mapping comuni per la RicercaDocumento
        version false
        id generator: 'assigned', name: 'idDocumento', column: 'id_documento'

        ente column: 'ente'

        compLettura type: 'yes_no'
        compModifica type: 'yes_no'
        compCancellazione type: 'yes_no'

        compUtente column: 'comp_utente'
        compRuolo column: 'comp_ruolo'
        columns {
            compUnita {
                column name: 'comp_unita_progr'
                column name: 'comp_unita_dal'
                column name: 'comp_unita_ottica'
            }
        }
    }

    static hibernateFilters = {
        multiEnteFilter(condition: 'ente = :enteCorrente', types: 'string')
    }
}
