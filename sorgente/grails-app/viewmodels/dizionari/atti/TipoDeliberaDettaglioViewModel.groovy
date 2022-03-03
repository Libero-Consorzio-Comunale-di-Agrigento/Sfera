package dizionari.atti

import afc.AfcAbstractRecord
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.atti.IntegrazioneContabilita
import it.finmatica.atti.cf.integrazione.IAttiIntegrazioneServiceCf
import it.finmatica.atti.commons.StrutturaOrganizzativaService
import it.finmatica.atti.dizionari.OggettoRicorrente
import it.finmatica.atti.dizionari.TipoAllegato
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.tipologie.*
import it.finmatica.atti.dto.dizionari.MappingIntegrazioneDTOService
import it.finmatica.atti.dto.dizionari.OggettoRicorrenteDTO
import it.finmatica.atti.dto.dizionari.TipoAllegatoDTO
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.documenti.tipologie.*
import it.finmatica.atti.dto.impostazioni.MappingIntegrazioneDTO
import it.finmatica.atti.impostazioni.CaratteristicaTipologia
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.documenti.AllegatiObbligatori
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.protocollo.ProtocolloGdm
import it.finmatica.atti.integrazioni.protocollo.ProtocolloGdmConfig
import it.finmatica.atti.odg.Commissione
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.dto.configuratore.iter.WkfCfgIterDTO
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.gestionetesti.ui.dizionari.GestioneTestiModelloDTOService
import it.finmatica.zkutils.SuccessHandler
import org.hibernate.FetchMode
import org.hibernate.criterion.CriteriaSpecification
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Popup
import org.zkoss.zul.Window

class TipoDeliberaDettaglioViewModel extends AfcAbstractRecord {

	// services
	GestioneTestiModelloDTOService  gestioneTestiModelloDTOService
	StrutturaOrganizzativaService   strutturaOrganizzativaService
	ParametroTipologiaService       parametroTipologiaService
	TipoDocumentoCfDTOService       tipoDocumentoCfDTOService
	IAttiIntegrazioneServiceCf		attiCfIntegrazioneService
	IntegrazioneContabilita			integrazioneContabilita
	TipoDeliberaDTOService          tipoDeliberaDTOService
	SpringSecurityService           springSecurityService
	ProtocolloGdmConfig 			protocolloGdmConfig
	SuccessHandler                  successHandler
	MappingIntegrazioneDTOService 	mappingIntegrazioneDTOService

	// componenti

	// dati
	def listaCfgIter
	def listaCfgIterDelibera
	def listaCfgIterPubblicazione
	def listaTipiRegistro
	def listaTipiVisto
	def listaCaratteristiche
	def listaCaratteristicheDelibera
	def listaTipoDeliberaCompetenza
	def listaModelliTestoFrontespizio
	def listaCertificati
	def listaTipoDocumentoCf

	def listaParametriDelibera
	def listaParametri
	def listaTipologieVisto
	def listaCommissioni
	def listaModelliTesto
	def listaFirmatari
	def listaOggettiRicorrenti
	def listaAllegatiObbligatori

	String                  tipoDocumentoEsterno
	List<CodiceDescrizione> listaTipiDocumento

	// stato
	Date data

	boolean categoriaAbilitata
	boolean abilitaPubblicazioneFinoRevoca
	boolean abilitaPubblicazioneTrasparenza
	boolean abilitaOggettiRicorrenti
	boolean abilitaIncaricato
    boolean mostraAllegatiVisualizzatore

	@NotifyChange("selectedRecord")
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") long id) {
		this.self = w

