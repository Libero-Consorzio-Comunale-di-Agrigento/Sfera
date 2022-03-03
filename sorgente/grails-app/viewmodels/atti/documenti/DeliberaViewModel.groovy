package atti.documenti

import it.finmatica.atti.AbstractViewModel
import it.finmatica.atti.IntegrazioneAlbo
import it.finmatica.atti.IntegrazioneContabilita
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.commons.DocumentoGenerico
import it.finmatica.atti.commons.FileAllegatoGenerico
import it.finmatica.atti.commons.FileAllegatoStorico
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.dizionari.DatiAggiuntiviService
import it.finmatica.atti.documenti.TipoDatoAggiuntivo
import it.finmatica.atti.impostazioni.RegolaCampoService
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.beans.AttiFileDownloader
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.dto.documenti.*
import it.finmatica.atti.dto.documenti.tipologie.TipoDeliberaDTO
import it.finmatica.atti.dto.integrazioni.JConsLogConservazioneDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.CampiDocumento
import it.finmatica.atti.impostazioni.CaratteristicaTipologiaService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.CasaDiVetroService
import it.finmatica.atti.integrazioni.ConservazioneService
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.atti.odg.dizionari.EsitoStandard
import it.finmatica.dto.DTO
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestionetesti.GestioneTestiService
import org.apache.commons.lang.StringUtils
import org.hibernate.FetchMode
import org.hibernate.criterion.CriteriaSpecification
import org.zkoss.bind.BindContext
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

import java.text.SimpleDateFormat

class DeliberaViewModel extends AbstractViewModel<Delibera> {

	// services
	AttiGestoreCompetenze 			gestoreCompetenze
	AttiFileDownloader 				attiFileDownloader
	CaratteristicaTipologiaService 	caratteristicaTipologiaService
	IntegrazioneContabilita			integrazioneContabilita
	AllegatoDTOService				allegatoDTOService
	CasaDiVetroService				casaDiVetroService
	VistoParereDTOService			vistoParereDTOService

	DeliberaDTOService           	deliberaDTOService
	PropostaDeliberaDTOService 		propostaDeliberaDTOService
	DocumentoDTOService				documentoDTOService
	DocumentoCollegatoDTOService 	documentoCollegatoDTOService
	NotificheService                notificheService
    ConservazioneService            conservazioneService
	IntegrazioneAlbo				integrazioneAlbo
	RegolaCampoService 				regolaCampoService
	TokenIntegrazioneService		tokenIntegrazioneService
	DatiAggiuntiviService			datiAggiuntiviService

	// dati
	DeliberaDTO delibera
	List<VistoParereDTO> listaVisti
	List listaCertificati
	def listaAllegati
	def listaDocumentiCollegati
	def storico
	String urlCasaDiVetro
	TipoDeliberaDTO tipologiaDelibera

	// mappa dei soggetti
	Map<String, it.finmatica.atti.zk.SoggettoDocumento> soggetti = [:]

	// stato
	String titolo
	def competenze
	String posizioneFlusso
	def campiProtetti = [:]
	def noteTrasmissionePrecedenti
	boolean attorePrecedente
	String unitaProponenteDesc
	boolean mostraNoteTrasmissionePrecedenti
	boolean abilitaNoteCommissione
	String nomeModelloTesto
	boolean testoLockato = false
	boolean lockPermanente = false
	boolean firmaRemotaAbilitata
	boolean mostraArchiviazioni
	boolean isNotificaPresente
	boolean isEstrattoPresente

	boolean protocollo
	boolean abilitaPubblicazioneFinoRevoca
	boolean abilitaRiservato
	boolean riservatoModificabile
	boolean mostraCorteConti
	boolean mostraEseguibilitaImmediata
	boolean mostraParereRevisoriConti

	// gestione contabilità
	boolean conDocumentiContabili = false
	boolean contabilitaAbilitata  = false

	// indica se nella lista dei visti si mostrano solo i visti validi o anche i non validi
	boolean mostraSoloVistiValidi = true
	String zulContabilita
	boolean mostraNote 	  = true
	boolean mostraStorico = true

