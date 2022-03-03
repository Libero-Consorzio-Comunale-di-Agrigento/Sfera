package odg.seduta

import it.finmatica.atti.documenti.DeterminaService
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.StatoOdg
import it.finmatica.atti.dto.documenti.DeterminaDTO
import it.finmatica.atti.dto.documenti.PropostaDeliberaDTO
import it.finmatica.atti.dto.documenti.PropostaDeliberaDTOService
import it.finmatica.atti.dto.odg.OggettoSedutaDTO
import it.finmatica.atti.dto.odg.SedutaDTO
import it.finmatica.atti.dto.odg.SedutaDTOService
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.apache.commons.collections.comparators.NullComparator
import org.hibernate.FetchMode
import org.hibernate.criterion.CriteriaSpecification
import org.zkoss.bind.BindContext
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.DropEvent
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Label
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class OdgProposteSedutaViewModel {

	// service
	SedutaDTOService			sedutaDTOService
	PropostaDeliberaDTOService 	propostaDeliberaDTOService
	DeterminaService 			determinaService

	// componenti
	Window self

	// dati
	List listaProposte 		= []
	List<OggettoSedutaDTO> 		listaProposteOdg	= []
	def							selectedProposta
	OggettoSedutaDTO 			selectedPropostaOdg
	SedutaDTO					seduta

	// stato
	String ordina
	String valoreRicerca

	int numProposteInserite = 0

	private long idOggettoSedutaAperto = -1;

	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("seduta") SedutaDTO seduta)  {
		this.self 	= w
		this.seduta = seduta
		caricaListeProposteOdg()
		caricaListeProposte()
	}

	/**
	 * Questo GlobalCommand scatta quando viene cliccato il tab delle proposte.
	 */
	@GlobalCommand
	void onRefreshProposte(Event e) {
		caricaListeProposteOdg()
	}

	@NotifyChange(["listaProposteOdg"])
	private void caricaListeProposteOdg () {
		// Siccome la join su AS4_ANAGRAFE_SOGGETTI è molto lenta con anagrafiche corpose (ad es. prov. ancona), non vado in join direttamente
		// nella query ma lascio che sia il DTO a recuperare singolarmente i soggetti
		listaProposteOdg = OggettoSeduta.createCriteria().list {
			eq ("seduta.id", seduta.id)
			order ("sequenzaConvocazione", "asc")

			fetchMode("propostaDelibera", 			FetchMode.JOIN)
			fetchMode("propostaDelibera.tipologia", FetchMode.JOIN)
		}

		listaProposteOdg = listaProposteOdg.toDTO(["delega.assessore"])

		BindUtils.postNotifyChange(null, null, this, "listaProposteOdg")
		numProposteInserite = listaProposteOdg.size()
        BindUtils.postNotifyChange(null, null, this, "numProposteInserite")
	}

	@NotifyChange(["listaProposte"])
	private void caricaListeProposte() {

        def listaProposteOdgSenzaEsito = listaProposteOdg.findAll {it.esito?.esitoStandard?.codice == it.finmatica.atti.odg.dizionari.EsitoStandard.RINVIO_UFFICIO && it.confermaEsito == false}
		// Siccome la join su AS4_ANAGRAFE_SOGGETTI è molto lenta con anagrafiche corpose (ad es. prov. ancona), non vado in join direttamente
		// nella query ma lascio che sia il DTO a recuperare singolarmente i soggetti
		listaProposte = PropostaDelibera.createCriteria().list {
			createAlias('iter', 'it', CriteriaSpecification.INNER_JOIN)
			eq("commissione.id", seduta.commissione.id)
			isNull("it.dataFine")
			isNotNull("numeroProposta")
			'in' ("statoOdg", [StatoOdg.COMPLETO, StatoOdg.COMPLETO_IN_ISTRUTTORIA])

			if (listaProposteOdgSenzaEsito*.propostaDelibera.id.size()>0) {
                not {
                    'in' ("id", listaProposteOdgSenzaEsito*.propostaDelibera.id)
                }
			}
			if (valoreRicerca)  {
				or {
					ilike("oggetto","%"+valoreRicerca+"%")
					if (valoreRicerca?.isNumber()) {
						eq("numeroProposta",valoreRicerca.toInteger())
						eq("annoProposta",  valoreRicerca.toInteger())
					}
				}
			}
			fetchMode("tipologia", FetchMode.JOIN)
		}.toDTO(["delega.assessore"])

		if (ordina) {
			def nullHigh = new NullComparator(true)
			switch (ordina) {
				case "anno":
					listaProposte.sort{ x,y->
						if (x.annoProposta == y.annoProposta) {
							x.numeroProposta <=> y.numeroProposta
						} else {
							x.annoProposta <=> y.annoProposta
						}
					}
					break;
				case "assessorato":
					listaProposte.sort{a, b-> nullHigh.compare(((a.getClass().getSimpleName()=="PropostaDeliberaDTO") ? a.delega?.descrizioneAssessorato: null), ((b.getClass().getSimpleName()=="PropostaDeliberaDTO") ? b.delega?.descrizioneAssessorato : null))}
					break;
				case "relatore":
					listaProposte.sort{a, b-> nullHigh.compare(((a.getClass().getSimpleName()=="PropostaDeliberaDTO") ? a.delega?.assessore?.denominazione : null), ((b.getClass().getSimpleName()=="PropostaDeliberaDTO") ? b.delega?.assessore?.denominazione : null))}
					break;
				case "tipologia":
					listaProposte.sort{a, b-> nullHigh.compare(a.tipologia.titolo, b.tipologia.titolo)}
					break;
				case "unitaProponente":
					break;
				default:
					break;
			}
		}
		selectedProposta = null
		BindUtils.postNotifyChange(null, null, this, "selectedProposta")
		BindUtils.postNotifyChange(null, null, this, "listaProposte")
	}

	@Command
	@NotifyChange(["listaProposte", "listaProposteOdg", "selectedProposta", "selectedPropostaOdg", "numProposteInserite"])
	public void onRimuoviProposta (@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		OggettoSedutaDTO selezionato = selectedPropostaOdg;
		if (ctx.triggerEvent instanceof DropEvent) {
			// se non sto trascinando un OggettoSedutaDTO, non fare niente:
			if (!(ctx.triggerEvent.dragged.value instanceof OggettoSedutaDTO)) {
				return
			}
			selezionato = ctx.triggerEvent.dragged.value
		}

		if (selezionato?.confermaEsito) {
			Messagebox.show("Impossibile rimuovere una proposta con un esito confermato!!")
		} else {
			sedutaDTOService.rimuoviProposta(selezionato);
			listaProposteOdg.remove(selezionato)
			numProposteInserite = listaProposteOdg.size()
			caricaListeProposte();
			selectedPropostaOdg = null;
		}
		BindUtils.postGlobalCommand(null, null, "onRefreshVerbalizzazione", null);
	}

	@Command
	@NotifyChange(["listaProposte", "listaProposteOdg", "selectedProposta", "selectedPropostaOdg", "numProposteInserite"])
	public void onInserisciProposta (@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx, @BindingParam("base") Long base) {
		if (sedutaDTOService.esisteSedutaSuccessivaConEsitoConfermato(seduta)) {
			Clients.showNotification ("Esiste una seduta successiva con proposte con esito confermato!", Clients.NOTIFICATION_TYPE_ERROR, null, "before_center", 5000, true);
			return
	    }
		def proposta = selectedProposta;
		if (ctx.triggerEvent instanceof DropEvent) {
			proposta =  ctx.triggerEvent.dragged.value;
		}

		// se sto droppando un oggettoSeduta, allora devo solo spostarlo, non inserirlo:
		if (proposta instanceof OggettoSedutaDTO) {
			if (base == null) {
				base = listaProposteOdg.size() - 1; // faccio -1 perché dopo faccio +1, ok, sembra non avere senso, ma ce l'ha.
			}
			sedutaDTOService.spostaOggettoSeduta (proposta.domainObject, (int)(base+1)) // faccio base+1 perché l'indice calcolato da zk è 0-based ma a me serve 1-based.
		} else {
			if (base == null) {
				base = listaProposteOdg.size();
			}
			sedutaDTOService.inserisciProposta(seduta, proposta, (int)(base+1)).toDTO();
			listaProposte.remove(proposta)
		}

		caricaListeProposteOdg();
		selectedProposta = null;
		BindUtils.postGlobalCommand(null, null, "onRefreshVerbalizzazione", null);
	}

	@Command
	@NotifyChange(["listaProposte", "listaProposteOdg", "selectedProposta", "selectedPropostaOdg", "numProposteInserite"])
	public void onInserisciTutte () {
		if (sedutaDTOService.esisteSedutaSuccessivaConEsitoConfermato(seduta)) {
			Clients.showNotification ("Esiste una seduta successiva con proposte con esito confermato!", Clients.NOTIFICATION_TYPE_ERROR, null, "before_center", 5000, true);
			return
	    }
		sedutaDTOService.inserisciTutteProposte (seduta, listaProposte)
		caricaListeProposteOdg()
		caricaListeProposte()
		BindUtils.postGlobalCommand(null, null, "onRefreshVerbalizzazione", null);
	}

	@Command
	@NotifyChange(["listaProposte", "listaProposteOdg", "selectedProposta", "selectedPropostaOdg", "numProposteInserite"])
	public void onRimuoviTutte () {
		sedutaDTOService.rimuoviTuttiOggettiSeduta (seduta)
		caricaListeProposteOdg()
		caricaListeProposte()
		BindUtils.postGlobalCommand(null, null, "onRefreshVerbalizzazione", null);
	}

	@NotifyChange(["listaProposte"])
	@Command onCercaProposte () {
		caricaListeProposte()
	}

	@NotifyChange(["listaProposteOdg", "selectedPropostaOdg"])
	@Command onSuSequenza () {
		if (selectedPropostaOdg.sequenzaConvocazione == 1)
			return;

		sedutaDTOService.spostaOggettoSedutaSu(selectedPropostaOdg);
		caricaListeProposteOdg()
		selectedPropostaOdg = listaProposteOdg.find { it.id == selectedPropostaOdg.id }
		BindUtils.postGlobalCommand(null, null, "onRefreshVerbalizzazione", null);
	}

	@NotifyChange(["listaProposteOdg", "selectedPropostaOdg"])
	@Command onGiuSequenza () {
		if (selectedPropostaOdg.sequenzaConvocazione == listaProposteOdg.size())
			return;

		sedutaDTOService.spostaOggettoSedutaGiu(selectedPropostaOdg);
		caricaListeProposteOdg()
		selectedPropostaOdg = listaProposteOdg.find { it.id == selectedPropostaOdg.id }
		BindUtils.postGlobalCommand(null, null, "onRefreshVerbalizzazione", null);
	}

	@Command onLinkOggettoSeduta (@BindingParam("oggetto") OggettoSedutaDTO oggetto) {
		// se ho già un oggetto seduta aperto, non faccio niente:
		if (idOggettoSedutaAperto > 0) {
			return;
		}

		idOggettoSedutaAperto = oggetto.id;

		Window w = Executions.createComponents("/odg/oggettoSeduta.zul", self, [id: oggetto.id, wp: 'proposteSeduta'])
		w.onClose {
			// tolgo il flag dell'oggetto seduta aperto:
			idOggettoSedutaAperto = -1;
			caricaListeProposteOdg()
			BindUtils.postNotifyChange(null,null, this, "listaProposteOdg")
		}
		w.doModal()
	}

	@Command onLink (@BindingParam("oggetto") def oggetto) {
		Window w
		if (oggetto instanceof PropostaDeliberaDTO) {
			w = Executions.createComponents("/atti/documenti/propostaDelibera.zul", self, [id: oggetto.id])
		} else if (oggetto instanceof DeterminaDTO) {
			w = Executions.createComponents("/atti/documenti/determina.zul", self, [id: oggetto.id])
		}
		w.onClose {
			caricaListeProposte()
			BindUtils.postNotifyChange(null,null, this, "listaProposteOdg")
		}
		w.doModal()
	}

	@Command getUnita(@ContextParam(ContextType.COMPONENT) Label lc, @BindingParam("oggetto") def oggetto) {
		So4UnitaPubb rs = oggetto.domainObject.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4
		String unita = "Unità Proponente: "
		lc.value = ((rs) ? (unita + rs.descrizione ) : unita)
	}
}
