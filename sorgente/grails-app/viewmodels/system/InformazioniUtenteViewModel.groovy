package system

import grails.plugin.springsecurity.SpringSecurityService
import groovy.xml.StreamingMarkupBuilder
import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.utility.UtenteService
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.AttiAd4Service
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.beans.AttiFileDownloader
import it.finmatica.atti.documenti.beans.AttiGestoreTransazioneFirma
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.PreferenzaUtenteService
import it.finmatica.atti.integrazioni.AttiFirmaService
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.zkoss.bind.BindContext
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.media.Media
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class InformazioniUtenteViewModel {

	AttiGestoreTransazioneFirma attiGestoreTransazioneFirma
	SpringSecurityService 		springSecurityService
	GestioneTestiService 		gestioneTestiService
	AttiFileDownloader			attiFileDownloader
	GrailsApplication 			grailsApplication
	AttiAd4Service 				attiAd4Service
	UtenteService 				utenteService
	IGestoreFile 				gestoreFile
	PreferenzaUtenteService		preferenzaUtenteService

	// component
	Window self

	String nominativo
	String cognomeNome
	String codiceUtente

	String amministrazione
	String ottica
	String ruoloAccesso

	String vecchiaPassword
	String nuovaPassword
	String confermaPassword
	boolean passwordVerificata = false
	boolean editaTestoAppletJava = true
	boolean cambioPassword

	def ruoliUo

	long idAllegato, idFileAllegato
	def preferenze

	String tipoEditor

	@Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		self 		= w
		idAllegato  = -1;
		idFileAllegato = -1;
		nominativo  = springSecurityService.currentUser.nominativo;
		cognomeNome = springSecurityService.currentUser.nominativoSoggetto?:"";
		codiceUtente= springSecurityService.currentUser.id;
		Ad4Ruolo r = Ad4Ruolo.get(springSecurityService.principal.authorities.authority[0]);

		ruoloAccesso = "${r.ruolo.substring(grailsApplication.config.grails.plugins.amministrazionedatabase.modulo.length()+1)} - ${r.descrizione}"

		amministrazione = springSecurityService.principal.amm().codice+" - "+springSecurityService.principal.amm().descrizione
		ottica = springSecurityService.principal.ottica().codice+" - "+springSecurityService.principal.ottica().descrizione

		ruoliUo = [];
		for (def uo : springSecurityService.principal.uo()) {
			ruoliUo << [tipo:"uo", codice:uo.id, dal:uo.dal, descrizione:uo.descrizione];

			for (def ruolo : uo.ruoli) {
				ruoliUo << [tipo:"ruolo", codice:ruolo.codice, descrizione:ruolo.descrizione]
			}
		}
		caricaPreferenze()

		tipoEditor = Impostazioni.EDITOR_DEFAULT.valore;
		cambioPassword = Impostazioni.CAMBIO_PASSWORD.abilitato;
		gestioneTestiService.setDefaultEditor(Impostazioni.EDITOR_DEFAULT.valore, Impostazioni.EDITOR_DEFAULT_PATH.valore);
	}

	@NotifyChange("idAllegato")
	@Command onTestFirma (@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
		String url = null;
		logAd4("TEST - Firma.")
		Allegato.withTransaction {
			eliminaAllegato ();

			Media media = event.media

			Allegato allegato = new Allegato();
			allegato.titolo = "ALLEGATO PER FIRMA DI TEST, SI PUO' ELIMINARE SENZA PROBLEMI."
			allegato.statoFirma = StatoFirma.DA_FIRMARE;
			allegato.save()

			FileAllegato fileAllegato 	= new FileAllegato()
			fileAllegato.nome 			= media.name
			fileAllegato.contentType 	= media.contentType
			fileAllegato.dimensione 	= -1

			allegato.addToFileAllegati(fileAllegato)
			allegato.save()

			gestoreFile.addFile(allegato, fileAllegato, media.binary ? media.streamData : new ByteArrayInputStream(media.stringData.bytes))

			attiGestoreTransazioneFirma.addFileDaFirmare(AttiFirmaService.creaIdRiferimento(allegato, fileAllegato), allegato, fileAllegato)
			url = attiGestoreTransazioneFirma.finalizzaTransazioneFirma()
			
			// con il nuovo plugin di firma, posso firmare con la firma senza applet, siccome in questo punto devo "forzare" l'apertura della pagina normale con l'applet, rinomino a manazza la pagina da aprire
			url = url.replace ("/firmaJWS.jsp", "/firma.jsp")
			idAllegato 		= allegato.id;
			idFileAllegato 	= fileAllegato.id;
		}

		logAd4("TEST - Apro popup di firma: $url")
		Executions.createComponents("/commons/popupFirma.zul", self, [urlPopupFirma: url]).doModal()
	}
	
	@NotifyChange("idAllegato")
	@Command onTestFirmaSenzaApplet (@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
		String url = null;
		logAd4("TEST - Firma.")
		Allegato.withTransaction {
			eliminaAllegato ();
			
			Media media = event.media
					
			Allegato allegato = new Allegato();
			allegato.titolo = "ALLEGATO PER FIRMA DI TEST, SI PUO' ELIMINARE SENZA PROBLEMI."
			allegato.statoFirma = StatoFirma.DA_FIRMARE
			allegato.save()
			
			FileAllegato fileAllegato 	= new FileAllegato()
			fileAllegato.nome 			= media.name
			fileAllegato.contentType 	= media.contentType
			fileAllegato.dimensione 	= -1
			
			allegato.addToFileAllegati(fileAllegato)
			allegato.save()
			
			gestoreFile.addFile(allegato, fileAllegato, media.binary ? media.streamData : new ByteArrayInputStream(media.stringData.bytes))
			
			attiGestoreTransazioneFirma.addFileDaFirmare(AttiFirmaService.creaIdRiferimento(allegato, fileAllegato), allegato, fileAllegato)
			url = attiGestoreTransazioneFirma.finalizzaTransazioneFirma()
			
			// con il nuovo plugin di firma, posso firmare con la firma senza applet, siccome in questo punto devo "forzare" l'apertura della pagina con javawebstart, rinomino a manazza la pagina da aprire 
			url = url.replace ("/firma.jsp", "/firmaJWS.jsp")
			idAllegato 		= allegato.id
			idFileAllegato 	= fileAllegato.id
		}
		
		logAd4("TEST - Apro popup di firma: $url")
		Executions.createComponents("/commons/popupFirma.zul", self, [urlPopupFirma: url]).doModal()
	}

	@NotifyChange("tipoEditor")
	@Command onAggiornaEditor () {
		tipoEditor = Impostazioni.EDITOR_DEFAULT.valore
		gestioneTestiService.setDefaultEditor(Impostazioni.EDITOR_DEFAULT.valore, Impostazioni.EDITOR_DEFAULT_PATH.valore)
	}
	
	@Command onAbilitaAppletJava () {
		if (editaTestoAppletJava) {
			gestioneTestiService.abilitaApplet()
		} else {
			gestioneTestiService.abilitaJnlp()
		}
	}

	@Command onTestEditaTesto () {
		logAd4("TEST - Edita Testo.")
		// scelgo un modello a caso tra determine o delibere
		GestioneTestiModello m = GestioneTestiModello.createCriteria().get {
			tipoModello {
				'in'("codice", [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO])
			}

			eq ("tipo", Impostazioni.FORMATO_DEFAULT.valore)

			maxResults(1)
			eq ("valido", true)
		}
		String query = new String(m.tipoModello.query);
		def xml = new XmlSlurper().parseText(query);
		def outputBuilder = new StreamingMarkupBuilder()
		if (xml.testStaticData.documentRoot.text() == "") {
			Clients.showNotification("Non è possibile testare il modello perché nell'XML della query non ci sono i dati di prova nel tag <testStaticData>", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 8000, true)
			return;
		}
		String staticData = outputBuilder.bind{ mkp.yield xml.testStaticData.documentRoot }
		InputStream testo = gestioneTestiService.stampaUnione (new ByteArrayInputStream(m.fileTemplate), staticData, Impostazioni.FORMATO_DEFAULT.valore)

		String urlDocumento = gestioneTestiService.apriEditorTesto(testo, "FILE_DI_TEST_DA_ELIMINARE.${Impostazioni.FORMATO_DEFAULT.valore}");

		logAd4("TEST - Testo creato con successo: $urlDocumento")
	}

	@Command onCambiaPassword () {
		logAd4("TEST - Cambio password.")
		utenteService.updatePassword(vecchiaPassword, nuovaPassword, confermaPassword);
		Clients.showNotification("Password aggiornata con successo.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true)
	}

	@NotifyChange("passwordVerificata")
	@Command onVerificaPassword(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		String conferma = ctx?.triggerEvent?.value?:confermaPassword;
		passwordVerificata = nuovaPassword?.length() > 0 && nuovaPassword.equals(conferma);
	}

	@Command onDownloadFileFirmato () {
		logAd4("TEST - Download del File Firmato per verifica.")
		attiFileDownloader.downloadFileAllegato(Allegato.get(idAllegato), FileAllegato.get(idFileAllegato));
	}

	@Command onClose () {
		if (gestioneTestiService.isEditorAperto()) {
			// se ho un documento ancora aperto, do errore:
			throw new AttiRuntimeException ("Per proseguire è necessario chiudere l'editor aperto.")
		}
		Allegato.withTransaction {
			eliminaAllegato();
		}
		
		Events.postEvent("onClose", self, null)
	}

	private void eliminaAllegato () {
		if (idAllegato > 0) {
			Allegato.withTransaction {
				Allegato a = Allegato.get(idAllegato);
				FileAllegato f = FileAllegato.get(idFileAllegato);
				a.removeFromFileAllegati(f);
				gestoreFile.removeFile(a, f)
				a.delete()
				idAllegato = -1;
				idFileAllegato = -1;
			}
		}
	}

	private void logAd4 (String note) {
		String testo   = "ip macchina client: ${Executions.getCurrent().getRemoteAddr()} - browser: ${Executions.getCurrent().getHeader("User-Agent")}";
		attiAd4Service.logAd4(note, testo);
	}

	@NotifyChange("preferenze")
	@Command onSalvaPreferenza (@BindingParam("codice") String codice, @BindingParam("codiceDescrizione") CodiceDescrizione codiceDescrizione) {
		preferenzaUtenteService.salva(codice, codiceDescrizione.codice)
		caricaPreferenze()
		Clients.showNotification("Preferenza Utente salvata con successo.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true)
	}

	@NotifyChange("preferenze")
	@Command onEliminaPreferenza (@BindingParam("codice") String codice) {
		Messagebox.show("Sei sicuro di voler eliminare questa preferenza?", "Conferma preferenza utente", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event event) {
					if (Messagebox.ON_OK.equals(event.getName())) {
						preferenzaUtenteService.rimuovi(codice)
						InformazioniUtenteViewModel.this.caricaPreferenze()
						Clients.showNotification("Preferenza Utente eliminata.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true)
					}
				}
			}
		)
	}

	private void caricaPreferenze(){
		preferenze = preferenzaUtenteService.getPreferenzeUtente()
		BindUtils.postNotifyChange(null, null, this, "preferenze")
	}
}
