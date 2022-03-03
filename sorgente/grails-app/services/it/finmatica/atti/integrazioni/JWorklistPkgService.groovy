package it.finmatica.atti.integrazioni

import groovy.sql.Sql
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.commons.StrutturaOrganizzativaService
import it.finmatica.atti.dizionari.Notifica
import it.finmatica.atti.documenti.DestinatarioNotificaAttivita
import it.finmatica.atti.documenti.SoggettoNotifica
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.jworklist.AbstractJWorklistDispatcher
import it.finmatica.gestioneiter.IDocumentoIterabile
import org.springframework.transaction.annotation.Transactional

import javax.sql.DataSource

class JWorklistPkgService extends AbstractJWorklistDispatcher {
    // service
    StrutturaOrganizzativaService strutturaOrganizzativaService

    // connessioni al db
    DataSource dataSource

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
            log.info("Invio la notifica oggetto: $oggetto, testo: $testo agli utenti: ${utenti.utente?.nominativo}");
        }

        // invio le notifiche ai singoli utenti.
        for (SoggettoNotifica soggettoNotifica : utenti) {
            String idRiferimento = getIdRiferimento(documento, notifica.tipoNotifica)
            // se ha già una notifica per questo id riferimento, non invio quella nuova:
            if (esisteNotificaJWorklist(idRiferimento, soggettoNotifica.utente)) {
                continue
            }

            try {
                List<String> idAttivitaCreate = creaNotificaJWorklist(documento, idRiferimento, soggettoNotifica.utente, oggetto, testo, priorita, stepCorrente, notifica.tipoNotifica)
                if (idAttivitaCreate != null && idAttivitaCreate.size() > 0) {
                    soggettoNotifica.idAttivita = idAttivitaCreate[0]
                    aggiungiNotificaJWorklist(idRiferimento, soggettoNotifica, documento, notifica)
                }
            } catch (Exception e1) {
                log.error("Si è verificato un secondo errore nell'invio della notifica con idRiferimento:$idRiferimento, utente:${soggettoNotifica.utente}. Non invio la notifica.", e)
            }
        }

        if (log.infoEnabled) {
            log.info("Notifiche (${utenti.idAttivita}) inviate agli utenti: ${utenti.utente?.nominativo}");
        }
    }

    @Override
    @Transactional
    void eliminaNotifica (long idNotificaDestinatarioAttivita) {
        log.debug("Eliminazione della notifica=" + idNotificaDestinatarioAttivita)
        DestinatarioNotificaAttivita notifica = DestinatarioNotificaAttivita.get(idNotificaDestinatarioAttivita)
        try {
            Sql sql = new Sql(dataSource)
            def params = [notifica.idAttivita, null, null]
            String call = "{call jwf_utility.P_ELIMINA_TASK_ESTERNO (?,?,?)}"
            sql.call(call, params)
        } catch (Exception e) {
            // se c'è stato un problema nella comunicazione, lo scrivo nei log.
            log.warn("Problemi durante l'eliminazione della notifica con id_attivita=" + notifica.idAttivita + "\n Errore: " + e.printStackTrace())
            return
        }
        notifica.delete()
    }

    private List<String> creaNotificaJWorklist (IDocumentoIterabile documentoIterabile, String idRiferimento, Ad4Utente utente, String oggetto, String testoAttivita, String priorita, String stepCorrente, String tipoNotifica) {
        boolean hasRuoloFirma = (strutturaOrganizzativaService.utenteHasRuoloDaImpostazioni(utente.id, Impostazioni.OTTICA_SO4.toString(), Impostazioni.RUOLO_SO4_FIRMA.toString()))
        String urlRiferimento = (hasRuoloFirma ? JWorklistConfig.getUrlJWorklist() + "/standalone.zul?operazione=DA_FIRMARE" : "")
        String urlRiferimentoDescrizione = (hasRuoloFirma ? "Documenti da firmare" : "")

        String urlEsecuzione = getUrlDocumento(documentoIterabile)
        String tooltipUrlEsecuzione = "Visualizza il Documento"
        Date dataAttivazione = new Date()
        String livelloPriorita = priorita
        String note = getNote(documentoIterabile, tipoNotifica)
        String paramInitIter 			= getParamInitIterString(documentoIterabile)
        String descIter = stepCorrente
        def scadenza = getScadenza(documentoIterabile, tipoNotifica)

        if (log.debugEnabled) {
            log.debug("Invio la notifica con idRiferimento=$idRiferimento all'utente: ${utente.id}");
        }

        def resp = []
        try {
            Sql sql = new Sql(dataSource)

            def params = [Sql.NUMERIC,
                          idRiferimento,
                          oggetto,           // testo del record sulla maschera della jworklist
                          testoAttivita,     // tooltip sul record nella maschera della jworklist
                          urlRiferimento,
                          urlRiferimentoDescrizione,
                          urlEsecuzione,
                          tooltipUrlEsecuzione,
                          scadenza ? new java.sql.Date(scadenza.getTime()) : null,
                          paramInitIter,
                          "",            // nome iter
                          descIter,     // descrizione iter
                          "",   // colore
                          "",   // ordinamento
                          new java.sql.Date(dataAttivazione.getTime()),
                          utente.id,
                          "",  // categoria
                          "",  // desktop
                          "",
                          TIPOLOGIA_ATTIVITA,
                          "",
                          "",
                          "",
                          "",
                          "",
                          null,
                          livelloPriorita,
                          note]

            String sqlCall = "{? = call jwf_utility.F_CREA_TASK_ESTERNO (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)}"
            sql.call(sqlCall, params) { result -> resp = [String.valueOf(result)] }

            if (log.debugEnabled) {
                log.debug("Notifica (${resp}) con idRiferimento: $idRiferimento inviata all'utente: ${utente.id}")
            }

            return resp

        } catch (Exception e) {
            // se c'è stato un problema nella comunicazione, lo scrivo nei log.
            log.warn("Problemi durante la creazione della notifica per il Documento n." + idRiferimento + ". ", e)
            return null
        }
    }
}
