package atti.integrazioni.contabilita

import it.finmatica.atti.contabilita.MovimentoAnagrafica
import it.finmatica.atti.contabilita.MovimentoContabile
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.dto.contabilita.MovimentoAnagraficaDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.CampiDocumento
import it.finmatica.atti.integrazioni.contabilita.IntegrazioneContabilitaJSerfin
import it.finmatica.atti.integrazioniws.sicraweb.classes.Esito
import it.finmatica.atti.integrazioniws.sicraweb.classes.inserimentoImpegni.RispostaInserimentoImpegni
import it.finmatica.atti.integrazioniws.sicraweb.classes.ricercaBudgetBilancio.RispostaRicercaBudgetBilancio
import org.apache.log4j.Logger
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.EventListener
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

import static it.finmatica.zkutils.LabelUtils.getLabel as l

class PopupSceltaMovContJSerfinViewModel {

    public static final Logger log = Logger.getLogger(PopupSceltaMovContJSerfinViewModel.class)

    // service
    IntegrazioneContabilitaJSerfin integrazioneContabilitaJSerfin
    AttiGestoreCompetenze gestoreCompetenze

    // componenti
    Window self

    def    competenze

    // dati
    IDocumento atto

    String tipoDettaglio
    //indica il tipo di movimento. Valori possibili: PRENOTAZIONE, IMPEGNO, SUBIMPEGNO, ACCERTAMENTO. Memorizzato nel campo TIPO_DETTAGLIO del movimento_contabile
    String operazioneScelta
    //indica il tipo di operazione (variazione, nuovo impegno...ecc). Memorizzato nel campo OPERA del movimento_contabile
    List<MovimentoAnagraficaDTO> listaAnagrafiche
    MovimentoAnagraficaDTO anagrafica
    String labelSalva = ""
    String labelTitolo = ""

    //etichette parametri in base all'operazione scelta
    boolean ricercaAbilitata = false //abilito il pulsante di ricerca
    boolean anagraficaAbilitata = false
    boolean visualizzaTipoFinanziamento = false //stabilisce se visualizzare il tipo di finanziamento

    boolean isInserimento = false //stabilisce apro la pagina in inserimento oppure in modifica
    boolean modificabile = false
    def campiProtetti
    boolean isRicercaAnagrafica = true //stabilisce se si pu� collegare un movimento ad un'anagrafica
    boolean isRicercaCapitoliCodiceLavoro = false //stabilisce se poter ricercare i capitoli anche per codice lavoro
    boolean isRicercaMovimentiCodiceGlobale = false //stabilisce se poter ricercare i movimenti per codice globale
    boolean isAbilitataVariazioneSubImpegno = false //stabilise se � abilitata la sezione di variazione subimpegno
    boolean isInVariazioneAnnoCompetenzaUgualeImpegno = true    //stabilise se in fase di variazione, l'anno di competenza deve essere uguale a quello dell'impegno (PARMA)
                                                                //oppure modificabile dall'utente (TRENTO)
    boolean annoCompetenzaVisibile = false //la ricerca per anno competenza NON c'� in ricerca capitoli
    String labelRicercaForm = "Ricerca"
    String labelRicercaNumero = "Numero"
    String labelRicercaAnno = "Anno"
    String labelAnnoEsigibilita = "Anno Esigibilita'"
    String labelRicercaEmptyMessage = ""
    String labelInserimentoError = ""

    //  String operazione = "IMPEGNO" //operazione scelta dall'utente
    String operazione
    def listaContabile
    String numero
    int anno = Calendar.getInstance().get(Calendar.YEAR)
    int annoCompetenza = Calendar.getInstance().get(Calendar.YEAR)
    String codiceLavoro
    String codiceGlobale
    String codiceTipoFinanziamento
    String tipoFinanziamento

    def movimentoSelezionato
    MovimentoContabile movimentoContabile //movimento contabile memorizzato su db

    //campi del movimento modificabili in maschera
    BigDecimal importo
    String oggetto
    def vLivello
    String cig
    String cup

    //campi del movimento che mostro in sola lettura (quando apro la maschera in modifica)
    String parte
    String idEsterno
    String capitolo
    String descrizioneCapitolo
    Integer progressivoEsterno
    String annoEsercizio
    String descrizioneFinanziamento1
    String movimentoPadre  //movimento dal quale � scaturita la variazione
    String numeroProgrammazione
    String numeroImpegnoPadre

    //flag che indicano la grid visualizzata
    boolean visElencoCapitoli = true
    boolean visElencoPrenotazioni = false
    boolean visElencoImpegni = false
    boolean visElencoSubImpegni = false
    boolean visElencoAccertamenti = false
    boolean visCodiceGlobale = false

    boolean visDuplica = false          //indica se visualizzare i campi della duplica
    boolean visPulsanteDuplica = false  //indica se visualizzare il pulsante della duplica

    boolean importiNegativi = false //indica se accettare o meno importi negativi

    //in apertura della pop-up leggo i campi memorizzati (per capire se sono stati modificati o meno dall'utente)
    BigDecimal importoApertura
    String oggettoApertura
    String cigApertura
    String cupApertura

    boolean isVLivelloObbligatorio = false //indica se il v livello � sempre obbligatorio (quando � visualizzato in maschera)

