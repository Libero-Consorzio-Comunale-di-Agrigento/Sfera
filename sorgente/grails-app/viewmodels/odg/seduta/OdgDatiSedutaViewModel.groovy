package odg.seduta

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.atti.dto.odg.SedutaDTO
import it.finmatica.atti.dto.odg.SedutaDTOService
import it.finmatica.atti.dto.odg.dizionari.TipoSedutaDTO
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.odg.Commissione
import it.finmatica.atti.odg.dizionari.TipoSeduta
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zul.Window

class OdgDatiSedutaViewModel {

	// services
	SpringSecurityService springSecurityService
	SedutaDTOService sedutaDTOService

	// componenti
	Window self

	// dati
	SedutaDTO seduta
	List<TipoSedutaDTO> listaTipoSeduta
	List<CommissioneDTO> listaCommissione
	boolean mostraVotoPresidente
	boolean modificaDataSeduta = true

    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("seduta") SedutaDTO seduta)  {
        this.self = w
		this.seduta = seduta

		mostraVotoPresidente = Impostazioni.ODG_MOSTRA_VOTO_PRESIDENTE.abilitato;

		listaTipoSeduta  = TipoSeduta.createCriteria().list() {
			eq("valido", true)
			order ("titolo", "asc")
		}?.toDTO()

		listaCommissione = Commissione.createCriteria().list() {
			fetchMode("ruoloCompetenze", FetchMode.JOIN)
			if (seduta.commissione != null){
				or {
					eq("valido",true)
					eq("id", seduta.commissione.id)
				}
			}
			else {
				eq("valido",true)
			}
			if (!(AttiUtils.isUtenteAmministratore())) {
				ruoloCompetenze {
					'in'("ruolo", springSecurityService.principal.uo().ruoli.flatten().codice.unique())
				}
			}
			order ("titolo", "asc")
		}?.toDTO()

		// Nel caso in cui ci sia una sola commissione valida viene inserita direttamente in seduta (#16577)
		if (listaCommissione?.size() == 1){
			CommissioneDTO commissione 	= listaCommissione.get(0)
			this.seduta.commissione 	= commissione
			this.seduta.votoPresidente 	= commissione.votoPresidente
			this.seduta.pubblica		= commissione.sedutaPubblica
		}
		// Nel caso in cui ci sia un solo tipo seduta valido viene inserito direttamente in seduta (#16577)
		if (listaTipoSeduta?.size() == 1){
			this.seduta.tipoSeduta = listaTipoSeduta.get(0)
		}

		modificaDataSeduta = !sedutaDTOService.esistonoOggettiSedutaConfermati(seduta.domainObject)
    }

	@NotifyChange(["seduta"])
	@Command onChangeCommissione(@BindingParam("commissione") CommissioneDTO commissione) {
		seduta.votoPresidente = commissione.votoPresidente
		seduta.pubblica = commissione.sedutaPubblica
		BindUtils.postNotifyChange(null,null,this, "seduta")
	}

	@GlobalCommand
	@NotifyChange(["seduta"])
	void onRefreshCommissione()  {
		// metodo vuoto, serve solo per effettuare il refresh della seduta
	}
}
