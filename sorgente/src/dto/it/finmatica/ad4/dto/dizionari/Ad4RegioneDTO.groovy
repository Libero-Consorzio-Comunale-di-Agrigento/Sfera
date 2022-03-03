package it.finmatica.ad4.dto.dizionari;

import it.finmatica.ad4.dizionari.Ad4Regione;
import it.finmatica.dto.DtoUtils;

public class Ad4RegioneDTO implements it.finmatica.dto.DTO<Ad4Regione> {
    private static final long serialVersionUID = 1L;

    Long id;
    String denominazione;


    public Ad4Regione getDomainObject () {
        return Ad4Regione.get(this.id)
    }

    public Ad4Regione copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
