package dizionari.odg

import afc.AfcAbstractRecord
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.impostazioni.CaratteristicaTipologiaDTO
import it.finmatica.atti.dto.odg.CommissioneComponenteDTO
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.atti.dto.odg.CommissioneStampaDTO
import it.finmatica.atti.dto.odg.dizionari.CommissioneDTOService
import it.finmatica.atti.dto.odg.dizionari.IncaricoDTO
import it.finmatica.atti.dto.odg.dizionari.RuoloPartecipanteDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.CaratteristicaTipologia
import it.finmatica.atti.impostazioni.Impostazione
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.protocollo.ProtocolloGdm
import it.finmatica.atti.integrazioni.protocollo.ProtocolloGdmConfig
import it.finmatica.atti.odg.*
import it.finmatica.atti.odg.dizionari.Incarico
import it.finmatica.atti.odg.dizionari.RuoloPartecipante
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.dto.configuratore.iter.WkfCfgIterDTO
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.select.Selectors
import org.zkoss.zk.ui.select.annotation.Wire
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class CommissioneDettaglioViewModel extends AfcAbstractRecord {

    // service
    CommissioneDTOService commissioneDTOService
    ProtocolloGdmConfig protocolloGdmConfig

    // componenti
    @Wire("#popupSceltaStampa")
    Window  popupSceltaStampa
    @Wire("#popupModificaComponente")
    Window  popupModificaComponente

    // dati
    List<TipoRegistroDTO> listaTipoRegistro
    List<WkfCfgIterDTO>   listaIter // iter per le delibere
    List<Ad4Ruolo>        listaRuoli

    // dati per le stampe
    CommissioneStampaDTO             commissioneStampa
    List<CommissioneStampaDTO>       listaStampe
    List<GestioneTestiModelloDTO>    listaModelli
    List<WkfCfgIterDTO>              listaCfgIter // iter per le stampe
    List<CaratteristicaTipologiaDTO> listaCaratteristiche
    boolean                          visualizzaTutti = false
    String                           tipoDocumentoEsterno
    List<CodiceDescrizione>          listaTipiDocumento

    // dati per i componenti
    List<CommissioneComponenteDTO> listaComponenti
    CommissioneComponenteDTO       componenteSelezionato
    List<RuoloPartecipanteDTO>     listaRuoliPartecipante
    List<IncaricoDTO>              listaIncarichi

    // dati per lo storico dei componenti:
    List<CommissioneComponenteDTO> listaComponentiStorico
    Date                           dataComponentiStorico
    Date    dataValiditaComponente  = new Date()

    @NotifyChange(["selectedRecord", "totalSize"])
    @Init
    void init (@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
        this.self = w

        if (id != null) {
            selectedRecord = caricaCommissione(id)
            aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
            aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
        } else {
            selectedRecord = new CommissioneDTO(valido: true)
            selectedRecord.secondaConvocazione = true
            selectedRecord.pubblicaWeb = true
            selectedRecord.ruoloCompetenze = Ad4Ruolo.get(Impostazioni.RUOLO_SO4_ODG.valore).toDTO()
            selectedRecord.ruoliObbligatori = true
            selectedRecord.votoPresidente = true
        }

        listaTipoRegistro = TipoRegistro.list().toDTO()
        listaIter = [new WkfCfgIterDTO(nome: "-- usa iter scritto in tipologia --", progressivo: -1)] + WkfCfgIter.iterValidi.findAllByTipoOggetto(
                WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), [sort: "nome", order: "asc"]).toDTO()
        listaRuoli = Ad4Ruolo.findAllByRuoloLike(Impostazione.PREFISSO_RUOLO + '%', [sort: "ruolo", order: "asc"])?.toDTO()

        listaIncarichi = Incarico.findAllByValido(true, [sort: 'titolo', order: 'desc']).toDTO()
        listaIncarichi.add(0, new IncaricoDTO(id: -1, titolo: "-- nessuno --"));

        caricaListaStampe();
        caricaListaComponenti();
        listaRuoliPartecipante = RuoloPartecipante.list().toDTO()
        listaRuoliPartecipante.add(0, new RuoloPartecipanteDTO(codice: "", descrizione: "-- nessuno --"))
    }

    // FIXME: Questo fa schifissimo ma serve per quei clienti che NON hanno il protocollo gdm: infatti fare l'inject del bean direttamente significherebbe
    // caricare il bean protocolloEsternoGdm e le relative classi del Protocollo con conseguente errore ClassNotFoundException.
    // Va fatto un refactor per gestire meglio i parametri delle integrazioni dalle tipologie.
    List<CodiceDescrizione> getListaTipiDocumentoProtocollo () {
        return protocolloGdmConfig?.getTipiDocumento()?:[]
    }

    @AfterCompose
    void afterCompose (@ContextParam(ContextType.VIEW) Component view) {
        Selectors.wireComponents(view, this, false);
    }

    private CommissioneDTO caricaCommissione (Long idCommissione) {
        return Commissione.createCriteria().get {
            eq("id", idCommissione)

            fetchMode("cfgIter", FetchMode.JOIN)
            fetchMode("ruoloCompetenze", FetchMode.JOIN)
            fetchMode("tipoRegistro", FetchMode.JOIN)
            fetchMode("utenteIns", FetchMode.JOIN)
            fetchMode("utenteUpd", FetchMode.JOIN)
        }?.toDTO()
    }

    /*
     * Gestione dei componenti
     */

    private void caricaListaComponenti () {
        listaComponenti = CommissioneComponente.createCriteria().list() {
            eq('commissione.id', selectedRecord.id)
            eq("valido", true)
            order('sequenza', 'asc')

            fetchMode("incarico", FetchMode.JOIN)
            fetchMode("componente", FetchMode.JOIN)
            fetchMode("ruoloPartecipante", FetchMode.JOIN)
        }.toDTO()

        BindUtils.postNotifyChange(null, null, this, "listaComponenti")
    }

    @Command
    void onEliminaComponente (@BindingParam("componente") CommissioneComponenteDTO componente) {
        componenteSelezionato.eliminaComponente = true
        dataValiditaComponente = new Date()
        onModificaComponente(componente)
    }

    @Command
    void onSpostaInAlto () {
        commissioneDTOService.spostaComponenteSu(componenteSelezionato);
        caricaListaComponenti();
    }

    @Command
    void onSpostaInBasso () {
        commissioneDTOService.spostaComponenteGiu(componenteSelezionato);
        caricaListaComponenti();
    }

    @Command
    void onSalvaComponente () {
        if (componenteSelezionato.eliminaComponente){
            if (dataValiditaComponente == null){
                throw new AttiRuntimeException("Occorre specificare la Data di Fine Validità")
            }
            commissioneDTOService.eliminaComponente(componenteSelezionato, dataValiditaComponente)
        }
        else {
            commissioneDTOService.salvaComponente(componenteSelezionato);
        }
        onChiudiComponente();
    }

    @Command
    void onChiudiComponente () {
        popupModificaComponente.visible = false;
        caricaListaComponenti();
    }

    @NotifyChange(["componenteSelezionato", "dataValiditaComponente"])
    @Command
    void onModificaComponente (@BindingParam("componente") CommissioneComponenteDTO componente) {
        if (componente == null) {
            componenteSelezionato = new CommissioneComponenteDTO();
            componenteSelezionato.commissione = selectedRecord;
            componenteSelezionato.sequenza = (CommissioneComponente.countByCommissioneAndValido(selectedRecord.domainObject, true) + 1)
            componenteSelezionato.sequenzaFirma = (CommissioneComponente.countByCommissioneAndValidoAndFirmatario(selectedRecord.domainObject, true, true) + 1)
            BindUtils.postNotifyChange(null, null, this, "componenteSelezionato")
        }
        else {
            BindUtils.postNotifyChange(null, null, this, "componenteSelezionato")
        }
        popupModificaComponente.doModal()
    }

    /*
     * Gestione storico componenti
     */

    @NotifyChange(["dataComponentiStorico", "listaComponentiStorico"])
    @Command
    void onCercaStorico () {
        if (dataComponentiStorico == null) {
            dataComponentiStorico = new Date();
        }
        caricaListaComponentiStorico(dataComponentiStorico.clearTime())
    }

    private void caricaListaComponentiStorico (Date date) {
        listaComponentiStorico = CommissioneComponente.createCriteria().list() {
            eq('commissione.id', selectedRecord.id)
            le("validoDal", date)
            or {
                isNull("validoAl")
                ge("validoAl", date )
            }

            order('sequenza', 'asc')

            fetchMode("incarico", FetchMode.JOIN)
            fetchMode("componente", FetchMode.JOIN)
            fetchMode("ruoloPartecipante", FetchMode.JOIN)
        }.toDTO()

        BindUtils.postNotifyChange(null, null, this, "listaComponentiStorico")
    }

    /*
     * Gestione Stampe
     */

    private void caricaCfgIter () {
        listaCfgIter = WkfCfgIter.iterValidi.findAllByTipoOggetto(WkfTipoOggetto.get(SedutaStampa.TIPO_OGGETTO), [sort: "nome", order: "asc"]).toDTO()
        listaCfgIter.add(0, new WkfCfgIterDTO(nome: "-- nessuno --", descrizione: "", progressivo: -1))
    }

    private void caricaCaratteristicheTipologie () {
        listaCaratteristiche =
                CaratteristicaTipologia.findAllByTipoOggetto(WkfTipoOggetto.get(SedutaStampa.TIPO_OGGETTO), [sort: "titolo", order: "asc"]).toDTO()
        listaCaratteristiche.add(0, new CaratteristicaTipologiaDTO(id:-1, titolo: "-- nessuno --", descrizione: ""))
    }

    @Command
    void caricaListaStampe () {
        listaStampe = CommissioneStampa.createCriteria().list() {
            eq ('commissione.id', selectedRecord.id)
            if (!visualizzaTutti) {
                eq ("valido", true)
            }
        }.toDTO()
        BindUtils.postNotifyChange(null, null, this, "listaStampe")
    }

    private void caricaListaModelliTesto (String codiceStampa) {
        // ottiene le stampe che ha la commissione e le esclude dalla lista
        listaModelli = GestioneTestiModello.createCriteria().list {
            tipoModello {
                like("codice", codiceStampa)
            }

            fetchMode("tipoModello", FetchMode.JOIN)
        }.toDTO()

        BindUtils.postNotifyChange(null, null, this, "listaModelli")
    }

    @NotifyChange(["listaCfgIter", "listaCaratteristiche", "commissioneStampa", "listaTipiDocumento"])
    @Command
    void onApriPopupStampa (@BindingParam("stampa") CommissioneStampaDTO commissioneStampa) {
        if (commissioneStampa == null) {
            commissioneStampa = new CommissioneStampaDTO(id: -1, valido: true)
        }
        this.commissioneStampa = commissioneStampa
        if (this.commissioneStampa.id > 0) {
            listaTipiDocumento = getListaTipiDocumentoProtocollo ()
            tipoDocumentoEsterno = protocolloGdmConfig?.getTipoDocumento(this.commissioneStampa.id)?:""
        }
        caricaListaModelliTesto(getCodiceModelloTesto(this.commissioneStampa.codice))
        caricaCfgIter()
        caricaCaratteristicheTipologie()
        popupSceltaStampa.doModal()
    }

    private String getCodiceModelloTesto (String codiceStampa) {
        if (codiceStampa == CommissioneStampa.VERBALE) {
            return Seduta.MODELLO_TESTO_VERBALE
        } else if (codiceStampa == CommissioneStampa.CONVOCAZIONE) {
            return Seduta.MODELLO_TESTO_CONVOCAZIONE+"%"
        } else if (codiceStampa == CommissioneStampa.DELIBERA) {
            return "${CommissioneStampa.DELIBERA}%"
        } else {
            return null
        }
    }

    @NotifyChange("listaModelli")
    @Command
    void onSelectTipoStampa () {
        caricaListaModelliTesto(getCodiceModelloTesto(commissioneStampa.codice))
    }

    @NotifyChange("listaStampe")
    @Command
    void onSettaValidoCommissioneStampa (@BindingParam("valido") boolean valido) {
        Messagebox.show("Modificare la validità della commissione?", "Modifica validità",
                        Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                        new org.zkoss.zk.ui.event.EventListener() {
                            void onEvent (Event e) {
                                if (Messagebox.ON_OK.equals(e.getName())) {
                                    commissioneStampa.valido = valido
                                    commissioneDTOService.salvaStampa(selectedRecord, commissioneStampa, tipoDocumentoEsterno)
                                    caricaListaStampe()
                                    BindUtils.postNotifyChange(null, null, CommissioneDettaglioViewModel.this, "selectedRecord")
                                    BindUtils.postNotifyChange(null, null, CommissioneDettaglioViewModel.this, "datiCreazione")
                                    BindUtils.postNotifyChange(null, null, CommissioneDettaglioViewModel.this, "datiModifica")
                                }
                            }
                        }
        )
    }

    @Command
    @NotifyChange(["visualizzaTutti"])
    void onVisualizzaTutti () {
        visualizzaTutti = !visualizzaTutti
        caricaListaStampe()
    }

    @Command
    void onAggiungiStampa () {
        commissioneDTOService.salvaStampa(selectedRecord, commissioneStampa, tipoDocumentoEsterno)
        onChiudiStampa()
    }

    @Command
    void onChiudiStampa () {
        popupSceltaStampa.visible = false
        caricaListaStampe()
    }

    //////////////////////////////////////////
    //				SALVATAGGIO				//
    //////////////////////////////////////////

    @NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
    @Command
    def onSalva () {
        boolean isNuovaCommissione = (selectedRecord.id == null)
        def idCommissione = commissioneDTOService.salva(selectedRecord).id
        selectedRecord = caricaCommissione(idCommissione)
        if (isNuovaCommissione) {
            aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
        }
        aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
    }

    @NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
    @Command
    def onSalvaChiudi () {
        onSalva()
        onChiudi()
    }

    @Command
    def onSettaValido (@BindingParam("valido") boolean valido) {
        Messagebox.show("Modificare la validità della commissione?", "Modifica validità",
                        Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                        new org.zkoss.zk.ui.event.EventListener() {
                            public void onEvent (Event e) {
                                if (Messagebox.ON_OK.equals(e.getName())) {
                                    super.getSelectedRecord().valido = valido
                                    onSalva()
                                    BindUtils.postNotifyChange(null, null, CommissioneDettaglioViewModel.this, "selectedRecord")
                                    BindUtils.postNotifyChange(null, null, CommissioneDettaglioViewModel.this, "datiCreazione")
                                    BindUtils.postNotifyChange(null, null, CommissioneDettaglioViewModel.this, "datiModifica")
                                }
                            }
                        }
        )
    }

    @Command
    void onCercaSoggetto () {
        Window w = Executions.createComponents("/commons/popupRicercaSoggetti.zul", self, [id: -1])
        w.onClose { Event event ->
            if (event.data != null) {
                this.componenteSelezionato.componente = event.data;
                BindUtils.postNotifyChange(null, null, this, "componenteSelezionato")
            }
        }
        w.doModal()
    }
}
