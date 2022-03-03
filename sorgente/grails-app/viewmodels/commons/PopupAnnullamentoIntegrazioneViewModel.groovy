package commons

import grails.util.Holders
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.dto.documenti.DocumentoCollegatoDTO
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.ricercadocumenti.CampiRicerca
import it.finmatica.atti.integrazioni.ricercadocumenti.DocumentoEsterno
import it.finmatica.atti.integrazioni.ricercadocumenti.PagedList
import it.finmatica.atti.integrazioni.ricercadocumenti.RicercaDocumentiEsterni
import it.finmatica.atti.integrazioni.ricercadocumenti.agsde2.RicercaDelibere
import it.finmatica.atti.integrazioni.ricercadocumenti.agsde2.RicercaDetermine
import org.springframework.core.annotation.AnnotationAwareOrderComparator
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.Include
import org.zkoss.zul.Window
import org.zkoss.zul.event.PagingEvent

class PopupAnnullamentoIntegrazioneViewModel {

	Window self

    private List<RicercaDocumentiEsterni> ricercaDocumentiEsterni

    PagedList<DocumentoEsterno> listaDocumenti
    CampiRicerca                campiRicerca

    List<DocumentoEsterno> listaDocumentiSelezionati

    List tipiRicerca
    int  tipoRicercaIndex

	String tipoDocumento
    String codiceUoProponente

    String classificaCodice
    String classificaDescrizione
    Date classificaDal
    String fascicoloAnno
    String fascicoloNumero
    String fascicoloOggetto

    String campoClassifica
    String campoFasciolo

    @Wire("include")
    Include include

