package it.finmatica.atti.dto.odg.dizionari

import it.finmatica.atti.odg.dizionari.EsitoStandard
import it.finmatica.dto.DtoUtils

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class EsitoStandardDTO implements it.finmatica.dto.DTO<EsitoStandard> {
    private static final long serialVersionUID = 1L;

    String codice;
    boolean creaDelibera;
    boolean determina;
    boolean prossimaSeduta;
    String titolo;


    public EsitoStandard getDomainObject () {
        return EsitoStandard.get(this.codice)
    }

    public EsitoStandard copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
