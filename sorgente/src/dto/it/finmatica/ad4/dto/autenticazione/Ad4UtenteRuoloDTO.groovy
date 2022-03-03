package it.finmatica.ad4.dto.autenticazione;

import it.finmatica.ad4.autenticazione.Ad4UtenteRuolo;
import it.finmatica.dto.DtoUtils;

public class Ad4UtenteRuoloDTO implements it.finmatica.dto.DTO<Ad4UtenteRuolo> {
    private static final long serialVersionUID = 1L;

    Long id;
    Ad4RuoloDTO ad4Ruolo;
    Ad4UtenteDTO ad4Utente;


    public Ad4UtenteRuolo getDomainObject () {
        return Ad4UtenteRuolo.createCriteria().get {
            eq('ad4Ruolo.ruolo', this.ad4Ruolo.ruolo)
            eq('ad4Utente.id', this.ad4Utente.id)
        }
    }

    public Ad4UtenteRuolo copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
