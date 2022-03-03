package it.finmatica.gestioneiter.dto.impostazioni

import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.impostazioni.WkfImpostazione

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class WkfImpostazioneDTO implements it.finmatica.dto.DTO<WkfImpostazione> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    String caratteristiche;
    String codice;
    String descrizione;
    String ente;
    String etichetta;
    boolean modificabile;
    String predefinito;
    String valore;


    public WkfImpostazione getDomainObject () {
        return WkfImpostazione.findByCodiceAndEnte(this.codice, this.ente)
    }

    public WkfImpostazione copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.



}
