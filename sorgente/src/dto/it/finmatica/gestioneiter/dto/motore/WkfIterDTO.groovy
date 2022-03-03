package it.finmatica.gestioneiter.dto.motore

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.dto.configuratore.iter.WkfCfgIterDTO
import it.finmatica.gestioneiter.motore.WkfIter
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class WkfIterDTO implements it.finmatica.dto.DTO<WkfIter> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    WkfCfgIterDTO cfgIter;
    Date dataFine;
    Date dataInizio;
    Date dateCreated;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    WkfStepDTO stepCorrente;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;


    public WkfIter getDomainObject () {
        return WkfIter.get(this.id)
    }

    public WkfIter copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
