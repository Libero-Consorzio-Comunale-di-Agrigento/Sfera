package atti.documenti

import grails.orm.PagedResultList
import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.AbstractViewModel
import it.finmatica.atti.IProtocolloEsterno
import it.finmatica.atti.IntegrazioneAlbo
import it.finmatica.atti.IntegrazioneContabilita
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.commons.DocumentoGenerico
import it.finmatica.atti.commons.FileAllegatoGenerico
import it.finmatica.atti.commons.FileAllegatoStorico
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.contabilita.MovimentoContabile
import it.finmatica.atti.dizionari.Categoria
import it.finmatica.atti.dizionari.DatiAggiuntiviService
import it.finmatica.atti.dizionari.ImpattoGenerazionale
import it.finmatica.atti.dizionari.OggettoRicorrente
import it.finmatica.atti.dizionari.TipoBudget
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.TipoDatoAggiuntivo
import it.finmatica.atti.documenti.beans.AttiFileDownloader
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.competenze.DeterminaCompetenze
import it.finmatica.atti.documenti.tipologie.GestioneTestiModelloCompetenza
import it.finmatica.atti.documenti.tipologie.TipoDetermina
import it.finmatica.atti.documenti.tipologie.TipoDeterminaCompetenza
import it.finmatica.atti.documenti.viste.RicercaSiav
import it.finmatica.atti.dto.documenti.*
import it.finmatica.atti.dto.documenti.tipologie.TipoDeterminaDTO
import it.finmatica.atti.dto.dizionari.ImpattoGenerazionaleDTO
import it.finmatica.atti.dto.documenti.viste.So4DelegaService
import it.finmatica.atti.dto.integrazioni.JConsLogConservazioneDTO
import it.finmatica.atti.dto.integrazioni.Ce4FornitoreDTO
import it.finmatica.atti.dto.integrazioni.Ce4ContoDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.export.ExportService
import it.finmatica.atti.impostazioni.*
import it.finmatica.atti.integrazioni.CasaDiVetroService
import it.finmatica.atti.integrazioni.ConservazioneService
import it.finmatica.atti.integrazioni.Ce4Conto
import it.finmatica.atti.integrazioni.Ce4Fornitore
import it.finmatica.atti.integrazioni.pitre.PiTreField
import it.finmatica.atti.integrazioni.pitre.PiTreTemplate
import it.finmatica.atti.integrazioni.pnd.SmartPnd
import it.finmatica.atti.integrazioni.protocollo.ProtocolloPiTre
import it.finmatica.atti.integrazioni.protocollo.ProtocolloPiTreConfig
import it.finmatica.dto.DTO
import it.finmatica.gestioneiter.Attore
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestionetesti.TipoFile
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.so4.login.detail.UnitaOrganizzativa
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.apache.commons.io.FilenameUtils
import org.apache.log4j.Logger
import org.hibernate.FetchMode
import org.hibernate.criterion.CriteriaSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.zkoss.bind.BindContext
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.media.Media
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.event.SelectEvent
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.ListModelList
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.gestionetesti.ui.dizionari.GestioneTestiModelloDTOService

import java.text.DecimalFormat
import java.text.SimpleDateFormat

import static it.finmatica.zkutils.LabelUtils.getLabel

class DeterminaViewModel extends AbstractViewModel<Determina> {

    private static final Logger log = Logger.getLogger(DeterminaViewModel.class)

    // services
    AttiGestoreCompetenze          gestoreCompetenze
    AttiFileDownloader             attiFileDownloader
    CaratteristicaTipologiaService caratteristicaTipologiaService
    IntegrazioneContabilita        integrazioneContabilita
    CasaDiVetroService             casaDiVetroService
    ConservazioneService           conservazioneService
    ExportService                  exportService

    DeterminaDTOService            determinaDTOService
    DocumentoDTOService            documentoDTOService
    AllegatoDTOService             allegatoDTOService
    VistoParereDTOService          vistoParereDTOService
    VistoParereService             vistoParereService
    DestinatarioNotificaDTOService destinatarioNotificaDTOService
    DocumentoCollegatoDTOService   documentoCollegatoDTOService
    NotificheService               notificheService
    RegolaCampoService             regolaCampoService
    IntegrazioneAlbo               integrazioneAlbo
    DocumentoService               documentoService
    TokenIntegrazioneService       tokenIntegrazioneService
    DatiAggiuntiviService          datiAggiuntiviService
    BudgetDTOService               budgetDTOService
    GestioneTestiModelloDTOService gestioneTestiModelloDTOService
    So4DelegaService               so4DelegaService

    SmartPnd                       smartPnd

    ProtocolloPiTre protocolloPiTre

    // componenti
    Window popupCambiaTipologia

    // dati
    def                   listaTipologie
    DeterminaDTO          determina
    VistoParereDTO        visto
    List<VistoParereDTO>  listaVisti
    List                  listaCertificati
    def                   listaDestinatariInterni
    def                   listaDestinatariEsterni
    def                   listaDestinatariPiTre
    def                   listaAllegati
    def                   listaDocumentiCollegati
    def                   listaMacrocategorie
    def                   listaModelliTesto
    def                   storico
    def                   listaCategorie
    def                   listaBudget
    def                   listaTipiBudget
    List<Ce4ContoDTO>     listaCe4Conti
    List<Ce4FornitoreDTO> listaCe4Fornitori
    boolean               categoriaAbilitata
    boolean               isNotificaPresente
    boolean               incaritatoAbilitato
    boolean               isEstrattoPresente
    boolean               budgetAbilitato

    IProtocolloEsterno       protocolloEsterno

    private DeterminaNumero _numerazioneCommittataCache
    private boolean         _numerazioneCommittataCacheLoaded = false

    // mappa dei soggetti
    Map<String, it.finmatica.atti.zk.SoggettoDocumento> soggetti = [:]

    // stato
    def    competenze
    String posizioneFlusso
    def    campiProtetti
    String urlCasaDiVetro

    boolean firmaRemotaAbilitata
    boolean abilitaNoteContabili
    boolean riservatoVisibile
    boolean riservatoAbilitato
    boolean abilitaDestinatariEsterni
    boolean abilitaDestinatariInterni

    boolean abilitaDestinatari
    boolean destinatariInterniObbligatori = false
    boolean abilitaPubblicazioneProposte
    boolean abilitaPubblicazioneFinoRevoca
    boolean abilitaRichiestaEsecutivita
    boolean abilitaPriorita
    boolean riservatoModificabile
    boolean visContabilita = true //indica se l'utente può visualizzare il folder della contabilità
    boolean mostraArchiviazioni
    boolean mostraCorteConti
    boolean mostraDatiTesoriere
    boolean mostraDataScadenzaDisposizioni = false
    boolean mostraImportoTotale = false
    boolean mostraNote                    = true
    boolean mostraStorico                 = true
    boolean mostraEseguibilitaImmediata
    boolean modelloTestoUnitaProponente
    boolean mostraComboModelloTesti = true
    boolean mostraRiflessiContabili = false
    boolean mostraDataControlloGestione = false
    boolean mostraAttoSoggettoControllo = false
    boolean mostraMacrocategoria = false
    boolean gestioneFondi
    boolean relataAlbo

    // paginazione integrazione ce4
    int    activePage = 0
    int    pageSize   = 10
    int    totalSize  = 0
    String filtroRicerca

    // abilitazione del protocollo
    boolean protocollo
    boolean classifica_obb
    boolean fascicolo_obb

    // gestione del testo
    boolean testoLockato
    boolean lockPermanente

    // gestione delle note di trasmissione
    def     noteTrasmissionePrecedenti
    boolean attorePrecedente
    boolean mostraNoteTrasmissionePrecedenti

    // gestione contabilità
    boolean conDocumentiContabili = false
    boolean contabilitaAbilitata  = false

    // indica se nella lista dei visti si mostrano solo i visti validi o anche i non validi
    boolean mostraSoloVistiValidi = true;

    boolean controllaPriorita     = false
    boolean isMotivazionePresente = false

    String scadenzaTabLabel
    String zulContabilita

    // indica se abilitare la visualizzazione della relata di pubblicazione dell'albo
    boolean mostraRelata

    //PND per i destinatari gestita per la determina
    boolean  pndNotifica = false
    boolean  pndPagamento = false

    String tipoIntegrazioneContabilita
    boolean isIntegrazioneContabilitaAtsPavia = false

    // Impatto Generazionale
    boolean impattoGenerazionaleAbilitato
    List<ImpattoGenerazionaleDTO> listaImpattoGenerazionale
    ImpattoGenerazionaleDTO impattoGenerazionale
    boolean mostraImpattoGenerazionale = false

    // Protocollazione
    String tipoProtocollazione
    boolean isIntegrazioneProtocolloPiTre = false

    boolean isProtocollato = false

    // indica se è bloccato da un altro utente
    boolean isLocked = false

