package dizionari.odg

import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.atti.dto.odg.SedutaPartecipanteDTO
import it.finmatica.atti.export.ExportService
import it.finmatica.atti.odg.Commissione
import it.finmatica.atti.odg.CommissioneComponente
import it.finmatica.atti.integrazioni.SedutaExcel
import it.finmatica.atti.odg.SedutaPartecipante
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import org.hibernate.FetchMode
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
	ExportService exportService

	// componenti
	Window self

	// dati
	List<CommissioneDTO> listaCommissione
	Date periodoDal
	Date periodoAl
	List<As4SoggettoCorrenteDTO> listaPartecipanti

	CommissioneDTO selectedCommissione
	As4SoggettoCorrenteDTO selectedPartecipante

	boolean stampaAttestazioneVisible = false

	// stato

	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("dataSelezionata") Date dataSelezionata) {
		this.self = w
		// ottengo il primo giorno del mese:
		this.periodoDal = dataSelezionata.updated(date:1).clearTime()
		// ottengo l'ultimo giorno del mese:
		this.periodoAl = (periodoDal.updated(month:periodoDal[Calendar.MONTH]+1))-1;
		caricaListaCommissione()

		caricaListaPartecipanti()

		isAttestazioneVisibile()

	}

	private void isAttestazioneVisibile() {
		stampaAttestazioneVisible = false
		// prendo la query del modello testo adibito alla stampa dei gettoni
		List<GestioneTestiModello> modelli = GestioneTestiModello.createCriteria().list {
			eq ("tipoModello.codice", "ODG_ATTESTAZIONE_PRESENZA")
			eq ("valido", true)
		}

		if (modelli.size() > 0) {
			stampaAttestazioneVisible = true
		}
		BindUtils.postNotifyChange(null, null, this, "stampaAttestazioneVisible")
	}

	private void caricaListaPartecipanti(){
		As4SoggettoCorrenteDTO tutti = new As4SoggettoCorrenteDTO([id: -1, nome: "TUTTI", cognome:""])
		listaPartecipanti = new ArrayList<As4SoggettoCorrenteDTO>()
		listaPartecipanti.add(tutti)

		if(selectedCommissione.id==-1) {
			List<As4SoggettoCorrenteDTO> listaComponenti = CommissioneComponente.createCriteria().list() {
				projections {
					distinct("componente")
				}
				//eq("nome", selectedCommissione.id)
			}.toDTO()

			Collections.sort(listaComponenti, new Comparator<As4SoggettoCorrenteDTO>(){

				public int compare(As4SoggettoCorrenteDTO o1, As4SoggettoCorrenteDTO o2)
				{
					return o1.cognome.compareTo(o2.cognome);
				}
			})

			for(As4SoggettoCorrenteDTO componente : listaComponenti){
				if(componente.nome != null)
					listaPartecipanti.add(componente)
			}


		}

		selectedPartecipante = tutti

		BindUtils.postNotifyChange(null, null, this, "listaPartecipanti")
		BindUtils.postNotifyChange(null, null, this, "selectedPartecipante")
	}

	private void caricaListaCommissione() {
		listaCommissione = Commissione.createCriteria().list() {
			order("titolo","asc")
		}.toDTO()
		CommissioneDTO tutte = new CommissioneDTO([id:-1,titolo:"TUTTE",descrizione: "Tutte le commissioni"])
		listaCommissione.add(0,tutte)
		selectedCommissione=tutte

		BindUtils.postNotifyChange(null, null, this, "listaCommissione")
	}

	@Command onChangeCommissione() {
		if(selectedCommissione != null){
			List<As4SoggettoCorrenteDTO> listaComponenti = CommissioneComponente.createCriteria().list() {
				projections{
					distinct("componente")
				}
				if (selectedCommissione.id != -1){
					eq("commissione.id", selectedCommissione.id)
				}
			}.toDTO()

			Collections.sort(listaComponenti, new Comparator<As4SoggettoCorrenteDTO>(){

				public int compare(As4SoggettoCorrenteDTO o1, As4SoggettoCorrenteDTO o2)
				{
					return o1.cognome.compareTo(o2.cognome);
				}
			})

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
        mappaParametri["id"] 	= selectedCommissione?.id
        mappaParametri["ni"] 	= selectedPartecipante.id
        mappaParametri["dal"] 	= ft.format(periodoDal)
        mappaParametri["al"] 	= ft.format(periodoAl)

		InputStream testoPdf = gestioneTestiService.stampaUnione(modello, mappaParametri, GestioneTestiService.FORMATO_PDF, true)
		Filedownload.save(testoPdf, GestioneTestiService.getContentType(GestioneTestiService.FORMATO_PDF), "${nomeFile}.pdf")
    }

	@Command onCalcola () {
        stampa ("ODG_GETTONE_PRESENZA", "Gettoni Presenza")
	}

	@Command onExportExcel(){
		if (periodoDal == null ||
				periodoAl  == null ||
				selectedPartecipante == null) {
			Messagebox.show("Per proseguire è necessario selezionare tutti i campi", "Attenzione", Messagebox.OK , Messagebox.EXCLAMATION)
			return;
		}
		SimpleDateFormat ft 	= new SimpleDateFormat ("dd/MM/yyyy")

		List<SedutaPartecipante> sedutaPartecipanteList = SedutaPartecipante.createCriteria().list {
			fetchMode("seduta", FetchMode.JOIN)

			seduta {
				gte("dataInizioSeduta", periodoDal)
				lte("dataInizioSeduta", periodoAl)
				if(selectedCommissione.id>0)
					eq("commissione.id",selectedCommissione.id	)

			}
			if(selectedPartecipante.id>0) {
				fetchMode("commissioneComponente", FetchMode.JOIN)
                or {
						eq("componenteEsterno.id", selectedPartecipante.id)
						commissioneComponente {
							eq("componente.id", selectedPartecipante.id)
						}
                }
            }
		}.toDTO(["componenteEsterno", "commissioneComponente.componente"])

		List<SedutaExcel> exportSedute= new ArrayList<SedutaExcel>();
		for (SedutaPartecipanteDTO sedutaPartecipanteDTO: sedutaPartecipanteList){
			SedutaPartecipante sedutaPartecipante=sedutaPartecipanteDTO.domainObject
			As4SoggettoCorrente soggetto = sedutaPartecipante.componenteEsterno?:sedutaPartecipante.commissioneComponente.componente
			/*if(selectedPartecipante.id>0 && soggetto.id!=selectedPartecipante.id)
				continue*/
			Commissione commissione = Commissione.get(sedutaPartecipante.seduta.commissione.id)

			SedutaExcel exportSeduta = new SedutaExcel()
			exportSeduta.nominativo=soggetto?.nome + " " + soggetto?.cognome
			exportSeduta.commissione=commissione.titolo
			exportSeduta.dataSeduta=sedutaPartecipante.seduta.dataSeduta //ft.format(sedutaPartecipante.seduta.dataSeduta)
			exportSeduta.presente=(sedutaPartecipante.presente?"S":"N")
			if(exportSeduta.presente.equals("S")) {
				exportSeduta.dataEntrata = sedutaPartecipante.dataEntrata
				exportSeduta.dataUscita = sedutaPartecipante.dataUscita
				exportSeduta.oraEntrata = sedutaPartecipante.oraEntrata
				exportSeduta.oraUscita = sedutaPartecipante.oraUscita
			}
			exportSedute.add(exportSeduta)
		}


		def exportOptions =   [    nominativo 		: [label:'Nominativo', 		index: 0, columnType: 'TEXT']
								  , commissione		: [label:'Commissione', 	index: 1, columnType: 'TEXT']
								  , dataSeduta		: [label:'Data Seduta', 	index: 2, columnType: 'DATE', formato:'dd/MM/yyyy']
								  , presente 		: [label:'Presente', 		index: 3, columnType: 'TEXT']
								  , dataEntrata 	: [label:'Data Entrata',index: 4, columnType: 'DATE', formato:'dd/MM/yyyy']
								  , oraEntrata 	: [label:'Ora Entrata',index: 5, columnType: 'TEXT']
								  , dataUscita 	: [label:'Data Uscita', index: 6, columnType: 'DATE', formato:'dd/MM/yyyy']
								  , oraUscita 	: [label:'Ora Uscita',index: 7, columnType: 'TEXT']
		]

		def export = exportSedute.collect { [   	nominativo           :it.nominativo
													, commissione                :it.commissione
													, dataSeduta                  :it.dataSeduta
													, presente             :it.presente
													, dataEntrata     :it.dataEntrata
													, oraEntrata 	:it.oraEntrata
													, dataUscita               :it.dataUscita
												    , oraUscita 	:it.oraUscita
		]}
		exportService.downloadExcel(exportOptions, export)

	}

	@Command onStampaStatistiche () {
        stampa ("ODG_STATISTICHE_PRESENZA", "Statistiche di Presenza")
	}

	@Command onStampaAttestazionePresenza () {
		stampa ("ODG_ATTESTAZIONE_PRESENZA", "Attestazione di Presenza")
	}

	@Command onAnnulla() {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}
}
