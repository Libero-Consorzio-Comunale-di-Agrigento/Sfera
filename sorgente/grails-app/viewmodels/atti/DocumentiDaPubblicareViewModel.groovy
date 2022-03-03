package atti

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.dto.documenti.viste.DocumentoStepDTOService
import it.finmatica.atti.export.ExportService
import it.finmatica.atti.impostazioni.Impostazioni
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.Init
import org.zkoss.zk.ui.Executions
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class DocumentiDaPubblicareViewModel {

	// services
	DocumentoStepDTOService documentoStepDTOService
	SpringSecurityService   springSecurityService
	ExportService           exportService

	// componenti
	Window self

	// dati
	def lista
	def selected

	def tipoOggetto
	def tipiOggetto 	= [
							[codice: null					, nome: "Visualizza tutti"	]
						  ,	[codice: 'DETERMINA'			, nome: "Proposte di Determina e Determine"	, zul: '/atti/documenti/determina.zul']
						  , [codice: 'VISTO'				, nome: "Visti associati alle Determine"	, zul: '/atti/documenti/visto.zul']
						  , [codice: 'CERTIFICATO'	, nome: "Certificati"						, zul: '/atti/documenti/certPubblicazione.zul']
						  ]

	// ricerca
	String testoCerca  = ""

	// paginazione
	int activePage  = 0
	int pageSize 	= 30
	int totalSize 	= 100

    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self 	= w
		tipoOggetto = tipiOggetto.find { it.codice == null }
		caricaLista()
    }

	@Command onRefresh() {
		caricaLista()
	}

	@Command onCerca() {
		activePage = 0
		caricaLista()
	}

	@Command onNuovo() {
		creaPopup("/atti/documenti/determina.zul", [id: -1])
	}

	@Command onModifica() {
		creaPopup(tipiOggetto.find { it.codice == selected.tipoOggetto }.zul, [id: selected.idDocumento, idPadre: selected.idPadre])
	}

	@Command onElimina() {
	}

	private void caricaLista() {
		def documenti = documentoStepDTOService.inCarico(testoCerca, tipoOggetto?.codice, null, null, pageSize, activePage, null, false)
		lista	  = documenti.result
		totalSize = documenti.total

		BindUtils.postNotifyChange(null, null, this, "lista")
		BindUtils.postNotifyChange(null, null, this, "totalSize")
		BindUtils.postNotifyChange(null, null, this, "activePage")
	}

	private void creaPopup(String zul, def parametri) {
		Window w = Executions.createComponents(zul, self, parametri)
		w.doModal()
		w.onClose {
			caricaLista()
		}
	}
	
	@Command
	public void onExportExcel() {
		if (totalSize > Impostazioni.ESPORTAZIONE_NUMERO_MASSIMO.valoreInt){
			Messagebox.show ("Attenzione: il numero dei documenti da esportare supera il massimo consentito.", 
							 "Esportazione interrotta.", Messagebox.OK , Messagebox.EXCLAMATION, null)
			return
		}

		try {
			def documenti = documentoStepDTOService.inCarico(testoCerca, tipoOggetto?.codice, null, null, pageSize, activePage, null, false, true)
			def export = documenti.result.collect { [   
				  idDocumento            :it.idDocumento
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
		} finally {
			caricaLista()
		}
	}
}
