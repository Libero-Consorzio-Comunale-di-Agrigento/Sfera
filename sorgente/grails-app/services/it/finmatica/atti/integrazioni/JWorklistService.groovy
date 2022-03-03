package it.finmatica.atti.integrazioni

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.commons.StrutturaOrganizzativaService
import it.finmatica.atti.dizionari.Notifica
import it.finmatica.atti.dizionari.NotificaErrore
import it.finmatica.atti.documenti.DestinatarioNotificaAttivita
import it.finmatica.atti.documenti.NotificheErroreService
import it.finmatica.atti.documenti.SoggettoNotifica
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.jworklist.AbstractJWorklistDispatcher
import it.finmatica.atti.integrazioniws.ads.jworkflow.ExternalTaskServiceResponse
import it.finmatica.atti.integrazioniws.ads.jworkflow.MetadatoUtente
import it.finmatica.gestioneiter.IDocumentoIterabile
import org.springframework.transaction.annotation.Transactional

class JWorklistService extends AbstractJWorklistDispatcher {

	// service
	StrutturaOrganizzativaService 	strutturaOrganizzativaService
	SpringSecurityService 			springSecurityService

	it.finmatica.atti.integrazioniws.ads.jworkflow.JWorklistService jworklistServiceClient
    NotificheErroreService notificheErroreService

	/**
	 * Invia una notifica sulla jworklist.
	 *
	 * Questa funzione richiede una nuova transazione perché va a scrivere anche sulla DestinatariNotificheAttività
	 * in modo tale che tutta l'operazione sia "consistente": se scrivo sulla jworklist scrivo anche sulla DestinatariNotificheAttivita
	 *
	 * @param documento
	 * @param tipoNotifica
	 * @param oggetto
	 * @param testo
	 * @param utenti
	 * @param priorita
	 * @param stepCorrente
	 */
	@Override
	@Transactional
	void notifica (def documento, Notifica notifica, String oggetto, String testo, List<SoggettoNotifica> utenti, String priorita, String stepCorrente) {

		if (log.infoEnabled) {
			log.info ("Invio la notifica oggetto: $oggetto, testo: $testo agli utenti: ${utenti.utente?.nominativo}")
		}

		// invio le notifiche ai singoli utenti.
		for (SoggettoNotifica soggettoNotifica : utenti) {

			String idRiferimento = getIdRiferimento(documento, notifica.tipoNotifica)

			// se ha già una notifica per questo id riferimento, non invio quella nuova:
            try {
                if (esisteNotificaJWorklist(idRiferimento, soggettoNotifica.utente) && !notificheErroreService.esisteErroreNotifica(idRiferimento)){
                    continue
                }
            } catch (Exception ex){
                log.error("Si è verificato un errore nella verifica della notifica con idRiferimento:$idRiferimento, utente:${soggettoNotifica.utente}. Procedo comunque con la creazione della notifica.", ex)
            }

			List<String> idAttivitaCreate
			   
			// Ogni tanto può succedere (ad es. a Belluno) che nel job notturno la notifica via jworklist non funzioni per qualche ragione (ad es. Read Timeout).
			// Siccome la notifica viene inviata nella funzione "afterCommit", dopo cioè la commit su agsde2 ma prima di quella su gdm, 
			// per evitare problemi di sincronizzazione, faccio il catch di eventuali errori di invio della notifica e faccio un secondo tentativo di invio. Se anche questo non funzionasse, peccato:
			// meglio non avere la notifica in jworklist ma avere la situazione stabile e sincronizzata su db tra agsde2 e gdm.
    		try {
				idAttivitaCreate = creaNotificaJWorklist(documento, idRiferimento, soggettoNotifica.utente, oggetto, testo, priorita, stepCorrente, notifica.tipoNotifica)
			} catch (Exception e) {
				log.warn ("Si è verificato un errore nell'invio della notifica con idRiferimento:$idRiferimento, utente:${soggettoNotifica.utente}. Procedo con un secondo tentativo.", e)
			
				try {
					idAttivitaCreate = creaNotificaJWorklist(documento, idRiferimento, soggettoNotifica.utente, oggetto, testo, priorita, stepCorrente, notifica.tipoNotifica)
				} catch (Exception e1) {
					log.error ("Si è verificato un secondo errore nell'invio della notifica con idRiferimento:$idRiferimento, utente:${soggettoNotifica.utente}. Non invio la notifica.", e)
					idAttivitaCreate = null
				}
			}

            if (idAttivitaCreate == null) {
                notificheErroreService.creaErroreNotifica(NotificaErrore.OPERAZIONE_INVIO, notifica, idRiferimento, documento.iter.stepCorrente)
            }

			if (idAttivitaCreate != null && idAttivitaCreate.size() > 0) {
				soggettoNotifica.idAttivita = idAttivitaCreate[0]
				aggiungiNotificaJWorklist(idRiferimento, soggettoNotifica, documento, notifica)
			}
		}

		if (log.infoEnabled) {
			log.info ("Notifiche (${utenti.idAttivita}) inviate agli utenti: ${utenti.utente?.nominativo}")
		}
	}

