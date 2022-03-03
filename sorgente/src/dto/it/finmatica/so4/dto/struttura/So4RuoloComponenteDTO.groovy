package it.finmatica.so4.dto.struttura

import it.finmatica.ad4.dto.autenticazione.Ad4RuoloDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.struttura.So4RuoloComponente

public class So4RuoloComponenteDTO implements it.finmatica.dto.DTO<So4RuoloComponente> {
    private static final long serialVersionUID = 1L;

    Long id;
    Date al;
    So4ComponenteDTO componente;
    Date dal;
    Ad4RuoloDTO ruolo;


    public So4RuoloComponente getDomainObject () {
        return So4RuoloComponente.get(this.id)
    }

    public So4RuoloComponente copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
