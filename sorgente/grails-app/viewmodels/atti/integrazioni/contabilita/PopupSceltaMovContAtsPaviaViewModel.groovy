package atti.integrazioni.contabilita

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.contabilita.MovimentoContabile
import it.finmatica.atti.dizionari.DizionarioContabile
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.documenti.VistoParereService
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.CampiDocumento
import it.finmatica.atti.integrazioni.contabilita.IntegrazioneContabilitaAtsPavia
import org.apache.log4j.Logger
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Window

class PopupSceltaMovContAtsPaviaViewModel {

    public static final Logger log = Logger.getLogger(PopupSceltaMovContAtsPaviaViewModel.class);

    // service
    SpringSecurityService springSecurityService
    VistoParereService vistoParereService
    IntegrazioneContabilitaAtsPavia integrazioneContabilitaAtsPavia
    AttiGestoreCompetenze gestoreCompetenze

    // componenti
    Window self

    def competenze

    // dati
    IDocumento atto
    //Determina determina

    boolean modificabile = false
    def campiProtetti

  //  def movimentoSelezionato
    MovimentoContabile movimento //movimento contabile memorizzato su db

    List<DizionarioContabile> listaAnni

    DizionarioContabile annoEsercizio
    String conto
    String contoDescrizione
    String bilancio
    String bilancioDescrizione
    String sottoBudget
    String sottoBudgetDescrizione
    String progetto
    String note
    BigDecimal importo
    BigDecimal importoIniziale
    BigDecimal importoVariazione
    BigDecimal importoResiduo
    BigDecimal importoProvvedimento

    boolean attoConImpegnoSpesa = false
    boolean attoConVariazione = false
    boolean attoTrovaCopertura = false

    boolean bilancioVisibile = false
    boolean sottoBudgetVisibile = false
    boolean importoVisibile = false
    boolean importoInizialeVisibile = false
    boolean variazioneVisibile = false
    boolean importoResiduoVisibile = false
    boolean importoProvvedimentoVisibile = false
    boolean progettoVisibile = false

    boolean sottoBudgetObbligatorio = false

