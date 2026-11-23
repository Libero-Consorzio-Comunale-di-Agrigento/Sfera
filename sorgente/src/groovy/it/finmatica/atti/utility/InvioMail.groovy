package it.finmatica.atti.utility

import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.mail.Mail

class InvioMail {


    /**
     * Invia una mail contenente gli errori riscontrati durante l'elaborazione.
     *
     * @param oggetto oggetto della mail
     * @param testo testo del messaggio della mail
     * @param errorString descrizione o dettaglio dell'errore
     */
    static void inviaMailErrori(String oggetto, String testo, String errorString) {
        inviaMailErrori(oggetto,testo,null,errorString,null)
    }

    /**
     * Invia una mail contenente gli errori riscontrati durante l'elaborazione.
     *
     * @param oggetto oggetto della mail
     * @param testo testo del messaggio della mail
     * @param errorLogFile file di log contenente gli errori
     * @param errorString descrizione o dettaglio dell'errore, alternativo a errorLogFile
     * @param nomeErrorLogFile nome del file di log degli errori, ( errori.log di default )
     */
    static void inviaMailErrori(String oggetto, String testo,  File errorLogFile, String errorString, String nomeErrorLogFile) {
        //Recupero Parametro
        //Controllo se attivo InvioMailErrori ( se diverso da - )
        String INVIO_MAIL_ERRORI = Impostazioni.INVIO_MAIL_ERRORI.valore
        if (INVIO_MAIL_ERRORI != null && !INVIO_MAIL_ERRORI.equals("-")) {
            if (nomeErrorLogFile == null || nomeErrorLogFile.length() == 0) {
                nomeErrorLogFile = "errori.log"
            }
            it.finmatica.atti.mail.Allegato tempAllegato = null
            List<it.finmatica.atti.mail.Allegato> allegatiMail = null
            if (errorLogFile != null) {
                tempAllegato = new it.finmatica.atti.mail.Allegato(nomeErrorLogFile, new ByteArrayInputStream(errorLogFile.getBytes()))
            } else if (errorString != null && errorString.length() > 0) {
                tempAllegato = new it.finmatica.atti.mail.Allegato(nomeErrorLogFile, new ByteArrayInputStream(errorString.getBytes()))
            }
            if (tempAllegato != null) {
                allegatiMail = new ArrayList<it.finmatica.atti.mail.Allegato>()
                allegatiMail.add(tempAllegato)
            }
            List<String> listaMail = Arrays.asList(INVIO_MAIL_ERRORI.split(";"));
            Mail.invia(Impostazioni.ALIAS_INVIO_MAIL.valore, Impostazioni.MITTENTE_INVIO_MAIL.valore
                    , listaMail
                    , testo
                    , oggetto
                    , allegatiMail)
        }

    }


}
