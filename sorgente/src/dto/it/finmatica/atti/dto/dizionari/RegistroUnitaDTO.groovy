package it.finmatica.atti.dto.dizionari

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dizionari.RegistroUnita
import it.finmatica.atti.dto.impostazioni.CaratteristicaTipologiaDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class RegistroUnitaDTO implements it.finmatica.dto.DTO<RegistroUnita> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    CaratteristicaTipologiaDTO caratteristica;
    Date dateCreated;
    Date lastUpdated;
    TipoRegistroDTO tipoRegistro;
    So4UnitaPubbDTO unitaSo4;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;


    public RegistroUnita getDomainObject () {
        return RegistroUnita.get(this.id)
    }

    public RegistroUnita copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
