package it.finmatica.atti.contabilita

import it.finmatica.atti.dto.contabilita.MovimentoAnagraficaDTO

class MovimentoAnagraficaService {

    public void salva(MovimentoAnagraficaDTO movimentoAnagraficaDTO) {
        MovimentoAnagrafica movimentoAnagrafica = movimentoAnagraficaDTO.getDomainObject()
        movimentoAnagrafica.save()
    }

    public void elimina(MovimentoAnagraficaDTO movimentoAnagraficaDTO) {
        MovimentoAnagrafica movimentoAnagrafica = movimentoAnagraficaDTO.getDomainObject()
        movimentoAnagrafica.delete(failOnError: true)
    }

}