	@Override
	@Transactional
	void eliminaNotifica (long idNotificaDestinatarioAttivita) {
        log.debug("Eliminazione della notifica=" + idNotificaDestinatarioAttivita)
        DestinatarioNotificaAttivita notifica = DestinatarioNotificaAttivita.get(idNotificaDestinatarioAttivita)
        try {
            ExternalTaskServiceResponse resp = jworklistServiceClient.deleteExternalTask(null, notifica.idAttivita, null, null)

            String result = resp.getResult()
            if (!result.equals("0")) {
                log.warn("Problemi durante l'eliminazione della notifica con id_attivita=" + notifica.idAttivita + "\n Errore: " + resp.getErrStr())
                def documento = getDocumento(notifica.idRiferimento)
                notificheErroreService.creaErroreNotifica(NotificaErrore.OPERAZIONE_ELIMINA, null, notifica.idRiferimento, documento.iter.stepCorrente)
            } else {
                notifica.delete()
            }
        } catch (Exception ex) {
            log.error("Problemi durante l'eliminazione della notifica ${notifica.idAttivita}", ex)
            def documento = getDocumento(notifica.idRiferimento)
            notificheErroreService.creaErroreNotifica(NotificaErrore.OPERAZIONE_ELIMINA, null, notifica.idRiferimento, documento.iter.stepCorrente)
        }
    }

	private List<String> creaNotificaJWorklist (IDocumentoIterabile documentoIterabile, String idRiferimento, Ad4Utente utente, String oggetto, String testoAttivita, String priorita, String stepCorrente, String tipoNotifica) {
		String utenteApplicativo 		= springSecurityService.currentUser.id
		boolean hasRuoloFirma			= (strutturaOrganizzativaService.utenteHasRuoloDaImpostazioni(utente.id, Impostazioni.OTTICA_SO4.toString(), Impostazioni.RUOLO_SO4_FIRMA.toString()))
		String urlRiferimento 			= (hasRuoloFirma?JWorklistConfig.getUrlJWorklist()+"/standalone.zul?operazione=DA_FIRMARE":"")
		String urlRiferimentoDescrizione= (hasRuoloFirma?"Documenti da firmare":"")

		String urlEsecuzione 			= getUrlDocumento(documentoIterabile)
		String tooltipUrlEsecuzione 	= "Visualizza il Documento"
        MetadatoUtente utenteEsterno	= creaUtenteEsterno(utente.id)
		Date dataAttivazione			= new Date()
		Date dataArrivo 				= new Date()
		String livelloPriorita 			= priorita
		String note 					= getNote(documentoIterabile, tipoNotifica)
		String paramInitIter 			= getParamInitIterString(documentoIterabile)
		String descIter         		= stepCorrente
		Date scadenza					= getScadenza(documentoIterabile, tipoNotifica)

		if (log.debugEnabled) {
			log.debug("Invio la notifica con idRiferimento=$idRiferimento all'utente: ${utente.id}");
		}

        try {
            ExternalTaskServiceResponse resp = jworklistServiceClient.createExternalTask(utenteApplicativo
                , idRiferimento
                , oggetto			// testo del record sulla maschera della jworklist
                , testoAttivita 	// tooltip sul record nella maschera della jworklist
                , urlRiferimento
                , urlRiferimentoDescrizione
                , urlEsecuzione
                , tooltipUrlEsecuzione
                , scadenza ? scadenza.format("dd/MM/yyyy HH:mm:ss"): null
                , paramInitIter
                , "" // nome iter
                , descIter // descrizione iter
                , ""  // colore
                , ""  // ordinamento
                , dataAttivazione.format("dd/MM/yyyy HH:mm:ss")
                , utenteEsterno
                , "" // categoria
                , "" // desktop
                , ""
                , TIPOLOGIA_ATTIVITA
                , "", "", "", "", ""
                , dataArrivo.format("dd/MM/yyyy HH:mm:ss")
                , livelloPriorita
                , note)
            // se c'è stato un problema nella comunicazione con il webservice, lo scrivo nei log.
            if (!resp.getResult().equals("0")) {
                log.warn("Problemi durante la creazione della notifica per il Documento n."+idRiferimento+" \n Errore: "+resp.getErrStr())
                return null
            }

            if (log.debugEnabled) {
                log.debug ("Notifica (${resp.getAttivita()}) con idRiferimento: $idRiferimento inviata all'utente: ${utente.id}")
            }

            return resp.getAttivita()
        } catch (Exception ex){
            log.error("Problemi durante la creazione della notifica per Documento n." + idRiferimento + " ed utente " + utenteEsterno + " non inserito. ", ex)
            return null
        }
	}

	private MetadatoUtente creaUtenteEsterno (String utenteAD4) {
		MetadatoUtente ue = new MetadatoUtente()
		ue.setUtenteAD4(utenteAD4)
		return ue
	}
}
