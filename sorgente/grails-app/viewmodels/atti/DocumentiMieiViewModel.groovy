package atti

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.*
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.documenti.viste.DocumentoStepDTOService
import it.finmatica.atti.export.ExportService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.odg.SedutaStampa
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.SortEvent
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class DocumentiMieiViewModel {

    // services
    SpringSecurityService   springSecurityService
    DocumentoStepDTOService documentoStepDTOService
    ExportService           exportService

    // componenti
    Window self

    // dati
    def lista
    def selected
    def listaAllegati

    List<TipoRegistroDTO> listaTipiRegistro
    TipoRegistroDTO       tipoRegistro

    def tipoOggetto
    def tipiOggetto = [[oggetti: null, nome: Labels.getLabel("tipoOggetto.tutti")]
                       , [oggetti: [Determina.TIPO_OGGETTO], nome: Labels.getLabel("tipoOggetto.determine")]
                       , [oggetti: [PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO], nome: Labels.getLabel("tipoOggetto.delibere")]
                       , [oggetti: [VistoParere.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO_PARERE], nome: Labels.getLabel("tipoOggetto.vistiEPareri")]
                       , [oggetti: [Certificato.TIPO_OGGETTO], nome: Labels.getLabel("tipoOggetto.certificati")]
                       , [oggetti: [SedutaStampa.TIPO_OGGETTO], nome: Labels.getLabel("tipoOggetto.seduteStampe")]]

    def zul = [(Determina.TIPO_OGGETTO)           : '/atti/documenti/determina.zul'
               , (VistoParere.TIPO_OGGETTO)       : '/atti/documenti/visto.zul'
               , (VistoParere.TIPO_OGGETTO_PARERE): '/atti/documenti/parere.zul'
               , (Certificato.TIPO_OGGETTO)       : '/atti/documenti/certificato.zul'
               , (PropostaDelibera.TIPO_OGGETTO)  : '/atti/documenti/propostaDelibera.zul'
               , (Delibera.TIPO_OGGETTO)          : '/atti/documenti/delibera.zul'
               , (SedutaStampa.TIPO_OGGETTO)      : '/odg/seduta/sedutaStampa.zul']

    // ricerca
    String testoCerca = ""

    // paginazione
    int activePage = 0
    int pageSize   = 30
    int totalSize  = 100

	boolean creaDeterminaVisible 		= false
	boolean creaPropostaDeliberaVisible = false
	boolean stampaRegistroVisible		= false
	boolean abilitaColonnaDataEsecutivita = false
	String richiestaEsecutivitaLabel
	boolean abilitaColonnaDataOrdinamento = false
    boolean aperturaAtto = true

	def grailsApplication

	def orderMap = Impostazioni.GESTIONE_DATA_ORDINAMENTO.abilitato ? ['priorita': 'desc',
																		'dataOrdinamento': 'asc',
																		'anno':'desc',
																		'numero':'desc',
																		'annoProposta':'desc',
																		'numeroProposta':'desc'
																		] :
																	['priorita': 'desc',
		'anno':'desc',
		'numero':'desc',
		'annoProposta':'desc',
		'numeroProposta':'desc'
		]


    @Init init(@ContextParam(ContextType.COMPONENT) Window w) {
		this.self 	= w
		tipoOggetto = tipiOggetto[0]
		creaDeterminaVisible 		= springSecurityService.principal.hasRuolo(Impostazioni.RUOLO_SO4_CREA_DETERMINA.valore)
		creaPropostaDeliberaVisible = springSecurityService.principal.hasRuolo(Impostazioni.RUOLO_SO4_CREA_PROPOSTA_DELIBERA.valore)
		stampaRegistroVisible		= springSecurityService.principal.hasRuolo(Impostazioni.RUOLO_SO4_ODG.valore)
		abilitaColonnaDataEsecutivita 	= Impostazioni.RICHIESTA_ESECUTIVITA_COLONNA.abilitato
		richiestaEsecutivitaLabel 		= Impostazioni.RICHIESTA_ESECUTIVITA_LABEL.valore
		abilitaColonnaDataOrdinamento	= Impostazioni.GESTIONE_DATA_ORDINAMENTO.abilitato
		caricaLista()
		caricaRegistri()
    }

    @Command
    onRefresh () {
        caricaLista()
    }

    @Command
    onCerca () {
        activePage = 0
        caricaLista()
    }

    @Command
    onNuovo () {
        creaPopup("/atti/documenti/determina.zul", [id: -1])
    }

    @Command
    onNuovaPropostaDelibera () {
        creaPopup("/atti/documenti/propostaDelibera.zul", [id: -1, fuoriSacco: false])
    }

    @Command
    onModifica (@BindingParam("selected") def selDocumento) {
        if (aperturaAtto) {
            aperturaAtto = false
            if (selDocumento != null) {
                selected = selDocumento
            }

            creaPopup(zul[selected.tipoOggetto], [id: selected.idDocumento, idPadre: selected.idPadre])
        }
    }

    @Command
    onCambiaTipo () {
        caricaRegistri()
        onCerca()
    }

    private void caricaRegistri () {
        listaTipiRegistro = TipoRegistro.createCriteria().list() {
            tipoRegistro = null
            if (tipoOggetto.nome == Labels.getLabel("tipoOggetto.determine")) {
                eq("determina", true)
            } else if (tipoOggetto.nome == Labels.getLabel("tipoOggetto.delibere")) {
                eq("delibera", true)
            } else {
                or {
                    eq("delibera", true)
                    eq("determina", true)
                }
            }
            eq("valido", true)
        }.toDTO()
        listaTipiRegistro.add(0, new TipoRegistroDTO(codice: null, descrizione: Labels.getLabel("tipoOggetto.tutti")))
        BindUtils.postNotifyChange(null, null, this, "listaTipiRegistro")
        BindUtils.postNotifyChange(null, null, this, "tipoRegistro")
    }

    private void caricaLista () {
        def documenti = documentoStepDTOService.inCarico(testoCerca, tipoOggetto?.oggetti, tipoRegistro?.codice, null, pageSize, activePage, orderMap,
                                                         false)
        lista = documenti.result
        totalSize = documenti.total

        BindUtils.postNotifyChange(null, null, this, "lista")
        BindUtils.postNotifyChange(null, null, this, "totalSize")
        BindUtils.postNotifyChange(null, null, this, "activePage")
    }

    private void creaPopup (String zul, def parametri) {
        Window w = Executions.createComponents(zul, self, parametri)
        w.doModal()
        w.onClose {
            aperturaAtto =  true
            caricaLista()
        }
    }

    @Command
    onStampaRegistro (@BindingParam("tipo") String tipo) {
        creaPopup("/atti/popupStampaRegistro.zul", [tipo: tipo])
    }

    /* GESTIONE MENU ALLEGATO */

    @Command
    onMostraAllegati (@BindingParam("documento") def documento) {
        listaAllegati = documentoStepDTOService.caricaAllegatiDocumento(documento.idDocumento, documento.tipoOggetto);

        BindUtils.postNotifyChange(null, null, this, "listaAllegati")
    }

    @Command
    onDownloadFileAllegato (@BindingParam("fileAllegato") def value) {
        documentoStepDTOService.downloadFileAllegato(value)
    }

    @Command
    public void onEseguiOrdinamento (@BindingParam("campi") String campi, @ContextParam(ContextType.TRIGGER_EVENT) SortEvent event) {
        for (String campo : campi?.split(",")?.reverse()) {
            orderMap.remove(campo)
            orderMap = [(campo): event?.isAscending() ? 'asc' : 'desc'] + orderMap
        }
        onCerca()
    }

    @Command
    public void onExportExcel () {
        if (totalSize > Impostazioni.ESPORTAZIONE_NUMERO_MASSIMO.valoreInt) {
            Messagebox.show("Attenzione: il numero dei documenti da esportare supera il massimo consentito.",
                            "Esportazione interrotta.",
                            Messagebox.OK, Messagebox.EXCLAMATION, null
            );
            return;
        }
        try {
            def documenti = documentoStepDTOService.inCarico(testoCerca, tipoOggetto?.oggetti, tipoRegistro?.codice, null, pageSize, activePage,
                                                             orderMap, false, true)
            def export = documenti.result.collect {
                [idDocumento           : it.idDocumento
                 , idPadre             : it.idPadre
                 , stato               : it.stato
                 , statoFirma          : it.statoFirma
                 , statoConservazione  : it.statoConservazione
                 , statoOdg            : it.statoOdg
                 , stepNome            : it.stepNome
                 , stepDescrizione     : it.stepDescrizione
                 , stepTitolo          : it.stepTitolo
                 , tipoOggetto         : it.tipoOggetto
                 , tipoRegistro        : it.tipoRegistro
                 , riservato           : it.riservato
                 , oggetto             : it.oggetto
                 , unitaProponente     : it.unitaProponente
                 , anno                : it.anno
                 , annoProposta        : it.annoProposta
                 , numero              : it.numero
                 , numeroProposta      : it.numeroProposta
                 , idTipologia         : it.idTipologia
                 , titoloTipologia     : it.titoloTipologia
                 , descrizioneTipologia: it.descrizioneTipologia
                 , dataAdozione        : it.dataAdozione
                 , statoVistiPareri    : it.statoVistiPareri
                 , dataScadenza        : it.dataScadenza
                 , priorita            : it.priorita
                ]
            }
            exportService.downloadExcel(documenti.exportOptions, export)
        }
        finally {
            caricaLista()
        }
    }
}
