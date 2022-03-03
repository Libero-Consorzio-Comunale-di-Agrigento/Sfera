package it.finmatica.gestioneiter.dto.configuratore.dizionari

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.configuratore.dizionari.WkfPulsante
import it.finmatica.gestioneiter.dto.configuratore.iter.WkfCfgCompetenzaDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class WkfPulsanteDTO implements it.finmatica.dto.DTO<WkfPulsante> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    List<WkfAzioneDTO> azioni;
    Set<WkfCfgCompetenzaDTO> cfgCompetenze;
    boolean competenzaInModifica;
    WkfAzioneDTO condizioneVisibilita;
    Date dateCreated;
    String descrizione;
    So4AmministrazioneDTO ente;
    String etichetta;
    String icona;
    Date lastUpdated;
    String messaggioConferma;
    WkfTipoOggettoDTO tipoOggetto;
    String tooltip;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;

    public void addToAzioni (WkfAzioneDTO wkfAzione) {
        if (this.azioni == null)
            this.azioni = new ArrayList<WkfAzioneDTO>()
        this.azioni.add (wkfAzione);
    }

    public void removeFromAzioni (WkfAzioneDTO wkfAzione) {
        if (this.azioni == null)
            this.azioni = new ArrayList<WkfAzioneDTO>()
        this.azioni.remove (wkfAzione);
    }
    public void addToCfgCompetenze (WkfCfgCompetenzaDTO wkfCfgCompetenza) {
        if (this.cfgCompetenze == null)
            this.cfgCompetenze = new HashSet<WkfCfgCompetenzaDTO>()
        this.cfgCompetenze.add (wkfCfgCompetenza);
        wkfCfgCompetenza.pulsante = this
    }

    public void removeFromCfgCompetenze (WkfCfgCompetenzaDTO wkfCfgCompetenza) {
        if (this.cfgCompetenze == null)
            this.cfgCompetenze = new HashSet<WkfCfgCompetenzaDTO>()
        this.cfgCompetenze.remove (wkfCfgCompetenza);
        wkfCfgCompetenza.pulsante = null
    }

    public WkfPulsante getDomainObject () {
        return WkfPulsante.get(this.id)
    }

    public WkfPulsante copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.



}
