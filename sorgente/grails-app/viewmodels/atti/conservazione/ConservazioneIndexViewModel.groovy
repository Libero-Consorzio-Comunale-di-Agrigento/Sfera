package atti.conservazione

import atti.ricerca.MascheraRicercaDocumento
import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.integrazioni.ConservazioneService
import org.apache.log4j.Logger
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.SortEvent
import org.zkoss.zul.Window

class ConservazioneIndexViewModel {

	private static final Logger log = Logger.getLogger(ConservazioneIndexViewModel.class)
	
	def tipiDocumento =  [(Determina.TIPO_OGGETTO)			:[popup: "/atti/popupRicercaDocumenti.zul", nome: Labels.getLabel("tipoOggetto.determine")	, labelCategoria: Labels.getLabel("label.categoria.determina")			, zul:"/atti/documenti/determina.zul", 			icona:"/images/agsde2/22x22/logo_determina_22.png"         ]
						, (Delibera.TIPO_OGGETTO)			:[popup: "/atti/popupRicercaDocumenti.zul", nome: Labels.getLabel("tipoOggetto.delibere")	, labelCategoria: Labels.getLabel("label.categoria.propostaDelibera")	, zul:"/atti/documenti/delibera.zul", 			icona:"/images/agsde2/22x22/logo_delibera_22.png" ]
						, (PropostaDelibera.TIPO_OGGETTO)	:[popup: "/atti/popupRicercaDocumenti.zul", nome: Labels.getLabel("tipoOggetto.delibere")	, labelCategoria: Labels.getLabel("label.categoria.propostaDelibera")	, zul:"/atti/documenti/propostaDelibera.zul", 	icona:"/images/agsde2/22x22/logo_proposta_delibera_22.png" ]]

	// services
	ConservazioneService conservazioneService
	SpringSecurityService springSecurityService
	AttiGestoreCompetenze gestoreCompetenze
	
	// componenti
	Window self
	Window popupRicercaAvanzata

	// ricerca
	private MascheraRicercaDocumento ricercaDetermine
	private MascheraRicercaDocumento ricercaDelibere
	
