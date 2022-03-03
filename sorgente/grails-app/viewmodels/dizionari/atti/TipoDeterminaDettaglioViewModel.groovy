package dizionari.atti

import afc.AfcAbstractRecord
import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.IntegrazioneContabilita
import it.finmatica.atti.cf.integrazione.IAttiIntegrazioneServiceCf
import it.finmatica.atti.commons.StrutturaOrganizzativaService
import it.finmatica.atti.dizionari.OggettoRicorrente
import it.finmatica.atti.dizionari.TipoAllegato
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.Certificato
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.tipologie.*
import it.finmatica.atti.dto.dizionari.MappingIntegrazioneDTOService
import it.finmatica.atti.dto.dizionari.OggettoRicorrenteDTO
import it.finmatica.atti.dto.dizionari.TipoAllegatoDTO
import it.finmatica.atti.dto.documenti.tipologie.*
import it.finmatica.atti.dto.impostazioni.MappingIntegrazioneDTO
import it.finmatica.atti.impostazioni.CaratteristicaTipologia
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.documenti.AllegatiObbligatori
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.protocollo.ProtocolloGdmConfig
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

class TipoDeterminaDettaglioViewModel extends AfcAbstractRecord {

	// services
	GestioneTestiModelloDTOService 	gestioneTestiModelloDTOService
	StrutturaOrganizzativaService  	strutturaOrganizzativaService
	ParametroTipologiaService      	parametroTipologiaService
	TipoDocumentoCfDTOService      	tipoDocumentoCfDTOService
	IAttiIntegrazioneServiceCf		attiCfIntegrazioneService
	IntegrazioneContabilita			integrazioneContabilita
	TipoDeterminaDTOService        	tipoDeterminaDTOService
	SpringSecurityService          	springSecurityService
	ProtocolloGdmConfig				protocolloGdmConfig
	SuccessHandler                 	successHandler
	MappingIntegrazioneDTOService   mappingIntegrazioneDTOService

	// dati
	def listaCfgIter
	def listaCfgIterCert
	def listaTipiRegistro
	def listaTipiVisto
	def listaCaratteristiche
	def listaTipoDeterminaCompetenza
	def listaCertificati
	def listaTipoDocumentoCf

	def listaParametri
	def listaTipologieVisto
	def listaModelliTesto
	def listaModelliTestoFrontespizio
	def listaFirmatari
	def listaOggettiRicorrenti
	boolean abilitaOggettiRicorrenti
	def listaAllegatiObbligatori
	boolean abilitaIncaricato
    boolean mostraEseguibilitaImmediata
    boolean mostraAllegatiVisualizzatore
    String tipoDocumentoEsterno
    List<CodiceDescrizione> listaTipiDocumento

	// stato
	Date data

	@NotifyChange("selectedRecord")
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") long id) {
		this.self = w

		listaCfgIter = new ArrayList<WkfCfgIter>()
		listaCfgIter = WkfCfgIter.iterValidi.findAllByTipoOggetto(WkfTipoOggetto.get(Determina.TIPO_OGGETTO), [sort: "nome", order: "asc"]).toDTO()
		listaCfgIter.add(0, new WkfCfgIterDTO(nome: "", descrizione: "", id: -1))
		listaTipiRegistro 	 = TipoRegistro.findAllByValido(true, [sort:"descrizione", order:"asc"]).toDTO()
		listaTipologieVisto  = [new TipoVistoParereDTO(codice:"", titolo:"-- nessuno --")] + TipoVistoParere.list([sort: 'titolo', order: 'asc']).toDTO()
		listaCaratteristiche = CaratteristicaTipologia.findAllByTipoOggettoAndValido(WkfTipoOggetto.get(Determina.TIPO_OGGETTO), true, [sort: "titolo", order: "asc"]).toDTO(['caratteristicheTipiSoggetto'])
		listaFirmatari		 = strutturaOrganizzativaService.getComponentiConRuoloInOttica(Impostazioni.RUOLO_SO4_FIRMATARIO_CERT_PUBB.valore, springSecurityService.principal.ottica().codice).toDTO(["soggetto.utenteAd4"]).soggetto.utenteAd4.unique()

		listaModelliTestoFrontespizio 	= [new GestioneTestiModelloDTO(id:-1, nome:"-- nessuno --")] + gestioneTestiModelloDTOService.getListaModelli("FRONTESPIZIO_DETERMINA");
		listaCfgIterCert				= WkfCfgIter.iterValidi.findAllByTipoOggetto(WkfTipoOggetto.get(Certificato.TIPO_OGGETTO), [sort: "nome", order: "asc"]).toDTO()
		caricaTipoDetermina (id)