    // indica se il documento deve essere comunque aperto in lettura (delegato)
    boolean forzaCompetenzeLettura

    // utente per conto del quale si sta operando (delega So4)
    Ad4Utente delegante

    @NotifyChange(["determina", "competenze", "categoriaAbilitata", "mostraImpattoGenerazionale"])
    @Init
    init(
            @ContextParam(ContextType.COMPONENT) Window w,
            @ExecutionArgParam("id") Long idDetermina, @ExecutionArgParam("idDocumentoEsterno") Long idDocumentoEsterno, @ExecutionArgParam("competenzeLettura") Boolean competenzeLettura,
            @ExecutionArgParam("classificazione") String classificazione, @ExecutionArgParam("numeroFascicolo") String numeroFascicolo, @ExecutionArgParam("annoFascicolo") String annoFascicolo,
            @ExecutionArgParam("idPraticaSportello") String idPraticaSportello,
            @ExecutionArgParam("delegante") Ad4Utente delegante) {
        this.self = w

        firmaRemotaAbilitata            = Impostazioni.FIRMA_REMOTA.abilitato
        protocollo                      = Impostazioni.PROTOCOLLO_SEZIONE.abilitato
        classifica_obb                  = Impostazioni.PROTOCOLLO_CLASSIFICA_OBBL.abilitato
        fascicolo_obb                   = Impostazioni.PROTOCOLLO_FASCICOLO_OBBL.abilitato
        abilitaNoteContabili            = Impostazioni.NOTE_CONTABILI.abilitato
        riservatoVisibile               = Impostazioni.RISERVATO.abilitato
        abilitaDestinatariEsterni       = Impostazioni.DESTINATARI_ESTERNI.abilitato
        abilitaDestinatariInterni       = Impostazioni.DESTINATARI_INTERNI.abilitato
        abilitaDestinatari              = (abilitaDestinatariEsterni || abilitaDestinatariInterni)
        abilitaPubblicazioneProposte    = Impostazioni.PUBBLICAZIONE_VIS_PROP_DETERMINA.abilitato
        abilitaPubblicazioneFinoRevoca  = Impostazioni.PUBBLICAZIONE_FINO_REVOCA.abilitato
        abilitaRichiestaEsecutivita     = Impostazioni.RICHIESTA_ESECUTIVITA.abilitato
        abilitaPriorita                 = Impostazioni.PRIORITA.abilitato
        categoriaAbilitata              = Impostazioni.CATEGORIA_DETERMINA.abilitato
        mostraArchiviazioni             = Impostazioni.PROTOCOLLO_MOSTRA_SEZIONE_ARCHIVIAZIONI.abilitato
        mostraCorteConti                = Impostazioni.GESTIONE_CORTE_CONTI.abilitato
        mostraDatiTesoriere             = Impostazioni.DATI_TESORIERE.abilitato
        incaritatoAbilitato             = Impostazioni.INCARICATO.abilitato
        mostraEseguibilitaImmediata     = Impostazioni.ESEGUIBILITA_IMMEDIATA_DETE_ATTIVA.abilitato
        modelloTestoUnitaProponente     = Impostazioni.MODELLI_TESTO_UNITA_PROPONENTE.abilitato
        gestioneFondi                   = Impostazioni.GESTIONE_FONDI.abilitato
        budgetAbilitato                 = Impostazioni.GESTIONE_BUDGET.abilitato
        impattoGenerazionaleAbilitato   = Impostazioni.IMPATTO_GENERAZIONALE.abilitato
        scadenzaTabLabel                = Impostazioni.RICHIESTA_ESECUTIVITA_LABEL.valore
        tipoIntegrazioneContabilita     = Impostazioni.CONTABILITA.valore
        tipoProtocollazione             = Impostazioni.PROTOCOLLO.valore
        forzaCompetenzeLettura          = competenzeLettura
        this.delegante                  = delegante

        if (tipoIntegrazioneContabilita == "integrazioneContabilitaAtsPavia") {
            isIntegrazioneContabilitaAtsPavia = true
            BindUtils.postNotifyChange(null, null, this, "isIntegrazioneContabilitaAtsPavia")
        }

        if (tipoProtocollazione == "protocolloPiTre") {
            isIntegrazioneProtocolloPiTre = true
            BindUtils.postNotifyChange(null, null, this, "isIntegrazioneProtocolloPiTre")
        }

        if (idDetermina != null) {
            determina = new DeterminaDTO(id: idDetermina)
        } else {
            determina = Determina.findByIdDocumentoEsterno(idDocumentoEsterno).toDTO()
            idDetermina = determina.id
        }

        if (idDetermina > 0) {
            Determina d = Determina.get(idDetermina)
            aggiornaMaschera(d)
        } else {
            if (!springSecurityService.principal.hasRuolo(Impostazioni.RUOLO_SO4_CREA_DETERMINA.valore)) {
                throw new AttiRuntimeException("L'utente ${springSecurityService.principal.username} non ha i diritti di inserimento di una atto.")
            }

            //quando creo un nuovo atto, la lista dei modelli testi si deve vedere solo se NON attiva l'impostazione modelloTestoUnitaProponente
            if (modelloTestoUnitaProponente) {
                mostraComboModelloTesti = false
            }

            determina.dataProposta = new Date()
            competenze = [lettura: true, modifica: true, cancellazione: true]

            //se il protocollo è GDM provo a valorizzare classifica e fascicolo se passati nell'url
            if ("protocolloEsternoGdm".equals(tipoProtocollazione)) {
                if (classificazione != null) {
                    IProtocolloEsterno.Classifica classifica = protocolloEsterno.getClassifica(classificazione, determina.dataProposta)
                    if (classifica != null) {
                        determina.classificaCodice  = classifica.codice
                        determina.classificaDal = classifica.dal
                        determina.classificaDescrizione = classifica.descrizione

                        if ((annoFascicolo != null) && (numeroFascicolo != null)) {
                            IProtocolloEsterno.Fascicolo fascicolo = protocolloEsterno.getFascicolo(classifica, numeroFascicolo, Integer.parseInt(annoFascicolo))
                            if (fascicolo != null) {
                                determina.fascicoloAnno = fascicolo.anno
                                determina.fascicoloNumero = fascicolo.numero
                                determina.fascicoloOggetto = fascicolo.oggetto
                            }
                        }
                    }
                }

            }

            //memorizzo l'eventuale collegamento ad una pratica SURF
            if (idPraticaSportello) {
                DatoAggiuntivoDTO datoAggiuntivoDTO = new DatoAggiuntivoDTO()
                datoAggiuntivoDTO.codice = TipoDatoAggiuntivo.ID_PRATICA_SPORTELLO
                datoAggiuntivoDTO.valore = idPraticaSportello
                determina.addToDatiAggiuntivi(datoAggiuntivoDTO)
            }

            // in apertura della maschera, il redattore è l'utente corrente:
            As4SoggettoCorrente s = springSecurityService.principal.soggetto
            UnitaOrganizzativa uo = springSecurityService.principal.uo()[0]
            soggetti[TipoSoggetto.REDATTORE] = new it.finmatica.atti.zk.SoggettoDocumento(TipoSoggetto.get(TipoSoggetto.REDATTORE), s.utenteAd4, So4UnitaPubb.getUnita(uo.id, uo.ottica, uo.dal).get())

            listaVisti = []
        }

        listaTipologie = TipoDeterminaCompetenza.createCriteria().list() {
            projections {
                tipoDetermina {
                    groupProperty("id")
                    groupProperty("titolo")
                    groupProperty("descrizione")
                }
            }

            tipoDetermina {
                eq("valido", true)
                isNotNull("progressivoCfgIter")
            }

            AttiGestoreCompetenze.controllaCompetenze(delegate)(springSecurityService.principal)

            tipoDetermina { order("titolo") }
        }.collect { new TipoDeterminaDTO(id: it[0], titolo: it[1], descrizione: it[2]) }

        if(determina.tipologia!=null && isIntegrazioneProtocolloPiTre) {
            mostraAttoSoggettoControllo = protocolloPiTre.checkMostraControlliTemplate(PiTreField.ATTO_SOGGETTO_CONTROLLO, String.valueOf(determina.tipologia.id))
            mostraMacrocategoria = protocolloPiTre.checkMostraControlliTemplate(PiTreField.ATTO_SOGGETTO_CONTROLLO, String.valueOf(determina.tipologia.id))
            if(determina.attoSoggettoControllo){
                listaMacrocategorie = protocolloPiTre.loadValoriCampo(PiTreField.MACROCATEGORIA, String.valueOf(determina.tipologia.id))
            }else{
                listaMacrocategorie = protocolloPiTre.loadValoriCampo(PiTreField.MACROCATEGORIA_NO, String.valueOf(determina.tipologia.id))
            }
            BindUtils.postNotifyChange(null, null, this, "listaMacrocategorie")
            BindUtils.postNotifyChange(null, null, this, "mostraAttoSoggettoControllo")
            BindUtils.postNotifyChange(null, null, this, "mostraMacrocategoria")
        }

        mostraRiflessiContabili = TipoDatoAggiuntivo.isAbilitato(TipoDatoAggiuntivo.RIFLESSI_CONTABILI)

        if (determina.dataScadenza != null && determina.iter?.dataFine == null) {
            scadenzaTabLabel += " *";
        }

        aggiornaPulsanti()
        caricaListaCategorie()
        caricaListaBudget()
        caricaListaImpattoGenerazionale()
        abilitaDataControlloGestione()
        abilitaDataScadenzaDisposizioni()
        abilitaImportoTotale()
    }

