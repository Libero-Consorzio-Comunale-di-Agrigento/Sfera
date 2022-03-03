package odg

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.StatoOdg
import it.finmatica.atti.documenti.competenze.PropostaDeliberaCompetenze
import it.finmatica.atti.dto.documenti.viste.DocumentoStepDTOService
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.export.ExportService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.odg.Commissione
import it.finmatica.atti.odg.OdgDTOService
import it.finmatica.atti.odg.dizionari.EsitoStandard
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.gestionetesti.TipoFile
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import org.hibernate.FetchMode
import org.hibernate.criterion.CriteriaSpecification
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.SortEvent
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.Filedownload
import org.zkoss.zul.Listbox
import org.zkoss.zul.Listitem
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window
import groovy.xml.StreamingMarkupBuilder

import static it.finmatica.zkutils.LabelUtils.getLabel as l

class DocumentiDaOdgViewModel {

	// services
	SpringSecurityService 		springSecurityService
	DocumentoStepDTOService 	documentoStepDTOService
	OdgDTOService				odgDTOService
	ExportService				exportService
	GestioneTestiService 		gestioneTestiService

	// componenti
	Window self
	@Wire("#listaDocumenti")
	Listbox listboxDocumenti

	// dati
	def lista
	def selected
	def listaAllegati
	def zul = [DETERMINA: '/atti/documenti/determina.zul', PROPOSTA_DELIBERA: '/atti/documenti/propostaDelibera.zul']

	// ricerca
	String testoCerca  = ""

	// paginazione
	int activePage  = 0
	int pageSize 	= 30
	int totalSize 	= 100

	def exportOptions =   [   idDocumento 			: [label: 'ID', 								index: -1, columnType: 'NUMBER']
							, tipoOggetto			: [label: 'Tipo Oggetto', 						index: -1, columnType: 'TEXT']
							, titoloTipologia		: [label: 'Tipologia', 							index:  0, columnType: 'TEXT']
							, numeroProposta		: [label:l("label.ricerca.numeroProposta"), 	index:  1, columnType: 'NUMBER']
							, annoProposta			: [label:l("label.ricerca.annoProposta"), 		index:  2, columnType: 'NUMBER']
							, stepTitolo			: [label: 'Step Titolo',						index:  5, columnType: 'TEXT']
							, oggetto				: [label: 'Oggetto', 							index:  3, columnType: 'TEXT']
							, descrizioneUo			: [label: 'Unita Proponente', 					index:  4, columnType: 'TEXT']
							, statoOdg				: [label: 'Stato Odg',							index: -1, columnType: 'TEXT']
							, inOdg		 			: [label: 'In OdG',								index:  6, columnType: 'BOOLEAN']
						    , sequenza	 			: [label: 'Sequenza In OdG',					index: -1, columnType: 'NUMBER']]

	// stato
	List statiDocumento 	= ["Istruttoria Completa", "In Istruttoria", "Disponibili per Odg", "Tutti i documenti"]
	String statoDocumento 	= ""
	List<CommissioneDTO> 	listaCommissione
	CommissioneDTO 			selectedCommissione
	GestioneTestiModelloDTO	modello
	GestioneTestiModelloDTO stampa

	def orderMap = [
			'deli_tipo.sequenza': 'asc',
			'deli.annoProposta':'asc',
			'deli.numeroProposta':'asc'
		]
	
    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self 	= w

		listaCommissione = Commissione.createCriteria().list() {
			eq ("valido",true)
			fetchMode("ruoloCompetenze", FetchMode.JOIN)
			order('titolo', 'asc')
			order('descrizione', 'asc')
		}.toDTO()

		listaCommissione.add(0, new CommissioneDTO(id:-1, titolo:"Tutte"))
		statoDocumento 		= statiDocumento[0]
		selectedCommissione = listaCommissione[0]

		modello = GestioneTestiModello.createCriteria().get(){
			eq("tipoModello.codice", "ODG_PROPOSTE_COMPLETE")
			eq("valido", true)
		}?.toDTO()

		stampa = GestioneTestiModello.createCriteria().get(){
			eq("tipoModello.codice", "ODG_PROPOSTE_COMPLETE_MODENA")
			eq("valido", true)
		}?.toDTO()

