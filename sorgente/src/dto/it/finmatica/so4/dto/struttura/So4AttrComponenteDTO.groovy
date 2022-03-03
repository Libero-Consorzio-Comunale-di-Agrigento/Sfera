package it.finmatica.so4.dto.struttura

import it.finmatica.dto.DtoUtils
import it.finmatica.so4.struttura.So4AttrComponente

public class So4AttrComponenteDTO implements it.finmatica.dto.DTO<So4AttrComponente> {
    private static final long serialVersionUID = 1L;

    Long id;
    Date al;
    String assegnazionePrevalente;
    String codiceIncarico;
    So4ComponenteDTO componente;
    Date dal;
    String descrizioneIncarico;
    String eMail;
    String fax;
    String gradazione;
    Integer ordinamento;
    BigDecimal percentualeImpiego;
    Boolean seResponsabile;
    String telefono;
    String tipoAssegnazione;


    public So4AttrComponente getDomainObject () {
        return So4AttrComponente.get(this.id)
    }

    public So4AttrComponente copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
