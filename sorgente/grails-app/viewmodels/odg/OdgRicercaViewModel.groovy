package odg

import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.dizionari.Delega
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.dto.dizionari.DelegaDTO
import it.finmatica.atti.dto.documenti.DeliberaDTO
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.atti.dto.odg.OggettoSedutaDTO
import it.finmatica.atti.dto.odg.OggettoSedutaDTOService
import it.finmatica.atti.dto.odg.SedutaDTO
import it.finmatica.atti.dto.odg.dizionari.EsitoDTO
import it.finmatica.atti.dto.odg.dizionari.EsitoDTOService
import it.finmatica.atti.odg.Commissione
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zul.Label
import org.zkoss.zul.Window

class OdgRicercaViewModel {

	// service
	OggettoSedutaDTOService oggettoSedutaDTOService
	EsitoDTOService esitoDTOService

	// componenti
	Window self

	// dati
	CommissioneDTO 				commissioneSelected
	EsitoDTO 					esitoSelected
	List<CommissioneDTO>		listaCommissione
	List<EsitoDTO> 				listaEsito
	List<OggettoSedutaDTO>		listaOggettoSeduta
	OggettoSedutaDTO			selectedOggettoSeduta
	List<DelegaDTO>				listaDelega
	DelegaDTO					delegaSelected
	As4SoggettoCorrenteDTO 		selectedUtenteProponente
	So4UnitaPubbDTO	 			selectedUnitaProponente

	Date 	dataDal
	Date 	dataAl
	Integer numeroProposta
	String	oggettoProposta

	// stato
	boolean 					filtriVisibili 	= false
	String 						lbFiltri

	@NotifyChange("listaCommissione")
	@Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w
		lbFiltri = "Mostra filtri"
		listaEsito 			= esitoDTOService.getListaEsiti ();
		listaDelega 		= Delega.findAllByValido(true, [sort:'sequenza', order:'asc']).toDTO()
		listaCommissione 	= Commissione.findAllByValido(true, [sort:'titolo', order:'asc']).toDTO()
	}

	@NotifyChange("listaOggettoSeduta")
	@Command onCerca() {
		listaOggettoSeduta = oggettoSedutaDTOService.ricerca(commissioneSelected, esitoSelected, dataDal, dataAl, numeroProposta, oggettoProposta, delegaSelected, selectedUtenteProponente, selectedUnitaProponente)
	}

	@Command onLinkSeduta (@BindingParam("seduta") SedutaDTO seduta) {
		Window w = Executions.createComponents("/odg/seduta/index.zul", self, [id : seduta.id])
		w.onClose {

		}
		w.doModal()
	}

	@Command onLinkOggettoSeduta (@BindingParam("oggetto") OggettoSedutaDTO oggetto) {
		Window w = Executions.createComponents("/odg/oggettoSeduta.zul", self, [id : oggetto.id, wp : 'ricercaSeduta'])
		w.onClose {
			onCerca()
			BindUtils.postNotifyChange(null,null, this, "listaOggettoSeduta")
		}
		w.doModal()
	}

	@NotifyChange(["filtriVisibili", "filtriAttivi", "lbFiltri"])
	@Command openCloseFiltri() {
		filtriVisibili 	= !filtriVisibili
		if (controllaFiltriAttivi()) lbFiltri="Mostra filtri (attivi)" else lbFiltri="Mostra filtri"
	}

	@Command checkDelibera(@ContextParam(ContextType.COMPONENT) Label lc, @BindingParam("oggetto")  OggettoSedutaDTO oggetto) {
		DeliberaDTO rs
		if (oggetto.propostaDelibera) {
			rs = Delibera.createCriteria().get {
				fetchMode("propostaDelibera", 	FetchMode.JOIN)
				fetchMode("oggettoSeduta", 		FetchMode.JOIN)
				eq("propostaDelibera.id", oggetto.propostaDelibera.id)
				eq("oggettoSeduta.id", oggetto.id)
			}?.toDTO()
		}
		lc.value = ((rs) ? ("Delibera " +rs.numeroDelibera +"/" +rs.annoDelibera +" - " +rs.oggetto) : null)
	}

	@Command onLinkDelibera (@BindingParam("oggetto") OggettoSedutaDTO oggetto) {
		DeliberaDTO rs = Delibera.createCriteria().get {
			fetchMode("propostaDelibera", 	FetchMode.JOIN)
			fetchMode("oggettoSeduta", 		FetchMode.JOIN)
			
			eq("propostaDelibera.id", 	oggetto.propostaDelibera.id)
			eq("oggettoSeduta.id", 		oggetto.id)
		}?.toDTO()

		if (rs) {
			Window w = Executions.createComponents("/atti/documenti/delibera.zul", self, [id : rs.id])
			w.doModal()
		}
	}

	private boolean controllaFiltriAttivi() {

		if (commissioneSelected!=null)
			return true

		if (dataAl!=null)
			return true

		if (dataDal!=null)
			return true

		if (selectedUnitaProponente!=null)
			return true

		if (selectedUtenteProponente!=null)
			return true

		if (esitoSelected!=null)
			return true

		if (delegaSelected!=null)
			return true

		if (numeroProposta!=null)
			return true

		return false
	}
}
