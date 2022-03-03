package dizionari.impostazioni

import afc.AfcAbstractGrid
import grails.gorm.PagedResultList
import it.finmatica.atti.dto.dizionari.MappingIntegrazioneDTOService
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.ModuloIntegrazione
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import org.springframework.beans.factory.annotation.Autowired
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.Window

import org.zkoss.zk.ui.event.InputEvent

class MappingIntegrazioniListaViewModel extends AfcAbstractGrid {

    // services
    MappingIntegrazioneDTOService mappingIntegrazioneDTOService

    // componenti
    Window self

    @Autowired
    List<ModuloIntegrazione> integrazioni

    @Init
    void init(@ContextParam(ContextType.COMPONENT) Window w) {
        this.self = w
    }

    @AfterCompose
    void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, false)
    }

    List<ModuloIntegrazione> getListaIntegrazioni() {
        return integrazioni.findAll{it.visibile}.sort { it.descrizione }
    }

    @NotifyChange(["listaMappingIntegrazioni", "totalSize"])
    @Command
    def onPagina() {
    }

    @NotifyChange(["mappingIntegrazione", "integrazioneSelezionata", "parametroSelezionato", "listaValoriSfera", "listaParametri"])
    @Command
    def onModifica(@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
        Window w = Executions.createComponents("/dizionari/impostazioni/mappingIntegrazioniDettaglio.zul", self, [integrazione:selectedRecord])
        w.onClose {
            activePage = 0
            BindUtils.postNotifyChange(null, null, this, "listaMappingIntegrazioni")
            BindUtils.postNotifyChange(null, null, this, "totalSize")
            BindUtils.postNotifyChange(null, null, this, "activePage")
        }
        w.doModal()
    }

    @NotifyChange(["listaMappingIntegrazioni", "totalSize", "selectedRecord", "activePage", "filtro"])
    @Command
    def onRefresh() {
        filtro = null
        selectedRecord = null
        activePage = 0
    }

    @Override
    def onModifica(@BindingParam("isNuovoRecord") boolean isNuovoRecord) {
        return null
    }

    @NotifyChange(["listaMappingIntegrazioni", "totalSize", "selectedRecord"])
    @Command
    def onElimina() {
        mappingIntegrazioneDTOService.elimina(selectedRecord)
        selectedRecord = null
    }

    @NotifyChange(["visualizzaTutti", "listaMappingIntegrazioni", "totalSize"])
    @Command
    def onVisualizzaTutti() {
        visualizzaTutti = !visualizzaTutti
        selectedRecord = null
        activePage = 0
    }

    @NotifyChange(["listaMappingIntegrazioni", "totalSize", "selectedRecord", "activePage"])
    @Command
    def onFiltro(@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
        activePage = 0
    }

    @NotifyChange(["listaMappingIntegrazioni", "totalSize", "selectedRecord", "activePage", "filtro"])
    @Command
    def onCancelFiltro() {
        onRefresh()
    }
}