	// indica se abilitare la visualizzazione della relata di pubblicazione dell'albo
	boolean mostraRelata

	// indica se è bloccato da un altro utente
	boolean isLocked = false

	// indica se il documento deve essere comunque aperto in lettura (delegato)
	boolean forzaCompetenzeLettura

	@NotifyChange(["delibera", "competenze"])
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long idDelibera, @ExecutionArgParam("idDocumentoEsterno") Long idDocumentoEsterno, @ExecutionArgParam("competenzeLettura") Boolean competenzeLettura) {
		this.self = w

		firmaRemotaAbilitata 			= Impostazioni.FIRMA_REMOTA.abilitato
		protocollo						= Impostazioni.PROTOCOLLO_SEZIONE.abilitato
		abilitaPubblicazioneFinoRevoca	= Impostazioni.PUBBLICAZIONE_FINO_REVOCA.abilitato
		abilitaRiservato 				= Impostazioni.RISERVATO.abilitato
		mostraArchiviazioni             = Impostazioni.PROTOCOLLO_MOSTRA_SEZIONE_ARCHIVIAZIONI.abilitato
		mostraCorteConti				= Impostazioni.GESTIONE_CORTE_CONTI.abilitato
		mostraEseguibilitaImmediata		= Impostazioni.ESEGUIBILITA_IMMEDIATA_ATTIVA.abilitato
		mostraParereRevisoriConti		= Impostazioni.PARERE_REVISORI_CONTI.abilitato
		forzaCompetenzeLettura			= competenzeLettura

		if (idDelibera != null) {
			delibera = new DeliberaDTO([id: idDelibera])
		} else {
			idDelibera = Delibera.findByIdDocumentoEsterno(idDocumentoEsterno).id
		}

		Delibera d = Delibera.get(idDelibera);
		aggiornaMaschera(d)

		//se la delibera ha avuto almeno un esito (in odg) di tipo INVIA_COMMISSIONE allora devono essere visibili le note della commissione
		abilitaNoteCommissione = false
		List listaEsiti = OggettoSeduta.createCriteria().list() {
			projections {
				rowCount()
			  }
			eq("propostaDelibera.id", d.proposta.id)
			eq("confermaEsito", true)
			esito {
				eq("esitoStandard.codice", EsitoStandard.INVIA_COMMISSIONE)
			}
		}

		if (listaEsiti[0] > 0)
			abilitaNoteCommissione = true
		// per la delibera, aggiorno i pulsanti solo se ho un iter associato.
		if (d.iter != null) {
			aggiornaPulsanti()
		}
	}

	/*
	 * 	Metodi per il calcolo dei Soggetti della determina
	 */

	@Command onSceltaSoggetto (@BindingParam("tipoSoggetto") String tipoSoggetto, @BindingParam("categoriaSoggetto") String categoriaSoggetto) {
		
		// carico la tipologia in questo modo siccome nessun cliente usa la scelta dei soggetti nella delibera tranne l'AREU
		// facendo così evito di caricare per tutti una roba che non interessa a nessuno.
		if (tipologiaDelibera == null) {
			tipologiaDelibera = delibera.domainObject.tipologiaDocumento.toDTO()
		}
		
		Window w = Executions.createComponents ("/atti/documenti/popupSceltaSoggetto.zul", self, [idCaratteristicaTipologia: tipologiaDelibera.caratteristicaTipologiaDelibera.id
			, documento: 			delibera
			, soggetti: 			soggetti
			, tipoSoggetto: 		tipoSoggetto
			, categoriaSoggetto:	categoriaSoggetto])
		w.onClose { event ->
			// se ho annullato la modifica, non faccio niente:
			if (event.data == null)
				return;

			// altrimenti aggiorno i soggetti.
			BindUtils.postNotifyChange(null, null, this, "soggetti");
            self.invalidate()
        }
		w.doModal()
	}

	/*
	 * Gestione dati di conservazione
	 */
	JConsLogConservazioneDTO getLogConservazione () {
        return conservazioneService.getLastLog (delibera.idDocumentoEsterno, delibera.statoConservazione)
	}

	/*
	 * Gestione Applet Testo
	 */

	@NotifyChange(["testoLockato"])
	@Command editaTesto() {
		testoLockato = gestioneTesti.editaTesto (delibera);
	}

	@Command onEliminaTesto () {
		gestioneTesti.eliminaTesto(delibera, this)
	}

	@NotifyChange(["lockPermanente"])
	@Command onToggleLockPermanente () {
		lockPermanente = !lockPermanente;
	}

	@Command onDownloadTesto () {
		Delibera d = delibera.domainObject
		attiFileDownloader.downloadFileAllegato(d, d.testo)
	}

	@Command onDownloadTestoStorico (@BindingParam("tipoOggetto") String tipoOggetto, @BindingParam("id") Long id, @BindingParam("idFileAllegato") Long idFileAllegato) {
		FileAllegatoStorico f = FileAllegatoStorico.get(idFileAllegato)
		attiFileDownloader.downloadFileAllegato(DocumentoFactory.getDocumentoStorico(id, tipoOggetto), f, true)
	}

	@Command onDownloadStampaUnica () {
		Delibera d 	= delibera.domainObject
		attiFileDownloader.downloadFileAllegato(d, d.stampaUnica)
	}

	/*
	 * Gestione degli allegati
	 */

	@Command onModificaAllegato (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("nuovo") boolean nuovo, @BindingParam("selected") def selected) {
		// succede quando un utente fa "doppio click" sulla tabella vuota.
		if (!nuovo && selected == null) {
			return;
		}
		
		Window w = Executions.createComponents("/atti/documenti/allegato.zul", self, [id: (nuovo?-1:selected.id), documento: delibera, competenzeLettura: forzaCompetenzeLettura])
		w.onClose {
			if (!(delibera.idDocumentoEsterno > 0)) {
				// potrei aver aggiornato la determina, quindi ne riprendo i numeri di versione e idDocumentoEsterno.
				Delibera d = delibera.domainObject;
				delibera.version = d.version;
				delibera.idDocumentoEsterno = d.idDocumentoEsterno;
			}
			refreshListaAllegati ()
		}
		w.doModal()
	}

	@Command onEliminaAllegato (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("allegato") AllegatoDTO allegato) {
		Messagebox.show("Eliminare l'allegato selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						allegatoDTOService.elimina (allegato, delibera)
						delibera.version = delibera.domainObject.version;
						DeliberaViewModel.this.refreshListaAllegati()
					}
				}
			}
		)
	}

	private void refreshListaAllegati () {
		listaAllegati = Allegato.createCriteria().list {
			eq ("delibera.id", delibera.id)
			order ("sequenza", "asc")
			order ("titolo",   "asc")
		}.toDTO()
		BindUtils.postNotifyChange(null, null, this, "listaAllegati")
	}

	/*
	 *  Gestione Pareri
	 */

	private void refreshListaVisti () {
		def visti = VistoParere.createCriteria().list {
			or {
				eq ("propostaDelibera.id", 	delibera.proposta.id)
				eq ("delibera.id", 			delibera.id)
			}

			if (mostraSoloVistiValidi) {
				eq ("valido", true)
			} else {
				or {
					eq ("valido", true)
					ne ("esito", EsitoVisto.DA_VALUTARE)
				}
			}

			fetchMode ("tipologia",  FetchMode.JOIN)
			fetchMode ("unitaSo4",   FetchMode.JOIN)
			fetchMode ("firmatario", FetchMode.JOIN)
			fetchMode ("iter", FetchMode.JOIN)
		}

		// se la delibera ha dei pareri, mostro solo quelli e non quelli della proposta.
		def pareriDelibera = visti.findAll { it.delibera != null };
		def pareriProposta = visti.findAll { it.propostaDelibera != null };

		for (def parere : pareriProposta){
			if (pareriDelibera.findAll{ it.tipologia.codice == parere.tipologia.codice}.size() > 0){
				visti.remove(parere)
			}
		}

		// alla fine, per ogni visto, controllo di avere le competenze in modifica:
		listaVisti = []
		for (VistoParere visto : visti.sort{ it.iter?.dataInizio }) {
			VistoParereDTO dto = visto.toDTO();
			dto.competenzeInModifica = gestoreCompetenze.getCompetenze(visto);
			listaVisti << dto
		}
		
		BindUtils.postNotifyChange(null, null, this, "listaVisti")
	}

	@Command onModificaVistoParere (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("nuovo") boolean nuovo, @BindingParam("selected") def selected) {
		if (!nuovo && selected == null)
			return;

		Window w = Executions.createComponents("/atti/documenti/parere.zul", self, [id: nuovo?-1:selected.id, documento: delibera.proposta, tipodoc : "propostaDelibera", competenzeLettura: forzaCompetenzeLettura])
		w.onClose {
			aggiornaMaschera(delibera.domainObject)
			aggiornaPulsanti()
		}
		w.doModal()
	}

	@NotifyChange("mostraSoloVistiValidi")
	@Command onMostraVistiValidi () {
		mostraSoloVistiValidi = !mostraSoloVistiValidi;
		refreshListaVisti()
	}
	
	@Command onEliminaVistoParere (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("visto") VistoParereDTO visto) {
		Messagebox.show("Eliminare il parere selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						vistoParereDTOService.elimina (visto)
						delibera.version = delibera.domainObject.version;
						DeliberaViewModel.this.refreshListaVisti()
							
						// alcune condizioni di visibilità sui pulsanti dipendono dalla presenza o meno dei visti
						DeliberaViewModel.this.aggiornaPulsanti()
					}
				}
			}
		)
	}

	/*
	 * Gestione della proposta:
	 */
	@Command onApriAtto () {
		Window w = Executions.createComponents ("/atti/documenti/propostaDelibera.zul", self, [id: delibera.proposta.id])
		w.doModal()
	}

	@Command onApriTestoAtto () {
		def documento = delibera.domainObject.proposta
		attiFileDownloader.downloadFileAllegato(documento, documento.testo)
	}

	/*
	 *  Gestione documenti collegati
	 */

	private void refreshListaDocumentiCollegati () {
		listaDocumentiCollegati = documentoCollegatoDTOService.getListaDocumentiCollegati(delibera.domainObject)
		BindUtils.postNotifyChange(null, null, this, "listaDocumentiCollegati")
	}

	/*
	 * Gestisce le note di trasmissioni
	 */

	private void aggiornaNoteTrasmissionePrecedenti () {
		def result = documentoDTOService.getNoteTrasmissionePrecedenti (delibera)
		noteTrasmissionePrecedenti 		 = result.noteTrasmissionePrecedenti
		attorePrecedente	 			 = result.attorePrecedente
		mostraNoteTrasmissionePrecedenti = result.mostraNoteTrasmissionePrecedenti

		BindUtils.postNotifyChange(null, null, this, "mostraNoteTrasmissionePrecedenti")
		BindUtils.postNotifyChange(null, null, this, "noteTrasmissionePrecedenti")
		BindUtils.postNotifyChange(null, null, this, "attorePrecedente")
	}

	/*
	 * Gestione dello storico:
	 */
	private void caricaStorico () {
		storico = propostaDeliberaDTOService.caricaStorico(delibera.propostaDelibera) + deliberaDTOService.caricaStorico(delibera);
		BindUtils.postNotifyChange(null, null, this, "storico")
	}

	/*
	 * Gestione della contabilità
	 */
	@Command
    void onAggiornaContabilita () {
        aggiornaContabilita(delibera.domainObject)
    }

	void aggiornaContabilita (Delibera d) {
		if (d != null) {
			integrazioneContabilita.aggiornaMaschera (d, false)
		}
	}

	/**
	 * Quando l'utente seleziona il tab dei riferimenti, controllo che il documento sia in casa di vetro:
	 */
	@NotifyChange("urlCasaDiVetro")
	@Command onApriTabRiferimenti () {
		// aggiorno l'url del documento in casa di vetro:
		urlCasaDiVetro = casaDiVetroService.getUrlDocumentoSePresente(delibera.proposta);
	}

	/*
	 * Gestione certificati
	 */
	@Command onApriCertificato (@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		Window w = Executions.createComponents("/atti/documenti/certificato.zul", self, [id:ctx.component.selectedItem.value.id])
		w.doModal()
	}

	@NotifyChange('listaCertificati')
	@Command refreshListaCertificati () {
		listaCertificati = Certificato.createCriteria().list {
			createAlias ("firmatari", "f", CriteriaSpecification.LEFT_JOIN)
			createAlias ("f.firmatario", "uf", CriteriaSpecification.LEFT_JOIN)

			projections {
				property ("id")
				tipologia {
					property "titolo"
				}
				property ("f.dataFirma")
				property ("uf.nominativoSoggetto")
			}

			eq ("delibera.id", delibera.id)

			order ("dateCreated", "asc")
		}.collect { row -> [id:row[0], tipo:row[1], dataFirma:row[2], firmatario:row[3]] }
	}

	/*
	 *  Gestione Chiusura Maschera
	 */

	@Command onChiudi () {
		// se devo rilasciare il lock sul testo, lo rilascio.
		gestioneTesti.uploadEUnlockTesto(delibera, lockPermanente);
		tokenIntegrazioneService.unlockDocumento(delibera.domainObject)
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	/*
	 *  Presa Visione
	 */

	@Command onPresaVisione () {
		notificheService.eliminaNotifica(delibera.domainObject, springSecurityService.currentUser)
		isNotificaPresente = false
		BindUtils.postNotifyChange(null, null, this, "isNotificaPresente")
		onChiudi()
	}

	// gestione del pulsante "statico" salva:
	@Command onSalva () {
		Collection<String> messaggiValidazione = validaMaschera();
		if (messaggiValidazione != null && messaggiValidazione.size() > 0) {
			Clients.showNotification(StringUtils.join(messaggiValidazione, "\n"), Clients.NOTIFICATION_TYPE_ERROR, self, "middle_center", -1, true);
			return;
		}
		if (delibera.oggetto.size() > Impostazioni.LUNGHEZZA_OGGETTO.valoreInt) {
			Messagebox.show("L'oggetto inserito è superiore a " + Impostazioni.LUNGHEZZA_OGGETTO + " caratteri")
			return;
		}
		deliberaDTOService.salva(this)
	}

	/*
	 * Implementazione dei Metodi per AbstractViewModel
	 */
	DTO<Delibera> getDocumentoDTO () {
		return delibera
	}

	@Override
	WkfCfgIter getCfgIter() {
		return WkfCfgIter.getIterIstanziabile(delibera?.oggettoSeduta?.seduta?.commissione?.progressivoCfgIter?:(long)-1).get()
	}

	Delibera getDocumentoIterabile (boolean controllaConcorrenza) {
		if (delibera.id > 0) {
			Delibera domainObject = delibera.getDomainObject()
			if (controllaConcorrenza && delibera?.version >= 0 && domainObject.version != delibera?.version) {
				throw new AttiRuntimeException("Attenzione: un altro utente ha modificato il documento su cui si sta lavorando. Impossibile continuare. \n (dto.version=${delibera.version}!=domain.version=${domainObject.version})")
			}

			return domainObject
		}

		return new Delibera()
	}

	Collection<String> validaMaschera () {
		def messaggi = [];

		if (delibera.oggetto == null || delibera.oggetto.trim().length() == 0) {
			messaggi << "L'Oggetto è obbligatorio."
		}
		else {
			delibera.oggetto = AttiUtils.replaceCaratteriSpeciali(delibera.oggetto)
		}

		if (delibera.oggetto != null && !AttiUtils.controllaCharset(delibera.oggetto)) {
			messaggi << "L'Oggetto contiene dei caratteri non supportati."
		}

		if (delibera.oggetto != null && delibera.oggetto.size() > Impostazioni.LUNGHEZZA_OGGETTO.valoreInt) {
			messaggi << "La lunghezza dell'oggetto inserito è superiore a " + Impostazioni.LUNGHEZZA_OGGETTO.valore + " caratteri"
		}

		if (Impostazioni.ESEGUIBIILITA_IMMEDIATA_MOTIVAZIONI.abilitato && delibera.eseguibilitaImmediata && (delibera.motivazioniEseguibilita == null || delibera.motivazioniEseguibilita?.isEmpty())) {
			messaggi << "Motivazione obbligatoria per documenti con Eseguibilità Immediata";
		}

		return messaggi;
	}

	void aggiornaDocumentoIterabile (Delibera d) {
		if (delibera.oggetto.size() > Impostazioni.LUNGHEZZA_OGGETTO.valoreInt) {
			throw new AttiRuntimeException("La lunghezza dell'oggetto inserito è superiore a " + Impostazioni.LUNGHEZZA_OGGETTO.valore + " caratteri")
			return;
		}

		// salvo e sblocco il testo
		gestioneTesti.uploadEUnlockTesto (d);

		d.oggetto 	            = delibera.oggetto
		d.riservato             = delibera.riservato
		d.eseguibilitaImmediata = delibera.eseguibilitaImmediata
		d.motivazioniEseguibilita = delibera.motivazioniEseguibilita
		d.pubblicaRevoca 		= delibera.pubblicaRevoca
		d.giorniPubblicazione 	= delibera.giorniPubblicazione
		d.note					= delibera.note
		d.noteTrasmissione		= delibera.noteTrasmissione

		d.daInviareCorteConti = delibera.daInviareCorteConti
		if (d.daInviareCorteConti) {
			d.dataInvioCorteConti = delibera.dataInvioCorteConti
		} else {
			d.dataInvioCorteConti = null
		}

		d.dataEsecutivitaManuale = delibera.dataEsecutivitaManuale

        caratteristicaTipologiaService.salvaSoggettiModificati(d, soggetti)
		documentoDTOService.salvaDatiAggiuntivi(d, delibera)
	}

	void aggiornaMaschera (Delibera d) {
		// per prima cosa controllo che l'utente abbia le competenze in lettura sul documento
		competenze = gestoreCompetenze.getCompetenze(d, true)
		competenze.lettura = competenze.lettura ?: forzaCompetenzeLettura
		if (!competenze.lettura) {
			delibera = null
			throw new AttiRuntimeException("L'utente ${springSecurityService.principal.username} non ha i diritti di lettura sull'atto con id ${d.id}")
		}

		if (d.statoFirma == StatoFirma.IN_FIRMA || d.statoFirma == StatoFirma.FIRMATO_DA_SBLOCCARE) {
			competenze.modifica 	 = false
			competenze.cancellazione = false
		}

		isLocked = tokenIntegrazioneService.isLocked(d)
		
		// calcolo i campi che devo proteggere in lettura
		campiProtetti = CampiDocumento.getMappaCampi(d.campiProtetti)

		// verifico che l'utente possa gestire il riservato:
		riservatoModificabile = (!d.riservato || gestoreCompetenze.utenteCorrenteVedeRiservato(d));
		unitaProponenteDesc   = d.proposta.getSoggetto (TipoSoggetto.UO_PROPONENTE).unitaSo4.descrizione;

		// aggiorno i dati del lock sul testo:
		testoLockato = (gestioneTesti.isTestoLockato(d))

		delibera = d.toDTO(["registroDelibera", "propostaDelibera.tipologia.caratteristicaTipologiaDelibera", "propostaDelibera.commissione", "oggettoSeduta.seduta", "iter.stepCorrente.cfgStep", "oggettoSeduta.delega.assessore", "testo", "oggettoSeduta.esito", "datiAggiuntivi"]);

		nomeModelloTesto 	  = d.modelloTesto?.nome

		// calcolo il titolo
		titolo = (d.numeroDelibera > 0 ? "Delibera n. ${d.numeroDelibera}/${d.annoDelibera}" : "Proposta di Delibera"+(d.proposta.numeroProposta > 0 ? " n. ${d.proposta.numeroProposta}/${d.proposta.annoProposta}" : ""))

		// calcolo la posizione del flusso
		posizioneFlusso = d.iter?.stepCorrente?.cfgStep?.nome

		// calcolo i vari soggetti della delibera
		soggetti = caratteristicaTipologiaService.calcolaSoggettiDto(d);

		// verifico presenza notifiche
		isNotificaPresente = notificheService.isNotificaPresente(delibera.domainObject, springSecurityService.currentUser)

		// carico lo storico:
		caricaStorico()

		// aggiorno i documenti collegati
		refreshListaDocumentiCollegati()

		// aggiorno i visti/pareri:
		refreshListaVisti();

		// aggiorno gli allegati:
		refreshListaAllegati();

		// aggiorno le note di trasmissioni dello step precedente
		aggiornaNoteTrasmissionePrecedenti ();

		// aggiorna i certificati
		refreshListaCertificati();

		// gestione contabilità
		contabilitaAbilitata = integrazioneContabilita.isAbilitata(d)
		if (contabilitaAbilitata) {
			conDocumentiContabili = integrazioneContabilita.isConDocumentiContabili(d)
			zulContabilita = integrazioneContabilita.getZul(d)
			aggiornaContabilita(d)

			BindUtils.postNotifyChange(null, null, this, "conDocumentiContabili")
			BindUtils.postNotifyChange(null, null, this, "zulContabilita")
		}
		zulContabilita = integrazioneContabilita.getZul(d)

		mostraRelata = Impostazioni.RELATA_ALBO.abilitato && integrazioneAlbo.hasRelata(d)

		mostraNote 	  = regolaCampoService.isBloccoVisibile(d, d.tipoOggetto, "NOTE")
		mostraStorico = regolaCampoService.isBloccoVisibile(d, d.tipoOggetto, "STORICO")

		isEstrattoPresente = datiAggiuntiviService.isDatoPresente(d, TipoDatoAggiuntivo.ESTRATTO);

		BindUtils.postNotifyChange(null, null, this, "listaAllegati")
		BindUtils.postNotifyChange(null, null, this, "campiProtetti")
		BindUtils.postNotifyChange(null, null, this, "delibera")
		BindUtils.postNotifyChange(null, null, this, "competenze")
		BindUtils.postNotifyChange(null, null, this, "posizioneFlusso")
		BindUtils.postNotifyChange(null, null, this, "titolo")
		BindUtils.postNotifyChange(null, null, this, "testoLockato")
		BindUtils.postNotifyChange(null, null, this, "soggetti")
		BindUtils.postNotifyChange(null, null, this, "contabilitaAbilitata")
	}
	
	@Command
	void apriDocumentoCollegato (@BindingParam("documentoCollegato") DocumentoCollegatoDTO documentoCollegato) {
		documentoCollegatoDTOService.apriDocumento(documentoCollegato)
	}

	@Command
	public void onDownloadRelata(){
		def map = integrazioneAlbo.getRelata(delibera.domainObject);
		DocumentoGenerico doc = new DocumentoGenerico();
		doc.TIPO_OGGETTO 		= "RELATA"
		doc.id 					= Long.parseLong(map.relata.id_documento)
		doc.idDocumentoEsterno	= Long.parseLong(map.relata.id_documento)

		if (map.relata.data != null && map.relata.data != "") {
			doc.annoProtocollo = Integer.parseInt(map.relata.anno)
			doc.numeroProtocollo = Integer.parseInt(map.relata.numero)
			doc.dataNumeroProtocollo = new SimpleDateFormat("dd/MM/yyyy").parse(map.relata.data)
		}

		FileAllegatoGenerico fAllegato = new FileAllegatoGenerico();
		fAllegato.idFileEsterno 	= Long.parseLong(map.relata.id_oggetto_file)
		fAllegato.id 				= Long.parseLong(map.relata.id_oggetto_file)
		fAllegato.nome 			= map.relata.filename
		fAllegato.contentType 	= GestioneTestiService.getContentType(map.relata?.filename?.substring(map.relata.filename?.lastIndexOf(".")+1))

		attiFileDownloader.downloadFileAllegato(doc, fAllegato)
	}
}
