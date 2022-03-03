package commons

import groovy.xml.StreamingMarkupBuilder
import it.finmatica.atti.commons.StrutturaOrganizzativaService
import it.finmatica.atti.dizionari.*
import it.finmatica.atti.documenti.ControlloRegolarita
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.tipologie.TipoDelibera
import it.finmatica.atti.documenti.tipologie.TipoDetermina
import it.finmatica.atti.dto.dizionari.*
import it.finmatica.atti.dto.documenti.ControlloRegolaritaDTO
import it.finmatica.atti.dto.documenti.ControlloRegolaritaDocumentoDTO
import it.finmatica.atti.dto.documenti.tipologie.TipoDeliberaDTO
import it.finmatica.atti.dto.documenti.tipologie.TipoDeterminaDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.hibernate.criterion.CriteriaSpecification
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window
import static it.finmatica.zkutils.LabelUtils.getLabel as l

class PopupControlloRegolaritaViewModel {

    // service
    ControlloRegolaritaDTOService controlloRegolaritaDTOService
    StrutturaOrganizzativaService strutturaOrganizzativaService

    // componenti
    Window self
    @Wire("#popupAzioni")
    Window popupAzioni

    // dati
    ControlloRegolaritaDTO controlloRegolarita;

    List lista = new ArrayList()
    List listaTipologie = []
    List tipologie = new ArrayList()
    List<GestioneTestiModelloDTO> listaStampe
    List<TipoRegistroDTO> listaTipiRegistro
    List<CategoriaDTO> listaCategorie
    List<CategoriaDTO> categorie = new ArrayList<CategoriaDTO>()
    List registri = new ArrayList()
    List<TipoControlloRegolaritaDTO> listaTipiControlloRegolarita
    List<String> listaAmbiti = [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO];
    boolean impegnoDiSpesa = false
    def notifica
    boolean daNotificare = false
    int attiEstratti = 0
    int numeroAttiDaEstrarre = 0
    int percentualeAttiDaEstrarre = 0

    List<So4UnitaPubb> listaAree
    So4UnitaPubb area
    List<So4UnitaPubb> listaServizi = new ArrayList<So4UnitaPubb>()
    So4UnitaPubb servizio
    List strutture = new ArrayList();

