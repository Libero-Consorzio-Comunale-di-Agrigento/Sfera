package it.finmatica.atti.dto.odg.dizionari

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.odg.dizionari.Incarico
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class IncaricoDTO implements it.finmatica.dto.DTO<Incarico> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Date dateCreated;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    String titolo;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;


    public Incarico getDomainObject () {
        return Incarico.get(this.id)
    }

    public Incarico copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
