package it.finmatica.atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.commons.StrutturaOrganizzativaService
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class VistoParereSoggettoService {

	SpringSecurityService springSecurityService
	StrutturaOrganizzativaService strutturaOrganizzativaService

	public So4UnitaPubb getUnitaInTipologia (def visto, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		def uo = getListaUnitaInTipologia(visto, soggetti, codiceRuolo, tipoSoggettoPartenza)

		if (uo?.size() > 0) {
			return uo[0]
		} else {
			return null
		}
	}

	public List<So4UnitaPubb> getListaUnitaInTipologia (def visto, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		if (visto?.tipologia?.getListaUnitaDestinatarie()?.length > 0) {
			return So4UnitaPubb.allaData(new Date()).perOttica(springSecurityService.principal.ottica().codice) {
				'in'("progr", visto?.tipologia?.getListaUnitaDestinatarie())
			}
		} else {
			return strutturaOrganizzativaService.getUnitaUtente(springSecurityService.principal.id, springSecurityService.principal.ottica().codice)
		}
	}

	public So4UnitaPubb getUnitaInTipologiaInRamoUoProponente (def visto, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		def uo = getListaUnitaInTipologiaInRamoUoProponente(visto, soggetti, codiceRuolo, tipoSoggettoPartenza)

		if (uo?.size() > 0) {
			return uo[0]
		} else {
			return null
		}
	}

	public List<So4UnitaPubb> getListaUnitaInTipologiaInRamoUoProponente (def visto, def soggetti, String codiceRuolo, TipoSoggetto tipoSoggettoPartenza) {
		if (visto?.tipologia?.getListaUnitaDestinatarie()?.length > 0) {
			So4UnitaPubb uoProponenteDetermina = visto.proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4
			List<So4UnitaPubb> listaUoPadri = []
			if (uoProponenteDetermina != null) {
				listaUoPadri = strutturaOrganizzativaService.getUnitaPadri(uoProponenteDetermina.progr, uoProponenteDetermina.ottica.codice);
			}

			List<So4UnitaPubb> uoTipologia = So4UnitaPubb.allaData(new Date()).perOttica(springSecurityService.principal.ottica().codice).findAllByProgrInList(visto.tipologia.getListaUnitaDestinatarie())

			return uoTipologia.findAll { uoTipo ->
				listaUoPadri.find { uoPadre ->
					uoTipo.progr == uoPadre.progr
				} != null
			}
		} else {
			return strutturaOrganizzativaService.getUnitaUtente(springSecurityService.principal.id, springSecurityService.principal.ottica().codice)
		}
	}
}
