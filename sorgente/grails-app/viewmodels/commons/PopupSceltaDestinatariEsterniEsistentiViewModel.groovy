package commons

import grails.orm.PagedResultList
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.documenti.DestinatarioNotifica
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.tipologie.TipoDetermina
import it.finmatica.atti.dto.dizionari.EmailDTOService
import it.finmatica.atti.dto.documenti.DestinatarioNotificaDTO
import it.finmatica.atti.integrazioni.pnd.SmartPnd
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.*
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class PopupSceltaDestinatariEsterniEsistentiViewModel {

    EmailDTOService emailDTOService

    String filtroSoggetti
    DestinatarioNotificaDTO destNotifica
    As4SoggettoCorrenteDTO soggetto
    SmartPnd smartPnd
    boolean pndNotifica
    boolean pndPagamento
    BigDecimal importo
    Integer giorniScadenza
    Date dataScadenza

    String email

    def destinatariList
    int pageSize = 10
    int activePage = 0
    int totalSize = 0

    Window self

    String tipoNotificaCompetenza   = DestinatarioNotifica.TIPO_NOTIFICA_COMPETENZA
    String tipoNotificaConoscenza   = DestinatarioNotifica.TIPO_NOTIFICA_CONOSCENZA
    String tipoNotificaPnd          = DestinatarioNotifica.TIPO_NOTIFICA_PND
    int indiceNotifica = 0

    @NotifyChange(["destNotifica", "destinatariList", "totalSize", "pndNotifica"])
    @Init
    void init(@ContextParam(ContextType.COMPONENT) Window w,
            @ExecutionArgParam("destinatarioEsterno") DestinatarioNotificaDTO input,
            @ExecutionArgParam("documento") IDocumento documento) {
        this.self = w

        // Inizializo le variabili di classe
        activePage = 0
        filtroSoggetti = ""
        destNotifica = new DestinatarioNotificaDTO()
        destNotifica.tipoNotifica = DestinatarioNotifica.TIPO_NOTIFICA_COMPETENZA

        switch (destNotifica.tipoNotifica) {
            case DestinatarioNotifica.TIPO_NOTIFICA_COMPETENZA:
                indiceNotifica = 0
                break
            case DestinatarioNotifica.TIPO_NOTIFICA_CONOSCENZA:
                indiceNotifica = 1
                break
            case DestinatarioNotifica.TIPO_NOTIFICA_PND:
                indiceNotifica = 2
                break
        }

        if (documento instanceof Determina) {
            TipoDetermina tipoDetermina = documento.tipologiaDocumento
            if (tipoDetermina) {
                pndNotifica = tipoDetermina.pndNotifica
                if (pndNotifica) {
                    pndPagamento = smartPnd.isNotificaPagamento(tipoDetermina.id)
                }
                if (pndPagamento) {
                    giorniScadenza = smartPnd.getGiorniScadenza(tipoDetermina.id)
                }
            }
            else {
                pndNotifica  = false
                pndPagamento = false
            }
        }
        else {
            pndNotifica = false
            pndPagamento = false
        }
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
        if (DestinatarioNotifica.TIPO_NOTIFICA_PND.equals(destNotifica.tipoNotifica)) {
            if (pndNotifica) {
                if (pndPagamento) {
                    if (importo == null) {
                        Messagebox.show("Inserire l'importo del pagamento", "Attenzione!", Messagebox.OK , Messagebox.EXCLAMATION)
                        return
                    }

                    if (dataScadenza == null && giorniScadenza == null) {
                        Messagebox.show("Inserire i giorni o la data di scadenza del pagamento", "Attenzione!", Messagebox.OK , Messagebox.EXCLAMATION)
                        return
                    }

                    destNotifica.importo = importo
                    destNotifica.dataScadenza = dataScadenza
                    destNotifica.giorniScadenza = giorniScadenza
                }
            }
        }

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

    @Command onCercaSoggettoAs4 () {
        Window w = Executions.createComponents("/commons/popupRicercaSoggetti.zul", self, [id : -1, ricercaAs4: true, pndNotifica: pndNotifica])
        w.onClose { Event event ->
            if (event.data != null) {
                if (pndNotifica) {
                  //  this.destNotifica.ni = event.data.cognome
                    As4SoggettoCorrente soggettoCorrente = As4SoggettoCorrente.get(event.data.ni)
                    if (soggettoCorrente) {
                        this.destNotifica.soggetto = soggettoCorrente.toDTO()
                    }
                    if (event.data.idRecapito)
                        this.destNotifica.idRecapito = event.data.idRecapito
                    if (event.data.idContatto)
                        this.destNotifica.idContatto = event.data.idContatto
                    this.destNotifica.cognome = event.data.cognome
                    this.destNotifica.nome = event.data.nome
                    this.destNotifica.codiceFiscale = event.data.codiceFiscale
                    this.destNotifica.indirizzo = event.data.indirizzo
                    this.destNotifica.comune = event.data.comune
                    this.destNotifica.provincia = event.data.provincia
                    this.destNotifica.cap = event.data.cap
                    this.destNotifica.indirizzoEmail = event.data.email
                }
                else {
                    this.destNotifica.soggetto = event.data;
                }
                BindUtils.postNotifyChange(null, null, this, "destNotifica")
            }
        }
        w.doModal()
    }
}
