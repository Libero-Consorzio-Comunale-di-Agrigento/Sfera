package atti.integrazioni.contabilita

import it.finmatica.atti.dizionari.DizionarioContabile
import it.finmatica.atti.integrazioni.contabilita.IntegrazioneContabilitaAtsPavia
import it.finmatica.atti.integrazioni.ricercadocumenti.PagedList
import it.finmatica.so4.struttura.So4Amministrazione
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Window
import org.zkoss.zul.event.PagingEvent

class PopupRicercaDizionariAtsPaviaViewModel {
	// componenti
	Window self
	IntegrazioneContabilitaAtsPavia integrazioneContabilitaAtsPavia

	So4Amministrazione ente

	String dizionario; // può essere "CONTO", "BILANCIO", "SOTTOBUDGET"
	String titolo
	DizionarioContabile dizionarioSelezionato
	PagedList<DizionarioContabile> listaDizionari
	String codice
	String descrizione

	int startFrom = 0
	int maxResults = 100

	/*
	 * Questa popup può funzionare per i tre tipi di dizionario:
	 * - conto
	 * - bilancio
	 * - sottoBudget
	 */
	@Init init (@ContextParam(ContextType.COMPONENT) Window w
				, @ExecutionArgParam("ente") So4Amministrazione ente
				, @ExecutionArgParam("dizionario") String dizionario)  {
		this.self = w
		this.ente = ente
		this.titolo = "Ricerca "+ dizionario
		this.dizionario = dizionario.toUpperCase();
		this.codice = '';
		this.descrizione = '';
	}

	@NotifyChange(["listaDizionari"])
	@Command
	void onRicerca(@BindingParam("codice") String codice, @BindingParam("descrizione") String descrizione) {
		if (((codice == null) || (codice.equals(""))) && ((descrizione == null) || (descrizione.equals("")))) {
			Clients.showNotification("Inserire codice o descrizione per effettuare la ricerca.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 3000, true)
		} else {
			this.startFrom = 0
			this.codice = codice
			this.descrizione = descrizione
			listaDizionari = integrazioneContabilitaAtsPavia.getDizionario(this.ente, dizionario, codice, descrizione, startFrom, maxResults)
			BindUtils.postNotifyChange(null, null, this, "listaDizionari")
		}
	}

	@NotifyChange(["listaDizionari"])
	@Command
	void onPagina (@ContextParam(ContextType.TRIGGER_EVENT) PagingEvent pagingEvent) {
		startFrom = pagingEvent.activePage * maxResults
		listaDizionari = integrazioneContabilitaAtsPavia.getDizionario(this.ente, dizionario, codice, descrizione, startFrom, maxResults)
		BindUtils.postNotifyChange(null, null, this, "listaDizionari")
	}

	@Command
	@NotifyChange(["*"])
	void onSelect () {
		listaDizionari = new PagedList<>([], 0)
	}

	@Command onChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@Command
	onSalva() {
		Events.postEvent(Events.ON_CLOSE, self, dizionarioSelezionato)
	}
}