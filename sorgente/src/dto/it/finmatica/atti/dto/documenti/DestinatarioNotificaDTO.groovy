package it.finmatica.atti.dto.documenti

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.documenti.DestinatarioNotifica
import it.finmatica.atti.documenti.DestinatarioNotificaAttivita
import it.finmatica.atti.dto.dizionari.EmailDTO
import it.finmatica.atti.dto.odg.SedutaStampaDTO
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO

@GrailsCompileStatic
public class DestinatarioNotificaDTO implements it.finmatica.dto.DTO<DestinatarioNotifica> {
    private static final long serialVersionUID = 1L;

    Long id
    Long version
    DeliberaDTO delibera
    DeterminaDTO determina
    EmailDTO email
    PropostaDeliberaDTO propostaDelibera
    String tipoDestinatario
    String tipoNotifica
    As4SoggettoCorrenteDTO soggetto
    So4UnitaPubbDTO unitaSo4
    Ad4UtenteDTO utente
    SedutaStampaDTO sedutaStampa
    BigDecimal importo
    Integer giorniScadenza
    Date dataScadenza
    String codiceEsterno
    Long idRecapito
    Long idContatto
    String cognome
    String nome
    String codiceFiscale
    String indirizzo
    String comune
    String provincia
    String cap
    String indirizzoEmail

    //aggiunti per PiTre
    String	idSoggettoEsterno
    String 	tipoSoggettoEsterno
    String 	tipoCorrispondenteEsterno
    String  tipoRubricaEsterna


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

        if (soggetto != null) {
            return soggetto.denominazione
        }

        if ((cognome != null) || (nome != null)) {
            return cognome +" "+nome
        }
    }

    String getIndirizzoEmail () {
        //se ho memorizzato il campo in tabella, questo ha la precedenza
        if (indirizzoEmail != null) {
            return indirizzoEmail
        }

        if (email != null) {
            return email.indirizzoEmail
        }

        // questo è orribile ma sarebbe necessario un refactor più impegnativo.
        if (utente != null) {
            return domainObject.soggettoCorrente.indirizzoWeb
        }

        if (soggetto != null) {
            return soggetto.indirizzoWeb
        }

        return ""
    }

    String getIdAttivita () {
        return DestinatarioNotificaAttivita.findByDestinatarioNotifica(domainObject)?.idAttivita
    }
}
