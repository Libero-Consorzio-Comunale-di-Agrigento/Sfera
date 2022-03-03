package atti.integrazioni.contabilita

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.contabilita.MovimentoContabileInterno
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IProposta
import it.finmatica.atti.dto.contabilita.MovimentoContabileInternoDTO
import it.finmatica.atti.integrazioni.contabilita.IntegrazioneContabilitaAscotWeb
import net.sf.json.JSONArray
import org.apache.log4j.Logger
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.ContextParam
import org.zkoss.bind.annotation.ContextType
import org.zkoss.bind.annotation.ExecutionArgParam
import org.zkoss.bind.annotation.Init
import org.zkoss.bind.annotation.NotifyChange
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

import java.text.SimpleDateFormat

class PopupSceltaMovIntViewModel {

    public static final Logger log = Logger.getLogger(PopupSceltaMovIntViewModel.class);

    // service
    SpringSecurityService           springSecurityService
    IntegrazioneContabilitaAscotWeb integrazioneContabilitaAscotWeb

    // componenti
    Window self

    // dati
    IProposta        atto
    def              selectedRecord
    Date             date                     = new Date()
    SimpleDateFormat dateFormatter            = new SimpleDateFormat("yyyy")
    String           note
    boolean          gestioneMovimentiInterni = true
    String           labelSalva               = ""
    String           labelTitolo              = ""

    // dati capitolo
    int        esercizio
    int        esercizioProvenienza
    String     eos
    String     capitolo
    String     articolo
    List       listaCapitoliFiltrati = []
    int        totalSize
    def        selected
    BigDecimal importo

    // dati soggetto
    String piva
    String cf
    String cognome
    String nome
    String idSoggetto
    List   listaSoggettiFiltrati = []
    def    soggettoSelezionato

    @NotifyChange("listaUnita")
    @Init
    init(@ContextParam(ContextType.COMPONENT) Window w
         , @ExecutionArgParam("atto") IProposta atto
         , @ExecutionArgParam("movimento") MovimentoContabileInternoDTO movimento) {

        this.self = w
        this.atto = atto

        if (movimento?.id == null) {
            // creazione
            labelSalva = "Inserisci"
            labelTitolo = "Aggiungi Movimento Contabile"
            eos = "S"
            esercizio = dateFormatter.format(date).toInteger()
            esercizioProvenienza = dateFormatter.format(date).toInteger()
        } else {
            // modifica
            labelSalva = "Salva"
            labelTitolo = "Modifica Movimento Contabile"
            selectedRecord = caricaMovimentoDto(movimento?.id)
            eos = selectedRecord.eos.toString()
            esercizio = selectedRecord.esercizio.toInteger()
            esercizioProvenienza = selectedRecord.epf.toInteger()
            capitolo = selectedRecord.capitolo.toString()
            articolo = selectedRecord.articolo.toString()
            idSoggetto = selectedRecord.progressivoSoggetto
            note = selectedRecord.note
            importo = selectedRecord.importo
            aggiornaMovimento()
        }
    }

    private aggiornaMovimento() {
        BindUtils.postNotifyChange(null, null, this, 'eos')
        BindUtils.postNotifyChange(null, null, this, 'esercizio')
        BindUtils.postNotifyChange(null, null, this, 'esercizioProvenienza')
        BindUtils.postNotifyChange(null, null, this, 'articolo')
        BindUtils.postNotifyChange(null, null, this, 'capitolo')
        BindUtils.postNotifyChange(null, null, this, "note")
        BindUtils.postNotifyChange(null, null, this, 'importo')
        BindUtils.postNotifyChange(null, null, this, 'idSoggetto')
        onCercaSoggetti()
        onCercaCapitoli()
    }

    private MovimentoContabileInternoDTO caricaMovimentoDto(Long id) {
        MovimentoContabileInterno movimentoContabileInterno = MovimentoContabileInterno.createCriteria().get {
            eq("id", id)
        }
        return movimentoContabileInterno.toDTO()
    }

