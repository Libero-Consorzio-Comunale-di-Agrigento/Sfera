package it.finmatica.gestioneiter.dto.configuratore.dizionari

import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAzioneParametro

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class WkfAzioneParametroDTO implements it.finmatica.dto.DTO<WkfAzioneParametro> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    WkfAzioneDTO azione;
    String codice;
    String descrizione;


    public WkfAzioneParametro getDomainObject () {
        return WkfAzioneParametro.get(this.id)
    }

    public WkfAzioneParametro copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.
}