    /*
     * Gestione scelta e cambio tipologia
     */
    @AfterCompose
    void afterCompose(@SelectorParam("#popupCambiaTipologia") Window popupTipologia) {
        this.popupCambiaTipologia = popupTipologia
        if (determina.tipologia == null) {
            popupCambiaTipologia.doModal()
        }
    }

    @Command
    onSelectTipologia() {
        if (determina.id <= 0) {
            determina.tipologia =
                    TipoDetermina.findById(determina.tipologia.id, [fetch: [caratteristicaTipologia: 'eager', oggettiRicorrenti: 'eager']]).toDTO()

            determina.controlloFunzionario = (determina.tipologia.funzionarioObbligatorio || Impostazioni.DEFAULT_FUNZIONARIO.abilitato)
            determina.giorniPubblicazione = determina.tipologia.giorniPubblicazione
            determina.pubblicaRevoca = determina.tipologia.pubblicazioneFinoARevoca
            determina.diventaEsecutiva = determina.tipologia.diventaEsecutiva
            determina.eseguibilitaImmediata = determina.tipologia.eseguibilitaImmediata
            determina.tipologia.caratteristicaTipologia = CaratteristicaTipologia.findById(determina.tipologia.caratteristicaTipologia.id, [fetch: [caratteristicheTipiSoggetto: 'eager']]).toDTO();

            calcolaSoggetti(TipoSoggetto.REDATTORE)
            aggiornaPulsanti()
            caricaListaModelloTesto()
            caricaListaImpattoGenerazionale()
            abilitaDataControlloGestione()
            abilitaDataScadenzaDisposizioni()
            abilitaImportoTotale()

            if(determina.tipologia!=null && isIntegrazioneProtocolloPiTre) {
                mostraAttoSoggettoControllo = protocolloPiTre.checkMostraControlliTemplate(PiTreField.ATTO_SOGGETTO_CONTROLLO, String.valueOf(determina.tipologia.id))
                mostraMacrocategoria = protocolloPiTre.checkMostraControlliTemplate(PiTreField.ATTO_SOGGETTO_CONTROLLO, String.valueOf(determina.tipologia.id))

                if(determina.attoSoggettoControllo){
                    listaMacrocategorie = protocolloPiTre.loadValoriCampo(PiTreField.MACROCATEGORIA, String.valueOf(determina.tipologia.id))
                }else{
                    listaMacrocategorie = protocolloPiTre.loadValoriCampo(PiTreField.MACROCATEGORIA_NO, String.valueOf(determina.tipologia.id))
                }


                BindUtils.postNotifyChange(null, null, this, "listaMacrocategorie")
                BindUtils.postNotifyChange(null, null, this, "mostraAttoSoggettoControllo")
                BindUtils.postNotifyChange(null, null, this, "mostraMacrocategoria")
            }

            BindUtils.postNotifyChange(null, null, this, "listaModelliTesto")
            BindUtils.postNotifyChange(null, null, this, "listaImpattoGenerazionale")
            BindUtils.postNotifyChange(null, null, this, "soggetti")
            BindUtils.postNotifyChange(null, null, this, "determina")

            this.popupCambiaTipologia.visible = false
        }
    }

    @NotifyChange(["listaMacrocategorie","macrocategoria"])
    @Command
    public void onCambioMacroCategoria() {
        if(determina.attoSoggettoControllo){
            listaMacrocategorie = protocolloPiTre.loadValoriCampo(PiTreField.MACROCATEGORIA, String.valueOf(determina.tipologia.id))
        }else{
            listaMacrocategorie = protocolloPiTre.loadValoriCampo(PiTreField.MACROCATEGORIA_NO, String.valueOf(determina.tipologia.id))
        }
        determina.macrocategoria=""

        BindUtils.postNotifyChange(null, null, this, "macrocategoria")
        BindUtils.postNotifyChange(null, null, this, "listaMacrocategorie")
    }

    void onCambiaTipologia(TipoDeterminaDTO tipologia) {
        determina.tipologia = tipologia
        determina.tipologia =
                TipoDetermina.findById(determina.tipologia.id, [fetch: [caratteristicaTipologia: 'eager', oggettiRicorrenti: 'eager']]).toDTO()

        if (determina.tipologia.funzionarioObbligatorio) {
            determina.controlloFunzionario = determina.tipologia.funzionarioObbligatorio
        } else {
            determina.controlloFunzionario = Impostazioni.DEFAULT_FUNZIONARIO.abilitato
        }
        determina.giorniPubblicazione = determina.tipologia.giorniPubblicazione
        determina.pubblicaRevoca = determina.tipologia.pubblicazioneFinoARevoca
        determina.diventaEsecutiva = tipologia.diventaEsecutiva
        determina.eseguibilitaImmediata = tipologia.eseguibilitaImmediata

        calcolaSoggetti(TipoSoggetto.REDATTORE)
        aggiornaPulsanti()
        caricaListaModelloTesto()
        
        determina = determinaDTOService.cambiaTipologia(determina)
        caricaListaImpattoGenerazionale()
        abilitaDataControlloGestione()
        abilitaDataScadenzaDisposizioni()
        abilitaImportoTotale()

        if(determina.tipologia!=null && isIntegrazioneProtocolloPiTre) {
            mostraAttoSoggettoControllo = protocolloPiTre.checkMostraControlliTemplate(PiTreField.ATTO_SOGGETTO_CONTROLLO, String.valueOf(determina.tipologia.id))
            mostraMacrocategoria = protocolloPiTre.checkMostraControlliTemplate(PiTreField.ATTO_SOGGETTO_CONTROLLO, String.valueOf(determina.tipologia.id))
            if(determina.attoSoggettoControllo){
                listaMacrocategorie = protocolloPiTre.loadValoriCampo(PiTreField.MACROCATEGORIA, String.valueOf(determina.tipologia.id))
            }else{
                listaMacrocategorie = protocolloPiTre.loadValoriCampo(PiTreField.MACROCATEGORIA_NO, String.valueOf(determina.tipologia.id))
            }
            BindUtils.postNotifyChange(null, null, this, "listaMacrocategorie")
            BindUtils.postNotifyChange(null, null, this, "mostraAttoSoggettoControllo")
            BindUtils.postNotifyChange(null, null, this, "mostraMacrocategoria")
        }

        BindUtils.postNotifyChange(null, null, this, "listaImpattoGenerazionale")
        BindUtils.postNotifyChange(null, null, this, "listaModelliTesto")
        BindUtils.postNotifyChange(null, null, this, "soggetti")
        BindUtils.postNotifyChange(null, null, this, "determina")
    }

    /*
     * Gestione dati di conservazione
     */
    JConsLogConservazioneDTO getLogConservazione() {
        return conservazioneService.getLastLog(determina.idDocumentoEsterno, determina.statoConservazione)
    }

    /*
     * Gestione Applet e download Testo
     */
    @NotifyChange(["testoLockato"])
    @Command
    editaTesto() {
        testoLockato = gestioneTesti.editaTesto(determina);
    }

    @Command
    onEliminaTesto() {
        gestioneTesti.eliminaTesto(determina, this)
    }

    @NotifyChange(["lockPermanente"])
    @Command
    onToggleLockPermanente() {
        lockPermanente = !lockPermanente;
    }

    @Command
    onUploadTesto(@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
        Media media = event.media
        String nomefile = media.name;

        // verifico i formati possibili del file.
        def formatiPossibili = ["pdf", "p7m"];
        if (!formatiPossibili.contains(FilenameUtils.getExtension(nomefile).toLowerCase())) {
            Clients.showNotification(
                    "Impossibile caricare il file: l'allegato è di un tipo non consentito, le estensioni consentite sono: ${formatiPossibili.join(", ")}.",
                    Clients.NOTIFICATION_TYPE_ERROR, self, "before_center", 3000, true);
            return;
        }

        // poi carico il file allegato
        gestioneTesti.uploadTestoManuale(determina.domainObject, media);

        Clients.showNotification("Testo caricato correttamente.", Clients.NOTIFICATION_TYPE_INFO, self, "before_center", 3000, true);
    }

