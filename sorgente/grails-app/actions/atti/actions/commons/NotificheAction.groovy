package atti.actions.commons

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.AbstractViewModel
import it.finmatica.atti.dizionari.Notifica
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.documenti.*
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.integrazioni.pec.IntegrazionePecDucd
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione
import org.zkoss.zk.ui.Executions
import org.zkoss.zul.Window

class NotificheAction {

	// services
	SpringSecurityService 	springSecurityService
	IntegrazionePecDucd		integrazionePecDucd
	NotificheService 		notificheService

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Visionata Notifica",
		descrizione	= "Elimina le notifiche inviate per singolo utente o unità")
	IDocumentoIterabile presaVisione (def d) {
		notificheService.eliminaNotifica(d, springSecurityService.currentUser)
		return d
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "L'utente ha una notifica sulla jworklist?",
		descrizione	= "Ritorna TRUE se l'utente corrente ha una notifica sulla jworklist non legata al cambio step.")
	boolean isNotificaPresente (def d) {
		return notificheService.isNotificaPresente(d, springSecurityService.currentUser)
	}

	/*
	 * Azioni di notifica
	 */

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Notifica Atto da firmare",
		descrizione	= "Invio email TIPO_NOTIFICA_DA_FIRMARE agli attori dello step corrente del flusso.")
	IDocumentoIterabile notificaFirmatario (def d) {
		notificheService.notifica (TipoNotifica.DA_FIRMARE, d)
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO],
		nome		= "Notifica Generica 1",
		descrizione	= "Invia la notifica generica 1")
	IDocumentoIterabile notificaGenerica1 (def d) {
		notificheService.notifica (TipoNotifica.GENERICA_1, d)
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO],
		nome		= "Notifica Generica 2",
		descrizione	= "Invia la notifica generica 2")
	IDocumentoIterabile notificaGenerica2 (def d) {
		notificheService.notifica (TipoNotifica.GENERICA_2, d)
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO],
		nome		= "Notifica Generica 3",
		descrizione	= "Invia la notifica generica 3")
	IDocumentoIterabile notificaGenerica3 (def d) {
		notificheService.notifica (TipoNotifica.GENERICA_3, d)
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO],
		nome		= "Notifica Generica 4",
		descrizione	= "Invia la notifica generica 4")
	IDocumentoIterabile notificaGenerica4 (def d) {
		notificheService.notifica (TipoNotifica.GENERICA_4, d)
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO],
		nome		= "Notifica Generica 5",
		descrizione	= "Invia la notifica generica 5")
	IDocumentoIterabile notificaGenerica5 (def d) {
		notificheService.notifica (TipoNotifica.GENERICA_5, d)
		return d
	}

	@Action(tipo	= TipoAzione.CLIENT,
		tipiOggetto	= [SedutaStampa.TIPO_OGGETTO],
		nome		= "Invia la stampa di convocazione",
		descrizione	= "Invia la stampa di convocazione")
	SedutaStampa notificaConvocazione (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
        SedutaStampa sedutaStampa = viewModel.getDocumentoIterabile(false)
        Notifica notifica = Notifica.perTipo(TipoNotifica.CONVOCAZIONE_SEDUTA, sedutaStampa.TIPO_OGGETTO).get()

        if (notifica == null) {
            throw new AttiRuntimeException("La notifica di tipo '${TipoNotifica.getTitolo(TipoNotifica.CONVOCAZIONE_SEDUTA)}' non è configurata nel dizionario delle notifiche per il tipo di documento: ${sedutaStampa.TIPO_OGGETTO}.")
        }

        Window w = Executions.createComponents("/odg/popupNotificheMail.zul", null, [seduta: sedutaStampa.seduta.toDTO(), sedutaStampa: sedutaStampa.toDTO(['destinatariNotifiche', 'destinatariNotifiche.email', 'destinatariNotifiche.utente', 'destinatariNotifiche.email.indirizzoEmail']), consentiAggiuntaNuoveEmail:false, notifica:notifica.toDTO()])
        w.onClose {
            viewModel.eseguiPulsante(idCfgPulsante, idAzioneClient)
        }
        w.doModal()
    }


	@Action(tipo	= TipoAzione.CLIENT,
			tipiOggetto	= [SedutaStampa.TIPO_OGGETTO],
			nome		= "Invia la stampa di verbale",
			descrizione	= "Invia la stampa di verbale")
	SedutaStampa notificaVerbale (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
		SedutaStampa sedutaStampa = viewModel.getDocumentoIterabile(false)
		Notifica notifica = Notifica.perTipo(TipoNotifica.VERBALE_SEDUTA, sedutaStampa.TIPO_OGGETTO).get()

		if (notifica == null) {
			throw new AttiRuntimeException("La notifica di tipo '${TipoNotifica.getTitolo(TipoNotifica.VERBALE_SEDUTA)}' non è configurata nel dizionario delle notifiche per il tipo di documento: ${sedutaStampa.TIPO_OGGETTO}.")
		}

		Window w = Executions.createComponents("/odg/popupNotificheMail.zul", null, [seduta: sedutaStampa.seduta.toDTO(), sedutaStampa: sedutaStampa.toDTO(['destinatariNotifiche', 'destinatariNotifiche.email', 'destinatariNotifiche.utente', 'destinatariNotifiche.email.indirizzoEmail']), consentiAggiuntaNuoveEmail:false, notifica:notifica.toDTO()])
		w.onClose {
			viewModel.eseguiPulsante(idCfgPulsante, idAzioneClient)
		}
		w.doModal()
	}

	@Action(tipo	= TipoAzione.CLIENT,
		tipiOggetto	= [SedutaStampa.TIPO_OGGETTO],
		nome		= "Apre le ricevute PEC",
		descrizione	= "Apre le ricevute PEC")
	SedutaStampa apriRicevutePEC (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
        SedutaStampa sedutaStampa = viewModel.getDocumentoIterabile(false)
        String urlRicevuta = integrazionePecDucd.getUrlRicevuta(sedutaStampa)
        Executions.getCurrent().sendRedirect(urlRicevuta, "_blank")
    }

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto	= [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Certificato.TIPO_OGGETTO],
			nome		= "Aggiorna le notifiche",
			descrizione	= "Rigenera le notifiche di tipo JWORKLIST presenti (da utilizzare solo dietro un pulsante).")
	IDocumentoIterabile aggiornaNotifiche(def d) {
		notificheService.aggiorna (d)
		return d
	}
}

