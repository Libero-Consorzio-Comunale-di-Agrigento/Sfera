package it.finmatica.ad4.dto.dizionari;

import it.finmatica.ad4.dizionari.Ad4Stato;
import it.finmatica.dto.DtoUtils;

public class Ad4StatoDTO implements it.finmatica.dto.DTO<Ad4Stato> {
    private static final long serialVersionUID = 1L;

    Long id;
    String cittadinanza;
    String denominazione;
    String sigla;


    public Ad4Stato getDomainObject () {
        return Ad4Stato.get(this.id)
    }

    public Ad4Stato copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