		if (id > 0) {
            listaTipiDocumento = getListaTipiDocumentoProtocollo()
            tipoDocumentoEsterno = protocolloGdmConfig?.getTipoDocumento(id)?:""
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica (selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		}

		abilitaOggettiRicorrenti = Impostazioni.OGGETTI_RICORRENTI_TIPOLOGIE.abilitato
        mostraEseguibilitaImmediata = Impostazioni.ESEGUIBILITA_IMMEDIATA_DETE_ATTIVA.abilitato
		abilitaIncaricato = selectedRecord.caratteristicaTipologia?.caratteristicheTipiSoggetto?.tipoSoggetto*.codice?.contains(TipoSoggetto.INCARICATO)
        mostraAllegatiVisualizzatore = Impostazioni.VIS_GESTIONE_PUBBLCAZIONE_ALLEGATI.abilitato

		refreshListaTipiVisto ()
		refreshListaModelloTesto()
		caricaListaCertificati ()
		caricaListaOggettiRicorrenti()
		caricaAllegatiObbligatori()
	}

    // FIXME: Questo fa schifissimo ma serve per quei clienti che NON hanno il protocollo gdm: infatti fare l'inject del bean direttamente significherebbe
    // caricare il bean protocolloEsternoGdm e le relative classi del Protocollo con conseguente errore ClassNotFoundException.
    // Va fatto un refactor per gestire meglio i parametri delle integrazioni dalle tipologie.
    List<CodiceDescrizione> getListaTipiDocumentoProtocollo () {
		return protocolloGdmConfig?.getTipiDocumento()?:[]
    }

	private void caricaTipoDetermina (long id) {
		if (id > 0) {
			selectedRecord = TipoDetermina.createCriteria().get {
				eq ("id", id)
				fetchMode("parametri", FetchMode.JOIN)
			}.toDTO(["parametri"])

			refreshOggettiRicorrenti()
			caricaListaParametri()
			caricaListaTipoDeterminaCompetenza()
			caricaListaTipoDocumentoCf()

		} else {
			selectedRecord = new TipoDeterminaDTO(id: -1, valido:true)
		}
	}

	@NotifyChange("listaParametri")
	@Command caricaListaParametri() {
		listaParametri = parametroTipologiaService.getListaParametri ("tipoDetermina", selectedRecord.id, selectedRecord.progressivoCfgIter?:-1)
	}

	@NotifyChange("listaParametri")
	@Command svuotaParametro(@BindingParam("parametro") def p) {
		p.valore = null
	}

	/*
	 * Gestione Tipologie Visto
	 */

