package it.finmatica.gestioneiter.dto.configuratore.dizionari

import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class WkfTipoOggettoDTO implements it.finmatica.dto.DTO<WkfTipoOggetto> {
    private static final long serialVersionUID = 1L;

    String codice;
    String descrizione;
    boolean iterabile;
    String nome;
    String oggettiFigli;
    boolean valido;


    public WkfTipoOggetto getDomainObject () {
        return WkfTipoOggetto.get(this.codice)
    }

    public WkfTipoOggetto copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.



}