    @Init
    void init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long idControlloRegolarita) {
        this.self = w

        if (idControlloRegolarita > 0) {
            this.controlloRegolarita = ControlloRegolarita.get(idControlloRegolarita).toDTO();
            if (controlloRegolarita.percentuale) {
                percentualeAttiDaEstrarre = controlloRegolarita.attiDaEstrarre
            } else {
                numeroAttiDaEstrarre = controlloRegolarita.attiDaEstrarre
            }

            String xml = controlloRegolarita.criteriRicerca
            if (xml != null && xml.length() > 0) {
                def map = convertiXMLInJson(xml)
                impegnoDiSpesa = "Si".equals(map.impegnoDiSpesa)
                registri = map.registri.collect { new TipoRegistroDTO(codice: it.id, descrizione: it.descrizione) }
                categorie = map.categorie.collect { new CategoriaDTO(id: it.id, codice: it.codice) }
                if (controlloRegolarita.ambito == Delibera.TIPO_OGGETTO)
                    tipologie = map.tipologie.collect { new TipoDeliberaDTO(id: it.id, titolo: it.titolo) }
                else if (controlloRegolarita.ambito == Determina.TIPO_OGGETTO)
                    tipologie = map.tipologie.collect { new TipoDeterminaDTO(id: it.id, titolo: it.titolo) }
                if (map.area?.id) {
                    area = So4UnitaPubb.getUnita(Long.valueOf(map.area.id), it.finmatica.atti.impostazioni.Impostazioni.OTTICA_SO4.valore).get()
                    servizio = map.servizio?.id ? So4UnitaPubb.getUnita(Long.valueOf(map.servizio.id), it.finmatica.atti.impostazioni.Impostazioni.OTTICA_SO4.valore).get() : null
                }
                strutture = map.strutture.collect { it.id }
                onChangeAmbito()
            }
        } else {
            this.controlloRegolarita = new ControlloRegolaritaDTO();
            this.controlloRegolarita.stato = ControlloRegolarita.STATO_REDAZIONE;
        }

        listaStampe = GestioneTestiModello.createCriteria().list() {
            projections {
                property("id")
                property("nome")
            }
            eq("valido", true)
            eq("tipoModello.codice", ControlloRegolarita.MODELLO_TESTO)
        }.collect { row -> new GestioneTestiModelloDTO(id: row[0], nome: row[1]) }
        def notifiche = Notifica.createCriteria().list() {
            eq("valido", true)
            eq("tipoNotifica", TipoNotifica.CONTROLLO_REGOLARITA)

        }
        notifica = notifiche[0]
        caricaListaDocumenti();

        listaAree = So4UnitaPubb.createCriteria().list {
            createAlias("suddivisione", "sud", CriteriaSpecification.LEFT_JOIN)
            eq("ottica.codice", it.finmatica.atti.impostazioni.Impostazioni.OTTICA_SO4.valore)
            eq("sud.codice", it.finmatica.atti.impostazioni.Impostazioni.SO4_SUDDIVISIONE_AREA.valore)
            isNull("al")
        }

        caricaListaServizi()
    }

    @AfterCompose
    void afterCompose(@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, false);
    }

    @Command
    void onChangeAmbito() {
        listaTipiRegistro = TipoRegistro.createCriteria().list() {
            if (controlloRegolarita.ambito == Determina.TIPO_OGGETTO) {
                eq("determina", true)
            }

            if (controlloRegolarita.ambito == Delibera.TIPO_OGGETTO) {
                eq("delibera", true)
            }
            eq("valido", true)
        }.toDTO();

        lista = [];

        listaTipiControlloRegolarita = TipoControlloRegolarita.createCriteria().list() {
            eq('valido', true)
            eq('ambito', controlloRegolarita.ambito)
            order("sequenza", "asc")
        }.toDTO();

        if (listaTipiControlloRegolarita.size() > 1) {
            listaTipiControlloRegolarita.add(0, new TipoControlloRegolaritaDTO(id: -1, titolo: "--seleziona--"))
        }
        if (controlloRegolarita.tipoControlloRegolarita == null) {
            controlloRegolarita.tipoControlloRegolarita = listaTipiControlloRegolarita[0]
        }

        def tipoDocumento

        if (controlloRegolarita.ambito == Delibera.TIPO_OGGETTO)
            tipoDocumento = TipoDelibera
        else if (controlloRegolarita.ambito == Determina.TIPO_OGGETTO)
            tipoDocumento = TipoDetermina

        listaTipologie = tipoDocumento.createCriteria().list() {
            eq("valido", true)
            order("titolo", "asc")
        }.toDTO()

        listaCategorie = Categoria.createCriteria().list() {
            eq("valido", true)
            if (controlloRegolarita.ambito == Delibera.TIPO_OGGETTO)
                eq("tipoOggetto", Categoria.TIPO_OGGETTO_PROPOSTA_DELIBERA)
            else if (controlloRegolarita.ambito == Determina.TIPO_OGGETTO)
                eq("tipoOggetto", Categoria.TIPO_OGGETTO_DETERMINA)

            order("codice", "asc")
        }

        BindUtils.postNotifyChange(null, null, this, "lista")
        BindUtils.postNotifyChange(null, null, this, "listaTipiRegistro")
        BindUtils.postNotifyChange(null, null, this, "listaTipiControlloRegolarita")
        BindUtils.postNotifyChange(null, null, this, "controlloRegolarita")
        BindUtils.postNotifyChange(null, null, this, "listaTipologie")
        BindUtils.postNotifyChange(null, null, this, "listaCategorie")
    }

    @Command
    void onChiudi() {
        Events.postEvent(Events.ON_CLOSE, self, null)
    }

    @Command
    void onTrovaDocumenti() {
        verificaCampiObbligatori()
        caricaListaDocumenti(lista.size() == 0);
    }

    @Command
    void onChangeArea() {
        caricaListaServizi()
        servizio = listaServizi.get(0);
        BindUtils.postNotifyChange(null, null, this, "listaServizi")
        BindUtils.postNotifyChange(null, null, this, "servizio")
    }

    @Command
    void onSelectServizio() {
        if (servizio?.codice?.length() > 0) {
            List<So4UnitaPubb> listaStruttureServizio = strutturaOrganizzativaService.getUnitaFiglieNLivello(servizio.progr, servizio.ottica.codice, servizio.dal, -1)
            strutture = listaStruttureServizio.collect {
                codice:
                it.progr
            }
            strutture.add(0, servizio.progr)
        } else {
            caricaListaServizi()
        }
        BindUtils.postNotifyChange(null, null, this, "listaServizi")
        BindUtils.postNotifyChange(null, null, this, "servizio")
    }

    private void caricaListaDocumenti(boolean cerca = false) {
        if (controlloRegolarita.id != null && controlloRegolarita.id > 0 && !cerca) {
            lista = controlloRegolaritaDTOService.getListaDocumenti(controlloRegolarita).toDTO();
        } else if (controlloRegolarita.id != null) {
            def criteriRicerca = creaCriteriRicerca()
            lista = controlloRegolaritaDTOService.calcolaListaDocumentiRandom(controlloRegolarita, criteriRicerca);
            controlloRegolarita.dataEstrazione = new Date()
            onSalva()
        }
        if (lista != null) {
            daNotificare = lista.findAll { it.notificata == false && it.esitoControlloRegolarita != null }.size() > 0;
            attiEstratti = lista.size()
            BindUtils.postNotifyChange(null, null, this, "lista")
            BindUtils.postNotifyChange(null, null, this, "attiEstratti")
            BindUtils.postNotifyChange(null, null, this, "daNotificare")
        }
    }

    @Command
    void onNotifica() {
        def result = controlloRegolaritaDTOService.notifica(controlloRegolarita, notifica)
        boolean erroreInvio = result.erroreInvio
        if (erroreInvio) {
            Clients.showNotification("Non è stato possibile inviare alcune notifiche.\nPer i seguenti firmatari sono non è configurata alcuna email: " + result.elencoFirmatariVuoti, Clients.NOTIFICATION_TYPE_ERROR, null, "before_center", 5000, true)
            BindUtils.postNotifyChange(null, null, this, "controlloRegolarita")
            BindUtils.postNotifyChange(null, null, this, "lista")
            BindUtils.postNotifyChange(null, null, this, "daNotificare")
            return
        }
        if (controlloRegolarita.stato == ControlloRegolarita.STATO_CHIUSO) {
            controlloRegolarita.stato = ControlloRegolarita.STATO_INVIATO
            onSalva()
        }
        daNotificare = false

        Clients.showNotification("Notifiche inviate", Clients.NOTIFICATION_TYPE_INFO, null, "before_center", 3000, true)
        BindUtils.postNotifyChange(null, null, this, "controlloRegolarita")
        BindUtils.postNotifyChange(null, null, this, "lista")
        BindUtils.postNotifyChange(null, null, this, "daNotificare")
    }

    @Command
    void onInvia() {
    }

    @Command
    void onStampa() {
        controlloRegolaritaDTOService.creaStampaRiassuntiva(controlloRegolarita)
    }

    @Command
    void onApriDocumento(@BindingParam("documento") def documento) {
        if (controlloRegolarita.ambito.equals(Determina.TIPO_OGGETTO)) {
            Executions.createComponents("/atti/documenti/determina.zul", self, [id: documento.determina.id]).doModal()
        }

        if (controlloRegolarita.ambito.equals(Delibera.TIPO_OGGETTO)) {
            Executions.createComponents("/atti/documenti/delibera.zul", self, [id: documento.delibera.id]).doModal()
        }
    }

    @Command
    void onCancella() {
        Messagebox.show("Eliminare il controllo selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                new org.zkoss.zk.ui.event.EventListener() {
                    public void onEvent(Event e) {
                        if (Messagebox.ON_OK.equals(e.getName())) {
                            controlloRegolaritaDTOService.elimina(controlloRegolarita)
                            onChiudi();
                        }
                    }
                });
    }

    @Command
    void onSalva() {
        controlloRegolarita.percentuale = percentualeAttiDaEstrarre > 0
        controlloRegolarita.attiDaEstrarre = (numeroAttiDaEstrarre > 0) ? numeroAttiDaEstrarre : percentualeAttiDaEstrarre

        verificaCampiObbligatori()
        def map = creaCriteriRicerca()
        String xml = convertiJsonInXML(map)
        controlloRegolarita.criteriRicerca = xml
        controlloRegolarita = controlloRegolaritaDTOService.salva(controlloRegolarita, lista);
        caricaListaDocumenti(false)
        Clients.showNotification("Controllo di regolarità salvato", Clients.NOTIFICATION_TYPE_INFO, null, "before_center", 3000, true)
        BindUtils.postNotifyChange(null, null, this, "lista")
        BindUtils.postNotifyChange(null, null, this, "controlloRegolarita")
    }

    @Command
    void onAssegnaEsito(@BindingParam("documento") ControlloRegolaritaDocumentoDTO documento) {
        Window w = Executions.createComponents("/commons/popupAssegnaEsitoControlloRegolarita.zul", self, [ambito: controlloRegolarita.ambito, idEsito: documento.esitoControlloRegolarita?.id, note: documento.note])
        w.onClose { event ->
            if (event.data) {
                EsitoControlloRegolaritaDTO selezionato = event.data.selectedEsito
                if (selezionato) {
                    controlloRegolaritaDTOService.assegnaEsito(documento, selezionato, event.data.note)
                    caricaListaDocumenti();
                    BindUtils.postNotifyChange(null, null, this, "lista")
                    BindUtils.postNotifyChange(null, null, this, "controlloRegolarita")
                    BindUtils.postNotifyChange(null, null, this, "daNotificare")
                    Clients.showNotification("Esito registrato", Clients.NOTIFICATION_TYPE_INFO, null, "before_center", 3000, true)
                }
            }
        }
        w.doModal()
    }

    @Command
    void onChiudiControllo() {
        if (lista.findAll { it.esitoControlloRegolarita == null }.size() > 0) {
            throw new AttiRuntimeException("Impossibile chiudere il controllo di regolarità. Esistono atti per cui non è stato specificato un esito");
        }
        Messagebox.show("Chiudere il controllo di regolarità?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                new org.zkoss.zk.ui.event.EventListener() {
                    public void onEvent(Event e) {
                        if (Messagebox.ON_OK.equals(e.getName())) {
                            controlloRegolarita = controlloRegolaritaDTOService.chiudiControllo(controlloRegolarita);
                            BindUtils.postNotifyChange(null, null, PopupControlloRegolaritaViewModel.this, "lista")
                            BindUtils.postNotifyChange(null, null, PopupControlloRegolaritaViewModel.this, "controlloRegolarita")
                        }
                    }
                });
    }

    @Command
    void onChangeList(@BindingParam("list") List list, @BindingParam("item") def item) {
        if (!list.contains(item)) {
            list.add(item)
        }
        BindUtils.postNotifyChange(null, null, this, "tipologie")
        BindUtils.postNotifyChange(null, null, this, "registri")
        BindUtils.postNotifyChange(null, null, this, "categorie")
    }

    @Command
    void onRemoveItem(@BindingParam("list") List list, @BindingParam("item") def item) {
        if (list.contains(item)) {
            list.remove(item)
        }
        BindUtils.postNotifyChange(null, null, this, "tipologie")
        BindUtils.postNotifyChange(null, null, this, "registri")
        BindUtils.postNotifyChange(null, null, this, "categorie")
    }

    private Map creaCriteriRicerca() {
        return [
                tipologie       : tipologie.collect { [id: it.id, titolo: it.titolo] }
                , registri      : registri.collect { [id: it.codice, descrizione: it.descrizione] }
                , categorie     : categorie.collect { [id: it.id, codice: it.codice] }
                , area          : area?.progr > 0 ? [id: area?.progr, descrizione: area?.descrizione] : null
                , servizio      : (servizio?.progr > 0) ? [id: servizio?.progr, descrizione: servizio?.descrizione] : null
                , strutture     : strutture.collect { [id: it] }
                , impegnoDiSpesa: impegnoDiSpesa ? "Si" : "No"
        ]
    }

    public Map convertiXMLInJson(String xml) {
        def parsed = new XmlSlurper().parseText(xml)
        def map = [registri        : parsed.registri.registro.collect { [id: it.id.text(), descrizione: it.descrizione.text()] }
                   , tipologie     : parsed.tipologie.tipologia.collect { [id: it.id.text().toInteger(), titolo: it.titolo.text()] }
                   , impegnoDiSpesa: parsed.impegnoDiSpesa.text()
                   , categorie     : parsed.categorie.categoria.collect { [id: it.id.text().toInteger(), codice: it.codice.text()] }
                   , area          : (parsed.area?.id?.text()?.length() > 0) ? [id: parsed.area?.id?.text()?.toInteger(), descrizione: parsed.area?.descrizione?.text()] : null
                   , servizio      : (parsed.servizio?.id?.text()?.length() > 0) ? [id: parsed.servizio?.id?.text()?.toInteger(), descrizione: parsed.servizio?.descrizione?.text()] : null
                   , strutture     : parsed.strutture.struttura.collect { [id: it.id.text().toInteger()] }
        ]
        return map

    }

    private String convertiJsonInXML(Map map) {
        def xml = new StreamingMarkupBuilder().bind {
            criteriRicerca {
                impegnoDiSpesa(map.impegnoDiSpesa)
                registri {
                    for (item in map.registri) {
                        registro {
                            id(item.id)
                            descrizione(item.descrizione)
                        }
                    }
                }

                tipologie {
                    for (item in map.tipologie) {
                        tipologia {
                            id(item.id)
                            titolo(item.titolo)
                        }
                    }
                }

                categorie {
                    for (item in map.categorie) {
                        categoria {
                            id(item.id)
                            codice(item.codice)
                        }
                    }
                }
                if (map.area?.id) {
                    area {
                        id(map.area.id)
                        descrizione(map.area.descrizione)
                    }
                }
                if (map.servizio?.id) {
                    servizio {
                        id(map.servizio.id)
                        descrizione(map.servizio.descrizione)
                    }
                }
                if (strutture?.size() > 0) {
                }
                strutture {
                    for (item in map.strutture) {
                        struttura {
                            id(item.id)
                        }
                    }
                }

            }
        }
    }

    void verificaCampiObbligatori() {
        if (controlloRegolarita.ambito == null) {
            throw new AttiRuntimeException("Campo obbligatorio: Ambito");
        }
        if (controlloRegolarita.tipoControlloRegolarita == null || controlloRegolarita.tipoControlloRegolarita.id == -1) {
            throw new AttiRuntimeException("Campo obbligatorio: Finalità di estrazione");
        }
        if (controlloRegolarita.dataEsecutivitaDal == null) {
            throw new AttiRuntimeException(l("message.controlloRegolarita.dataEsecutivitaDal"));
        }
        if (controlloRegolarita.dataEsecutivitaAl == null) {
            throw new AttiRuntimeException(l("message.controlloRegolarita.dataEsecutivitaAl"));
        }
		if (controlloRegolarita.attiDaEstrarre == 0){
			throw new AttiRuntimeException("Campo obbligatorio: Numero o Percentuale atti da estrarre");
        }
    }

    @Command
    void caricaListaServizi() {
        if (area && !strutture.contains(area.progr)) {
            listaServizi = strutturaOrganizzativaService.getUnitaFiglieNLivello(area.progr, area.ottica.codice, area.dal).findAll({
                it.suddivisione.codice == it.finmatica.atti.impostazioni.Impostazioni.SO4_SUDDIVISIONE_SERVIZIO.valore
            })
			strutture = strutturaOrganizzativaService.getUnitaFiglieNLivello(area.progr, area.ottica.codice, area.dal).collect{  codice : it.progr }
            strutture.add(0, area.progr)
            listaServizi.add(0, new So4UnitaPubb(codice: "", descrizione: ""))
        }
    }
}
