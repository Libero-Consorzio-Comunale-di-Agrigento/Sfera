package it.finmatica.atti.dto.dizionari

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dizionari.ImpattoGenerazionale
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

@GrailsCompileStatic
public class ImpattoGenerazionaleDTO implements it.finmatica.dto.DTO<ImpattoGenerazionale> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Date dateCreated;
    boolean delibera;
    boolean determina;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    String titolo;
    String descrizione;
    String frase;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;

    public ImpattoGenerazionale getDomainObject () {
        return ImpattoGenerazionale.get(this.id)
    }

    public ImpattoGenerazionale copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
