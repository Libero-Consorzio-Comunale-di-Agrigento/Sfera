package dizionari.atti

import afc.AfcAbstractRecord
import it.finmatica.atti.dizionari.Email
import it.finmatica.atti.dizionari.Notifica
import it.finmatica.atti.dizionari.NotificaEmail
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.documenti.NotificheService
import it.finmatica.atti.dto.dizionari.EmailDTO
import it.finmatica.atti.dto.dizionari.NotificaDTO
import it.finmatica.atti.dto.dizionari.NotificaDTOService
import it.finmatica.atti.dto.dizionari.NotificaEmailDTO
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.atti.odg.Commissione
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgStep
import org.hibernate.FetchMode
import org.hibernate.criterion.CriteriaSpecification
import org.zkoss.bind.BindContext
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class NotificaDettaglioViewModel extends AfcAbstractRecord {

	// service
	NotificaDTOService 		notificaDTOService

	// componenti
	Window popupNuovoDestinatario

	// dati
	List<TipoNotifica>		listaTipoNotifica
	List<CommissioneDTO>	listaCommissione
	List<EmailDTO>			listaEmail
	List<NotificaEmailDTO>	listaNotificaEmail
	List<NotificaEmailDTO>	listaDestinatari
	EmailDTO 				selectedEmail
	NotificaEmailDTO		selectedNotificaEmail
	NotificaEmailDTO		destinatario
	def listaTipiAllegati
	def listaTipiDestinatari
	def listaStep
	def oggettiDisponibili

	// stato
	boolean	inModifica  = false
	String stato
	boolean	commissione = false

	@NotifyChange(["selectedRecord", "soggettiList", "totalSize"])
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w
		if (id != null) {
			caricaNotificaDto(id)

			commissione = (selectedRecord.tipoNotifica == TipoNotifica.CONVOCAZIONE_SEDUTA || selectedRecord.tipoNotifica == TipoNotifica.VERBALE_SEDUTA)

		} else {
			selectedRecord = new NotificaDTO(valido:true)
		}
		listaTipoNotifica 		= TipoNotifica.lista;
		listaCommissione  		= Commissione.list().toDTO()
		listaTipiDestinatari 	= ['' : [descrizione:'-- nessuno --']] + NotificheService.DESTINATARI;
		listaTipiAllegati 		= ['' : [descrizione:'-- nessuno --']] + NotificheService.ALLEGATI;
	}

	@AfterCompose
	public void afterCompose(@SelectorParam("#popupNuovoDestinatario") Window popupNuovoDestinatario) {
		this.popupNuovoDestinatario = popupNuovoDestinatario
	}

	public def getCampiDisponibili () {
		// ottengo i campi che sono disponibili per gli oggetti di questa notifica:
		return NotificheService.CAMPI.findAll { campo -> (campo.value.oggetti.intersect(oggettiDisponibili.findAll { it.selezionato }.codice).size() > 0) }
	}

	@NotifyChange('destinatario')
	@Command onAggiungiDestinatario () {
		this.destinatario = new NotificaEmailDTO(id:-1, notifica:selectedRecord)
		this.popupNuovoDestinatario.doModal();
	}

	@Command onChiudiPopup () {
		this.popupNuovoDestinatario.setVisible(false);
		this.destinatario = null;
	}

	@Command onCercaSoggetto () {
		Window w = Executions.createComponents("/commons/popupRicercaSoggetti.zul", self, [id: -1])
		w.onClose { Event event ->
			if (event.data != null) {
				this.destinatario.soggetto = event.data;
				BindUtils.postNotifyChange(null, null, this, "destinatario")
			}
		}
		w.doModal()
	}

	@NotifyChange(["listaEmail", "listaNotificaEmail", "listaDestinatari"])
	@Command onSalvaDestinatario () {
		notificaDTOService.salva(destinatario)
		refreshListe();
		this.popupNuovoDestinatario.setVisible(false);
	}

	@NotifyChange(["listaEmail", "listaNotificaEmail", "listaDestinatari"])
	@Command onEliminaDestinatario (@BindingParam("destinatario")NotificaEmailDTO destinatario) {
		notificaDTOService.elimina(destinatario)
		refreshListe();
	}

	private void caricaNotificaDto (Long idNotifica) {
		Notifica notifica = Notifica.createCriteria().get {
			eq ("id", idNotifica)
			fetchMode("utenteIns", FetchMode.JOIN)
			fetchMode("utenteUpd", FetchMode.JOIN)
		}

		selectedRecord = notifica.toDTO()

		aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
		aggiornaDatiModifica (selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)

		refreshOggettiDisponibili()
		refreshListe();
		refreshListaStep()


		BindUtils.postNotifyChange (null, null, this, "selectedRecord")
		BindUtils.postNotifyChange (null, null, this, "datiCreazione")
		BindUtils.postNotifyChange (null, null, this, "datiModifica")
		BindUtils.postNotifyChange (null, null, this, "listaEmail")
		BindUtils.postNotifyChange (null, null, this, "listaStep")
		BindUtils.postNotifyChange (null, null, this, "listaNotificaEmail")
	}

	public void refreshListe () {
		listaNotificaEmail = NotificaEmail.createCriteria().list {
			createAlias ("email", 	"s_email", 	CriteriaSpecification.LEFT_JOIN)
			eq("notifica.id", selectedRecord.id)
			isNotNull("email")
			order("s_email.ragioneSociale", "asc")
			order("s_email.cognome", "asc")
			order("s_email.nome", "asc")
			fetchMode("notifica", FetchMode.JOIN)
			fetchMode("email", FetchMode.JOIN)
		}.toDTO()

		listaDestinatari = NotificaEmail.createCriteria().list {
			createAlias ("email", 	"s_email", 	CriteriaSpecification.LEFT_JOIN)
			eq("notifica.id", selectedRecord.id)
			isNull("email")
			order("s_email.ragioneSociale", "asc")
			order("s_email.cognome", "asc")
			order("s_email.nome", "asc")
			fetchMode("ruolo", FetchMode.JOIN)
			fetchMode("unita", FetchMode.JOIN)
		}.toDTO(["soggetto"])

		listaEmail = Email.createCriteria().list {
			eq("valido", true)
			if (listaNotificaEmail.size()>0) {
				not {
					'in' ("id", listaNotificaEmail*.email?.id)
				}
			}
			order("ragioneSociale", "asc")
			order("cognome", "asc")
			order("nome", "asc")
			fetchMode("notifica", FetchMode.JOIN)
		}.toDTO()
	}

	private void refreshListaStep () {
		listaStep = selectedRecord.tipo?.azioni == null ? null : WkfCfgStep.createCriteria().list {
			createAlias ("azioniIngresso", 	"azioniIn", 	CriteriaSpecification.LEFT_JOIN)
			createAlias ("azioniUscita", 	"azioniOut", 	CriteriaSpecification.LEFT_JOIN)

			projections {
				cfgIter { groupProperty ("nome") }

				groupProperty ("titolo")
			}

			cfgIter {
				eq ("stato", WkfCfgIter.STATO_IN_USO)
			}

			or {
				for (String metodoAzione : selectedRecord.tipo.azioni.split(",")) {
					and {
						eq ("azioniIn.nomeBean",   metodoAzione.split("\\.")[0]);
						eq ("azioniIn.nomeMetodo", metodoAzione.split("\\.")[1]);
					}
				}

				for (String metodoAzione : selectedRecord.tipo.azioni.split(",")) {
					and {
						eq ("azioniOut.nomeBean",   metodoAzione.split("\\.")[0]);
						eq ("azioniOut.nomeMetodo", metodoAzione.split("\\.")[1]);
					}
				}
			}
		}.collect { [titoloIter:it[0], titoloStep:it[1]] }
	}

	private void refreshOggettiDisponibili () {
		def listaOggetti = selectedRecord.getListaOggetti();
		oggettiDisponibili = TipoNotifica.lista.find { it.codice == selectedRecord.tipoNotifica }?.oggetti.collect { [codice: it, selezionato: listaOggetti.contains(it)] };
	}

	@NotifyChange(["campiDisponibili"])
	@Command onSelezionaOggettiDisponibili () {
	}

	@NotifyChange(["commissione", "selectedRecord", "selectedRecord.tipo", "listaStep", "oggettiDisponibili", "campiDisponibili"])
	@Command onChangeTipologia() {
		if (selectedRecord.tipoNotifica == TipoNotifica.CONVOCAZIONE_SEDUTA || selectedRecord.tipoNotifica == TipoNotifica.VERBALE_SEDUTA) {
			commissione = true
		} else {
			commissione = false
			selectedRecord.commissione = null
		}

		// di default, seleziono tutti gli oggetti:
		selectedRecord.oggetti = TipoNotifica.lista.find { it.codice == selectedRecord.tipoNotifica }?.oggetti?.join("#")

		refreshListaStep();
		refreshOggettiDisponibili();
	}

	//////////////////////////////////////////
	//				SALVATAGGIO				//
	//////////////////////////////////////////

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica", "listaEmail", "listaNotificaEmail"])
	@Command onDuplica () {
		// prima salvo, poi duplico:
		if (!onSalva()) {
			return false;
		}

		// se ho salvato correttamente, duplico la notifica:
		selectedRecord = notificaDTOService.duplica (selectedRecord);
		caricaNotificaDto(selectedRecord.id);

		Clients.showNotification("Notifica duplicata.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true)
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica", "listaEmail", "listaNotificaEmail"])
	@Command onSalva () {

		// Prima di salvare, controllo che nel testo e nell'oggetto della notifica ci siano solo campi validi:
		// ottengo i campi scritti nel testo:
		def campiUsati = controllaCampiUsati(selectedRecord.oggetto);
		if (campiUsati.size() > 0) {
			Clients.showNotification("Non è possibile salvare: nell'Oggetto sono stati inseriti dei campi non validi: "+campiUsati.join(","), Clients.NOTIFICATION_TYPE_ERROR, null, "middle_center", 5000, true);
			return false;
		}

		campiUsati = controllaCampiUsati(selectedRecord.testo);
		if (campiUsati.size() > 0) {
			Clients.showNotification("Non è possibile salvare: nel Testo sono stati inseriti dei campi non validi: "+campiUsati.join(","), Clients.NOTIFICATION_TYPE_ERROR, null, "middle_center", 5000, true);
			return false;
		}

		selectedRecord.oggetti = oggettiDisponibili.findAll { it.selezionato }.codice.join ("#");
		def idNotifica = notificaDTOService.salva(selectedRecord).id
		caricaNotificaDto(idNotifica)

		Clients.showNotification("Notifica salvata.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true)
		return true;
	}

	private def controllaCampiUsati (String testo) {
		def campiOggetto 	 = ( testo =~ /\[([\w]+)\]/ ).collect { match -> match[1] };
		def campiDisponibili = getCampiDisponibili().collect { it.key };

		return (campiOggetto - campiDisponibili);
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalvaChiudi() {
		onSalva()
		onChiudi()
	}

	@Command onSettaValido(@BindingParam("valido") boolean valido) {
		Messagebox.show("Modificare la validità della notifica?", "Modifica validità",
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						super.getSelectedRecord().valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, NotificaDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, NotificaDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, NotificaDettaglioViewModel.this, "datiModifica")
					}
				}
			})
	}

	@Command
	@NotifyChange(["listaEmail", "listaNotificaEmail"])
	public void dropToList1(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		if (ctx.triggerEvent.dragged.value!=null && !listaEmail.contains(ctx.triggerEvent.dragged.value)) {
			NotificaEmailDTO selezionato = ctx.triggerEvent.dragged.value
			notificaDTOService.elimina(selezionato)
			refreshListe()
		}
	}

	@Command
	@NotifyChange(["listaEmail", "listaNotificaEmail"])
	public void dropToList2(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		if (ctx.triggerEvent.dragged.value!=null && !listaNotificaEmail.contains(ctx.triggerEvent.dragged.value)) {
			NotificaEmailDTO nuovo = new NotificaEmailDTO(id: -1)
			EmailDTO selezionata = ctx.triggerEvent.dragged.value
			nuovo.email = selezionata
			nuovo.notifica = selectedRecord
			notificaDTOService.salva(nuovo)
			refreshListe()
		}
	}

	@Command
	@NotifyChange(["listaEmail", "listaNotificaEmail"])
	public void insertToList1(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		if (!listaEmail.contains(ctx.triggerEvent.dragged.value)) {
			NotificaEmailDTO selezionato = ctx.triggerEvent.dragged.value
			notificaDTOService.elimina(selezionato)
			refreshListe()
		}
	}

	@Command
	@NotifyChange(["listaEmail", "listaEmailAssociate"])
	public void insertToList2(@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx){
		if (!listaNotificaEmail.contains(ctx.triggerEvent.dragged.value)) {
			NotificaEmailDTO nuovo = new NotificaEmailDTO(id: -1)
			EmailDTO selezionata = ctx.triggerEvent.dragged.value
			nuovo.email = selezionata
			nuovo.notifica = selectedRecord
			notificaDTOService.salva(nuovo)
			refreshListe()
		}
	}

	@NotifyChange(["listaEmail", "listaNotificaEmail"])
	@Command onSelTuttoADx (@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		listaEmail.each { EmailDTO selezionata ->
			NotificaEmailDTO nuovo = new NotificaEmailDTO(id: -1)
			nuovo.email = selezionata
			nuovo.notifica = selectedRecord
			notificaDTOService.salva(nuovo)
		}
		refreshListe()
	}

	@NotifyChange(["listaEmail", "listaNotificaEmail"])
	@Command onSelTuttoASx (@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		listaNotificaEmail.each { NotificaEmailDTO selezionato ->
			notificaDTOService.elimina(selezionato)
		}
		refreshListe()
	}

	@NotifyChange(["listaEmail", "listaNotificaEmail", "selectedEmail"])
	@Command onSelADx (@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		NotificaEmailDTO nuovo = new NotificaEmailDTO(id: -1)
		nuovo.email = selectedEmail
		nuovo.notifica = selectedRecord
		notificaDTOService.salva(nuovo)
		refreshListe()
	}

	@NotifyChange(["listaEmail", "listaNotificaEmail", "selectedNotificaEmail"])
	@Command onSelASx (@ContextParam(ContextType.BIND_CONTEXT) BindContext ctx) {
		notificaDTOService.elimina(selectedNotificaEmail)
		refreshListe()
	}
}
