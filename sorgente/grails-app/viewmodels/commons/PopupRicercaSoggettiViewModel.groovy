package commons

import grails.orm.PagedResultList
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.dto.dizionari.EmailDTOService
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubb
import org.hibernate.FetchMode
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Window

class PopupRicercaSoggettiViewModel {

	EmailDTOService emailDTOService

	// componenti
	Window self

	// dati
	def listaSoggetti
	def soggetto
	String nome 			= ""
	String cognome 		 	= ""
	String codiceFiscale 	= ""
	Date dataNascita 	 	= null
	String tipoRicerca      = "so4"
    boolean ricercaSuSfera

	// stato
	int activePage = 0
	int pageSize = 30
	int totalSize

	@Init
    void init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("ricercaSuSfera") Boolean ricercaSuSfera) {
        if (ricercaSuSfera == null) {
            ricercaSuSfera = false
        }
        this.ricercaSuSfera = ricercaSuSfera.booleanValue()
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

			order ("cognome", 	"asc")
			order ("nome", 		"asc")

			fetchMode("utenteAd4", FetchMode.JOIN)
		}

		totalSize = resultList.totalCount
        listaSoggetti = resultList.toDTO()
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

	@Command onAnnulla () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@Command onSeleziona() {
		Events.postEvent(Events.ON_CLOSE, self, soggetto)
	}
}
