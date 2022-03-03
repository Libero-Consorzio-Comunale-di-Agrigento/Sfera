package it.finmatica.gestioneiter.dto.configuratore.iter

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgCompetenza
import it.finmatica.gestioneiter.dto.configuratore.dizionari.WkfAttoreDTO
import it.finmatica.gestioneiter.dto.configuratore.dizionari.WkfPulsanteDTO
import it.finmatica.gestioneiter.dto.configuratore.dizionari.WkfTipoOggettoDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class WkfCfgCompetenzaDTO implements it.finmatica.dto.DTO<WkfCfgCompetenza> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    String assegnazione;
    WkfAttoreDTO attore;
    boolean cancellazione;
    WkfCfgStepDTO cfgStep;
    boolean creazione;
    Date dateCreated;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    boolean lettura;
    boolean modifica;
    WkfPulsanteDTO pulsante;
    WkfPulsanteDTO pulsanteProvenienza;
    WkfTipoOggettoDTO tipoOggetto;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;


    public WkfCfgCompetenza getDomainObject () {
        return WkfCfgCompetenza.get(this.id)
    }

    public WkfCfgCompetenza copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

	public boolean isFromPulsante(){
		return (pulsanteProvenienza != null)
	}

	public boolean isIngresso() {
		return WkfCfgCompetenza.ASSEGNAZIONE_IN.equalsIgnoreCase(assegnazione)
	}

	public boolean isUscita() {
		return WkfCfgCompetenza.ASSEGNAZIONE_OUT.equalsIgnoreCase(assegnazione)
	}


}
