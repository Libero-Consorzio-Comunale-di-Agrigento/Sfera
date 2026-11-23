package it.finmatica.atti.dto.documenti

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.PropostaDeliberaDelega
import it.finmatica.atti.dto.dizionari.DelegaDTO

@GrailsCompileStatic
class PropostaDeliberaDelegaDTO implements it.finmatica.dto.DTO<PropostaDeliberaDelega> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;

    PropostaDeliberaDTO propostaDelibera
    DelegaDTO           delega
    int                 sequenza

    Date         dateCreated
    Ad4UtenteDTO utenteIns

    PropostaDeliberaDelega getDomainObject () {
        return PropostaDeliberaDelega.get(this.id)
    }

    PropostaDeliberaDelega copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

}
