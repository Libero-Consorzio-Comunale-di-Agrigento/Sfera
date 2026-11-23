package it.finmatica.atti.dto.odg

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.odg.OggettoSedutaCollegato

@GrailsCompileStatic
public class OggettoSedutaCollegatoDTO implements it.finmatica.dto.DTO<OggettoSedutaCollegato> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Date dateCreated;
    Date lastUpdated;
    OggettoSedutaDTO oggettoSeduta;
    SedutaDTO seduta;
    int sequenzaConvocazione;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;


    public OggettoSedutaCollegato getDomainObject () {
        return OggettoSedutaCollegato.get(this.id)
    }

    public OggettoSedutaCollegato copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.
}
