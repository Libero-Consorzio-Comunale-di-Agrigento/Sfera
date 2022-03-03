package odg.seduta

import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.StatoOdg
import it.finmatica.atti.dto.documenti.DeliberaDTO
import it.finmatica.atti.dto.odg.ConvocatiSedutaDTOService
import it.finmatica.atti.dto.odg.OggettoSedutaDTO
import it.finmatica.atti.dto.odg.OggettoSedutaDTOService
import it.finmatica.atti.dto.odg.SedutaDTO
import it.finmatica.atti.dto.odg.SedutaDTOService
import it.finmatica.atti.dto.odg.dizionari.EsitoDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.atti.odg.dizionari.EsitoStandard
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.hibernate.FetchMode
import org.zkoss.bind.BindContext
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Label
import org.zkoss.zul.Listbox
import org.zkoss.zul.Listcell
import org.zkoss.zul.Window

class OdgVerbalizzazioneSedutaViewModel {

   	// service
	OggettoSedutaDTOService oggettoSedutaDTOService
	SedutaDTOService		sedutaDTOService
	ConvocatiSedutaDTOService convocatiSedutaDTOService

	// componenti
	Window self

	// dati
	SedutaDTO				seduta
	OggettoSedutaDTO		selectedProposta
	List<OggettoSedutaDTO>	listaProposte

	// stato
	boolean up 				= false
	boolean down 			= false
	boolean isDrop 			= false
	boolean conferma 		= false
	boolean assegna			= false
	boolean numeraDelibera	= false

	String oggettoMail
	String testoMail
	List<As4SoggettoCorrenteDTO>	listaSoggetto

	int proposteEsitoConfermato = 0
	int proposteEsitoDaConfermare = 0
	
	boolean odgNumeraDelibere = true;

