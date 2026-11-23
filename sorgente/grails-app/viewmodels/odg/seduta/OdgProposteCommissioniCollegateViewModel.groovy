package odg.seduta

import it.finmatica.atti.documenti.PropostaDeliberaSoggetto
import it.finmatica.atti.dto.documenti.DeterminaDTO
import it.finmatica.atti.dto.documenti.PropostaDeliberaDTO
import it.finmatica.atti.dto.odg.OggettoSedutaCollegatoDTO
import it.finmatica.atti.dto.odg.OggettoSedutaDTO
import it.finmatica.atti.dto.odg.SedutaDTO
import it.finmatica.atti.dto.odg.SedutaDTOService
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.odg.CommissioneCollegata
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.atti.odg.OggettoSedutaCollegato
import it.finmatica.atti.odg.Seduta
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.hibernate.FetchMode
import org.zkoss.bind.BindContext
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.DropEvent
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Label
import org.zkoss.zul.Window

class OdgProposteCommissioniCollegateViewModel {

	// service
	SedutaDTOService			sedutaDTOService

	// componenti
	Window self

	// dati
	List 							listaProposte 		= []
	List<OggettoSedutaDTO> 			listaProposteOdg	= []
	OggettoSedutaDTO				selectedProposta
	OggettoSedutaCollegatoDTO		selectedPropostaOdg
	SedutaDTO						seduta
	List<CommissioneCollegata> 		listaCommissione

	Date   dataSeduta
	CommissioneCollegata commissioneSelezionata

	int numProposteInserite = 0

