package it.finmatica.atti.dto.odg.dizionari

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.odg.dizionari.OrganoControlloRuolo
import it.finmatica.dto.DtoUtils

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class OrganoControlloRuoloDTO implements it.finmatica.dto.DTO<OrganoControlloRuolo> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Date dateCreated;
    String descrizione;
    Date lastUpdated;
    OrganoControlloDTO organoControllo;
    String titolo;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;


    public OrganoControlloRuolo getDomainObject () {
        return OrganoControlloRuolo.get(this.id)
    }

    public OrganoControlloRuolo copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
