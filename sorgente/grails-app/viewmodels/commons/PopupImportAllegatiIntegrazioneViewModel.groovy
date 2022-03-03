package commons

import grails.util.Holders
import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.beans.AttiFileDownloader
import it.finmatica.atti.dto.documenti.AllegatoDTO
import it.finmatica.atti.dto.documenti.AllegatoDTOService
import it.finmatica.atti.integrazioni.ricercadocumenti.CampiRicerca
import it.finmatica.atti.integrazioni.ricercadocumenti.AllegatoEsterno
import it.finmatica.atti.integrazioni.ricercadocumenti.PagedList
import it.finmatica.atti.integrazioni.ricercadocumenti.RicercaAllegatiDocumentiEsterni
import org.springframework.core.annotation.AnnotationAwareOrderComparator
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.Include
import org.zkoss.zul.Window
import org.zkoss.zul.event.PagingEvent

class PopupImportAllegatiIntegrazioneViewModel {

	Window self

    List<RicercaAllegatiDocumentiEsterni> ricercaAllegatiDocumentiEsterni

    PagedList<AllegatoEsterno> listaAllegatiDocumenti
    CampiRicerca                campiRicerca

    List<AllegatoEsterno> listaAllegatiDocumentiSelezionati

    List tipiRicerca
    int tipoRicercaIndex
    def gestoreDocumentaleEsterno
    AllegatoDTO allegato
    AllegatoDTOService allegatoDTOService
    AttiFileDownloader attiFileDownloader

    @Wire("include")
    Include include

    @Init
    @NotifyChange(["selectedRicerca", "campiRicerca"])
    init (@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("allegato") AllegatoDTO allegato) {
		this.self = w
        this.allegato = allegato

        // siccome il plugin zk non supporta il binding delle variabili a liste di bean, lo faccio a mano
        ricercaAllegatiDocumentiEsterni = Holders.getApplicationContext().getBeansOfType(RicercaAllegatiDocumentiEsterni)
                .findAll { it.value.abilitato }.collect { it.value }.sort(true, new AnnotationAwareOrderComparator())
        tipiRicerca = ricercaAllegatiDocumentiEsterni.collect { [id: it.class.name, titolo: it.titolo] }
        if (tipiRicerca.size() > 0) {
            tipoRicercaIndex = 0
            onSelect()
        }
	}

    @AfterCompose
    void afterCompose (@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, false);
    }

	@Command
    @NotifyChange(["listaAllegatiDocumenti"])
    void onRicerca () {
        campiRicerca.startFrom = 0
        listaAllegatiDocumenti = selectedRicerca.ricerca(campiRicerca)
	}

    @Command
    @NotifyChange(["*"])
    void onSelect () {
        campiRicerca = getSelectedRicerca().getCampiRicerca()
        listaAllegatiDocumenti = new PagedList<>([], 0)
        listaAllegatiDocumentiSelezionati = []
	}

    String getZulCampiRicerca () {
        return getSelectedRicerca().getZulCampiRicerca()
	}

    RicercaAllegatiDocumentiEsterni getSelectedRicerca () {
        return ricercaAllegatiDocumentiEsterni[tipoRicercaIndex]
	}

    @Command
    void onChiudi () {
        BindUtils.postNotifyChange(null, null, this, "allegato")
        Events.postEvent(Events.ON_CLOSE, self, allegato)
	}

    @NotifyChange(["campiRicerca", "listaAllegatiDocumenti"])
    @Command
    void onPagina (@ContextParam(ContextType.TRIGGER_EVENT) PagingEvent pagingEvent) {
        campiRicerca.startFrom = pagingEvent.activePage * campiRicerca.maxResults
        listaAllegatiDocumenti = selectedRicerca.ricerca(campiRicerca)
    }

    @NotifyChange(["allegato"])
    @Command onImportaDocumenti () {
        this.allegato = allegatoDTOService.importaAllegatiEsterni (allegato, listaAllegatiDocumentiSelezionati)
        onChiudi()
    }

    @Command onDownloadFileAllegato (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("allegatoEsterno") AllegatoEsterno allegatoEsterno) {
        attiFileDownloader.downloadFileAllegato (allegatoEsterno.getDocumentoEsterno(), allegatoEsterno)
    }
}
