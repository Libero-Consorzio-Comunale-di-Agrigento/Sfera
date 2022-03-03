package it.finmatica.atti.commons

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.so4.login.So4UserDetail
import it.finmatica.so4.login.detail.Amministrazione
import it.finmatica.so4.login.detail.Ottica
import it.finmatica.so4.struttura.So4Ottica
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.hibernate.FetchMode
import org.springframework.security.core.GrantedAuthority

import java.nio.charset.Charset
import java.util.regex.Matcher
import java.util.regex.Pattern

class AttiUtils {

    static String replaceCaratteriSpeciali(String stringa) {
        def mappaCaratteri = Holders.grailsApplication.config.atti.mappaCaratteri
        for (def elemento : mappaCaratteri) {
            stringa = stringa.replaceAll(elemento.search, elemento.replace);
        }
        return stringa
    }


    static boolean controllaAllegato(String stringa) {
        def caratteri = Holders.grailsApplication.config.atti.caratteri.allegato
        Pattern p = Pattern.compile("["+caratteri+"]");
        Matcher m = p.matcher(stringa);
        m.find()
    }

    static boolean controllaCharset(String stringa, String charsetName) {
        Charset charset = Charset.forName(charsetName);
        String decoded = charset.decode(charset.encode(stringa)).toString();
        return (decoded.equals(stringa));
    }

    static boolean controllaCharset(String stringa) {
        Charset charset = Charset.forName(Impostazioni.DB_CHARSET.getValore());
        String decoded = charset.decode(charset.encode(stringa)).toString();
        return (decoded.equals(stringa));
    }

    static void controllaCharsetCampo(String nomeCampo, String stringa) {
        if (!controllaCharset(stringa, Impostazioni.DB_CHARSET.getValore())) {
            throw new AttiRuntimeException("Attenzione! Il campo " + nomeCampo + " contiene dei caratteri non supportati!");
        }
    }

    static Date dataOra(Date data) {
        return dataOra(data, null);
    }

    static Date dataOra(Date data, String ora) {
        if (data == null) {
            return null
        }

        String[] hhmm = (ora != null) ? ora.split(":") : new String[0];
        String hh = "0";
        String mm = "0";
        if (hhmm.length > 1) {
            hh = hhmm[0];
            mm = hhmm[1];
        }
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(data);
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hh));
        cal.set(Calendar.MINUTE, Integer.parseInt(mm));
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    static boolean isUtenteAmministratore() {
        GrailsApplication grailsApplication = Holders.getGrailsApplication()
        SpringSecurityService springSecurityService = (SpringSecurityService) Holders.getApplicationContext().getBean("springSecurityService")
        String ruoloAmministratore = grailsApplication.config.grails.plugins.amministrazionedatabase.modulo + "_AGDAMMI"
        for (GrantedAuthority authority : springSecurityService.principal.authorities) {
            if (authority.authority == ruoloAmministratore) {
                return true
            }
        }
        return false
    }

    static void eseguiAutenticazione(String utente, String ente = null) {
        if (ente?.trim()?.length() == 0) {
            ente = null
        }

        SpringSecurityService springSecurityService = Holders.applicationContext.springSecurityService

        // da' errore se non viene trovato l'utente,
        // carica automaticamente tutto quanto dell'utente.
        springSecurityService.reauthenticate(utente)

        So4UserDetail principal = springSecurityService.principal

        // se l'utente che fa login non ha amministrazioni/ottiche, allora devo gestirle a mano:
        if (principal.amministrazioni == null || principal.amministrazioni.size() == 0) {
            principal.amministrazioni = caricaAmministrazioniGestite(ente)
        }

        if (ente != null) {
            setAmministrazioneOttica(ente)
            abilitaMultiEnteFilter(principal.amm().codice)
        }
    }

    static void setAmministrazioneOttica(String ente = null, String ottica = null) {
        if (ente?.trim()?.length() == 0) {
            ente = null
        }

        if (ottica?.trim()?.length() == 0) {
            ottica = null
        }

        SpringSecurityService springSecurityService = Holders.applicationContext.springSecurityService
        So4UserDetail principal = springSecurityService.principal

        // se l'utente ha una sola amministrazione, seleziono quella automaticamente
        if (principal.amministrazioni.size() == 1) {
            // ottengo il valore dell'ottica dalle impostazioni:
            ente = principal.amministrazioni[0].codice
        }

        if (ente == null) {
            throw new AttiRuntimeException("È necessario specificare un ente per tra quelli possibili: ${principal.amministrazioni.codice}. Ente Richiesto: ${ente}")
        }

        if (ottica == null) {
            ottica = Impostazioni.OTTICA_SO4.getValore(ente)
        }

        principal.setAmministrazioneOtticaCorrente(ente, ottica)
    }

    static List<Amministrazione> caricaAmministrazioniGestite(String ente) {
        if (ente?.trim()?.length() == 0) {
            ente = null
        }

        // ottengo i codici degli enti gestiti dall'applicativo
        String[] codiciEntiSo4 = Impostazioni.ENTI_SO4.valori

        // se ho un ente da selezionare ma questo non è contenuto negli enti possibili, dò errore.
        if (ente != null && !codiciEntiSo4.contains(ente)) {
            throw new AttiRuntimeException("Il codice ente richiesto '${ente}' non è tra quelli gestiti: ${codiciEntiSo4}.")
        }

        def ottiche = So4Ottica.createCriteria().list {
            amministrazione {
                'in'("codice", codiciEntiSo4)
            }
            fetchMode("amministrazione", FetchMode.JOIN)
            fetchMode("amministrazione.soggetto", FetchMode.JOIN)
        }

        def amministrazioni = []
        for (So4Ottica ottica : ottiche) {
            Amministrazione amministrazione = amministrazioni.find { it.codice == ottica.amministrazione.codice }

            if (amministrazione == null) {
                amministrazione = new Amministrazione(codice: ottica.amministrazione.codice, descrizione: ottica.amministrazione.soggetto.cognome, ottiche: [])
                amministrazioni << amministrazione
            }

            if (amministrazione.ottica(ottica.codice) == null) {
                amministrazione.ottiche << new Ottica(codice: ottica.codice, descrizione: ottica.descrizione, unitaOrganizzative: [])
            }
        }

        return amministrazioni
    }

    static void abilitaMultiEnteFilter(String codiceEnte) {
        if (codiceEnte?.trim()?.length() == 0) {
            codiceEnte = null
        }

        // imposto il filtro dell'ente per la sessione hibernate
        Holders.applicationContext.sessionFactory.currentSession.enableFilter("multiEnteFilter").setParameter("enteCorrente", codiceEnte);
    }
}
