package it.finmatica.ad4.dto.autenticazione;

import it.finmatica.ad4.autenticazione.Ad4Utente;
import it.finmatica.dto.DtoUtils;

public class Ad4UtenteDTO implements it.finmatica.dto.DTO<Ad4Utente> {
    private static final long serialVersionUID = 1L;

    String id;
    boolean accountExpired;
    boolean accountLocked;
    boolean enabled;
    boolean esisteSoggetto;
    String nominativo;
    String nominativoSoggetto;
    String password;
    boolean passwordExpired;
    String tipoUtente;


    public Ad4Utente getDomainObject () {
        return Ad4Utente.get(this.id)
    }

    public Ad4Utente copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
