package it.finmatica.atti.dto.odg

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.odg.OggettoSedutaDelega
import it.finmatica.atti.dto.dizionari.DelegaDTO

@GrailsCompileStatic
class OggettoSedutaDelegaDTO implements it.finmatica.dto.DTO<OggettoSedutaDelega> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;

    OggettoSedutaDTO oggettoSeduta
    DelegaDTO         delega
    int               sequenza

    Date         dateCreated
    Ad4UtenteDTO utenteIns

    OggettoSedutaDelega getDomainObject () {
        return OggettoSedutaDelega.get(this.id)
    }

    OggettoSedutaDelega copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

}
