import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.atti.admin.AggiornamentoService
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.jobs.AttiJobExecutor
import it.finmatica.atti.mail.Mail
import org.bouncycastle.jce.provider.BouncyCastleProvider

import java.security.Security

class BootStrap {

    SpringSecurityService springSecurityService
    AggiornamentoService  aggiornamentoService
    AttiJobExecutor       attiJobExecutor

    def init = { servletContext ->

        // per supportare comunicazioni in https (necessarie quando il cliente espone sfera su https e il webdavClient deve comunicare via https)
        // ed evitare problemi di 'Could not generate DH keypair' dobbiamo inizializzare il sistema registrando un provider bouncycastle che risolve un
        // un bug conosciuto di java6: http://bugs.java.com/bugdatabase/view_bug.do?bug_id=6521495
        // risolto in java8: http://bugs.java.com/bugdatabase/view_bug.do?bug_id=7044060
        Security.addProvider(new BouncyCastleProvider())

        // eseguo l'aggiornamento dei dati se sono al primo avvio di sfera.
        // eseguo l'autenticazione con l'utente batch (di solito AGSDE2)
        String[] codiciEnti = attiJobExecutor.eseguiAutenticazione(Holders.config.grails.plugins.anagrafesoggetti.utenteBatch)

        for (String codiceEnte : codiciEnti) {
            try {
                AttiUtils.abilitaMultiEnteFilter(codiceEnte)
                AttiUtils.setAmministrazioneOttica(codiceEnte)
                aggiornamentoService.doPendingUpdate(servletContext)

            } catch (Throwable e) {
                log.error("Errore in aggiornamento", e)
                Mail.invia(Impostazioni.ALIAS_INVIO_MAIL.valore, Impostazioni.MITTENTE_INVIO_MAIL.valore,
                           Holders.config.atti.emailProblemi
                           , "SFERA: Si sono verificati dei problemi in aggiornamento all'avvio del contesto."
                           ,"In aggiornamento all'avvio del contesto: ${springSecurityService.principal.amm()?.descrizione}"
                           , [])
            }
        }
    }

    def destroy = {

    }
}
