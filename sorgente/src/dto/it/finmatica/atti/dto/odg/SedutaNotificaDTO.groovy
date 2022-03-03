package it.finmatica.atti.dto.odg

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dto.dizionari.NotificaDTO
import it.finmatica.atti.odg.SedutaNotifica
import it.finmatica.dto.DtoUtils

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class SedutaNotificaDTO implements it.finmatica.dto.DTO<SedutaNotifica> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Date dataInvio;
    String indirizziEmail;
    NotificaDTO notifica;
    OggettoSedutaDTO oggettoSeduta;
    SedutaDTO seduta;
    Ad4UtenteDTO utenteInvio;


    public SedutaNotifica getDomainObject () {
        return SedutaNotifica.get(this.id)
    }

    public SedutaNotifica copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
