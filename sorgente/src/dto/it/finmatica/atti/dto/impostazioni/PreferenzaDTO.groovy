package it.finmatica.atti.dto.impostazioni

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.impostazioni.Preferenza
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

@GrailsCompileStatic
class PreferenzaDTO implements it.finmatica.dto.DTO<Preferenza> {
    private static final long serialVersionUID = 1L;

    Long id
    String codice
    String etichetta
    String descrizione
    String ente
    String nomeMetodo
    String valoreDefault

    public Preferenza getDomainObject () {
        return Preferenza.get(this.id)
    }

    public Preferenza copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

}
