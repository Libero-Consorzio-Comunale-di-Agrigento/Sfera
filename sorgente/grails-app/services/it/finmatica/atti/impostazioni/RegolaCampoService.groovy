package it.finmatica.atti.impostazioni

import it.finmatica.atti.dto.impostazioni.RegolaCampoDTO
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAttoreService
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto

class RegolaCampoService {

    WkfAttoreService wkfAttoreService

    RegolaCampoDTO salva(RegolaCampoDTO regolaCampoDTO) {
        RegolaCampo regolaCampo = new RegolaCampo()
        if (regolaCampoDTO.id > 0) {
            regolaCampo = RegolaCampo.get(regolaCampoDTO.id)
        }
        regolaCampo.blocco = regolaCampoDTO.blocco
        regolaCampo.campo = regolaCampoDTO.campo
        regolaCampo.valido = regolaCampoDTO.valido
        regolaCampo.tipoOggetto = regolaCampoDTO.tipoOggetto?.domainObject
        regolaCampo.wkfAttore = regolaCampoDTO.wkfAttore?.domainObject

        // TODO: per ora usiamo solo queste regole di default.
        // poi prima o poi aggiungeremo la possibilità di gestire queste opzioni
        regolaCampo.visibile = true
        regolaCampo.modificabile = true
        regolaCampo.invertiRegola = false

        regolaCampo.save(failOnError: true)
        return regolaCampo.toDTO()
    }

    boolean isBloccoVisibile (IDocumentoIterabile domainObject, String codiceTipoOggetto, String nomeBlocco) {
        WkfTipoOggetto tipoOggetto = WkfTipoOggetto.get(codiceTipoOggetto)
        List<RegolaCampo> listaRegole = RegolaCampo.findAllByValidoAndTipoOggettoAndBlocco(true, tipoOggetto, nomeBlocco)

        if (!listaRegole) {
            return true
        }

        for (RegolaCampo regola : listaRegole) {
            if (wkfAttoreService.valutaAttore(domainObject, regola.wkfAttore, true)) {
                return regola.visibile
            }
        }

        return false
    }

    void elimina(RegolaCampoDTO regolaCampoDTO) {
        RegolaCampo regolaCampo = regolaCampoDTO.getDomainObject()
        regolaCampo.delete()
    }
}