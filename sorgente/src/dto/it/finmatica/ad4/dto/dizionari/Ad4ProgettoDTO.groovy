package it.finmatica.ad4.dto.dizionari;

import it.finmatica.ad4.dizionari.Ad4Progetto;
import it.finmatica.dto.DtoUtils;

public class Ad4ProgettoDTO implements it.finmatica.dto.DTO<Ad4Progetto> {
    private static final long serialVersionUID = 1L;

    String descrizione;
    String note;
    Integer priorita;
    String progetto;


    public Ad4Progetto getDomainObject () {
        return Ad4Progetto.get(this.progetto)
    }

    public Ad4Progetto copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
