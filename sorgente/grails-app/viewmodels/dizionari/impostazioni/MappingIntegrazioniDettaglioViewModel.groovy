package dizionari.impostazioni

import it.finmatica.atti.config.AbstractWebServiceConfig
import it.finmatica.atti.contabilita.MovimentoContabile
import it.finmatica.atti.dto.dizionari.MappingIntegrazioneDTOService
import it.finmatica.atti.dto.impostazioni.MappingIntegrazioneDTO
import it.finmatica.atti.integrazioni.parametri.ModuloIntegrazione
import org.apache.commons.lang.math.RandomUtils
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.ListModelList
import org.zkoss.zul.Listheader
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window
import org.zkoss.zul.event.PagingEvent

class MappingIntegrazioniDettaglioViewModel {

    // services
    MappingIntegrazioneDTOService mappingIntegrazioneDTOService

    // componenti
    Window self

    // dati
    ModuloIntegrazione integrazione
    List tabs
    String tabSelected

    boolean webservice

    @NotifyChange(["selectedRecord", "locked"])
    @Init
    void init(
            @ContextParam(ContextType.COMPONENT) Window w,
            @ExecutionArgParam("integrazione") ModuloIntegrazione integrazione, @ExecutionArgParam("tabSelected") String tabSelected) {
        this.self = w
        this.integrazione = integrazione

        webservice = (integrazione instanceof AbstractWebServiceConfig)

        tabs = mappingIntegrazioneDTOService.getParametriIntegrazioni(integrazione, "")
        this.tabSelected = tabSelected ?: tabs[0]?.titolo
    }

    @NotifyChange(["tabs"])
    @Command
    def onElimina(@BindingParam("valore") MappingIntegrazioneDTO mappingIntegrazioneDTO, @BindingParam("tab") Map tab) {
        if (mappingIntegrazioneDTO.id > 0) {
            Messagebox.show("Eliminare il dettaglio dell'integrazione selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                new org.zkoss.zk.ui.event.EventListener() {
                    void onEvent (Event e) {
                        if (Messagebox.ON_OK.equals(e.getName())) {
                            mappingIntegrazioneDTOService.elimina(mappingIntegrazioneDTO)
                            tab.valori.remove(mappingIntegrazioneDTO)
                            BindUtils.postNotifyChange(null, null, MappingIntegrazioniDettaglioViewModel.this, "tabs");
                        }
                    }
                })
        }
        else {
            tab.valori.remove(mappingIntegrazioneDTO)
        }
    }

    @Command
    def onSalvaMapping(@BindingParam("valore") MappingIntegrazioneDTO mappingIntegrazioneDTO) {
        mappingIntegrazioneDTOService.salva(mappingIntegrazioneDTO)
    }

    @NotifyChange(["tabs"])
    @Command
    def onChangeTab(@BindingParam("titolo") String titolo) {
        this.tabSelected = titolo
        tabs = mappingIntegrazioneDTOService.getParametriIntegrazioni(integrazione, tabSelected)
    }

    @Command
    void onAggiungiValore(@ContextParam(ContextType.COMPONENT) Listheader listheader, @BindingParam("tab") Map tab) {
        def mapping = new MappingIntegrazioneDTO(id: -RandomUtils.nextInt(), codice: tab.parametro.codice, categoria: integrazione.codice, parametroIntegrazione: tab.parametro)
        tab.valori.add(0, mapping)

        // effettuo il refresh "puntuale" della lista a cui ho aggiunto l'elemento.
        // in questo modo non fa refresh di tutta la pagina che essendo calcolata dinamicamente toglierebbe il focus dal tab selezionato.
        ((ListModelList) listheader.getListbox().model).add(0, mapping)
    }

    @NotifyChange(["tabs"])
    @Command
    void onSalva() {
        mappingIntegrazioneDTOService.salva(tabs)
        if (webservice) {
            ((AbstractWebServiceConfig) integrazione).ricaricaParametri()
        }
        //tabs = mappingIntegrazioneDTOService.getParametriIntegrazioni(integrazione)
    }

    @Command
    void onChiudi() {
        Events.postEvent(Events.ON_CLOSE, self, null)
    }

    @NotifyChange(["tabs"])
    @Command
    void onTestWebservice() {
        onSalva()
        ((AbstractWebServiceConfig) integrazione).testWebservice()
        Clients.showNotification("Test eseguito con successo", Clients.NOTIFICATION_TYPE_INFO, null, "middle_center", 3000, true)
    }

    @Command
    void onSalvaChiudi() {
        onSalva()
        onChiudi()
    }
}
