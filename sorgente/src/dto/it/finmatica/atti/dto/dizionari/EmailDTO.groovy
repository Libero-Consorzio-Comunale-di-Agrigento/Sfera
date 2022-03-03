package it.finmatica.atti.dto.dizionari

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dizionari.Email
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class EmailDTO implements it.finmatica.dto.DTO<Email> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    String cognome;
    Date dateCreated;
    So4AmministrazioneDTO ente;
    String indirizzoEmail;
    Date lastUpdated;
    String nome;
    String ragioneSociale;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;


    public Email getDomainObject () {
        return Email.get(this.id)
    }

    public Email copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

    // questi metodi servono per "simulare" un As4SoggettoCorrente nella pagina "popupRicercaSoggetti.zul"
    String getIndirizzoWeb () {
        return indirizzoEmail
    }

    String getDenominazione () {
        if (ragioneSociale?.trim()?.length() > 0) {
            return ragioneSociale
        }
        return "${cognome} ${nome}"
    }

    String getCodiceFiscale () {
        return ""
    }

    Date getDataNascita () {
        return null
    }

    def getUtenteAd4 () {
        return [nominativo:""]
    }

}