    @Init
    init(@ContextParam(ContextType.COMPONENT) Window w
         , @ExecutionArgParam("atto") IDocumento atto
         , @ExecutionArgParam("movimento") MovimentoContabile movimento
         , @ExecutionArgParam("attoConImpegnoSpesa") boolean attoConImpegnoSpesa
         , @ExecutionArgParam("attoConVariazione") boolean attoConVariazione
         , @ExecutionArgParam("attoTrovaCopertura") boolean attoTrovaCopertura) {

        this.self = w
        this.atto = atto
        this.movimento = movimento

        this.attoConImpegnoSpesa = attoConImpegnoSpesa
        this.attoConVariazione = attoConVariazione
        this.attoTrovaCopertura = attoTrovaCopertura

        competenze = gestoreCompetenze.getCompetenze(atto, true)

        modificabile = competenze.modifica
        if (modificabile) {
            modificabile = (!(atto.hasProperty("campiProtetti") && CampiDocumento.getMappaCampi(atto.campiProtetti).CONTABILITA))
            // calcolo i campi che devo proteggere in lettura
            campiProtetti = CampiDocumento.getMappaCampi(atto.campiProtetti)
        }
        else {
            //se il documento principale non è modificabile...verifico se l'utente ha diritti sul visto/parere contabile
            VistoParere vistoParere = vistoParereService.getVistoContabile (atto)
            if (vistoParere) {
                competenze = gestoreCompetenze.getCompetenze(vistoParere, true)

                modificabile = competenze.modifica
                // calcolo i campi che devo proteggere in lettura
                campiProtetti = CampiDocumento.getMappaCampi(atto.campiProtetti)
            }
        }
        //atto con nuovo impegno di spesa
        if (attoConImpegnoSpesa) {
            importoInizialeVisibile = false
            variazioneVisibile = false
            importoResiduoVisibile = false
            importoProvvedimentoVisibile = false

            bilancioVisibile = true
            importoVisibile = true
            progettoVisibile = true
            sottoBudgetVisibile = true

            //Per gli atti con impegno di spesa il sottoBudget lo deve inserire solo la ragioneria
            sottoBudgetObbligatorio = (!(atto.hasProperty("campiProtetti") && CampiDocumento.getMappaCampi(atto.campiProtetti).ATS_PAVIA_RAGIONERIA))
        } else {

            if (attoConVariazione) {
                bilancioVisibile = false
                importoVisibile = false
                importoResiduoVisibile = false

                progettoVisibile = true
                sottoBudgetVisibile = true
                importoInizialeVisibile = true
                variazioneVisibile = true
                importoProvvedimentoVisibile = true
                progettoVisibile = true

                //Per gli atti di variazione il sottoBudget lo deve inserire solo l'unità proponente
                sottoBudgetObbligatorio = (!(atto.hasProperty("campiProtetti") && CampiDocumento.getMappaCampi(atto.campiProtetti).ATS_PAVIA_CAMPI_CONTABILI))
            } else {

                if (attoTrovaCopertura) {
                    bilancioVisibile = false
                    importoVisibile = false
                    importoResiduoVisibile = true

                    progettoVisibile = true
                    sottoBudgetVisibile = true
                    importoInizialeVisibile = true
                    variazioneVisibile = false
                    importoProvvedimentoVisibile = true
                    progettoVisibile = true

                    //Per gli atti per trovare la copertura il sottoBudget lo deve inserire solo l'unità proponente
                    sottoBudgetObbligatorio = (!(atto.hasProperty("campiProtetti") && CampiDocumento.getMappaCampi(atto.campiProtetti).ATS_PAVIA_CAMPI_CONTABILI))
                }
            }
        }
        caricaListaAnni()

        //leggo i dati del movimento contabile
        if (movimento) {
            aggiornaMovimento()
        }

        BindUtils.postNotifyChange(null, null, this, "modificabile")
        BindUtils.postNotifyChange(null, null, this, "bilancioVisibile")
        BindUtils.postNotifyChange(null, null, this, "importoVisibile")
        BindUtils.postNotifyChange(null, null, this, "progettoVisibile")
        BindUtils.postNotifyChange(null, null, this, "sottoBudgetVisibile")
        BindUtils.postNotifyChange(null, null, this, "importoInizialeVisibile")
        BindUtils.postNotifyChange(null, null, this, "variazioneVisibile")
        BindUtils.postNotifyChange(null, null, this, "importoResiduoVisibile")
        BindUtils.postNotifyChange(null, null, this, "importoProvvedimentoVisibile")
        BindUtils.postNotifyChange(null, null, this, "sottoBudgetObbligatorio")
        BindUtils.postNotifyChange(null, null, this, "modificabile")
    }

    private aggiornaMovimento() {
        this.conto = movimento.conto
        this.contoDescrizione = movimento.contoDescrizione
        this.bilancio = movimento.bilancio
        this.bilancioDescrizione = movimento.bilancioDescrizione
        this.sottoBudget = movimento.sottoBudget
        this.sottoBudgetDescrizione = movimento.sottoBudgetDescrizione
        this.importo = movimento.importo
        this.importoIniziale = movimento.importoIniziale
        this.importoVariazione = movimento.importoVariazione
        this.importoResiduo = movimento.importoResiduo
        this.importoProvvedimento = movimento.importoProvvedimento
        this.progetto = movimento.progetto
        this.annoEsercizio = DizionarioContabile.findByTipoAndValidoAndCodice("ANNO", true, movimento.annoEsercizio?.toString())
        this.note = movimento.note
    }

