package atti.actions.commons

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.documenti.BudgetService
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.IProposta
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.gestioneiter.Attore
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class BudgetAction {

    BudgetService budgetService

    @Action(tipo	= Action.TipoAzione.CALCOLO_ATTORE,
        tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
        nome		= "Ritorna l'unità associata al prossimo budget da far approvare",
        descrizione = "Ritorna l'unità associata al prossimo budget da far approvare")
    Attore getUnitaBudget (IDocumento documento) {
        IProposta proposta
        if (documento instanceof Delibera) {
            proposta = documento.proposta
        } else {
            proposta = documento
        }

        So4UnitaPubb unitaPubb = budgetService.prossimaUnitaCheDeveApprovare(proposta)
        if (unitaPubb != null){
            return new Attore (unitaSo4: unitaPubb)
        }
        return null
    }

    @Action(tipo	= Action.TipoAzione.CALCOLO_ATTORE,
        tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
        nome		= "Ritorna il dirigente associato al prossimo budget da far approvare",
        descrizione = "Ritorna il dirigente associato al prossimo budget da far approvare")
    Attore getDirigenteBudget (IDocumento documento) {
        IProposta proposta
        if (documento instanceof Delibera) {
            proposta = documento.proposta
        } else {
            proposta = documento
        }

        Ad4Utente utente = budgetService.prossimoCheDeveApprovare(proposta)
        if (utente != null){
            return new Attore (utenteAd4: utente)
        }
        return null
    }

    @Action(tipo	= Action.TipoAzione.CONDIZIONE,
        tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
        nome		= "Esiste un budget da far approvare?",
        descrizione = "Ritorna TRUE solo se esiste un budget da far approvare.")
    boolean isBudgetDaApprovare (IDocumento documento) {
        IProposta proposta
        if (documento instanceof Delibera) {
            proposta = documento.proposta
        } else {
            proposta = documento
        }

        return budgetService.esistonoBudgetDaApprovare(proposta)
    }

    @Action(tipo	= Action.TipoAzione.AUTOMATICA,
        tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
        nome		= "Approva il budget",
        descrizione = "Approva il prossimo budget associato alla proposta")
    public IProposta approvaBudget (IDocumento documento) {
        IProposta proposta
        if (documento instanceof Delibera) {
            proposta = documento.proposta
        } else {
            proposta = documento
        }

        budgetService.approvaProssimoBudget(proposta)
        return proposta
    }

    @Action(tipo	= Action.TipoAzione.AUTOMATICA,
        tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
        nome		= "Imposta i budget da approvare",
        descrizione = "Imposta tutti i budget associati alla proposta come da approvare")
    public IProposta annullaApprovazioneBudget (IDocumento documento) {
        IProposta proposta
        if (documento instanceof Delibera) {
            proposta = documento.proposta
        } else {
            proposta = documento
        }

        budgetService.annullaApprovazioneBudget(proposta)
        return proposta
    }

    @Action(tipo	= Action.TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
            nome		= "Annulla i budget associati alla proposta",
            descrizione = "Annulla tutti i budget associati alla proposta")
    public IProposta annullaBudget (IDocumento documento) {
        IProposta proposta
        if (documento instanceof Delibera) {
            proposta = documento.proposta
        } else {
            proposta = documento
        }

        budgetService.annullaBudget(proposta)
        return proposta
    }
}