package it.finmatica.atti.dto.impostazioni

import it.finmatica.atti.impostazioni.RegolaCalcolo
import it.finmatica.dto.DtoUtils

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class RegolaCalcoloDTO implements it.finmatica.dto.DTO<RegolaCalcolo> {
    private static final long serialVersionUID = 1L;

    Long id;
    String categoria;
    String descrizione;
    String nomeBean;
    String nomeMetodo;
    String tipo;
    String titolo;


    public RegolaCalcolo getDomainObject () {
        return RegolaCalcolo.get(this.id)
    }

    public RegolaCalcolo copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