		listaCfgIter 				= [new WkfCfgIterDTO(progressivo:-1, nome:"-- nessuno --", descrizione: "")] + WkfCfgIter.iterValidi.findAllByTipoOggetto(WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), [sort: "nome", order: "asc"]).toDTO()
		listaCfgIterDelibera 		= [new WkfCfgIterDTO(progressivo:-1, nome:"-- nessuno --", descrizione: "")] + WkfCfgIter.iterValidi.findAllByTipoOggetto(WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), [sort: "nome", order: "asc"]).toDTO()
		
		listaCfgIterPubblicazione 	= [new WkfCfgIterDTO(progressivo:-1, nome:"-- nessuno --", descrizione: "")] + WkfCfgIter.iterValidi.findAllByTipoOggetto(WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), [sort: "nome", order: "asc"]).toDTO()
		listaTipiRegistro 			= [new TipoRegistroDTO(codice:"", descrizione:"-- usa registro specificato in commissione --")] + TipoRegistro.findAllByValido(true, [sort:"descrizione", order:"asc"]).toDTO()
		listaTipologieVisto  		= [new TipoVistoParereDTO(codice:"", titolo:"-- nessuno --")] + TipoVistoParere.list([sort: 'titolo', order: 'asc']).toDTO()
		listaCertificati 			= [new TipoCertificatoDTO(id:-1, titolo:"-- nessuno --", descrizione: "Seleziona un Certificato")] + TipoCertificato.findAllByValido(true, [sort: "titolo", order: "asc"]).toDTO()
		listaCaratteristiche 		= CaratteristicaTipologia.findAllByTipoOggettoAndValido(WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), true, [sort: "titolo", order: "asc"]).toDTO(['caratteristicheTipiSoggetto'])
		listaCaratteristicheDelibera= CaratteristicaTipologia.findAllByTipoOggettoAndValido(WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), true, [sort: "titolo", order: "asc"]).toDTO()
		listaCommissioni			= Commissione.findAllByValido(true, [sort: 'titolo', order: 'asc']).toDTO()
		listaModelliTestoFrontespizio 	= [new GestioneTestiModelloDTO(id:-1, nome:"-- nessuno --")] + gestioneTestiModelloDTOService.getListaModelli("FRONTESPIZIO_DELIBERA");

		listaFirmatari		 		= strutturaOrganizzativaService.getComponentiConRuoloInOttica(Impostazioni.RUOLO_SO4_FIRMATARIO_CERT_PUBB.valore, springSecurityService.principal.ottica().codice).toDTO(["soggetto.utenteAd4"]).soggetto.utenteAd4.unique()
		caricaTipoDelibera (id)

		if (id > 0) {
            listaTipiDocumento = getListaTipiDocumentoProtocollo()
            tipoDocumentoEsterno = protocolloGdmConfig?.getTipoDocumento(id)?:""
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica (selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		}

		categoriaAbilitata              = Impostazioni.CATEGORIA_PROPOSTA_DELIBERA.abilitato
		abilitaPubblicazioneFinoRevoca  = Impostazioni.PUBBLICAZIONE_FINO_REVOCA.abilitato
		abilitaPubblicazioneTrasparenza = Impostazioni.INTEGRAZIONE_CASA_DI_VETRO.abilitato;
		abilitaOggettiRicorrenti = Impostazioni.OGGETTI_RICORRENTI_TIPOLOGIE.abilitato
        mostraAllegatiVisualizzatore = Impostazioni.VIS_GESTIONE_PUBBLCAZIONE_ALLEGATI.abilitato
		abilitaIncaricato = selectedRecord.caratteristicaTipologia?.caratteristicheTipiSoggetto?.tipoSoggetto*.codice?.contains(TipoSoggetto.INCARICATO)

		refreshListaTipiVisto ()
		refreshListaModelloTesto ()
		caricaListaOggettiRicorrenti()
		caricaAllegatiObbligatori()
		BindUtils.postNotifyChange(null, null, this, "categoriaAbilitata")
	}

	// FIXME: Questo fa schifissimo ma serve per quei clienti che NON hanno il protocollo gdm: infatti fare l'inject del bean direttamente significherebbe
	// caricare il bean protocolloEsternoGdm e le relative classi del Protocollo con conseguente errore ClassNotFoundException.
	// Va fatto un refactor per gestire meglio i parametri delle integrazioni dalle tipologie.
	List<CodiceDescrizione> getListaTipiDocumentoProtocollo () {
		return protocolloGdmConfig?.getTipiDocumento()?:[]
	}

	private void caricaTipoDelibera (long id) {
		if (id > 0) {
			selectedRecord = TipoDelibera.createCriteria().get {
				eq ("id", id)
				fetchMode("parametri", FetchMode.JOIN)
			}.toDTO(["parametri"])

			refreshOggettiRicorrenti()
			caricaListaParametri()
			caricaListaParametriDelibera()
			caricaListaTipoDeliberaCompetenza()
            caricaListaTipoDocumentoCf()
		} else {
			selectedRecord = new TipoDeliberaDTO(id: -1, valido:true)
		}
	}

	@NotifyChange("listaParametri")
	@Command caricaListaParametri() {
		listaParametri 			= parametroTipologiaService.getListaParametri ("tipoDelibera", selectedRecord.id, selectedRecord.progressivoCfgIter?:-1)
	}

	@NotifyChange("listaParametriDelibera")
	@Command caricaListaParametriDelibera() {
		listaParametriDelibera 	= parametroTipologiaService.getListaParametri ("tipoDelibera", selectedRecord.id, selectedRecord.progressivoCfgIterDelibera?:-1)
	}

	@NotifyChange("listaParametri")
	@Command svuotaParametro(@BindingParam("parametro") def p) {
		p.valore = null
	}

	/*
	 * Gestione Tipologie Visto
	 */

	@Command onAggiungiTipologiaVisto (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("tipologiaVisto") TipoVistoParereDTO tipologiaVisto, @BindingParam("popup") Popup popup) {
		tipoDeliberaDTOService.aggiungiTipologiaVisto (selectedRecord, tipologiaVisto)
		refreshListaTipiVisto()
		popup.close()
	}

	@Command onEliminaTipologiaVisto (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("tipologiaVisto") TipoVistoParereDTO tipologiaVisto) {
		Messagebox.show("Eliminare il visto automatico selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						tipoDeliberaDTOService.eliminaTipologiaVisto (TipoDeliberaDettaglioViewModel.this.selectedRecord, tipologiaVisto)
						TipoDeliberaDettaglioViewModel.this.refreshListaTipiVisto()
					}
				}
			}
		)
	}

	private void refreshListaTipiVisto () {
		listaTipiVisto = selectedRecord?.domainObject?.tipiVisto?.toDTO();
		BindUtils.postNotifyChange(null, null, this, "listaTipiVisto")
	}

	/*
	 * Gestione Modello Testo
	 */
	@Command onAggiungiModelloTesto (@BindingParam("tipoOggetto")String tipoOggetto) {
		Window w = Executions.createComponents("/commons/popupSceltaModelloTesto.zul", self, [tipoOggetto: tipoOggetto])
		w.onClose { event ->
			if (event?.data != null) {
				tipoDeliberaDTOService.aggiungiModelloTesto (selectedRecord, event.data)
				refreshListaModelloTesto()
			}
		}
		w.doModal()
	}

	@Command onEliminaModelloTesto (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("modelloTesto") GestioneTestiModelloDTO gestioneTestiModello) {
		String messaggio = "Eliminare il modello testo selezionato?";
		
		if (gestioneTestiModello.id == this.selectedRecord.modelloTesto?.id ||
			gestioneTestiModello.id == this.selectedRecord.modelloTestoDelibera?.id) {
			messaggio = "Il modello testo selezionato è testo predefinito: eliminare comunque?"	
		}
		
		Messagebox.show(messaggio, "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						tipoDeliberaDTOService.eliminaModelloTesto (TipoDeliberaDettaglioViewModel.this.selectedRecord, gestioneTestiModello)
						onSalva()
						TipoDeliberaDettaglioViewModel.this.refreshListaModelloTesto()
					}
				}
			}
		)
	}


	@Command onAggiungiOggettoRicorrente () {
		Window w = Executions.createComponents ("/atti/documenti/popupSceltaOggettoRicorrente.zul", self, [listaOggettiRicorrenti: listaOggettiRicorrenti, cancella: false])
		w.onClose { event ->
			if(event?.data != null){
				tipoDeliberaDTOService.aggiungiOggettoRicorrente(selectedRecord, event.data)
				refreshOggettiRicorrenti()
				BindUtils.postNotifyChange(null, null, this, "selectedRecord")
			}
		}
		w.doModal()
	}

	@Command onEliminaOggettoRicorrente (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("oggettoRicorrente") OggettoRicorrenteDTO oggettoRicorrenteDto) {
		Messagebox.show("Eliminare l'oggetto ricorrente selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					public void onEvent(Event e) {
						if (Messagebox.ON_OK.equals(e.getName())) {
							tipoDeliberaDTOService.eliminaOggettoRicorrente(selectedRecord, oggettoRicorrenteDto)
							TipoDeliberaDettaglioViewModel.this.refreshOggettiRicorrenti()
							BindUtils.postNotifyChange(null, null, TipoDeliberaDettaglioViewModel.this, "selectedRecord")
						}
					}
				}
		)
	}

	public def getListaModelliTestoProposta() {
		return listaModelliTesto.findAll { it.tipoModello.codice.startsWith(PropostaDelibera.TIPO_OGGETTO) }
	}
	
	public def getListaModelliTestoDelibera() {
		return listaModelliTesto.findAll { it.tipoModello.codice.startsWith(Delibera.TIPO_OGGETTO) }
	}

	private void refreshListaModelloTesto () {
		listaModelliTesto = selectedRecord?.domainObject?.modelliTesto?.toDTO();
		BindUtils.postNotifyChange(null, null, this, "listaModelliTestoProposta")
		BindUtils.postNotifyChange(null, null, this, "listaModelliTestoDelibera")
	}

	private void caricaListaTipoDeliberaCompetenza() {
		List<TipoDeliberaCompetenza> lista = TipoDeliberaCompetenza.createCriteria().list() {
			eq("tipoDelibera.id", selectedRecord.id)
//			fetchMode("utenteAd4", FetchMode.JOIN)
//			fetchMode("ruoloAd4", FetchMode.JOIN)
//			fetchMode("unitaSo4", FetchMode.JOIN)
			createAlias ("utenteAd4", "ute", CriteriaSpecification.LEFT_JOIN)
			createAlias ("ruoloAd4", "ruolo", CriteriaSpecification.LEFT_JOIN)
			createAlias ("unitaSo4", "unita", CriteriaSpecification.LEFT_JOIN)

			order("titolo", "asc")
			order("ute.nominativo", "asc")
			order("ruolo.descrizione", "asc")
			order("unita.descrizione", "asc")

		}
		listaTipoDeliberaCompetenza = lista.toDTO()
		BindUtils.postNotifyChange(null, null, this, "listaTipoDeliberaCompetenza")
	}

	@Command onEliminaTipoDeliberaCompetenza (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("tipoDeliberaCompetenza") TipoDeliberaCompetenzaDTO tipoDetCompetenza) {
		Messagebox.show("Eliminare la competenza selezionata?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						tipoDeliberaDTOService.elimina(tipoDetCompetenza)
						TipoDeliberaDettaglioViewModel.this.caricaListaTipoDeliberaCompetenza()
					}
				}
			}
		)
	}

	@Command onAggiungiTipoDeliberaCompetenza () {
		Window w = Executions.createComponents ("/commons/popupCompetenzaDettaglio.zul", self, [documento: selectedRecord, tipoDocumento: "tipoDelibera"])
		w.onClose { caricaListaTipoDeliberaCompetenza() }
		w.doModal()
	}

	/*
	 * Gestione Tipo Documento CF		// COSI NON VA BENE: VA RIPENSATO CON IL BEAN integrazioneContabilita !
	 */
	public boolean isTabContabilitaVisibile () {
		return integrazioneContabilita.isTipiDocumentoAbilitati();
	}

	private void caricaListaTipoDocumentoCf () {
        if (!integrazioneContabilita.isTipiDocumentoAbilitati()) {
            return
        }

        List<TipoDocumentoCf> lista = TipoDocumentoCf.createCriteria().list() {
			eq("tipoDelibera.id", selectedRecord.id)
		}
		listaTipoDocumentoCf = lista.toDTO()
		List<it.finmatica.atti.cf.integrazione.TipoDocumentoCf> tipiDocumentoCf = attiCfIntegrazioneService.getTipiDocumento(springSecurityService.principal.amministrazione.codice)
		for (TipoDocumentoCfDTO tipoDocumentoCfDTO : listaTipoDocumentoCf) {
			tipoDocumentoCfDTO.tipoDocumentoCf = tipiDocumentoCf.find { it.codice == tipoDocumentoCfDTO.cfTipoDocumentoCodice }
		}

		BindUtils.postNotifyChange(null, null, this, "listaTipoDocumentoCf")
	}

	@Command onEliminaTipoDocumentoCf (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("tipoDocumentoCf") TipoDocumentoCfDTO tipoDocumentoCf) {
		Messagebox.show("Eliminare il tipo documento selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						tipoDocumentoCfDTOService.elimina(tipoDocumentoCf)
						TipoDeliberaDettaglioViewModel.this.caricaListaTipoDocumentoCf()
					}
				}
			}
		)
	}

	@Command onAggiungiTipoDocumentoCf () {
		Window w = Executions.createComponents ("/commons/popupTipoDocumentoCfDettaglio.zul", self, [documento: selectedRecord, tipoDocumento: "tipoDelibera", lista:listaTipoDocumentoCf])
		w.onClose { caricaListaTipoDocumentoCf() }
		w.doModal()
	}

	/*
	 * Implementazione dei metodi per AfcAbstractRecord
	 */
	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		boolean isNuovoTipoDelibera = !(selectedRecord.id > 0)
		
		// se la tipologia richiede la pubblicazione, verifico che ci siano i dati minimi
		if (selectedRecord.pubblicazione && (selectedRecord.progressivoCfgIterPubblicazione == null)) {
			Messagebox.show("Specificare l'iter di pubblicazione")
			Clients.showNotification("Per salvare è necessario specificare l'iter di pubblicazione della Delibera.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 5000, true)
			return;
		}

		// se la tipologia richiede la "trasformazione in delibera", verifico che ci siano i dati minimi della delibera:
		if (!isNuovoTipoDelibera && selectedRecord.adottabile) {
			if (selectedRecord.caratteristicaTipologiaDelibera == null) {
				Clients.showNotification("È necessario specificare la caratteristica della Delibera.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 5000, true)
				return;
			}
			
			if ((!selectedRecord.copiaTestoProposta) && selectedRecord.modelloTestoDelibera == null) {
				Clients.showNotification("È necessario specificare un modello testo predefinito per la delibera.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 5000, true)
				return;
			}
			
			if (selectedRecord.progressivoCfgIterDelibera == null) {
				Clients.showNotification("È necessario specificare un Iter per la Delibera.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 5000, true)
				return;
			}
		}
		
		selectedRecord = tipoDeliberaDTOService.salva(selectedRecord, (listaParametri?:[]) + (listaParametriDelibera?:[]), tipoDocumentoEsterno)

		refreshOggettiRicorrenti()

		if (isNuovoTipoDelibera) {
			caricaListaTipoDeliberaCompetenza();
		}
		
		aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
		aggiornaDatiModifica (selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
        caricaListaTipoDocumentoCf()
		
		successHandler.showMessage("Tipo delibera salvata")
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalvaChiudi() {
		onSalva()
		onChiudi()
	}

	@Command onSettaValido(@BindingParam("valido") boolean valido) {
		Messagebox.show(Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTesto",[valido?"valido":"non valido"].toArray()), Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						TipoDeliberaDettaglioViewModel.this.selectedRecord.valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, TipoDeliberaDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, TipoDeliberaDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, TipoDeliberaDettaglioViewModel.this, "datiModifica")
					}
				}
			}
		)
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onDuplica () {
		selectedRecord = tipoDeliberaDTOService.duplica(selectedRecord);
		Clients.showNotification("Tipologia duplicata.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true)
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica", "abilitaIncaricato"])
	@Command onCambiaCaratteristica () {
		abilitaIncaricato = selectedRecord.caratteristicaTipologia?.caratteristicheTipiSoggetto?.tipoSoggetto*.codice?.contains(TipoSoggetto.INCARICATO)
	}

	private void caricaListaOggettiRicorrenti () {
		def listaId = selectedRecord.oggettiRicorrenti.collect {it.id}

		listaOggettiRicorrenti = OggettoRicorrente.createCriteria().list {
			eq ("valido", true)
			eq ("delibera", true)
			if (listaId.size() > 0) {
				not { 'in'("id", listaId) }
			}
			order("oggetto", "asc")
		}.toDTO()
	}

	private refreshOggettiRicorrenti () {
		selectedRecord.oggettiRicorrenti = TipoDelibera.createCriteria().get {
			eq ("id", selectedRecord.id)
			fetchMode("oggettiRicorrenti", FetchMode.JOIN)

		}.toDTO().oggettiRicorrenti?.sort{it.oggetto}
		caricaListaOggettiRicorrenti()
	}


	private void caricaAllegatiObbligatori (){
		def lista = MappingIntegrazione.findAllByCategoriaAndCodiceAndValoreInterno(AllegatiObbligatori.MAPPING_CATEGORIA, AllegatiObbligatori.MAPPING_CODICE_TIPOLOGIA, String.valueOf(selectedRecord.id)).toDTO()
		listaAllegatiObbligatori = TipoAllegato.findAllByIdInList(lista*.valoreEsterno).toDTO()
	}

	@Command onAggiungiAllegatoObbligatorio() {
		Window w = Executions.createComponents("/dizionari/atti/popupSceltaTipoAllegato.zul", self, [tipologia: 'DELIBERA', id: selectedRecord.id, tipoAllegato: null])
		w.onClose { event ->
			if (event.data != null) {
				TipoAllegatoDTO tipoAllegatoDTO = event.data;
				MappingIntegrazioneDTO integrazione = new MappingIntegrazioneDTO()
				integrazione.categoria = AllegatiObbligatori.MAPPING_CATEGORIA
				integrazione.codice = AllegatiObbligatori.MAPPING_CODICE_TIPOLOGIA
				integrazione.valoreInterno = selectedRecord.id.toString()
				integrazione.valoreEsterno = tipoAllegatoDTO.id.toString()
				mappingIntegrazioneDTOService.salva(integrazione)
				caricaAllegatiObbligatori()
				BindUtils.postNotifyChange(null, null, this, "listaAllegatiObbligatori")
				BindUtils.postNotifyChange(null, null, this, "selectedRecord")
			}
		}
		w.doModal()
	}

	@Command onEliminaAllegatoObbligatorio(@BindingParam("allegatoObbligatorio") TipoAllegatoDTO allegatoObbligatorio) {
		org.zkoss.zhtml.Messagebox.show("Vuoi cancellare il tipo allegato obbligatorio?", "Attenzione",
				org.zkoss.zhtml.Messagebox.OK | org.zkoss.zhtml.Messagebox.CANCEL, org.zkoss.zhtml.Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					public void onEvent(Event e) {
						if (org.zkoss.zhtml.Messagebox.ON_OK.equals(e.getName())) {
							MappingIntegrazioneDTO integrazione = MappingIntegrazione.findByCategoriaAndCodiceAndValoreInternoAndValoreEsterno(AllegatiObbligatori.MAPPING_CATEGORIA, AllegatiObbligatori.MAPPING_CODICE_TIPOLOGIA, selectedRecord.id.toString(), allegatoObbligatorio.id.toString()).toDTO()
							mappingIntegrazioneDTOService.elimina(integrazione)
							caricaAllegatiObbligatori()
							BindUtils.postNotifyChange(null, null, TipoDeliberaDettaglioViewModel.this, "listaAllegatiObbligatori")
							BindUtils.postNotifyChange(null, null, TipoDeliberaDettaglioViewModel.this, "selectedRecord")
						}
					}
				});
	}

}
