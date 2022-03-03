package it.finmatica.so4.dto.struttura

import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.struttura.So4Componente

public class So4ComponenteDTO implements it.finmatica.dto.DTO<So4Componente> {
    private static final long serialVersionUID = 1L;

    Long id;
    Date al;
    Integer ciSoggettoGp4;
    Date dal;
    String nominativoSoggetto;
    So4OtticaDTO ottica;
    Long progrUnita;
    As4SoggettoCorrenteDTO soggetto;
    String stato;


    public So4Componente getDomainObject () {
        return So4Componente.get(this.id)
    }

    public So4Componente copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
