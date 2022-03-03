package it.finmatica.atti.dto.dizionari

import it.finmatica.ad4.dto.autenticazione.Ad4RuoloDTO
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.dizionari.NotificaEmail
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class NotificaEmailDTO implements it.finmatica.dto.DTO<NotificaEmail> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Date dateCreated;
    EmailDTO email;
    String funzione;
    Date lastUpdated;
    NotificaDTO notifica;
    Ad4RuoloDTO ruolo;
    As4SoggettoCorrenteDTO soggetto;
    So4UnitaPubbDTO unita;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;


    public NotificaEmail getDomainObject () {
        return NotificaEmail.get(this.id)
    }

    public NotificaEmail copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
