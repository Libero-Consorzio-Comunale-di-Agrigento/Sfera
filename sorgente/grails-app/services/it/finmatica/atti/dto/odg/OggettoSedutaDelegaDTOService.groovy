package it.finmatica.atti.dto.odg

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.dizionari.Delega
import it.finmatica.atti.dto.dizionari.DelegaDTO
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.atti.odg.OggettoSedutaDelega

class OggettoSedutaDelegaDTOService {

    SpringSecurityService springSecurityService

    OggettoSedutaDelegaDTO aggiungiDelega (OggettoSedutaDTO oggettoSedutaDto, DelegaDTO delegaDto) {
        OggettoSeduta oggettoSeduta = oggettoSedutaDto.domainObject
        Delega        delega        = delegaDto.domainObject

        OggettoSedutaDelega oggettoSedutaDelega = new OggettoSedutaDelega()
        oggettoSedutaDelega.delega       = delega
        oggettoSedutaDelega.sequenza     = OggettoSedutaDelega.countByOggettoSeduta(oggettoSeduta) + 1
        oggettoSedutaDelega.utenteIns    = springSecurityService.currentUser
        oggettoSedutaDelega.dateCreated  = new Date()

        oggettoSeduta.addToAltreDeleghe(oggettoSedutaDelega)
        oggettoSeduta.save()

        return oggettoSedutaDelega.toDTO()
    }

    void eliminaDelega (long idOggettoSedutaDelega) {
        OggettoSedutaDelega oggettoSedutaDelega = OggettoSedutaDelega.get(idOggettoSedutaDelega)
        oggettoSedutaDelega.delete(failOnError: true)
    }

    List<OggettoSedutaDelegaDTO> getListaAltreDeleghe (OggettoSeduta oggettoSeduta) {
        return OggettoSedutaDelega.createCriteria().list() {
            eq("oggettoSeduta", oggettoSeduta)
            order("id", "asc")
        }.toDTO(["delega.assessore"])
    }
}
