package it.finmatica.atti.dto.documenti

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.DatoAggiuntivo
import it.finmatica.atti.dto.dizionari.TipoDatoAggiuntivoValoreDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

@GrailsCompileStatic
class DatoAggiuntivoDTO implements it.finmatica.dto.DTO<DatoAggiuntivo> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;

    // Collegamento ai vari documenti:
    DeterminaDTO determina
    DeliberaDTO delibera
    PropostaDeliberaDTO propostaDelibera

    // codice del datoAggiuntivo
    String codice
    String valore

    TipoDatoAggiuntivoValoreDTO valoreTipoDato

    boolean valido = true
    Date validoDal
    Date validoAl

    So4AmministrazioneDTO ente
    Date dateCreated
    Ad4UtenteDTO utenteIns
    Date lastUpdated
    Ad4UtenteDTO utenteUpd

    DatoAggiuntivo getDomainObject () {
        return DatoAggiuntivo.get(this.id)
    }

    DatoAggiuntivo copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
