package atti.actions.integrazioni

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.documenti.DestinatarioNotifica
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.integrazioni.pnd.SmartPnd
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione
import org.springframework.beans.factory.annotation.Autowired

import javax.sql.DataSource

/**
 * Contiene le azioni per la comunicazione con SmartPND.
 */
class SmartPndAction {
    SpringSecurityService springSecurityService
    @Autowired DataSource dataSource

    SmartPnd smartPnd

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO],
            nome = "Invia la notifica PND",
            descrizione = "Invia la notifica tramite SmartPND")
    def inviaNotificaPnd (def documento) {
        smartPnd.creaNotifica(documento)
        return documento;
    }

    @Action(tipo = TipoAzione.CONDIZIONE,
            tipiOggetto = [Determina.TIPO_OGGETTO],
            nome = "Ci sono comunicazioni PND ancora da inviare ?",
            descrizione = "Ritorna TRUE se ci sono comunicazioni ancora da inviare a SmartPND, false altrimenti")
    boolean isNotificaPndDaInviare (IDocumento d) {
        for (DestinatarioNotifica dest in d.destinatariNotifiche) {
            //considereo solo le notifiche esterne, ti tipo PND, non ancora notificate
            if ((DestinatarioNotifica.TIPO_DESTINATARIO_ESTERNO.equals(dest.tipoDestinatario)) && (DestinatarioNotifica.TIPO_NOTIFICA_PND.equals(dest.tipoNotifica)) && (dest.codiceEsterno == null)) {
                return true
            }
        }
        return false
    }

    @Action(tipo = TipoAzione.CONDIZIONE,
            tipiOggetto = [Determina.TIPO_OGGETTO],
            nome = "Ci sono notifiche PND da inviare a SmartPND?",
            descrizione = "Ritorna TRUE se ci sono notifiche da inviare a SmartPND, false altrimenti")
    boolean isEsistonoNotifichePndDaInviare (IDocumento d) {
        for (DestinatarioNotifica dest in d.destinatariNotifiche) {
            //considereo solo le notifiche esterne, ti tipo PND, non ancora notificate
            if ((DestinatarioNotifica.TIPO_DESTINATARIO_ESTERNO.equals(dest.tipoDestinatario)) && (DestinatarioNotifica.TIPO_NOTIFICA_PND.equals(dest.tipoNotifica)) && (dest.codiceEsterno != null)) {
                return true
            }
        }
        return false
    }
}