	@Command onAggiungiTipologiaVisto (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("tipologiaVisto") TipoVistoParereDTO tipologiaVisto, @BindingParam("popup") Popup popup) {
		tipoDeterminaDTOService.aggiungiTipologiaVisto (selectedRecord, tipologiaVisto)
		refreshListaTipiVisto()
		popup.close()
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica", "abilitaIncaricato"])
	@Command onCambiaCaratteristica () {
		abilitaIncaricato = selectedRecord.caratteristicaTipologia?.caratteristicheTipiSoggetto?.tipoSoggetto*.codice?.contains(TipoSoggetto.INCARICATO)
	}

	@Command onEliminaTipologiaVisto (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("tipologiaVisto") TipoVistoParereDTO tipologiaVisto) {
		Messagebox.show("Eliminare il visto automatico selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						tipoDeterminaDTOService.eliminaTipologiaVisto (TipoDeterminaDettaglioViewModel.this.selectedRecord, tipologiaVisto)
						TipoDeterminaDettaglioViewModel.this.refreshListaTipiVisto()
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

	@Command onAggiungiModelloTesto () {
		Window w = Executions.createComponents("/commons/popupSceltaModelloTesto.zul", self, [tipoOggetto: Determina.TIPO_OGGETTO])
		w.onClose { event ->
			if (event?.data != null) {
				tipoDeterminaDTOService.aggiungiModelloTesto (selectedRecord, event.data)
				refreshListaModelloTesto()
			}
		}
		w.doModal()
	}

	@Command onEliminaModelloTesto (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("modelloTesto") GestioneTestiModelloDTO gestioneTestiModello) {
		if (gestioneTestiModello.id == this.selectedRecord.modelloTesto.id){
			Messagebox.show("Il modello testo selezionato è testo predefinito: eliminare comunque?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					void onEvent(Event e) {
						if (Messagebox.ON_OK.equals(e.getName())) {
							TipoDeterminaDettaglioViewModel.this.selectedRecord.modelloTesto = null
							tipoDeterminaDTOService.eliminaModelloTesto (TipoDeterminaDettaglioViewModel.this.selectedRecord, gestioneTestiModello)
							onSalva()
							TipoDeterminaDettaglioViewModel.this.refreshListaModelloTesto()
						}
					}
				}
			)
		} else {
			Messagebox.show("Eliminare il modello testo selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					void onEvent(Event e) {
						if (Messagebox.ON_OK.equals(e.getName())) {
							tipoDeterminaDTOService.eliminaModelloTesto (TipoDeterminaDettaglioViewModel.this.selectedRecord, gestioneTestiModello)
							TipoDeterminaDettaglioViewModel.this.refreshListaModelloTesto()
						}
					}
				}
			)
		}
	}

	private void refreshListaModelloTesto () {
		listaModelliTesto = selectedRecord?.domainObject?.modelliTesto?.toDTO();
		BindUtils.postNotifyChange(null, null, this, "listaModelliTesto")
	}

	/*
	 * Gestione Competenze
	 */
	private void caricaListaTipoDeterminaCompetenza() {
		List<TipoDeterminaCompetenza> lista = TipoDeterminaCompetenza.createCriteria().list() {
			eq("tipoDetermina.id", selectedRecord.id)
			//fetchMode("utenteAd4", FetchMode.JOIN)
			//fetchMode("ruoloAd4",  FetchMode.JOIN)
			//fetchMode("unitaSo4",  FetchMode.JOIN)

			createAlias ("utenteAd4", "ute", CriteriaSpecification.LEFT_JOIN)
			createAlias ("ruoloAd4", "ruolo", CriteriaSpecification.LEFT_JOIN)
			createAlias ("unitaSo4", "unita", CriteriaSpecification.LEFT_JOIN)

			order("titolo", "asc")
			order("ute.nominativo", "asc")
			order("ruolo.descrizione", "asc")
			order("unita.descrizione", "asc")
		}
		listaTipoDeterminaCompetenza = lista.toDTO()
		BindUtils.postNotifyChange(null, null, this, "listaTipoDeterminaCompetenza")
	}

	@Command onEliminaTipoDeterminaCompetenza (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("tipoDeterminaCompetenza") TipoDeterminaCompetenzaDTO tipoDetCompetenza) {
		Messagebox.show("Eliminare la competenza selezionata?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						tipoDeterminaDTOService.elimina(tipoDetCompetenza)
						TipoDeterminaDettaglioViewModel.this.caricaListaTipoDeterminaCompetenza()
					}
				}
			}
		)
	}

	@Command onAggiungiTipoDeterminaCompetenza () {
		Window w = Executions.createComponents ("/commons/popupCompetenzaDettaglio.zul", self, [documento: selectedRecord, tipoDocumento: "tipoDetermina"])
		w.onClose { caricaListaTipoDeterminaCompetenza() }
		w.doModal()
	}


	@Command onAggiungiOggettoRicorrente () {
		Window w = Executions.createComponents ("/atti/documenti/popupSceltaOggettoRicorrente.zul", self, [listaOggettiRicorrenti: listaOggettiRicorrenti, cancella: false])
		w.onClose { event ->
			if(event?.data != null){
				tipoDeterminaDTOService.aggiungiOggettoRicorrente(selectedRecord, event.data)
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
							tipoDeterminaDTOService.eliminaOggettoRicorrente(selectedRecord, oggettoRicorrenteDto)
							TipoDeterminaDettaglioViewModel.this.refreshOggettiRicorrenti()
							BindUtils.postNotifyChange(null, null, TipoDeterminaDettaglioViewModel.this, "selectedRecord")
						}
					}
				}
		)
	}

	/*
	 * Gestione Tipo Documento CF		// COSI NON VA BENE: VA RIPENSATO CON IL BEAN integrazioneContabilita !
	 */
	boolean isTabContabilitaVisibile () {
		return integrazioneContabilita.isTipiDocumentoAbilitati();
	}

	private void caricaListaTipoDocumentoCf () {
        if (!integrazioneContabilita.isTipiDocumentoAbilitati()) {
            return
        }

		List<TipoDocumentoCf> lista = TipoDocumentoCf.createCriteria().list() {
			eq("tipoDetermina.id", selectedRecord.id)
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
				void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						tipoDocumentoCfDTOService.elimina(tipoDocumentoCf)
						TipoDeterminaDettaglioViewModel.this.caricaListaTipoDocumentoCf()
					}
				}
			}
		)
	}

	@Command onAggiungiTipoDocumentoCf () {
		Window w = Executions.createComponents ("/commons/popupTipoDocumentoCfDettaglio.zul", self, [documento: selectedRecord, tipoDocumento: "tipoDetermina", lista:listaTipoDocumentoCf])
		w.onClose { caricaListaTipoDocumentoCf() }
		w.doModal()
	}

	/*
	 * Implementazione dei metodi per AfcAbstractRecord
	 */

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		boolean isNuovoTipoDetermina = !(selectedRecord.id > 0)

		if (selectedRecord.pubblicazione && selectedRecord.progressivoCfgIterPubblicazione == null) {
			Messagebox.show("Specificare l'iter di pubblicazione")
            return
        }

        if (!selectedRecord.pubblicazione)
            selectedRecord.progressivoCfgIterPubblicazione = null

        if (selectedRecord.registroUnita && selectedRecord.tipoRegistro != null)
            selectedRecord.tipoRegistro = null

        if (!selectedRecord.codiceGara)
            selectedRecord.codiceGaraObbligatorio = false

        selectedRecord = tipoDeterminaDTOService.salva(selectedRecord, listaParametri, tipoDocumentoEsterno)

        refreshOggettiRicorrenti()

        if (isNuovoTipoDetermina) {
            aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
            caricaListaTipoDeterminaCompetenza()
            caricaListaTipoDocumentoCf()
        }
        aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
        successHandler.showMessage("Tipo determina salvata")
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
					if (Messagebox.ON_OK.equals(e.getName())) {
						TipoDeterminaDettaglioViewModel.this.selectedRecord.valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, TipoDeterminaDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, TipoDeterminaDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, TipoDeterminaDettaglioViewModel.this, "datiModifica")
					}
				}
			}
		)
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onDuplica () {
		selectedRecord = tipoDeterminaDTOService.duplica(selectedRecord);
		Clients.showNotification("Tipologia duplicata.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true)
	}

	private void caricaListaCertificati () {
		listaCertificati = new ArrayList<TipoCertificatoDTO>()
		TipoCertificatoDTO fake = new TipoCertificatoDTO()
		fake.titolo = ""
		fake.descrizione = "Seleziona un certificato"
		fake.id = -1
		listaCertificati.add(fake)
		def lista = TipoCertificato.createCriteria().list(){ eq("valido", true) }.toDTO()
		listaCertificati.addAll(lista)
		BindUtils.postNotifyChange(null, null, this, "listaCertificati")
	}

	/*
     * Gestione Oggetti Ricorrenti
     */
	private void caricaListaOggettiRicorrenti () {
		def listaId = selectedRecord.oggettiRicorrenti.collect {it.id}

		listaOggettiRicorrenti = OggettoRicorrente.createCriteria().list {
			eq ("valido", true)
			eq ("determina", true)
			if (listaId.size() > 0) {
				not { 'in'("id", listaId) }
			}
			order("oggetto", "asc")
		}.toDTO()
	}

	private void caricaAllegatiObbligatori (){
		def lista = MappingIntegrazione.findAllByCategoriaAndCodiceAndValoreInterno(AllegatiObbligatori.MAPPING_CATEGORIA, AllegatiObbligatori.MAPPING_CODICE_TIPOLOGIA, String.valueOf(selectedRecord.id)).toDTO()
		listaAllegatiObbligatori = TipoAllegato.findAllByIdInList(lista*.valoreEsterno).toDTO()
	}

	@Command onAggiungiAllegatoObbligatorio() {
		Window w = Executions.createComponents("/dizionari/atti/popupSceltaTipoAllegato.zul", self, [tipologia: 'DETERMINA', id: selectedRecord.id, tipoAllegato: null])
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
							BindUtils.postNotifyChange(null, null, TipoDeterminaDettaglioViewModel.this, "listaAllegatiObbligatori")
							BindUtils.postNotifyChange(null, null, TipoDeterminaDettaglioViewModel.this, "selectedRecord")
						}
					}
				});
	}


	private refreshOggettiRicorrenti () {
        selectedRecord.oggettiRicorrenti = TipoDetermina.createCriteria().get {
            eq ("id", selectedRecord.id)
            fetchMode("oggettiRicorrenti", FetchMode.JOIN)

        }.toDTO().oggettiRicorrenti?.sort{it.oggetto}
        caricaListaOggettiRicorrenti()
    }
}
