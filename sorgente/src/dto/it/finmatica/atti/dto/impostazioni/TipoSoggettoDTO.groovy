package it.finmatica.atti.dto.impostazioni

import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.dto.DtoUtils

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
public class TipoSoggettoDTO implements it.finmatica.dto.DTO<TipoSoggetto> {
    private static final long serialVersionUID = 1L;

    String categoria;
    String codice;
    String descrizione;
    String titolo;


    public TipoSoggetto getDomainObject() {
        return TipoSoggetto.get(this.codice)
    }

    public TipoSoggetto copyToDomainObject() {
        return null
    }

    /* * * codice personalizzato * * */
    // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
