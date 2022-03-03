package it.finmatica.ad4.dto.dizionari;

import it.finmatica.ad4.dizionari.Ad4Provincia;
import it.finmatica.dto.DtoUtils;

public class Ad4ProvinciaDTO implements it.finmatica.dto.DTO<Ad4Provincia> {
    private static final long serialVersionUID = 1L;

    Long id;
    String denominazione;
    Ad4RegioneDTO regione;
    String sigla;


    public Ad4Provincia getDomainObject () {
        return Ad4Provincia.get(this.id)
    }

    public Ad4Provincia copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
