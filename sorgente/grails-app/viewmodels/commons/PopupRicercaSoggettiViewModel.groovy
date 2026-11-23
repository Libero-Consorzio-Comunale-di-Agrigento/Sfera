package commons

import grails.orm.PagedResultList
import it.finmatica.ad4.dizionari.Ad4Provincia
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.documenti.viste.RicercaAnagrafica
import it.finmatica.atti.dto.dizionari.EmailDTOService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubb
import org.hibernate.FetchMode
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class PopupRicercaSoggettiViewModel {

	EmailDTOService emailDTOService

	// componenti
	Window self

	// dati
	def listaSoggetti
	def listaSoggettiRicercaAnagrafica
	def soggetto
	String nome 			= ""
	String cognome 		 	= ""
	String codiceFiscale 	= ""
	Date dataNascita 	 	= null
	String tipoRicerca      = "so4"
    boolean ricercaSuSfera
	boolean pndNotifica = false

	// stato
	int activePage = 0
	int pageSize = 30
	int totalSize

	@Init
    void init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("ricercaSuSfera") Boolean ricercaSuSfera, @ExecutionArgParam("pndNotifica") Boolean pndNotifica,  @ExecutionArgParam("ricercaAs4") Boolean ricercaAs4) {
        if (ricercaSuSfera == null) {
            ricercaSuSfera = false
        }

		if (pndNotifica == null) {
			pndNotifica = false
		}

		if (ricercaAs4) {
			tipoRicerca = "as4"
		}

        this.ricercaSuSfera = ricercaSuSfera.booleanValue()
		this.pndNotifica = pndNotifica.booleanValue()
		this.self = w
	}

	@NotifyChange(["listaSoggetti", "totalSize", "activePage"])
	@Command
    void onCerca (@BindingParam("paginate") boolean paginate) {
		if (paginate == false) {
			activePage = 0
		}

        switch (tipoRicerca) {
            case "so4":
                ricercaSuSo4()
                break
            case "as4":
                ricercaSuAs4()
                break
            case "sfera":
                ricercaSuEmailSfera()
                break
        }
	}

	@NotifyChange(["listaSoggettiRicercaAnagrafica", "totalSize", "activePage"])
	@Command
	void onCercaPnd (@BindingParam("paginate") boolean paginate) {
		if (paginate == false) {
			activePage = 0
		}

		ricercaSuAnagrafeSoggetti()
	}

    @Command
    void onCreaNuovo () {
        Window w = Executions.createComponents ("/dizionari/atti/emailDettaglio.zul", self, [:])
        w.onClose {
            ricercaSuEmailSfera()
        }
        w.doModal()
    }

	private void ricercaSuEmailSfera () {
		PagedResultList elencoDestinatariEsterni = emailDTOService.cerca(cognome, pageSize, activePage)
		totalSize = elencoDestinatariEsterni.totalCount
		listaSoggetti = elencoDestinatariEsterni.toDTO()
	}

	private void ricercaSuAs4 () {
        PagedResultList resultList = As4SoggettoCorrente.createCriteria().list(max:pageSize, offset: pageSize * (activePage)) {

			if (cognome.trim().length() > 0) {
				ilike ("cognome", "%"+cognome+"%")
			}

			if (nome.trim().length() > 0) {
				ilike ("nome", "%"+nome+"%")
			}

			if (codiceFiscale.trim().length() > 0) {
				ilike ("codiceFiscale", "%"+codiceFiscale+"%")
			}

			if (dataNascita != null) {
				eq ("dataNascita", dataNascita)
			}

			if (pndNotifica) {
				isNotNull ("codiceFiscale")
			}
			order ("cognome", 	"asc")
			order ("nome", 		"asc")

			fetchMode("utenteAd4", FetchMode.JOIN)
		}

		totalSize = resultList.totalCount
        listaSoggetti = resultList.toDTO(['provinciaResidenza', 'comuneResidenza'])
	}

	private void ricercaSuSo4 () {
        def resultList = So4ComponentePubb.createCriteria().listDistinct {
			projections {
				property ("soggetto")
			}
			soggetto {
				if (cognome.trim().length() > 0) {
					ilike ("cognome", "%"+cognome+"%")
				}

				if (nome.trim().length() > 0) {
					ilike ("nome", "%"+nome+"%")
				}

				if (codiceFiscale.trim().length() > 0) {
					ilike ("codiceFiscale", "%"+codiceFiscale+"%")
				}

				if (dataNascita != null) {
					eq ("dataNascita", dataNascita)
				}

				maxResults (pageSize)
				firstResult(pageSize * activePage)

				order ("cognome", 	"asc")
				order ("nome", 		"asc")
			}

			fetchMode("soggetto",  FetchMode.JOIN)
			fetchMode("utenteAd4", FetchMode.JOIN)
		}

		totalSize = So4ComponentePubb.createCriteria().get {
			projections {
				countDistinct ("soggetto.id")
			}
			soggetto {
				if (cognome.trim().length() > 0) {
					ilike ("cognome", "%"+cognome+"%")
				}

				if (nome.trim().length() > 0) {
					ilike ("nome", "%"+nome+"%")
				}

				if (codiceFiscale.trim().length() > 0) {
					ilike ("codiceFiscale", "%"+codiceFiscale+"%")
				}

				if (dataNascita != null) {
					eq ("dataNascita", dataNascita)
				}
			}
		}

        listaSoggetti = resultList.toDTO()
	}

	private void ricercaSuAnagrafeSoggetti () {
		PagedResultList resultList = RicercaAnagrafica.createCriteria().list(max:pageSize, offset: pageSize * (activePage)) {

			if (cognome.trim().length() > 0) {
				ilike ("cognome", "%"+cognome+"%")
			}

			if (nome.trim().length() > 0) {
				ilike ("nome", "%"+nome+"%")
			}

			if (codiceFiscale.trim().length() > 0) {
				ilike ("codiceFiscale", "%"+codiceFiscale+"%")
			}

			if (dataNascita != null) {
				eq ("dataNascita", dataNascita)
			}

			isNotNull ("codiceFiscale")

			order ("cognome", 	"asc")
			order ("nome", 		"asc")
		}

		totalSize = resultList.totalCount
		listaSoggettiRicercaAnagrafica = resultList
	}


	@Command onAnnulla () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@Command onSeleziona() {
		if (pndNotifica) {
			if ((soggetto.nome == null) || (soggetto.cognome == null)) {
				Messagebox.show("Impossibile notificare tramite PND ad un soggetto senza nome e cognome", "Attenzione!", Messagebox.OK , Messagebox.EXCLAMATION)
				return
			}

			if (soggetto.codiceFiscale == null) {
				Messagebox.show("Impossibile notificare tramite PND ad un soggetto senza codice fiscale", "Attenzione!", Messagebox.OK , Messagebox.EXCLAMATION)
				return
			}

			if (soggetto.provincia == null) {
				Messagebox.show("Impossibile notificare tramite PND ad un soggetto se non e' indicata la provincia ", "Attenzione!", Messagebox.OK , Messagebox.EXCLAMATION)
				return
			}
//			else {
//				Ad4Provincia ad4Provincia = Ad4Provincia.get(soggetto.provinciaResidenza.id)
//				if (ad4Provincia == null) {
//					Messagebox.show("Impossibile notificare tramite PND ad un soggetto se non e' indicata una provincia di residenza valida ", "Attenzione!", Messagebox.OK , Messagebox.EXCLAMATION)
//					return
//				}
//				else {
//					if (ad4Provincia.sigla == null) {
//						Messagebox.show("Impossibile notificare tramite PND. La provincia di residenza non ha una sigla valida ", "Attenzione!", Messagebox.OK , Messagebox.EXCLAMATION)
//						return
//					}
//				}
//			}

			if (soggetto.comune == null) {
				Messagebox.show("Impossibile notificare tramite PND ad un soggetto se non e' indicato il comune ", "Attenzione!", Messagebox.OK , Messagebox.EXCLAMATION)
				return
			}

			if (soggetto.cap == null) {
				Messagebox.show("Impossibile notificare tramite PND ad un soggetto se non e' indicato il cap ", "Attenzione!", Messagebox.OK , Messagebox.EXCLAMATION)
				return
			}
		}
		Events.postEvent(Events.ON_CLOSE, self, soggetto)
	}

	@Command onAnagraficaPagina() {
		Clients.evalJavaScript(" window.open('" + Impostazioni.URL_ANAGRAFICA.valore + "' , '_blank'); ")
	}
}
