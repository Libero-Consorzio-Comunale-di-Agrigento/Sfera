package it.finmatica.so4.dto.strutturaPubblicazione

import it.finmatica.ad4.dto.autenticazione.Ad4RuoloDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.strutturaPubblicazione.So4RuoloComponentePubb

public class So4RuoloComponentePubbDTO implements it.finmatica.dto.DTO<So4RuoloComponentePubb> {
    private static final long serialVersionUID = 1L;

    Long id;
    Date al;
    So4ComponentePubbDTO componente;
    Date dal;
    Ad4RuoloDTO ruolo;


    public So4RuoloComponentePubb getDomainObject () {
        return So4RuoloComponentePubb.get(this.id)
    }

    public So4RuoloComponentePubb copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
