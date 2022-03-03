package it.finmatica.atti.dto.commons

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.commons.TokenIntegrazione

@GrailsCompileStatic
public class TokenIntegrazioneDTO implements it.finmatica.dto.DTO<TokenIntegrazione> {
    private static final long serialVersionUID = 1L;

    Long   id;
    String idRiferimento;
    String dati;
    String tipo;
    String stato;
    String ente;
    Date   dateCreated;
    Date   lastUpdated;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    Long version;

    public TokenIntegrazione getDomainObject () {
        return TokenIntegrazione.get(this.id)
    }

    public TokenIntegrazione copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.
}
