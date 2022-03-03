package it.finmatica.gestioneiter.dto.motore

import it.finmatica.ad4.dto.autenticazione.Ad4RuoloDTO
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.motore.WkfAttoreStep
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class WkfAttoreStepDTO implements it.finmatica.dto.DTO<WkfAttoreStep> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Date dateCreated;
    Date lastUpdated;
    Ad4RuoloDTO ruoloAd4;
    WkfStepDTO step;
    So4UnitaPubbDTO unitaSo4;
    Ad4UtenteDTO utenteAd4;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;


    public WkfAttoreStep getDomainObject () {
        return WkfAttoreStep.get(this.id)
    }

    public WkfAttoreStep copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
