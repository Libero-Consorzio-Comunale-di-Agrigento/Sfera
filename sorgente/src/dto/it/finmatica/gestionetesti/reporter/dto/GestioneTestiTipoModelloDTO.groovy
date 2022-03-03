package it.finmatica.gestionetesti.reporter.dto

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionetesti.reporter.GestioneTestiTipoModello
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class GestioneTestiTipoModelloDTO implements it.finmatica.dto.DTO<GestioneTestiTipoModello> {
    private static final long serialVersionUID = 1L;

    Long version;
    String codice;
    Date dateCreated;
    String descrizione;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    byte[] query;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;


    public GestioneTestiTipoModello getDomainObject () {
        return GestioneTestiTipoModello.get(this.codice)
    }

    public GestioneTestiTipoModello copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
