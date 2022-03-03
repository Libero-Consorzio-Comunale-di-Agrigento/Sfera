package it.finmatica.atti.dto.documenti

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.DestinatarioNotifica
import it.finmatica.atti.documenti.DestinatarioNotificaAttivita
import it.finmatica.atti.dto.dizionari.EmailDTO
import it.finmatica.atti.dto.odg.SedutaStampaDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class DestinatarioNotificaDTO implements it.finmatica.dto.DTO<DestinatarioNotifica> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    DeliberaDTO delibera;
    DeterminaDTO determina;
    EmailDTO email;
    PropostaDeliberaDTO propostaDelibera;
    String tipoDestinatario;
    String tipoNotifica;
    So4UnitaPubbDTO unitaSo4;
    Ad4UtenteDTO utente;
    SedutaStampaDTO sedutaStampa;


    DestinatarioNotifica getDomainObject () {
        return DestinatarioNotifica.get(this.id)
    }

    DestinatarioNotifica copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

    String getDenominazione () {
        if (email != null) {
            if (email.ragioneSociale?.length() > 0) {
                return email.ragioneSociale
            } else {
                return "${email.cognome?:""} ${email.nome?:""}"
            }
        }

        if (utente != null) {
            return utente.nominativoSoggetto
        }

        if (unitaSo4 != null) {
            return unitaSo4.descrizione
        }
    }

    String getIndirizzoEmail () {
        if (email != null) {
            return email.indirizzoEmail
        }

        // questo è orribile ma sarebbe necessario un refactor più impegnativo.
        if (utente != null) {
            return domainObject.soggettoCorrente.indirizzoWeb
        }

        return ""
    }

    String getIdAttivita () {
        return DestinatarioNotificaAttivita.findByDestinatarioNotifica(domainObject)?.idAttivita
    }
}
