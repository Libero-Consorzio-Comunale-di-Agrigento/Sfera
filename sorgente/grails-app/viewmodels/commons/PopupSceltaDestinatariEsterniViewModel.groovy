package commons

import it.finmatica.atti.documenti.DestinatarioNotifica
import it.finmatica.atti.dto.dizionari.EmailDTO
import it.finmatica.atti.dto.documenti.DestinatarioNotificaDTO
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupSceltaDestinatariEsterniViewModel {

    def springSecurityService
    def strutturaOrganizzativaService
    Window self

    DestinatarioNotificaDTO destinatarioInput
    DestinatarioNotificaDTO destinatarioOutput
    String tipoNotificaCompetenza = DestinatarioNotifica.TIPO_NOTIFICA_COMPETENZA
    String tipoNotificaConoscenza = DestinatarioNotifica.TIPO_NOTIFICA_CONOSCENZA
    int indiceNotifica = 0

    @Init
    void init(@ContextParam(ContextType.COMPONENT) Window w,
              @ExecutionArgParam("destinatarioEsterno") DestinatarioNotificaDTO input) {
        this.self = w
        destinatarioOutput = new DestinatarioNotificaDTO()
        destinatarioOutput.email = new EmailDTO()
        //a prescindere da quanto passato in ingresso, il destinatario della notifica deve essere esterno in questa popup
        destinatarioOutput.tipoDestinatario = DestinatarioNotifica.TIPO_DESTINATARIO_ESTERNO
        destinatarioInput = input

        //se viene passato un destinatario in ingresso allora inizializzo il dato di uscita con le informazioni passate
        if (destinatarioInput != null) {
            destinatarioOutput.email = new EmailDTO()
            destinatarioOutput.email.nome = destinatarioInput?.email?.nome
            destinatarioOutput.email.cognome = destinatarioInput?.email?.cognome
            destinatarioOutput.email.indirizzoEmail = destinatarioInput?.email?.indirizzoEmail
            destinatarioOutput.email.ragioneSociale = destinatarioInput?.email?.ragioneSociale
            destinatarioOutput.tipoNotifica = destinatarioInput?.tipoNotifica
        } else {
            //altrimenti imposto solo il tipo di notifica di default per il radio button
            destinatarioOutput.tipoNotifica = DestinatarioNotifica.TIPO_NOTIFICA_COMPETENZA
        }
        indiceNotifica = destinatarioOutput.tipoNotifica == DestinatarioNotifica.TIPO_NOTIFICA_COMPETENZA ? 0 : 1
    }

    @Command
    onSalva() {
        Events.postEvent(Events.ON_CLOSE, self, destinatarioOutput)
    }

    @Command
    onAnnulla() {
        Events.postEvent(Events.ON_CLOSE, self, null)
    }
}