    @Command
    onDownloadTesto() {
        Determina d = determina.domainObject
        attiFileDownloader.downloadFileAllegato(d, d.testo)
    }

    @Command
    onDownloadTestoStorico(
            @BindingParam("tipoOggetto") String tipoOggetto, @BindingParam("id") Long id, @BindingParam("idFileAllegato") Long idFileAllegato) {
        FileAllegatoStorico f = FileAllegatoStorico.get(idFileAllegato)
        attiFileDownloader.downloadFileAllegato(DocumentoFactory.getDocumentoStorico(id, tipoOggetto), f, true)
    }

    @Command
    onDownloadStampaUnica() {
        Determina d = determina.domainObject
        attiFileDownloader.downloadFileAllegato(d, d.stampaUnica)
    }

    /*
     * Gestione del Modello Testo
     */

    private void caricaListaModelloTesto() {
        listaModelliTesto = determinaDTOService.getListaModelliTestoAbilitati(determina.tipologia.id, springSecurityService.principal)
        if(Impostazioni.MODELLI_TESTO_UNITA_PROPONENTE.abilitato && determina.soggetti) {
            Determina domainObject = determina.getDomainObject()
            So4UnitaPubb uo = domainObject.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4
            if (uo) {
                List<GestioneTestiModelloDTO> toRemove= new ArrayList<GestioneTestiModelloDTO>()
                for (GestioneTestiModelloDTO gestioneTestiModelloDTO : listaModelliTesto) {
                    List<GestioneTestiModelloCompetenza> lista = GestioneTestiModelloCompetenza.createCriteria().list() {
                        eq("gestioneTestiModello.id", gestioneTestiModelloDTO.id)
                        createAlias ("unitaSo4", "unita", CriteriaSpecification.LEFT_JOIN)
                        ne("unita.progr",uo.progr)
                    }
                    if(lista!=null && lista.size()>0)
                        toRemove.add(gestioneTestiModelloDTO)
                }
                for(GestioneTestiModelloDTO elem: toRemove){
                    listaModelliTesto.removeElement(elem)
                }
            }
        }

        if (determina.modelloTesto == null) {
            determina.modelloTesto = determina?.tipologia?.modelloTesto?.getDomainObject()?.toDTO()
        }
    }

    private void abilitaDataControlloGestione() {
        mostraDataControlloGestione = TipoDatoAggiuntivo.isAbilitato(TipoDatoAggiuntivo.DATA_CONTROLLO_GESTIONE)
        if (mostraDataControlloGestione) {
            if (determina.tipologia) {
                mostraDataControlloGestione = determina.tipologia.visDataControlloGestione
            }
            else {
                mostraDataControlloGestione = false
            }
            BindUtils.postNotifyChange(null, null, this, "mostraDataControlloGestione")
        }
    }

    private void abilitaDataScadenzaDisposizioni() {
        mostraDataScadenzaDisposizioni = Impostazioni.DATA_SCADENZA_DISPOSIZIONI.abilitato
        if (mostraDataScadenzaDisposizioni) {
            if (determina.tipologia) {
                mostraDataScadenzaDisposizioni = determina.tipologia.visDataScadenzaDisposizione
            }
            else {
                mostraDataScadenzaDisposizioni = false
            }
            BindUtils.postNotifyChange(null, null, this, "mostraDataScadenzaDisposizioni")
        }
    }

    private void abilitaImportoTotale() {
        mostraImportoTotale = Impostazioni.GESTIONE_IMPORTO_TOTALE.abilitato
        if (mostraImportoTotale) {
            if (determina.tipologia) {
                mostraImportoTotale = determina.tipologia.visImportoTotale
            }
            else {
                mostraImportoTotale = false
            }
            BindUtils.postNotifyChange(null, null, this, "mostraImportoTotale")
        }
    }

    //visualizza o meno la lista degli impatti generazionali
    private void caricaListaImpattoGenerazionale() {
        mostraImpattoGenerazionale = false
        if (determina.tipologia) {
            if (impattoGenerazionaleAbilitato && determina.tipologia.impattoGenerazionale) {
                mostraImpattoGenerazionale = true

                //carico la lista degli impatti generazionali
                PagedResultList lista = ImpattoGenerazionale.createCriteria().list(max: 100, offset: 0) {
                    eq ("valido",true)
                    eq ("determina", true)
                    order ("titolo", "asc")
                    order ("descrizione", "asc")
                }
                listaImpattoGenerazionale = lista.toDTO()

                if (determina.impattoGenerazionale?.id > 0) {
                    impattoGenerazionale = ImpattoGenerazionale.get(determina.impattoGenerazionale.id).toDTO()
                }
                BindUtils.postNotifyChange(null, null, this, "listaImpattoGenerazionale")
            }
        }
        BindUtils.postNotifyChange(null, null, this, "mostraImpattoGenerazionale")
    }

    /*
     * Scelta dell'oggetto della determina
     */
    @Command
    onSceltaOggettoRicorrente() {
        def listaOggettiRicorrenti = Impostazioni.OGGETTI_RICORRENTI_TIPOLOGIE.abilitato ? determina.tipologia.oggettiRicorrenti : OggettoRicorrente.findAllByValidoAndDetermina(true, true).toDTO()
        Window w = Executions.createComponents("/atti/documenti/popupSceltaOggettoRicorrente.zul", self,
                [listaOggettiRicorrenti: listaOggettiRicorrenti, cancella: true])
        w.onClose { event ->
            if (event.data != null) {
                if (event.data.id > 0) {
                    determina.oggettoRicorrente = event.data
                    determina.oggetto = event.data.oggetto.toUpperCase()
                } else {
                    determina.oggettoRicorrente = null
                }
                BindUtils.postNotifyChange(null, null, this, "determina")
            }
        }
        w.doModal()
    }

    /*
     * Gestione dello storico:
     */
    private void caricaStorico() {
        storico = determinaDTOService.caricaStorico(determina);
        BindUtils.postNotifyChange(null, null, this, "storico")
    }

    /*
     * Gestione della contabilità
     */
    @Command
    void onAggiornaContabilita() {
        aggiornaContabilita(determina.domainObject)
    }

    void aggiornaContabilita(Determina d) {
        if (d != null) {
            integrazioneContabilita.aggiornaMaschera(d, (competenze.modifica && !(campiProtetti.CONTABILITA) && d.tipologia.scritturaMovimentiContabili))
        }
    }

    /*
     * Gestisce le note di trasmissioni
     */
    private void aggiornaNoteTrasmissionePrecedenti() {
        def result = documentoDTOService.getNoteTrasmissionePrecedenti(determina)
        noteTrasmissionePrecedenti = result.noteTrasmissionePrecedenti
        attorePrecedente = result.attorePrecedente
        mostraNoteTrasmissionePrecedenti = result.mostraNoteTrasmissionePrecedenti

        BindUtils.postNotifyChange(null, null, this, "mostraNoteTrasmissionePrecedenti")
        BindUtils.postNotifyChange(null, null, this, "noteTrasmissionePrecedenti")
        BindUtils.postNotifyChange(null, null, this, "attorePrecedente")
    }

    /*
     * Gestione Destinatari
     */
    @Command
    void onAggiungiDestinatariInterni() {
        Window w = Executions.createComponents("/commons/popupSceltaDestinatariInterni.zul", self,
                [destinatari: listaDestinatariInterni*.destinatario])
        w.onClose { event ->
            DeterminaDTO d = destinatarioNotificaDTOService.salvaDestinatariInterni(determina, event.data)
            determina.version = d.version
            refreshListaDestinatariInterni()
        }
        w.doModal()
    }

    @Command
    void onAggiungiDestinatariEsterni() {
        Window w = Executions.createComponents("/commons/popupSceltaDestinatariEsterniEsistenti.zul", self, [destinatari: listaDestinatariEsterni, documento: determina.domainObject])
        w.onClose { event ->
            if (event.data == null) {
                return null
            }

            DeterminaDTO d = destinatarioNotificaDTOService.aggiungiDestinatarioEsterno(determina, event.data)
            determina.version = d.version
            refreshListaDestinatariEsterni()
        }
        w.doModal()
    }

    @Command
    void onAggiungiDestinatariPiTre() {
        Window w = Executions.createComponents("/atti/integrazioni/protocollo/popupSceltaDestinatariPiTre.zul", self, [destinatari: listaDestinatariPiTre, documento: determina.domainObject])
        w.onClose { event ->
            if (event.data == null) {
                return null
            }

            DeterminaDTO d = destinatarioNotificaDTOService.aggiungiDestinatarioPiTre(determina, event.data)
            determina.version = d.version
            refreshListaDestinatariPiTre()
        }
        w.doModal()
    }

