package it.finmatica.atti.dto.impostazioni

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

@GrailsCompileStatic
class MappingIntegrazioneDTO implements it.finmatica.dto.DTO<MappingIntegrazione> {
    private static final long serialVersionUID = 1L;

    Long id
    String categoria
    String codice
    String valoreInterno
    String valoreEsterno
    int sequenza
    String descrizione
    So4AmministrazioneDTO ente

    public MappingIntegrazione getDomainObject () {
        return MappingIntegrazione.get(this.id)
    }

    public MappingIntegrazione copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

    ParametroIntegrazione parametroIntegrazione

    String getTitolo () {
        return parametroIntegrazione.titolo
    }
}