    @Init
    init(@ContextParam(ContextType.COMPONENT) Window w
         , @ExecutionArgParam("atto") IProposta atto
         , @ExecutionArgParam("movimento") MovimentoContabile movimento
         , @ExecutionArgParam("tipologia") String tipologia
         , @ExecutionArgParam("duplica") boolean duplica) {

        this.self = w
        this.atto = atto
        this.tipoDettaglio = tipologia
        annoCompetenzaVisibile = false

        isRicercaAnagrafica = integrazioneContabilitaJSerfin.isRicercaAnagraficaAbilitata()
        isRicercaCapitoliCodiceLavoro = integrazioneContabilitaJSerfin.isRicercaCapitoliCodiceLavoro()
        isRicercaMovimentiCodiceGlobale = integrazioneContabilitaJSerfin.isRicercaMovimentiCodiceGlobale()
        visualizzaTipoFinanziamento = integrazioneContabilitaJSerfin.isVisualizzaTipoFinanziamento()

        isAbilitataVariazioneSubImpegno = integrazioneContabilitaJSerfin.isAbilitataVariazioneSubImpegno()
        isInVariazioneAnnoCompetenzaUgualeImpegno = integrazioneContabilitaJSerfin.isInVariazioneAnnoCompetenzaUgualeImpegno()

        isVLivelloObbligatorio = integrazioneContabilitaJSerfin.isVLivelloObbligatorio()

        if (isInVariazioneAnnoCompetenzaUgualeImpegno) {
            labelAnnoEsigibilita = "Anno Esigibilita'"
        }
        else {
            labelAnnoEsigibilita = "Anno Competenza"
        }

        if (movimento?.id == null) {
            anagraficaAbilitata = true
            isInserimento = true
            modificabile = true
            labelSalva = "Inserisci"

            visDuplica = false
            visPulsanteDuplica = false

            switch (tipoDettaglio) {
                case 'PRENOTAZIONE':
                    labelTitolo = "Aggiungi Prenotazione"
                    operazioneScelta = 'PRENOTAZIONE'
                    labelRicercaNumero = "Numero Capitolo"
                    annoCompetenzaVisibile = false
                    labelInserimentoError = "Selezionare un capitolo"
                    break
                case 'IMPEGNO':
                    labelTitolo = "Aggiungi Impegno"
                    operazioneScelta = 'IMPEGNO'
                    labelRicercaNumero = "Numero Capitolo"
                    annoCompetenzaVisibile = false
                    labelInserimentoError = "Selezionare un capitolo"
                    break
                case 'SUBIMPEGNO':
                    operazioneScelta = 'SUBIMPEGNO'
                    labelTitolo = "Aggiungi SubImpegno"
                    labelRicercaForm = "Ricerca Impegno"
                    labelRicercaNumero = "Numero Impegno"
                    labelRicercaAnno = "Anno"
                    labelRicercaEmptyMessage = "Nessun impegno trovato"
                    labelInserimentoError = "Selezionare un impegno"
                    visElencoCapitoli = false
                    visElencoPrenotazioni = false
                    visElencoImpegni = true
                    visElencoSubImpegni = false
                    visElencoAccertamenti = false
                    annoCompetenzaVisibile = true
                    visCodiceGlobale = isRicercaMovimentiCodiceGlobale

                    break
                case 'ACCERTAMENTO':
                    labelTitolo = "Aggiungi Accertamento"
                    operazioneScelta = 'ACCERTAMENTO'
                    labelRicercaNumero = "Numero Capitolo"
                    annoCompetenzaVisibile = false
                    break
                default:
                    labelTitolo = "Aggiungi Movimento Contabile"
                    break
            }
            anagrafica = null
        } else {
            anagraficaAbilitata = false
            isInserimento = false
            visElencoCapitoli = false
            visElencoPrenotazioni = false
            visElencoImpegni = false
            visElencoSubImpegni = false
            visElencoAccertamenti = false
            visDuplica = duplica
            visPulsanteDuplica = true

            competenze = gestoreCompetenze.getCompetenze(atto, true)
            modificabile = competenze.modifica
            if (modificabile)
                modificabile = (!(atto.hasProperty("campiProtetti") && CampiDocumento.getMappaCampi(atto.campiProtetti).CONTABILITA))

            //valorizzo con variabili tutti i campi del movimento in modo da mostrarli in sola lettura
            tipoDettaglio = movimento.tipoDettaglio
            operazioneScelta = movimento.opera
            anno = movimento.annoEsercizio
            numero = movimento.numero
            importo = movimento.importo
            oggetto = movimento.descrizione
            //vLivello = movimento.pdcLiv5
            vLivello = new HashMap<String, String>(["sigla": movimento.pdcLiv5, "descrizione": movimento.descrizionePdcLiv5])
            cig = movimento.cig
            cup = movimento.cup
            parte = movimento.tipoUscita
            idEsterno = movimento.idEsterno
            capitolo = movimento.capitolo
            descrizioneCapitolo = movimento.descrizioneCapitolo
            annoEsercizio = movimento.annoEsercizio
            descrizioneFinanziamento1 = movimento.descrizioneFinanziamento1
            progressivoEsterno = movimento.progressivoEsterno
            movimentoPadre = movimento.numeroVariazione
            numeroProgrammazione = movimento.numeroProgrammazione
            numeroImpegnoPadre = movimento.numeroImpegnoPadre
            codiceGlobale = movimento.codiceGlobale
            codiceTipoFinanziamento = movimento.codtipofinanz
            tipoFinanziamento = movimento.destipofinanz

            //salvo in variabili i dati letti in apertura per confrontarli in caso di modifica da parte dell'utente
            importoApertura = importo
            oggettoApertura = oggetto
            cigApertura = cig
            cupApertura = cup

            labelSalva = "Modifica"

            switch (tipoDettaglio) {
                case 'PRENOTAZIONE':
                    labelTitolo = "Modifica Prenotazione"
                    break
                case 'IMPEGNO':
                    labelTitolo = "Modifica Impegno"
                    break
                case 'SUBIMPEGNO':
                    labelTitolo = "Modifica SubImpegno"
                    break
                case 'ACCERTAMENTO':
                    labelTitolo = "Modifica Accertamento"
                    break
                default:
                    labelTitolo = "Aggiungi Movimento Contabile"
                    break
            }

            switch (operazioneScelta) {
                case 'PRENOTAZIONE':
                    annoCompetenzaVisibile = false
                    importiNegativi = false
                    break

                case 'PRENOTAZIONE_VARIAZIONE':
                    annoCompetenzaVisibile = true
                    importiNegativi = true
                    break

                case 'IMPEGNO':
                    annoCompetenzaVisibile = false
                    importiNegativi = false
                    break

                case 'IMPEGNO_PRENOTAZIONE':
                    annoCompetenzaVisibile = true
                    importiNegativi = false
                    break

                case 'IMPEGNO_VARIAZIONE':
                    annoCompetenzaVisibile = true
                    importiNegativi = true
                    break

                case 'SUBIMPEGNO':
                    annoCompetenzaVisibile = true
                    importiNegativi = false
                    break

                case 'SUBIMPEGNO_VARIAZIONE':
                    annoCompetenzaVisibile = true
                    importiNegativi = true
                    break

                case 'ACCERTAMENTO':
                    annoCompetenzaVisibile = false
                    importiNegativi = false
                    break

                case 'ACCERTAMENTO_VARIAZIONE':
                    annoCompetenzaVisibile = true
                    importiNegativi = true
                    break
            }
            if (movimento.anagrafica)
                anagrafica = movimento.anagrafica.toDTO()
            else
                anagrafica = new MovimentoAnagraficaDTO(id: -1L, ragioneSociale: 'Movimento contabile senza beneficiario')

            movimentoContabile = movimento
        }
        caricaListaAnagrafica()
    }

    private aggiornaMovimento() {

    }

    @Command
    onChiudiPopup() {
        Events.postEvent(Events.ON_CLOSE, self, null)
    }

    private void caricaListaAnagrafica() {
        listaAnagrafiche = MovimentoAnagrafica.createCriteria().list() {
            if (atto instanceof Determina) {
                eq("determina", atto)
            } else if (atto instanceof Delibera) {
                eq("propostaDelibera", atto.propostaDelibera)
            } else if (atto instanceof PropostaDelibera) {
                eq("propostaDelibera", atto)
            } else {
                isNull("id")
            }
            order("ragioneSociale", "asc")
            order("cognome", "asc")
            order("nome", "asc")
        }.toDTO()

        // costruisco le DTO fittizie direttamente (non passando da MovimentoAnagrafica.toDTO()):
        // su un'istanza di dominio transient (mai salvata) .toDTO() non preserva l'id assegnato a mano,
        // lo restituisce sempre null. Con id null per entrambe le voci il PropertyConverter, cercando
        // la corrispondenza per id, trovava sempre la prima (il '--') indipendentemente da quella cliccata.
        MovimentoAnagraficaDTO anagraficaVuota = null
        if (tipoDettaglio != 'ACCERTAMENTO') {
            anagraficaVuota = new MovimentoAnagraficaDTO(id: -2L, ragioneSociale: '--')
            listaAnagrafiche.add(anagraficaVuota)
        }
        MovimentoAnagraficaDTO nuovaAnagrafica = new MovimentoAnagraficaDTO(id: -1L, ragioneSociale: 'Movimento contabile senza beneficiario')
        listaAnagrafiche.add(nuovaAnagrafica)

        if (anagrafica?.id == null && anagraficaVuota != null) {
            //senza una selezione valorizzata lato server, il combobox readonly mostra di default il testo
            //dell'ultima voce del model (anche se non e' stata realmente selezionata): valorizzo esplicitamente
            //il placeholder in modo che la combo parta da una selezione coerente con lo stato server
            anagrafica = anagraficaVuota
        }
        BindUtils.postNotifyChange(null, null, this, "listaAnagrafiche")
        // Ricostruendo il model della combobox va rinnovato anche il binding di selectedItem,
        // altrimenti la selezione corrente (anagrafica) viene persa e il client rimanda null al server.
        BindUtils.postNotifyChange(null, null, this, "anagrafica")
    }

