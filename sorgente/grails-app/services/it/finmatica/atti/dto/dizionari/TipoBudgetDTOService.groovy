package it.finmatica.atti.dto.dizionari

import it.finmatica.atti.documenti.Budget
import it.finmatica.atti.documenti.storico.TipoBudgetStorico
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.dizionari.TipoBudget
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAttoreService

class TipoBudgetDTOService {

    WkfAttoreService wkfAttoreService

    TipoBudgetDTO salva(TipoBudgetDTO tipoBudgetDTO) {
        TipoBudget tipoBudget = new TipoBudget()
        if (tipoBudgetDTO.id > 0) {
            tipoBudget = TipoBudget.get(tipoBudgetDTO.id)
        }
        else {
            if (Impostazioni.GESTIONE_FONDI.abilitato) {
                int presente = TipoBudget.countByTipoAndUnitaSo4AndAnnoAndAttivo(tipoBudgetDTO.tipo, tipoBudgetDTO.unitaSo4.domainObject, tipoBudgetDTO.anno, true)
                if (presente) {
                    throw new AttiRuntimeException("${tipoBudgetDTO.tipo} già presente per l'unità e l'anno selezionato")
                }
            }
            tipoBudgetDTO.importoDisponibile = tipoBudgetDTO.importoIniziale
            tipoBudgetDTO.importoPrenotato = 0
            tipoBudgetDTO.importoAutorizzato = 0
        }
        tipoBudget.titolo = tipoBudgetDTO.titolo
        tipoBudget.anno = tipoBudgetDTO.anno
        tipoBudget.tipo = tipoBudgetDTO.tipo
        tipoBudget.contoEconomico = tipoBudgetDTO.contoEconomico
        tipoBudget.importoIniziale = tipoBudgetDTO.importoIniziale
        tipoBudget.importoPrenotato = tipoBudgetDTO.importoPrenotato
        tipoBudget.importoDisponibile = tipoBudgetDTO.importoDisponibile
        tipoBudget.importoAutorizzato = tipoBudgetDTO.importoAutorizzato
        tipoBudget.unitaSo4 = tipoBudgetDTO.unitaSo4.domainObject
        tipoBudget.utenteAd4 = tipoBudgetDTO.utenteAd4.domainObject
        tipoBudget.attivo = tipoBudgetDTO.attivo
        tipoBudget.valido = tipoBudgetDTO.valido

        tipoBudget.save(failOnError: true)
        return tipoBudget.toDTO()
    }

    void salvaStorico(TipoBudgetDTO tipoBudgetDTO, String motivazioni) {
        if (tipoBudgetDTO.id < 0) {
            return;
        }
        TipoBudgetStorico storico = new TipoBudgetStorico()

        storico.importoDisponibile  = tipoBudgetDTO.importoDisponibile
        storico.importoPrenotato    = tipoBudgetDTO.importoPrenotato
        storico.importoAutorizzato  = tipoBudgetDTO.importoAutorizzato
        storico.importoIniziale     = tipoBudgetDTO.importoIniziale
        storico.contoEconomico      = tipoBudgetDTO.contoEconomico
        storico.tipo                = tipoBudgetDTO.tipo
        storico.unitaSo4            = tipoBudgetDTO.unitaSo4.domainObject
        storico.utenteAd4           = tipoBudgetDTO.utenteAd4.domainObject
        storico.tipoBudget          = tipoBudgetDTO.domainObject
        storico.titolo              = tipoBudgetDTO.titolo
        storico.anno                = tipoBudgetDTO.anno
        storico.attivo              = tipoBudgetDTO.attivo
        storico.valido              = tipoBudgetDTO.valido
        storico.motivazioni         = motivazioni

        storico.save(failOnError: true)
        return;
    }

    void elimina(TipoBudgetDTO tipoBudgetDTO) {
        TipoBudget tipoBudget = tipoBudgetDTO.getDomainObject()
        tipoBudget.valido = false;
        tipoBudget.save()
    }

    def listaBudget(TipoBudgetDTO tipoBudgetDTO) {
        return Budget.findAllByTipoBudget(tipoBudgetDTO.domainObject, [sort: 'id', order: 'asc'])
    }

    def listaStorico(TipoBudgetDTO tipoBudgetDTO) {
        return TipoBudgetStorico.findAllByTipoBudget(tipoBudgetDTO.domainObject, [sort: 'dateCreated', order: 'asc'])
    }
}