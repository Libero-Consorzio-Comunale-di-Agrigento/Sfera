package it.finmatica.atti.dto.impostazioni

import it.finmatica.atti.impostazioni.Impostazione
import it.finmatica.dto.DtoUtils

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class ImpostazioneDTO implements it.finmatica.dto.DTO<Impostazione> {
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
    byte[] risorsa;
    String valore;


    public Impostazione getDomainObject () {
        return Impostazione.findByCodiceAndEnte(this.codice, this.ente)
    }

    public Impostazione copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
