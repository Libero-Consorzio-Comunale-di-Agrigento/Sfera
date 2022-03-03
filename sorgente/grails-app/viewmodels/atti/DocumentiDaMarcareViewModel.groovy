package atti

import atti.ricerca.MascheraRicercaDocumento
import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.DocumentoFactory
import it.finmatica.atti.documenti.DocumentoService
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.StatoMarcatura
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.dto.documenti.viste.DocumentoStepDTOService
import it.finmatica.atti.export.ExportService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.AttiFirmaService
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.SortEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window
import static it.finmatica.zkutils.LabelUtils.getLabel as l

class DocumentiDaMarcareViewModel {

	// services
	SpringSecurityService 	springSecurityService
	AttiGestoreCompetenze 	gestoreCompetenze
	DocumentoStepDTOService documentoStepDTOService
	ExportService			exportService
	DocumentoService		documentoService
	AttiFirmaService		attiFirmaService

	// componenti
	Window self
	Window popupRicercaAvanzata

	// ricerca
	MascheraRicercaDocumento ricerca
	boolean abilitaMarca = false
	boolean abilitaSmarca = false
	def selected
	
	// lista degli allegati del singolo documento (visibile dalla graffetta)
	def listaAllegati

	@Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self = w
		onCambiaTipo()
		BindUtils.postNotifyChange(null, null, this, "ricerca")
	}

	@NotifyChange("ricerca")
	@Command onCambiaTipo() {
		// se sono in init, "ricerca" è null quindi imposto come default la ricerca su Determina.
		// altrimenti, l'utente ha cambiato il tipo documento da interfaccia, quindi me lo segno e reinizializzo la ricerca con quei valori.
		String tipoDocumento 	= ricerca?.tipoDocumento?:Determina.TIPO_OGGETTO
		ricerca 				= new MascheraRicercaDocumento(anno:Calendar.getInstance().get(Calendar.YEAR), tipoDocumento:tipoDocumento)
		ricerca.registroAtto  	= null
		ricerca.ricercaMarcatura = true
		ricerca.daMarcare		 = true

		// elimino la popup di ricerca così da costruirne una nuova (utile soprattutto in caso di passaggio da un tipo documento a un tipo documento collegato
		// ad es. da ricerca su determine a ricerca su certificati.
		popupRicercaAvanzata 	= null
		ricerca.caricaListe()
		ricerca.listaDocumenti = []
		ricerca.totalSize = 0
		ricerca.activePage = 0
		selected = null
	}

	@NotifyChange(["abilitaMarca", "abilitaSmarca"])
	@Command onSelectDocumento () {
		abilitaMarca  = true
		abilitaSmarca = selected.statoMarcatura == StatoMarcatura.MARCATO
	}

	@NotifyChange(["ricerca", "abilitaMarca", "abilitaSmarca"])
	@Command onMarcaDocumenti(){
		abilitaMarca  = false
		abilitaSmarca = false
		def atto = null
		if (selected.tipoDocumentoPrincipale == Delibera.TIPO_OGGETTO && selected.idDocumentoPrincipale > 0) {
			atto = DocumentoFactory.getDocumento(selected.idDocumentoPrincipale).toDTO(['allegati', 'visti', 'certificati'])
		} else {
			atto = DocumentoFactory.getDocumento(selected.idDocumento).toDTO(['allegati', 'visti', 'certificati'])
		}
		Window w = Executions.createComponents("/atti/popupMarcaAllegati.zul", self, [documento : atto, marcati:false])
		w.onClose { event ->
			if(event?.data != null){
				attiFirmaService.aggiungiMarcaturaAllegati(atto, event.data.allegati)
				onRefresh()
				BindUtils.postNotifyChange(null, null, this, "ricerca")
				Messagebox.show(l("label.operazione.marca"),l("label.tab.marcaturaTemporale"), null, Messagebox.INFORMATION, null);
			}
		}
		w.doModal()
		selected = null
	}

	@NotifyChange(["ricerca", "abilitaMarca", "abilitaSmarca"])
	@Command onSmarcaDocumenti(){
		abilitaMarca  = false
		abilitaSmarca = false
		def atto = DocumentoFactory.getDocumento(selected.idDocumento).toDTO()
		Window w = Executions.createComponents("/atti/popupMarcaAllegati.zul", self, [documento : atto, marcati: true])
		w.onClose { event ->
			if(event?.data != null){
				attiFirmaService.rimuoviMarcaturaAllegati(atto, event.data.allegati, event.data.smarcaDocumento)
				onRefresh()
				BindUtils.postNotifyChange(null, null, this, "ricerca")
				Messagebox.show(l("label.operazione.smarca"),l("label.tab.marcaturaTemporale"), null, Messagebox.INFORMATION, null);
			}
		}
		w.doModal()
		selected = null
	}

	@NotifyChange("ricerca")
	@Command onRefresh() {
		ricerca.ricerca (springSecurityService.principal)
	}

	@NotifyChange("ricerca")
	@Command onPagina() {
		ricerca.pagina (springSecurityService.principal)
	}

	@NotifyChange("ricerca")
	@Command onCerca() {
		ricerca.ricerca (springSecurityService.principal)
	}

	@Command onApriDocumento (@BindingParam("documento") def documento) {
		// il caso della proposta di delibera è particolare:
		// se ho cliccato su una riga di proposta di delibera, allora devo decidere se posso aprire la delibera oppure no.

		// posso aprire la delibera solo se:
		// 1) la delibera effettivamente esiste (la proposta potrebbe non essere ancora diventata delibera)
		// 2) la delibera esiste e l'utente corrente ha le competenze in lettura

		// in tutti gli altri casi, apro normalmente la popup.
		if ( (documento.tipoDocumento == Delibera.TIPO_OGGETTO 	|| documento.tipoDocumento == PropostaDelibera.TIPO_OGGETTO) &&
				documento.tipoDocumentoPrincipale == Delibera.TIPO_OGGETTO 	&& documento.idDocumentoPrincipale > 0) {
			
			// se ho le competenze per vedere la delibera, la apro, altrimenti apro la proposta di delibera:
			// questa situazione può esserci quando la delibera è numerata ma non è ancora "pubblicata"
			if (gestoreCompetenze.getCompetenze(Delibera.get(documento.idDocumentoPrincipale)).lettura) {
				apriDocumento("/atti/documenti/delibera.zul", [id: documento.idDocumentoPrincipale])
    		} else {
    			apriDocumento(ricerca.tipiDocumento[PropostaDelibera.TIPO_OGGETTO].zul, [id: documento.idDocumento])
    		}
		} else {
			apriDocumento(ricerca.tipiDocumento[documento.tipoDocumento].zul, [id: documento.idDocumento])
		}
	}

	private void apriDocumento (String zul, def parametri) {
		Window w = Executions.createComponents(zul, self, parametri)
		w.doModal()
	}

	/* GESTIONE "GRAFFETTA" di download degli allegati dalla lista dei documenti */

	@Command onMostraAllegati(@BindingParam("documento") def documento) {
		listaAllegati = documentoStepDTOService.caricaAllegatiDocumento (documento.idDocumento, documento.tipoDocumento)
		BindUtils.postNotifyChange(null, null, this, "listaAllegati")
	}

	@NotifyChange("listaAllegati")
	@Command onOpenAllegati (@ContextParam(ContextType.TRIGGER_EVENT)Event e) {
		if (!(e.isOpen())) {
			listaAllegati = []
		}
	}

	@Command onDownloadFileAllegato (@BindingParam("fileAllegato") def value) {
		documentoStepDTOService.downloadFileAllegato(value)
	}
	
	/* GESTIONE POPUP DI RICERCA AVANZATA */
	@NotifyChange("ricerca")
	@Command apriPopupRicercaAvanzata () {
		if (popupRicercaAvanzata == null) {
			popupRicercaAvanzata = Executions.createComponents(ricerca.tipiDocumento[ricerca.tipoDocumento].popup, self, null);
		}
		popupRicercaAvanzata.doModal();
	}
	
	@NotifyChange("ricerca")
	@Command onChiudiRicercaAvanzata() {
		popupRicercaAvanzata.setVisible(false)
	}
	
	@NotifyChange("ricerca")
	@Command onCercaAvanzata() {
		onChiudiRicercaAvanzata()
		ricerca.ricerca (springSecurityService.principal)
	}
	
	@NotifyChange("ricerca")
	@Command onSvuotaFiltri () {
		MascheraRicercaDocumento nuovaRicerca = new MascheraRicercaDocumento(anno:Calendar.getInstance().get(Calendar.YEAR), tipoDocumento:ricerca.tipoDocumento)
		nuovaRicerca.caricaListe();
		ricerca = nuovaRicerca;
		ricerca.ricerca (springSecurityService.principal)

		popupRicercaAvanzata.setVisible(false)
		popupRicercaAvanzata = null;
		apriPopupRicercaAvanzata()
	}
	
	@NotifyChange("ricerca")
	@Command
	public void onEseguiOrdinamento(@BindingParam("campi") String campi, @ContextParam(ContextType.TRIGGER_EVENT) SortEvent event) {
		for (String campo : campi?.split(",")?.reverse()){
			ricerca.modificaColonnaOrdinamento(campo, event?.isAscending() ? 'asc' : 'desc')
		}
		ricerca.ricerca (springSecurityService.principal)
	}

	@Command
	public void onExportExcel() {
		if (ricerca.totalSize > Impostazioni.ESPORTAZIONE_NUMERO_MASSIMO.valoreInt){
			Messagebox.show ("Attenzione: il numero dei documenti da esportare supera il massimo consentito.", 
							 "Esportazione interrotta.",
							 Messagebox.OK , Messagebox.EXCLAMATION, null
			);
			return;
		}
		try{
			ricerca.pagina(springSecurityService.principal, true)
			exportService.downloadExcel(ricerca.exportOptions, ricerca.listaDocumenti)
		}
		finally{
			ricerca.pagina(springSecurityService.principal)
		}
	}
	

}