	// stato
	String tabSelezionato
	def documentiSelezionati

    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self 	= w
		onApriTab(Determina.TIPO_OGGETTO);
    }
	
	public void setRicerca (MascheraRicercaDocumento ricerca) {
		if (Determina.TIPO_OGGETTO == tabSelezionato) {
			ricercaDetermine = ricerca;
		} else {
			ricercaDelibere = ricerca;
		}
	}
	
	public MascheraRicercaDocumento getRicerca () {
		if (Determina.TIPO_OGGETTO == tabSelezionato) {
			return ricercaDetermine
		} else {
			return ricercaDelibere
		}
	}
	
	@NotifyChange(["ricerca", "documentiSelezionati"])
	@Command onApriTab (@BindingParam("tab") String tab) {
		tabSelezionato 			= tab;
		popupRicercaAvanzata 	= null;
		documentiSelezionati 	= [];
		
		if (Determina.TIPO_OGGETTO == tabSelezionato && ricercaDetermine == null) {
			ricercaDetermine = new MascheraRicercaDocumento(ricercaConservazione: true, anno:Calendar.getInstance().get(Calendar.YEAR), tipoDocumento:Determina.TIPO_OGGETTO, statoConservazione: "ALL")
			ricercaDetermine.caricaListe()
			ricercaDetermine.ricerca (springSecurityService.principal)
			
		} else if (PropostaDelibera.TIPO_OGGETTO == tabSelezionato && ricercaDelibere == null) {
    		ricercaDelibere  = new MascheraRicercaDocumento(ricercaConservazione: true, anno:Calendar.getInstance().get(Calendar.YEAR), tipoDocumento:PropostaDelibera.TIPO_OGGETTO, statoConservazione: "ALL")
    		ricercaDelibere.caricaListe()
			ricercaDelibere.ricerca (springSecurityService.principal)
		}
	}
	
	@NotifyChange(["ricerca", "documentiSelezionati"])
	@Command onRefresh() {
		documentiSelezionati = [];
		if (Determina.TIPO_OGGETTO == tabSelezionato) {
			ricercaDetermine.ricerca (springSecurityService.principal)
		} else {
			ricercaDelibere.ricerca (springSecurityService.principal)
		}
	}

	@NotifyChange(["ricerca", "documentiSelezionati"])
	@Command onPagina() {
		documentiSelezionati = [];
		if (Determina.TIPO_OGGETTO == tabSelezionato) {
			ricercaDetermine.pagina (springSecurityService.principal)
		} else {
			ricercaDelibere.pagina (springSecurityService.principal)
		}
	}

	@NotifyChange(["ricerca", "documentiSelezionati"])
	@Command onCerca() {
		documentiSelezionati = [];
		if (Determina.TIPO_OGGETTO == tabSelezionato) {
			ricercaDetermine.ricerca (springSecurityService.principal)
		} else {
			ricercaDelibere.ricerca (springSecurityService.principal)
		}
	}

	@Command onApriDocumento (@BindingParam("documento") def documento) {
		// il caso della proposta di delibera è particolare:
		// se ho cliccato su una riga di proposta di delibera, allora devo decidere se posso aprire la delibera oppure no.

		// posso aprire la delibera solo se:
		// 1) la delibera effettivamente esiste (la proposta potrebbe non essere ancora diventata delibera)
		// 2) la delibera esiste e l'utente corrente ha le competenze in lettura

		// in tutti gli altri casi, apro normalmente la popup.
		if (documento.tipoDocumentoPrincipale == Delibera.TIPO_OGGETTO 	&&
			documento.idDocumentoPrincipale   > 0 						&&
			gestoreCompetenze.getCompetenze(Delibera.get(documento.idDocumentoPrincipale)).lettura) {

			// apro la delibera
			apriDocumento("/atti/documenti/delibera.zul", [id: documento.idDocumentoPrincipale])
		} else {
			apriDocumento(tipiDocumento[documento.tipoDocumento].zul, [id: documento.idDocumento])
		}
	}

	private void apriDocumento (String zul, def parametri) {
		Window w = Executions.createComponents(zul, self, parametri)
		w.doModal()
	}
	
	/* GESTIONE POPUP DI RICERCA AVANZATA */
	@NotifyChange(["ricerca", "documentiSelezionati"])
	@Command apriPopupRicercaAvanzata () {
		if (popupRicercaAvanzata == null) {
			popupRicercaAvanzata = Executions.createComponents(tipiDocumento[tabSelezionato].popup, self, null);
		}
		popupRicercaAvanzata.doModal();
		documentiSelezionati = [];
	}
	
	@NotifyChange(["ricerca", "documentiSelezionati"])
	@Command onChiudiRicercaAvanzata() {
		popupRicercaAvanzata.setVisible(false)
		documentiSelezionati = [];
	}
	
	@NotifyChange(["ricerca", "documentiSelezionati"])
	@Command onCercaAvanzata() {
		onChiudiRicercaAvanzata()
		documentiSelezionati = [];
		if (Determina.TIPO_OGGETTO == tabSelezionato) {
			ricercaDetermine.ricerca (springSecurityService.principal)
		} else {
			ricercaDelibere.ricerca (springSecurityService.principal)
		}
	}
	
	@NotifyChange(["ricerca", "documentiSelezionati"])
	@Command onSvuotaFiltri () {
		if (Determina.TIPO_OGGETTO == tabSelezionato) {
			documentiSelezionati = [];
			ricercaDetermine = new MascheraRicercaDocumento(ricercaConservazione: true, tipoDocumento:Determina.TIPO_OGGETTO)
			ricercaDetermine.caricaListe()
			ricercaDetermine.ricerca()
		} else {
			documentiSelezionati = [];
			ricercaDelibere  = new MascheraRicercaDocumento(ricercaConservazione: true, tipoDocumento:PropostaDelibera.TIPO_OGGETTO)
			ricercaDelibere.caricaListe()
			ricercaDelibere.ricerca()
		}
	}

	@NotifyChange(["ricerca", "documentiSelezionati"])
	@Command onConserva() {
		conservazioneService.conservaDocumenti (documentiSelezionati)
		onRefresh();
	}
	
	@NotifyChange(["ricerca", "documentiSelezionati"])
	@Command onDaConservare() {
		conservazioneService.documentiDaConservare (documentiSelezionati)
		onRefresh()
	}

	@NotifyChange(["ricerca", "documentiSelezionati"])
	@Command onAggiornaStatiConservazioneDelibere() {
		conservazioneService.aggiornaStatiConservazioneDelibere()
		onRefresh()
	}


	@NotifyChange(["ricerca", "documentiSelezionati"])
	@Command onAggiornaStatiConservazioneDetermine() {
		conservazioneService.aggiornaStatiConservazioneDetermine()
		onRefresh()
	}

	@NotifyChange(["ricerca", "documentiSelezionati"])
	@Command
	void onEseguiOrdinamento(@BindingParam("campi") String campi, @ContextParam(ContextType.TRIGGER_EVENT) SortEvent event) {
		for (String campo : campi?.split(",")?.reverse()){
			ricerca.modificaColonnaOrdinamento(campo, event?.isAscending() ? 'asc' : 'desc')
		}
		onCerca()
	}
	
}
