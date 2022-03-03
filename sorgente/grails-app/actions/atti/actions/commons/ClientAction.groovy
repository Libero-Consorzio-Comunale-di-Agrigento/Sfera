package atti.actions.commons

import atti.documenti.DeterminaViewModel
import atti.documenti.PropostaDeliberaViewModel
import it.finmatica.atti.AbstractViewModel
import it.finmatica.atti.documenti.*
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.AttiFirmaService
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.IGestoreCompetenze
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.zkutils.SuccessHandler
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

/**
 * Contiene le azioni client comuni a tutti i documenti
 */
class ClientAction {

	GrailsApplication 	grailsApplication
	IGestoreCompetenze  gestoreCompetenze
	AttiFirmaService 	attiFirmaService
	WkfIterService 	 	wkfIterService
	SuccessHandler 	 	successHandler
	AllegatoService		allegatoService

	@Action(tipo	= TipoAzione.CLIENT,
		tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
		nome		= "Mostra popup di conferma in caso non ci siano allegati",
		descrizione	= "")
	void controllaAllegatiPresenti (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
		IDocumento doc = viewModel.getDocumentoIterabile(false);

		// conto quanti allegati ci sono
		if (doc.allegati?.count { it.valido } > 0) {
			viewModel.eseguiPulsante (idCfgPulsante, idAzioneClient);
			return;
		}

		Messagebox.show ("Attenzione: non sono presenti allegati sul documento. Si è sicuri di voler proseguire?", "Attenzione: non ci sono allegati",
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				void onEvent (Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						viewModel.eseguiPulsante (idCfgPulsante, idAzioneClient);
					}
				}
			}
		)
	}

	@Action(tipo	= TipoAzione.CLIENT,
		tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
		nome		= "Apre l'url configurato",
		descrizione	= "Apre l'url configurato nel parametro urlDaAprireInPopup nel Config.groovy")
	void apriUrlPopup (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
		String url = grailsApplication.config.urlDaAprireInPopup?:null

		if (url == null) {
			throw new AttiRuntimeException ("Nessun url configurato per essere aperto.");
		}

		// salto l'invalidate della maschera:
		successHandler.saltaInvalidate();

		// apro l'url
		Executions.getCurrent().sendRedirect(url, "_blank");
	}

	@Action(tipo	= TipoAzione.CLIENT,
			tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
			nome		= "Apre la pagina GDM per l'inserimento dei dati aggiuntivi",
			descrizione	= "Apre la pagina GDM per l'inserimento dei dati aggiuntivi")
	void apriDatiAggiuntiviGDM (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
        //esempio di chiamata: http://svi-affge/appsjsuite/datiaggiuntivi/jsp/datiAggiuntivi.jsp?appl=SFERA&tipoDoc=DETE&chiave=ID_DETERMINA&accessoDoc=W&id=100
        //appl = SFERA (nome dell'applicativo chiamante)
        //tipoDoc = codice presente in tipologia (usato come chiave)
		//accessoDoc = indica se il modello gdm deve essere aperto in lettura o scrittura (valori possibili R o W)
        //id = id del documento di SFERA

        IDocumento doc = viewModel.getDocumentoIterabile(false)

        String codiceEsterno = doc.tipologiaDocumento.codiceEsterno
        String id = doc.id
		if (doc instanceof Delibera)
			id = doc.propostaDelibera.id

		//verifico se l'utente ha competenze di modifica sul documento
		def competenze = gestoreCompetenze.getCompetenze(doc);
        String accessoDoc = 'R';
        if (competenze.modifica)
            accessoDoc = 'W'

        String url = Impostazioni.URL_SERVER_GDM.valore+"/appsjsuite/datiaggiuntivi/jsp/datiAggiuntivi.jsp?appl=SFERA&tipoDoc="+codiceEsterno+"&accessoDoc="+accessoDoc+"&id="+id

		// salto l'invalidate della maschera:
		successHandler.saltaInvalidate()

		// apro l'url
		Executions.getCurrent().sendRedirect(url, "_blank")
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto = [Determina.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
		nome		= "Nasconde i messaggi di avvenuto salvataggio.",
		descrizione	= "Nasconde i messaggi di avvenuto salvataggio.")
	def nascondiMessaggi (def documento) {
		successHandler.nascondiMessaggi();
		return documento;
	}

	@Action(tipo	= TipoAzione.CLIENT,
		tipiOggetto = [Determina.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, SedutaStampa.TIPO_OGGETTO],
		nome		= "Chiude la popup",
		descrizione	= "Chiude la maschera")
	void chiudiPopup (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
		// chiudo la maschera (si basa sul fatto che viewModel.self contenga l'oggetto Window da chiudere)
		Events.postEvent(Events.ON_CLOSE, viewModel.self, null)

		// issue 52209 per evitare errore di version
		viewModel.documentoDTO.version = viewModel.documentoDTO.getDomainObject().version

		// dice all'iter di proseguire l'esecuzione (ad es, andare al prossimo step)
		viewModel.eseguiPulsante(idCfgPulsante, idAzioneClient)
	}

	@Action(tipo	= TipoAzione.CLIENT,
		tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Apre popup cambio tipologia",
		descrizione	= "Apre la popup che permette il cambio di tipologia")
	void apriPopupCambioTipologia (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {

		// può avere i valori o determina o propostaDelibera
		String documento
		if (viewModel instanceof DeterminaViewModel) {
			documento = "determina"
		} else if(viewModel instanceof PropostaDeliberaViewModel) {
			documento = "propostaDelibera"
		}

		Window w = Executions.createComponents ("/commons/popupCambiaTipologia.zul", viewModel.self, [documento: documento])
		w.onClose() { event ->
			if (event.data != null) {
				viewModel.onCambiaTipologia (event.data)
			}
		}
		w.doModal()
	}

	@Action(tipo	= TipoAzione.CLIENT,
			tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
			nome		= "Aggiungi le note di trasmissione all'atto",
			descrizione	= "Aggiungi le note di trasmissione all'atto")
	void aggiungiNotaTrasmissione (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
		Window w = Executions.createComponents ("/commons/popupNote.zul", viewModel.self, [note: viewModel.documentoDTO.noteTrasmissione, title: "Aggiungi Note di Trasmissione", label: "Note di Trasmissione"])
		w.onClose() { event ->
			if (event.data != null) {
				viewModel.documentoDTO.noteTrasmissione = event.data.note
				viewModel.eseguiPulsante(idCfgPulsante, idAzioneClient)
			}
		}
		w.doModal()
	}

	@Action(tipo	= TipoAzione.CLIENT,
			tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO],
			nome		= "Mostra popup di conferma in caso ci siano allegati non convertiti in pdf",
			descrizione	= "")
	void controllaAllegatiNonPdfPresenti (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
		IDocumento doc = viewModel.getDocumentoIterabile(false);
		viewModel.documentoDTO.version = doc.version

		// controlliamo che non esistano allegati in formato diverso da pdf
		if (allegatoService.esistonoAllegatiNonPdf(doc)) {
			Messagebox.show ("Attenzione: sono presenti allegati sul documento non convertiti in Pdf. Si è sicuri di voler proseguire?", "Attenzione: ci sono allegati non in formato Pdf",
				Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					void onEvent (Event e) {
						if (Messagebox.ON_OK.equals(e.getName())) {
							viewModel.eseguiPulsante (idCfgPulsante, idAzioneClient);
						}
					}
				}
			)
		}
		else {
			viewModel.eseguiPulsante (idCfgPulsante, idAzioneClient);
		}
	}

	@Action(tipo	= TipoAzione.CLIENT,
			tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO],
			nome		= "Verifica l'impronta del testo principale e degli allegati",
			descrizione	= "Verifica l'impronta del testo principale e degli allegati")
	void apriVerficaImpronta (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
		IDocumento doc = viewModel.getDocumentoIterabile(false);
		Window w = Executions.createComponents ("/commons/popupVerificaImpronta.zul", viewModel.self, [documento: doc])
		w.doModal()
	}

}
