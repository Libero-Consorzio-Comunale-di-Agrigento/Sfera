package it.finmatica.atti.dto.documenti

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dizionari.TipoBudget
import it.finmatica.atti.documenti.Budget
import it.finmatica.atti.dto.dizionari.TipoBudgetDTO
import it.finmatica.atti.dto.dizionari.TipoDatoAggiuntivoValoreDTO
import it.finmatica.atti.dto.documenti.DeterminaDTO
import it.finmatica.atti.dto.documenti.PropostaDeliberaDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

@GrailsCompileStatic
class BudgetDTO implements it.finmatica.dto.DTO<Budget> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;

    // Collegamento ai vari documenti:
    DeterminaDTO determina
    PropostaDeliberaDTO propostaDelibera

    TipoBudgetDTO tipoBudget
    BigDecimal  importo
    boolean     approvato
    boolean     annullato
    int         sequenza
    String      contoEconomico
    String      codiceProgetto
    String      codiceFornitore
    Date dataInizioValidita
    Date dataFineValidita

    boolean valido = true

    So4AmministrazioneDTO ente
    Date dateCreated
    Ad4UtenteDTO utenteIns
    Date lastUpdated
    Ad4UtenteDTO utenteUpd

    Budget getDomainObject () {
        return Budget.get(this.id)
    }

    Budget copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

    public def getProposta () {
        return determina?:propostaDelibera;
    }

}
