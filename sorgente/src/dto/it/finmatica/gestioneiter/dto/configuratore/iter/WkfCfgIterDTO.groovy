package it.finmatica.gestioneiter.dto.configuratore.iter

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.dto.configuratore.dizionari.WkfTipoOggettoDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class WkfCfgIterDTO implements it.finmatica.dto.DTO<WkfCfgIter> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    List<WkfCfgStepDTO> cfgStep;
    Date dateCreated;
    String descrizione;
    So4AmministrazioneDTO ente;
    Long idCfgIterRevisione;
    Date lastUpdated;
    String nome;
    long progressivo;
    long revisione;
    String stato;
    WkfTipoOggettoDTO tipoOggetto;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean verificato;

    public void addToCfgStep (WkfCfgStepDTO wkfCfgStep) {
        if (this.cfgStep == null)
            this.cfgStep = new ArrayList<WkfCfgStepDTO>()
        this.cfgStep.add (wkfCfgStep);
        wkfCfgStep.cfgIter = this
    }

    public void removeFromCfgStep (WkfCfgStepDTO wkfCfgStep) {
        if (this.cfgStep == null)
            this.cfgStep = new ArrayList<WkfCfgStepDTO>()
        this.cfgStep.remove (wkfCfgStep);
        wkfCfgStep.cfgIter = null
    }

    public WkfCfgIter getDomainObject () {
        return WkfCfgIter.get(this.id)
    }

    public WkfCfgIter copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.
}
