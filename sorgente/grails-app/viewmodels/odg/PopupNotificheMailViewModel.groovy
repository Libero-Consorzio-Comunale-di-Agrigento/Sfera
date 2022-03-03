package odg

import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.documenti.DestinatarioNotifica
import it.finmatica.atti.documenti.NotificheService
import it.finmatica.atti.documenti.SoggettoNotifica
import it.finmatica.atti.dto.dizionari.NotificaDTO
import it.finmatica.atti.dto.odg.CommissioneStampaDTO
import it.finmatica.atti.dto.odg.OggettoSedutaDTO
import it.finmatica.atti.dto.odg.SedutaDTO
import it.finmatica.atti.dto.odg.SedutaStampaDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.odg.CommissioneStampa
import it.finmatica.atti.odg.Seduta
import it.finmatica.atti.odg.SedutaService
import it.finmatica.atti.odg.SedutaStampa
import org.hibernate.FetchMode
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.ListModelList
import org.zkoss.zul.Listbox
import org.zkoss.zul.Listitem
import org.zkoss.zul.Window

/**
 * Questa popup viene usata in tre punti diversi con scopi leggermente diversi tra loro:
 *
 * 1) SedutaIndexViewModel: per inviare la stampa di convocazione della seduta
 * 2) OdgOggettoSedutaViewModel: per inviare le notifiche TipoNotifica.DELIBERA_SEGRETARIO, TipoNotifica.VERBALIZZAZIONE_PROPOSTA del singolo oggetto-seduta
 * 3) NotificheAction: per inviare la stampa di convocazione o verbale come "documento iterabile" vero e proprio.
 */
class PopupNotificheMailViewModel {

    // service
    NotificheService notificheService
    SedutaService sedutaService

    // componenti
    Window self
    @Wire("#listboxSoggetti")
    Listbox listboxSoggetti

    // dati
    List<SoggettoNotifica> listaSoggetti
    Set<SoggettoNotifica> soggettiSelezionati
    List<String> listaIndirizzi
    String altroIndirizzo
    String titoloListaSoggetto
    SedutaDTO seduta
    SedutaStampaDTO sedutaStampa
    OggettoSedutaDTO oggettoSeduta
    NotificaDTO notifica
    boolean aggiungiNuoveEmail
    boolean convocazione
    List<CommissioneStampaDTO> listaCommissioneStampa
    def selectedStampa

    @Init
    void init(@ContextParam(ContextType.COMPONENT) Window w,
              @ExecutionArgParam("sedutaStampa") SedutaStampaDTO sedutaStampa,
              @ExecutionArgParam("seduta") SedutaDTO seduta,
              @ExecutionArgParam("oggettoSeduta") OggettoSedutaDTO oggettoSeduta,
              @ExecutionArgParam("notifica") NotificaDTO notifica,
              @ExecutionArgParam("consentiAggiuntaNuoveEmail") Boolean consentiAggiuntaNuoveEmail) {

        Seduta sed = seduta.domainObject
        SedutaStampa sedStamp = sedutaStampa?.domainObject

        if (consentiAggiuntaNuoveEmail == null) {
            consentiAggiuntaNuoveEmail = true
        }

        this.aggiungiNuoveEmail = consentiAggiuntaNuoveEmail.booleanValue()

        this.self = w
        this.titoloListaSoggetto = "Componenti " + sed.commissione.titolo
        this.sedutaStampa = sedutaStampa
        this.seduta = seduta
        this.oggettoSeduta = oggettoSeduta
        this.notifica = notifica
        convocazione = (TipoNotifica.CONVOCAZIONE_SEDUTA == notifica.tipoNotifica)
        caricaListaCommissioneStampa(seduta.commissione.id)

        if (sedStamp != null) {
            this.listaSoggetti = getSoggettiNotifica(sedStamp.destinatariNotifiche)
            this.listaIndirizzi = []
            this.soggettiSelezionati = listaSoggetti.findAll { it.idAttivita == null }
        } else {
            this.listaSoggetti = notificheService.calcolaSoggettiNotifica(notifica.domainObject, oggettoSeduta?.domainObject ?: sed)

            // elenco degli indirizzi email aggiunti manualmente in popup e definiti come Email nella notifica:
            this.listaIndirizzi = listaSoggetti.findAll {
                it.utente == null && it.soggetto == null && it.email != null
            }.email

            // elenco dei soggetti calcolati in base alle impostazioni della notifica (esclusi i soli indirizzi email)
            this.listaSoggetti = listaSoggetti.findAll { it.utente != null || it.soggetto != null }
            this.soggettiSelezionati = listaSoggetti.findAll { it.email != null }
        }

        listaSoggetti.sort { it.denominazione }
    }

