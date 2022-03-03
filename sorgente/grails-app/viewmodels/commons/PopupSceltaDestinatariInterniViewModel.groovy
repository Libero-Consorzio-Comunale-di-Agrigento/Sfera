package commons

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.commons.AlberoSo4
import it.finmatica.atti.commons.StrutturaOrganizzativaService
import it.finmatica.atti.dto.documenti.DestinatarioNotificaDTO
import it.finmatica.so4.dto.strutturaPubblicazione.So4ComponentePubbDTO
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO
import it.finmatica.so4.struttura.So4Ottica
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubb
import org.hibernate.FetchMode
import org.zkoss.bind.BindContext
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.DropEvent
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupSceltaDestinatariInterniViewModel {

    SpringSecurityService         springSecurityService
    StrutturaOrganizzativaService strutturaOrganizzativaService
    Window                        self
    AlberoSo4                     albero
    String                        filtro = ""
    So4UnitaPubbDTO               rootFittizia
    String                        ente
    String                        codiceOttica
    So4Ottica                     ottica

    List<So4UnitaPubbDTO>         listaUo
    List<So4ComponentePubbDTO>    listaComponenti
    List<DestinatarioNotificaDTO> setDestinatariInput
    List<DestinatarioNotificaDTO> setDestinatariOutput

    @Init
    void init (@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("destinatari") List<DestinatarioNotificaDTO> setInput) {

        this.self = w
        ente = springSecurityService.principal.amm().codice
        ottica = springSecurityService.principal.ottica
        codiceOttica = ottica.codice

        //leggo la lista di destinatari notifica in ingresso
        setDestinatariInput = setInput

        //popolo le liste degli Uo e dei componenti
        listaUo = new ArrayList<So4UnitaPubbDTO>()
        listaComponenti = new ArrayList<So4ComponentePubbDTO>()
        popolaListeUI()
        rootFittizia = new So4UnitaPubbDTO()
        rootFittizia.progr = 0
        rootFittizia.descrizione = "finto"
        filtro = ""
        albero = new AlberoSo4(ente, codiceOttica, rootFittizia, filtro)
    }

    @NotifyChange(["albero"])
    @Command
    void onFiltro () {
        albero = new AlberoSo4(ente, codiceOttica, rootFittizia, filtro)
    }

    private void popolaListeUI () {
        setDestinatariInput.each {
            if (it.utente == null) {
                // inserisco l'Uo
                listaUo.add(it.unitaSo4)
            } else if (it.utente != null && it.unitaSo4 != null) {
                // inserisco il componente
                So4ComponentePubb componente = strutturaOrganizzativaService.getComponente(it.utente.getDomainObject(), it.unitaSo4.getDomainObject())
                listaComponenti.add(componente.toDTO())
            }
        }
    }


    @NotifyChange(["listaUo"])
    @Command
    void onDropInsertUo (@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
        DropEvent event = ctx.triggerEvent
        String operation = null
        //leggo il dropped
        def dropped = event.target
        def dragged = event.getDragged().getAttribute("elemento")
        //effettuo il controllo
        if (dragged instanceof So4UnitaPubbDTO) {
            boolean presente = true
            So4UnitaPubbDTO uo = (So4UnitaPubbDTO) dragged
            presente = controllaUoPresente(uo)
            if (!presente && uo.progr >= 0) {
                //aggiunge la Unità organizzativa alla lista
                listaUo.add(uo)
            } else {
                //messaggio errore che dice che è già presente
            }
        } else {
            //messaggio errore
        }
    }

    private boolean controllaUoPresente (So4UnitaPubbDTO uo) {
        boolean presente = false
        def result = listaUo.find {
            el -> el.progr == uo.progr
        }
        if (result != null) {
            presente = true
        }
        return presente
    }

    @NotifyChange(["listaComponenti"])
    @Command
    void onDropInsertComponente (@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
        DropEvent event = ctx.triggerEvent
        String operation = null
        // leggo il dropped
        def dropped = event.target
        def dragged = event.getDragged().getAttribute("elemento")
        // effettuo il controllo
        if (dragged instanceof So4ComponentePubbDTO) {
            boolean presente = true
            So4ComponentePubbDTO comp = (So4ComponentePubbDTO) dragged
            presente = controllaComponentePresente(comp)
            if (!presente) {
                //aggiunge la Unità organizzativa alla lista
                listaComponenti.add(comp)
            } else {
                //messaggio errore che dice che è già presente
            }
        } else {
            //messaggio errore
        }
    }

    private boolean controllaComponentePresente (So4ComponentePubbDTO comp) {
        boolean presente = false
        def result = listaComponenti.find {
            el -> el.soggetto.id == comp.soggetto.id
        }
        if (result != null) {
            presente = true
        }
        return presente
    }

    @Command
    void onSelezionaDestinatari () {
        setDestinatariOutput = new ArrayList<DestinatarioNotificaDTO>()

        for (comp in listaComponenti) {
            caricaComponente(comp)
        }

        for (uo in listaUo) {
            caricaUnitaOrganizzativa(uo)
        }
        Events.postEvent(Events.ON_CLOSE, self, setDestinatariOutput)
    }

    private void caricaComponente (So4ComponentePubbDTO comp) {
        //creo il DTO da inserire
        So4ComponentePubb componente = new So4ComponentePubb()
        componente.progrUnita = comp.progrUnita
        componente.ottica = ottica
        DestinatarioNotificaDTO destinatarioDTO = new DestinatarioNotificaDTO()
        As4SoggettoCorrenteDTO soggettoCorrenteDto = As4SoggettoCorrente.createCriteria().get() {
            eq("id", comp.soggetto.id)
            fetchMode("utenteAd4", FetchMode.JOIN)
        }.toDTO()
        destinatarioDTO.utente = soggettoCorrenteDto.utenteAd4
        destinatarioDTO.unitaSo4 = componente.getUnitaPubb().toDTO()
        setDestinatariOutput.add(destinatarioDTO)
    }

    private void caricaUnitaOrganizzativa (So4UnitaPubbDTO uo) {
        //creo il DTO da inserire
        DestinatarioNotificaDTO destinatarioDTO = new DestinatarioNotificaDTO()
        destinatarioDTO.unitaSo4 = uo

        setDestinatariOutput.add(destinatarioDTO)
    }

    @Command
    void onAnnulla () {
        Events.postEvent(Events.ON_CLOSE, self, setDestinatariInput)
    }

    @NotifyChange(["listaComponenti"])
    @Command
    void onEliminaComponente (@BindingParam("comp") So4ComponentePubbDTO componente) {
        listaComponenti.remove(componente)
    }

    @NotifyChange(["listaUo"])
    @Command
    void onEliminaUo (@BindingParam("uo") So4UnitaPubbDTO unitaOrganizzativa) {
        listaUo.remove(unitaOrganizzativa)
    }
}
