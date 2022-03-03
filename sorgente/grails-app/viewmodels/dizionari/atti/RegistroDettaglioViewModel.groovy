package dizionari.atti

import afc.AfcAbstractRecord
import it.finmatica.atti.dizionari.Registro
import it.finmatica.atti.dto.dizionari.RegistroDTO
import it.finmatica.atti.dto.dizionari.RegistroDTOService
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class RegistroDettaglioViewModel extends AfcAbstractRecord {

	RegistroDTO 	selectedRecord

	// services
	RegistroDTOService	registroDTOService


	@NotifyChange(["selectedRecord"])
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("tipoRegistro") TipoRegistroDTO tipoRegistro , @ExecutionArgParam("id") Long id) {
		this.self = w
		if (id != null) {
			selectedRecord = caricaRegistroDto(id)
		} else {

			int annoAttuale = ((Registro.createCriteria().get {
				projections {
					max ("anno")
				}
				eq("tipoRegistro.codice", tipoRegistro.codice)
				eq("valido", true)
			}?:(Calendar.getInstance().get(Calendar.YEAR)-1)) + 1)
			selectedRecord = new RegistroDTO(valido:true, anno: annoAttuale, ultimoNumero: 0)
			selectedRecord.tipoRegistro = tipoRegistro
		}
	}

	private RegistroDTO caricaRegistroDto (Long idRegistro) {
		Registro registro = Registro.createCriteria().get {
			eq("id", idRegistro)
			fetchMode("tipoRegistro", FetchMode.JOIN)
		}
		return registro.toDTO()
	}


	/*
	 * Implementazione della classe AfcAbstractRecord
	 */

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		if (Registro.createCriteria().get {
			eq("anno", selectedRecord.anno)
			eq("tipoRegistro.codice", selectedRecord.tipoRegistro.codice)
			eq("valido", true)
		} != null) {
			Messagebox.show ("Non è possibile inserire il nuovo registro: esiste già un registro attivo per l'anno ${selectedRecord.anno}, selezionare un anno diverso.");
			return;
		}

		selectedRecord = registroDTOService.salva(selectedRecord)
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
						BindUtils.postNotifyChange(null, null, RegistroDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, RegistroDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, RegistroDettaglioViewModel.this, "datiModifica")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)
	}
}