    private caricaListaCommissioneStampa(Long id) {
        if (convocazione) {
            listaCommissioneStampa = CommissioneStampa.createCriteria().list () {
                eq("commissione.id", id)
                eq("codice", CommissioneStampa.CONVOCAZIONE)
                eq("valido", true)
                modelloTesto {
                    like("tipoModello.codice", it.finmatica.atti.odg.Seduta.MODELLO_TESTO_CONVOCAZIONE+"%")
                }
                fetchMode("modelloTesto", FetchMode.JOIN)
            }.toDTO()

            if (listaCommissioneStampa.size() == 1) {
                selectedStampa = listaCommissioneStampa.get(0)
            }
        }
    }

    private List<SoggettoNotifica> getSoggettiNotifica(Collection<DestinatarioNotifica> destinatari) {
        List<SoggettoNotifica> soggetti = []

        for (DestinatarioNotifica destinatarioNotifica : destinatari) {
            soggetti << new SoggettoNotifica(destinatarioNotifica)
        }

        return soggetti
    }

    @AfterCompose
    void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, false)
    }

    @Command
    void onChiudi() {
        Events.postEvent(Events.ON_CLOSE, self, null)
    }

    @NotifyChange(["listaIndirizzi", "altroIndirizzo"])
    @Command
    void onInserisciIndirizzo() {
        if (altroIndirizzo != null && !altroIndirizzo.equals("")) {
            listaIndirizzi.add(altroIndirizzo)
            altroIndirizzo = null
        }
    }

    @NotifyChange("listaIndirizzi")
    @Command
    void onEliminaIndirizzo(@BindingParam("indirizzo") String indirizzo) {
        listaIndirizzi.remove(indirizzo)
    }

    @Command
    void onSelezionaTutti() {
        listboxSoggetti.selectAll()
    }

    @Command
    void onInvioNotifica() {
        List<String> indirizzi = listboxSoggetti.getSelectedItems().value.email
        indirizzi = ((indirizzi + listaIndirizzi) - null).unique()
        // per sicurezza elimino gli indirizzi nulli anche se non dovrebbero essercene.

        // se non ho indirizzi email, do una segnalazione e non faccio nulla.
        if (indirizzi.size() == 0) {
            Clients.showNotification("Nessun destinatario ha indirizzi email, non verrà inviata alcuna notifica.", Clients.NOTIFICATION_TYPE_WARNING,
                    null, "top_center", 5000, true)
            return
        }
        if (sedutaStampa == null && selectedStampa == null && convocazione){
            throw new AttiRuntimeException("Il campo 'Stampa' è obbligatorio.")
        }

        // invio ai soli soggetti selezionati:
        if (sedutaStampa != null) {
            notificheService.notifica(notifica.domainObject, sedutaStampa.domainObject, listboxSoggetti.getSelectedItems().value)
            onChiudi()
        } else if (notifica != null) {
            sedutaService.inviaNotifica(seduta, oggettoSeduta, notifica, indirizzi, selectedStampa)
        }

        Clients.showNotification("Notifica inviata con successo.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true)
    }
}
