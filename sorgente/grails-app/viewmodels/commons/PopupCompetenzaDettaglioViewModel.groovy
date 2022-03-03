package commons

import grails.orm.PagedResultList
import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.dto.autenticazione.Ad4RuoloDTO
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.commons.StrutturaOrganizzativaService
import it.finmatica.atti.documenti.Allegato
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.competenze.*
import it.finmatica.atti.documenti.tipologie.GestioneTestiModelloCompetenza
import it.finmatica.atti.documenti.tipologie.TipoDeliberaCompetenza
import it.finmatica.atti.documenti.tipologie.TipoDeterminaCompetenza
import it.finmatica.atti.dto.documenti.competenze.*
import it.finmatica.atti.dto.documenti.tipologie.GestioneTestiModelloCompetenzaDTO
import it.finmatica.atti.dto.documenti.tipologie.TipoDeliberaCompetenzaDTO
import it.finmatica.atti.dto.documenti.tipologie.TipoDeterminaCompetenzaDTO
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.OperazioniLogService
import it.finmatica.gestioneiter.dto.configuratore.dizionari.WkfAzioneDTO
import it.finmatica.gestioneiter.dto.configuratore.dizionari.WkfTipoOggettoDTO
import it.finmatica.gestioneiter.impostazioni.WkfImpostazione
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubb
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.event.InputEvent
import org.zkoss.zk.ui.event.OpenEvent
import org.zkoss.zk.ui.event.SelectEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class PopupCompetenzaDettaglioViewModel {
    def springSecurityService
    def so4UnitaPubbService

	StrutturaOrganizzativaService strutturaOrganizzativaService
    OperazioniLogService        operazioniLogService

	def selectedRecord

    List<Ad4UtenteDTO> listaUtenteAd4Dto
    List<Ad4RuoloDTO> listaRuoloAd4Dto
    List<WkfAzioneDTO> listaMetodoCalcolo
    List<So4UnitaPubbDTO> listaUnitaOrganizzativa
    List<WkfTipoOggettoDTO> listaTipoOggetto
    String utenti


    int pageSize = 10
    int activePageUtenteAd4 = 0
    int totalSizeUtenteAd4 = 0
    int activePageRuoloAd4 = 0
    int totalSizeRuoloAd4 = 0
    int activePageUnitaOrganizzativa = 0
    int totalSizeUnitaOrganizzativa = 0

    String filtroUtenteAd4 = ""
    String filtroRuoloAd4 = ""
    String filtroUnitaOrganizzativa = ""
    String paginaLog

    String prefissoRuoli = ""

    Window self
    String titolo

    def tipiOggetto = [tipoDetermina      : [isDocumento: false, dto: TipoDeterminaCompetenzaDTO, competenza: TipoDeterminaCompetenza, doc: "tipoDetermina"]
                       , tipoDelibera     : [isDocumento: false, dto: TipoDeliberaCompetenzaDTO, competenza: TipoDeliberaCompetenza, doc: "tipoDelibera"]
                       , modelloCompetenza: [isDocumento: false, dto: GestioneTestiModelloCompetenzaDTO, competenza: GestioneTestiModelloCompetenza, doc: "gestioneTestiModello"]
                       , DETERMINA        : [isDocumento: true, dto: DeterminaCompetenzeDTO, competenza: DeterminaCompetenze, doc: "determina"]
                       , PROPOSTA_DELIBERA: [isDocumento: true, dto: PropostaDeliberaCompetenzeDTO, competenza: PropostaDeliberaCompetenze, doc: "propostaDelibera"]
                       , DELIBERA         : [isDocumento: true, dto: DeliberaCompetenzeDTO, competenza: DeliberaCompetenze, doc: "delibera"]
                       , PARERE           : [isDocumento: true, dto: VistoParereCompetenzeDTO, competenza: VistoParereCompetenze, doc: "vistoParere"]
                       , VISTO            : [isDocumento: true, dto: VistoParereCompetenzeDTO, competenza: VistoParereCompetenze, doc: "vistoParere"]
                       , CERTIFICATO      : [isDocumento: true, dto: CertificatoCompetenzeDTO, competenza: CertificatoCompetenze, doc: "certificato"]]
    String codiceOggetto;
    boolean isDocumento;


    @NotifyChange(["selectedRecord", "listaUtenteAd4Dto", "listaRuoloAd4Dto", "listaUnitaOrganizzativa", "listaTipoOggetto"])
    @Init
    init(
            @ContextParam(ContextType.COMPONENT) Window w,
            @ExecutionArgParam("documento") def doc, @ExecutionArgParam("tipoDocumento") String tipoDoc, @ExecutionArgParam("id") Long id, @ExecutionArgParam("paginaLog") String paginaLog ) {
        this.self = w
        codiceOggetto = tipoDoc;
        this.paginaLog = paginaLog
        if (id > -1) {
            selectedRecord = tipiOggetto[tipoDoc].competenza.newInstance().get(id).toDTO(['utenteAd4', 'ruoloAd4', 'unitaSo4'])
            caricaListaUtentiConRuoloInStruttura()
        }
        else {
            selectedRecord = tipiOggetto[tipoDoc].dto.newInstance();
            selectedRecord."${tipiOggetto[tipoDoc].doc}" = doc;
        }
        isDocumento = tipiOggetto[tipoDoc].isDocumento;

        //leggo il prefisso dei ruoli da visualizzare
        WkfImpostazione impostazione = WkfImpostazione.createCriteria().get {
            eq('codice', "PREFISSO_RUOLO_AD4")
        }
        if (impostazione.valore != "*") {
            prefissoRuoli = impostazione.valore
        } else {
            prefissoRuoli = ""
        }

        //inizializzo le liste delle combobox
        listaUtenteAd4Dto = caricaListaUtentiAd4()
        listaRuoloAd4Dto = caricaListaRuoliAd4()
        listaUnitaOrganizzativa = caricaListaUnitaOrganizzativa()
    }

    //metodi per il calcolo delle combobox
    private List<Ad4UtenteDTO> caricaListaUtentiAd4() {
        PagedResultList utenti = As4SoggettoCorrente.createCriteria().list(max: pageSize, offset: pageSize * activePageUtenteAd4) {
            projections {
                property("utenteAd4")
            }
            utenteAd4 {
                ilike("nominativo", filtroUtenteAd4 + "%")
                order("nominativo", "asc")
            }
        }
        totalSizeUtenteAd4 = utenti.totalCount
        return utenti.toDTO()
    }

    private void caricaListaUtentiConRuoloInStruttura() {
        if (paginaLog?.length() > 0) {
            def uo = selectedRecord.unitaSo4
            def ruolo = selectedRecord.ruoloAd4
            if (ruolo != null && uo != null) {
                utenti = strutturaOrganizzativaService.getComponentiConRuoloInUnitaFiglie(ruolo.ruolo, uo.progr, uo.ottica.codice, uo.dal)*.soggetto.utenteAd4.nominativo.toString()
            } else if (ruolo != null) {
                utenti = strutturaOrganizzativaService.getComponentiConRuoloInOttica(ruolo.ruolo, Impostazioni.OTTICA_SO4.valore)*.soggetto.utenteAd4.nominativo.toString()
            } else if (uo != null) {
                utenti = strutturaOrganizzativaService.getComponentiInUnita(uo)*.soggetto.utenteAd4.nominativo.toString();
            } else {
                utenti = selectedRecord.utenteAd4.nominativo
            }
        }
    }

    private List<Ad4RuoloDTO> caricaListaRuoliAd4() {
        PagedResultList ruoli = Ad4Ruolo.createCriteria().list(max: pageSize, offset: pageSize * activePageRuoloAd4) {
            ilike("ruolo", prefissoRuoli + "%")
            or {
                ilike("ruolo", "%" + filtroRuoloAd4 + "%")
                ilike("descrizione", "%" + filtroRuoloAd4 + "%")
            }

            order("ruolo", "asc")
        }
        totalSizeRuoloAd4 = ruoli.totalCount
        return ruoli.toDTO()
    }


    public List<So4UnitaPubbDTO> caricaListaUnitaOrganizzativa() {
        String ente = springSecurityService.principal.amm().codice
        String ottica = springSecurityService.principal.ottica().codice
        PagedResultList lista = so4UnitaPubbService.cercaUnitaPubb(ente, ottica, new Date(), filtroUnitaOrganizzativa, pageSize, pageSize * activePageUnitaOrganizzativa)
        totalSizeUnitaOrganizzativa = lista.totalCount
        return lista.toDTO()
    }

    private boolean controlloValoriNull() {
        // controllo che almeno uno dei tre (Utente, Ruolo, Unità Organizzativa) sia selezionato
        if (selectedRecord.utenteAd4 == null && selectedRecord.ruoloAd4 == null && selectedRecord.unitaSo4 == null) {
            Messagebox.show("Inserire almeno uno tra i seguenti dati: Utente, Ruolo, Unità Organizzativa!", "Attenzione!", Messagebox.OK, Messagebox.EXCLAMATION)
            return false;
        }

        // controllo che se è inserito l'utente allora nessun altro tra Ruolo e Unità Organizzativa sia inserito
        if (selectedRecord.utenteAd4 != null && (selectedRecord.ruoloAd4 != null || selectedRecord.unitaSo4 != null)) {
            Messagebox.show("Se viene selezionato un utente allora non si devono inserire i campi: Ruolo e Unità Organizzativa!", "Attenzione!", Messagebox.OK, Messagebox.EXCLAMATION)
            return false;
        }

        // se i controlli passano tutti allora è possibile salvare
        return true;
    }

    //METODI PER BANDBOX UTENTE AD4
    @NotifyChange(["selectedRecord", "utenti"])
    @Command
    onSelectUtenteAd4(
            @ContextParam(ContextType.TRIGGER_EVENT) SelectEvent event, @BindingParam("target") Component target) {
        // SOLO se ho selezionato un solo item
        if (event.getSelectedItems()?.size() == 1) {
            filtroUtenteAd4 = ""
            selectedRecord.utenteAd4 = event.getSelectedItems().toArray()[0].value
            target?.close()
            caricaListaUtentiConRuoloInStruttura()
        }
    }

    @NotifyChange(["listaUtenteAd4Dto", "totalSizeUtenteAd4"])
    @Command
    onPaginaUtenteAd4() {
        listaUtenteAd4Dto = caricaListaUtentiAd4()
    }

    @NotifyChange(["listaUtenteAd4Dto", "totalSizeUtenteAd4", "activePageUtenteAd4"])
    @Command
    onOpenUtenteAd4(@ContextParam(ContextType.TRIGGER_EVENT) OpenEvent event) {
        if (event.open) {
            activePageUtenteAd4 = 0
            listaUtenteAd4Dto = caricaListaUtentiAd4()
        }
    }


    @NotifyChange(["listaUtenteAd4Dto", "totalSizeUtenteAd4", "activePageUtenteAd4", "utenti"])
    @Command
    onChangingUtenteAd4(@ContextParam(ContextType.TRIGGER_EVENT) InputEvent event) {
        selectedRecord.utenteAd4 = null
        activePageUtenteAd4 = 0
        filtroUtenteAd4 = event.getValue()
        listaUtenteAd4Dto = caricaListaUtentiAd4()
    }

    //METODI PER BANDBOX RUOLO AD4
    @NotifyChange(["selectedRecord", "utenti"])
    @Command
    onSelectRuoloAd4(
            @ContextParam(ContextType.TRIGGER_EVENT) SelectEvent event, @BindingParam("target") Component target) {
        // SOLO se ho selezionato un solo item
        if (event.getSelectedItems()?.size() == 1) {
            filtroRuoloAd4 = ""
            selectedRecord.ruoloAd4 = event.getSelectedItems().toArray()[0].value
            target?.close()
            caricaListaUtentiConRuoloInStruttura()
        }
    }

    @NotifyChange(["listaRuoloAd4Dto", "totalSizeRuoloAd4"])
    @Command
    onPaginaRuoloAd4() {
        listaRuoloAd4Dto = caricaListaRuoliAd4()
    }

    @NotifyChange(["listaRuoloAd4Dto", "totalSizeRuoloAd4", "activePageRuoloAd4"])
    @Command
    onOpenRuoloAd4(@ContextParam(ContextType.TRIGGER_EVENT) OpenEvent event) {
        if (event.open) {
            activePageRuoloAd4 = 0
            listaRuoloAd4Dto = caricaListaRuoliAd4()
        }
    }

    @NotifyChange(["listaRuoloAd4Dto", "totalSizeRuoloAd4", "activePageRuoloAd4", "utenti"])
    @Command
    onChangingRuoloAd4(@ContextParam(ContextType.TRIGGER_EVENT) InputEvent event) {
        selectedRecord.ruoloAd4 = null
        activePageRuoloAd4 = 0
        filtroRuoloAd4 = event.getValue()
        listaRuoloAd4Dto = caricaListaRuoliAd4()
    }

    // METODI PER BANDBOX UNITA ORGANIZZATIVA
    @NotifyChange(["selectedRecord", "utenti"])
    @Command
    onSelectUnitaOrganizzativa(
            @ContextParam(ContextType.TRIGGER_EVENT) SelectEvent event, @BindingParam("target") Component target) {
        // SOLO se ho selezionato un solo item
        if (event.getSelectedItems()?.size() == 1) {
            filtroUnitaOrganizzativa = ""
            selectedRecord.unitaSo4 = event.getSelectedItems().toArray()[0].value
            target?.close()
            caricaListaUtentiConRuoloInStruttura()
        }
    }

    @NotifyChange(["listaUnitaOrganizzativa", "totalSizeUnitaOrganizzativa"])
    @Command
    onPaginaUnitaOrganizzativa() {
        listaUnitaOrganizzativa = caricaListaUnitaOrganizzativa()
    }

    @NotifyChange(["listaUnitaOrganizzativa", "totalSizeUnitaOrganizzativa", "activePageUnitaOrganizzativa"])
    @Command
    onOpenUnitaOrganizzativa(@ContextParam(ContextType.TRIGGER_EVENT) OpenEvent event) {
        if (event.open) {
            activePageUnitaOrganizzativa = 0
            listaUnitaOrganizzativa = caricaListaUnitaOrganizzativa()
        }
    }

    @NotifyChange(["listaUnitaOrganizzativa", "totalSizeUnitaOrganizzativa", "activePageUnitaOrganizzativa", "utenti"])
    @Command
    onChangingUnitaOrganizzativa(@ContextParam(ContextType.TRIGGER_EVENT) InputEvent event) {
        selectedRecord.unitaSo4 = null
        activePageUnitaOrganizzativa = 0
        filtroUnitaOrganizzativa = event.getValue()
        listaUnitaOrganizzativa = caricaListaUnitaOrganizzativa()
    }

    @NotifyChange(["listaMetodoCalcolo", "totalSizeMetodoCalcolo"])
    @Command
    onCambiaTipoOggetto() {
        listaMetodoCalcolo = caricaListaMetodoCalcolo()
    }

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onInserisci () {
		if(controlloValoriNull()){

			inserisciCompetenze(selectedRecord.unitaSo4?.getDomainObject());

            def uo = selectedRecord.unitaSo4?.getDomainObject()
            if (uo != null && selectedRecord.id == null) {
                List<So4UnitaPubb> uoFiglie = strutturaOrganizzativaService.getUnitaFiglieNLivello(uo.progr, uo.ottica.codice, uo.dal);
                for (So4UnitaPubb unita: uoFiglie) {
                    inserisciCompetenze(unita);
                }
            }
			onChiudi();
		}
	}

	private void inserisciCompetenze(So4UnitaPubb unita) {
        Determina.withTransaction {
            def competenza = selectedRecord.getDomainObject() ?: tipiOggetto[codiceOggetto].competenza.newInstance();
            if (!isDocumento) {
                competenza.titolo = titolo
            }
            else {
                competenza.lettura = selectedRecord.lettura
                competenza.modifica = selectedRecord.modifica
            }
            competenza.utenteAd4 = selectedRecord.utenteAd4?.getDomainObject()
            competenza.ruoloAd4 = selectedRecord.ruoloAd4?.getDomainObject()
            competenza.unitaSo4 = selectedRecord.unitaSo4?.getDomainObject()
            competenza."${tipiOggetto[codiceOggetto].doc}" = selectedRecord."${tipiOggetto[codiceOggetto].doc}".getDomainObject()
            competenza.save();
            selectedRecord = competenza.toDTO(['utenteAd4', 'ruoloAd4', 'unitaSo4'])
        }
        scriviLog("Aggiunta/Modificata competenza",  "Aggiunta/Modificata la competenza a "+(selectedRecord.utenteAd4?.nominativo ?: (selectedRecord.ruoloAd4?.ruolo && selectedRecord.unitaSo4?.descrizione ? selectedRecord.ruoloAd4?.ruolo +"/"+selectedRecord.unitaSo4?.descrizione: (selectedRecord.ruoloAd4?.ruolo ?: selectedRecord.unitaSo4?.descrizione))))
	}

    @Command
    onChiudi() {
        Events.postEvent(Events.ON_CLOSE, self, null)
    }

    private void scriviLog(String operazione, String descrizione) {
        if (paginaLog?.length() > 0){
            operazioniLogService.creaLog(selectedRecord."${tipiOggetto[codiceOggetto].doc}".id, codiceOggetto, paginaLog, operazione, descrizione)
        }
    }

}
