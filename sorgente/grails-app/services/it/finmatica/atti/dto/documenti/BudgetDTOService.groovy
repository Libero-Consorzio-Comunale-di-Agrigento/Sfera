package it.finmatica.atti.dto.documenti

import it.finmatica.atti.dizionari.TipoBudget
import it.finmatica.atti.documenti.Budget
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IProposta
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.dto.dizionari.TipoBudgetDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.Ce4Conto
import it.finmatica.atti.integrazioni.Ce4Fornitore
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class BudgetDTOService {

	public Budget salva (BudgetDTO budgetDTO) {
		// per prima cosa salvo l'budget:
		Budget budget = budgetDTO.getDomainObject()?:new Budget();
        TipoBudget tipoBudget = getTipoBudget(budgetDTO.tipoBudget)

        // in caso di integrazione con CE4 verifico correttezza dati
        if (Impostazioni.CONTABILITA.valore == "integrazioneContabilitaCe4") {
            if (budgetDTO?.contoEconomico) {
                List<Ce4Conto> ce4ContoList = Ce4Conto.createCriteria().list() {
                    eq("contoEsteso", budgetDTO?.contoEconomico)
                }
                if (ce4ContoList.size()==0) {
                    throw new AttiRuntimeException("Non è possibile effettuare il salvataggio del budget ${tipoBudget.titolo}, il Conto Economico deve avere valido.")
                }
            }
            if (!budgetDTO?.contoEconomico) {
                throw new AttiRuntimeException("Non è possibile effettuare il salvataggio del budget ${tipoBudget.titolo}, il Conto Economico deve avere valorizzato.")
            }
            if (budgetDTO?.codiceFornitore) {
                List<Ce4Fornitore> ce4FornitoreList = Ce4Fornitore.createCriteria().list() {
                    eq("contoFornitore", budgetDTO?.codiceFornitore)
                }
                if (ce4FornitoreList.size()==0) {
                    throw new AttiRuntimeException("Non è possibile effettuare il salvataggio del budget ${tipoBudget.titolo}, il Conto Fornitore deve avere valido.")
                }
            }
            if (!budgetDTO?.dataInizioValidita  || !budgetDTO?.dataFineValidita) {
                throw new AttiRuntimeException("Non è possibile effettuare il salvataggio del budget ${tipoBudget.titolo}, il Periodo di validità deve avere valido.")
            }
        }

        // controllo validità data inizio e fine validita
        if (budgetDTO.dataInizioValidita && budgetDTO.dataFineValidita) {
            if (budgetDTO.dataInizioValidita > budgetDTO.dataFineValidita) {
                throw new AttiRuntimeException("Non è possibile effettuare il salvataggio del budget ${tipoBudget.titolo}, data inizione validità maggiore di data fine validità.")
            }
            String annoInzioValidita = budgetDTO.dataInizioValidita?.format("yyyy")
            String annoFineValidita = budgetDTO.dataFineValidita?.format("yyyy")
            if (annoInzioValidita != annoFineValidita) {
                throw new AttiRuntimeException("Non è possibile effettuare il salvataggio del budget ${tipoBudget.titolo}, anni diversi in intervallo validità.")
            }
        }

        if (budget.id > 0) {
            budget.contoEconomico       = budgetDTO.contoEconomico
            budget.codiceProgetto       = budgetDTO.codiceProgetto
            budget.codiceFornitore      = budgetDTO.codiceFornitore
            budget.dataInizioValidita   = budgetDTO.dataInizioValidita
            budget.dataFineValidita     = budgetDTO.dataFineValidita
            budget = budget.save()
            return budget
        }
        if (tipoBudget?.importoDisponibile - budgetDTO.importo < 0) {
            throw new AttiRuntimeException("Non è possibile effettuare il salvataggio del budget ${tipoBudget.titolo}, l'importo richiesto supera quello disponibile.")
        }
        else if (Impostazioni.GESTIONE_FONDI.abilitato && budgetDTO.importo > Impostazioni.GESTIONE_BUDGET_IMPORTO_MASSIMO.valoreInt) {
            throw new AttiRuntimeException("Non è possibile effettuare il salvataggio del budget ${tipoBudget.titolo}, l'importo richiesto supera l'importo massimo.")
        }
        else if (Impostazioni.GESTIONE_FONDI.abilitato && !controllaImportoMensile(budgetDTO.getProposta().domainObject, budgetDTO.importo)) {
            throw new AttiRuntimeException("Non è possibile effettuare il salvataggio del budget ${tipoBudget.titolo}, viene superato l'importo massimo mensile.")
        }
        else{
            int sequenza = 1
            if (budgetDTO.determina != null) {
                sequenza = Budget.countByDeterminaAndTipoBudget(budgetDTO.determina?.domainObject, budgetDTO.tipoBudget?.domainObject) + 1
            } else if (budgetDTO.propostaDelibera != null){
                sequenza = Budget.countByPropostaDeliberaAndTipoBudget(budgetDTO.propostaDelibera?.domainObject, budgetDTO.tipoBudget?.domainObject) + 1
            }

            budget.determina 	  		= budgetDTO.determina?.domainObject
            budget.propostaDelibera     = budgetDTO.propostaDelibera?.domainObject
            budget.tipoBudget           = budgetDTO.tipoBudget?.domainObject
            budget.contoEconomico       = budgetDTO.contoEconomico
            budget.codiceProgetto       = budgetDTO.codiceProgetto
            budget.codiceFornitore      = budgetDTO.codiceFornitore

            budget.dataInizioValidita   = budgetDTO.dataInizioValidita
            budget.dataFineValidita     = budgetDTO.dataFineValidita

            budget.importo              = budgetDTO.importo
            budget.sequenza             = sequenza
            budget.approvato            = budgetDTO.approvato
            budget.valido               = budgetDTO.valido

            if (Impostazioni.GESTIONE_FONDI.abilitato){
                budget.approvato = true
            }
            budget = budget.save()

            // controllo la disponibilità dei budget
            tipoBudget.importoDisponibile -= budgetDTO.importo
            tipoBudget.importoPrenotato   += budgetDTO.importo
            tipoBudget.save(failOnError: true)
        }

        return budget
	}


    public salvaBudget(def lista){
        for (def budget: lista){
            salva(budget)
        }
    }


	public void eliminaBudget (BudgetDTO budgetDTO) {
		if (budgetDTO.id > 0) {
            Budget budget = budgetDTO.getDomainObject()
            if (budgetDTO.approvato && Impostazioni.GESTIONE_FONDI.disabilitato) {
                throw new AttiRuntimeException("Non è possibile eliminare un budget già approvato")
            } else {
                TipoBudget tipoBudget = getTipoBudget(budgetDTO.tipoBudget)
                tipoBudget.importoDisponibile += budgetDTO.importo
                tipoBudget.importoPrenotato -= budgetDTO.importo
                tipoBudget.save(failOnError: true)
            }
            budget.delete(failOnError: true)
        }
	}

    public void approvaBudget (BudgetDTO budgetDTO) {
        Budget budget = budgetDTO.getDomainObject()
        if (budgetDTO.approvato){
            throw new AttiRuntimeException("Non è possibile approvare un budget già approvato")
        }
        else  {
            budget.approvato = true
            budget.save(failOnError: true)
        }
    }

    public void autorizzaBudget (BudgetDTO budgetDTO) {
        Budget budget = budgetDTO.getDomainObject()
        TipoBudget tipoBudget = getTipoBudget(budgetDTO.tipoBudget)
        tipoBudget.importoAutorizzato += budgetDTO.importo
        tipoBudget.importoPrenotato -= budgetDTO.importo
        tipoBudget.save(failOnError: true)
    }

    public void annullaBudget (BudgetDTO budgetDTO) {
        Budget budget = budgetDTO.getDomainObject()
        TipoBudget tipoBudget = getTipoBudget(budgetDTO.tipoBudget)
        if ( !budget.annullato ) {
            tipoBudget.importoPrenotato -= budgetDTO.importo
            tipoBudget.importoDisponibile += budgetDTO.importo
            tipoBudget.save(failOnError: true)
            budget.annullato = true
            budget.save(failOnError: true)
        }
    }

    private TipoBudget getTipoBudget(TipoBudgetDTO tipoBudgetDTO){
        return TipoBudget.createCriteria().get{
            eq("id", tipoBudgetDTO.id)
            delegate.lock(true)	// utilizzo la select for update
        }
    }

    public boolean controllaImportoMensile(IProposta proposta, BigDecimal importo){
        BigDecimal totale = importo;
        Calendar c = Calendar.getInstance();
        c.setTime(proposta.dateCreated)
        c.set(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        Date dataInizio = c.getTime();
        c.add(Calendar.MONTH, 1)
        Date dataFine = c.getTime()

        So4UnitaPubb unitaPubb = proposta.unitaProponente
        BigDecimal somma = (Determina.createCriteria().get() {
            projections {
                budgets {
                    sum("importo")
                }
            }
            soggetti{
                eq ("unitaSo4", unitaPubb)
                eq("tipoSoggetto.codice", TipoSoggetto.UO_PROPONENTE)
            }
            eq ("valido", true)
            between("dateCreated", dataInizio, dataFine)

        }?:0)+ (PropostaDelibera.createCriteria().get() {
            projections {
                budgets {
                    sum("importo")
                }
            }
            soggetti{
                eq ("unitaSo4", unitaPubb)
                eq("tipoSoggetto.codice", TipoSoggetto.UO_PROPONENTE)
            }
            eq ("valido", true)
            between("dateCreated", dataInizio, dataFine)

        }?:0)
        return somma+importo <= Impostazioni.GESTIONE_BUDGET_IMPORTO_MENSILE.valoreInt;
    }

}
