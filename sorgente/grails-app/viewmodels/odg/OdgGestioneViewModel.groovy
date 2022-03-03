package odg

import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.atti.dto.odg.SedutaDTO
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.atti.odg.SedutaPartecipante
import it.finmatica.atti.odg.SedutaService
import it.finmatica.atti.odg.dizionari.RuoloPartecipante
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zul.A
import org.zkoss.zul.Checkbox
import org.zkoss.zul.Window

class OdgGestioneViewModel {

	// servizi
	SedutaService sedutaService;

	// componenti
	Window self

	// dati
	List<Date> listaDateSeduta = null
	List<CommissioneDTO> listaCommissione
	CommissioneDTO commissioneSelected
	HashMap<Date, List<SedutaDTO>> listaSedute
	HashMap<SedutaDTO, String> listaNumProposte
	HashMap<SedutaDTO, String> listaPresidente
	SedutaDTO selectedSeduta
	boolean abilitaGettonePresenza = true

	// stato
	Date dataSelezionata
	boolean seduteDaVerbalizzare = false

	@Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self 			= w
		listaPresidente 	= [:]
		listaNumProposte 	= [:]
		dataSelezionata 	= new Date();
		caricaListaCommissione()
		caricaListaSeduta()
		abilitaGettonePresenza = Impostazioni.ODG_GETTONE_PRESENZA_ATTIVO.abilitato
	}

	@NotifyChange([
		"listaSedute",
		"commissioneSelected"
	])
	private void caricaListaSeduta() {
		listaSedute = sedutaService.cercaSedute(dataSelezionata, commissioneSelected?.id?:-1, seduteDaVerbalizzare);
		listaDateSeduta = listaSedute.collect { it.key }
	}

	@NotifyChange(["listaCommissione", "commissioneSelected"])
	private void caricaListaCommissione() {
		listaCommissione = [new CommissioneDTO(id:-1, titolo:"TUTTE")]+sedutaService.getListaCommissioni()?.toDTO()
		commissioneSelected = listaCommissione[0]
	}

	@NotifyChange([
		"listaSedute",
		"commissioneSelected",
		"listaDateSeduta"
	])
	@Command onCreaSeduta() {
		Window w = Executions.createComponents("/odg/seduta/index.zul", self, [id : -1])
		w.onClose {
			caricaListaSeduta()
			BindUtils.postNotifyChange(null, null, this, "listaSedute")
			BindUtils.postNotifyChange(null, null, this, "commissioneSelected")
			BindUtils.postNotifyChange(null, null, this, "listaDateSeduta")
		}
		w.doModal()
	}

	@NotifyChange([
		"listaSedute",
		"commissioneSelected",
		"listaDateSeduta"
	])
	@Command ricercaListaSedute() {
		caricaListaSeduta()
	}

	@NotifyChange([
		"listaSedute",
		"commissioneSelected",
		"listaDateSeduta"
	])
	@Command onLinkSeduta (@ContextParam(ContextType.COMPONENT) A a) {
		Window w = Executions.createComponents("/odg/seduta/index.zul", self, [id : a.attributes.seduta.id])
		w.onClose {
			caricaListaSeduta()
			BindUtils.postNotifyChange(null, null, this, "listaSedute")
			BindUtils.postNotifyChange(null, null, this, "commissioneSelected")
			BindUtils.postNotifyChange(null, null, this, "listaDateSeduta")
		}
		w.doModal()
	}

	@NotifyChange([
		"listaSeduta",
		"listaDateSeduta"
	])
	@Command onChangeDateCalendar (@ContextParam(ContextType.COMPONENT) org.zkoss.zul.Calendar c) {
		dataSelezionata = c.value;
		caricaListaSeduta()
	}

	@NotifyChange("listaNumProposte")
	@Command calcolaNumProposte(@BindingParam("seduta") SedutaDTO seduta){
		int numeroOggettiSeduta = OggettoSeduta.countBySeduta(seduta.domainObject)

		if (numeroOggettiSeduta > 0) {
			listaNumProposte[seduta] = numeroOggettiSeduta+" proposte in Ordine del Giorno"
		}
	}

	@NotifyChange("listaPresidente")
	@Command calcolaPresidente(@BindingParam("seduta") SedutaDTO seduta) {
		// metto "list" anziché "get" perché è possibile che un utente per errore metta due presidenti.
		def partecipanti = SedutaPartecipante.createCriteria().list {
			eq ("seduta.id", seduta.id)
			ruoloPartecipante {
				eq ("codice", RuoloPartecipante.CODICE_PRESIDENTE)
			}
			// Qui c'era la fetch join. L'ho tolta perché da alcuni clienti (ad es sandonato milanese) andava in doppio full-join
		}
        //#39087: in caso di più presidenti prender quello presente (che deve essere solo 1)
        if (partecipanti.size() > 1 ){
            partecipanti = partecipanti.findAll{it.presente == true};
        }
		def sedutaPartecipante = (partecipanti.size() > 0 ? partecipanti[0] : null);

		if (sedutaPartecipante) {
			As4SoggettoCorrente presidente = sedutaPartecipante.componenteEsterno?:sedutaPartecipante.commissioneComponente.componente;

			if (presidente) {
				listaPresidente[seduta] = sedutaPartecipante.ruoloPartecipante.descrizione+" "+(presidente.cognome?:"")+" "+(presidente.nome?:"")
			}
		}
	}

	@NotifyChange([
		"listaSedute",
		"commissioneSelected",
		"listaDateSeduta"
	])
	@Command ricercaListaSeduteDaVerbalizzare(@ContextParam(ContextType.COMPONENT) Checkbox cb) {
		seduteDaVerbalizzare = cb.checked
		caricaListaSeduta()
	}

	@Command onCalcolaGettonePresenza() {
		Window w = Executions.createComponents("/dizionari/odg/popupCalcoloGettone.zul", self, [dataSelezionata:dataSelezionata])
		w.doModal()
	}
}
