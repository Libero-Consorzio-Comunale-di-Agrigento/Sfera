package it.finmatica.atti.dto.impostazioni

import it.finmatica.ad4.dto.autenticazione.Ad4RuoloDTO
import it.finmatica.atti.impostazioni.CaratteristicaTipoSoggetto
import it.finmatica.dto.DtoUtils

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class CaratteristicaTipoSoggettoDTO implements it.finmatica.dto.DTO<CaratteristicaTipoSoggetto> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    CaratteristicaTipologiaDTO caratteristicaTipologia;
    RegolaCalcoloDTO regolaCalcoloDefault;
    RegolaCalcoloDTO regolaCalcoloLista;
    Ad4RuoloDTO ruolo;
    int sequenza;
    TipoSoggettoDTO tipoSoggetto;
    TipoSoggettoDTO tipoSoggettoPartenza;


    public CaratteristicaTipoSoggetto getDomainObject () {
        return CaratteristicaTipoSoggetto.get(this.id)
    }

    public CaratteristicaTipoSoggetto copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
