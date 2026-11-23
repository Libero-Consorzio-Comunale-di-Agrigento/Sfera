package odg.seduta

import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.PropostaDeliberaSoggetto
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.StatoOdg
import it.finmatica.atti.dto.documenti.DeliberaDTO
import it.finmatica.atti.dto.odg.*
import it.finmatica.atti.dto.odg.dizionari.EsitoDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.atti.odg.SedutaPartecipante
import it.finmatica.atti.odg.dizionari.EsitoStandard
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.hibernate.FetchMode
import org.zkoss.bind.BindContext
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Component
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.*

class OdgVerbalizzazioneSedutaOrdinaViewModel {

   	// service
	OggettoSedutaDTOService oggettoSedutaDTOService
	SedutaDTOService		sedutaDTOService
	ConvocatiSedutaDTOService convocatiSedutaDTOService

	// componenti
	Window self

	// dati
	SedutaDTO				seduta
	OggettoSedutaDTO		selectedProposta
	//List<OggettoSedutaDTO>	listaProposte
	ListModelList<OggettoSedutaDTO> listaProposte = new ListModelList<>()

	// stato
	boolean up 				= false
	boolean down 			= false
	boolean isDrop 			= false
	boolean conferma 		= false
	boolean assegna			= false
	boolean numeraDelibera	= false

	//Impostazioni
	boolean dataOraPresenzaComponenti

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

		//Parametri
		dataOraPresenzaComponenti=Impostazioni.ODG_DATA_ORA_PRESENZA_COMPONENTI.isAbilitato()
		odgNumeraDelibere = Impostazioni.ODG_NUMERA_DELIBERE.isAbilitato()

		listaProposte.clear()

		caricaListaProposte()
	}

	// per selectedTab
	@GlobalCommand
	@Command
	void onRefreshVerbalizzazioneOrdina (Event e) {
		caricaListaProposte()
	}

	// per compatibilità
	@GlobalCommand
	@Command
	void onRefreshVerbalizzazione (Event e) {
		caricaListaProposte()
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
		listaProposte.setMultiple(false)

		caricaUnitaProponenti(listaProposte)
		caricaLabelDelibere(listaProposte)

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
			if (proposta.sequenzaDiscussione != val) {
				proposta.sequenzaDiscussione = val
				oggettoSedutaDTOService.salva(proposta)
			}
			val++
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
			numeraDelibera 	= numeraDelibera && (oggetto.confermaEsito && oggetto.esito.esitoStandard.codice == EsitoStandard.ADOTTATO && Delibera.countByOggettoSeduta(oggetto.domainObject) == 0)
		}
	}

	/*
	@NotifyChange(["listaProposte", "up", "down"])
	@Command onSuSequenza (@BindingParam("lista") Listbox lista) {
		int index = lista.getSelectedIndex()
		if(index>0) {
			OggettoSedutaDTO selezionato = lista.getSelectedItem().value
			OggettoSedutaDTO superiore = lista.getItemAtIndex(index - 1).value

			superiore.sequenzaDiscussione = superiore.sequenzaDiscussione + 1
			oggettoSedutaDTOService.salva(superiore)
			selezionato.sequenzaDiscussione = selezionato.sequenzaDiscussione - 1
			oggettoSedutaDTOService.salva(selezionato)

			listaProposte.remove(selezionato)
			listaProposte.add(index - 1, selezionato)

			up = !(index - 1 == 0)
			down = !(index - 1 == listaProposte.size() - 1)
		}
	}
	*/

	@NotifyChange(["listaProposte", "selectedProposta", "lista"])
	@Command
	onSuSequenza(@BindingParam("lista") Listbox lista) {
		if (lista.getSelectedIndex() > 0) {
			def corrente = lista.getItemAtIndex(lista.getSelectedIndex()).value
			def precedente = lista.getItemAtIndex(lista.getSelectedIndex() - 1).value

			Long sequenzaTmpCampo = precedente.sequenzaDiscussione
			precedente.sequenzaDiscussione = corrente.sequenzaDiscussione
			corrente.sequenzaDiscussione = sequenzaTmpCampo

			listaProposte.sort { it.sequenzaDiscussione }

			selectedProposta = corrente

			oggettoSedutaDTOService.spostaOggettoSedutaSu(selectedProposta, precedente)
		}
	}

	/*
	@NotifyChange(["listaProposte", "up", "down"])
	@Command onGiuSequenza (@BindingParam("lista") Listbox lista) {
		int index = lista.getSelectedIndex()
		if(index < lista.getItems().size()-1) {
			OggettoSedutaDTO selezionato = lista.getSelectedItem().value
			OggettoSedutaDTO inferiore = lista.getItemAtIndex(index + 1).value

			inferiore.sequenzaDiscussione = inferiore.sequenzaDiscussione - 1
			oggettoSedutaDTOService.salva(inferiore)
			selezionato.sequenzaDiscussione = selezionato.sequenzaDiscussione + 1
			oggettoSedutaDTOService.salva(selezionato)

			listaProposte.remove(selezionato)
			listaProposte.add(index + 1, selezionato)

			up = !(index + 1 == 0)
			down = !(index + 1 == listaProposte.size() - 1)
		}
	}
	*/

	@NotifyChange(["listaProposte", "selectedProposta", "lista"])
	@Command
	onGiuSequenza(@BindingParam("lista") Listbox lista) {
		if (lista.getSelectedIndex() >= 0 && lista.getSelectedIndex() < listaProposte.size() - 1) {
			def corrente = lista.getItemAtIndex(lista.getSelectedIndex()).value
			def successivo = lista.getItemAtIndex(lista.getSelectedIndex() + 1).value

			Long sequenzaTmpCampo = successivo.sequenzaDiscussione
			successivo.sequenzaDiscussione = corrente.sequenzaDiscussione
			corrente.sequenzaDiscussione = sequenzaTmpCampo

			listaProposte.sort { it.sequenzaDiscussione }

			selectedProposta = corrente

			oggettoSedutaDTOService.spostaOggettoSedutaGiu(selectedProposta, successivo)
		}
	}

	@Command getUnita (@ContextParam(ContextType.COMPONENT) Label lc, @BindingParam("oggetto") def oggetto) {
		String unita = "Unità Proponente: "
		So4UnitaPubb rs = PropostaDelibera.get(oggetto.id).getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4
		lc.value = ((rs) ? (unita +rs.descrizione ) : unita)
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

	private void caricaLabelDelibere(List lista) {
		def ids = lista*.id.findAll { it != null }
		if (!ids) return
		List delibere = Delibera.createCriteria().list {
			oggettoSeduta { 'in'("id", ids) }
			isNotNull("propostaDelibera")
			fetchMode("registroDelibera", FetchMode.JOIN)
		}
		Map labelMap = delibere.collectEntries { d ->
			String label = ""
			if (d.numeroDelibera > 0) {
				label = "${d.numeroDelibera}\\${d.annoDelibera}"
				if (d.registroDelibera?.descrizione) label += " ${d.registroDelibera.descrizione}"
			}
			if (d.oggetto) label += " - ${d.oggetto}"
			[(d.oggettoSeduta.id): label]
		}
		lista.each { o -> o.labelDelibera = labelMap[o.id] ?: "" }
	}
}