    @Command
    void onEliminaDestinatarioNotifica(@ContextParam(ContextType.TRIGGER_EVENT) Event event
                                       , @BindingParam("destinatario") def destinatario
                                       , @BindingParam("tipo") String tipo) {
        Messagebox.show("Eliminare il destinatario selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                new org.zkoss.zk.ui.event.EventListener() {
                    void onEvent(Event e) {
                        if (Messagebox.ON_OK.equals(e.getName())) {
                            if (tipo == "E") {
                                destinatarioNotificaDTOService.eliminaDestinatarioNotifica(destinatario)
                                DeterminaViewModel.this.refreshListaDestinatariEsterni()
                            } else if (tipo == "I") {
                                destinatarioNotificaDTOService.eliminaDestinatarioNotifica(destinatario.destinatario)
                                DeterminaViewModel.this.refreshListaDestinatariInterni()
                            } else if (tipo == "P3") {
                                destinatarioNotificaDTOService.eliminaDestinatarioNotifica(destinatario)
                                DeterminaViewModel.this.refreshListaDestinatariPiTre()
                            }
                            determina.version = determina.domainObject.version
                        }
                    }
                })
    }

    @Command
    void onVisualizzaNotificaPND(@ContextParam(ContextType.TRIGGER_EVENT) Event event
                                       , @BindingParam("destinatario") def destinatario
                                       , @BindingParam("tipo") String tipo) {
        if (tipo == "E") {
            String url = smartPnd.getUrlComunicazione(destinatario.codiceEsterno)
            if (url)
                Executions.getCurrent().sendRedirect(url, "_blank")
        }
    }

    private void refreshListaDestinatariInterni() {
        listaDestinatariInterni = destinatarioNotificaDTOService.getListaDestinatariInterni(determina)
        BindUtils.postNotifyChange(null, null, this, "listaDestinatariInterni")
    }

    private void refreshListaDestinatariEsterni() {
        listaDestinatariEsterni = destinatarioNotificaDTOService.getListaDestinatariEsterni(determina)
        BindUtils.postNotifyChange(null, null, this, "listaDestinatariEsterni")
    }

    private void refreshListaDestinatariPiTre() {
        listaDestinatariPiTre = destinatarioNotificaDTOService.getListaDestinatariPiTre(determina)
        BindUtils.postNotifyChange(null, null, this, "listaDestinatariPiTre")
    }

    /*
     *  Gestione Visti
     */

    private void refreshListaVisti() {
        def visti = VistoParere.createCriteria().list {
            eq("determina.id", determina.id)

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
        }

        // alla fine, per ogni visto, controllo di avere le competenze in modifica:
        listaVisti = []
        for (VistoParere visto : visti) {
            VistoParereDTO dto = visto.toDTO();
            dto.competenzeInModifica = gestoreCompetenze.getCompetenze(visto)
            listaVisti << dto
        }

        BindUtils.postNotifyChange(null, null, this, "listaVisti")
    }

    @Command
    onModificaVistoParere(
            @ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("nuovo") boolean nuovo, @BindingParam("selected") def selected) {
        if (!nuovo && selected == null) {
            return
        }

        Window w = Executions.createComponents("/atti/documenti/visto.zul", self,
                [id: nuovo ? -1 : selected.id, documento: determina, tipodoc: "determina", competenzeLettura: forzaCompetenzeLettura])
        w.onClose {
            //potrei aver aggiornato la determina
            aggiornaMaschera(determina.domainObject)
            aggiornaPulsanti()
        }
        w.doModal()
    }

    @Command
    onEliminaVistoParere(@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("visto") VistoParereDTO visto) {
        Messagebox.show("Eliminare il visto selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                new org.zkoss.zk.ui.event.EventListener() {
                    void onEvent(Event e) {
                        if (Messagebox.ON_OK.equals(e.getName())) {
                            vistoParereDTOService.elimina(visto)
                            determina.version = determina.domainObject.version
                            DeterminaViewModel.this.refreshListaVisti()
                        }
                    }
                })
    }

    @NotifyChange("mostraSoloVistiValidi")
    @Command
    onMostraVistiValidi() {
        mostraSoloVistiValidi = !mostraSoloVistiValidi
        refreshListaVisti()
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
                eq("determina.id", determina.id)
                order("sequenza", "asc")
            }.toDTO(["tipoBudget"])
            BindUtils.postNotifyChange(null, null, this, "listaBudget")
        }
    }

    @NotifyChange(["listaCe4Conti", "totalSize"])
    @Command
    void onRicercaCe4Conto(@BindingParam("search") String search) {
        activePage = 0
        filtroRicerca = search
        listaCe4Conti = loadCe4Conti().toDTO()
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
        listaCe4Conti = loadCe4Conti()
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
        listaCe4Fornitori = loadCe4Fornitori().toDTO()
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
        listaCe4Fornitori = loadCe4Fornitori()
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
        listaBudget.add(new BudgetDTO(id: -1, dataInizioValidita: mydate1, dataFineValidita: mydate2, determina: determina))
        BindUtils.postNotifyChange(null, null, this, "listaBudget")
    }

    @Command
    onEliminaBudget(@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("budget") BudgetDTO budget) {
        Messagebox.show("Eliminare il budget selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                new org.zkoss.zk.ui.event.EventListener() {
                    void onEvent(Event e) {
                        if (Messagebox.ON_OK.equals(e.getName())) {
                            budgetDTOService.eliminaBudget(budget)
                            determina.version = determina.domainObject.version
                            DeterminaViewModel.this.caricaListaBudget()
                        }
                    }
                })
    }

    /*
     * 	Metodi per il calcolo dei Soggetti della determina
     */

    @Command
    onSceltaSoggetto(@BindingParam("tipoSoggetto") String tipoSoggetto, @BindingParam("categoriaSoggetto") String categoriaSoggetto) {
        Window w = Executions.createComponents("/atti/documenti/popupSceltaSoggetto.zul", self,
                [idCaratteristicaTipologia: determina.tipologia.caratteristicaTipologia.id
                 , documento              : determina
                 , soggetti               : soggetti
                 , tipoSoggetto           : tipoSoggetto
                 , categoriaSoggetto      : categoriaSoggetto])
        w.onClose { event ->
            // se ho annullato la modifica, non faccio niente:
            if (event.data == null) {
                return
            };

            // altrimenti aggiorno i soggetti.
            BindUtils.postNotifyChange(null, null, this, "soggetti")
            self.invalidate()
        }
        w.doModal()
    }

    private void calcolaSoggetti(@BindingParam("tipoSoggetto") String tipoSoggetto) {
        caratteristicaTipologiaService.aggiornaSoggetti(determina.tipologia.caratteristicaTipologia.id, determina.domainObject, soggetti, tipoSoggetto)
        BindUtils.postNotifyChange(null, null, this, "soggetti")
    }

    /*
     * Gestione allegati
     */

    @Command
    onModificaAllegato(
            @ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("nuovo") boolean nuovo, @BindingParam("selected") def selected) {
        // succede quando un utente fa "doppio click" sulla tabella vuota.
        if (!nuovo && selected == null) {
            return
        }

        Window w = Executions.createComponents("/atti/documenti/allegato.zul", self, [id: (nuovo ? -1 : selected.id), documento: determina, competenzeLettura: forzaCompetenzeLettura])
        w.onClose {
            if (!(determina.idDocumentoEsterno > 0)) {
                // potrei aver aggiornato la determina, quindi ne riprendo i numeri di versione e idDocumentoEsterno.
                Determina d = determina.domainObject
                determina.version = d.version
                determina.idDocumentoEsterno = d.idDocumentoEsterno
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
                            allegatoDTOService.elimina(allegato, determina)
                            determina.version = determina.domainObject.version;
                            DeterminaViewModel.this.refreshListaAllegati()
                        }
                    }
                }
        )
    }

    private void refreshListaAllegati() {
        listaAllegati = Allegato.createCriteria().list {
            eq("determina.id", determina.id)
            order("sequenza", "asc")
            order("titolo", "asc")
        }.toDTO(['fileAllegati'])
        BindUtils.postNotifyChange(null, null, this, "listaAllegati")
    }

    /*
     * Gestione DetermineCollegate
     */

    @Command
    onAggiungiDocumentoCollegato() {
        Window w = Executions.createComponents("/commons/popupAnnullamentoIntegrazione.zul", self, [tipoDocumento: Determina.TIPO_OGGETTO, codiceUoProponente: soggetti[TipoSoggetto.UO_PROPONENTE]?.unita?.codice])

        w.onClose { event ->
            if (event != null && event?.data != null) {
                DeterminaDTO d = documentoCollegatoDTOService.aggiungiDocumentiCollegati(determina, event.data)
                determina.version = d.version
                refreshListaDocumentiCollegati()
            }
        }
        w.doModal()
    }

    private void refreshListaDocumentiCollegati() {
        listaDocumentiCollegati = documentoCollegatoDTOService.getListaDocumentiCollegati(determina.domainObject)
        BindUtils.postNotifyChange(null, null, this, "listaDocumentiCollegati")
    }

    @Command
    void onEliminaDocumentoCollegato(
            @ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("documentoCollegato") def documentoCollegato) {
        Messagebox.show("Eliminare il collegamento selezionato?", "Attenzione!", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                new org.zkoss.zk.ui.event.EventListener() {
                    void onEvent(Event e) {
                        if (Messagebox.ON_OK.equals(e.getName())) {
                            documentoCollegatoDTOService.eliminaDocumentoCollegato(determina, documentoCollegato.id)
                            determina.version = determina.domainObject.version;
                            DeterminaViewModel.this.refreshListaDocumentiCollegati()
                        }
                    }
                })
    }

    @Command
    void apriDocumentoCollegato(@BindingParam("documentoCollegato") DocumentoCollegatoDTO documentoCollegato) {
        documentoCollegatoDTOService.apriDocumento(documentoCollegato)
    }

    /**
     * Quando l'utente seleziona il tab dei riferimenti, controllo che il documento sia in casa di vetro:
     */
    @NotifyChange("urlCasaDiVetro")
    @Command
    onApriTabRiferimenti() {
        // aggiorno l'url del documento in casa di vetro:
        urlCasaDiVetro = casaDiVetroService.getUrlDocumentoSePresente(determina);
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
                tipologia { property "titolo" }
                property("f.dataFirma")
                property("uf.nominativoSoggetto")
            }

            eq("determina.id", determina.id)

            order("dateCreated", "asc")
        }.collect { row -> [id: row[0], tipo: row[1], dataFirma: row[2], firmatario: row[3]] }
    }

    /*
     * Gestione popup protocollo
     */

    @NotifyChange('determina')
    @Command
    apriClassificazione() {
        String zul = protocolloEsterno.getZulClassifica()
        Window w = Executions.createComponents(zul, self,
                [codiceUoProponente: soggetti[TipoSoggetto.UO_PROPONENTE].unita.codice])
        w.onClose { event ->
            if (event.data) {
                if (event.data.codice != determina.classificaCodice) {
                    determina.fascicoloAnno = 0
                    determina.fascicoloNumero = null
                    determina.fascicoloOggetto = null
                }
                determina.classificaCodice = event.data.codice
                determina.classificaDescrizione = event.data.descrizione
                determina.classificaDal = event.data.dal
                BindUtils.postNotifyChange(null, null, this, "determina")
            }
        }
        w.doModal()
    }

    @NotifyChange('determina')
    @Command
    apriFascicoli() {
        String zul = protocolloEsterno.getZulFascicolo()
        Window w = null
        if(isIntegrazioneProtocolloPiTre) {
            So4UnitaPubb unitaProponente= soggetti[TipoSoggetto.UO_PROPONENTE].unita?.domainObject
            if(unitaProponente) {
                w = Executions.createComponents(zul, self, [unitaProponente: unitaProponente, tipologia: String.valueOf(determina.tipologia.id),sceltaUnitaProponente:false, utenteRedattore :soggetti[TipoSoggetto.REDATTORE].utente?.domainObject])
            }else{
                throw new AttiRuntimeException("Attenzione: è necessario selezionare una unità proponente.")
            }
        }  else {
            w = Executions.createComponents(zul, self, [classificaCodice: determina.classificaCodice, classificaDescrizione: determina.classificaDescrizione, classificaDal: determina.classificaDal, codiceUoProponente: soggetti[TipoSoggetto.UO_PROPONENTE].unita.codice])
        }
        w.onClose { event ->
            if (event.data) {
                if(isIntegrazioneProtocolloPiTre){
                    determina.fascicoloNumero = event.data.numero
                    determina.fascicoloAnno = ProtocolloPiTre.estraiAnnoDaFascicoloPiTre(event.data.numero)
                    determina.fascicoloOggetto = event.data.oggetto
                    determina.idFascicoloEsterno = event.data.sub
                }else {

                    // se ho cambiato la classificazione, la riaggiorno
                    if (event.data.classifica.codice != determina.classificaCodice) {
                        determina.classificaCodice = event.data.classifica.codice
                        determina.classificaDescrizione = event.data.classifica.descrizione
                        determina.classificaDal = event.data.classifica.dal
                    }
                    determina.fascicoloAnno = event.data.anno
                    determina.fascicoloNumero = event.data.numero
                    determina.fascicoloOggetto = event.data.oggetto
                }
                BindUtils.postNotifyChange(null, null, this, "determina")
            }
        }
        w.doModal()
    }

    /*
     * Gestione delle categorie
     */

    private void caricaListaCategorie() {
        if (categoriaAbilitata) {
            listaCategorie = Categoria.createCriteria().list() {
                eq("tipoOggetto", Categoria.TIPO_OGGETTO_DETERMINA)
                or {
                    eq("valido", true)
                    if (determina?.categoria?.id) {
                        eq("id", determina.categoria.id)
                    }
                }
                order("sequenza", "asc")
                order("codice", "asc")
            }.toDTO()
            BindUtils.postNotifyChange(null, null, this, "listaCategorie")
        }

        BindUtils.postNotifyChange(null, null, this, "categoriaAbilitata")
    }

    /*
     *  Gestione Chiusura Maschera
     */

    @Command
    onChiudi() {
        // se devo rilasciare il lock sul testo, lo rilascio.
        gestioneTesti.uploadEUnlockTesto(determina, lockPermanente);
        tokenIntegrazioneService.unlockDocumento(determina.domainObject)
        Events.postEvent(Events.ON_CLOSE, self, null)
    }

    /*
     *  Presa Visione
     */

    @Command
    onPresaVisione() {
        notificheService.eliminaNotifica(determina.domainObject, springSecurityService.currentUser)
        isNotificaPresente = false
        BindUtils.postNotifyChange(null, null, this, "isNotificaPresente")
        onChiudi()
    }

    @Command
    onChiudiPopup() {
        if (determina.id > 0) {
            // vuol dire che ho cambiato la tipologia allora chiudo solo la popup della tipologia
            popupCambiaTipologia.visible = false
            Events.postEvent(Events.ON_CLOSE, this.popupCambiaTipologia, null)
        } else {
            // altrimenti ho messo annulla durante la creazione
            Events.postEvent(Events.ON_CLOSE, self, null)
        }
    }

    /*
     * Gestione della priorità
     */

    @Command
    onChangePriorita(@BindingParam("valore") String valore) {
        if ((determina.priorita == null || determina.priorita == 0) && Integer.parseInt(valore) > 0) {
            controllaPriorita = true
        }
        determina.priorita = Integer.parseInt(valore)
        BindUtils.postNotifyChange(null, null, this, "priorita")
    }

    /*
     * Implementazione dei Metodi per AbstractViewModel
     */

    DTO<Determina> getDocumentoDTO() {
        return determina
    }

    @Override
    WkfCfgIter getCfgIter() {
        return WkfCfgIter.getIterIstanziabile(determina?.tipologia?.progressivoCfgIter ?: ((long) -1)).get()
    }

    Determina getDocumentoIterabile(boolean controllaConcorrenza) {
        if (determina.id > 0) {
            Determina domainObject = determina.getDomainObject()
            if (controllaConcorrenza && determina?.version >= 0 && domainObject.version != determina?.version) {
                throw new AttiRuntimeException(
                        "Attenzione: un altro utente ha modificato il documento su cui si sta lavorando. Impossibile continuare. \n (dto.version=${determina.version}!=domain.version=${domainObject.version})")
            }

            return domainObject
        }

        return new Determina()
    }

    Collection<String> validaMaschera() {
        def messaggi = []

        if (determina.oggetto == null || determina.oggetto.trim().length() == 0) {
            messaggi << "L'Oggetto è obbligatorio."
        } else {
            determina.oggetto = AttiUtils.replaceCaratteriSpeciali(determina.oggetto)
        }

        if (determina.oggetto != null && !AttiUtils.controllaCharset(determina.oggetto)) {
            messaggi << "L'Oggetto contiene dei caratteri non supportati."
        }

        if (determina.oggetto != null && determina.oggetto.size() > Impostazioni.LUNGHEZZA_OGGETTO.valoreInt) {
            messaggi << "La lunghezza dell'oggetto inserito è superiore a " + Impostazioni.LUNGHEZZA_OGGETTO.valore + " caratteri"
        }

        if (classifica_obb && determina.classificaCodice == null && fascicolo_obb && determina.fascicoloNumero == null) {
            messaggi << "Classifica e Fascicolo Obbligatori"
        } else {
            if (classifica_obb && determina.classificaCodice == null) {
                messaggi << "Classifica Obbligatoria"
            }
            if (fascicolo_obb && determina.fascicoloNumero == null) {
                messaggi << "Fascicolo Obbligatorio"
            }
        }

        boolean funzionarioPrevisto = determina.tipologia.caratteristicaTipologia.caratteristicheTipiSoggetto?.any {
            it.tipoSoggetto.codice == TipoSoggetto.FUNZIONARIO
        }
        //if (!('/atti/documenti/determina/determina_redattore_dirigente.zul'.equals(determina.tipologia.caratteristicaTipologia.layoutSoggetti)))
        if (funzionarioPrevisto && determina.controlloFunzionario && soggetti[TipoSoggetto.FUNZIONARIO]?.utente == null) {
            messaggi << Labels.getLabel("message.determina.funzionario")
        }

        if (determina.categoria == null && determina.tipologia.categoriaObbligatoria == true) {
            messaggi << Labels.getLabel("message.determina.categoria")
        }

        if (determina.tipologia.codiceGara && determina.codiceGara?.length() > 0 && determina.codiceGara?.length() < 10) {
            messaggi << "Il Codice Identificativo Gara deve contenere 10 caratteri"
        } else if (determina.codiceGara?.indexOf(" ")>-1) {
            messaggi << "Il Codice Identificativo Gara non deve contenere spazi"
        } else if (determina.annoProposta > 0 && determina.numeroProposta > 0) {
            if (determina.tipologia.codiceGara && determina.tipologia.codiceGaraObbligatorio && (determina.codiceGara == null || determina.codiceGara?.length() == 0)) {
                messaggi << "Codice Identificativo Gara obbligatorio"
            }
        }

        if (determina.priorita > 0 && (controllaPriorita || isMotivazionePresente) && (determina.motivazione == null || determina.motivazione.isEmpty())) {
            messaggi << "Motivazione obbligatoria per documenti con Priorità"
        }

        if (determina.tipologia?.caratteristicaTipologia?.caratteristicheTipiSoggetto?.tipoSoggetto*.codice?.contains(TipoSoggetto.INCARICATO) &&
                determina.tipologia.incaricatoObbligatorio && soggetti[TipoSoggetto.INCARICATO]?.utente == null) {
            messaggi << Labels.getLabel("message.determina.incaricato");
        }

        String doppiaFirma = TipoDatoAggiuntivo.getValore(determina.datiAggiuntivi, TipoDatoAggiuntivo.ASSENZA_DOPPIA_FIRMA)
        String motivazioni = TipoDatoAggiuntivo.getValore(determina.datiAggiuntivi, TipoDatoAggiuntivo.MOTIVAZIONE_ASSENZA_DOPPIA_FIRMA)
        if (TipoDatoAggiuntivo.isAbilitato(TipoDatoAggiuntivo.ASSENZA_DOPPIA_FIRMA) && doppiaFirma == 'Y' && !(motivazioni?.length() > 0)) {
            messaggi << Labels.getLabel("message.motivazioniDoppiaFirmaMancante")
        }
        if (determina.tipologia.cupVisibile && TipoDatoAggiuntivo.isAbilitato(TipoDatoAggiuntivo.CUP)) {
            String cup = TipoDatoAggiuntivo.getValore(determina.datiAggiuntivi, TipoDatoAggiuntivo.CUP)
            if (determina.tipologia.cupObbligatorio && !(cup?.trim()?.length() > 0)) {
                messaggi << Labels.getLabel("message.motivazioniCUPMancante")
            }
            if ((cup?.trim()?.length() > 0) && !(cup?.trim().length() == 15)) {
                messaggi << Labels.getLabel("message.motivazioniCUPErrato")
            }
        }

        if (mostraImportoTotale && determina.tipologia.importoTotaleObbligatorio && determina.importoTotale == null) {
            messaggi << "Importo Totale obbligatorio"
        }

        if (Impostazioni.ESEGUIBIILITA_IMMEDIATA_DETE_MOTIVAZIONI.abilitato && determina.eseguibilitaImmediata && (determina.motivazioniEseguibilita == null || determina.motivazioniEseguibilita?.isEmpty())) {
            messaggi << "Motivazione obbligatoria per documenti con Eseguibilità Immediata";
        }

        if(isIntegrazioneProtocolloPiTre && mostraMacrocategoria && (determina.macrocategoria==null || determina.macrocategoria.length()==0)){
            messaggi << "E' obbligatorio inserire la macroCategoria";
        }

        if (mostraDataScadenzaDisposizioni && determina.dataScadenzaDisposizioni == null) {
            messaggi << "E' obbligatorio inserire la data di scadenza delle disposizioni";
        }

        if (messaggi.size() > 0) {
            messaggi.add(0, "Impossibile continuare:")
        }

        return messaggi
    }

    void aggiornaDocumentoIterabile(Determina d) {
        // salvo e sblocco il testo
        gestioneTesti.uploadEUnlockTesto(d)

        d.oggetto = determina.oggetto.toUpperCase()
        d.tipologia = determina.tipologia?.domainObject
        d.codiceGara = determina.codiceGara?.toUpperCase()

        // se ho modificato la uo proponente della determina, devo aggiornarla anche per gli eventuali visti
        if (soggetti[TipoSoggetto.UO_PROPONENTE] != null && d.id > 0) {
            vistoParereService.allineaUnitaDocumentoPrincipale(d, soggetti[TipoSoggetto.UO_PROPONENTE].unita?.domainObject)
        }

        // se ho modificato il dirigente della determina, devo aggiornare anche il dirigente dell'eventuale visto
        if (soggetti[TipoSoggetto.DIRIGENTE] != null && d.id > 0) {
            vistoParereService.allineaFirmatarioDocumentoPrincipale(d, soggetti[TipoSoggetto.DIRIGENTE].utente?.domainObject)
        }

        d.controlloFunzionario = determina.controlloFunzionario
        d.giorniPubblicazione = determina.giorniPubblicazione
        d.pubblicaRevoca = determina.pubblicaRevoca
        d.statoOdg = StatoOdg.INIZIALE
        d.daInviareCorteConti = determina.daInviareCorteConti
        if (d.daInviareCorteConti) {
            d.dataInvioCorteConti = determina.dataInvioCorteConti
        } else {
            d.dataInvioCorteConti = null
        }

        d.fascicoloAnno = determina.fascicoloAnno
        d.fascicoloNumero = determina.fascicoloNumero
        d.fascicoloOggetto = determina.fascicoloOggetto
        d.idFascicoloEsterno = determina.idFascicoloEsterno
        d.attoSoggettoControllo=determina.attoSoggettoControllo
        d.macrocategoria=determina.macrocategoria
        d.classificaCodice = determina.classificaCodice
        d.classificaDal = determina.classificaDal
        d.classificaDescrizione = determina.classificaDescrizione
        d.riservato = determina.riservato
        d.dataProposta = determina.dataProposta
        d.note = determina.note
        d.noteTrasmissione = determina.noteTrasmissione
        d.noteContabili = determina.noteContabili
        d.modelloTesto = determina?.modelloTesto?.domainObject
        d.categoria = determina?.categoria?.domainObject
        d.dataScadenza = determina?.dataScadenza
        d.motivazione = determina?.motivazione
        d.priorita = determina?.priorita
        d.diventaEsecutiva = determina?.diventaEsecutiva
        d.inviatoTesoriere = determina?.inviatoTesoriere
        d.noteTesoriere = determina?.noteTesoriere
        if (determina?.numeroProtTesoriere) {
            d.numeroProtTesoriere = determina.numeroProtTesoriere
        }
        d.dataProtTesoriere = determina.dataProtTesoriere
        d.oggettoRicorrente = determina?.oggettoRicorrente?.domainObject
        d.dataMinimaPubblicazione = determina?.dataMinimaPubblicazione
        d.eseguibilitaImmediata = determina?.eseguibilitaImmediata
        d.motivazioniEseguibilita = determina?.motivazioniEseguibilita
        d.controllaDestinatari = determina?.controllaDestinatari

        d.inEvidenza = determina?.inEvidenza
        d.dataInEvidenzaDa = determina?.dataInEvidenzaDa
        d.dataInEvidenzaA = determina?.dataInEvidenzaA
        d.importo = determina?.importo
        d.importoTotale = determina?.importoTotale
        d.dataScadenzaDisposizioni = determina?.dataScadenzaDisposizioni

        if(impattoGenerazionale?.id>0) {
            determina.impattoGenerazionale=impattoGenerazionale
            d.impattoGenerazionale = determina.impattoGenerazionale.domainObject
        } else
            d.impattoGenerazionale = null

        documentoService.controllaOggettoRicorrente(d)

        caratteristicaTipologiaService.salvaSoggettiModificati(d, soggetti)
        documentoDTOService.salvaDatiAggiuntivi(d, determina)
        budgetDTOService.salvaBudget(listaBudget)
    }

    void aggiornaMaschera(Determina d) {
        // per prima cosa controllo che l'utente abbia le competenze in lettura sul documento
        competenze = gestoreCompetenze.getCompetenze(d, true)
        competenze.lettura = competenze.lettura ?: forzaCompetenzeLettura

        if (!competenze.lettura) {
            determina = null
            throw new AttiRuntimeException(
                    "L'utente ${springSecurityService.principal.username} non ha i diritti di lettura sulla determina con id ${d.id}")
        }

        if (hasDelegaValida()) {
            if (d.statoFirma == StatoFirma.DA_FIRMARE) { // || d.statoFirma == StatoFirma.IN_FIRMA || d.statoFirma == StatoFirma.FIRMATO_DA_SBLOCCARE) {
                gestoreCompetenze.assegnaCompetenze(d, WkfTipoOggetto.get(Determina.TIPO_OGGETTO), new Attore(utenteAd4: springSecurityService.principal.soggetto.utenteAd4), true, true, false, null)
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
            // numeraDeterminaECommit può aggiornare un'istanza Hibernate diversa lasciando d stale
            if (d.statoFirma == StatoFirma.FIRMATO_DA_SBLOCCARE) {
                d.refresh()
            }
        }

        isLocked = tokenIntegrazioneService.isLocked(d)

        //il campo tipologia non si deve abilitare
        riservatoModificabile = d.tipologia.gestioneRiservato

        if (riservatoModificabile) {
            // verifico che l'utente possa gestire il riservato:
            riservatoModificabile = (!d.riservato || gestoreCompetenze.utenteCorrenteVedeRiservato(d));
        }

        //se l'utente non può vedere i riservati e la tipologia prevede questo controllo, allora non potrà visualizzare il folder contabilità
        if ((!riservatoModificabile) && (d.tipologia.contabilitaCheckRiservato)) {
            visContabilita = false
        }
        else {
            visContabilita = true
        }

        // calcolo la posizione del flusso (può esseree nullo per i documenti trascodificati)
        posizioneFlusso = d.iter?.stepCorrente?.cfgStep?.nome

        // calcolo i campi che devo proteggere in lettura
        campiProtetti = CampiDocumento.getMappaCampi(d.campiProtetti)

        // prendo il DTO con tutti i campi necessari
        determina = d.toDTO([
                "tipologia.caratteristicaTipologia",
                "tipologia.caratteristicaTipologia.caratteristicheTipiSoggetto.tipoSoggetto.codice",
                "categoria",
                "registroDetermina",
                "registroDetermina2",
                "modelloTesto", "testo", "datiAggiuntivi",
                "tipologia.oggettiRicorrenti", "oggettoRicorrente"
        ])

        // aggiorno i dati del lock sul testo
        testoLockato = gestioneTesti.isTestoLockato(d)

        // verifico presenza notifiche
        isNotificaPresente = notificheService.isNotificaPresente(d, springSecurityService.currentUser)
        isMotivazionePresente = determina.motivazione != null && !determina.motivazione.isEmpty()

        // carico la lista di allegati
        refreshListaAllegati()

        // carico la lista dei visti
        refreshListaVisti()

        // carico i certificati
        refreshListaCertificati()

        // carica la lista dei destinatari
        refreshListaDestinatariInterni()

        refreshListaDestinatariEsterni()

        refreshListaDestinatariPiTre()

        // carica lista delle determine collegate
        refreshListaDocumentiCollegati()

        // aggiorno le note di trasmissioni dello step precedente
        aggiornaNoteTrasmissionePrecedenti()

        // aggiorno la lista dei modelli testo selezionabili
        caricaListaModelloTesto()

        // aggiorno lo storico
        caricaStorico()

        // calcolo i vari soggetti della determina
        soggetti = caratteristicaTipologiaService.calcolaSoggettiDto(d)

        isProtocollato = (determina.numeroProtocollo>0)

        // gestione contabilità
        contabilitaAbilitata = integrazioneContabilita.isAbilitata(d)
        if (contabilitaAbilitata) {
            conDocumentiContabili = integrazioneContabilita.isConDocumentiContabili(d)
            zulContabilita = integrazioneContabilita.getZul(d)

            aggiornaContabilita(d)

            BindUtils.postNotifyChange(null, null, this, "conDocumentiContabili")
            BindUtils.postNotifyChange(null, null, this, "zulContabilita")
        }

        mostraRelata = relataAlbo && integrazioneAlbo.hasRelata(d)

        mostraNote = regolaCampoService.isBloccoVisibile(d, d.tipoOggetto, "NOTE")
        mostraStorico = regolaCampoService.isBloccoVisibile(d, d.tipoOggetto, "STORICO")

        isEstrattoPresente = datiAggiuntiviService.isDatoPresente(d, TipoDatoAggiuntivo.ESTRATTO)

        pndNotifica = d.tipologia.pndNotifica
        if (pndNotifica)
            pndPagamento = smartPnd.isNotificaPagamento(d.tipologia.id)

        caricaListaBudget()

        //se ho il testo già memorizzato allora non devo visualizzare la combo di scelta dei modelli testi
        if (d.testo?.id) {
            mostraComboModelloTesti = false
        }
        else {
            mostraComboModelloTesti = true
        }

        _numerazioneCommittataCache = null
        _numerazioneCommittataCacheLoaded = false

        BindUtils.postNotifyChange(null, null, this, "campiProtetti")
        BindUtils.postNotifyChange(null, null, this, "determina")
        BindUtils.postNotifyChange(null, null, this, "numeroDeterminaEffettivo")
        BindUtils.postNotifyChange(null, null, this, "annoDeterminaEffettivo")
        BindUtils.postNotifyChange(null, null, this, "descrizioneRegistroDeterminaEffettivo")
        BindUtils.postNotifyChange(null, null, this, "competenze")
        BindUtils.postNotifyChange(null, null, this, "posizioneFlusso")
        BindUtils.postNotifyChange(null, null, this, "listaModelliTesto")
        BindUtils.postNotifyChange(null, null, this, "testoLockato")
        BindUtils.postNotifyChange(null, null, this, "soggetti")
        BindUtils.postNotifyChange(null, null, this, "isNotificaPresente")
        BindUtils.postNotifyChange(null, null, this, "contabilitaAbilitata")
        BindUtils.postNotifyChange(null, null, this, "listaBudget")
        BindUtils.postNotifyChange(null, null, this, "pndNotifica")
        BindUtils.postNotifyChange(null, null, this, "mostraComboModelloTesti")
    }

    @Command
    void onDownloadRelata() {
        def map = integrazioneAlbo.getRelata(determina.domainObject);
        DocumentoGenerico doc = new DocumentoGenerico()
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
        fAllegato.contentType = TipoFile.getInstanceByEstensione(fAllegato.estensione)

        attiFileDownloader.downloadFileAllegato(doc, fAllegato)
    }

    public def getProposta() {
        return determina
    }

    @Command
    public void onExportBudgetExcel() {

        DecimalFormat formatter = new DecimalFormat("#,###.00");
        def exportOptions

        def lista = listaBudget.collect { row ->
            [atto                : determina.domainObject.estremiAtto
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

    @NotifyChange(["importo"])
    @Command
    public void aggiornaTotaleImporto() {
        //per ora viene usata solo dall'integrazione con ATS Pavia dove non abbiamo distinzione tra ENTRATA e SPESA
        determina.importo = integrazioneContabilita.totaleImporto(determina.domainObject, null)
        BindUtils.postNotifyChange(null, null, this, "determina")
    }

    Integer getNumeroDeterminaEffettivo() {
        if (determina?.statoFirma == StatoFirma.FIRMATO_DA_SBLOCCARE && !(determina?.numeroDetermina > 0)) {
            Integer nc = caricaNumerazioneCommittata()?.numeroDetermina
            return (nc != null && nc > 0) ? nc : null
        }
        return determina?.numeroDetermina
    }

    Integer getAnnoDeterminaEffettivo() {
        if (determina?.statoFirma == StatoFirma.FIRMATO_DA_SBLOCCARE && !(determina?.numeroDetermina > 0)) {
            Integer ac = caricaNumerazioneCommittata()?.annoDetermina
            return (ac != null && ac > 0) ? ac : null
        }
        return determina?.annoDetermina
    }

    String getDescrizioneRegistroDeterminaEffettivo() {
        if (determina?.statoFirma == StatoFirma.FIRMATO_DA_SBLOCCARE && !(determina?.numeroDetermina > 0)) {
            return caricaNumerazioneCommittata()?.registroDetermina?.descrizione ?: ''
        }
        return determina?.registroDetermina?.descrizione ?: ''
    }

    private DeterminaNumero caricaNumerazioneCommittata() {
        if (!_numerazioneCommittataCacheLoaded) {
            _numerazioneCommittataCache = DeterminaNumero.findByDeterminaId(determina?.id)
            _numerazioneCommittataCacheLoaded = true
        }
        return _numerazioneCommittataCache
    }

    private boolean hasDelegaValida() {
        return delegante != null && !so4DelegaService.getDeleghe(springSecurityService.currentUser, delegante).isEmpty()
    }
}
