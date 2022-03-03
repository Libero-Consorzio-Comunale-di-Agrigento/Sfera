package it.finmatica.atti.integrazioni.jworklist

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.dizionari.Notifica
import it.finmatica.atti.documenti.DestinatarioNotificaAttivita
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.SoggettoNotifica
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

interface JWorklistDispatcher {

    public static final String TIPOLOGIA_ATTIVITA = "ATTI_2.0"
    public static final String PRIORITA_ALTA      = "PA"
    public static final String PRIORITA_NORMALE   = "PN"

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
    void notifica (def documento, Notifica notifica, String oggetto, String testo, List<SoggettoNotifica> utenti, String priorita, String stepCorrente)

    void eliminaNotifica (long idNotificaDestinatarioAttivita)

    boolean esisteNotificaJWorklist (String idRiferimento, Ad4Utente utente)

    boolean esisteNotificaJWorklist (String idRiferimento, So4UnitaPubb unitaSo4)

    List<DestinatarioNotificaAttivita> getNotificheDaEliminare (String idRiferimento)

    List<DestinatarioNotificaAttivita> getNotificheDaEliminare (String idRiferimento, Notifica notifica)

    List<DestinatarioNotificaAttivita> getNotificheDaEliminare (String idRiferimento, Ad4Utente utente)

    List<DestinatarioNotificaAttivita> getNotificheDaEliminare (String idRiferimento, So4UnitaPubb unitaSo4)

    String getUrlDocumento (def documento)

    String getIdRiferimento (IDocumentoIterabile documentoIterabile, String tipoNotifica)

    IDocumento getDocumento (String idRiferimento)
}