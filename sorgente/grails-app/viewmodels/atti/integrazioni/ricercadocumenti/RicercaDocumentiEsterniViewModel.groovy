package atti.integrazioni.ricercadocumenti

import grails.util.Holders
import it.finmatica.atti.integrazioni.ricercadocumenti.CampiRicerca
import it.finmatica.atti.integrazioni.ricercadocumenti.DocumentoEsterno
import it.finmatica.atti.integrazioni.ricercadocumenti.PagedList
import it.finmatica.atti.integrazioni.ricercadocumenti.RicercaDocumentiEsterni
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.Init
import org.zkoss.bind.annotation.NotifyChange
import org.zkoss.zk.ui.select.annotation.VariableResolver
import org.zkoss.zk.ui.select.annotation.WireVariable
import org.zkoss.zkplus.spring.DelegatingVariableResolver

@VariableResolver(DelegatingVariableResolver)
class RicercaDocumentiEsterniViewModel {

    @WireVariable
    List<RicercaDocumentiEsterni> ricercaDocumentiEsterni

    PagedList<DocumentoEsterno>   listaDocumenti
    CampiRicerca                  campiRicerca

    List tipiRicerca
    int tipoRicercaIndex

    @Init
    init () {
        // siccome il plugin zk non supporta il binding delle variabili a liste di bean, lo faccio a mano
        ricercaDocumentiEsterni = Holders.getApplicationContext().getBeansOfType(RicercaDocumentiEsterni).collect { it.value }
        campiRicerca = new CampiRicerca()
        tipiRicerca = ricercaDocumentiEsterni.collect { [id: it.class.name, titolo: it.titolo] }
        if (tipiRicerca.size() > 0) {
            tipoRicercaIndex = 0
        }
    }

    @Command
    @NotifyChange("listaDocumenti")
    void onRicerca () {
        listaDocumenti = selectedRicerca.ricerca(campiRicerca)
    }

    @Command
    @NotifyChange("selectedRicerca")
    void onSelect () {
        // lasciato intenzionalmente vuoto: grazie a notifychange verrà refreshato getSelectedRicerca
    }

    String getZulCampiRicerca () {
        return getSelectedRicerca().getZulCampiRicerca()
    }

    RicercaDocumentiEsterni getSelectedRicerca () {
        return ricercaDocumentiEsterni[tipoRicercaIndex]
    }
}