package it.finmatica.atti.dto.integrazioni

import it.finmatica.atti.integrazioni.Ce4Conto
import it.finmatica.dto.DTO
import it.finmatica.dto.DtoUtils

class Ce4ContoDTO implements DTO<Ce4Conto> {

    Long id
    String contoEsteso
    String descrizione
    String tipoConto

    @Override
    Ce4Conto getDomainObject() {
        return Ce4Conto.get(id)
    }

    @Override
    Ce4Conto copyToDomainObject() {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

}
