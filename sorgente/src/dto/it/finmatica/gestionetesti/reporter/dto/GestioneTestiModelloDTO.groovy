package it.finmatica.gestionetesti.reporter.dto

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class GestioneTestiModelloDTO implements it.finmatica.dto.DTO<GestioneTestiModello> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Date dateCreated;
    String descrizione;
    So4AmministrazioneDTO ente;
    byte[] fileTemplate;
    Date lastUpdated;
    String nome;
    String tipo;
    GestioneTestiTipoModelloDTO tipoModello;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;


    public GestioneTestiModello getDomainObject () {
        return GestioneTestiModello.get(this.id)
    }

    public GestioneTestiModello copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


    public String getContentType () {
        return it.finmatica.gestionetesti.GestioneTestiService.getContentType(tipo);
    }

    public String getNomeFile () {
        return nome+"."+tipo;
    }
}
