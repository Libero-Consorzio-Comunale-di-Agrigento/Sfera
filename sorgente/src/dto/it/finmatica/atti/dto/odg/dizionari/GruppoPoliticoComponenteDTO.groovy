package it.finmatica.atti.dto.odg.dizionari

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.odg.dizionari.GruppoPoliticoComponente
import it.finmatica.atti.odg.dizionari.OrganoControlloComponente
import it.finmatica.atti.dto.odg.dizionari.GruppoPoliticoDTO
import it.finmatica.dto.DtoUtils

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class GruppoPoliticoComponenteDTO implements it.finmatica.dto.DTO<GruppoPoliticoComponente> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    As4SoggettoCorrenteDTO componente;
    Date dateCreated;
    Date lastUpdated;
    GruppoPoliticoDTO gruppoPolitico;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;


    public GruppoPoliticoComponente getDomainObject () {
        return GruppoPoliticoComponente.get(this.id)
    }

    public GruppoPoliticoComponente copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
