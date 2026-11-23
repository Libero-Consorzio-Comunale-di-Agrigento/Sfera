package atti.documenti

import grails.orm.PagedResultList
import it.finmatica.atti.AbstractViewModel
import it.finmatica.atti.IntegrazioneAlbo
import it.finmatica.atti.IntegrazioneContabilita
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.commons.DocumentoGenerico
import it.finmatica.atti.commons.FileAllegatoGenerico
import it.finmatica.atti.commons.FileAllegatoStorico
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.dizionari.DatiAggiuntiviService
import it.finmatica.atti.dizionari.ImpattoGenerazionale
import it.finmatica.atti.dizionari.TipoBudget
import it.finmatica.atti.documenti.TipoDatoAggiuntivo
import it.finmatica.atti.export.ExportService
import it.finmatica.atti.impostazioni.RegolaCampoService
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.beans.AttiFileDownloader
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.dto.documenti.*
import it.finmatica.atti.dto.documenti.tipologie.TipoDeliberaDTO
import it.finmatica.atti.dto.integrazioni.JConsLogConservazioneDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.CampiDocumento
import it.finmatica.atti.impostazioni.CaratteristicaTipologiaService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.CasaDiVetroService
import it.finmatica.atti.integrazioni.Ce4Conto
import it.finmatica.atti.integrazioni.Ce4Fornitore
import it.finmatica.atti.dto.integrazioni.Ce4FornitoreDTO
import it.finmatica.atti.dto.integrazioni.Ce4ContoDTO
import it.finmatica.atti.dto.dizionari.ImpattoGenerazionaleDTO
import it.finmatica.atti.integrazioni.ConservazioneService
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.atti.odg.dizionari.EsitoStandard
import it.finmatica.dto.DTO
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.dto.documenti.viste.So4DelegaService
import it.finmatica.gestioneiter.Attore
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestionetesti.GestioneTestiService
import org.apache.commons.lang.StringUtils
import org.hibernate.FetchMode
import org.hibernate.criterion.CriteriaSpecification
import org.zkoss.bind.BindContext
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.event.SelectEvent
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.ListModelList
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

import java.text.DecimalFormat
import java.text.SimpleDateFormat

import it.finmatica.atti.dto.odg.dizionari.GruppoPoliticoComponenteDTO
import it.finmatica.atti.documenti.GruppoPoliticoDocumentoService

class DeliberaViewModel extends AbstractViewModel<Delibera> {

    // services
    AttiGestoreCompetenze           gestoreCompetenze
    AttiFileDownloader              attiFileDownloader
    CaratteristicaTipologiaService  caratteristicaTipologiaService
    IntegrazioneContabilita         integrazioneContabilita
    AllegatoDTOService              allegatoDTOService
    CasaDiVetroService              casaDiVetroService
    VistoParereDTOService           vistoParereDTOService

    DeliberaDTOService              deliberaDTOService
    PropostaDeliberaDTOService      propostaDeliberaDTOService
    DocumentoDTOService             documentoDTOService
    DocumentoCollegatoDTOService    documentoCollegatoDTOService
    NotificheService                notificheService
    ConservazioneService            conservazioneService
    IntegrazioneAlbo                integrazioneAlbo
    RegolaCampoService              regolaCampoService
    TokenIntegrazioneService        tokenIntegrazioneService
    DatiAggiuntiviService           datiAggiuntiviService
    BudgetDTOService                budgetDTOService
    ExportService                   exportService

	GruppoPoliticoDocumentoDTOService gruppoPoliticoDocumentoDTOService
	GruppoPoliticoDocumentoService    gruppoPoliticoDocumentoService
	So4DelegaService				so4DelegaService

    // dati
    DeliberaDTO delibera
    List<VistoParereDTO> listaVisti
    List listaCertificati
    def listaAllegati
    ListModelList<GruppoPoliticoDocumentoDTO> listaFirmatariGruppoPolitico = new ListModelList<>();
    def listaDocumentiCollegati
    def storico
    String urlCasaDiVetro
    TipoDeliberaDTO tipologiaDelibera

    // mappa dei soggetti
    Map<String, it.finmatica.atti.zk.SoggettoDocumento> soggetti = [:]

    // stato
    String titolo
    def competenze
    String posizioneFlusso
    def campiProtetti = [:]
    def noteTrasmissionePrecedenti
    boolean attorePrecedente
    String unitaProponenteDesc
    boolean mostraNoteTrasmissionePrecedenti
    boolean abilitaNoteCommissione
    boolean abilitaNoteProposta
    String nomeModelloTesto
    boolean testoLockato = false
    boolean lockPermanente = false
    boolean firmaRemotaAbilitata
    boolean mostraArchiviazioni
    boolean isNotificaPresente
    boolean isEstrattoPresente

    boolean protocollo
    boolean abilitaPubblicazioneFinoRevoca
    boolean riservatoVisibile
    boolean riservatoModificabile
    boolean visContabilita = true //indica se l'utente può visualizzare il folder della contabilità
    boolean mostraCorteConti
    boolean mostraEseguibilitaImmediata
    boolean mostraParereRevisoriConti
    boolean gestioneEmendamenti
    boolean gestioneFondi
    boolean gestioneGruppiPolitici
    boolean relataAlbo

    // gestione contabilità
    boolean conDocumentiContabili = false
    boolean contabilitaAbilitata = false

    // indica se nella lista dei visti si mostrano solo i visti validi o anche i non validi
    boolean mostraSoloVistiValidi = true
    String zulContabilita
    boolean mostraNote = true
    boolean mostraStorico = true

    // indica se abilitare la visualizzazione della relata di pubblicazione dell'albo
    boolean mostraRelata

    // indica se è bloccato da un altro utente
    boolean isLocked = false

    // indica se il documento deve essere comunque aperto in lettura (delegato)
    boolean forzaCompetenzeLettura

	// utente per conto del quale si sta operando (delega So4)
	Ad4Utente delegante

    boolean mostraGestioneGruppiPolitici = false

