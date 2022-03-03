package it.finmatica.atti.dto.integrazioni

import it.finmatica.atti.documenti.StatoConservazione
import it.finmatica.atti.integrazioni.JConsLogConservazione
import it.finmatica.dto.DTO
import it.finmatica.dto.DtoUtils

class JConsLogConservazioneDTO implements DTO<JConsLogConservazione> {

    Long id
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

    @Override
    JConsLogConservazione getDomainObject() {
        return JConsLogConservazione.get(id)
    }

    @Override
    JConsLogConservazione copyToDomainObject() {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

    StatoConservazione statoConservazione
    String urlRicevuta
}
