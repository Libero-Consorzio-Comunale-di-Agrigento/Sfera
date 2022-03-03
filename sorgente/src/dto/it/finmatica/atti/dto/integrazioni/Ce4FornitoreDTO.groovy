package it.finmatica.atti.dto.integrazioni

import it.finmatica.atti.integrazioni.Ce4Fornitore
import it.finmatica.dto.DTO
import it.finmatica.dto.DtoUtils

class Ce4FornitoreDTO implements DTO<Ce4Fornitore> {

    Long id
    String contoFornitore
    String ragioneSociale
    String tipoConto
    String partitaIva
    String codiceFiscale

    @Override
    Ce4Fornitore getDomainObject() {
        return Ce4Fornitore.get(id)
    }

    @Override
    Ce4Fornitore copyToDomainObject() {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

}
