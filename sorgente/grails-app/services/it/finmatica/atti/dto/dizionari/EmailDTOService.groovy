package it.finmatica.atti.dto.dizionari

import grails.orm.PagedResultList
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.dizionari.Email
import it.finmatica.atti.exceptions.AttiRuntimeException

class EmailDTOService {

    EmailDTO salva(EmailDTO emailDto) {
        Email email = Email.get(emailDto.id) ?: new Email()
        if (email.version != emailDto.version) {
            throw new AttiRuntimeException(AttiRuntimeException.ERRORE_MODIFICA_CONCORRENTE)
        }
        email.nome = emailDto.nome
        email.cognome = emailDto.cognome
        email.ragioneSociale = emailDto.ragioneSociale
        email.indirizzoEmail = emailDto.indirizzoEmail
        email.valido = emailDto.valido

        email.save(failOnError: true)

        return email.toDTO()
    }

    void elimina(EmailDTO emailDto) {
        Email.get(emailDto.id).delete(failOnError: true)
    }

    PagedResultList cerca(String filtroSoggetti, int pageSize, int activePage) {
        return Email.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
            or {
                ilike("cognome", "%" + filtroSoggetti + "%")
                ilike("nome", "%" + filtroSoggetti + "%")
                ilike("ragioneSociale", "%" + filtroSoggetti + "%")
            }
            order("cognome", "asc")
            order("nome", "asc")
        }
    }

    /**
     * Crea o recupera una Email da un soggetto as4
     *
     * @param soggetto
     * @return una Email oppure null se il soggetto non ha email.
     */
    Email getEmail(As4SoggettoCorrente soggetto) {
        if (soggetto.indirizzoWeb == null) {
            return null
        }

        Email email = Email.findByIndirizzoEmail(soggetto.indirizzoWeb)
        if (email == null) {
            email = new Email(indirizzoEmail: soggetto.indirizzoWeb, nome: soggetto.nome, cognome: soggetto.cognome)
            email.save()
        }

        return email
    }

    /**
     * Crea o recupera una Email da un indirizzo email
     *
     * @param soggetto
     * @return una Email oppure null se il soggetto non ha email.
     */
    Email getEmail(String indirizzoEmail) {
        Email email = Email.findByIndirizzoEmail(indirizzoEmail)
        if (email == null) {
            email = new Email(indirizzoEmail: indirizzoEmail, nome: indirizzoEmail, cognome: indirizzoEmail)
            email.save()
        }

        return email
    }

}
