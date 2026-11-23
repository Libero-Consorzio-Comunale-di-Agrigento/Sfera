package it.finmatica.atti.dto.odg

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.odg.CommissioneCollegata

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class CommissioneCollegataDTO implements it.finmatica.dto.DTO<CommissioneCollegata> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    CommissioneDTO commissione;
    CommissioneDTO commissioneCollegata;
    Date dateCreated;
    Date lastUpdated;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;

    public CommissioneCollegata getDomainObject () {
        return CommissioneCollegata.get(this.id)
    }

    public CommissioneCollegata copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
