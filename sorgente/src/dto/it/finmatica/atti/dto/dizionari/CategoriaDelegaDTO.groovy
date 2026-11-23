package it.finmatica.atti.dto.dizionari

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dizionari.CategoriaDelega
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class CategoriaDelegaDTO implements it.finmatica.dto.DTO<CategoriaDelega> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    String titolo;
    Date dateCreated;
    String descrizione;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    Date validoAl;
    Date validoDal;

    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;


    public CategoriaDelega getDomainObject () {
        return CategoriaDelega.get(this.id)
    }

    public CategoriaDelega copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
