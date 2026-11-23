package it.finmatica.atti.dto.documenti

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.dizionari.Delega
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.PropostaDeliberaDelega
import it.finmatica.atti.dto.dizionari.DelegaDTO

class PropostaDeliberaDelegaDTOService {

    SpringSecurityService springSecurityService

    PropostaDeliberaDelegaDTO aggiungiDelega (PropostaDeliberaDTO propostaDeliberaDto, DelegaDTO delegaDto) {
        PropostaDelibera propostaDelibera = propostaDeliberaDto.domainObject
        Delega           delega           = delegaDto.domainObject

        PropostaDeliberaDelega propostaDeliberaDelega = new PropostaDeliberaDelega()
        propostaDeliberaDelega.delega       = delega
        propostaDeliberaDelega.sequenza     = PropostaDeliberaDelega.countByPropostaDelibera(propostaDelibera) + 1
        propostaDeliberaDelega.utenteIns    = springSecurityService.currentUser
        propostaDeliberaDelega.dateCreated  = new Date()

        propostaDelibera.addToAltreDeleghe(propostaDeliberaDelega)
        propostaDelibera.save()

        return propostaDeliberaDelega.toDTO()
    }

    void eliminaDelega (long idPropostaDeliberaDelega) {
        PropostaDeliberaDelega propostaDeliberaDelega = PropostaDeliberaDelega.get(idPropostaDeliberaDelega)
        propostaDeliberaDelega.delete(failOnError: true)
    }

    List<PropostaDeliberaDelegaDTO> getListaAltreDeleghe (PropostaDelibera propostaDelibera) {
        return PropostaDeliberaDelega.createCriteria().list() {
            eq("propostaDelibera", propostaDelibera)
            order("id", "asc")
        }.toDTO(["delega.assessore"])
    }
}
