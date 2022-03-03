package atti

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.*
import it.finmatica.atti.dto.documenti.viste.DocumentoStepDTOService
import it.finmatica.atti.dto.documenti.viste.So4DelegaDTO
import it.finmatica.atti.dto.documenti.viste.So4DelegaService
import it.finmatica.atti.export.ExportService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.AttiFirmaService
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.SortEvent
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.Listbox
import org.zkoss.zul.Listitem
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class DocumentiDaFirmareViewModel {

	// services
	DocumentoStepDTOService documentoStepDTOService
	AttiFirmaService		attiFirmaService
	ExportService			exportService
	So4DelegaService		so4DelegaService
	SpringSecurityService 	springSecurityService

	// componenti
	Window self
	@Wire("#listaDocumentiDaFirmare")
	Listbox listbox

	// dati
	def lista
	def selected
	def selectedItems
	def listaAllegati
	def listaDeleganti
	def delegante

	So4UnitaPubbDTO unitaProponente

	// stato
	def abilitaFirma = false
	def abilitaSblocca = false
	def tipoOggetto
	def tipiOggetto = [[oggetti: null, nome: Labels.getLabel("tipoOggetto.tutti")]
					   , [oggetti: [Determina.TIPO_OGGETTO], nome: Labels.getLabel("tipoOggetto.determine")]
					   , [oggetti: [PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO], nome: Labels.getLabel("tipoOggetto.delibere")]
					   , [oggetti: [VistoParere.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO_PARERE], nome: Labels.getLabel("tipoOggetto.vistiEPareri")]
					   , [oggetti: [Certificato.TIPO_OGGETTO], nome: Labels.getLabel("tipoOggetto.certificati")]
					   , [oggetti: [SedutaStampa.TIPO_OGGETTO], nome: Labels.getLabel("tipoOggetto.seduteStampe")]]

	def zul = [(Determina.TIPO_OGGETTO)           : '/atti/documenti/determina.zul'
			   , (VistoParere.TIPO_OGGETTO)       : '/atti/documenti/visto.zul'
			   , (VistoParere.TIPO_OGGETTO_PARERE): '/atti/documenti/parere.zul'
			   , (Certificato.TIPO_OGGETTO)       : '/atti/documenti/certificato.zul'
			   , (PropostaDelibera.TIPO_OGGETTO)  : '/atti/documenti/propostaDelibera.zul'
			   , (Delibera.TIPO_OGGETTO)          : '/atti/documenti/delibera.zul'
			   , (SedutaStampa.TIPO_OGGETTO)      : '/odg/seduta/sedutaStampa.zul']

	// ricerca
	String testoCerca  = ""

	// paginazione
	int activePage  = 0
	int pageSize 	= 30
	int totalSize 	= 100

	def orderMap = [
		'anno':'asc',
		'numero':'asc',
		'annoProposta':'asc',
		'numeroProposta':'asc'
		]

    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self 		= w
		tipoOggetto 	= tipiOggetto[0]
		caricaLista()
		listaDeleganti = so4DelegaService.getDeleganti(springSecurityService.currentUser)?.toDTO()
		if (listaDeleganti.size() > 0){
			Ad4UtenteDTO utente = springSecurityService.currentUser.toDTO()
			utente.nominativoSoggetto = ""
			listaDeleganti.add(0, utente);
		}
    }

	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
	}

	@NotifyChange(["abilitaFirma", "abilitaSblocca"])
	@Command onSelectDocumento () {
		abilitaFirma = controllaDocumentiSelezionati(false)
		abilitaSblocca = controllaDocumentiSelezionati(true)
	}

	private boolean controllaDocumentiSelezionati (boolean firmatiDaSbloccare) {
		for (Listitem item : listbox.getSelectedItems()) {
			if (firmatiDaSbloccare && item.value.statoFirma != StatoFirma.FIRMATO_DA_SBLOCCARE.toString()) {
				return false;
			}
			else if (!firmatiDaSbloccare && item.value.statoFirma == StatoFirma.FIRMATO_DA_SBLOCCARE.toString()) {
				return false;
			}
		}

		return listbox.getSelectedItems().size() > 0;
	}

	@Command onFirmaDocumenti() {
		activePage = 0
		// giusto per essere sicuri di aver cliccato bene:
		if (!controllaDocumentiSelezionati(false)) {
			Messagebox.show("Attenzione: non è possibile firmare documenti di tipo diverso. Selezionare documenti omogenei")
			return;
		}

		String urlFirma = attiFirmaService.multiFirma (listbox.getSelectedItems()*.value);

		Window w = Executions.createComponents("/commons/popupFirma.zul", self, [urlPopupFirma: urlFirma])
		w.onClose { event ->
			caricaLista()
		}
		w.doModal()
	}


    @NotifyChange(["abilitaSblocca"])
	@Command onSbloccaDocumenti() {
		activePage = 0

		if (!controllaDocumentiSelezionati(true)) {
			Messagebox.show("Attenzione: non è possibile sbloccare documenti di tipo diverso. Selezionare documenti omogenei")
			return;
		}
		attiFirmaService.sbloccaDocumentiFirmati (listbox.getSelectedItems()*.value, (delegante?.id?:springSecurityService.currentUser.id));

        caricaLista()
        abilitaSblocca = false
	}

	@NotifyChange("selectedRecord")
	@Command onItemDoubleClick(@ContextParam(ContextType.COMPONENT) Listitem l) {
		selected = l.value
		onApriDocumento ()
	}

	@Command onApriDocumento () {
		Window w = Executions.createComponents(zul[selected.tipoOggetto], self, [id: selected.idDocumento, idPadre: selected.idPadre, competenzeLettura:true])
		w.doModal()
		w.onClose {
			caricaLista()
		}
	}

	/*
	 * Metodi per la ricerca
	 */

	@Command onRefresh() {
		caricaLista()
	}

	@Command onCerca() {
		activePage = 0
		caricaLista()
	}

	private void caricaLista() {
		def documenti = documentoStepDTOService.inCarico (testoCerca, tipoOggetto?.oggetti, null, [StatoFirma.DA_FIRMARE, StatoFirma.IN_FIRMA, StatoFirma.FIRMATO_DA_SBLOCCARE], pageSize, activePage, orderMap, true, false, unitaProponente?.descrizione, delegante?.domainObject)
		lista	  = documenti.result
		totalSize = documenti.total

		BindUtils.postNotifyChange(null, null, this, "lista")
		BindUtils.postNotifyChange(null, null, this, "totalSize")
		BindUtils.postNotifyChange(null, null, this, "activePage")
	}

	/* GESTIONE MENU ALLEGATO */

	@Command onMostraAllegati(@BindingParam("documento")def documento) {
		listaAllegati = documentoStepDTOService.caricaAllegatiDocumento (documento.idDocumento, documento.tipoOggetto);
		BindUtils.postNotifyChange(null, null, this, "listaAllegati")
	}

	@Command onDownloadFileAllegato (@BindingParam("fileAllegato") def value) {
		documentoStepDTOService.downloadFileAllegato(value)
	}
	
	@Command
	public void onEseguiOrdinamento(@BindingParam("campi") String campi, @ContextParam(ContextType.TRIGGER_EVENT) SortEvent event) {
		for (String campo : campi?.split(",")?.reverse()){
			orderMap.remove(campo)
			orderMap = [(campo):event?.isAscending() ? 'asc' : 'desc'] + orderMap
		}
		onCerca()
	}
	
	@Command
	public void onExportExcel() {
		if (totalSize > Impostazioni.ESPORTAZIONE_NUMERO_MASSIMO.valoreInt){
			Messagebox.show ("Attenzione: il numero dei documenti da esportare supera il massimo consentito.", 
							 "Esportazione interrotta.",
							 Messagebox.OK , Messagebox.EXCLAMATION, null
			);
			return;
		}
		try{
			def documenti = documentoStepDTOService.inCarico (testoCerca, tipoOggetto?.oggetti, null, [StatoFirma.DA_FIRMARE, StatoFirma.IN_FIRMA, StatoFirma.FIRMATO_DA_SBLOCCARE], pageSize, activePage, orderMap, true, true)
			def export = documenti.result.collect { [   idDocumento           :it.idDocumento
				, idPadre                :it.idPadre
				, stato                  :it.stato
				, statoFirma             :it.statoFirma
				, statoConservazione     :it.statoConservazione
				, statoOdg               :it.statoOdg
				, stepNome               :it.stepNome
				, stepDescrizione        :it.stepDescrizione
				, stepTitolo             :it.stepTitolo
				, tipoOggetto            :it.tipoOggetto
				, tipoRegistro           :it.tipoRegistro
				, riservato              :it.riservato
				, oggetto                :it.oggetto
				, unitaProponente        :it.unitaProponente
				, anno                   :it.anno
				, annoProposta           :it.annoProposta
				, numero                 :it.numero
				, numeroProposta         :it.numeroProposta
				, idTipologia            :it.idTipologia
				, titoloTipologia        :it.titoloTipologia
				, descrizioneTipologia   :it.descrizioneTipologia
				, dataAdozione			 :it.dataAdozione
				, statoVistiPareri		 :it.statoVistiPareri
				]}
			exportService.downloadExcel(documenti.exportOptions, export)
		}
		finally{
			caricaLista()
		}
	}
}
