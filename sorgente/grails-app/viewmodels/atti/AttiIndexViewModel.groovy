package atti

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.commons.StrutturaOrganizzativaService
import it.finmatica.atti.impostazioni.Impostazioni
import org.zkoss.bind.BindContext
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zul.Tab
import org.zkoss.zul.Tabbox
import org.zkoss.zul.Tabpanel
import org.zkoss.zul.Window

import static it.finmatica.zkutils.LabelUtils.getLabel as l

class AttiIndexViewModel {

	// services
	SpringSecurityService 			springSecurityService
	StrutturaOrganizzativaService 	strutturaOrganizzativaService

	// componenti
	Window self
	def listaTab

	String selezionato

	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("codiceTab") String codiceTab) {
		this.self = w
		boolean notificheOrganoControlloVisible  = springSecurityService.principal.hasRuolo (Impostazioni.RUOLO_SO4_ORGANI_CONTROLLO.valore);
		boolean controlloRegolaritaVisibile 	 = springSecurityService.principal.hasRuolo (Impostazioni.RUOLO_SO4_CONTROLLO_REGOLARITA.valore);
		boolean daMarcareVisible 	 			 = springSecurityService.principal.hasRuolo (Impostazioni.RUOLO_SO4_MARCATURA.valore);

		// il tab da firmare è visibile solo se l'utente corrente ha uno dei ruoli di firma:
		boolean daFirmareVisible = [
			  Impostazioni.RUOLO_SO4_FIRMA.valore
			, Impostazioni.RUOLO_SO4_FIRMATARIO_CERT_ESEC.valore
			, Impostazioni.RUOLO_SO4_FIRMATARIO_DECRETI.valore
			, Impostazioni.RUOLO_SO4_FIRMATARIO_CERT_PUBB.valore
		].find {
			springSecurityService.principal.hasRuolo(it);
		} != null

		listaTab = [
			  [codice:"miei_documenti",			nome: "I Miei Documenti"				, zul: "/atti/documentiMiei.zul", 				visibile:true]
			, [codice:"ricerca",				nome: "Ricerca"			 				, zul: "/atti/ricercaDocumenti.zul", 			visibile:true]
			, [codice:"da_firmare",				nome: "Da Firmare"		 				, zul: "/atti/documentiDaFirmare.zul", 			visibile:daFirmareVisible]
			, [codice:"da_marcare",				nome: l("label.tab.marcaturaTemporale") , zul: "/atti/documentiDaMarcare.zul", 			visibile:daMarcareVisible]
			, [codice:"organi_di_controllo",	nome: "Notifiche organi di controllo" 	, zul: "/atti/notificheOrganiDiControllo.zul", 	visibile:notificheOrganoControlloVisible]
			, [codice:"controllo_di_regolarita",nome: "Controllo di Regolarità"		 	, zul: "/atti/controlloRegolarita.zul", 		visibile:controlloRegolaritaVisibile]		
		]
		selezionato = (codiceTab)?codiceTab:"miei_documenti"
	}

	@Command caricaTab(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx, @BindingParam("zul") String zul) {
		Tab tab				= (Tab) ctx.getComponent()
		Tabpanel tabPanel	= tab.linkedPanel
		if (tabPanel != null && (tabPanel.children == null || tabPanel.children.empty)) {
			Executions.createComponents(zul, tabPanel, null);
		}
	}

	@Command caricaPrimoTab(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		Tabbox tabbox 		= (Tabbox) ctx.getComponent()
		Tabpanel tabPanel 	= tabbox.getSelectedTab()?.linkedPanel
		if (tabPanel != null && (tabPanel.children == null || tabPanel.children.empty)) {
			Executions.createComponents(listaTab.find { it.codice == selezionato }.zul, tabPanel, null);
		}
	}
}
