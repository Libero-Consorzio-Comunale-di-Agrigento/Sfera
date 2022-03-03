package it.finmatica.gestioneiter.dto.configuratore.iter

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgStep
import it.finmatica.gestioneiter.dto.configuratore.dizionari.WkfAttoreDTO
import it.finmatica.gestioneiter.dto.configuratore.dizionari.WkfAzioneDTO
import it.finmatica.gestioneiter.dto.configuratore.dizionari.WkfGruppoStepDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class WkfCfgStepDTO implements it.finmatica.dto.DTO<WkfCfgStep> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    WkfAttoreDTO attore;
    List<WkfAzioneDTO> azioniIngresso;
    List<WkfAzioneDTO> azioniUscita;
    Set<WkfCfgCompetenzaDTO> cfgCompetenze;
    WkfCfgIterDTO cfgIter;
    List<WkfCfgPulsanteDTO> cfgPulsanti;
    WkfCfgStepDTO cfgStepSuccessivoNo;
    WkfCfgStepDTO cfgStepSuccessivoSblocco;
    WkfCfgStepDTO cfgStepSuccessivoSi;
    WkfAzioneDTO condizione;
    WkfAzioneDTO condizioneSblocco;
    Date dateCreated;
    String descrizione;
    WkfGruppoStepDTO gruppoStep;
    Date lastUpdated;
    String nome;
    int sequenza;
    String titolo;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;

    public void addToAzioniIngresso (WkfAzioneDTO wkfAzione) {
        if (this.azioniIngresso == null)
            this.azioniIngresso = new ArrayList<WkfAzioneDTO>()
        this.azioniIngresso.add (wkfAzione);
    }

    public void removeFromAzioniIngresso (WkfAzioneDTO wkfAzione) {
        if (this.azioniIngresso == null)
            this.azioniIngresso = new ArrayList<WkfAzioneDTO>()
        this.azioniIngresso.remove (wkfAzione);
    }
    public void addToAzioniUscita (WkfAzioneDTO wkfAzione) {
        if (this.azioniUscita == null)
            this.azioniUscita = new ArrayList<WkfAzioneDTO>()
        this.azioniUscita.add (wkfAzione);
    }

    public void removeFromAzioniUscita (WkfAzioneDTO wkfAzione) {
        if (this.azioniUscita == null)
            this.azioniUscita = new ArrayList<WkfAzioneDTO>()
        this.azioniUscita.remove (wkfAzione);
    }
    public void addToCfgCompetenze (WkfCfgCompetenzaDTO wkfCfgCompetenza) {
        if (this.cfgCompetenze == null)
            this.cfgCompetenze = new HashSet<WkfCfgCompetenzaDTO>()
        this.cfgCompetenze.add (wkfCfgCompetenza);
        wkfCfgCompetenza.cfgStep = this
    }

    public void removeFromCfgCompetenze (WkfCfgCompetenzaDTO wkfCfgCompetenza) {
        if (this.cfgCompetenze == null)
            this.cfgCompetenze = new HashSet<WkfCfgCompetenzaDTO>()
        this.cfgCompetenze.remove (wkfCfgCompetenza);
        wkfCfgCompetenza.cfgStep = null
    }
    public void addToCfgPulsanti (WkfCfgPulsanteDTO wkfCfgPulsante) {
        if (this.cfgPulsanti == null)
            this.cfgPulsanti = new ArrayList<WkfCfgPulsanteDTO>()
        this.cfgPulsanti.add (wkfCfgPulsante);
        wkfCfgPulsante.cfgStep = this
    }

    public void removeFromCfgPulsanti (WkfCfgPulsanteDTO wkfCfgPulsante) {
        if (this.cfgPulsanti == null)
            this.cfgPulsanti = new ArrayList<WkfCfgPulsanteDTO>()
        this.cfgPulsanti.remove (wkfCfgPulsante);
        wkfCfgPulsante.cfgStep = null
    }

    public WkfCfgStep getDomainObject () {
        return WkfCfgStep.get(this.id)
    }

    public WkfCfgStep copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

	private boolean modificato = false;
}
