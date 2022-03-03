package it.finmatica.gestioneiter.dto.motore

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.dto.configuratore.iter.WkfCfgStepDTO
import it.finmatica.gestioneiter.motore.WkfStep

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class WkfStepDTO implements it.finmatica.dto.DTO<WkfStep> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Set<WkfAttoreStepDTO> attori;
    WkfCfgStepDTO cfgStep;
    Date dataFine;
    Date dataInizio;
    Date dateCreated;
    WkfIterDTO iter;
    Date lastUpdated;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;

    public void addToAttori (WkfAttoreStepDTO wkfAttoreStep) {
        if (this.attori == null)
            this.attori = new HashSet<WkfAttoreStepDTO>()
        this.attori.add (wkfAttoreStep);
        wkfAttoreStep.step = this
    }

    public void removeFromAttori (WkfAttoreStepDTO wkfAttoreStep) {
        if (this.attori == null)
            this.attori = new HashSet<WkfAttoreStepDTO>()
        this.attori.remove (wkfAttoreStep);
        wkfAttoreStep.step = null
    }

    public WkfStep getDomainObject () {
        return WkfStep.get(this.id)
    }

    public WkfStep copyToDomainObject () {
        return null
    }

	/* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

}
