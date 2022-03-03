package dizionari.impostazioni

import afc.AfcAbstractRecord
import groovy.xml.StreamingMarkupBuilder
import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.atti.documenti.tipologie.*
import it.finmatica.atti.dto.documenti.tipologie.GestioneTestiModelloCompetenzaDTO
import it.finmatica.atti.dto.documenti.tipologie.GestioneTestiModelloCompetenzaDTOService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.odg.Commissione
import it.finmatica.gestionetesti.CorrettoreTesto
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.gestionetesti.reporter.GestioneTestiTipoModello
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiTipoModelloDTO
import it.finmatica.gestionetesti.ui.dizionari.GestioneTestiModelloDTOService
import org.apache.commons.io.IOUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.mapping.LinkGenerator
import org.hibernate.FetchMode
import org.hibernate.criterion.CriteriaSpecification
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.media.Media
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Filedownload
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class GestioneTestiModelloDettaglioViewModel  extends AfcAbstractRecord {

	GrailsApplication		grailsApplication
	LinkGenerator 			grailsLinkGenerator
	GestioneTestiService	gestioneTestiService

	GestioneTestiModelloDTO 	selectedRecord
	boolean fileGiaInserito

	List<GestioneTestiTipoModelloDTO> listaGestioneTestiTipoModelloDTO
	List<GestioneTestiModelloCompetenzaDTO> listaGestioneTestiModelloCompetenza

	def campiDisponibili

	// services
	GestioneTestiModelloDTOService           gestioneTestiModelloDTOService
	GestioneTestiModelloCompetenzaDTOService gestioneTestiModelloCompetenzaDTOService

	@NotifyChange(["selectedRecord", "fileGiaInserito"])
    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w
		fileGiaInserito = false

		if (id != null) {
			selectedRecord = caricaGestioneTestiModelloDto(id)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
			caricaListaGestioneTestiModelloCompetenza()
		} else {
			selectedRecord = new GestioneTestiModelloDTO(valido:true)
		}

		caricaListaGestioneTestiTipoModello()
		caricaCampiDisponibili();
    }

	@Command
	public void onSelectTipoModello () {
		caricaCampiDisponibili();
	}

	private void caricaCampiDisponibili () {
		GestioneTestiTipoModello tipoModello = selectedRecord.tipoModello?.domainObject;
		campiDisponibili = [];
		if (tipoModello == null) {
			BindUtils.postNotifyChange(null, null, this, "campiDisponibili");
			return;
		}
		def xml = new XmlSlurper().parseText(new String(tipoModello.query));

		// per ogni query, prendo l'id e il suo alias:
		xml.queryes.'**'.findAll { it.name() == 'query'}.each { q ->
			def campi = q.@help_field_aliases.text().split(",")*.trim();

			campiDisponibili << [ nome: 		q.@id.text()
								, descrizione: 	q.@help_descrizione.text()
								, istruzione:  	"[#list documentRoot.${q.@id.text()} as ${q.@help_query_alias.text()}]\n[/#list]"];

			def campiQuery = [];
			// per ogni campo, cerco il corrispondente e lo metto nei campi possibili:
			for (String campo : campi) {
				def c = xml.definitions.metaDato.find { it.nomeSimbolico.text() == campo };
				if (c != null) {
					campiQuery << [ nome: 			c.nomeSimbolico.text()
								  , descrizione:	c.descrizione.text()
								  , istruzione: 	"\${documentRoot."+q.@id.text()+"."+c.nomeSimbolico.text()+"}\n\${"+q.@help_query_alias.text()+"."+c.nomeSimbolico.text()+"}"];
				}
			}
			campiDisponibili.addAll(campiQuery.sort {it.nome});
		}

		BindUtils.postNotifyChange(null, null, this, "campiDisponibili");
	}

	private GestioneTestiModelloDTO caricaGestioneTestiModelloDto (Long id) {
		GestioneTestiModello gestioneTestiModello = GestioneTestiModello.createCriteria().get {
			eq("id", id)
		}
		GestioneTestiModelloDTO result = gestioneTestiModello.toDTO()
		if (result.fileTemplate != null) {
			fileGiaInserito		= true
			result.fileTemplate	= null
		} else {
			fileGiaInserito=false
		}
		return result
	}

	private void caricaListaGestioneTestiTipoModello() {
		listaGestioneTestiTipoModelloDTO = GestioneTestiTipoModello.createCriteria().list() {
			eq("valido", true)
			order("codice", "asc")
		}.toDTO()
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica", "fileGiaInserito"])
	@Command onUpload (@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
		Media media = event.media
		selectedRecord = gestioneTestiModelloDTOService.salva(selectedRecord, media.byteData, media.name);
		aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
		aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)

		if (fileGiaInserito == false) {
			aggiungiCompetenzaDefault(selectedRecord)
		}
		fileGiaInserito=true
	}

	@Command onDownloadOrigineDati () {
        GestioneTestiTipoModello tipoModello = selectedRecord.tipoModello?.domainObject
		String origineDati = gestioneTestiService.creaOrigineDati(new String(tipoModello.query))
        Filedownload.save(origineDati.getBytes("UTF-8"), "text/plain", tipoModello.codice+".txt")
	}

	@Command onDownload (@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
		Filedownload.save(gestioneTestiModelloDTOService.getFileAllegato(selectedRecord.id), selectedRecord.contentType, selectedRecord.nomeFile)
	}

	// metodi che gestiscono l'assegnazione delle competenze ai modelli testo

	private void caricaListaGestioneTestiModelloCompetenza() {
		List<GestioneTestiModelloCompetenza> lista = GestioneTestiModelloCompetenza.createCriteria().list() {
			eq("gestioneTestiModello.id", selectedRecord.id)
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
		listaGestioneTestiModelloCompetenza = lista.toDTO()
		BindUtils.postNotifyChange(null, null, this, "listaGestioneTestiModelloCompetenza")
	}

	@Command onEliminaGestioneTestiModelloCompetenza (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("gestioneTestiModelloCompetenza") GestioneTestiModelloCompetenzaDTO tipoDetCompetenza) {
		Messagebox.show("Eliminare la competenza selezionata?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						gestioneTestiModelloCompetenzaDTOService.elimina(tipoDetCompetenza)
						GestioneTestiModelloDettaglioViewModel.this.caricaListaGestioneTestiModelloCompetenza()
					}
				}
			}
		)
	}

	@Command onAggiungiGestioneTestiModelloCompetenza () {
		Window w = Executions.createComponents ("/commons/popupCompetenzaDettaglio.zul", self, [documento: selectedRecord, tipoDocumento: "modelloCompetenza"])
		w.onClose {
			caricaListaGestioneTestiModelloCompetenza()
		}
		w.doModal()
	}

	private void aggiungiCompetenzaDefault(GestioneTestiModelloDTO gestioneTestiModello) {
		GestioneTestiModelloCompetenzaDTO defaultCompetenza = new GestioneTestiModelloCompetenzaDTO()
		String codiceRuolo = Impostazioni.RUOLO_ACCESSO_APPLICATIVO.valore
		defaultCompetenza.ruoloAd4 = Ad4Ruolo.createCriteria().get() {
			eq("ruolo", codiceRuolo)
		}.toDTO()
		defaultCompetenza.titolo = "Visibile a tutti"
		defaultCompetenza.gestioneTestiModello = gestioneTestiModello
		defaultCompetenza.lettura = true
		gestioneTestiModelloCompetenzaDTOService.salva(defaultCompetenza)
		caricaListaGestioneTestiModelloCompetenza()
	}

	// Estendo i metodi abstract di AfcAbstractRecord

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		selectedRecord = gestioneTestiModelloDTOService.salva(selectedRecord)
		aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
		aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalvaChiudi() {
		onSalva()
		onChiudi ()
	}

	@Command onSettaValido(@BindingParam("valido") boolean valido) {
		// se voglio disattivare il modello testo, prima verifico che non sia usato da nessuna tipologia di determina/delibera ancora valida.
		if (selectedRecord.valido && valido == false) {
			def tipologie = [];
			tipologie.addAll(TipoDetermina.inUsoPerModelloTesto(selectedRecord.id).list())
			tipologie.addAll(TipoDelibera.inUsoPerModelloTesto(selectedRecord.id).list())
			tipologie.addAll(TipoCertificato.inUsoPerModelloTesto(selectedRecord.id).list())
			tipologie.addAll(TipoVistoParere.inUsoPerModelloTesto(selectedRecord.id).list())
			tipologie.addAll(Commissione.inUsoPerModelloTesto(selectedRecord.id).list())

			if (tipologie.size() > 0) {
				Clients.showNotification ("Non è possibile disattivare il modello testo perché è usato da altre tipologie ancora attive:\n" +
										  (tipologie.titolo.join("\n")), Clients.NOTIFICATION_TYPE_WARNING, self, "before_center", tipologie.size()*3000, true);
				return;
			}
		}

		Messagebox.show(Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTesto",[valido?"valido":"non valido"].toArray()), Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
				public void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						GestioneTestiModelloDettaglioViewModel.this.selectedRecord.valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, GestioneTestiModelloDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, GestioneTestiModelloDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, GestioneTestiModelloDettaglioViewModel.this, "datiModifica")
					}
				}
			}
		)
	}

	@Command onVisualizzaCampi () {
		String urlTipoModello = grailsLinkGenerator.link(absolute:true, controller:'gestioneTesti', action:'tipoModelloTesto', id:selectedRecord.tipoModello.codice);
		String urlReporter	  = grailsApplication.config.grails.plugins.gestionetesti.urlReporter?:"";
		if (urlReporter == null || urlReporter == "") {
			Clients.showNotification("Non è possibile procedere con la visualizzazione dei campi perché il parametro urlReporter non è settato.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 8000, true)
			return;
		}

		String url = "${urlReporter}/CreateHelpServletByURL?sourceXmlDataURL=${urlTipoModello}";
		Clients.evalJavaScript("window.open('" + url + "','','top=100,left=200,height=600,width=800,scrollbars=1,resizable=1')");
	}

	@Command onVerificaModello () {
		String urlTipoModello 	= grailsLinkGenerator.link(absolute:true, controller:'gestioneTesti', action:'tipoModelloTesto', id:selectedRecord.tipoModello.codice);
		String urlModello 		= grailsLinkGenerator.link(absolute:true, controller:'gestioneTesti', action:'modelloTesto', id:selectedRecord.id);
		String urlReporter	  	= grailsApplication.config.grails.plugins.gestionetesti.urlReporter?:"";
		if (urlReporter == null || urlReporter == "") {
			Clients.showNotification("Non è possibile procedere con la verifica del modello perché il parametro urlReporter non è settato.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 8000, true)
			return;
		}
		String url = "${urlReporter}/CheckTemplateServletByURL?sourceTemplateURL=${urlModello}&sourceXmlDataURL=${urlTipoModello}";
		Clients.evalJavaScript("window.open('" + url + "','','top=100,left=200,height=600,width=800,scrollbars=1,resizable=1')");
	}

	@Command onCorreggiModello () {
		GestioneTestiModello m = selectedRecord.domainObject;
		CorrettoreTesto correttore = new CorrettoreTesto();
		InputStream is = correttore.correggiTesto(new ByteArrayInputStream(m.fileTemplate), m.tipo);
		m.fileTemplate = IOUtils.toByteArray(is);
		m.save();

		Clients.showNotification("Il modello è stato corretto, cliccare Prova Modello per verificarlo.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true)
	}

	@Command onProvaModello () {
		GestioneTestiModello m = selectedRecord.domainObject;
		String query = new String(m.tipoModello.query);
		def xml = new XmlSlurper().parseText(query);
		def outputBuilder = new StreamingMarkupBuilder()
		if (xml.testStaticData.documentRoot.text() == "") {
			Clients.showNotification("Non è possibile testare il modello perché nell'XML della query non ci sono i dati di prova nel tag <testStaticData>", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 8000, true)
			return;
		}

		String staticData = outputBuilder.bind{ mkp.yield xml.testStaticData.documentRoot }
		InputStream testo = gestioneTestiService.stampaUnione (new ByteArrayInputStream(m.fileTemplate), staticData, m.tipo)
		Filedownload.save(testo, m.contentType, "${m.nome}.${m.tipo}");
	}

	@Command onProvaModelloPdf () {
		GestioneTestiModello m = selectedRecord.domainObject;
		String query = new String(m.tipoModello.query);
		def xml = new XmlSlurper().parseText(query);
		def outputBuilder = new StreamingMarkupBuilder()
		if (xml.testStaticData.documentRoot.text() == "") {
			Clients.showNotification("Non è possibile testare il modello perché nell'XML della query non ci sono i dati di prova nel tag <testStaticData>", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 8000, true)
			return;
		}

		String staticData = outputBuilder.bind{ mkp.yield xml.testStaticData.documentRoot }
		InputStream testo = gestioneTestiService.stampaUnione (new ByteArrayInputStream(m.fileTemplate), staticData, GestioneTestiService.FORMATO_PDF)
		Filedownload.save(testo, GestioneTestiService.getContentType(GestioneTestiService.FORMATO_PDF), "${m.nome}.${GestioneTestiService.FORMATO_PDF}");
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onDuplica () {
		selectedRecord = gestioneTestiModelloDTOService.duplica(selectedRecord);
		Clients.showNotification("Tipologia duplicata.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true)
	}
}