    @NotifyChange(["listaAnagrafiche"])
    @Command
    onRicercaAnagrafica() {
        Window w = Executions.createComponents("/atti/integrazioni/contabilita/popupSceltaSoggettiJSerfin.zul", null,
                [atto: atto, movimento: null])
        w.onClose { event ->
            caricaListaAnagrafica()
            if (event.data != null) {
                anagrafica = event.data
                if (anagrafica.ragioneSociale.equals('')) {
                    anagrafica.ragioneSociale = anagrafica.cognome + ' '+ anagrafica.nome
                }
            }
            BindUtils.postNotifyChange(null, null, this, "listaAnagrafiche")
            BindUtils.postNotifyChange(null, null, this, "anagrafica")
        }
        w.doModal()
    }

    @NotifyChange(["listaContabile", "movimentoSelezionato", "labelRicercaEmptyMessage"])
    @Command
    void onRicerca(@BindingParam("numero") String numero, @BindingParam("anno") String anno, @BindingParam("codiceLavoro") String codiceLavoro) {
        //se l'utente scrive un valore e poi svuota il campo, le stringhe arrivano come "" e non come null
        if  ("".equals(numero)) {
            numero = null
        }

        if  ("".equals(anno)) {
            anno = null
        }

        if  ("".equals(codiceLavoro)) {
            codiceLavoro = null
        }

        if  ("".equals(codiceGlobale)) {
            codiceGlobale = null
        }

        if (((numero == null) || (anno == null)) && (!isRicercaCapitoliCodiceLavoro) && (!isRicercaMovimentiCodiceGlobale)) {
            Clients.showNotification("Inserire gli estremi numero e anno per effettuare la ricerca.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 3000, true)
        } else {
            if (((numero == null) || (anno == null))  && (codiceLavoro == null) && (codiceGlobale == null) && (isRicercaCapitoliCodiceLavoro || isRicercaMovimentiCodiceGlobale)) {
                Clients.showNotification("Inserire gli estremi numero e anno o codice per effettuare la ricerca.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 3000, true)
            } else {
                movimentoSelezionato = null
                //In base al tipoDettaglio (prenotazione - accertamento - impegno -subImpegno) e all'operazione scelta cambia la chiamata al ws da fare
                switch (tipoDettaglio) {
                    case 'PRENOTAZIONE':
                        if (operazioneScelta == "PRENOTAZIONE") {
                            getCapitoli(numero?.toInteger(), anno?.toInteger(), codiceLavoro?.toInteger(), 'U')
                        } else {
                            if (operazioneScelta == "PRENOTAZIONE_VARIAZIONE") {
                                getPrenotazioni(numero?.toInteger(), anno?.toInteger(), annoCompetenza?.toInteger(), codiceGlobale?.toLong())
                            }
                        }
                        break
                    case 'IMPEGNO':
                        if (operazioneScelta == "IMPEGNO") {
                            getCapitoli(numero?.toInteger(), anno?.toInteger(), codiceLavoro?.toInteger(), 'U')
                        } else {
                            if (operazioneScelta == "IMPEGNO_PRENOTAZIONE") {
                                getPrenotazioni(numero?.toInteger(), anno?.toInteger(), annoCompetenza?.toInteger(), codiceGlobale?.toLong())
                            } else {
                                if (operazioneScelta == "IMPEGNO_VARIAZIONE") {
                                    getImpegni(numero?.toInteger(), anno?.toInteger(), annoCompetenza?.toInteger(), codiceGlobale?.toLong())
                                }
                            }
                        }
                        break

                    case 'SUBIMPEGNO':
                        if (operazioneScelta == "SUBIMPEGNO") {
                            getImpegni(numero?.toInteger(), anno?.toInteger(), annoCompetenza?.toInteger(), codiceGlobale?.toLong())
                        } else {
                            if (operazioneScelta == "SUBIMPEGNO_VARIAZIONE") {
                                getSubImpegni(numero?.toInteger(), anno?.toInteger(), annoCompetenza?.toInteger(), codiceGlobale?.toLong())
                            }
                        }
                        break

                    case 'ACCERTAMENTO':
                        if (operazioneScelta == "ACCERTAMENTO") {
                            getCapitoli(numero?.toInteger(), anno?.toInteger(), codiceLavoro?.toInteger(), 'E')
                        } else {
                            if (operazioneScelta == "ACCERTAMENTO_VARIAZIONE") {
                                getAccertamenti(numero?.toInteger(), anno?.toInteger(), annoCompetenza?.toInteger(), codiceGlobale?.toLong())
                            }
                        }
                        break
                    default:
                        break
                }

                //movimentoSelezionato = new Capitolo(numero:'08', anno:'2023', estremi: '08/08', descrizione: "CAPITOLO 08/08", disponibilita: "180,00")
                BindUtils.postNotifyChange(null, null, this, "movimentoSelezionato")
                BindUtils.postNotifyChange(null, null, this, "listaContabile")
                BindUtils.postNotifyChange(null, null, this, "labelRicercaEmptyMessage")
            }
        }
    }

    @Command
    onSalva() {
        if (!oggetto) {
            throw new AttiRuntimeException("Inserire l'oggetto del movimento.")
        }

        if (isRicercaAnagrafica) {
            if (anagrafica?.id == -2) {
                throw new AttiRuntimeException("Non e' possibile effettuare il salvataggio. Selezionare un'anagrafica o indicare che il movimento contabile non ha beneficiario")
            }
        }

        if (isInserimento) {
            if (!movimentoSelezionato) {
                throw new AttiRuntimeException("Non e' possibile effettuare il salvataggio. " + labelInserimentoError)
            }
        }
        if (!movimentoContabile) {
            movimentoContabile = new MovimentoContabile()
            movimentoContabile.idDocumento = atto.id
            movimentoContabile.tipoDocumento = atto.tipoOggetto
        }

        if (anagrafica?.id > -1) {
            movimentoContabile.anagrafica = anagrafica.domainObject
        }

        //campi comuni a tutte le tipologie di movimento e inseriti a mano (sono tutti modicabili a video
        movimentoContabile.importo = importo?:0
        movimentoContabile.descrizione = oggetto.toUpperCase()
        movimentoContabile.cig = cig
        movimentoContabile.cup = cup

        /*
        valori per azione
        0 Creazione impegno/accertamento/sub/proposta
        1 Creazione variazione impegno/accertamento/sub/proposta
        2 Modifica testata impegno/accertamento/sub/proposta/variazione
        3 Cancellazione impegno/accertamento/sub/proposta
        */
        String azioneWs = "0"

        if (movimentoSelezionato) {
            //sono in fase di creazione movimento contabile, e quindi prendo i dati da quanto selezionato in maschera
            movimentoContabile.tipoDettaglio = tipoDettaglio  //PRENOTAZIONE - IMPEGNO - ACCERTAMENTO - SUBIMPEGNO
            movimentoContabile.opera = operazioneScelta

            //verifico se il vLivello � obbligatorio ed era visibile in maschera
            if (isVLivelloObbligatorio) {
                //vm.visElencoCapitoli or modifica) and (vm.movimentoSelezionato.lista_piani_fin_collegati.size()
                if ((visElencoCapitoli || modificabile ) && (movimentoSelezionato.lista_piani_fin_collegati?.size() > 0)) {
                    if (vLivello == null) {
                        //errore perch� il V livello � obbligatorio (se mostrato in maschera)
                        throw new AttiRuntimeException(l("message.JSerfin.VLivelloMancante"))
                    }
                }
            }
            //se il VLivello � stato selezionato dal menu a tendina
            if (vLivello) {
                movimentoContabile.pdcLiv5 = vLivello.sigla
                movimentoContabile.descrizionePdcLiv5 = vLivello.descrizione
            } else {
                movimentoContabile.pdcLiv5 = movimentoSelezionato.sigpianofin
                movimentoContabile.descrizionePdcLiv5 = movimentoSelezionato.despianofin
            }

            //campi che variano in base all'oggetto selezionato
            switch (operazioneScelta) {  //da capire se lasciare tipoDettaglio o operazioneScelta
                case 'PRENOTAZIONE':
                case 'IMPEGNO':
                case 'IMPEGNO_PRENOTAZIONE':
                case 'SUBIMPEGNO':
                case 'ACCERTAMENTO':
                    azioneWs = "0"

                    if ("ACCERTAMENTO".equals(operazioneScelta)) {
                        movimentoContabile.tipoUscita = 'E'
                    }
                    else {
                        movimentoContabile.tipoUscita = 'U'
                    }

                    movimentoContabile.idCapitolo = movimentoSelezionato.idcapitolo
                    if ("IMPEGNO_PRENOTAZIONE".equals(operazioneScelta)) {
                        movimentoContabile.idProgrammazione = movimentoSelezionato.idimpegno
                        movimentoContabile.numeroProgrammazione = movimentoSelezionato.codannuale
                        movimentoContabile.descrizioneCapitolo = movimentoSelezionato.descapitolo
                        movimentoContabile.capitolo = movimentoSelezionato.codificacapitolo
                    }
                    else {
                        if ("SUBIMPEGNO".equals(operazioneScelta)) {
                            movimentoContabile.capitolo = movimentoSelezionato.codificacapitolo
                            movimentoContabile.descrizioneFinanziamento1 = movimentoSelezionato.descrizionebudget //????
                            movimentoContabile.descrizioneCapitolo = movimentoSelezionato.descapitolo
                            movimentoContabile.idImpegno = movimentoSelezionato.idimpegno
                            movimentoContabile.numeroImpegnoPadre = movimentoSelezionato.codannuale
                        } else {
                            movimentoContabile.capitolo = movimentoSelezionato.codifica
                            movimentoContabile.descrizioneFinanziamento1 = movimentoSelezionato.descrizionebudget
                            movimentoContabile.descrizioneCapitolo = movimentoSelezionato.descrizione
                        }
                    }
                    movimentoContabile.numero = movimentoSelezionato.numcapitolo
                    movimentoContabile.annoCompetenza = movimentoSelezionato.annocompetenza
                    movimentoContabile.annoEsercizio = movimentoSelezionato.annocompetenza

                    movimentoContabile.idProgetto = movimentoSelezionato.idprogetto
                    movimentoContabile.codProgetto = movimentoSelezionato.codprogetto
                    movimentoContabile.progetto = movimentoSelezionato.desprogetto
                    movimentoContabile.copFpv = movimentoSelezionato.copfpv
                    movimentoContabile.sigCpt = movimentoSelezionato.sigcpt
                    movimentoContabile.desCpt = movimentoSelezionato.descpt

                    movimentoContabile.codcentrocosto = movimentoSelezionato.codcentrocosto
                    movimentoContabile.descentrocosto = movimentoSelezionato.descentrocosto

                    movimentoContabile.codtipofinanz = movimentoSelezionato.codtipofinanz
                    movimentoContabile.destipofinanz = movimentoSelezionato.destipofinanz
                    movimentoContabile.codlavoro = movimentoSelezionato.codlavoro
                    movimentoContabile.deslavoro = movimentoSelezionato.deslavoro
                    movimentoContabile.codtipospesa = movimentoSelezionato.codtipospesa
                    movimentoContabile.destipospesa = movimentoSelezionato.destipospesa

                    movimentoContabile.codCentroGestore = movimentoSelezionato.codrespservcapitolo
                    movimentoContabile.desCentroGestore = movimentoSelezionato.desrespservcapitolo

                    break
                case 'PRENOTAZIONE_VARIAZIONE':
                case 'IMPEGNO_VARIAZIONE':
                case 'ACCERTAMENTO_VARIAZIONE':
                case 'SUBIMPEGNO_VARIAZIONE':
                    azioneWs = "1"

                    if ("ACCERTAMENTO_VARIAZIONE".equals(operazioneScelta)) {
                        movimentoContabile.tipoUscita = 'E'
                    }
                    else {
                        movimentoContabile.tipoUscita = 'U'
                    }

                  //  if ("PRENOTAZIONE_VARIAZIONE".equals(operazioneScelta) or "IMPEGNO_VARIAZIONE".equals(operazioneScelta)) {
                        movimentoContabile.idProgrammazione = movimentoSelezionato.idimpegno
                        movimentoContabile.numeroProgrammazione = movimentoSelezionato.codAnnuale
                        //    movimentoContabile.idCapitolo = movimentoSelezionato.idcapitolo  tolgo perch� lo metto sempre
                        movimentoContabile.numero = movimentoSelezionato.numcapitolo
                        movimentoContabile.numeroVariazione = movimentoSelezionato.codannuale //inserisco il numero della prenotazione originale

                        movimentoContabile.codcentrocosto = movimentoSelezionato.codcentrocosto
                        movimentoContabile.descentrocosto = movimentoSelezionato.descentrocosto

                        movimentoContabile.codtipofinanz = movimentoSelezionato.codtipofinanz
                        movimentoContabile.destipofinanz = movimentoSelezionato.destipofinanz

                        movimentoContabile.codlavoro = movimentoSelezionato.codlavoro
                        movimentoContabile.deslavoro = movimentoSelezionato.deslavoro

                        movimentoContabile.idProgetto = movimentoSelezionato.idprogetto
                        movimentoContabile.codProgetto = movimentoSelezionato.codprogetto
                        movimentoContabile.progetto = movimentoSelezionato.desprogetto

                        movimentoContabile.copFpv = movimentoSelezionato.copfpv

//                    } else {
//                        //     movimentoContabile.idCapitolo = movimentoSelezionato.idimpegno  giusto metterlo uguale sempre ?
//                        movimentoContabile.numero = movimentoSelezionato.numcapitolo
//                        movimentoContabile.numeroVariazione = movimentoSelezionato.codannuale //inserisco il numero dell'impegno originale
//                    }
                    movimentoContabile.sigCpt = movimentoSelezionato.sigcpt
                    movimentoContabile.desCpt = movimentoSelezionato.descpt

                    movimentoContabile.idCapitolo = movimentoSelezionato.idcapitolo
                    movimentoContabile.idImpegno = movimentoSelezionato.idimpegno

                    movimentoContabile.capitolo = movimentoSelezionato.codificacapitolo
                    movimentoContabile.descrizioneCapitolo = movimentoSelezionato.descapitolo
                    movimentoContabile.annoEsercizio = movimentoSelezionato.anno
                    movimentoContabile.annoCompetenza = movimentoSelezionato.annocompetenza
                    //Per trento le variazioni possono avere un anno di competenza diverso dall'impegno originale (e memorizzano il dato inserito dall'utente)
                    movimentoContabile.annoCompetenzaVariazione = annoCompetenza
                    movimentoContabile.descrizioneFinanziamento1 = movimentoSelezionato.descrizione

                    if ("SUBIMPEGNO_VARIAZIONE".equals(operazioneScelta)) {
                        movimentoContabile.descrizioneFinanziamento1 = movimentoSelezionato.descrizionebudget //????
                        movimentoContabile.numeroImpegnoPadre = movimentoSelezionato.codannuale
                    }

                    break
            }

            if (integrazioneContabilitaJSerfin.isContabilitaIntegrata(atto)) {
                creaImpegno(movimentoContabile, azioneWs)
            }
            else {
                salvaMovimentoContabile()
            }
        } else {
            //sono in modifica
            //Se l'utente ha modificato solo l'oggetto allora devo comunicare a JSerfin la modifica, se invece ha modificato anche gli altri campi
            //devo procedere alla cancellazione del movimento su jserfin per poi ricrearlo
            Boolean daModificare = false
            Boolean daEliminare = false

            if (!(oggettoApertura.equals(oggetto))) {
                daModificare = true
                daEliminare = false
            }

            if ((importoApertura != importo) || (!(cigApertura.equals(cig))) || (!(cupApertura.equals(cup)))) {
                daModificare = false
                daEliminare = true
            }

            Boolean impegnoEliminato = false
            if (daEliminare) {
                if (integrazioneContabilitaJSerfin.isContabilitaIntegrata(atto)) {
                    log.info("Prima di eliminare impegno su JSerfin: id = " + movimentoContabile.id)
                    integrazioneContabilitaJSerfin.eliminaImpegno(movimentoContabile)
                    log.info("Eliminato impegno su JSerfin: id = " + movimentoContabile.id)
                    impegnoEliminato = true
                }

                //modificaImpegno(atto, movimentoContabile, azioneWs)
                movimentoContabile.annoEsercizio = anno
                //campi che variano in base all'oggetto selezionato
                switch (operazioneScelta) {
                    case 'PRENOTAZIONE':
                    case 'IMPEGNO':
                    case 'IMPEGNO_PRENOTAZIONE':
                    case 'ACCERTAMENTO':
                    case 'SUBIMPEGNO':
                        azioneWs = "0"
                        break
                    case 'PRENOTAZIONE_VARIAZIONE':
                    case 'IMPEGNO_VARIAZIONE':
                    case 'ACCERTAMENTO_VARIAZIONE':
                        azioneWs = "1"
                        break
                }
            }

            if (daModificare) {
                azioneWs = "2"
            }

            if (daEliminare || daModificare) {
                if (integrazioneContabilitaJSerfin.isContabilitaIntegrata(atto)) {
                    log.info("JSerfin creaImpegno (da eliminare o modificare): idMovimentoContabile = " + movimentoContabile.id+" azioneWs = "+azioneWs+" impegnoELiminato = "+impegnoEliminato)
                    creaImpegno(movimentoContabile, azioneWs, impegnoEliminato)
                }
                else {
                    salvaMovimentoContabile()
                }
            }
        }
    }

    private void creaImpegno(MovimentoContabile movimentoContabile, String azioneWs, Boolean impegnoEliminato = false) {
        RispostaInserimentoImpegni risposta
        risposta = integrazioneContabilitaJSerfin.creaImpegno(atto, movimentoContabile, azioneWs, null)

        //in caso di errore restituisco il messaggio
        if ("OK".equals(risposta.messaggi.tipo)) {
            if (!("2".equals(azioneWs))) {
                //aggiorno gli id solo se ho creato un nuovo impegno (altrimenti perdo le chiavi di collegamento)
                movimentoContabile.idEsterno = risposta?.impegno[0]?.idimpegno?.toString() //id dell'impegno
                movimentoContabile.progressivoEsterno = risposta?.impegno[0]?.codannuale
                movimentoContabile.codiceGlobale = risposta?.impegno[0]?.codice
            }
            salvaMovimentoContabile()

            //codice annuale dell'impegno
        } else {
            if ("ERR".equals(risposta.messaggi.tipo)) {
                if (impegnoEliminato) {
                    movimentoContabile.delete()
                }
                if (risposta.messaggi.descrizione?.length() > 500) {
                    throw new Exception(risposta.messaggi.descrizione.substring(0, 500))
                } else {
                    throw new Exception(risposta.messaggi.descrizione)
                }
            } else {
                //il terzo caso � il WARN. Si deve mostrare il messaggio del ws all'utente che dovr� decidere se procedere o meno
                String messaggio = risposta.messaggi.messaggio_xml
                Messagebox.show(risposta.messaggi.descrizione + '\n\nContinuare ?', "Attenzione",
                        Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
                        new EventListener() {
                            void onEvent(Event e) {
                                if (Messagebox.ON_OK.equals(e.getName())) {
                                    confermaInserimento(risposta, azioneWs, messaggio, impegnoEliminato)
                                }
                            }
                        })
            }
        }
    }

    void confermaInserimento(RispostaInserimentoImpegni risposta, String azioneWs, String messaggio, Boolean impegnoEliminato) {
        risposta = integrazioneContabilitaJSerfin.creaImpegno(atto, movimentoContabile, azioneWs, messaggio)
        if (!risposta.messaggi.tipo.equals("OK")) {
            if (impegnoEliminato) {
                movimentoContabile.delete()
            }
            if (risposta.messaggi.descrizione?.length() > 500) {
                throw new Exception(risposta.messaggi.descrizione.substring(0, 500))
            } else {
                throw new Exception(risposta.messaggi.descrizione)
            }

        } else {
            movimentoContabile.idEsterno = risposta?.impegno[0]?.idimpegno?.toString()  //id dell'impegno
            movimentoContabile.progressivoEsterno = risposta?.impegno[0]?.codannuale    //codice annuale dell'impegno
            movimentoContabile.codiceGlobale = risposta?.impegno[0]?.codice             //codice globale dell'impegno

            salvaMovimentoContabile()
        }
    }

    void salvaMovimentoContabile() {
        movimentoContabile.save()

        Clients.showNotification("Movimento salvato con successo.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true)
        Events.postEvent(Events.ON_CLOSE, self, null)
    }
    @Command
    onDuplica() {
        MovimentoContabile newMovimentoContabile = new MovimentoContabile()
        //movimentoContabile = movimentoContabile
        //modifico i campi che potrebbero essere stati cambiati
        newMovimentoContabile.idDocumento = movimentoContabile.idDocumento
        newMovimentoContabile.tipoDocumento = movimentoContabile.tipoDocumento
        newMovimentoContabile.anagrafica = movimentoContabile.anagrafica

        newMovimentoContabile.importo = importo
        newMovimentoContabile.descrizione = oggetto + " (duplica)"
        newMovimentoContabile.cig = cig
        newMovimentoContabile.cup = cup

        newMovimentoContabile.descrizionePdcLiv5 = movimentoContabile.descrizionePdcLiv5
        newMovimentoContabile.pdcLiv5 = movimentoContabile.pdcLiv5

        newMovimentoContabile.tipoDettaglio = movimentoContabile.tipoDettaglio
        //PRENOTAZIONE - IMPEGNO - ACCERTAMENTO - SUBIMPEGNO
        newMovimentoContabile.opera = movimentoContabile.opera
        newMovimentoContabile.tipoUscita = movimentoContabile.tipoUscita
        newMovimentoContabile.idEsterno = movimentoContabile.idEsterno
        newMovimentoContabile.capitolo = movimentoContabile.capitolo
        newMovimentoContabile.idCapitolo = movimentoContabile.idCapitolo
        newMovimentoContabile.descrizioneCapitolo = movimentoContabile.descrizioneCapitolo
        newMovimentoContabile.numero = movimentoContabile.numero
        newMovimentoContabile.annoEsercizio = movimentoContabile.annoEsercizio
        newMovimentoContabile.annoCompetenza = movimentoContabile.annoCompetenza
        newMovimentoContabile.descrizioneFinanziamento1 = movimentoContabile.descrizioneFinanziamento1

        newMovimentoContabile.idProgetto = movimentoContabile.idProgetto
        newMovimentoContabile.codProgetto = movimentoContabile.codProgetto
        newMovimentoContabile.progetto = movimentoContabile.progetto
        newMovimentoContabile.copFpv = movimentoContabile.copFpv
        newMovimentoContabile.sigCpt = movimentoContabile.sigCpt
        newMovimentoContabile.desCpt = movimentoContabile.desCpt

        newMovimentoContabile.codcentrocosto = movimentoContabile.codcentrocosto
        newMovimentoContabile.descentrocosto = movimentoContabile.descentrocosto

        String azioneWs = "0"
        //campi che variano in base all'oggetto selezionato
        switch (movimentoContabile.tipoDettaglio) {
            case 'PRENOTAZIONE':
            case 'IMPEGNO':
            case 'ACCERTAMENTO':
                azioneWs = "0"
                break
            case 'PRENOTAZIONE_VARIAZIONE':
            case 'IMPEGNO_PRENOTAZIONE':
            case 'IMPEGNO_VARIAZIONE':
            case 'SUBIMPEGNO':
            case 'ACCERTAMENTO_VARIAZIONE':
                azioneWs = "1"
                break
        }

        if (integrazioneContabilitaJSerfin.isContabilitaIntegrata(atto)) {
            log.info("JSerfin creaImpegno (onDuplica): idMovimentoContabile = " + movimentoContabile.id+" azioneWs = "+azioneWs)
            creaImpegno(newMovimentoContabile, azioneWs)
        }
        newMovimentoContabile.save()

        Clients.showNotification("Movimento duplicato con successo.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true)
        Events.postEvent(Events.ON_CLOSE, self, null)

        Window w = Executions.createComponents("/atti/integrazioni/contabilita/popupSceltaMovimentiContabiliJSerfin.zul", null,
                [atto: atto, movimento: movimentoContabile, tipologia: null, duplica: true])

        w.doModal()
    }

    @Command
    onChiudi() {
        Events.postEvent(Events.ON_CLOSE, self, null)
    }

    @Command
    onInserimentoOggetto() {
        String descAnagrafica = ""
        if (anagrafica?.id > -1) {
            if (anagrafica.ragioneSociale) {
                descAnagrafica = anagrafica.ragioneSociale
            } else {
                descAnagrafica = anagrafica.cognome + " " + anagrafica.nome
            }

            descAnagrafica = descAnagrafica + " - "
        }
        oggetto = descAnagrafica + atto.oggetto
        if (oggetto?.length() > 100) {
            oggetto = oggetto.substring(0, 100)
        }

        BindUtils.postNotifyChange(null, null, this, "oggetto")
    }

    @Command
    onCheckOperazione(@ContextParam(ContextType.TRIGGER_EVENT) Event event) {
        if (event.target.checked) {
            operazioneScelta = event.target.value.toString()

            ricercaAbilitata = true
            listaContabile = []

            switch (operazioneScelta) {
                case 'PRENOTAZIONE':
                    labelRicercaForm = "Ricerca Capitolo"
                    labelRicercaNumero = "Numero Capitolo"
                    labelRicercaAnno = "Anno"
                    labelRicercaEmptyMessage = "Nessun capitolo trovato"
                    labelInserimentoError = "Selezionare un capitolo"
                    visElencoCapitoli = true
                    visElencoPrenotazioni = false
                    annoCompetenzaVisibile = false
                    importiNegativi = false
                    visCodiceGlobale = false

                    break

                case 'PRENOTAZIONE_VARIAZIONE':
                    labelRicercaForm = "Ricerca Prenotazione"
                    labelRicercaNumero = "Numero Prenotazione"
                    labelRicercaAnno = "Anno"
                    labelRicercaEmptyMessage = "Nessuna prenotazione trovata"
                    labelInserimentoError = "Selezionare una prenotazione"
                    visElencoCapitoli = false
                    visElencoPrenotazioni = true
                    visElencoImpegni = false
                    visElencoSubImpegni = false
                    visElencoAccertamenti = false
                    annoCompetenzaVisibile = true
                    importiNegativi = true
                    visCodiceGlobale = isRicercaMovimentiCodiceGlobale
                    break

                case 'IMPEGNO':
                    labelRicercaForm = "Ricerca Capitolo"
                    labelRicercaNumero = "Numero Capitolo"
                    labelRicercaAnno = "Anno"
                    labelRicercaEmptyMessage = "Nessun capitolo trovato"
                    labelInserimentoError = "Selezionare un capitolo"
                    visElencoCapitoli = true
                    visElencoPrenotazioni = false
                    visElencoImpegni = false
                    visElencoSubImpegni = false
                    visElencoAccertamenti = false
                    annoCompetenzaVisibile = false
                    importiNegativi = false
                    visCodiceGlobale = false
                    break

                case 'IMPEGNO_PRENOTAZIONE':
                    labelRicercaForm = "Ricerca Prenotazione"
                    labelRicercaNumero = "Numero Prenotazione"
                    labelRicercaAnno = "Anno"
                    labelRicercaEmptyMessage = "Nessuna prenotazione trovata"
                    labelInserimentoError = "Selezionare una prenotazione"
                    visElencoCapitoli = false
                    visElencoPrenotazioni = true
                    visElencoImpegni = false
                    visElencoSubImpegni = false
                    visElencoAccertamenti = false
                    annoCompetenzaVisibile = true
                    importiNegativi = false
                    visCodiceGlobale = isRicercaMovimentiCodiceGlobale
                    break

                case 'IMPEGNO_VARIAZIONE':
                    labelRicercaForm = "Ricerca Impegno"
                    labelRicercaNumero = "Numero Impegno"
                    labelRicercaAnno = "Anno"
                    labelRicercaEmptyMessage = "Nessun impegno trovato"
                    labelInserimentoError = "Selezionare un impegno"
                    visElencoCapitoli = false
                    visElencoPrenotazioni = false
                    visElencoImpegni = true
                    visElencoSubImpegni = false
                    visElencoAccertamenti = false
                    annoCompetenzaVisibile = true
                    importiNegativi = true
                    visCodiceGlobale = isRicercaMovimentiCodiceGlobale
                    break

                case 'SUBIMPEGNO':
                    labelRicercaForm = "Ricerca Impegno"
                    labelRicercaNumero = "Numero Impegno"
                    labelRicercaAnno = "Anno"
                    labelRicercaEmptyMessage = "Nessun impegno trovato"
                    labelInserimentoError = "Selezionare un impegno"
                    visElencoCapitoli = false
                    visElencoPrenotazioni = false
                    visElencoImpegni = true
                    visElencoSubImpegni = false
                    visElencoAccertamenti = false
                    annoCompetenzaVisibile = true
                    importiNegativi = false
                    visCodiceGlobale = isRicercaMovimentiCodiceGlobale
                    break

                case 'SUBIMPEGNO_VARIAZIONE':
                    labelRicercaForm = "Ricerca Sub-Impegno"
                    labelRicercaNumero = "Numero Sub-Impegno"
                    labelRicercaAnno = "Anno"
                    labelRicercaEmptyMessage = "Nessun sub-impegno trovato"
                    labelInserimentoError = "Selezionare un sub-impegno"
                    visElencoCapitoli = false
                    visElencoPrenotazioni = false
                    visElencoImpegni = false
                    visElencoSubImpegni = true
                    visElencoAccertamenti = false
                    annoCompetenzaVisibile = true
                    importiNegativi = true
                    visCodiceGlobale = isRicercaMovimentiCodiceGlobale
                    break

                case 'ACCERTAMENTO':
                    labelRicercaForm = "Ricerca Capitolo"
                    labelRicercaNumero = "Numero Capitolo"
                    labelRicercaAnno = "Anno"
                    labelRicercaEmptyMessage = "Nessun capitolo trovato"
                    visElencoCapitoli = true
                    visElencoPrenotazioni = false
                    visElencoImpegni = false
                    visElencoSubImpegni = false
                    visElencoAccertamenti = false
                    annoCompetenzaVisibile = false
                    importiNegativi = false
                    visCodiceGlobale = false
                    break

                case 'ACCERTAMENTO_VARIAZIONE':
                    labelRicercaForm = "Ricerca Accertamento"
                    labelRicercaNumero = "Numero Accertamento"
                    labelRicercaAnno = "Anno"
                    labelRicercaEmptyMessage = "Nessun accertamento trovato"
                    labelInserimentoError = "Selezionare un accertamento"
                    visElencoCapitoli = false
                    visElencoPrenotazioni = false
                    visElencoImpegni = false
                    visElencoSubImpegni = false
                    visElencoAccertamenti = true
                    annoCompetenzaVisibile = true
                    importiNegativi = true
                    visCodiceGlobale = isRicercaMovimentiCodiceGlobale
                    break
            }
        }

        BindUtils.postNotifyChange(null, null, this, "visElencoCapitoli")
        BindUtils.postNotifyChange(null, null, this, "visElencoPrenotazioni")
        BindUtils.postNotifyChange(null, null, this, "visElencoImpegni")
        BindUtils.postNotifyChange(null, null, this, "visElencoSubImpegni")
        BindUtils.postNotifyChange(null, null, this, "visElencoAccertamenti")
        BindUtils.postNotifyChange(null, null, this, "visCodiceGlobale")
        BindUtils.postNotifyChange(null, null, this, "labelRicercaForm")
        BindUtils.postNotifyChange(null, null, this, "labelRicercaNumero")
        BindUtils.postNotifyChange(null, null, this, "labelRicercaAnno")
        BindUtils.postNotifyChange(null, null, this, "labelAnnoEsigibilita")
        BindUtils.postNotifyChange(null, null, this, "labelRicercaEmptyMessage")
        BindUtils.postNotifyChange(null, null, this, "annoCompetenzaVisibile")
        BindUtils.postNotifyChange(null, null, this, "importiNegativi")
        BindUtils.postNotifyChange(null, null, this, "listaContabile")
    }

    void getCapitoli(Integer numero, Integer anno, Integer codiceLavoro, String movimento) {
        //movimento: E = entrata, usato per gli accertamenti
        //movimento: U = uscita, usato per impegni e prenotazioni
        Long disponibilita
        RispostaRicercaBudgetBilancio response = integrazioneContabilitaJSerfin.getCapitoli(numero, anno, codiceLavoro)
        boolean consideroElemento = true
        listaContabile = []

        if (response?.lista_budget?.budget) {
            // costruzione della lista
            response?.lista_budget?.budget.each {
                if ((it.parte).equals(movimento)) {
                    consideroElemento = true
                }
                else {
                    consideroElemento = false
                }

                if (consideroElemento) {
                    if (it.stanziamento > 0) {
                        consideroElemento = true
                    }
                    else {
                        consideroElemento = false
                    }
                }

                if (consideroElemento) {
                    //nel mapping stabilisco se visualizzare solo i capitoli con copfpv = 0 o se considerarli tutti
                    if (!(integrazioneContabilitaJSerfin.isVisualizzareCapitoliFPV())) {
                        if (it.copfpv == 0) {
                            consideroElemento = true
                        }
                        else {
                            consideroElemento = false
                        }
                    }
                }

                if (consideroElemento) {
                    listaContabile.addAll([parte                    : it.parte,
                                           idcapitolo               : it.idcapitolo,
                                           descrizione              : it.descrizione,
                                           numcapitolo              : it.numcapitolo,
                                           annocompetenza           : it.annocompetenza,
                                         //  disponibile              : disponibilita,
                                           codcentrocosto           : it.codcentrocosto,
                                           descentrocosto           : it.descentrocosto,
                                           idprogetto               : it.idprogetto,
                                           codprogetto              : it.codprogetto,
                                           desprogetto              : it.desprogetto,
                                           idtipofinanz             : it.idtipofinanz,
                                           codtipofinanz            : it.codtipofinanz,
                                           destipofinanz            : it.destipofinanz,
                                           idlavoro                 : it.idlavoro,
                                           codlavoro                : it.codlavoro,
                                           deslavoro                : it.deslavoro,
                                           idtipospesa              : it.idtipospesa,
                                           codtipospesa             : it.codtipospesa,
                                           destipospesa             : it.destipospesa,
                                           copfpv                   : it.copfpv,
                                           sigcpt                   : it.sigcpt,
                                           descpt                   : it.descpt,
                                           sigpianofin              : it.sigpianofin,
                                           despianofin              : it.despianofin,
                                           codifica                 : it.codifica,
                                           sigpianofin              : it.sigpianofin,
                                           despianofin              : it.despianofin,
                                           sigpianofincapitolo      : it.sigpianofincapitolo,
                                           despianofincapitolo      : it.despianofincapitolo,
                                           disponibile              : it.disponibile,
                                           codrespservcapitolo      : it.codrespservcapitolo,
                                           desrespservcapitolo      : it.desrespservcapitolo,
                                           descrizionebudget        : pulisciDescrizione(it.descrizionebudget),
                                           lista_piani_fin_collegati: elencoPianiFinanziari(it.lista_piani_fin_collegati, it.sigpianofincapitolo, it.despianofincapitolo),
                                           vLivelloObbligatorio     : (it.lista_piani_fin_collegati?.piano_fin?.size() && isVLivelloObbligatorio)])
                }
            }

            if (listaContabile.size() == 0) {
                Clients.showNotification("Nessun capitolo trovato", Clients.NOTIFICATION_TYPE_WARNING, null, "middle_center", 5000, true);
                labelRicercaEmptyMessage = "Nessun capitolo trovato"
            }
        } else {
            //non ho dei budget di ritorno....visualizzo il messaggio se presente nella chiamata
            if (response?.esito?.tipo?.equals(Esito.OK)) {
                if (response?.esito?.descrizione)
                    Clients.showNotification(response?.esito?.descrizione, Clients.NOTIFICATION_TYPE_WARNING, null, "middle_center", 5000, true);

                labelRicercaEmptyMessage = "Nessun capitolo trovato"
            }
        }
    }

    List<Map<String, String>> elencoPianiFinanziari(it.finmatica.atti.integrazioniws.sicraweb.classes.ricercaBudgetBilancio.ListaPianiFinCollegati lista_piani_fin_collegati, String sigpianofincapitolo, String despianofincapitolo) {
        List<Map<String, String>> risultato = []
        for (def piano in lista_piani_fin_collegati) {
            if (piano?.piano_fin) {
                for (it.finmatica.atti.integrazioniws.sicraweb.classes.ricercaBudgetBilancio.PianoFin valore in piano?.piano_fin) {
                    risultato.add(new HashMap<String, String>(["sigla": valore.sigla, "descrizione": valore.descrizione]))
                }
            }
        }

        if (risultato.size() == 0) {
            risultato.add(new HashMap<String, String>(["sigla": sigpianofincapitolo, "descrizione": despianofincapitolo]))
        }
        return risultato
    }

    String pulisciDescrizione(String riga) {
        riga = riga.replaceAll('Budget di Spesa:\n', '')
        riga = riga.replaceAll('Centro di costo: \n', '')
        riga = riga.replaceAll('Piano Finanziario: \n', '')
        riga = riga.replaceAll('Piano dei conti: \n', '')
        riga = riga.replaceAll('Lavoro: \n', '')
        riga = riga.replaceAll('Tipo spesa: \n', '')
        riga = riga.replaceAll('Tipo finanziamento: \n', '')
        riga = riga.replaceAll('Programma/Progetto: \n', '')
        riga = riga.replaceAll('Coperto da F.P.V.: \n', '')

        return riga
    }

    void getPrenotazioni(Integer numero, Integer anno, Integer annoCompetenza, Long codiceGlobale) {
        listaContabile = []
        def response = integrazioneContabilitaJSerfin.getPrenotazioni(numero, anno, annoCompetenza, codiceGlobale)
        /*
            L'xml ritorna i seguenti tag:
            idImpegno: id della prenotazione
            idCapitolo: id del capitolo
         */

        // costruzione della lista
        response.each {
            listaContabile.addAll([parte           : it.parte,
                                   idcapitolo      : it.idcapitolo,
                                   descrizione     : it.descrizione,
                                   numcapitolo     : it.numcapitolo,
                                   annocompetenza  : it.annocompetenza,
                                   idimpegno       : it.idimpegno,
                                   codificacapitolo: it.codificacapitolo,
                                   descapitolo     : it.descapitolo,
                                   codannuale      : it.codannuale,
                                   anno            : it.anno,
                                   disponibile     : it.prenotatodisponibile,
                                   idcentrocosto   : it.idcentrocosto,
                                   codcentrocosto  : it.codcentrocosto,
                                   descentrocosto  : it.descentrocosto,
                                   idlavoro        : it.idlavoro,
                                   codlavoro       : it.codlavoro,
                                   deslavoro       : it.deslavoro,
                                   idtipofinanz    : it.idtipofinanz,
                                   codtipofinanz   : it.codtipofinanz,
                                   destipofinanz   : it.destipofinanz,
                                   idprogetto      : it.idprogetto,
                                   codprogetto     : it.codprogetto,
                                   desprogetto     : it.desprogetto,
                                   idcpt           : it.idcpt,
                                   sigcpt          : it.sigcpt,
                                   descpt          : it.descpt,
                                   copfpv          : it.copertofpv,
                                   sigpianofin     : it.sigpianofin,
                                   despianofin     : it.despianofin])
        }
    }

    void getImpegni(Integer numero, Integer anno, Integer annoCompetenza, Long codiceGlobale) {
        def response = integrazioneContabilitaJSerfin.getImpegni(numero, anno, annoCompetenza, codiceGlobale)
        listaContabile = []

        // costruzione della lista
        response.each {
            listaContabile.addAll([parte           : it.parte,
                                   idimpegno       : it.idimpegno,
                                   codannuale      : it.codannuale,
                                   anno            : it.anno,
                                   datavaluta      : it.datavaluta,
                                   descrizione     : it.descrizione,
                                   idcapitolo      : it.idcapitolo,
                                   numcapitolo     : it.numcapitolo,
                                   codificacapitolo: it.codificacapitolo,
                                   descapitolo     : it.descapitolo,
                                   annocompetenza  : it.annocompetenza,
                                   impegnato       : it.impegnato,
                                   disponibile     : it.disponibile,
                                   mandatiemessi   : it.mandatiemessi,
                                   documentiaperti : it.documentiaperti,
                                   idcentrocosto   : it.idcentrocosto,
                                   codcentrocosto  : it.codcentrocosto,
                                   descentrocosto  : it.descentrocosto,
                                   idlavoro        : it.idlavoro,
                                   codlavoro       : it.codlavoro,
                                   deslavoro       : it.deslavoro,
                                   idtipofinanz    : it.idtipofinanz,
                                   codtipofinanz   : it.codtipofinanz,
                                   destipofinanz   : it.destipofinanz,
                                   idprogetto      : it.idprogetto,
                                   codprogetto     : it.codprogetto,
                                   desprogetto     : it.desprogetto,
                                   idcpt           : it.idcpt,
                                   sigcpt          : it.sigcpt,
                                   descpt          : it.descpt,
                                   sigpianofin     : it.sigpianofin,
                                   despianofin     : it.despianofin,
                                   copfpv          : it.copertofpv,
                                   vLivello        : new HashMap<String, String>(["sigla": it.sigcpt, "descrizione": it.descpt])
            ])
        }
    }

    void getSubImpegni(Integer numero, Integer anno, Integer annoCompetenza, Long codiceGlobale) {
        def response = integrazioneContabilitaJSerfin.getSubImpegni(numero, anno, annoCompetenza, codiceGlobale)
        listaContabile = []

        // costruzione della lista
        response.each {
            listaContabile.addAll([parte           : it.parte,
                                   idimpegno       : it.idimpegno,
                                   codannuale      : it.codannuale,
                                   anno            : it.anno,
                                   datavaluta      : it.datavaluta,
                                   descrizione     : it.descrizione,
                                   idcapitolo      : it.idcapitolo,
                                   numcapitolo     : it.numcapitolo,
                                   codificacapitolo: it.codificacapitolo,
                                   descapitolo     : it.descapitolo,
                                   annocompetenza  : it.annocompetenza,
                                   impegnato       : it.impegnato,
                                   disponibile     : it.disponibile,
                                   mandatiemessi   : it.mandatiemessi,
                                   documentiaperti : it.documentiaperti,
                                   idcentrocosto   : it.idcentrocosto,
                                   codcentrocosto  : it.codcentrocosto,
                                   descentrocosto  : it.descentrocosto,
                                   idlavoro        : it.idlavoro,
                                   codlavoro       : it.codlavoro,
                                   deslavoro       : it.deslavoro,
                                   idtipofinanz    : it.idtipofinanz,
                                   codtipofinanz   : it.codtipofinanz,
                                   destipofinanz   : it.destipofinanz,
                                   idprogetto      : it.idprogetto,
                                   codprogetto     : it.codprogetto,
                                   desprogetto     : it.desprogetto,
                                   idcpt           : it.idcpt,
                                   sigcpt          : it.sigcpt,
                                   descpt          : it.descpt,
                                   copfpv          : it.copertofpv])
        }
    }


    void getAccertamenti(Integer numero, Integer anno, Integer annoCompetenza, Long codiceGlobale) {
        def response = integrazioneContabilitaJSerfin.getAccertamenti(numero, anno, annoCompetenza, codiceGlobale)
        listaContabile = []

        // costruzione della lista
        response.each {
            listaContabile.addAll([parte           : it.parte,
                                   idimpegno       : it.idimpegno,
                                   codannuale      : it.codannuale,
                                   anno            : it.anno,
                                   datavaluta      : it.datavaluta,
                                   descrizione     : it.descrizione,
                                   idcapitolo      : it.idcapitolo,
                                   numcapitolo     : it.numcapitolo,
                                   codificacapitolo: it.codificacapitolo,
                                   descapitolo     : it.descapitolo,
                                   annocompetenza  : it.annocompetenza,
                                   impegnato       : it.impegnato,
                                   disponibile     : it.disponibile,
                                   mandatiemessi   : it.mandatiemessi,
                                   documentiaperti : it.documentiaperti,
                                   idcentrocosto   : it.idcentrocosto,
                                   codcentrocosto  : it.codcentrocosto,
                                   descentrocosto  : it.descentrocosto,
                                   idlavoro        : it.idlavoro,
                                   codlavoro       : it.codlavoro,
                                   deslavoro       : it.deslavoro,
                                   idtipofinanz    : it.idtipofinanz,
                                   codtipofinanz   : it.codtipofinanz,
                                   destipofinanz   : it.destipofinanz,
                                   idprogetto      : it.idprogetto,
                                   codprogetto     : it.codprogetto,
                                   desprogetto     : it.desprogetto,
                                   idcpt           : it.idcpt,
                                   sigcpt          : it.sigcpt,
                                   descpt          : it.descpt,
                                   copfpv          : it.copertofpv,
                                   sigpianofin     : it.sigpianofin,
                                   despianofin     : it.despianofin])
        }
    }
}