	// contiene l'id dell'oggetto seduta correntemente aperto. Serve per evitare il doppio-click per aprire una proposta.
	// #bug: http://svi-redmine/issues/8027
	private long idOggettoSedutaAperto 	= -1;
	private long idDeliberaAperta 		= -1;

    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("seduta") SedutaDTO seduta)  {
        this.self = w
		this.seduta = seduta
		caricaListaProposte()
		odgNumeraDelibere = Impostazioni.ODG_NUMERA_DELIBERE.abilitato
	}

	@GlobalCommand
    @Command
	void onRefreshVerbalizzazione (Event e) {
		caricaListaProposte();
	}

	@NotifyChange("listaProposte")
	private void caricaListaProposte() {
		listaProposte = OggettoSeduta.createCriteria().list {
			eq ("seduta.id", seduta.id)
			order ("sequenzaDiscussione", "asc")

			fetchMode("esito", 						FetchMode.JOIN)
			fetchMode("propostaDelibera", 			FetchMode.JOIN)
			fetchMode("propostaDelibera.tipologia", FetchMode.JOIN)
		}.toDTO(["delega.assessore", "propostaDelibera.iter.stepCorrente"])  // carico l'assessore così perché con anagrafiche grandi è più veloce che mettere la fetch nella query.

		// imposto manualmente la sedutaDto solo perché l'ho già, così evito di fare una ulteriore select.
		listaProposte*.setSeduta (seduta)
		selectedProposta = null
		calcolaNumeroProposte()

		BindUtils.postNotifyChange(null,null, this, "selectedProposta")
		BindUtils.postNotifyChange(null,null, this, "listaProposte")
	}

	@Command checkDelibera(@ContextParam(ContextType.COMPONENT) Listcell lc, @BindingParam("oggetto")  OggettoSedutaDTO oggetto) {
		DeliberaDTO rs
		if (oggetto.propostaDelibera) {
			rs = Delibera.findWhere("propostaDelibera.id": oggetto.propostaDelibera.id, "oggettoSeduta.id":oggetto.id)?.toDTO(["registroDelibera"])
		}
		lc.setLabel((rs)? ((rs.numeroDelibera > 0 ? (rs.numeroDelibera +"/" +rs.annoDelibera+ " " +rs.registroDelibera?.descrizione?:""):"") + " - "+rs.oggetto) : "")
	}

	@Command onLinkDelibera (@BindingParam("oggetto") OggettoSedutaDTO oggetto) {
		if (idDeliberaAperta > 0) {
			return
		}

		Delibera rs = Delibera.findWhere("propostaDelibera.id": oggetto.propostaDelibera.id, "oggettoSeduta.id":oggetto.id)

		if (rs) {
			idDeliberaAperta = rs.id;
			Window w = Executions.createComponents("/atti/documenti/delibera.zul", self, [id : rs.id])
			w.onClose {
				idDeliberaAperta = -1;
				caricaListaProposte()
				BindUtils.postNotifyChange(null,null, this, "listaProposte")
			}
			w.doModal()
		}
	}

	@Command onConfermaEsito(@BindingParam("lista") Listbox lista) {
		List<OggettoSedutaDTO> selezionati = lista.getSelectedItems().value

		sedutaDTOService.confermaEsiti (lista.getSelectedItems().value);

		caricaListaProposte()
	}

	@Command onAssegnaEsito(@BindingParam("lista") Listbox lista) {
		def messaggi = convocatiSedutaDTOService.checkRuoliObbligatori(seduta);

		if (messaggi.size() > 0) {
			String messaggio = "ATTENZIONE: "+messaggi.join(", ");
			throw new AttiRuntimeException(messaggio)
		}

		for (OggettoSedutaDTO dto: lista.getSelectedItems().value){
			if (dto.propostaDelibera.stato == StatoDocumento.ANNULLATO){
				throw new AttiRuntimeException("Impossibile assegnare un esito alla proposta ${dto.propostaDelibera.numeroProposta}/${dto.propostaDelibera.annoProposta}, la proposta è stata ANNULLATA.")
			}
		}

		Window w = Executions.createComponents("/odg/popupAssegnaEsito.zul", self, [idCommissione:seduta.commissione.id])
		w.onClose { event ->
			if (event.data) {
				EsitoDTO selezionato = event.data.selectedEsito
				if (selezionato) {
					sedutaDTOService.assegnaEsiti (lista.selectedItems*.value, selezionato, event.data.note)
					caricaListaProposte()
					calcolaNumeroProposte()
					BindUtils.postNotifyChange(null,null, this, "proposteEsitoConfermato")
					BindUtils.postNotifyChange(null,null, this, "proposteEsitoDaConfermare")

					for (OggettoSedutaDTO oggettoDto : lista.selectedItems.value){
						sedutaDTOService.creaPartecipanti(oggettoDto.domainObject)
					}
				}
			}
		}
		w.doModal()
	}

	@Command onNumeraDelibera(@BindingParam("lista") Listbox lista) {
		if (sedutaDTOService.esisteSedutaPrecedenteConEsitoNonConfermato (seduta)) {
			Clients.showNotification ("Non è possibile numerare la delibera: esiste una seduta precedente con proposte senza esito confermato.", Clients.NOTIFICATION_TYPE_ERROR, null, "before_center", 5000, true);
			return
		}

		if (sedutaDTOService.esistonoDelibereNonNumerateInSedutePrecedenti(seduta)) {
			Clients.showNotification ("Non è possibile numerare la delibera: esiste una seduta precedente con proposte senza delibera numerata.", Clients.NOTIFICATION_TYPE_ERROR, null, "before_center", 5000, true);
			return
		}

		for (def oggettoSedutaDto : lista.selectedItems*.value.sort { it.sequenzaDiscussione }) {
			sedutaDTOService.creaDelibera (oggettoSedutaDto);
		}

		caricaListaProposte()

		BindUtils.postGlobalCommand(null, null, "abilitaTabFolders", null);
		BindUtils.postGlobalCommand(null, null, "onRefreshTesti", 	 null);
	}

	@Command
	@NotifyChange(["listaProposte", "up", "down", "selectedProposta", "isDrop"])
	void spostaOggetto(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx, @BindingParam("base") OggettoSedutaDTO base) {
		OggettoSedutaDTO oggettoSeduta = ctx.triggerEvent.dragged.value
		if (listaProposte.indexOf(base)>listaProposte.indexOf(oggettoSeduta)) {
			listaProposte.remove(oggettoSeduta);
			listaProposte.add(listaProposte.indexOf(base)+1, oggettoSeduta);
		} else {
			listaProposte.remove(oggettoSeduta);
			listaProposte.add(listaProposte.indexOf(base), oggettoSeduta);
		}
		riordinaODG()
		selectedProposta = null
		up = down = isDrop = false;
	}

	void riordinaODG () {
		int val = 1
		for (def proposta : listaProposte) {
			proposta.sequenzaDiscussione = val++
			oggettoSedutaDTOService.salva(proposta)
		}
	}

	@NotifyChange(["up", "down", "isDrop", "conferma"])
	@Command settaSelezione (@BindingParam("lista") Listbox lista) {
		if (lista.getSelectedItems().size() > 1 || lista.getSelectedItems().size() == 0) {
			up = down = isDrop = false;
		} else {
			isDrop = true
			OggettoSedutaDTO selezionato = lista.getSelectedItem().value
			up   = !(lista.getSelectedIndex() == 0)
			down = !(lista.getSelectedIndex() == listaProposte.size()-1)
		}

		// scorre la lista degli oggetti seduta e:
		// abilita il pulsante Assegna Esito solo se tutti gli elementi hanno stato "INSERITO" e non sono confermati.
		// abilita il pulsante Conferma Esito solo se tutti gli elementi hanno un esito e nessuno di questi è stato già confermato
		// abilita il pulsante Numera Delibera solo se tutte le proposte di delibera hanno esito confermato e positivo
		List<OggettoSedutaDTO> selezionati = lista.getSelectedItems().value
		conferma 		= true
		assegna 		= true
		numeraDelibera 	= true
		for (OggettoSedutaDTO oggetto : selezionati) {
			assegna  		&= (!oggetto.confermaEsito && !oggetto.isInIstruttoria())
			conferma 		&= (oggetto.esito != null && !oggetto.confermaEsito && oggetto.getStatoOdg() == StatoOdg.INSERITO)
			numeraDelibera 	&= (oggetto.confermaEsito && oggetto.esito.esitoStandard.codice == EsitoStandard.ADOTTATO && Delibera.countByOggettoSeduta(oggetto.domainObject) == 0)
		}
	}

	@NotifyChange(["listaProposte", "up", "down"])
	@Command onSuSequenza (@BindingParam("lista") Listbox lista) {
		int index = lista.getSelectedIndex()
		OggettoSedutaDTO selezionato = lista.getSelectedItem().value
		OggettoSedutaDTO superiore = lista.getItemAtIndex(index-1).value

		superiore.sequenzaDiscussione = superiore.sequenzaDiscussione + 1
		oggettoSedutaDTOService.salva(superiore)
		selezionato.sequenzaDiscussione = selezionato.sequenzaDiscussione - 1
		oggettoSedutaDTOService.salva(selezionato)

		listaProposte.remove(selezionato)
		listaProposte.add(index-1, selezionato)

		up   = !(index-1 == 0)
		down = !(index-1 == listaProposte.size()-1)
	}

	@NotifyChange(["listaProposte", "up", "down"])
	@Command onGiuSequenza (@BindingParam("lista") Listbox lista) {
		int index = lista.getSelectedIndex()
		OggettoSedutaDTO selezionato = lista.getSelectedItem().value
		OggettoSedutaDTO inferiore = lista.getItemAtIndex(index+1).value

		inferiore.sequenzaDiscussione = inferiore.sequenzaDiscussione - 1
		oggettoSedutaDTOService.salva(inferiore)
		selezionato.sequenzaDiscussione = selezionato.sequenzaDiscussione + 1
		oggettoSedutaDTOService.salva(selezionato)

		listaProposte.remove(selezionato)
		listaProposte.add(index+1, selezionato)

		up   = !(index +1 == 0)
		down = !(index +1 == listaProposte.size()-1)
	}

	@Command onLinkOggettoSeduta (@BindingParam("oggetto") OggettoSedutaDTO oggetto) {

		// se ho già un oggetto seduta aperto, non faccio niente:
		if (idOggettoSedutaAperto > 0) {
			return;
		}

		idOggettoSedutaAperto = oggetto.id;

		Window w = Executions.createComponents("/odg/oggettoSeduta.zul", self, [id : oggetto.id, wp : 'verbalizzazioneSeduta'])
		w.onClose {
			// tolgo il flag dell'oggetto seduta aperto:
			idOggettoSedutaAperto = -1;
			caricaListaProposte()
			BindUtils.postNotifyChange(null,null, this, "listaProposte")
		}
		w.doModal()
	}

	@Command getUnita (@ContextParam(ContextType.COMPONENT) Label lc, @BindingParam("oggetto") def oggetto) {
		String unita = "Unità Proponente: "
		So4UnitaPubb rs = PropostaDelibera.get(oggetto.id).getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4
		lc.value = ((rs) ? (unita +rs.descrizione ) : unita)
	}

	@NotifyChange(["proposteEsitoConfermato", "proposteEsitoDaConfermare"])
	private void calcolaNumeroProposte() {
		int esitoPresente=0, esitoNonPresente=0;

		for (OggettoSedutaDTO o : listaProposte) {
			if (o.confermaEsito)
				esitoPresente++;
			else
			   esitoNonPresente++;
		}

		proposteEsitoConfermato = esitoPresente
		proposteEsitoDaConfermare = esitoNonPresente

		BindUtils.postNotifyChange(null, null, this, "proposteEsitoConfermato")
		BindUtils.postNotifyChange(null, null, this, "proposteEsitoDaConfermare")
	}
}