    @Init
    @NotifyChange(["selectedRicerca", "campiRicerca", "campiRicercaDocumenti"])
    init (@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("tipoDocumento") String tipoDocumento, @ExecutionArgParam("codiceUoProponente") String codiceUoProponente) {

        this.self = w
        this.tipoDocumento = tipoDocumento
        this.codiceUoProponente = codiceUoProponente

        campoClassifica= ""
        campoFasciolo  = ""

        // siccome il plugin zk non supporta il binding delle variabili a liste di bean, lo faccio a mano
        ricercaDocumentiEsterni = Holders.getApplicationContext().getBeansOfType(RicercaDocumentiEsterni)
                                         .findAll { it.value.abilitato }.collect { it.value }.sort(true, new AnnotationAwareOrderComparator())
        tipiRicerca = ricercaDocumentiEsterni.collect { [id: it.class.name, titolo: it.titolo] }
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
    @NotifyChange(["listaDocumenti"])
    void onRicerca () {
        campiRicerca.startFrom = 0
        // FIXME: questa gestione va tolta da qui dentro e spostata in RicercaDocumentiDaFascicolare
        campiRicerca.filtri.put("CLASSIFICA",classificaCodice)
        campiRicerca.filtri.put("FASCICOLO_ANNO",fascicoloAnno)
        campiRicerca.filtri.put("FASCICOLO_NUMERO",fascicoloNumero)
        listaDocumenti = selectedRicerca.ricerca(campiRicerca)
	}

    @Command
    @NotifyChange(["*"])
    void onSelect () {
        campiRicerca = getSelectedRicerca().getCampiRicerca()
        listaDocumenti = new PagedList<>([], 0)
        listaDocumentiSelezionati = []
	}

    String getZulCampiRicerca () {
        return getSelectedRicerca().getZulCampiRicerca()
	}

    RicercaDocumentiEsterni getSelectedRicerca () {
        return ricercaDocumentiEsterni[tipoRicercaIndex]
	}

    @Command
    void onCollegaDocumento (@BindingParam("operazione") String operazione) {
        List<DocumentoCollegatoDTO> documentiCollegati = listaDocumentiSelezionati.collect {
            getSelectedRicerca().creaDocumentoCollegato(it, operazione)
		}
		
        Events.postEvent(Events.ON_CLOSE, self, documentiCollegati)
	}

    @Command
    void onChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

    @NotifyChange(["campiRicerca", "listaDocumenti"])
    @Command
    void onPagina (@ContextParam(ContextType.TRIGGER_EVENT) PagingEvent pagingEvent) {
        campiRicerca.startFrom = pagingEvent.activePage * campiRicerca.maxResults

        // FIXME: questa gestione va tolta da qui dentro e spostata in RicercaDocumentiDaFascicolare
        campiRicerca.filtri.put("CLASSIFICA",classificaCodice)
        campiRicerca.filtri.put("FASCICOLO_ANNO",fascicoloAnno)
        campiRicerca.filtri.put("FASCICOLO_NUMERO",fascicoloNumero)
        listaDocumenti = selectedRicerca.ricerca(campiRicerca)
    }

    // FIXME: questa gestione va tolta da qui dentro e spostata in RicercaDocumentiDaFascicolare o generalizzata
    @Command
    void apriClassificazione () {
        Window w = Executions.createComponents("/commons/popupClassificazioni.zul", self,[codiceUoProponente: codiceUoProponente])
        w.onClose { event ->
            if (event.data) {
                this.classificaCodice = event.data.codice
                this.classificaDescrizione = event.data.descrizione
                this.campoClassifica = classificaCodice+" - "+classificaDescrizione
                this.campoFasciolo = ""
                BindUtils.postNotifyChange(null, null, this, "campoClassifica")
                BindUtils.postNotifyChange(null, null, this, "campoFasciolo")
            }
        }
        w.doModal()
    }

    // FIXME: questa gestione va tolta da qui dentro e spostata in RicercaDocumentiDaFascicolare o generalizzata
    @Command
    void apriFascicoli () {
        Window w = Executions.createComponents("/commons/popupFascicoli.zul", self,
                [classificaCodice: classificaCodice, classificaDescrizione: classificaDescrizione, classificaDal: classificaDal, codiceUoProponente: codiceUoProponente])
        w.onClose { event ->
            if (event.data) {
                // se ho cambiato la classificazione, la riaggiorno
                if (event.data.classifica.codice != classificaCodice) {
                    classificaCodice 		= event.data.classifica.codice
                    classificaDescrizione   = event.data.classifica.descrizione
                    classificaDal 		    = event.data.classifica.dal
                }
                fascicoloAnno 	    = event.data.anno
                fascicoloNumero 	= event.data.numero
                fascicoloOggetto 	= event.data.oggetto

                this.campoClassifica = classificaCodice+" - "+classificaDescrizione
                this.campoFasciolo = fascicoloNumero+" / "+fascicoloAnno+" - "+fascicoloOggetto
                BindUtils.postNotifyChange(null, null, this, "campoClassifica")
                BindUtils.postNotifyChange(null, null, this, "campoFasciolo")
            }
        }
        w.doModal()
    }

    // FIXME: questa gestione va tolta da qui dentro e spostata in RicercaDocumentiDaFascicolare o generalizzata
    @NotifyChange(["campoFasciolo", "campoClassifica"])
    @Command
    void onSvuotaClassifica () {
        classificaCodice 		= ""
        classificaDescrizione 	= ""
        classificaDal			= null
        fascicoloAnno 	        = ""
        fascicoloNumero 	    = ""
        fascicoloOggetto        = ""
        campoFasciolo           = ""
        campoClassifica         = ""
    }


    boolean isAnnullaAbilitato () {
        if (tipoDocumento == PropostaDelibera.TIPO_OGGETTO) {
            return Impostazioni.ANNULLA_DELIBERA_COLLEGATA.abilitato && getSelectedRicerca() instanceof RicercaDelibere
        }

        if (tipoDocumento == Determina.TIPO_OGGETTO && getSelectedRicerca() instanceof RicercaDetermine) {
            return true
        }

        return false
    }

    boolean isIntegraAbilitato () {
        if (tipoDocumento == Determina.TIPO_OGGETTO && getSelectedRicerca() instanceof RicercaDetermine) {
            return true
        }

        return false
	}
}
