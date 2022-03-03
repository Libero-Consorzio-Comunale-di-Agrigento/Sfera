package it.finmatica.atti.commons

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.documenti.Allegato
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.login.TokenManager
import org.springframework.jdbc.datasource.DataSourceUtils
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

import javax.sql.DataSource

class TokenIntegrazioneService {

    DataSource ad4DataSource
    SpringSecurityService springSecurityService

    @Transactional(value = "transactionManagerAd4", readOnly = true)
    String getTokenAutenticazioneAd4 (String username) {
        return new TokenManager(DataSourceUtils.getConnection(ad4DataSource), username).getToken()
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    TokenIntegrazione beginTokenTransaction (String idRiferimento, String tipo) {
        // per prima cosa cerco se ce ne è già uno:
        TokenIntegrazione token = getToken(idRiferimento, tipo)

        if (token != null) {
            if (token.stato == TokenIntegrazione.STATO_SUCCESSO) {
                return token
            } else {
                // in qualunque altro caso, sta succedendo qualcosa di anomalo, blocco tutto e lo segnalo:
                throw new AttiRuntimeException(
                        "ATTENZIONE: Non è possibile procedere. Trovato token ${token.id} in stato ${token.stato} per l'id riferimento ${token.idRiferimento}. Contattare l'assistenza.")
            }
        }

        token = new TokenIntegrazione(idRiferimento: idRiferimento, tipo: tipo, stato: TokenIntegrazione.STATO_IN_CORSO)
        token.save()

        return token
    }

    @Transactional
    TokenIntegrazione lockDocumento (IDocumento documento) {
        if (!Impostazioni.CONCORRENZA_ACCESSO.abilitato) {
            return null
        }

        // per prima cosa cerco se ce ne è già uno:
        TokenIntegrazione token = getTokenDocumento(documento)
        if (token != null) {
            if (token.utenteIns.id == springSecurityService.currentUser.id) {
                return token
            }

            throw new AttiRuntimeException(
                    "ATTENZIONE: Non è possibile procedere. Trovato token ${token.id} in stato ${token.stato} per l'id riferimento ${token.idRiferimento}. Contattare l'assistenza.")
        }

        token = new TokenIntegrazione(idRiferimento:
                getIdRiferimento(documento), tipo: TokenIntegrazione.TIPO_LOCK_DOCUMENTO, stato: TokenIntegrazione.STATO_IN_CORSO)
        token.save()

        return token
    }

    @Transactional(readOnly = true)
    boolean isLocked (IDocumento documento) {
        if (!Impostazioni.CONCORRENZA_ACCESSO.abilitato) {
            return false
        }

        // per prima cosa cerco se ce ne è già uno:
        TokenIntegrazione token = getTokenDocumento(documento)
        return (token != null && token.utenteIns.id != springSecurityService.currentUser.id)
    }

    @Transactional(readOnly = true)
    boolean isLocked (Allegato documento) {
        if (!Impostazioni.CONCORRENZA_ACCESSO.abilitato) {
            return false
        }

        // per prima cosa cerco se ce ne è già uno:
        TokenIntegrazione token = getTokenDocumento(documento)
        return (token != null && token.utenteIns.id != springSecurityService.currentUser.id)
    }

    @Transactional
    void unlockDocumento (IDocumento documento) {
        if (documento == null) {
            return
        }

        if (!Impostazioni.CONCORRENZA_ACCESSO.abilitato) {
            return
        }

        TokenIntegrazione token = getTokenDocumento(documento)
        Ad4Utente utenteIns = token?.utenteIns
        Ad4Utente utenteCorrente = springSecurityService.currentUser
        if (token != null && (utenteIns.id == utenteCorrente.id)) {
            rimuoviLock(token)
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void setTokenSuccess (String idRiferimento, String tipo, String dati) {
        TokenIntegrazione token = getToken(idRiferimento, tipo)
        if (token == null) {
            throw new AttiRuntimeException(
                    "ATTENZIONE: Non è possibile procedere. Nessun token trovato in fase di ultimazione per l'id riferimento ${idRiferimento} e il tipo ${tipo}. Contattare l'assistenza.")
        }

        token.stato = TokenIntegrazione.STATO_SUCCESSO
        token.dati = dati
        token.save()
    }

    @Transactional
    void rimuoviLock (long idToken) {
        rimuoviLock(TokenIntegrazione.get(idToken))
    }

    @Transactional
    void rimuoviLock (TokenIntegrazione tokenIntegrazione) {
        tokenIntegrazione?.delete()
    }

    @Transactional(readOnly = true)
    TokenIntegrazione getToken (String idRiferimento, String tipo) {
        return TokenIntegrazione.findByIdRiferimentoAndTipo(idRiferimento, tipo)
    }

    @Transactional(readOnly = true)
    TokenIntegrazione getTokenDocumento (IDocumento documento) {
        return TokenIntegrazione.
                findByIdRiferimentoAndTipo(getIdRiferimento(documento), TokenIntegrazione.TIPO_LOCK_DOCUMENTO)
    }

    @Transactional(readOnly = true)
    TokenIntegrazione getTokenDocumento (Allegato documento) {
        return TokenIntegrazione.
                findByIdRiferimentoAndTipo(getIdRiferimento(documento), TokenIntegrazione.TIPO_LOCK_DOCUMENTO)
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void stopTokenTransaction (String idRiferimento, String tipo) {
        TokenIntegrazione.
                findByIdRiferimentoAndTipoAndStatoNotInList(idRiferimento, tipo, [TokenIntegrazione.STATO_SUCCESSO])?.
                delete()
    }

    @Transactional
    void endTokenTransaction (String idRiferimento, String tipo) {
        TokenIntegrazione.findByIdRiferimentoAndTipo(idRiferimento, tipo).delete()
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void rimuoviVecchioToken (String idRiferimento, String tipo) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -2);
        Date oldDate = cal.getTime();

        TokenIntegrazione.findByIdRiferimentoAndTipoAndDateCreatedLessThan(idRiferimento, tipo, oldDate)?.delete()
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    void rimuoviLockDocumenti (String tipo) {
        TokenIntegrazione.findAllByTipoAndStato(tipo, TokenIntegrazione.STATO_IN_CORSO)*.delete()
    }

    static String getIdRiferimento (IDocumento documento) {
        return documento.tipoOggetto + "_" + documento.idDocumento.toString()
    }

    static String getIdRiferimento (Allegato allegato) {
        return Allegato.TIPO_OGGETTO + "_" + allegato.id.toString()
    }
}
