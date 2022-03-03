package it.finmatica.atti.documenti.beans

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.dizionari.Notifica
import it.finmatica.atti.documenti.SoggettoNotifica
import it.finmatica.atti.mail.Allegato
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.apache.log4j.Logger
import org.springframework.transaction.support.TransactionSynchronizationAdapter
import org.springframework.transaction.support.TransactionSynchronizationManager

class NotificheDispatcher extends TransactionSynchronizationAdapter {

	private static final Logger log = Logger.getLogger(NotificheDispatcher.class)

	NotificheAsyncDispatcher 	notificheAsyncDispatcher
	SpringSecurityService 	 	springSecurityService
	NotificheAsyncService 		notificheAsyncService

	private def codaOperazioni
	private boolean async

	NotificheDispatcher() {
		codaOperazioni = []
		async = true
	}

	def getCodaOperazioni () {
		return this.codaOperazioni
	}

	boolean setAsync (boolean async) {
		this.async = async
	}
	
	void eliminaNotifiche (String idRiferimento) {
		register()
		codaOperazioni << [operazione: 'elimina', idRiferimento:idRiferimento]
	}

	void eliminaNotificheUtente (String idRiferimento, Ad4Utente utente) {
		register()
		codaOperazioni << [operazione: 'elimina', idRiferimento:idRiferimento, utente:utente]
	}

	void eliminaNotificheUnita (String idRiferimento, So4UnitaPubb unita) {
		register()
		codaOperazioni << [operazione: 'elimina', idRiferimento:idRiferimento, unita:unita]
	}

	void notificaAfterCommit (long idNotificaEmail, def documento) {
		register()
		codaOperazioni << [operazione: 'afterCommit', idNotificaEmail:idNotificaEmail, documentoClass: documento.class, documentoId: documento.id]
	}

	void notificaEMail (String tagMail, String mailSender, List<String> destinatariEmail, String testoNotifica, String oggettoNotifica, List<Allegato> allegatiEmail) {
		register()
		codaOperazioni << [operazione: 'email', tagMail: tagMail, mailSender: mailSender, oggettoNotifica: oggettoNotifica, testoNotifica: testoNotifica, destinatariEmail: destinatariEmail, allegatiEmail: allegatiEmail]
	}

	void notificaPec (def documento, String tipoNotifica, List<SoggettoNotifica> destinatariEmail, String testoNotifica, String oggettoNotifica, boolean async) {
        def operazione = [operazione: 'pec', documentoClass: documento.class, documentoId: documento.id, tipoNotifica: tipoNotifica, oggettoNotifica: oggettoNotifica, testoNotifica: testoNotifica, destinatariEmail: destinatariEmail]
        if (async == false) {
            notificheAsyncService.inviaNotifiche ([operazione])
        } else {
            register()
            codaOperazioni << operazione
        }
	}

	void notificaJWorklist (def documento, Notifica notifica, String oggetto, String testo, List<SoggettoNotifica> utenti, String priorita, String stepCorrente) {
		register()
		codaOperazioni << [operazione: 'jworklist', documentoClass: documento.class, documentoId: documento.id, tipoNotifica: notifica.tipoNotifica, idNotifica: notifica.id, oggetto: oggetto, testo: testo, utenti: utenti, priorita: priorita, stepCorrente: stepCorrente]
	}

	@Override
	void afterCommit () {
        if (codaOperazioni.size() == 0) {
            return
        }

		try {
			// Svuoto la coda operazioni, in questo modo successive chiamate/commit nella stessa "request" (come la fase di fine firma) non invieranno notifiche già gestite.
			// Inoltre, torna utile per gestire le notifiche che devono attendere la fine del flusso, come la notifica agli attori dello step di destinazione.
            List codaOperazioni = this.codaOperazioni
			this.codaOperazioni = []

			if (async) {
				notificheAsyncDispatcher.inviaNotificheAsync (springSecurityService.currentUser.nominativo, springSecurityService.principal.amm().codice, springSecurityService.principal.ottica().codice, codaOperazioni)
			} else {
				notificheAsyncService.inviaNotifiche (codaOperazioni)
			}
		} catch (Throwable t) {
			// in caso di errore non posso propagare l'eccezione perché lascerebbe le commit a metà (farebbe commit su una connessione ma non sull'altra, in caso di più dataSource, che è praticamente il default)
			// quindi catturo l'errore e lo ignoro.
			log.warn ("Errore in invio notifiche", t)
		}
	}

	private void register () {
		if (!TransactionSynchronizationManager.getSynchronizations().contains(this)) {
			TransactionSynchronizationManager.registerSynchronization(this)
		}
	}
}
