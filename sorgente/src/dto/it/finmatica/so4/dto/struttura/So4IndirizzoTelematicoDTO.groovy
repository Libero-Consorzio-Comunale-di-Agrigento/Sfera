package it.finmatica.so4.dto.struttura

import groovy.transform.CompileStatic
import it.finmatica.dto.DTO
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO
import it.finmatica.so4.struttura.So4IndirizzoTelematico

@CompileStatic
class So4IndirizzoTelematicoDTO implements DTO<So4IndirizzoTelematico> {
    private static final long serialVersionUID = 1L;

    Long id
    String tipoIndirizzo
    String descrizioneTipoIndirizzo
    String indirizzo

    So4UnitaPubbDTO    unita
    So4AOODTO             aoo
    So4AmministrazioneDTO amministrazione

    So4IndirizzoTelematico getDomainObject () {
        return So4IndirizzoTelematico.get(this.id)
    }

    So4IndirizzoTelematico copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
