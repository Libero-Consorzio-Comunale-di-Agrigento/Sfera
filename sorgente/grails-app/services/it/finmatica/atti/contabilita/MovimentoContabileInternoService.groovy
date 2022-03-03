package it.finmatica.atti.contabilita

import it.finmatica.atti.dto.contabilita.MovimentoContabileInternoDTO

class MovimentoContabileInternoService {

    public void elimina(MovimentoContabileInternoDTO movimentoContabileInternoDTO) {
        MovimentoContabileInterno movimentoContabileInterno = movimentoContabileInternoDTO.getDomainObject()
        movimentoContabileInterno.delete(failOnError: true)
    }

}