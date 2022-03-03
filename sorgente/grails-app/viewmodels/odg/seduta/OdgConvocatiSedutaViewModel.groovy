package odg.seduta

import it.finmatica.atti.dto.odg.ConvocatiSedutaDTOService
import it.finmatica.atti.dto.odg.OggettoPartecipanteDTOService
import it.finmatica.atti.dto.odg.SedutaDTO
import it.finmatica.atti.dto.odg.SedutaPartecipanteDTO
import it.finmatica.atti.odg.OggettoPartecipante
import it.finmatica.atti.odg.SedutaPartecipante
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Listbox
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class OdgConvocatiSedutaViewModel {

	// service
	ConvocatiSedutaDTOService convocatiSedutaDTOService
	OggettoPartecipanteDTOService oggettoPartecipanteDTOService

	// componenti
	Window self
	Window wParent

	// dati
	SedutaDTO					 seduta
	SedutaPartecipanteDTO 		 selectedPartecipante
	List<SedutaPartecipanteDTO>	 listaPartecipanti
	String 						 lbRuoliObbligatori

	// stato

	@Init init (@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("seduta") SedutaDTO seduta, @ExecutionArgParam("wp") Window wp)  {
		this.self 		= w
		this.seduta 	= seduta
		this.wParent 	= wp
		this.lbRuoliObbligatori = ""
		caricaListaPartecipanti()
	}

	@GlobalCommand
	public void onRefreshConvocati(@BindingParam("messages") def messages) {
		caricaListaPartecipanti(false, messages?:[])
	}

	private void caricaListaPartecipanti(boolean showNotify=true, def messages=null) {
		selectedPartecipante = null;

		// Siccome la join su AS4_ANAGRAFE_SOGGETTI è molto lenta con anagrafiche corpose (ad es. prov. ancona), non vado in join direttamente
		// nella query ma lascio che sia il DTO a recuperare singolarmente i soggetti
		listaPartecipanti = SedutaPartecipante.createCriteria().list() {
			eq ('seduta.id', seduta.id)
			eq ('convocato', true)
			order("sequenza", "asc")
			fetchMode ("ruoloPartecipante", FetchMode.JOIN)
		}
		listaPartecipanti = listaPartecipanti.toDTO(["componenteEsterno", "commissioneComponente.componente", "incarico", "commissioneComponente.incarico"])

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

	private boolean controlloEspressioneVoto (long idSedutaPartecipante) {
		int count = OggettoPartecipante.createCriteria().get() {
			projections{
				rowCount()
			}
			eq('sedutaPartecipante.id', idSedutaPartecipante)
			isNotNull('voto')
		}

		return (count > 0)
	}

	private boolean controlloConfermaEsito (long idSedutaPartecipante) {
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

	private boolean controlloEsito (long idSedutaPartecipante) {
		int count = OggettoPartecipante.createCriteria().get() {
			projections{
				rowCount()
			}
			eq('sedutaPartecipante.id', idSedutaPartecipante)
			oggettoSeduta {
				isNotNull('esito')
			}
		}

		return (count > 0)
	}

	@Command onCreaPartecipante() {
		Window w = Executions.createComponents("/odg/seduta/popupPartecipanti.zul", self, [id: -1, seduta: seduta, sezione:PopupPartecipantiViewModel.SEZIONE_CONVOCATI])
		w.onClose {
			caricaListaPartecipanti()
		}
		w.doModal()
	}

	@Command onModificaPartecipante (@BindingParam("partecipante") SedutaPartecipanteDTO partecipante) {
		Window w = Executions.createComponents("/odg/seduta/popupPartecipanti.zul", self, [id: partecipante.id, seduta: seduta, sezione:PopupPartecipantiViewModel.SEZIONE_CONVOCATI])
		w.onClose {
			caricaListaPartecipanti()
		}
		w.doModal()
	}

	@NotifyChange(["listaPartecipanti", "lbRuoliObbligatori"])
	@Command onEliminaPartecipante (@BindingParam("partecipante") SedutaPartecipanteDTO partecipante) {
		Messagebox.show("Sei sicuro di voler cancellare questo convocato?", "Conferma cancellazione del convocato",
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						if (controlloConfermaEsito(partecipante.id)) {
							Clients.showNotification("Non è possibile eliminare il convocato selezionato: esiste una proposta con un esito confermato.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 5000, true)
						} else if (controlloEsito(partecipante.id)) {
							Clients.showNotification("Non è possibile eliminare il convocato selezionato: esiste una proposta con un esito.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 5000, true)
						} else {
							if (controlloEspressioneVoto(partecipante.id)) {
								Messagebox.show("Esiste un'espressione di voto: sei sicuro di voler cancellare il convocato?", "Conferma eliminazione convocato", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
									new org.zkoss.zk.ui.event.EventListener() {
										public void onEvent(Event event) {
											if (Messagebox.ON_OK.equals(event.getName())) {
												oggettoPartecipanteDTOService.eliminaPartecipante(partecipante);
											}
										}
									}
								)
							} else {
								oggettoPartecipanteDTOService.eliminaPartecipante(partecipante)
								caricaListaPartecipanti()
							}
						}
					}
				}
			}
		)
	}

	@NotifyChange(["listaPartecipanti", "selectedPartecipante"])
	@Command onSuSequenza (@BindingParam("listaConvocati") Listbox listaConvocati) {
		def precedente = listaConvocati.getSelectedIndex()-1
		convocatiSedutaDTOService.spostaConvocatoSu(selectedPartecipante, listaConvocati.getItemAtIndex(precedente).value, listaConvocati.getSelectedIndex());
		def tmp = listaConvocati.find { it.id == selectedPartecipante.id }
		caricaListaPartecipanti()
		selectedPartecipante = tmp
	}

	@NotifyChange(["listaPartecipanti", "selectedPartecipante"])
	@Command onGiuSequenza (@BindingParam("listaConvocati") Listbox listaConvocati) {
		def successivo = listaConvocati.getSelectedIndex()+1
		convocatiSedutaDTOService.spostaConvocatoGiu(selectedPartecipante, listaConvocati.getItemAtIndex(successivo).value, listaConvocati.getSelectedIndex());
		def tmp = listaConvocati.find { it.id == selectedPartecipante.id }
		caricaListaPartecipanti()
		selectedPartecipante = tmp
	}
}
