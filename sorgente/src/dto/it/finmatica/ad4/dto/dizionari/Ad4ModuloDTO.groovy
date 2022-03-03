package it.finmatica.ad4.dto.dizionari;

import it.finmatica.ad4.dizionari.Ad4Modulo;
import it.finmatica.dto.DtoUtils;

public class Ad4ModuloDTO implements it.finmatica.dto.DTO<Ad4Modulo> {
    private static final long serialVersionUID = 1L;

    String descrizione;
    String modulo;
    String note;
    Ad4ProgettoDTO progetto;


    public Ad4Modulo getDomainObject () {
        return Ad4Modulo.get(this.modulo)
    }

    public Ad4Modulo copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
