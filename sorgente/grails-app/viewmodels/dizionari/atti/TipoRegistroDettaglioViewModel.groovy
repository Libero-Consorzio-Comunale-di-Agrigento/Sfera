package dizionari.atti

import afc.AfcAbstractRecord
import it.finmatica.atti.dizionari.Registro
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.dto.dizionari.RegistroDTO
import it.finmatica.atti.dto.dizionari.RegistroDTOService
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.dizionari.TipoRegistroDTOService
import it.finmatica.atti.impostazioni.Impostazioni
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class TipoRegistroDettaglioViewModel extends AfcAbstractRecord {

	// services
	TipoRegistroDTOService	tipoRegistroDTOService
	RegistroDTOService 		registroDTOService

	// dati
	List<RegistroDTO> 	listaRegistroDTO

	// stato
	boolean visualizzaTutti
	boolean paginaUnicaAttiva

	@NotifyChange(["selectedRecord", "listaRegistroDTO","creaRegistroVisible"])
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("codice") String codice) {
		this.self = w

		if (codice == null) {
			selectedRecord = new TipoRegistroDTO(valido:true)
		} else {
			selectedRecord = TipoRegistro.get(codice).toDTO()
			caricaListaRegistro()
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		}
		paginaUnicaAttiva = Impostazioni.VIS_PAGINA_UNICA_ATTIVA.abilitato
	}

	private void caricaListaRegistro() {
		listaRegistroDTO = Registro.createCriteria().list {
			eq ("tipoRegistro.codice", selectedRecord?.codice)
			if (!visualizzaTutti) {
				eq ("valido", true)
			}
			order("anno",   "desc")
			order("valido", "desc")
		}.toDTO()
		BindUtils.postNotifyChange(null, null, this, "listaRegistroDTO")
	}

	@Command onCrea() {
		Window w = Executions.createComponents ("/dizionari/atti/registroDettaglio.zul", self, [tipoRegistro: selectedRecord])
		w.onClose {
			caricaListaRegistro()
			BindUtils.postNotifyChange(null, null, this, "listaRegistroDTO")
		}
		w.doModal()
	}

	@Command onVisualizzaTutti () {
		visualizzaTutti = !visualizzaTutti
		caricaListaRegistro()
		BindUtils.postNotifyChange(null, null, this, "visualizzaTutti")
	}

	/*
	 *  implementazione record di AfcAbstractRecord
	 */

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica","creaRegistroVisible"])
	@Command onSalva () {
		selectedRecord = tipoRegistroDTOService.salva(selectedRecord)
		aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
		aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
	}

	@Command onSalvaChiudi() {
		onSalva()
		onChiudi()
	}

	@Command onSettaValido(@BindingParam("valido") boolean valido) {
		Messagebox.show(Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTesto",[valido?"valido":"non valido"].toArray()), Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						TipoRegistroDettaglioViewModel.this.selectedRecord.valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, TipoRegistroDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, TipoRegistroDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, TipoRegistroDettaglioViewModel.this, "datiModifica")
					}
				}
			}
		)
	}

	@Command onEliminaRegistro (@BindingParam("reg") RegistroDTO registroDto) {
		Messagebox.show("Sei sicuro di voler chiudere il Registro?", "Chiudere Registro",
				Messagebox.OK | Messagebox.CANCEL, Messagebox.EXCLAMATION,
				new org.zkoss.zk.ui.event.EventListener() {
					void onEvent(Event e) {
						if (Messagebox.ON_OK.equals(e.getName())) {
							if (registroDto.valido) {
								registroDTOService.chiudiRegistro (registroDto)
								caricaListaRegistro ()
							} else {
								registroDTOService.riapriRegistro (registroDto)
								caricaListaRegistro ()
							}
							BindUtils.postNotifyChange(null, null, this, "listaRegistroDTO")
						}
					}
				}
		)
	}
}
