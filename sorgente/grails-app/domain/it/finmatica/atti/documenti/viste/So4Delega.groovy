package it.finmatica.atti.documenti.viste

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class So4Delega {

    Long      id

    Ad4Utente deleganteUtente
    Ad4Utente delegatoUtente

    Long    progressivoUnita
    String  codiceOttica
    String istanzaApplicativo
    String moduloApplicativo
    String tipologia

    Ad4Utente utenteAggiornamento
    Date      dataAggiornamento

    Date dal
    Date al


    static mapping = {

        table 'SO4_V_DELEGHE'
        id       column: 'id_delega'

        deleganteUtente column: 'delegante_utente'
        delegatoUtente  column: 'delegato_utente'

        tipologia column: 'codice_competenza_delega'

        progressivoUnita column : 'PROGR_UNITA_ORGANIZZATIVA'
        codiceOttica     column: 'OTTICA'

        utenteAggiornamento  column: 'utente_aggiornamento'
        version             false
    }

    static constraints = {

    }
}