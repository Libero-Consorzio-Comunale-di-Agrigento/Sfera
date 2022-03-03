package dizionari.impostazioni

import afc.AfcAbstractRecord
import it.finmatica.atti.impostazioni.RegolaCampo
import it.finmatica.atti.impostazioni.RegolaCampoService
import it.finmatica.atti.dto.impostazioni.RegolaCampoDTO
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAttore
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class RegolaCampoDettaglioViewModel extends AfcAbstractRecord {

	RegolaCampoDTO selectedRecord

	// services
	RegolaCampoService regolaCampoService
	List<it.finmatica.gestioneiter.dto.configuratore.dizionari.WkfTipoOggettoDTO> listaTipiOggetto
	List<it.finmatica.gestioneiter.dto.configuratore.dizionari.WkfAttoreDTO> listaAttori


	@NotifyChange(["selectedRecord"])
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("tipoRegistro") TipoRegistroDTO tipoRegistro , @ExecutionArgParam("id") Long id) {
		this.self = w
		if (id != null) {
			selectedRecord = RegolaCampo.get(id).toDTO()
		} else {
			selectedRecord = new RegolaCampoDTO(valido:true)
		}
		listaTipiOggetto = WkfTipoOggetto.findAllByIterabile (true)?.toDTO()
		caricaListaAttori()
	}

	private void caricaListaAttori() {
		if (selectedRecord.tipoOggetto?.codice) {
			listaAttori = WkfAttore.findAllByTipoOggettoOrTipoOggettoIsNull (selectedRecord.tipoOggetto.domainObject)?.toDTO()
		}
		else {
			listaAttori = WkfAttore.findAll()?.toDTO()
		}

	}

	@NotifyChange(["selectedRecord", "listaAttori"])
	@Command onCambiaTipo() {
		caricaListaAttori()
	}

	/*
	 * Implementazione della classe AfcAbstractRecord
	 */

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		selectedRecord = regolaCampoService.salva(selectedRecord)
	}


	@Command onSalvaChiudi() {
		onSalva ()
		onChiudi ()
	}

	@Command onSettaValido(@BindingParam("valido") boolean valido) {
		Messagebox.show(Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTesto",[valido?"valido":"non valido"].toArray()), Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						selectedRecord.valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, RegolaCampoDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, RegolaCampoDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, RegolaCampoDettaglioViewModel.this, "datiModifica")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)
	}
}
