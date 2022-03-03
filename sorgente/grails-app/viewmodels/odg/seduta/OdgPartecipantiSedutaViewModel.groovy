package odg.seduta

import it.finmatica.atti.dto.odg.*
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.odg.OggettoPartecipante
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.atti.odg.SedutaPartecipante
import it.finmatica.atti.odg.dizionari.RuoloPartecipante
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Listbox
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class OdgPartecipantiSedutaViewModel {

	// service
	OggettoPartecipanteDTOService   oggettoPartecipanteDTOService
	ConvocatiSedutaDTOService       convocatiSedutaDTOService
	SedutaDTOService 				sedutaDTOService

	// componenti
	Window self
	Window wParent

	// dati
	SedutaDTO					seduta
	SedutaPartecipanteDTO 		selectedPartecipante
	List<SedutaPartecipanteDTO>	listaPartecipanti
	String 						numeroPresenti
	String 						numeroAssenti
	boolean 					creaSecondaConvocazione
	boolean						abilitaCreaSecondaConvocazione
	boolean						mostraAssentiNonGiustificati

	// stato
	String 						ruoloSoggetto
	String						lbRuoliObbligatori

	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("seduta") SedutaDTO seduta, @ExecutionArgParam("wp") Window wp)  {
		this.self 		= w
		this.seduta 	= seduta
		this.wParent 	= wp
		ruoloSoggetto 	= ""
		numeroPresenti 	= ""
		numeroAssenti 	= ""

		caricaListaPartecipanti()
		calcolaNumPresenti()

		boolean assegnazioneEsiti = (OggettoSeduta.createCriteria().count {
			eq ("seduta.id",seduta.id)
			isNotNull("esito")
		} > 0);

		creaSecondaConvocazione 		= (seduta.commissione.secondaConvocazione && !assegnazioneEsiti && seduta.secondaSeduta == null)
		abilitaCreaSecondaConvocazione 	= (seduta.dataSecondaConvocazione != null && seduta.oraSecondaConvocazione != null)
		mostraAssentiNonGiustificati    = Impostazioni.ODG_MOSTRA_ASSENTI_NON_GIUSTIFICATI.abilitato;
	}

	/*
	 *  GESTIONE DEI PARTECIPANTI
	 */

	@Command onCreaPartecipante() {
		Window w = Executions.createComponents("/odg/seduta/popupPartecipanti.zul", self, [id: -1, seduta: seduta, sezione: PopupPartecipantiViewModel.SEZIONE_PARTECIPANTI])
		w.onClose {
			caricaListaPartecipanti()
			calcolaNumPresenti()
		}
		w.doModal()
	}

	@Command onModificaPartecipante(@BindingParam("partecipante") SedutaPartecipanteDTO partecipante) {
		Window w = Executions.createComponents("/odg/seduta/popupPartecipanti.zul", self, [id: partecipante.id, seduta: seduta, sezione: PopupPartecipantiViewModel.SEZIONE_PARTECIPANTI])
		w.onClose {
			caricaListaPartecipanti()
		}
		w.doModal()
	}

	@Command onEliminaPartecipante(@BindingParam("partecipante") SedutaPartecipanteDTO partecipante) {
		Messagebox.show("Sei sicuro di voler cancellare questo partecipante?", "Conferma cancellazione del partecipante",
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						if (sedutaDTOService.esistonoOggettiSedutaConfermatiConPartecipante(seduta.id, partecipante.id)) {
							Clients.showNotification("Non è possibile eliminare un partecipante presente in un oggetto seduta con esito già confermato.", Clients.NOTIFICATION_TYPE_ERROR, null, "before_center", 5000, true);
							return;
						}
						oggettoPartecipanteDTOService.eliminaPartecipante(partecipante);
						caricaListaPartecipanti()
					}
				}
			}
		)
	}

	@GlobalCommand
	public void onRefreshPartecipanti(@BindingParam("messages") def messages) {
		caricaListaPartecipanti(false, messages?:[])
		calcolaNumPresenti()
	}

	@GlobalCommand
	public void onRefreshPartecipantiAndShowNotify() {
		caricaListaPartecipanti(true, null)
		calcolaNumPresenti()
	}

	private void caricaListaPartecipanti(boolean showNotify=true, def messages=null) {
		selectedPartecipante = null;

		listaPartecipanti = SedutaPartecipante.createCriteria().list() {
			projections {
				"incarico.titolo"
				"commissioneComponente.incarico.titolo"
			}

			eq('seduta.id', seduta.id)

			fetchMode("commissioneComponente", 				FetchMode.JOIN)
			fetchMode("ruoloPartecipante", 					FetchMode.JOIN)
			fetchMode("commissioneComponente.incarico", 	FetchMode.JOIN)
			fetchMode("incarico", 							FetchMode.JOIN)

			order("sequenzaPartecipante", "asc")

			// siccome posso avere anagrafiche molto grandi, evito di andarci in join (va in fulljoin)
			// ma per ogni partecipante faccio due query dirette.
			// Con questa soluzione, alla provincia di Ancona (300K record in as4_soggetti_correnti), questa query passa da 10s a 200ms.
		}.toDTO(["componenteEsterno", "commissioneComponente.componente"])

		// se necessario, ricalcolo i messaggi di warning per l'utente:
		lbRuoliObbligatori = "";
		if (messages == null) {
			messages = convocatiSedutaDTOService.checkRuoliObbligatori(seduta);
		}

		if (messages.size() > 0) {
			lbRuoliObbligatori = "ATTENZIONE: "+messages.join(", ");
		}

		// di default devo propagare la notifica all'utente e agli altri viewModel.
		// se però questo stesso metodo è stato invocato dal globalcommand (quindi ha showNotify = false),
		// allora non devo fare nulla.
		if (showNotify) {
			if (messages.size() > 0) {
				Clients.showNotification("ATTENZIONE: "+messages.join(".\n"), Clients.NOTIFICATION_TYPE_ERROR, null, "before_center", 5000, true);
			}
			BindUtils.postGlobalCommand (null, null, "onRefreshPartecipanti", [messages:messages]);
			BindUtils.postGlobalCommand (null, null, "abilitaTabFolders", 	  null);
		}

		BindUtils.postNotifyChange (null, null, this, "selectedPartecipante")
		BindUtils.postNotifyChange (null, null, this, "listaPartecipanti")
		BindUtils.postNotifyChange (null, null, this, "lbRuoliObbligatori")
	}

	private void calcolaNumPresenti () {
		int num_presenti=0, num_assenti=0;

		for (SedutaPartecipanteDTO p : listaPartecipanti) {

			// non considero il segretario, gli invitati ed il presidente se non vota.
			if (p.ruoloPartecipante?.codice == RuoloPartecipante.CODICE_SEGRETARIO ||
				p.ruoloPartecipante?.codice == RuoloPartecipante.CODICE_INVITATO ||
				(p.ruoloPartecipante?.codice == RuoloPartecipante.CODICE_PRESIDENTE && !seduta.votoPresidente)) {
				continue;
			}

			if (p.assenteNonGiustificato) {
				num_assenti++;
				continue;
			}

			if (p.presente) {
				num_presenti++;
				continue;
			}

			num_assenti++;
		}
		numeroPresenti = num_presenti
		numeroAssenti  = num_assenti
		BindUtils.postNotifyChange(null, null, this, "numeroPresenti")
		BindUtils.postNotifyChange(null, null, this, "numeroAssenti")
	}

	@NotifyChange(["selectedPartecipante","numeroPresenti","numeroAssenti"])
	@Command onSettaPresenza(@BindingParam("valore") String valore, @BindingParam("partecipante") SedutaPartecipanteDTO partecipante) {
		partecipante.presente = (valore=="Presenti")
		partecipante.assenteNonGiustificato = (valore=="Assenti Non Giustificati")
		partecipante = convocatiSedutaDTOService.salva(partecipante)
		selectedPartecipante = partecipante
		calcolaNumPresenti()
		BindUtils.postNotifyChange(null, null, this, "selectedPartecipante")
	}

	@NotifyChange(["listaPartecipanti","numeroPresenti","numeroAssenti"])
	@Command onSelezionaTuttiPresenti() {
		convocatiSedutaDTOService.impostaTuttiPresenti (seduta);
		Clients.showNotification("Proposta salvata.", Clients.NOTIFICATION_TYPE_INFO, null, "before_center", 1000, true);
		caricaListaPartecipanti()
		calcolaNumPresenti()
	}

	private boolean controlloConfermaEsito(long idSedutaPartecipante) {
		int count = OggettoPartecipante.createCriteria().get() {
			projections{
				rowCount()
			}
			eq('sedutaPartecipante.id', idSedutaPartecipante)
			oggettoSeduta {
				eq('confermaEsito',true)
			}
		}

		return (count > 0)
	}

	@NotifyChange(["listaPartecipanti", "selectedPartecipante"])
	@Command onSuSequenza (@BindingParam("listaPartecipanti") Listbox listaPartecipanti) {
		def precedente = listaPartecipanti.getSelectedIndex()-1
		convocatiSedutaDTOService.spostaPartecipanteSu(selectedPartecipante, listaPartecipanti.getItemAtIndex(precedente).value, listaPartecipanti.getSelectedIndex());
		def tmp = listaPartecipanti.find { it.id == selectedPartecipante.id }
		caricaListaPartecipanti()
		selectedPartecipante = tmp
	}

	@NotifyChange(["listaPartecipanti", "selectedPartecipante"])
	@Command onGiuSequenza (@BindingParam("listaPartecipanti") Listbox listaPartecipanti) {
		def successivo = listaPartecipanti.getSelectedIndex()+1
		convocatiSedutaDTOService.spostaPartecipanteGiu(selectedPartecipante, listaPartecipanti.getItemAtIndex(successivo).value, listaPartecipanti.getSelectedIndex());
		def tmp = listaPartecipanti.find { it.id == selectedPartecipante.id }
		caricaListaPartecipanti()
		selectedPartecipante = tmp
	}

	/*
	 *  GESTIONE SECONDA SEDUTA
	 */

	@NotifyChange("listaPartecipanti")
	@Command onCreaSecondaSeduta() {
		if (seduta.dataSecondaConvocazione == null || seduta.oraSecondaConvocazione == null) {
			Messagebox.show("Occorre settare la data e l'ora della seconda convocazione!")
		}

		seduta = sedutaDTOService.creaSecondaSeduta(seduta).toDTO();
		apriSecondaSeduta(seduta.secondaSeduta.id)
	}

	private void apriSecondaSeduta (long idSeduta) {
		Window w = Executions.getCurrent().createComponents("/odg/seduta/index.zul", null, [id : idSeduta])
		w.doModal()
		Events.postEvent(Events.ON_CLOSE, wParent, null)
	}
}