	private long idOggettoSedutaAperto = -1;

	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("seduta") SedutaDTO seduta)  {
		this.self 	= w
		this.seduta = seduta

		listaCommissione = CommissioneCollegata.createCriteria().list() {
			eq("valido",true)
			eq("commissione.id", seduta.commissione.id)
		}

		caricaListeProposteOdg()
		caricaListeProposte()
	}

	/**
	 * Questo GlobalCommand scatta quando viene cliccato il tab delle proposte.
	 */
	@GlobalCommand
	void onRefreshProposteCommissioniCollegate(Event e) {
		caricaListeProposteOdg()
	}

	@NotifyChange(["listaProposteOdg"])
	private void caricaListeProposteOdg () {
		// Siccome la join su AS4_ANAGRAFE_SOGGETTI è molto lenta con anagrafiche corpose (ad es. prov. ancona), non vado in join direttamente
		// nella query ma lascio che sia il DTO a recuperare singolarmente i soggetti
		listaProposteOdg = OggettoSedutaCollegato.createCriteria().list {
			eq ("seduta.id", seduta.id)
			order ("sequenzaConvocazione", "asc")

			fetchMode("oggettoSeduta", 			FetchMode.JOIN)
			fetchMode("propostaDelibera", 			FetchMode.JOIN)
			fetchMode("propostaDelibera.tipologia", FetchMode.JOIN)
		}

		listaProposteOdg = listaProposteOdg.toDTO(["oggettoSeduta.seduta", "oggettoSeduta.propostaDelibera.tipologia","oggettoSeduta.delega.assessore"])
		caricaUnitaProponenti(listaProposteOdg*.oggettoSeduta.findAll { it != null })
		BindUtils.postNotifyChange(null, null, this, "listaProposteOdg")
		numProposteInserite = listaProposteOdg.size()
        BindUtils.postNotifyChange(null, null, this, "numProposteInserite")
	}

	@NotifyChange(["listaProposte"])
	private void caricaListeProposte() {
		listaProposte 		= []

		List<Seduta> seduteSel = []
		List<String> listaId = []

		//leggo le seduta per commissione, data (potrebbero essere più di una
		seduteSel = Seduta.findAllByCommissioneAndDataSedutaAndValido(commissioneSelezionata?.commissioneCollegata, dataSeduta, true)
		if (seduteSel) {
			for (Seduta s in seduteSel) {
				listaId << s.id
			}

			listaProposte = OggettoSeduta.createCriteria().list {
				'in'("seduta.id", listaId)
				//escludo gli oggetti_seduta già inseriti precedentemente
				if (listaProposteOdg.size() > 0) {
					not {
						'in'("id", listaProposteOdg.oggettoSeduta.id)
					}
				}
				order("sequenzaConvocazione", "asc")

				fetchMode("propostaDelibera", FetchMode.JOIN)
				fetchMode("propostaDelibera.tipologia", FetchMode.JOIN)
			}

			listaProposte = listaProposte.toDTO(["delega.assessore"])
			caricaUnitaProponenti(listaProposte)
			selectedProposta = null
			BindUtils.postNotifyChange(null, null, this, "selectedProposta")
			BindUtils.postNotifyChange(null, null, this, "listaProposte")
		}
	}

	@Command
	@NotifyChange(["listaProposte", "listaProposteOdg", "selectedProposta", "selectedPropostaOdg", "numProposteInserite"])
	public void onRimuoviProposta (@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		OggettoSedutaCollegatoDTO selezionato = selectedPropostaOdg;
		if (ctx.triggerEvent instanceof DropEvent) {
			// se non sto trascinando un OggettoSedutaDTO, non fare niente:
			if (!(ctx.triggerEvent.dragged.value instanceof OggettoSedutaDTO)) {
				return
			}
			selezionato = ctx.triggerEvent.dragged.value
		}

		sedutaDTOService.rimuoviOggettoSedutaCollegato(selezionato);
		listaProposteOdg.remove(selezionato)
		numProposteInserite = listaProposteOdg.size()
		caricaListeProposte();
		selectedPropostaOdg = null;
	}

	@Command
	@NotifyChange(["listaProposte", "listaProposteOdg", "selectedProposta", "selectedPropostaOdg", "numProposteInserite"])
	public void onInserisciProposta (@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx, @BindingParam("base") Long base) {
		if (sedutaDTOService.esisteSedutaSuccessivaConEsitoConfermato(seduta)) {
			Clients.showNotification ("Esiste una seduta successiva con proposte con esito confermato!", Clients.NOTIFICATION_TYPE_ERROR, null, "before_center", 5000, true);
			return
	    }

		OggettoSedutaDTO proposta = selectedProposta;  //è l'oggettoSeduta che sto selezionando
		if (ctx.triggerEvent instanceof DropEvent) {
			proposta =  ctx.triggerEvent.dragged.value;
		}

		if (base == null) {
			base = listaProposteOdg.size();
		}
			sedutaDTOService.inserisciOggettoSedutaCollegato(seduta, proposta, (int)(base+1)).toDTO();
			listaProposte.remove(proposta)

		caricaListeProposteOdg();
		selectedProposta = null;
	}

	@Command
	@NotifyChange(["listaProposte", "listaProposteOdg", "selectedProposta", "selectedPropostaOdg", "numProposteInserite"])
	public void onInserisciTutte () {
		if (sedutaDTOService.esisteSedutaSuccessivaConEsitoConfermato(seduta)) {
			Clients.showNotification ("Esiste una seduta successiva con proposte con esito confermato!", Clients.NOTIFICATION_TYPE_ERROR, null, "before_center", 5000, true);
			return
	    }
		sedutaDTOService.inserisciTuttiOggettiSedutaCollegati (seduta, listaProposte)
		caricaListeProposteOdg()
		caricaListeProposte()
	}

	@Command
	@NotifyChange(["listaProposte", "listaProposteOdg", "selectedProposta", "selectedPropostaOdg", "numProposteInserite"])
	public void onRimuoviTutte () {
		sedutaDTOService.rimuoviTuttiOggettiSedutaCollegati (seduta)
		caricaListeProposteOdg()
		caricaListeProposte()
	}

	@NotifyChange(["listaProposte"])
	@Command onCercaProposte () {
		caricaListeProposte()
	}

	@NotifyChange(["listaProposteOdg", "selectedPropostaOdg"])
	@Command onSuSequenza () {
		if (selectedPropostaOdg.sequenzaConvocazione == 1)
			return;

		sedutaDTOService.spostaOggettoSedutaCollegatoSu(selectedPropostaOdg);
		caricaListeProposteOdg()
		selectedPropostaOdg = listaProposteOdg.find { it.id == selectedPropostaOdg.id }
	}

	@NotifyChange(["listaProposteOdg", "selectedPropostaOdg"])
	@Command onGiuSequenza () {
		if (selectedPropostaOdg.sequenzaConvocazione == listaProposteOdg.size())
			return;

		sedutaDTOService.spostaOggettoSedutaCollegatoGiu(selectedPropostaOdg);
		caricaListeProposteOdg()
		selectedPropostaOdg = listaProposteOdg.find { it.id == selectedPropostaOdg.id }
	}

	@Command onLinkOggettoSeduta (@BindingParam("oggetto") OggettoSedutaCollegatoDTO oggetto) {
		// se ho già un oggetto seduta aperto, non faccio niente:
		if (idOggettoSedutaAperto > 0) {
			return;
		}

		idOggettoSedutaAperto = oggetto.id;

		Window w = Executions.createComponents("/atti/documenti/propostaDelibera.zul", self, [id: oggetto.oggettoSeduta.propostaDelibera.id])
				w.onClose {
			// tolgo il flag dell'oggetto seduta aperto:
			idOggettoSedutaAperto = -1;
			caricaListeProposteOdg()
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

	private void caricaUnitaProponenti(List lista) {
		def ids = lista*.propostaDelibera*.id.findAll { it != null }
		if (!ids) return
		def rows = PropostaDeliberaSoggetto.executeQuery(
			"select s.propostaDelibera.id, s.unitaSo4.descrizione from PropostaDeliberaSoggetto s " +
			"where s.propostaDelibera.id in :ids and s.tipoSoggetto.codice = :codice and s.attivo = true",
			[ids: ids, codice: TipoSoggetto.UO_PROPONENTE])
		Map unitaMap = rows.collectEntries { [(it[0]): (it[1] ?: "")] }
		lista.each { o ->
			if (o.propostaDelibera) o.unitaProponente = "Unità Proponente: " + (unitaMap[o.propostaDelibera.id] ?: "")
		}
	}
}