    // Impatto Generazionale
    boolean impattoGenerazionaleAbilitato
    List<ImpattoGenerazionaleDTO> listaImpattoGenerazionale
    ImpattoGenerazionaleDTO impattoGenerazionale
    boolean mostraImpattoGenerazionale = false

    // Protocollazione
    String tipoProtocollazione
    boolean isIntegrazioneProtocolloPiTre = false

    // Budget
    boolean budgetAbilitato
    def listaTipiBudget
    def listaBudget
    List<Ce4ContoDTO> listaCe4Conti
    List<Ce4FornitoreDTO> listaCe4Fornitori

    //PND per i destinatari gestita per la determina
    boolean pndNotifica = false
    boolean pndPagamento = false

    // paginazione integrazione ce4
    int activePage = 0
    int pageSize = 10
    int totalSize = 0
    String filtroRicerca

    @NotifyChange(["delibera", "competenze", "mostraImpattoGenerazionale", "mostraGestioneGruppiPolitici"])
	@Init
    init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long idDelibera, @ExecutionArgParam("idDocumentoEsterno") Long idDocumentoEsterno, @ExecutionArgParam("competenzeLettura") Boolean competenzeLettura, @ExecutionArgParam("delegante") Ad4Utente delegante) {
		this.self = w

        firmaRemotaAbilitata            = Impostazioni.FIRMA_REMOTA.abilitato
        protocollo                      = Impostazioni.PROTOCOLLO_SEZIONE.abilitato
        abilitaPubblicazioneFinoRevoca  = Impostazioni.PUBBLICAZIONE_FINO_REVOCA.abilitato
        riservatoVisibile               = Impostazioni.RISERVATO.abilitato
        abilitaNoteProposta             = Impostazioni.NOTE_PROPOSTA_DELIBERA.abilitato
        mostraArchiviazioni             = Impostazioni.PROTOCOLLO_MOSTRA_SEZIONE_ARCHIVIAZIONI.abilitato
        mostraCorteConti                = Impostazioni.GESTIONE_CORTE_CONTI.abilitato
        mostraEseguibilitaImmediata     = Impostazioni.ESEGUIBILITA_IMMEDIATA_ATTIVA.abilitato
        mostraParereRevisoriConti       = Impostazioni.PARERE_REVISORI_CONTI.abilitato
        gestioneEmendamenti             = Impostazioni.DELIBERA_EMENDAMENTO.abilitato
        gestioneFondi                   = Impostazioni.GESTIONE_FONDI.abilitato
        gestioneGruppiPolitici          = Impostazioni.GESTIONE_GRUPPI_POLITICI.abilitato
        budgetAbilitato                 = Impostazioni.GESTIONE_BUDGET.abilitato
        relataAlbo                      = Impostazioni.RELATA_ALBO.abilitato
        impattoGenerazionaleAbilitato   = Impostazioni.IMPATTO_GENERAZIONALE.abilitato
        tipoProtocollazione             = Impostazioni.PROTOCOLLO.valore
        forzaCompetenzeLettura          = competenzeLettura

        this.delegante					= delegante

        if (tipoProtocollazione == "protocolloPiTre") {
            isIntegrazioneProtocolloPiTre = true
            BindUtils.postNotifyChange(null, null, this, "isIntegrazioneProtocolloPiTre")
        }

        if (idDelibera != null) {
            delibera = new DeliberaDTO([id: idDelibera])
        } else {
            delibera = Delibera.findByIdDocumentoEsterno(idDocumentoEsterno).toDTO()
            idDelibera = delibera.id
        }

        Delibera d = Delibera.get(idDelibera)
        aggiornaMaschera(d)

        //se la delibera ha avuto almeno un esito (in odg) di tipo INVIA_COMMISSIONE allora devono essere visibili le note della commissione
        abilitaNoteCommissione = false
        List listaEsiti = OggettoSeduta.createCriteria().list() {
            projections {
                rowCount()
            }
            eq("propostaDelibera.id", d.proposta.id)
            eq("confermaEsito", true)
            esito {
                eq("esitoStandard.codice", EsitoStandard.INVIA_COMMISSIONE)
            }
        }

        if (listaEsiti[0] > 0)
            abilitaNoteCommissione = true

        // per la delibera, aggiorno i pulsanti solo se ho un iter associato.
        if (d.iter != null) {
            aggiornaPulsanti()
        }

        caricaListaBudget()
        caricaListaImpattoGenerazionale()
        caricaGestioneGruppiPolitici()
    }

    /*
     * 	Metodi per il calcolo dei Soggetti della determina
     */
    @Command
    onSceltaSoggetto(@BindingParam("tipoSoggetto") String tipoSoggetto, @BindingParam("categoriaSoggetto") String categoriaSoggetto) {

        // carico la tipologia in questo modo siccome nessun cliente usa la scelta dei soggetti nella delibera tranne l'AREU
        // facendo così evito di caricare per tutti una roba che non interessa a nessuno.
        if (tipologiaDelibera == null) {
            tipologiaDelibera = delibera.domainObject.tipologiaDocumento.toDTO()
        }

        Window w = Executions.createComponents("/atti/documenti/popupSceltaSoggetto.zul", self, [idCaratteristicaTipologia: tipologiaDelibera.caratteristicaTipologiaDelibera.id
                                                                                                 , documento              : delibera
                                                                                                 , soggetti               : soggetti
                                                                                                 , tipoSoggetto           : tipoSoggetto
                                                                                                 , categoriaSoggetto      : categoriaSoggetto])
        w.onClose { event ->
            // se ho annullato la modifica, non faccio niente:
            if (event.data == null)
                return;

            // altrimenti aggiorno i soggetti.
            BindUtils.postNotifyChange(null, null, this, "soggetti");
            self.invalidate()
        }
        w.doModal()
    }

    /*
     * Gestione dati di conservazione
     */
    JConsLogConservazioneDTO getLogConservazione() {
        return conservazioneService.getLastLog(delibera.idDocumentoEsterno, delibera.statoConservazione)
    }

    /*
     * Gestione Applet Testo
     */
    @NotifyChange(["testoLockato"])
    @Command
    editaTesto() {
        testoLockato = gestioneTesti.editaTesto(delibera);
    }

    @Command
    onEliminaTesto() {
        gestioneTesti.eliminaTesto(delibera, this)
    }

    @NotifyChange(["lockPermanente"])
    @Command
    onToggleLockPermanente() {
        lockPermanente = !lockPermanente;
    }

    @Command
    onDownloadTesto() {
        Delibera d = delibera.domainObject
        attiFileDownloader.downloadFileAllegato(d, d.testo)
    }

    @Command
    onDownloadTestoStorico(@BindingParam("tipoOggetto") String tipoOggetto, @BindingParam("id") Long id, @BindingParam("idFileAllegato") Long idFileAllegato) {
        FileAllegatoStorico f = FileAllegatoStorico.get(idFileAllegato)
        attiFileDownloader.downloadFileAllegato(DocumentoFactory.getDocumentoStorico(id, tipoOggetto), f, true)
    }

    @Command
    onDownloadStampaUnica() {
        Delibera d = delibera.domainObject
        attiFileDownloader.downloadFileAllegato(d, d.stampaUnica)
    }

    /*
     * Gestione degli allegati
     */
    @Command
    onModificaAllegato(@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("nuovo") boolean nuovo, @BindingParam("selected") def selected) {
        // succede quando un utente fa "doppio click" sulla tabella vuota.
        if (!nuovo && selected == null) {
            return;
        }

        Window w = Executions.createComponents("/atti/documenti/allegato.zul", self, [id: (nuovo ? -1 : selected.id), documento: delibera, competenzeLettura: forzaCompetenzeLettura])
        w.onClose {
            if (!(delibera.idDocumentoEsterno > 0)) {
                // potrei aver aggiornato la determina, quindi ne riprendo i numeri di versione e idDocumentoEsterno.
                Delibera d = delibera.domainObject;
                delibera.version = d.version;
                delibera.idDocumentoEsterno = d.idDocumentoEsterno;
            }
            refreshListaAllegati()
        }
        w.doModal()
    }

    @Command
    onEliminaAllegato(@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("allegato") AllegatoDTO allegato) {
        Messagebox.show("Eliminare l'allegato selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                new org.zkoss.zk.ui.event.EventListener() {
                    void onEvent(Event e) {
                        if (Messagebox.ON_OK.equals(e.getName())) {
                            allegatoDTOService.elimina(allegato, delibera)
                            delibera.version = delibera.domainObject.version;
                            DeliberaViewModel.this.refreshListaAllegati()
                        }
                    }
                }
        )
    }

    private void refreshListaAllegati() {
        listaAllegati = Allegato.createCriteria().list {
            eq("delibera.id", delibera.id)
            order("sequenza", "asc")
            order("titolo", "asc")
        }.toDTO(['fileAllegati'])
        BindUtils.postNotifyChange(null, null, this, "listaAllegati")
    }

    /*
     *  Gestione Pareri
     */
    private void refreshListaVisti() {
        def visti = VistoParere.createCriteria().list {
            or {
                eq("propostaDelibera.id", delibera.proposta.id)
                eq("delibera.id", delibera.id)
            }

            if (mostraSoloVistiValidi) {
                eq("valido", true)
            } else {
                or {
                    eq("valido", true)
                    ne("esito", EsitoVisto.DA_VALUTARE)
                }
            }

            tipologia {
                order("sequenzaStampaUnica", "asc")
                order("codice", "asc")
            }
            order("valido", "asc")
            order("dateCreated", "asc")

            fetchMode("tipologia", FetchMode.JOIN)
            fetchMode("unitaSo4", FetchMode.JOIN)
            fetchMode("firmatario", FetchMode.JOIN)
            fetchMode("iter", FetchMode.JOIN)
        }

        // se la delibera ha dei pareri, mostro solo quelli e non quelli della proposta.
        def pareriDelibera = visti.findAll { it.delibera != null };
        def pareriProposta = visti.findAll { it.propostaDelibera != null };

        for (def parere : pareriProposta) {
            if (pareriDelibera.findAll { it.tipologia.codice == parere.tipologia.codice }.size() > 0) {
                visti.remove(parere)
            }
        }

        // alla fine, per ogni visto, controllo di avere le competenze in modifica:
        listaVisti = []
        for (VistoParere visto : visti.sort { it.iter?.dataInizio }) {
            VistoParereDTO dto = visto.toDTO();
            dto.competenzeInModifica = gestoreCompetenze.getCompetenze(visto)
            listaVisti << dto
        }

        BindUtils.postNotifyChange(null, null, this, "listaVisti")
    }

    @Command
    onModificaVistoParere(@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("nuovo") boolean nuovo, @BindingParam("selected") def selected) {
        if (!nuovo && selected == null)
            return;

        Window w = Executions.createComponents("/atti/documenti/parere.zul", self, [id: nuovo ? -1 : selected.id, documento: delibera.proposta, tipodoc: "propostaDelibera", competenzeLettura: forzaCompetenzeLettura])
        w.onClose {
            aggiornaMaschera(delibera.domainObject)
            aggiornaPulsanti()
        }
        w.doModal()
    }

    @NotifyChange("mostraSoloVistiValidi")
    @Command
    onMostraVistiValidi() {
        mostraSoloVistiValidi = !mostraSoloVistiValidi;
        refreshListaVisti()
    }

    @Command
    onEliminaVistoParere(@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("visto") VistoParereDTO visto) {
        Messagebox.show("Eliminare il parere selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                new org.zkoss.zk.ui.event.EventListener() {
                    void onEvent(Event e) {
                        if (Messagebox.ON_OK.equals(e.getName())) {
                            vistoParereDTOService.elimina(visto)
                            delibera.version = delibera.domainObject.version;
                            DeliberaViewModel.this.refreshListaVisti()

                            // alcune condizioni di visibilità sui pulsanti dipendono dalla presenza o meno dei visti
                            DeliberaViewModel.this.aggiornaPulsanti()
                        }
                    }
                }
        )
    }

    /*
     * Gestione della proposta:
     */
    @Command
    onApriAtto() {
        Window w = Executions.createComponents("/atti/documenti/propostaDelibera.zul", self, [id: delibera.proposta.id])
        w.doModal()
    }

    @Command
    onApriTestoAtto() {
        def documento = delibera.domainObject.proposta
        attiFileDownloader.downloadFileAllegato(documento, documento.testo)
    }

    /*
     *  Gestione documenti collegati
     */
    private void refreshListaDocumentiCollegati() {
        listaDocumentiCollegati = documentoCollegatoDTOService.getListaDocumentiCollegati(delibera.domainObject)
        BindUtils.postNotifyChange(null, null, this, "listaDocumentiCollegati")
    }

    /*
     * Gestisce le note di trasmissioni
     */
    private void aggiornaNoteTrasmissionePrecedenti() {
        def result = documentoDTOService.getNoteTrasmissionePrecedenti(delibera)
        noteTrasmissionePrecedenti = result.noteTrasmissionePrecedenti
        attorePrecedente = result.attorePrecedente
        mostraNoteTrasmissionePrecedenti = result.mostraNoteTrasmissionePrecedenti

        BindUtils.postNotifyChange(null, null, this, "mostraNoteTrasmissionePrecedenti")
        BindUtils.postNotifyChange(null, null, this, "noteTrasmissionePrecedenti")
        BindUtils.postNotifyChange(null, null, this, "attorePrecedente")
    }


    /*
     * Gestione Gruppi Politici
     */
    @Command
    onAggiungiFirmatarioGruppoPolitico(@BindingParam("nuovo") boolean nuovo, @BindingParam("selected") def selected) {
        Window w = Executions.createComponents("/dizionari/odg/gruppoPoliticoDocumentoPopUp.zul", self, [id: (nuovo ? -1 : selected.id), documento: delibera, competenzeLettura: forzaCompetenzeLettura])
        w.onClose { event ->
            if (event?.data != null) {
                List<GruppoPoliticoComponenteDTO> lista = event.data
                for (GruppoPoliticoComponenteDTO gruppoPoliticoComponente : lista) {
                    GruppoPoliticoDocumento gruppoPoliticoDocumento = new GruppoPoliticoDocumento()
                    gruppoPoliticoDocumento.gruppoPolitico = gruppoPoliticoComponente.gruppoPolitico.domainObject
                    gruppoPoliticoDocumento.titolo = gruppoPoliticoComponente.gruppoPolitico.titolo
                    gruppoPoliticoDocumento.descrizione = gruppoPoliticoComponente.gruppoPolitico.descrizione
                    gruppoPoliticoDocumento.componente = gruppoPoliticoComponente.componente.domainObject
                    gruppoPoliticoDocumentoService.salva(gruppoPoliticoDocumento, delibera.domainObject)
                }
                refreshListaFirmatariGruppoPolitico()
            }
        }
        w.doModal()
    }

    @Command
    onEliminaFirmatarioGruppoPolitico(@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("firmatarioGruppoPolitico") GruppoPoliticoDocumentoDTO firmatarioGruppoPolitico) {
        Messagebox.show("Eliminare il firmatario selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                new org.zkoss.zk.ui.event.EventListener() {
                    void onEvent(Event e) {
                        if (Messagebox.ON_OK.equals(e.getName())) {
                            gruppoPoliticoDocumentoDTOService.elimina(firmatarioGruppoPolitico, delibera)
                            listaFirmatariGruppoPolitico.remove(firmatarioGruppoPolitico)
                            delibera.version = delibera.domainObject.version;
                            DeliberaViewModel.this.refreshListaFirmatariGruppoPolitico()
                        }
                    }
                }
        )
    }

    public void caricaGestioneGruppiPolitici() {
        mostraGestioneGruppiPolitici = false
        if (delibera.propostaDelibera.tipologia) {
            if (gestioneGruppiPolitici && delibera.propostaDelibera.tipologia.gestioneGruppiPolitici) {
                mostraGestioneGruppiPolitici = true
            }
        }
        BindUtils.postNotifyChange(null, null, this, "mostraGestioneGruppiPolitici")
    }


    private void refreshListaFirmatariGruppoPolitico() {
        caricaGestioneGruppiPolitici()
        if (mostraGestioneGruppiPolitici) {
            listaFirmatariGruppoPolitico = GruppoPoliticoDocumento.createCriteria().list {
                fetchMode("componente", FetchMode.JOIN)
                eq("delibera.id", delibera.id)
                order("descrizione", "asc")
                componente {
                    order("cognome", "asc")
                    order("nome", "asc")
                }
            }.toDTO()

            BindUtils.postNotifyChange(null, null, this, "listaFirmatariGruppoPolitico")
        }
    }

    //visualizza o meno la lista degli impatti generazionali
    private void caricaListaImpattoGenerazionale() {
        mostraImpattoGenerazionale = false
        if (impattoGenerazionaleAbilitato && delibera.propostaDelibera?.tipologia?.impattoGenerazionale) {
            mostraImpattoGenerazionale = true

            //carico la lista degli impatti generazionali
            PagedResultList lista = ImpattoGenerazionale.createCriteria().list(max: 100, offset: 0) {
                eq("valido", true)
                eq("delibera", true)
                order("titolo", "asc")
                order("descrizione", "asc")
            }
            listaImpattoGenerazionale = lista.toDTO()

            if (delibera.impattoGenerazionale?.id > 0) {
                impattoGenerazionale = ImpattoGenerazionale.get(delibera.impattoGenerazionale.id).toDTO()
            }
            BindUtils.postNotifyChange(null, null, this, "listaImpattoGenerazionale")
        }
        BindUtils.postNotifyChange(null, null, this, "mostraImpattoGenerazionale")
    }

    /*
     * Gestione dello storico:
     */
    private void caricaStorico() {
        storico = propostaDeliberaDTOService.caricaStorico(delibera.propostaDelibera) + deliberaDTOService.caricaStorico(delibera);
        BindUtils.postNotifyChange(null, null, this, "storico")
    }

    /*
     * Gestione della contabilità
     */
    @Command
    void onAggiornaContabilita() {
        aggiornaContabilita(delibera.domainObject)
    }

    void aggiornaContabilita(Delibera d) {
        if (d != null) {
            integrazioneContabilita.aggiornaMaschera(d, false)
        }
    }

    /**
     * Quando l'utente seleziona il tab dei riferimenti, controllo che il documento sia in casa di vetro:
     */
    @NotifyChange("urlCasaDiVetro")
    @Command
    onApriTabRiferimenti() {
        // aggiorno l'url del documento in casa di vetro:
        urlCasaDiVetro = casaDiVetroService.getUrlDocumentoSePresente(delibera.proposta);
    }

    /*
     * Gestione certificati
     */
    @Command
    onApriCertificato(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
        Window w = Executions.createComponents("/atti/documenti/certificato.zul", self, [id: ctx.component.selectedItem.value.id])
        w.doModal()
    }

    @NotifyChange('listaCertificati')
    @Command
    refreshListaCertificati() {
        listaCertificati = Certificato.createCriteria().list {
            createAlias("firmatari", "f", CriteriaSpecification.LEFT_JOIN)
            createAlias("f.firmatario", "uf", CriteriaSpecification.LEFT_JOIN)

            projections {
                property("id")
                tipologia {
                    property "titolo"
                }
                property("f.dataFirma")
                property("uf.nominativoSoggetto")
            }

            eq("delibera.id", delibera.id)

            order("dateCreated", "asc")
        }.collect { row -> [id: row[0], tipo: row[1], dataFirma: row[2], firmatario: row[3]] }
    }

    /*
     *  Gestione Chiusura Maschera
     */
    @Command
    onChiudi() {
        // se devo rilasciare il lock sul testo, lo rilascio.
        gestioneTesti.uploadEUnlockTesto(delibera, lockPermanente);
        tokenIntegrazioneService.unlockDocumento(delibera.domainObject)
        Events.postEvent(Events.ON_CLOSE, self, null)
    }

    /*
     *  Presa Visione
     */
    @Command
    onPresaVisione() {
        notificheService.eliminaNotifica(delibera.domainObject, springSecurityService.currentUser)
        isNotificaPresente = false
        BindUtils.postNotifyChange(null, null, this, "isNotificaPresente")
        onChiudi()
    }

    // gestione del pulsante "statico" salva:
    @Command
    onSalva() {
        Collection<String> messaggiValidazione = validaMaschera();
        if (messaggiValidazione != null && messaggiValidazione.size() > 0) {
            Clients.showNotification(StringUtils.join(messaggiValidazione, "\n"), Clients.NOTIFICATION_TYPE_ERROR, self, "middle_center", -1, true);
            return;
        }

        deliberaDTOService.salva(this)
    }

    /*
     * Implementazione dei Metodi per AbstractViewModel
     */
    DTO<Delibera> getDocumentoDTO() {
        return delibera
    }

    @Override
    WkfCfgIter getCfgIter() {
        return WkfCfgIter.getIterIstanziabile(delibera?.oggettoSeduta?.seduta?.commissione?.progressivoCfgIter ?: (long) -1).get()
    }

    Delibera getDocumentoIterabile(boolean controllaConcorrenza) {
        if (delibera.id > 0) {
            Delibera domainObject = delibera.getDomainObject()
            if (controllaConcorrenza && delibera?.version >= 0 && domainObject.version != delibera?.version) {
                throw new AttiRuntimeException("Attenzione: un altro utente ha modificato il documento su cui si sta lavorando. Impossibile continuare. \n (dto.version=${delibera.version}!=domain.version=${domainObject.version})")
            }

            return domainObject
        }

        return new Delibera()
    }

    Collection<String> validaMaschera() {
        def messaggi = []

        if (delibera.oggetto == null || delibera.oggetto.trim().length() == 0) {
            messaggi << "L'Oggetto è obbligatorio."
        } else {
            delibera.oggetto = AttiUtils.replaceCaratteriSpeciali(delibera.oggetto)
        }

        if (delibera.oggetto != null && !AttiUtils.controllaCharset(delibera.oggetto)) {
            messaggi << "L'Oggetto contiene dei caratteri non supportati."
        }

        if (delibera.oggetto != null && delibera.oggetto.size() > Impostazioni.LUNGHEZZA_OGGETTO.valoreInt) {
            messaggi << "La lunghezza dell'oggetto inserito è superiore a " + Impostazioni.LUNGHEZZA_OGGETTO.valore + " caratteri"
        }

        if (Impostazioni.ESEGUIBIILITA_IMMEDIATA_MOTIVAZIONI.abilitato && delibera.eseguibilitaImmediata && (delibera.motivazioniEseguibilita == null || delibera.motivazioniEseguibilita?.isEmpty())) {
            messaggi << "Motivazione obbligatoria per documenti con Eseguibilità Immediata"
        }

        return messaggi
    }

    void aggiornaDocumentoIterabile(Delibera d) {
        if (delibera.oggetto.size() > Impostazioni.LUNGHEZZA_OGGETTO.valoreInt) {
            throw new AttiRuntimeException("La lunghezza dell'oggetto inserito è superiore a " + Impostazioni.LUNGHEZZA_OGGETTO.valore + " caratteri")
            return;
        }

        // salvo e sblocco il testo
        gestioneTesti.uploadEUnlockTesto(d);

        d.oggetto = delibera.oggetto
        d.riservato = delibera.riservato
        d.eseguibilitaImmediata = delibera.eseguibilitaImmediata
        d.motivazioniEseguibilita = delibera.motivazioniEseguibilita
        d.pubblicaRevoca = delibera.pubblicaRevoca
        d.giorniPubblicazione = delibera.giorniPubblicazione
        d.note = delibera.note
        d.noteTrasmissione = delibera.noteTrasmissione

        d.daInviareCorteConti = delibera.daInviareCorteConti
        if (d.daInviareCorteConti) {
            d.dataInvioCorteConti = delibera.dataInvioCorteConti
        } else {
            d.dataInvioCorteConti = null
        }

        d.dataEsecutivitaManuale = delibera.dataEsecutivitaManuale

        if (impattoGenerazionale?.id > 0) {
            delibera.impattoGenerazionale = impattoGenerazionale
            d.impattoGenerazionale = delibera.impattoGenerazionale.domainObject
        } else
            d.impattoGenerazionale = null

        caratteristicaTipologiaService.salvaSoggettiModificati(d, soggetti)
        documentoDTOService.salvaDatiAggiuntivi(d, delibera)
        budgetDTOService.salvaBudget(listaBudget)
    }

    void aggiornaMaschera(Delibera d) {
        // per prima cosa controllo che l'utente abbia le competenze in lettura sul documento
        competenze = gestoreCompetenze.getCompetenze(d, true)
        competenze.lettura = competenze.lettura ?: forzaCompetenzeLettura
        if (!competenze.lettura) {
            delibera = null
            throw new AttiRuntimeException("L'utente ${springSecurityService.principal.username} non ha i diritti di lettura sull'atto con id ${d.id}")
        }

        if (hasDelegaValida()) {
            if (d.statoFirma == StatoFirma.DA_FIRMARE) {
                gestoreCompetenze.assegnaCompetenze(d, WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), new Attore(utenteAd4: springSecurityService.principal.soggetto.utenteAd4), true, true, false, null)
                competenze.modifica = true
            } else {
                // il documento è stato rimandato indietro: il delegante non ha più diritti di modifica
                competenze.modifica = false
                forzaCompetenzeLettura=false
            }
        }

        if (d.statoFirma == StatoFirma.IN_FIRMA || d.statoFirma == StatoFirma.FIRMATO_DA_SBLOCCARE) {
            competenze.modifica = false
            competenze.cancellazione = false
        }

        isLocked = tokenIntegrazioneService.isLocked(d)

        // calcolo i campi che devo proteggere in lettura
        campiProtetti = CampiDocumento.getMappaCampi(d.campiProtetti)

        riservatoModificabile = d.tipologiaDocumento.gestioneRiservato

		if (riservatoModificabile) {
			// verifico che l'utente possa gestire il riservato:
			riservatoModificabile = (!d.riservato || gestoreCompetenze.utenteCorrenteVedeRiservato(d));
		}
		//se l'utente non può vedere i riservati e la tipologia prevede questo controllo, allora non potrà visualizzare il folder contabilità
		if ((!riservatoModificabile) && (d.tipologiaDocumento.contabilitaCheckRiservato)) {
			visContabilita = false
		}
		else {
			visContabilita = true
		}

        unitaProponenteDesc = d.proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE).unitaSo4.descrizione;

        // aggiorno i dati del lock sul testo:
        testoLockato = (gestioneTesti.isTestoLockato(d))

        delibera = d.toDTO(["registroDelibera", "propostaDelibera.tipologia.caratteristicaTipologiaDelibera", "propostaDelibera.commissione", "propostaDelibera.delega.assessore", "oggettoSeduta.seduta", "iter.stepCorrente.cfgStep", "oggettoSeduta.delega.assessore", "testo", "oggettoSeduta.esito", "datiAggiuntivi"]);

        nomeModelloTesto = d.modelloTesto?.nome

        // calcolo il titolo
        titolo = (d.numeroDelibera > 0 ? "Delibera n. ${d.numeroDelibera}/${d.annoDelibera}" : "Proposta di Delibera" + (d.proposta.numeroProposta > 0 ? " n. ${d.proposta.numeroProposta}/${d.proposta.annoProposta}" : ""))

        // calcolo la posizione del flusso
        posizioneFlusso = d.iter?.stepCorrente?.cfgStep?.nome

        // calcolo i vari soggetti della delibera
        soggetti = caratteristicaTipologiaService.calcolaSoggettiDto(d);

        // verifico presenza notifiche
        isNotificaPresente = notificheService.isNotificaPresente(delibera.domainObject, springSecurityService.currentUser)

        // aggiorno i documenti collegati
        refreshListaDocumentiCollegati()

        // aggiorno i visti/pareri:
        refreshListaVisti()

        // aggiorno gli allegati:
        refreshListaAllegati()

        //carico la lista dei Firmatari Politici
        refreshListaFirmatariGruppoPolitico()

        // aggiorno le note di trasmissioni dello step precedente
        aggiornaNoteTrasmissionePrecedenti()

        // aggiorna i certificati
        refreshListaCertificati()

        // carico lo storico
        caricaStorico()

        // gestione contabilità
        contabilitaAbilitata = integrazioneContabilita.isAbilitata(d)
        if (contabilitaAbilitata) {
            conDocumentiContabili = integrazioneContabilita.isConDocumentiContabili(d)
            zulContabilita = integrazioneContabilita.getZul(d)
            aggiornaContabilita(d)

            BindUtils.postNotifyChange(null, null, this, "conDocumentiContabili")
            BindUtils.postNotifyChange(null, null, this, "zulContabilita")
        }
        zulContabilita = integrazioneContabilita.getZul(d)

        mostraRelata = relataAlbo && integrazioneAlbo.hasRelata(d)

        mostraNote = regolaCampoService.isBloccoVisibile(d, d.tipoOggetto, "NOTE")
        mostraStorico = regolaCampoService.isBloccoVisibile(d, d.tipoOggetto, "STORICO")

        isEstrattoPresente = datiAggiuntiviService.isDatoPresente(d, TipoDatoAggiuntivo.ESTRATTO);

        caricaListaBudget()

        BindUtils.postNotifyChange(null, null, this, "campiProtetti")
        BindUtils.postNotifyChange(null, null, this, "delibera")
        BindUtils.postNotifyChange(null, null, this, "competenze")
        BindUtils.postNotifyChange(null, null, this, "posizioneFlusso")
        BindUtils.postNotifyChange(null, null, this, "titolo")
        BindUtils.postNotifyChange(null, null, this, "testoLockato")
        BindUtils.postNotifyChange(null, null, this, "soggetti")
        BindUtils.postNotifyChange(null, null, this, "contabilitaAbilitata")
        BindUtils.postNotifyChange(null, null, this, "listaBudget")
    }

    @Command
    void apriDocumentoCollegato(@BindingParam("documentoCollegato") DocumentoCollegatoDTO documentoCollegato) {
        documentoCollegatoDTOService.apriDocumento(documentoCollegato)
    }

    @Command
    public void onDownloadRelata() {
        def map = integrazioneAlbo.getRelata(delibera.domainObject);
        DocumentoGenerico doc = new DocumentoGenerico();
        doc.TIPO_OGGETTO = "RELATA"
        doc.id = Long.parseLong(map.relata.id_documento)
        doc.idDocumentoEsterno = Long.parseLong(map.relata.id_documento)

        if (map.relata.data != null && map.relata.data != "") {
            doc.annoProtocollo = Integer.parseInt(map.relata.anno)
            doc.numeroProtocollo = Integer.parseInt(map.relata.numero)
            doc.dataNumeroProtocollo = new SimpleDateFormat("dd/MM/yyyy").parse(map.relata.data)
        }

        FileAllegatoGenerico fAllegato = new FileAllegatoGenerico();
        fAllegato.idFileEsterno = Long.parseLong(map.relata.id_oggetto_file)
        fAllegato.id = Long.parseLong(map.relata.id_oggetto_file)
        fAllegato.nome = map.relata.filename
        fAllegato.contentType = GestioneTestiService.getContentType(map.relata?.filename?.substring(map.relata.filename?.lastIndexOf(".") + 1))

        attiFileDownloader.downloadFileAllegato(doc, fAllegato)
    }


    /*
     * Metodi per la gestione del budget
     */
    private void caricaListaBudget() {
        if (budgetAbilitato) {
            if (gestioneFondi) {
                listaTipiBudget = TipoBudget.findAllByUnitaSo4AndAttivoAndValido(soggetti[TipoSoggetto.UO_PROPONENTE]?.unita?.domainObject, true, true, [sort: "titolo", order: "asc"]).toDTO()
            } else {
                listaTipiBudget = TipoBudget.findAllByAttivoAndValido(true, true, [sort: 'titolo', order: 'asc']).toDTO()
            }

            listaBudget = Budget.createCriteria().list() {
                eq("propostaDelibera.id", delibera.proposta.id)
                order("sequenza", "asc")
            }.toDTO(["tipoBudget"])

            listaCe4Conti = Ce4Conto.createCriteria().list() {
                order("descrizione", "asc")
            }

            listaCe4Fornitori = Ce4Fornitore.createCriteria().list() {
                order("ragioneSociale", "asc")
            }

            BindUtils.postNotifyChange(null, null, this, "listaBudget")
            BindUtils.postNotifyChange(null, null, this, "listaCe4Conti")
            BindUtils.postNotifyChange(null, null, this, "listaCe4Fornitori")
        }
    }

    @NotifyChange(["listaCe4Conti", "totalSize"])
    @Command
    void onRicercaCe4Conto(@BindingParam("search") String search) {
        activePage = 0
        filtroRicerca = search
        listaCe4Conti = loadCe4Conti()
        BindUtils.postNotifyChange(null, null, this, "totalSize")
        BindUtils.postNotifyChange(null, null, this, "listaCe4Conti")
    }

    @NotifyChange(["listaCe4Conti", "totalSize"])
    private PagedResultList loadCe4Conti() {
        PagedResultList elencoCe4Conti = Ce4Conto.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
            or {
                ilike("contoEsteso", "%" + filtroRicerca + "%")
                ilike("descrizione", "%" + filtroRicerca + "%")
            }
            order("descrizione", "asc")
        }
        totalSize = elencoCe4Conti.totalCount
        return elencoCe4Conti
    }

    @NotifyChange(["listaCe4Conti", "totalSize"])
    @Command
    void onPaginaCe4Conto() {
        listaCe4Conti = loadCe4Conti().toDTO()
        BindUtils.postNotifyChange(null, null, this, "totalSize")
        BindUtils.postNotifyChange(null, null, this, "listaCe4Conti")
    }

    @Command
    @NotifyChange(["listaBudget", "target"])
    void onSelectCe4Conto(
            @ContextParam(ContextType.TRIGGER_EVENT) SelectEvent event, @BindingParam("target") Component target, @BindingParam("ent") def ent, @BindingParam("sel") def sel) {
        // SOLO se ho selezionato un solo item
        if (event.getSelectedItems()?.size() == 1) {
            activePage = 0
            ent.contoEconomico = sel.value.contoEsteso
            //BindUtils.postNotifyChange(null, null, listaBudget, "*")
            BindUtils.postNotifyChange(null, null, this, "listaBudget")
            target?.parent.parent.close()
        }
    }

    @NotifyChange(["listaCe4Fornitori", "totalSize"])
    @Command
    void onRicercaCe4Fornitore(@BindingParam("search") String search) {
        activePage = 0
        filtroRicerca = search
        listaCe4Fornitori = loadCe4Fornitori()
        BindUtils.postNotifyChange(null, null, this, "totalSize")
        BindUtils.postNotifyChange(null, null, this, "listaCe4Fornitori")
    }

    @NotifyChange(["listaCe4Fornitori", "totalSize"])
    private PagedResultList loadCe4Fornitori() {
        PagedResultList elencoCe4Fornitori = Ce4Fornitore.createCriteria().list(max: pageSize, offset: pageSize * activePage) {
            or {
                ilike("contoFornitore", "%" + filtroRicerca + "%")
                ilike("ragioneSociale", "%" + filtroRicerca + "%")
            }
            order("ragioneSociale", "asc")
        }
        totalSize = elencoCe4Fornitori.totalCount
        return elencoCe4Fornitori
    }

    @NotifyChange(["listaCe4Fornitori", "totalSize"])
    @Command
    void onPaginaCe4Fornitore() {
        listaCe4Fornitori = loadCe4Fornitori().toDTO()
        BindUtils.postNotifyChange(null, null, this, "totalSize")
        BindUtils.postNotifyChange(null, null, this, "listaCe4Fornitori")
    }

    @Command
    @NotifyChange(["listaBudget", "target"])
    void onSelectCe4Fornitore(
            @ContextParam(ContextType.TRIGGER_EVENT) SelectEvent event, @BindingParam("target") Component target, @BindingParam("ent") def ent, @BindingParam("sel") def sel) {
        // SOLO se ho selezionato un solo item
        if (event.getSelectedItems()?.size() == 1) {
            activePage = 0
            ent.codiceFornitore = sel.value.contoFornitore
            BindUtils.postNotifyChange(null, null, this, "listaBudget")
            target?.parent.parent.close()
        }
    }

    @Command
    onAggiungiBudget() {
        Date now = new Date()
        Date mydate1 = new GregorianCalendar(now.year + 1900, Calendar.JANUARY, 1).time
        Date mydate2 = new GregorianCalendar(now.year + 1900, Calendar.DECEMBER, 31).time
        listaBudget.add(new BudgetDTO(id: -1, dataInizioValidita: mydate1, dataFineValidita: mydate2, propostaDelibera: delibera.proposta))
        //listaBudget.add(new BudgetDTO(id: -1, propostaDelibera: propostaDelibera))
        BindUtils.postNotifyChange(null, null, this, "listaBudget")
    }

    @Command
    onEliminaBudget(@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("budget") BudgetDTO budget) {
        Messagebox.show("Eliminare il budget selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                new org.zkoss.zk.ui.event.EventListener() {
                    void onEvent(Event e) {
                        if (Messagebox.ON_OK.equals(e.getName())) {
                            budgetDTOService.eliminaBudget(budget)
                            delibera.version = delibera.domainObject.version
                            DeliberaViewModel.this.caricaListaBudget()
                        }
                    }
                })
    }

    @Command
    public void onExportBudgetExcel() {

        DecimalFormat formatter = new DecimalFormat("#,###.00");
        def exportOptions

        def lista = listaBudget.collect { row ->
            [atto                : delibera.estremiAtto
             , tipoBudget        : row.tipoBudget?.titolo
             , importo           : row.importo ? formatter.format(row.importo) : ""
             , dataInizioValidita: row.dataInizioValidita?.format("dd/MM/yyyy") ?: ""
             , dataFineValidita  : row.dataFineValidita?.format("dd/MM/yyyy") ?: ""
             , contoEconomico    : row.contoEconomico
             , contoFornitore    : row.codiceFornitore
             , codiceProgetto    : row.codiceProgetto
             , approvato         : row.approvato ? "Si" : "No"]
        }

        if (!gestioneFondi) {
            exportOptions = [atto                : [label: 'Atto', index: 0, columnType: 'TEXT']
                             , tipoBudget        : [label: 'Budget', index: 1, columnType: 'TEXT']
                             , importo           : [label: 'Importo', index: 2, columnType: 'TEXT']
                             , dataInizioValidita: [label: 'Data Inizio Validità', index: 3, columnType: 'TEXT']
                             , dataFineValidita  : [label: 'Data Fine Validità', index: 4, columnType: 'TEXT']
                             , contoEconomico    : [label: 'Conto Economico', index: 5, columnType: 'TEXT']
                             , contoFornitore    : [label: 'Conto Fornitore', index: 6, columnType: 'TEXT']
                             , codiceProgetto    : [label: 'Codice Progetto', index: 7, columnType: 'TEXT']
                             , approvato         : [label: 'Approvato', index: 8, columnType: 'TEXT']]
        } else {
            exportOptions = [atto            : [label: 'Atto', index: 0, columnType: 'TEXT']
                             , tipoBudget    : [label: 'Budget', index: 1, columnType: 'TEXT']
                             , importo       : [label: 'Importo', index: 2, columnType: 'TEXT']
                             , contoEconomico: [label: 'Conto Economico', index: 3, columnType: 'TEXT']
                             , codiceProgetto: [label: 'Codice Progetto', index: 4, columnType: 'TEXT']]
        }

        try {
            exportService.downloadExcel(exportOptions, lista)
        } finally {
            // todo
        }
    }

    @Command
    onAggiungiDocumentoCollegato() {
        Window w = Executions.createComponents("/commons/popupAnnullamentoIntegrazione.zul", self, [tipoDocumento: Delibera.TIPO_OGGETTO])
        w.onClose { event ->
            if (event != null && event?.data != null) {
                DeliberaDTO d = documentoCollegatoDTOService.aggiungiDocumentiCollegati(delibera, event.data)
                delibera.version = d.version
                refreshListaDocumentiCollegati()
            }
        }
        w.doModal()
    }

	@Command
	void onEliminaDocumentoCollegato (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("documentoCollegato") def documentoCollegato) {
		Messagebox.show("Eliminare il collegamento selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					void onEvent(Event e) {
						if (Messagebox.ON_OK.equals(e.getName())) {
							documentoCollegatoDTOService.eliminaDocumentoCollegato (delibera, documentoCollegato.id)
							delibera.version = delibera.domainObject.version;
							DeliberaViewModel.this.refreshListaDocumentiCollegati()
						}
					}
				}
		)
	}

	private boolean hasDelegaValida() {
		return delegante != null && !so4DelegaService.getDeleghe(springSecurityService.currentUser, delegante).isEmpty()
	}
}