    @NotifyChange(["listaCapitoliFiltrati", "totalSize"])
    @Command
    onSeleziona() {
        String msg

        if (soggettoSelezionato == null || importo == null || selected[0] == null) {
            Messagebox.show("Non è possibile salvare il movimento, selezionare capitolo, soggetto ed importo.", "Inserimento", Messagebox.OK, Messagebox.EXCLAMATION)
            return
        }

        MovimentoContabileInterno m = null
        if (selectedRecord?.id == null) {
            m = new MovimentoContabileInterno()
            msg = "inserito"
        } else {
            msg = "salvato"
            m = MovimentoContabileInterno.get(selectedRecord?.id.toLong())
        }

        if (atto instanceof Determina) {
            m.determina = atto
        } else {
            m.propostaDelibera = atto
        }

        m.esercizio = esercizio
        m.capitolo = selected[0].capitolo
        m.descrizioneCapitolo = selected[0].descrizione
        m.articolo = selected[0].articolo
        m.epf = esercizioProvenienza
        m.pdcf = selected[0].pdcf
        m.eos = eos
        m.importo = importo
        m.progressivoSoggetto = soggettoSelezionato.progressivo
        m.descrizioneSoggetto = soggettoSelezionato.progressivo + " - " + soggettoSelezionato.cognome + " " + soggettoSelezionato.nome + " - " + soggettoSelezionato.indirizzo + " " + soggettoSelezionato.localita + " " + soggettoSelezionato.comune + " (" + soggettoSelezionato.provincia + ") c.f. " + soggettoSelezionato.CF + " p.i. " + soggettoSelezionato.PIVA
        m.codiceMissione = selected[0].codiceMissione
        m.codiceProgramma = selected[0].codiceProgramma
        m.note = note

        m.cognome = soggettoSelezionato.cognome
        m.nome = soggettoSelezionato.nome
        m.cf = soggettoSelezionato.CF
        m.piva = soggettoSelezionato.PIVA
        m.cfEstero = soggettoSelezionato.CFestero
        m.pivaEstero = soggettoSelezionato.PIVAestero
        m.indirizzo = soggettoSelezionato.indirizzo
        m.localita = soggettoSelezionato.localita
        m.comune = soggettoSelezionato.comune
        m.provincia = soggettoSelezionato.provincia
        m.cap = soggettoSelezionato.cap
        m.stato = soggettoSelezionato.stato
        m.telefono = soggettoSelezionato.telefono
        m.email = soggettoSelezionato.email
        m.pec = soggettoSelezionato.pec

        m.save()

        Messagebox.show("Movimento " + msg + " correttamente.", "", Messagebox.OK, Messagebox.INFORMATION)
    }

    @Command
    onChiudiPopup() {
        Events.postEvent(Events.ON_CLOSE, self, null)
    }

    @NotifyChange(["listaCapitoliFiltrati", "totalSize"])
    @Command
    onCercaCapitoli() {

        listaCapitoliFiltrati = []
        if (esercizio == null || esercizioProvenienza == null || capitolo == null || articolo == null) {
            Messagebox.show("Valorizzare tutti i campi per effettura la ricerca.", "Cerca", Messagebox.OK, Messagebox.EXCLAMATION)
            return
        }

        JSONArray response = integrazioneContabilitaAscotWeb.getCapitoliMovInt(esercizio.toString(), esercizioProvenienza.toString(), eos.toString(), capitolo.toString(), articolo.toString())

        // costruzione della lista
        response.each {
            listaCapitoliFiltrati.addAll([capitolo: it.capitolo, descrizione: it.descrizione, disponibilita: it.disponibilita, articolo: it.articolo, pdcf: it.PDCF.codice, codiceMissione: it.codiceMissione, codiceProgramma: it.codiceProgramma])
        }
        totalSize = listaCapitoliFiltrati.size()

        if (selectedRecord?.id != null) {
            selected = listaCapitoliFiltrati
        }
        BindUtils.postNotifyChange(null, null, this, "listaCapitoliFiltrati")
        BindUtils.postNotifyChange(null, null, this, "totalSize")
    }

    @NotifyChange(["listaSoggettiFiltrati"])
    @Command
    onCercaSoggetti() {
        if (selectedRecord?.id == null) {
            if (cognome == null || cognome.length() < 3) {
                if (nome == null || nome.length() < 3) {
                    Messagebox.show("Inserire almeno tre caratteri nel campo\n Cognome/Ragione Sociale o Nome per la ricerca del soggetto.", "Cerca", Messagebox.OK, Messagebox.EXCLAMATION)
                    return
                }
            }
        }
        listaSoggettiFiltrati = []
        JSONArray response = integrazioneContabilitaAscotWeb.getSoggetti(cognome.toString(), nome.toString(), cf.toString(), piva.toString(), idSoggetto.toString())

        // costruzione della lista
        response.each {
            listaSoggettiFiltrati.addAll([cognome: it.cognome.toString().replace('null', ''), nome: it.nome.toString().replace('null', ''), CF: it.CF.toString().replace('null', ''), CFestero: it.CFestero.toString().replace('null', ''), PIVA: it.PIVA.toString().replace('null', ''), PIVAestero: it.PIVAestero.toString().replace('null', ''), cap: it.cap.toString().replace('null', ''), comune: it.comune.toString().replace('null', ''), email: it.email.toString().replace('null', ''), indirizzo: it.indirizzo.toString().replace('null', ''), localita: it.localita.toString().replace('null', ''), pec: it.pec.toString().replace('null', ''), provincia: it.provincia.toString().replace('null', ''), stato: it.stato.toString().replace('null', ''), telefono: it.telefono.toString().replace('null', ''), progressivo: it.progressivo.toString().replace('null', ''), combo: it.progressivo.toString().replace('null', '') + " - " + it.cognome.toString().replace('null', '') + " " + it.nome.toString().replace('null', '') + " - " + it.indirizzo.toString().replace('null', '') + " " + it.localita.toString().replace('null', '') + " " + it.comune.toString().replace('null', '') + " (" + it.provincia.toString().replace('null', '') + ")"])
        }
        soggettoSelezionato = listaSoggettiFiltrati[0]
        BindUtils.postNotifyChange(null, null, this, "listaSoggettiFiltrati")
    }
}
