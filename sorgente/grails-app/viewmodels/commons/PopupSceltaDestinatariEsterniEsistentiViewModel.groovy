package commons

import grails.orm.PagedResultList
import it.finmatica.atti.dizionari.Email
import it.finmatica.atti.documenti.DestinatarioNotifica
import it.finmatica.atti.dto.dizionari.EmailDTOService
import it.finmatica.atti.dto.documenti.DestinatarioNotificaDTO
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zk.ui.event.OpenEvent
import org.zkoss.zk.ui.event.SelectEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class PopupSceltaDestinatariEsterniEsistentiViewModel {

    EmailDTOService emailDTOService

    String filtroSoggetti
    DestinatarioNotificaDTO destNotifica

    String email

    def destinatariList
    int pageSize = 10
    int activePage = 0
    int totalSize = 0

    Window self

    String tipoNotificaCompetenza = DestinatarioNotifica.TIPO_NOTIFICA_COMPETENZA
    String tipoNotificaConoscenza = DestinatarioNotifica.TIPO_NOTIFICA_CONOSCENZA
    int indiceNotifica = 0

    @NotifyChange(["destNotifica", "destinatariList", "totalSize"])
    @Init
    void init(@ContextParam(ContextType.COMPONENT) Window w,
            @ExecutionArgParam("destinatarioEsterno") DestinatarioNotificaDTO input) {
        this.self = w

        // Inizializo le variabili di classe
        activePage = 0
        filtroSoggetti = ""
        destNotifica = new DestinatarioNotificaDTO()
        destNotifica.tipoNotifica = DestinatarioNotifica.TIPO_NOTIFICA_COMPETENZA
        indiceNotifica = destNotifica.tipoNotifica == DestinatarioNotifica.TIPO_NOTIFICA_COMPETENZA ? 0 : 1
    }

    //////////////////////////////////////////
    //				SOGGETTI				//
    //////////////////////////////////////////

    @NotifyChange(["destinatariList", "totalSize", "activePage"])
    @Command
    void onChangingDestinatario(@ContextParam(ContextType.TRIGGER_EVENT) InputEvent event) {
        // onChanging può scattare anche subito dopo l'apertura del popup
        if (event.getValue() != email) {
            destNotifica.email = null
            activePage = 0
            filtroSoggetti = event.getValue()
            destinatariList = loadDestinatari().toDTO()
        }
    }

    @Command
    void onChangeDestinatario(@ContextParam(ContextType.TRIGGER_EVENT) InputEvent event) {
        if (filtroSoggetti != "" && destNotifica.email == null) {
            Messagebox.show("Destinatario non valido")
        }
    }

    @NotifyChange(["destinatariList", "totalSize", "activePage"])
    @Command
    void onOpenDestinatario(@ContextParam(ContextType.TRIGGER_EVENT) OpenEvent event) {
        if (event.open) {
            activePage = 0
            destinatariList = loadDestinatari().toDTO()
        }
    }

    @NotifyChange(["destinatariList", "totalSize"])
    @Command
    void onPaginaDestinatario() {
        destinatariList = loadDestinatari().toDTO()
    }

    @NotifyChange(["destNotifica", "email"])
    @Command
    void onSelectDestinatario(
            @ContextParam(ContextType.TRIGGER_EVENT) SelectEvent event, @BindingParam("target") Component target) {
        // SOLO se ho selezionato un solo item
        if (event.getSelectedItems()?.size() == 1) {
            filtroSoggetti = ""
            destNotifica.email = event.getSelectedItems().toArray()[0].value
            email = (destNotifica.email.cognome == null) ? destNotifica.email.ragioneSociale : destNotifica.email.cognome + " " + destNotifica.email.nome
            target?.close()
        }
    }

    private PagedResultList loadDestinatari() {
        PagedResultList elencoDestinatariEsterni = emailDTOService.cerca(filtroSoggetti, pageSize, activePage)
        totalSize = elencoDestinatariEsterni.totalCount
        return elencoDestinatariEsterni
    }

    @Command
    void onSalva() {
        Events.postEvent(Events.ON_CLOSE, self, destNotifica)
    }

    @Command
    void onAnnulla() {
        Events.postEvent(Events.ON_CLOSE, self, null)
    }

    @Command
    void onAggiungi() {
        Window w = Executions.createComponents("/commons/popupSceltaDestinatariEsterni.zul", self, null)
        w.onClose { event ->
            if (event.data != null) {
                if (event.data instanceof DestinatarioNotificaDTO) {
                    Events.postEvent(Events.ON_CLOSE, self, event.data)
                }
            }
        }
        w.doModal()
    }
}
