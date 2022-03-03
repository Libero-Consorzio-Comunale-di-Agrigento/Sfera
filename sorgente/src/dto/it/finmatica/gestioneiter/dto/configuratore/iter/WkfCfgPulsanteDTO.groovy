package it.finmatica.gestioneiter.dto.configuratore.iter

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgPulsante
import it.finmatica.gestioneiter.dto.configuratore.dizionari.WkfAttoreDTO
import it.finmatica.gestioneiter.dto.configuratore.dizionari.WkfPulsanteDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class WkfCfgPulsanteDTO implements it.finmatica.dto.DTO<WkfCfgPulsante> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Set<WkfAttoreDTO> attori;
    WkfCfgStepDTO cfgStep;
    WkfCfgStepDTO cfgStepSuccessivo;
    Date dateCreated;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    WkfPulsanteDTO pulsante;
    int sequenza;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;

    public void addToAttori (WkfAttoreDTO wkfAttore) {
        if (this.attori == null)
            this.attori = new HashSet<WkfAttoreDTO>()
        this.attori.add (wkfAttore);
    }

    public void removeFromAttori (WkfAttoreDTO wkfAttore) {
        if (this.attori == null)
            this.attori = new HashSet<WkfAttoreDTO>()
        this.attori.remove (wkfAttore);
    }

    public WkfCfgPulsante getDomainObject () {
        return WkfCfgPulsante.get(this.id)
    }

    public WkfCfgPulsante copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

	public void setEtichetta(String value) {
		pulsante?.etichetta = value;
	}

	public String getEtichetta() {
		return pulsante?.etichetta;
	}

	public String getIcona() {
		return pulsante?.icona;
	}
}
