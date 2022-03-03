package it.finmatica.atti.dto.impostazioni

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.impostazioni.Preferenza
import it.finmatica.atti.impostazioni.PreferenzaUtente

@GrailsCompileStatic
class PreferenzaUtenteDTO implements it.finmatica.dto.DTO<PreferenzaUtente> {
    private static final long serialVersionUID = 1L;

    Long id
    PreferenzaDTO preferenza
    Ad4UtenteDTO utente
    String valore

    public PreferenzaUtente getDomainObject () {
        return PreferenzaUtente.get(this.id)
    }

    public PreferenzaUtente copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

}
