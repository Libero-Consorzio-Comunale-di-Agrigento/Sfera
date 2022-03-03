package it.finmatica.atti.documenti

import it.finmatica.atti.dizionari.Notifica
import it.finmatica.atti.dizionari.NotificaErrore
import it.finmatica.atti.documenti.beans.NotificheDispatcher
import it.finmatica.atti.integrazioni.jworklist.JWorklistDispatcher
import it.finmatica.gestioneiter.motore.WkfStep
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

import java.text.DecimalFormat
import java.text.SimpleDateFormat

/**
 * Questa classe si occupa di tutta la gestione degli errori di invio e cancellazione delle notifiche jworklist.
 *
 * @author czappavigna
 *
 */
class NotificheErroreService {

    NotificheService        notificheService
    NotificheDispatcher     notificheDispatcher
    JWorklistDispatcher     jworklistDispatcher


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void creaErroreNotifica(String operazione, Notifica notifica, String idRiferimento, WkfStep step) {
        try {
            NotificaErrore notificaErrore = NotificaErrore.findByValidoAndOperazioneAndIdRiferimento(true, operazione, idRiferimento)
            if (notificaErrore == null) {
                NotificaErrore errore = new NotificaErrore(operazione: operazione, notifica: notifica, idRiferimento: idRiferimento, stepCorrente: step)
                errore.save()
            } else {
                notificaErrore.lastUpdated = new Date()
                notificaErrore.stepCorrente = step
                notificaErrore.save()
            }
        } catch (Exception ex) {
            log.error("Errore durante la creazione dell'errore notifica Tipo: ${operazione}, notifica ${notifica?.tipoNotifica}, idRiferimento: ${idRiferimento}", ex)
        }
    }

    public boolean esisteErroreNotifica(String idRiferimento) {
        return NotificaErrore.countByValidoAndIdRiferimento(true, idRiferimento) > 0
    }

    public List<NotificaErrore> getErroriNotifiche() {
        return NotificaErrore.findAllByValido(true, [sort: "lastUpdated", order: "asc"])
    }

    public gestisciErroriNotifica(NotificaErrore errore){
        def documento = jworklistDispatcher.getDocumento(errore.idRiferimento)
        if (documento.iter.stepCorrente.id == errore.stepCorrente.id) {
            try {
                if (errore.operazione == NotificaErrore.OPERAZIONE_INVIO) {
                    notificheService.eliminaNotifiche(documento, errore.notifica.tipoNotifica)
                    notificheService.notifica(errore.notifica.tipoNotifica, documento)
                } else if (errore.operazione == NotificaErrore.OPERAZIONE_ELIMINA) {
                    notificheDispatcher.eliminaNotifiche(errore.idRiferimento)
                }
            } catch (Exception ex){
                log.error("Errore in fase di creazione delle notifiche per il documento ${errore.idRiferimento}", ex)
                return
            }
        }
        errore.valido = false
        errore.save()
    }
}
