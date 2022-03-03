package atti

import atti.ricerca.MascheraRicercaDocumento
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Filedownload
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

import static it.finmatica.zkutils.LabelUtils.getLabel as l

class PopupStampaRegistroViewModel {

	// services
	def springSecurityService
	def grailsApplication

	// componenti
	Window self
	def maschera
	
	// dati
	def tipoOggetto
	GestioneTestiService gestioneTestiService

	@Init init(	@ContextParam(ContextType.COMPONENT) Window w,
			@ExecutionArgParam("tipo") String tipo) {
		this.self = w

		maschera = new MascheraRicercaDocumento(ricercaConservazione: false, anno:Calendar.getInstance().get(Calendar.YEAR), tipoDocumento:tipo)
		maschera.caricaListe()
		
		if (tipo == 'DETERMINA') {
			this.tipoOggetto = [codice: tipo, titolo: l("label.stampaRegistroDetermina"), modelloStampa: "REGISTRO_DETERMINE", modelloEstratto: "ESTRATTO_DETERMINE", oggetto: maschera]
		} else if (tipo == 'DELIBERA') {
			this.tipoOggetto = [codice: tipo, titolo: l("label.stampaRegistroPropostaDelibera"), modelloStampa: "REGISTRO_DELIBERE", modelloEstratto: "ESTRATTO_DELIBERE", oggetto: maschera]
		}
	}

	@Command onChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@Command onCerca () {
		Events.postEvent(Events.ON_CLOSE, self, [status: "Cerca"])
	}


	@NotifyChange("tipoOggetto")
	@Command svuotaFiltri () {
		maschera = new MascheraRicercaDocumento(ricercaConservazione: false, anno:Calendar.getInstance().get(Calendar.YEAR), tipoDocumento:tipoOggetto.codice)
		tipoOggetto.oggetto = maschera
		tipoOggetto.oggetto.caricaListe()
	}

	@Command onStampa () {
		if (!tipoOggetto.oggetto.isFiltriAttivi()) {
			Messagebox.show("Valorizzare almeno un campo di ricerca", tipoOggetto.titolo, Messagebox.OK, Messagebox.EXCLAMATION)
		} else {
			GestioneTestiModello modello = GestioneTestiModello.createCriteria().get(){
				eq("tipoModello.codice", tipoOggetto.modelloStampa)
				eq("valido", true)
			}

			if (modello == null) {
				throw new AttiRuntimeException ("Attenzione: non è possibile eseguire la stampa perchè non è presente il modello di testo per il "+tipoOggetto.modelloStampa);
			}

			def mappaParametri 	= getMappaParametri()
			InputStream testoPdf = gestioneTestiService.stampaUnione(modello, mappaParametri, GestioneTestiService.FORMATO_PDF)

			Filedownload.save(testoPdf, "application/pdf" , tipoOggetto.titolo+".pdf")
		}
	}

	@Command onStampaEstratti () {
		if (!tipoOggetto.oggetto.isFiltriAttivi()) {
			Messagebox.show("Valorizzare almeno un campo di ricerca", tipoOggetto.titolo, Messagebox.OK, Messagebox.EXCLAMATION)
		} else {
			GestioneTestiModello modello = GestioneTestiModello.createCriteria().get(){
				eq("tipoModello.codice", tipoOggetto.modelloEstratto)
				eq("valido", true)
			}

			if (modello == null) {
				throw new AttiRuntimeException ("Attenzione: non è possibile eseguire la stampa perchè non è presente il modello di testo per il "+tipoOggetto.modelloEstratto);
			}

			def mappaParametri = getMappaParametri()

			InputStream testoPdf = gestioneTestiService.stampaUnione(modello, mappaParametri, GestioneTestiService.FORMATO_PDF)

			Filedownload.save(testoPdf, "application/pdf" , tipoOggetto.titolo+".pdf")
		}
	}

	private def getMappaParametri() {
		String formatoDateDefault = "dd/MM/yyyy"
		def mappaParametri = [:]
		mappaParametri["p_anno"] = tipoOggetto.oggetto.anno?.toString()
		mappaParametri["p_registro"] = tipoOggetto.oggetto.registroAtto
		mappaParametri["p_da_numero"] = tipoOggetto.oggetto.numeroAttoDal?.toString()
		mappaParametri["p_a_numero"] = tipoOggetto.oggetto.numeroAttoAl?.toString()
		mappaParametri["p_data_adozione_dal"] = tipoOggetto.oggetto.dataAdozioneDal?.format(formatoDateDefault)
		mappaParametri["p_data_adozione_al"] = tipoOggetto.oggetto.dataAdozioneAl?.format(formatoDateDefault)
		mappaParametri["p_data_pubb_inizio_dal"] = tipoOggetto.oggetto.dataPubblicazioneDal?.format(formatoDateDefault)
		mappaParametri["p_data_pubb_inizio_al"] = null
		mappaParametri["p_data_pubb_fine_dal"] = null
		mappaParametri["p_data_pubb_fine_al"] = tipoOggetto.oggetto.dataPubblicazioneAl?.format(formatoDateDefault)
		mappaParametri["p_data_esec_dal"] = tipoOggetto.oggetto.dataEsecutivitaDal?.format(formatoDateDefault)
		mappaParametri["p_data_esec_al"] = tipoOggetto.oggetto.dataEsecutivitaAl?.format(formatoDateDefault)
		mappaParametri["p_codice_dirigente"] = tipoOggetto.oggetto.firmatario?.id
		mappaParametri["p_uo_proponente"] = tipoOggetto.oggetto.unitaProponente?.progr?.toString()
		mappaParametri["p_uo_proponente_ottica"] = tipoOggetto.oggetto.unitaProponente?.ottica?.codice?.toString()
		mappaParametri["p_uo_proponente_dal"] = tipoOggetto.oggetto.unitaProponente?.dal?.format(formatoDateDefault)
		mappaParametri["p_utente_sessione"] = springSecurityService.principal.getSoggetto().utenteAd4
		mappaParametri["p_ente"] = springSecurityService.principal.amm().codice
		return mappaParametri
	}
}