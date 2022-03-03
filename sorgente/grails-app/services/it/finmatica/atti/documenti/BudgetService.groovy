package it.finmatica.atti.documenti

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.dto.documenti.BudgetDTOService
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.gestioneiter.Attore
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class BudgetService {

    BudgetDTOService budgetDTOService

    public void autorizzaBudget(IProposta proposta){
        for(Budget budget: proposta.budgets){
            budgetDTOService.autorizzaBudget(budget.toDTO())
        }
    }

    public void annullaBudget(IProposta proposta){
        for(Budget budget: proposta.budgets){
            budgetDTOService.annullaBudget(budget.toDTO())
        }
    }

    public Ad4Utente prossimoCheDeveApprovare(IProposta proposta) {
        def budgets = proposta.budgets
        def utente = proposta.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4

        if (utente == null) {
            return null
        }


        for (Budget budget : budgets.sort { it.sequenza }) {
            if (budget.approvato) {
                continue;
            } else {
                if (budget.tipoBudget.utenteAd4.id.equals(utente.id)) {
                    budget.approvato = true
                    budget.save()
                } else {
                    return budget.tipoBudget.utenteAd4
                }
            }
        }
    }

    public So4UnitaPubb prossimaUnitaCheDeveApprovare(IProposta proposta) {
        def budgets = proposta.budgets
        def utente = proposta.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4

        if (utente == null) {
            return null
        }


        for (Budget budget : budgets.sort { it.sequenza }) {
            if (budget.approvato) {
                continue;
            } else {
                if (budget.tipoBudget.utenteAd4.id.equals(utente.id)) {
                    budget.approvato = true
                    budget.save()
                } else {
                    return budget.tipoBudget.unitaSo4
                }
            }
        }
    }

    public boolean esistonoBudgetDaApprovare(IProposta proposta) {
        def budgets = proposta.budgets
        def utente = proposta.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4

        if (utente == null) {
            return null
        }


        for (Budget budget : budgets.sort { it.sequenza }) {
            if (budget.approvato) {
                continue;
            } else {
                if (budget.tipoBudget.utenteAd4.id.equals(utente.id)) {
                    budget.approvato = true
                    budget.save()
                } else {
                    return true
                }
            }
        }
        return false
    }

    public void approvaProssimoBudget(IProposta proposta) {
        for (Budget budget : proposta.budgets.sort { it.sequenza }) {
            if (budget.approvato) {
                continue;
            } else {
                budget.approvato = true
                budget.save()
            }
        }
    }

    public void annullaApprovazioneBudget(IProposta proposta) {
        for (Budget budget : proposta.budgets.sort { it.sequenza }) {
            if (budget.approvato) {
                budget.approvato = false
                budget.save()
            }
        }
    }
}
