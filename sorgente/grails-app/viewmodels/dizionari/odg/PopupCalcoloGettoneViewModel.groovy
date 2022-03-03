package dizionari.odg

import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.atti.odg.Commissione
import it.finmatica.atti.odg.CommissioneComponente
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Filedownload
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

import java.text.SimpleDateFormat

class PopupCalcoloGettoneViewModel {

	// service
	GestioneTestiService gestioneTestiService

	// componenti
	Window self

	// dati
	List<CommissioneDTO> listaCommissione
	Date periodoDal
	Date periodoAl
	List<As4SoggettoCorrenteDTO> listaPartecipanti

	CommissioneDTO selectedCommissione
	As4SoggettoCorrenteDTO selectedPartecipante

	// stato

	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("dataSelezionata") Date dataSelezionata) {
		this.self = w
		// ottengo il primo giorno del mese:
		this.periodoDal = dataSelezionata.updated(date:1).clearTime()
		// ottengo l'ultimo giorno del mese:
		this.periodoAl = (periodoDal.updated(month:periodoDal[Calendar.MONTH]+1))-1;
		caricaListaCommissione()
	}

	private void caricaListaCommissione() {
		listaCommissione = Commissione.createCriteria().list() {
			order("titolo","asc")
		}.toDTO()
		BindUtils.postNotifyChange(null, null, this, "listaCommissione")
	}

	@Command onChangeCommissione() {
		if(selectedCommissione != null){
			List<As4SoggettoCorrenteDTO> listaComponenti = CommissioneComponente.createCriteria().list() {
				projections{
					distinct("componente")
				}
				eq("commissione.id", selectedCommissione.id)
			}.toDTO()

			As4SoggettoCorrenteDTO tutti = new As4SoggettoCorrenteDTO([id: -1, nome: "TUTTI", cognome:""])
			listaPartecipanti = new ArrayList<As4SoggettoCorrenteDTO>()
			listaPartecipanti.add(tutti)
			listaPartecipanti.addAll(listaComponenti)
			selectedPartecipante = tutti
		} else {
			listaPartecipanti = null
			selectedPartecipante = null
		}

		BindUtils.postNotifyChange(null, null, this, "listaPartecipanti")
		BindUtils.postNotifyChange(null, null, this, "selectedPartecipante")
	}

    private void stampa (String codiceTipoModello, String nomeFile) {
        if (periodoDal == null ||
                periodoAl  == null ||
                selectedCommissione == null ||
                selectedPartecipante == null) {
            Messagebox.show("Per proseguire è necessario selezionare tutti i campi", "Attenzione", Messagebox.OK , Messagebox.EXCLAMATION)
            return;
        }

        // prendo la query del modello testo adibito alla stampa dei gettoni
        List<GestioneTestiModello> modelli = GestioneTestiModello.createCriteria().list {
            eq ("tipoModello.codice", codiceTipoModello)
            eq ("valido", true)
        }

        if (modelli.size() > 1) {
            Messagebox.show("Nei dizionari è stato definito più di un modello per la stampa di '${nomeFile}', è necessario definirne uno solo.", "Attenzione", Messagebox.OK, Messagebox.EXCLAMATION)
            return
        }

        GestioneTestiModello modello = modelli[0]
        if (modello == null) {
            Messagebox.show("Nei dizionari non è stato definito il modello di testo da utilizzare per la stampa di '${nomeFile}'", "Attenzione", Messagebox.OK, Messagebox.EXCLAMATION)
            return
        }

        SimpleDateFormat ft 	= new SimpleDateFormat ("dd/MM/yyyy")
        def mappaParametri 		= [:]
        mappaParametri["id"] 	= selectedCommissione.id
        mappaParametri["ni"] 	= selectedPartecipante.id
        mappaParametri["dal"] 	= ft.format(periodoDal)
        mappaParametri["al"] 	= ft.format(periodoAl)

		InputStream testoPdf = gestioneTestiService.stampaUnione(modello, mappaParametri, GestioneTestiService.FORMATO_PDF)
		Filedownload.save(testoPdf, GestioneTestiService.getContentType(GestioneTestiService.FORMATO_PDF), "${nomeFile}.pdf")
    }

	@Command onCalcola () {
        stampa ("ODG_GETTONE_PRESENZA", "Gettoni Presenza")
	}

	@Command onStampaStatistiche () {
        stampa ("ODG_STATISTICHE_PRESENZA", "Statistiche di Presenza")
	}

	@Command onAnnulla() {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
