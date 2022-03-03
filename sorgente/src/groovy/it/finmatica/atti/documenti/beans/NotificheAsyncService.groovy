package it.finmatica.atti.documenti.beans

import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.dizionari.Notifica
import it.finmatica.atti.dizionari.NotificaEmail
import it.finmatica.atti.dizionari.NotificaErrore
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.NotificheErroreService
import it.finmatica.atti.documenti.NotificheService
import it.finmatica.atti.integrazioni.jworklist.JWorklistDispatcher
import it.finmatica.atti.integrazioni.pec.IntegrazionePecDucd
import it.finmatica.atti.mail.Mail
import org.apache.log4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

class NotificheAsyncService {

    private static final Logger log = Logger.getLogger(NotificheAsyncService.class)

    JWorklistDispatcher     jworklistDispatcher
    IntegrazionePecDucd     integrazionePecDucd
    NotificheService        notificheService
    NotificheErroreService  notificheErroreService

    // nota bene: uso la annotazione di spring perché così non viene creata una nuova connessione quando questo metodo viene invocato
    // dal metodo inviaNotificheAsync che è già transazionale.
    // se usassi l'annotation di grails invece, verrebbe comunque creata una nuova transazione (inutilmente)
    // la nuova transazione serve invece per quando questo metodo viene invocato da notificheDispatcher, infatti la documentazione stessa
    // dice di usare REQUIRES_NEW: http://docs.spring.io/spring/docs/4.1.8.RELEASE/javadoc-api/org/springframework/transaction/support/TransactionSynchronizationAdapter.html#afterCommit--
    @Transactional(propagation=Propagation.REQUIRES_NEW)
    void inviaNotifiche (def codaOperazioni) {
        log.debug(codaOperazioni)
        // eseguo la coda di operazioni così come è stata creata:
        for (def operazione : codaOperazioni) {
            switch (operazione.operazione) {
                case 'elimina':
                    log.info("Elimino la notifica: ${operazione}")
                    def notifiche

                    // se non ho nè utente nè unità, allora devo eliminare tutte le notifiche legate all'id_riferimento richiesto.
                    if (operazione.utente == null && operazione.unita == null) {
                        notifiche = jworklistDispatcher.getNotificheDaEliminare(operazione.idRiferimento)
                    } else {
                        notifiche = jworklistDispatcher.getNotificheDaEliminare(operazione.idRiferimento, operazione.utente ?: operazione.unita)
                    }

                    for (def n : notifiche) {
                        log.info("Elimino la notifica con id: ${n.idAttivita} e id riferimento: ${n.idRiferimento}")
                        try{
                            jworklistDispatcher.eliminaNotifica(n.id)
                        } catch (Exception ex){
                            log.error("Errore durante l'eliminazione della notifica con id: ${n.idAttivita} e id riferimento: ${n.idRiferimento}", ex)
                            def doc = jworklistDispatcher.getDocumento(n.idRiferimento)
                            notificheErroreService.creaErroreNotifica(NotificaErrore.OPERAZIONE_ELIMINA, null, n.idRiferimento, doc.iter.stepCorrente)
                        }

                    }
                    break

                case 'email':
                    log.info("Invio l'email ${operazione}")
                    Mail.invia(operazione.tagMail, operazione.mailSender, operazione.destinatariEmail, operazione.testoNotifica, operazione.oggettoNotifica, operazione.allegatiEmail)
                    break

                case 'pec':
                    log.info("Invio la pec ${operazione}")
                    def documento = operazione.documentoClass.get(operazione.documentoId)
                    integrazionePecDucd.inviaPec(operazione.tipoNotifica, documento, operazione.destinatariEmail)
                    break

                case 'jworklist':
                    log.info("Invio la notifica jworklist ${operazione}")
                    def documento = operazione.documentoClass.get(operazione.documentoId)
                    def notifica = Notifica.get(operazione.idNotifica)
                    try {
                        jworklistDispatcher.notifica(documento, notifica, operazione.oggetto, operazione.testo, operazione.utenti, operazione.priorita, operazione.stepCorrente)
                    } catch (Exception ex){
                        String idRiferimento = jworklistDispatcher.getIdRiferimento(documento, notifica.tipoNotifica)
                        log.error("Errore durante l'invio della notifica ${notifica.tipoNotifica} riferimento: ${idRiferimento}", ex)
                        notificheErroreService.creaErroreNotifica(NotificaErrore.OPERAZIONE_INVIO, notifica, idRiferimento, documento.iter.stepCorrente)
                    }

                    break

                case 'afterCommit':
                    log.info("Notifica After Commit: ${operazione}")
                    def notificaEmail = NotificaEmail.get(operazione.idNotificaEmail)
                    def notifica = notificaEmail.notifica
                    def documento = operazione.documentoClass.get(operazione.documentoId)
                    def soggetti = notificheService.calcolaSoggettoNotifica(notificaEmail, documento, notificaEmail.funzione)
                    log.info("Notifica After Commit: ${operazione}, ${soggetti}")
                    notificheService.notifica(notifica, documento, soggetti)
                    break

                default:
                    break
            }
        }
    }

    @Transactional
    void inviaNotifiche (def codaOperazioni, def utente, def ente) {
        AttiUtils.eseguiAutenticazione(utente, ente)
        inviaNotifiche(codaOperazioni)
    }
}