    @Command
    onSalva() {
        //Verifico l'inserimento dei campi obbligatori
        if (!conto) {
            throw new AttiRuntimeException("Impossibile salvare il movimento. E' necessario inserire il conto.")
        }

        if (!annoEsercizio) {
            throw new AttiRuntimeException("Impossibile salvare il movimento. E' necessario inserire l'anno.")
        }

        if (!progetto) {
            throw new AttiRuntimeException("Impossibile salvare il movimento. E' necessario inserire il progetto.")
        }

        if (attoConImpegnoSpesa) {
            if (!bilancio) {
                throw new AttiRuntimeException("Impossibile salvare il movimento. E' necessario inserire il bilancio.")
            }

            if (!importo) {
                throw new AttiRuntimeException("Impossibile salvare il movimento. E' necessario inserire l'importo.")
            }
        }

        if (attoConVariazione) {
            if (!sottoBudget) {
                throw new AttiRuntimeException("Impossibile salvare il movimento. E' necessario inserire il sotto-budget.")
            }

            if ((importoIniziale == null) || (importoIniziale < 0)) {
                throw new AttiRuntimeException("Impossibile salvare il movimento. E' necessario inserire l'importo iniziale.")
            }

            if (importoVariazione == null) {
                throw new AttiRuntimeException("Impossibile salvare il movimento. E' necessario inserire l'importo della variazione.")
            }

            if ((importoProvvedimento == null) || (importoProvvedimento < 0)) {
                throw new AttiRuntimeException("Impossibile salvare il movimento. E' necessario inserire l'importo del provvedimento.")
            }
        }

        if (attoTrovaCopertura) {
            if (!sottoBudget) {
                throw new AttiRuntimeException("Impossibile salvare il movimento. E' necessario inserire il sotto-budget.")
            }

            if (!importoIniziale) {
                throw new AttiRuntimeException("Impossibile salvare il movimento. E' necessario inserire l'importo iniziale.")
            }

            if (!importoResiduo) {
                throw new AttiRuntimeException("Impossibile salvare il movimento. E' necessario inserire l'importo residuo.")
            }

            if (!importoProvvedimento) {
                throw new AttiRuntimeException("Impossibile salvare il movimento. E' necessario inserire l'importo del provvedimento.")
            }
        }

        if (!movimento) {
            movimento = new MovimentoContabile()
            movimento.idDocumento = atto.id
            movimento.tipoDocumento = atto.tipoOggetto
        }

        movimento.conto = conto
        movimento.contoDescrizione = contoDescrizione
        movimento.bilancio = bilancio
        movimento.bilancioDescrizione = bilancioDescrizione
        movimento.sottoBudget = sottoBudget
        movimento.sottoBudgetDescrizione = sottoBudgetDescrizione
        movimento.importo = importo
        movimento.importoIniziale = importoIniziale
        movimento.importoVariazione = importoVariazione
        movimento.importoResiduo = importoResiduo
        movimento.importoProvvedimento = importoProvvedimento
        movimento.progetto = progetto
        movimento.annoEsercizio = annoEsercizio.codice.toInteger()
        movimento.note = note

        //Nel campo OPERA inserisco la tipologia di determina
        if (attoConImpegnoSpesa) {
            movimento.opera = "IMPEGNO"
        }
        else {
            if (attoConVariazione) {
                movimento.opera = "VARIAZIONE"
            }
            else {
                if (attoTrovaCopertura) {
                    movimento.opera = "COPERTURA"
                }
            }
        }

        movimento.save()

        Clients.showNotification("Movimento salvato con successo.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true)
        Events.postEvent(Events.ON_CLOSE, self, null)
    }

    @Command
    onChiudi() {
        Events.postEvent(Events.ON_CLOSE, self, null)
    }

    @NotifyChange(["conto", "contoDescrizione", "bilancio", "bilancioDescrizione", "sottoBudget", "sottoBudgetDescrizione"])
    @Command
    onCercaDizionario(@BindingParam("dizionario") String dizionario) {
        Window w = Executions.createComponents("/atti/integrazioni/contabilita/popupRicercaDizionariAtsPavia.zul", null, [ente: atto.ente, dizionario: dizionario])
        w.onClose { Event event ->
            if (event.data != null) {
                switch (dizionario) {
                    case "CONTO":
                        this.conto = event.data.codice;
                        this.contoDescrizione = event.data.descrizione;
                        break;
                    case "BILANCIO":
                        this.bilancio = event.data.codice;
                        this.bilancioDescrizione = event.data.descrizione;
                        break;
                    case "SOTTOBUDGET":
                        this.sottoBudget = event.data.codice;
                        this.sottoBudgetDescrizione = event.data.descrizione;
                        break;
                }

                BindUtils.postNotifyChange(null, null, this, "conto")
                BindUtils.postNotifyChange(null, null, this, "contoDescrizione")
                BindUtils.postNotifyChange(null, null, this, "bilancio")
                BindUtils.postNotifyChange(null, null, this, "bilancioDescrizione")
                BindUtils.postNotifyChange(null, null, this, "sottoBudget")
                BindUtils.postNotifyChange(null, null, this, "sottoBudgetDescrizione")
            }
        }
        w.doModal()
    }

    private void caricaListaAnni() {
        listaAnni = integrazioneContabilitaAtsPavia.getAnni(this.atto.ente)
        BindUtils.postNotifyChange(null, null, this, "listaAnni")
    }


}
