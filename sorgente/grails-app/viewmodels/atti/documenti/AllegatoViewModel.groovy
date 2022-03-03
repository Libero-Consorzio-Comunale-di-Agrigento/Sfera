package atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.dizionari.TipoAllegato
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.beans.AttiFileDownloader
import it.finmatica.atti.documenti.beans.AttiGestioneTesti
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.dto.commons.FileAllegatoDTO
import it.finmatica.atti.dto.dizionari.TipoAllegatoDTO
import it.finmatica.atti.dto.documenti.*
import it.finmatica.atti.impostazioni.OperazioniLogService
import it.finmatica.atti.impostazioni.CampiDocumento
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.webscan.WebScanService
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang.StringUtils
import org.apache.log4j.Logger
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.media.Media
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class AllegatoViewModel {

	private static final Logger log = Logger.getLogger(AllegatoViewModel.class)

	// services
	AttiGestoreCompetenze 	gestoreCompetenze
	AllegatoDTOService    	allegatoDTOService
	AllegatoService			allegatoService
	AttiFileDownloader	  	attiFileDownloader
	AttiGestioneTesti	  	gestioneTesti
	WebScanService 			webScanService
	SpringSecurityService	springSecurityService
	OperazioniLogService 	operazioniLogService

	// componenti
	Window self

	// dati
	AllegatoDTO allegato
	def fileAllegati

	// stato
	def competenze
	def campiProtetti
	def documento
	String uploadAttributeValue
	String tipoDocumento
	List<TipoAllegatoDTO> listaTipoAllegato
	FileAllegatoDTO testo
	boolean	abilitaCercaDocumenti
	boolean riservatoModificabile
	boolean abilitaRiservato
	boolean abilitaConversionePdf
	boolean abilitaModificaPubblicazione
	
	// gestione del testo
	boolean testoLockato
	boolean lockPermanente
	boolean abilitaCasaDiVetro
	boolean modificaCampi = true

	// indica se il documento deve essere comunque aperto in lettura (delegato)
	boolean forzaCompetenzeLettura
	String paginaLog

	@NotifyChange(["allegato", "listaTipoAllegato", "abilitaCreaFascicolo"])
    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") long idAllegato, @ExecutionArgParam("documento") def documento, @ExecutionArgParam("competenzeLettura") Boolean competenzeLettura, @ExecutionArgParam("paginaLog") String paginaLog) {
		this.self = w

		this.documento			= documento;
		abilitaRiservato 		= Impostazioni.RISERVATO.abilitato
		riservatoModificabile	= abilitaRiservato;
		abilitaCasaDiVetro 		= Impostazioni.INTEGRAZIONE_CASA_DI_VETRO.abilitato
		abilitaConversionePdf   = Impostazioni.ALLEGATO_CONVERTI_PDF.abilitato

		abilitaCercaDocumenti 	= (Impostazioni.DOCER.abilitato || Impostazioni.IMPORT_ALLEGATO_GDM.abilitato)
		forzaCompetenzeLettura  = competenzeLettura
		this.paginaLog			= paginaLog

		if (documento instanceof DeterminaDTO) {
			tipoDocumento = "determina"
		} else if(documento instanceof PropostaDeliberaDTO) {
			tipoDocumento = "propostaDelibera"
		} else if(documento instanceof DeliberaDTO) {
			tipoDocumento = "delibera"
		} else if(documento instanceof VistoParereDTO) {
			tipoDocumento = "vistoParere"
		}

		if (idAllegato > 0) {
			Allegato a 	= Allegato.get(idAllegato)
			competenze 	= gestoreCompetenze.getCompetenze(a)
			competenze.modifica = (paginaLog?.trim()?.length() > 0) || ((competenze.modifica||forzaCompetenzeLettura) && (!(documento.hasProperty("campiProtetti") && CampiDocumento.getMappaCampi(documento.campiProtetti).ALLEGATI)))
			if (a.tipoAllegato != null) {
				modificaCampi = a.tipoAllegato.modificaCampi
			}
			allegato 	= a.toDTO()
			ricaricaFileAllegati()
			// verifico che l'utente possa gestire il riservato:
			riservatoModificabile 	= (!allegato.riservato || gestoreCompetenze.utenteCorrenteVedeRiservato(a.getDocumentoPrincipale()));
		} else {
			allegato = new AllegatoDTO([id: idAllegato, (tipoDocumento): documento, valido:true])
			competenze = [lettura: true, modifica: true, cancellazione: true]

			allegato.stampaUnica 	= Impostazioni.ALLEGATO_STAMPA_UNICA_DEFAULT.abilitato
			allegato.numPagine 		= null
			allegato.quantita 		= 1
			allegato.statoFirma 	= Impostazioni.ALLEGATO_STATO_FIRMA_DEFAULT.valore
			allegato.sequenza 		= caricaNumeroSequenza(documento)
			allegato.riservato		= abilitaRiservato && Impostazioni.RISERVATO_DEFAULT.abilitato

			allegato.pubblicaCasaDiVetro = Impostazioni.INTEGRAZIONE_CASA_DI_VETRO.abilitato && documento.domainObject.tipologiaDocumento.pubblicazione && documento.domainObject.tipologiaDocumento.pubblicaAllegati
			allegato.pubblicaAlbo = documento.domainObject.tipologiaDocumento.pubblicazione && documento.domainObject.tipologiaDocumento.pubblicaAllegati && documento.domainObject.tipologiaDocumento.pubblicaAllegatiDefault
			allegato.pubblicaVisualizzatore = allegato.pubblicaAlbo
		}

		if ((allegato.codice == Allegato.ALLEGATO_OMISSIS || allegato.codice == Allegato.ALLEGATO_SCHEDA_CONTABILE  || allegato.codice == Allegato.ALLEGATO_MODIFICABILE) && fileAllegati.size() > 0) {
			testo = fileAllegati.first();
			abilitaConversionePdf = false;
		}

		abilitaModificaPubblicazione = documento.domainObject.tipologiaDocumento.pubblicaAllegati;

		// inizializzo i parametri necessari all'accettazione dell'allegato inserito
		uploadAttributeValue	= "true,maxsize=${(Integer.parseInt(Impostazioni.ALLEGATO_DIMENSIONE_MASSIMA.valore)*1024)}, native"

		caricaListaTipoAllegato()
    }

	@NotifyChange(["allegato", "modificaCampi"])
	@Command
	void onCambiaTipoAllegato () {
		// aggiorno la configurazione proveniente dal tipo allegato scelto, solo se non ho già salvato l'allegato (in tal caso, do' priorità alla scelta dell'utente)
		if (allegato.id > 0 || allegato.tipoAllegato == null) {
			return
		}

		allegato.pubblicaCasaDiVetro = allegato.tipoAllegato.pubblicaCasaDiVetro && Impostazioni.INTEGRAZIONE_CASA_DI_VETRO.abilitato && documento.domainObject.tipologiaDocumento.pubblicazione && documento.domainObject.tipologiaDocumento.pubblicaAllegati
		allegato.pubblicaAlbo 		= allegato.tipoAllegato.pubblicaAlbo && documento.domainObject.tipologiaDocumento.pubblicazione && documento.domainObject.tipologiaDocumento.pubblicaAllegati && documento.domainObject.tipologiaDocumento.pubblicaAllegatiDefault
		allegato.pubblicaVisualizzatore = allegato.tipoAllegato.pubblicaVisualizzatore
		allegato.stampaUnica		 = allegato.tipoAllegato.stampaUnica
		allegato.statoFirma			 = allegato.tipoAllegato.statoFirma ?: Impostazioni.ALLEGATO_STATO_FIRMA_DEFAULT.valore
		allegato.codice				 = allegato.tipoAllegato.codice

		modificaCampi = allegato.tipoAllegato.modificaCampi
	}

	private int caricaNumeroSequenza (def documento) {
		Class domainDocumento = null
		if (tipoDocumento == "determina") {
			domainDocumento = Determina
		} else if (tipoDocumento == "delibera") {
			domainDocumento = Delibera
		} else if (tipoDocumento == "propostaDelibera") {
			domainDocumento = PropostaDelibera
		} else if (tipoDocumento == "vistoParere") {
			domainDocumento = VistoParere
		}

		int numeroAllegati = 1 + domainDocumento.createCriteria().get() {
			projections {
				allegati {
					countDistinct("titolo")
				}
			}
			eq("id", documento.id)
		}
		return numeroAllegati
	}

	private void caricaListaTipoAllegato () {
		List<TipoAllegato> lista = TipoAllegato.createCriteria().list() {
			eq ("valido", true)
			or {
				isNull("tipologia")
				if (tipoDocumento == "vistoParere" && (documento.documentoPrincipale instanceof DeliberaDTO || documento.documentoPrincipale instanceof PropostaDeliberaDTO)) {
					eq("tipologia", VistoParere.TIPO_OGGETTO_PARERE)
				}
				else if (tipoDocumento == "vistoParere" && (documento.documentoPrincipale instanceof DeterminaDTO)) {
					eq("tipologia", VistoParere.TIPO_OGGETTO)
				}
				else  {
					eq("tipologia", documento.domainObject.TIPO_OGGETTO)
				}
			}
			order("titolo","asc")
		}
		listaTipoAllegato = lista.toDTO()
	}

	private void ricaricaFileAllegati () {
		fileAllegati = Allegato.createCriteria().list {
			projections {
				fileAllegati {
					property "nome"           // 0
					property "id"             // 1
					property "contentType"    // 2
					property "modificabile"   // 3
					property "firmato"        // 4
					property "dimensione"     // 5
					property "version"		  // 6 la proprietà version serve per poter gestire correttamente il controllo di modifica concorrente su edita-testo
				}
			}
			eq ("id", allegato.id)
			fileAllegati {
				or {
					isNotNull("allegato")
					ge("idFileEsterno",(long)0)
				}
			}
			fileAllegati {
				order("id", "asc")
			}
		}.collect { row -> new FileAllegatoDTO(nome: row[0], id: row[1], contentType: row[2], modificabile:row[3], firmato:row[4], dimensione:row[5], version: row[6]) }
		BindUtils.postNotifyChange(null, null, this, "fileAllegati")
	}

	@NotifyChange(["fileAllegati", "allegato"])
	@Command onUploadFileAllegato (@ContextParam(ContextType.TRIGGER_EVENT) Event event) {

		if (allegato.titolo == null) {
			Messagebox.show("Compilare il campo obbligatorio titolo", "Aggiungi", Messagebox.OK, Messagebox.EXCLAMATION)
			return
		}

		def medias = event.getMedias()
		for (Media media : medias) {
			String nomefile = media.name;

			if (nomefile.contains("'") || nomefile.contains("@")) {
				Clients.showNotification("Impossibile caricare il file: il nome dell'allegato contiene caratteri non consentiti ( ' @ ).", Clients.NOTIFICATION_TYPE_ERROR, self, "before_center", 3000, true);
				return;
			}
			if (Impostazioni.ALLEGATO_VERIFICA_NOMEFILE.abilitato && AttiUtils.controllaAllegato(nomefile)){
				Clients.showNotification("Impossibile caricare il file: il nome dell'allegato contiene caratteri non consentiti.", Clients.NOTIFICATION_TYPE_ERROR, self, "before_center", 3000, true);
				return;
			}

			// Controllo che non esista già un file con questo nome:
			int count = Allegato.numeroFilePerNome(allegato.id, nomefile).get()

			if (count > 0) {
				Clients.showNotification("Non è possibile caricare due volte un file con lo stesso nome: ${nomefile}.", Clients.NOTIFICATION_TYPE_ERROR, self, "before_center", 3000, true);
				return;
			}

			if (!Impostazioni.ALLEGATO_FORMATI_POSSIBILI.valori.contains(FilenameUtils.getExtension(nomefile).toLowerCase())) {
				Clients.showNotification("Impossibile caricare il file: l'allegato è di un tipo non consentito", Clients.NOTIFICATION_TYPE_ERROR, self, "before_center", 3000, true);
				return;
			}

			if (Impostazioni.SU_FORMATI_ESCLUSI.valori.contains(FilenameUtils.getExtension(nomefile).toLowerCase())) {
				Clients.showNotification("Il file inserito non è permesso in Stampa Unica.", Clients.NOTIFICATION_TYPE_WARNING, self, "before_center", 3000, true);
				allegato.stampaUnica = false
			}

			uploadFile(media.name, media.contentType, media.binary ? media.streamData : new ByteArrayInputStream(media.stringData.bytes))
		}
		
		ricaricaFileAllegati()
	}

	private void uploadFile (String nomeFile, String contentType, InputStream inputStream) {
		allegato = allegatoDTOService.uploadFile (allegato, nomeFile, contentType, inputStream)
		scriviLog("Upload File Allegato",  "Salvato File Allegato \"${nomeFile}\"")
	}

	@Command onApriPopupScansione () {
		if (allegato.titolo == null) {
			Messagebox.show("Compilare il campo obbligatorio titolo", "Scansione", Messagebox.OK, Messagebox.EXCLAMATION)
			return
		}
		String ente   = springSecurityService.principal.amm().codice
		long idCallback = webScanService.registerCallback ({ String nominativoUtente, String fileName, String contentType, InputStream inputStream ->
			
			if (!springSecurityService.loggedIn) {
				AttiUtils.eseguiAutenticazione(nominativoUtente, ente)
			}
			
			uploadFile (fileName, contentType, inputStream)
		}, springSecurityService.currentUser.nominativo)

		String urlScansione = webScanService.buildUrlWebScan (idCallback)

		Window w = Executions.createComponents ("/commons/popupScansione.zul", self, [urlScansione: urlScansione])
		w.onClose {
			// elimino la callback
			webScanService.unregisterCallback(idCallback)
			
			// ricarico gli allegati
			ricaricaFileAllegati()
		}
		w.doModal()
	}


	@NotifyChange(["fileAllegati", "allegato"])
	@Command onApriPopupRicercaDocumenti () {
		if (allegato.titolo == null) {
			Messagebox.show("Compilare il campo obbligatorio titolo", "Ricerca", Messagebox.OK, Messagebox.EXCLAMATION)
			return
		}

		Window w = Executions.createComponents ((Impostazioni.DOCER.abilitato)?"/commons/popupRicercaDocumenti.zul":"/commons/popupImportAllegatiIntegrazione.zul", self, [allegato:allegato])
		w.onClose { Event e  ->
			allegato = e.data
			// ricarico gli allegati
			ricaricaFileAllegati()

			BindUtils.postNotifyChange(null, null, this, "allegato")
			BindUtils.postNotifyChange(null, null, this, "fileAllegati")
		}
		w.doModal()
	}


	@NotifyChange("fileAllegati")
	@Command onEliminaFileAllegato (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("fileAllegato") def value) {
		Messagebox.show("Eliminare il file selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						allegatoDTOService.eliminaFileAllegato (allegato, value.id)
						AllegatoViewModel.this.ricaricaFileAllegati ()
						scriviLog("Eliminato File Allegato",  "Eliminato il File Allegato \"${value.nome}\"")
					}
				}
			}
		)
	}

	@Command onDownloadFileAllegato (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("fileAllegato") def value) {
		attiFileDownloader.downloadFileAllegato (allegato.domainObject, FileAllegato.get(value.id), false);
	}

	@Command onDownloadPdfFileAllegato (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("fileAllegato") def value) {
		allegatoService.anteprimaAllegatoPdf (allegato.domainObject, FileAllegato.get(value.id));
	}


	private boolean validaMaschera () {
		def messaggi = [];

		if (allegato.titolo?.size() >255) {
			messaggi << ("Dimensioni del campo Titolo superiori a 255 caratteri")
		}

		if (allegato.titolo == null) {
			messaggi << ("il campo 'Titolo' è obbligatorio")
		}
		else {
			allegato.titolo = AttiUtils.replaceCaratteriSpeciali(allegato.titolo)
		}

		if (allegato.titolo != null && !AttiUtils.controllaCharset(allegato.titolo)) {
			messaggi << "Il campo 'Titolo' contiene dei caratteri non supportati."
		}

		if (allegato.statoFirma == StatoFirma.DA_FIRMARE && !(fileAllegati?.size() > 0) && !(allegato.codice == Allegato.ALLEGATO_OMISSIS || allegato.codice == Allegato.ALLEGATO_SCHEDA_CONTABILE  || allegato.codice == Allegato.ALLEGATO_MODIFICABILE)) {
			messaggi << ("Se si vuole firmare l'allegato è necessario inserire almeno un file")
		}

		if (allegato.stampaUnica && !(fileAllegati?.size() > 0) && !(allegato.codice == Allegato.ALLEGATO_OMISSIS || allegato.codice == Allegato.ALLEGATO_SCHEDA_CONTABILE  || allegato.codice == Allegato.ALLEGATO_MODIFICABILE)) {
			messaggi << ("Se si vuole inserire l'allegato in stampa unica è necessario inserire almeno un file")
		}

		if (allegato.stampaUnica && allegato.riservato) {
			messaggi << ("Un allegato riservato non può essere inserito in stampa unica")
		}

		if (allegato.descrizione != null) {
			allegato.descrizione = AttiUtils.replaceCaratteriSpeciali(allegato.descrizione)
 			if (!AttiUtils.controllaCharset(allegato.descrizione)) {
				messaggi << "Il campo 'Descrizione' contiene dei caratteri non supportati."
			}
		}

		if (messaggi.size() > 0) {
			messaggi.add(0, "Impossibile salvare l'allegato:");
			Clients.showNotification(StringUtils.join(messaggi, "\n"), Clients.NOTIFICATION_TYPE_ERROR, self, "before_center", 5000, true);
			return false;
		}

		return true;
	}

	@NotifyChange("allegato")
	@Command onSalva () {
		boolean presenzaFormatiFileEscludi

		if (!validaMaschera()) {
			return true;
		}
		
		// controllo se ci sono file non permessi in stampa unica
		// se si, blocco e avviso
		for (i in  fileAllegati.first()) {
			if (Impostazioni.SU_FORMATI_ESCLUSI.valori.contains(FilenameUtils.getExtension(i.nome).toLowerCase())) {
				presenzaFormatiFileEscludi = true
			}
		}

		if (presenzaFormatiFileEscludi && allegato.stampaUnica) {
			Clients.showNotification("Sono presenti file con estensione non ammessa in Stampa Unica.", Clients.NOTIFICATION_TYPE_ERROR, self, "before_center", 3000, true);
			return false
		}

		allegato = allegatoDTOService.salva(allegato).toDTO()
		scriviLog("Salvataggio Allegato", "Salvato allegato \"${allegato.titolo}\"")
		Clients.showNotification("Allegato Salvato", Clients.NOTIFICATION_TYPE_INFO, self, "before_center", 3000, true);
		return true
	}

	@Command onChiudi () {
		// se devo rilasciare il lock sul testo, lo rilascio.
		gestioneTesti.uploadEUnlockTesto(allegato, lockPermanente);

		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@Command onSalvaChiudi() {
		if (!validaMaschera()) {
			return true;
		}
		if (onSalva())
			onChiudi()
	}
	
	/*
	 * Gestione Applet e download Testo
	 */

	@NotifyChange(["testoLockato"])
	@Command editaTesto() {
		testoLockato = gestioneTesti.editaTesto (allegato)
		ricaricaFileAllegati ()
	}

	@Command onEliminaTesto () {
		gestioneTesti.eliminaTesto(allegato, this)
		scriviLog("Eliminato Testo Allegato",  "Eliminato il testo dell'allegato \"${allegato.titolo}\"")
	}

	@NotifyChange(["lockPermanente"])
	@Command onToggleLockPermanente () {
		lockPermanente = !lockPermanente;
	}

	@Command onDownloadTesto () {
		def d = allegato.domainObject
		attiFileDownloader.downloadFileAllegato(d, d.fileAllegati.first())
	}

	void scriviLog(String operazione, String descrizione) {
		if (paginaLog?.length() > 0){
			operazioniLogService.creaLog(allegato.id, Allegato.TIPO_OGGETTO, paginaLog, operazione, descrizione)
		}
	}
}