		caricaLista()
    }
	
	@AfterCompose
	public void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
		Selectors.wireComponents(view, this, false);
	}

	@Command onModifica() {
		def selectedItem = listboxDocumenti.selectedItems.first().value;
		creaPopup(zul[selectedItem.tipoOggetto], [id: selectedItem.idDocumento, idPadre: -1])
	}

	@Command onModificaSelected(@ContextParam(ContextType.COMPONENT) Listitem l) {
		
		if (l != null) {
			creaPopup(zul[l.value.tipoOggetto], [id: l.value.idDocumento, idPadre: -1])
			
		} else if (listboxDocumenti.selectedItems.size() > 0) {
			def selectedItem = listboxDocumenti.selectedItems[0].value;
			creaPopup(zul[selectedItem.tipoOggetto], [id: selectedItem.idDocumento, idPadre: -1])
		}
	}

	@Command onNuovaPropostaDelibera() {
		creaPopup(zul.PROPOSTA_DELIBERA, [id: -1, fuoriSacco: true])
	}

	private void creaPopup (String zul, def parametri) {
		Window w = Executions.createComponents(zul, self, parametri)
		w.doModal()
		w.onClose {
			caricaLista()
		}
	}

	@Command onRefresh() {
		caricaLista()
	}

	@Command onCerca() {
		activePage = 0
		caricaLista()
	}

	@Command onMandaODG (@BindingParam("lista") Listbox lista) {
		odgDTOService.mandaInOdg(lista.getSelectedItems()*.value)
		caricaLista()
	}

	@Command onTornaDaODG (@BindingParam("lista") Listbox lista) {
		odgDTOService.togliDaOdg(lista.getSelectedItems()*.value)
		caricaLista()
	}

	private void caricaLista(boolean tutti = false) {
		Integer searchNumbers = (testoCerca?.length()> 0 && testoCerca?.equals(testoCerca?.replaceAll("\\D+", "")))? new Integer(testoCerca?.replaceAll("\\D+", "")) : null
		List<String> listaRuoliCompetenze = springSecurityService.principal.uo().ruoli.flatten().codice.unique()
		List<String> statiOdg = [];

		// devo trovare tutte le proposte di delibera e determine che hanno lo stato ODG:

		if (statoDocumento == "Disponibili per Odg") {
			statiOdg = [StatoOdg.COMPLETO, StatoOdg.COMPLETO_IN_ISTRUTTORIA];
		}

		if (statoDocumento == "Istruttoria Completa") {
			statiOdg = [StatoOdg.DA_COMPLETARE,  StatoOdg.COMPLETO];
		}
		if (statoDocumento == "In Istruttoria") {
			statiOdg = [StatoOdg.IN_ISTRUTTORIA, StatoOdg.COMPLETO_IN_ISTRUTTORIA];
		}

		if (statoDocumento == "Tutti i documenti") {
			statiOdg = [StatoOdg.DA_COMPLETARE,  StatoOdg.COMPLETO, StatoOdg.IN_ISTRUTTORIA, StatoOdg.COMPLETO_IN_ISTRUTTORIA];
		}

		// devo escludere tutte le determine/proposte delibera che sono già in una seduta, devo includere solo quelle che hanno il ruolo scritto in commissione
		lista = PropostaDeliberaCompetenze.createCriteria().list () {
			createAlias ("propostaDelibera", 	"deli", 		CriteriaSpecification.INNER_JOIN)
			createAlias ("deli.commissione", 	"deli_comm", 	CriteriaSpecification.LEFT_JOIN)
			createAlias ("deli.soggetti", 		"deli_sogg", 	CriteriaSpecification.INNER_JOIN)
			createAlias ("deli.oggettoSeduta", 	"deli_ogg_sed",	CriteriaSpecification.LEFT_JOIN)
			createAlias ("deli_ogg_sed.esito", 	"deli_esito",	CriteriaSpecification.LEFT_JOIN)
			createAlias ("deli_sogg.unitaSo4",	"deliUoProp", 	CriteriaSpecification.INNER_JOIN)
			createAlias ("deli.tipologia",		"deli_tipo",  	CriteriaSpecification.INNER_JOIN)
			createAlias ("deli.iter",			"deli_iter",  	CriteriaSpecification.INNER_JOIN)
			createAlias ("deli_iter.stepCorrente",	"step",  	CriteriaSpecification.INNER_JOIN)
			createAlias ("step.cfgStep",			"cfgStep",  	CriteriaSpecification.INNER_JOIN)

			projections {
				groupProperty ("deli.id")               // 0
				groupProperty ("deli_tipo.id")          // 1
				groupProperty ("deli_tipo.titolo")      // 2
				groupProperty ("deli.numeroProposta")   // 3
				groupProperty ("deli.annoProposta")     // 4
				groupProperty ("cfgStep.titolo")        // 5
				groupProperty ("deli.oggetto")          // 6
				groupProperty ("deliUoProp.descrizione")// 7
				groupProperty ("deli.statoOdg")   		// 8
				groupProperty ("deli.dataScadenza") 	// 9
				groupProperty ("deli_tipo.sequenza") // 10
			}

			if (selectedCommissione.id != -1) {
				eq ("deli.commissione.id", selectedCommissione.id)
			}

			if (!(AttiUtils.isUtenteAmministratore())) {
				'in' ("deli_comm.ruoloCompetenze.ruolo", listaRuoliCompetenze)
			}

			'in' ("deli.statoOdg", statiOdg)
			eq   ("deli_sogg.tipoSoggetto.codice", TipoSoggetto.UO_PROPONENTE)

			isNull("deli_iter.dataFine")
            eq("deli.valido", true)

			if (searchNumbers != null) {
				eq ("deli.numeroProposta", searchNumbers)
			} else {
    			or {
    				ilike ("deli.oggetto", 		"%" + testoCerca + "%")
    				ilike ("deli_tipo.titolo", 	"%" + testoCerca + "%")
    			}
			}

			or {
				isNull ("deli.oggettoSeduta")
				and {
					eq ("deli_esito.esitoStandard.codice", EsitoStandard.INVIA_COMMISSIONE)
					eq ("deli_ogg_sed.confermaEsito", true)
				}
			}

			orderMap.each{ k, v -> order k, v }
			if (!tutti){
				firstResult (pageSize * activePage)
				maxResults  (pageSize)
			}
		}
		lista = lista.collect { row ->
			 [ idDocumento: 	row[0]
			 , tipoOggetto: 	PropostaDelibera.TIPO_OGGETTO
			 , titoloTipologia: row[2]
			 , numeroProposta: 	row[3]
			 , annoProposta: 	row[4]
			 , stepTitolo: 		row[5]
			 , oggetto: 		row[6]
			 , descrizioneUo: 	row[7]
			 , statoOdg:		row[8]
			 , inOdg:			StatoOdg.isInOdg(row[8])
			 , dataScadenza:	row[9]
			 , sequenza:		row[10]]
		}

		totalSize = PropostaDeliberaCompetenze.createCriteria().list () {
			createAlias ("propostaDelibera", 	"deli", 		CriteriaSpecification.INNER_JOIN)
			createAlias ("deli.commissione", 	"deli_comm", 	CriteriaSpecification.LEFT_JOIN)
			createAlias ("deli.soggetti", 		"deli_sogg", 	CriteriaSpecification.INNER_JOIN)
			createAlias ("deli.oggettoSeduta", 	"deli_ogg_sed",	CriteriaSpecification.LEFT_JOIN)
			createAlias ("deli_ogg_sed.esito", 	"deli_esito",	CriteriaSpecification.LEFT_JOIN)
			createAlias ("deli_sogg.unitaSo4",	"deliUoProp", 	CriteriaSpecification.INNER_JOIN)
			createAlias ("deli.tipologia",		"deli_tipo",  	CriteriaSpecification.INNER_JOIN)
			createAlias ("deli.iter",			"deli_iter",  	CriteriaSpecification.INNER_JOIN)
			createAlias ("deli_iter.stepCorrente",	"step",  	CriteriaSpecification.INNER_JOIN)
			createAlias ("step.cfgStep",		"cfgStep",  	CriteriaSpecification.INNER_JOIN)

			projections {
				groupProperty ("deli.id")               // 0
				groupProperty ("deli_tipo.id")          // 1
				groupProperty ("deli_tipo.titolo")      // 2
				groupProperty ("deli.numeroProposta")   // 3
				groupProperty ("deli.annoProposta")     // 4
				groupProperty ("cfgStep.titolo")        // 5
				groupProperty ("deli.oggetto")          // 6
				groupProperty ("deliUoProp.descrizione")// 7
				groupProperty ("deli.statoOdg")   		// 8
				groupProperty ("deli.dataScadenza") 	// 9
				groupProperty ("deli_tipo.sequenza") // 10
			}

			if (selectedCommissione.id != -1) {
				eq ("deli.commissione.id", selectedCommissione.id)
			}
			'in' ("deli.statoOdg", statiOdg)
			if (!(AttiUtils.isUtenteAmministratore())) {
				'in' ("deli_comm.ruoloCompetenze.ruolo", listaRuoliCompetenze)
			}
			eq   ("deli_sogg.tipoSoggetto.codice", TipoSoggetto.UO_PROPONENTE)

			isNull("deli_iter.dataFine")
			eq("deli.valido", true)

			or {
				ilike ("deli.oggetto", "%" + testoCerca + "%")
				ilike ("deli_tipo.titolo", "%" + testoCerca + "%")
			}
			if (searchNumbers != null) {
				eq ("deli.numeroProposta", searchNumbers)
				eq ("deli.annoProposta",   searchNumbers)
			}

			or {
				isNull ("deli.oggettoSeduta")
				and {
					eq ("deli_esito.esitoStandard.codice", EsitoStandard.INVIA_COMMISSIONE)
					eq ("deli_ogg_sed.confermaEsito", true)
				}
			}
		}.size()

		BindUtils.postNotifyChange(null, null, this, "lista")
		BindUtils.postNotifyChange(null, null, this, "totalSize")
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
			caricaLista(true)
			exportService.downloadExcel(exportOptions, lista)
		}
		finally{
			caricaLista()
		}
	}


	@Command
	public void onStampa() {
		if (modello == null) {
			throw new AttiRuntimeException ("Attenzione: non è possibile eseguire la stampa perchè non è presente il modello di testo ODG_PROPOSTE_COMPLETE");
		}

		try {
			caricaLista(true)
			String staticData = new StreamingMarkupBuilder().bind {
				documentRoot {
					for (def item: lista){
						proposte {
							OGGETTO_PROPOSTA(item.oggetto)
							TIPO_PROPOSTA(item.titoloTipologia)
							ESTREMI_PROPOSTA(item.annoProposta+"/"+item.numeroProposta)
							UNITA_PROPONENTE(item.descrizioneUo)
							STATO_PROPOSTA(item.statoOdg)
							IN_ODG(item.inOdg ? 'SI' : 'NO')
							DATA_SEDUTA_PREVISTA(item.dataScadenza?.format("dd/MM/yyyy"))
						}
					}
				}
			}.toString()

			InputStream testo = gestioneTestiService.stampaUnione (new ByteArrayInputStream(modello.domainObject.fileTemplate), staticData, Impostazioni.FORMATO_DEFAULT.valore)
			Filedownload.save(testo, TipoFile.getInstanceByEstensione(Impostazioni.FORMATO_DEFAULT.valore).contentType , "ProposteComplete."+Impostazioni.FORMATO_DEFAULT.valore)
		}
		finally {
			caricaLista()
		}


	}

	@Command
	public void onStampaModello() {
		if (stampa == null) {
			throw new AttiRuntimeException ("Attenzione: non è possibile eseguire la stampa perchè non è presente il modello di testo ODG_PROPOSTE_COMPLETE_MODENA");
		}

		InputStream is = gestioneTestiService.stampaUnione(stampa.domainObject, [:], Impostazioni.FORMATO_DEFAULT.valore, true)
		Filedownload.save(is, TipoFile.getInstanceByEstensione(Impostazioni.FORMATO_DEFAULT.valore).contentType , "ProposteComplete."+Impostazioni.FORMATO_DEFAULT.valore)
	}

}
