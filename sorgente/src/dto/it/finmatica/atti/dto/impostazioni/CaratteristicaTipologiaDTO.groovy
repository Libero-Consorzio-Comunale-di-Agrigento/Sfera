package it.finmatica.atti.dto.impostazioni

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.impostazioni.CaratteristicaTipologia
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.dto.configuratore.dizionari.WkfTipoOggettoDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class CaratteristicaTipologiaDTO implements it.finmatica.dto.DTO<CaratteristicaTipologia> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    List<CaratteristicaTipoSoggettoDTO> caratteristicheTipiSoggetto;
    Date dateCreated;
    String descrizione;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    String layoutSoggetti;
    WkfTipoOggettoDTO tipoOggetto;
    String titolo;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;

    public void addToCaratteristicheTipiSoggetto (CaratteristicaTipoSoggettoDTO caratteristicaTipoSoggetto) {
        if (this.caratteristicheTipiSoggetto == null)
            this.caratteristicheTipiSoggetto = new ArrayList<CaratteristicaTipoSoggettoDTO>()
        this.caratteristicheTipiSoggetto.add (caratteristicaTipoSoggetto);
        caratteristicaTipoSoggetto.caratteristicaTipologia = this
    }

    public void removeFromCaratteristicheTipiSoggetto (CaratteristicaTipoSoggettoDTO caratteristicaTipoSoggetto) {
        if (this.caratteristicheTipiSoggetto == null)
            this.caratteristicheTipiSoggetto = new ArrayList<CaratteristicaTipoSoggettoDTO>()
        this.caratteristicheTipiSoggetto.remove (caratteristicaTipoSoggetto);
        caratteristicaTipoSoggetto.caratteristicaTipologia = null
    }

    public CaratteristicaTipologia getDomainObject () {
        return CaratteristicaTipologia.get(this.id)
    }

    public CaratteristicaTipologia copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
