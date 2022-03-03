package dizionari.atti

import afc.AfcAbstractRecord
import it.finmatica.atti.dizionari.TipoAllegato
import it.finmatica.atti.dto.dizionari.TipoAllegatoDTO
import it.finmatica.atti.dto.dizionari.TipoAllegatoDTOService
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.documenti.AllegatiObbligatori
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class TipoAllegatoDettaglioViewModel extends AfcAbstractRecord {

	TipoAllegatoDTO selectedRecord

	// services
	TipoAllegatoDTOService tipoAllegatoDTOService
	List<GestioneTestiModelloDTO> listaModelli

	@NotifyChange(["selectedRecord"])
	@Init
	void init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w

		if (id != null) {
			selectedRecord = caricaTipoAllegatoDto(id)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		} else {
			selectedRecord = new TipoAllegatoDTO(valido: true, statoFirma: Impostazioni.ALLEGATO_STATO_FIRMA_DEFAULT.valore)
		}

		caricaListaModelli()
	}

	private TipoAllegatoDTO caricaTipoAllegatoDto(Long idTipoAllegato) {
		return TipoAllegato.get(idTipoAllegato).toDTO()
	}

	// Estendo i metodi abstract di AfcAbstractRecord
	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command
	def onSalva() {
		selectedRecord = tipoAllegatoDTOService.salva(selectedRecord)
		aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
		aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command
	def onSalvaChiudi() {
		onSalva()
		onChiudi()
	}

	@Command
	def onSettaValido(@BindingParam("valido") boolean valido) {
		if (!valido && MappingIntegrazione.countByCategoriaAndValoreEsterno(AllegatiObbligatori.MAPPING_CATEGORIA, selectedRecord.id.toString()) > 0) {
			throw new AttiRuntimeException("Tipo Allegato utilizzato come allegato obbligatorio da una Tipologia o da una Categoria, operazione annullata!")
		}
		Messagebox.show(Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTesto", [valido ? "valido" : "non valido"].toArray()),
				Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTitolo"),
				Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
					public void onEvent(Event e) {
						if (Messagebox.ON_OK.equals(e.getName())) {
							selectedRecord.valido = valido
							onSalva()
							BindUtils.postNotifyChange(null, null, TipoAllegatoDettaglioViewModel.this, "selectedRecord")
							BindUtils.postNotifyChange(null, null, TipoAllegatoDettaglioViewModel.this, "datiCreazione")
							BindUtils.postNotifyChange(null, null, TipoAllegatoDettaglioViewModel.this, "datiModifica")
						}
					}
				}
		)
	}

	@NotifyChange(["selectedRecord", "listaModelli"])
	@Command
	void onCambiaCodice() {
		selectedRecord.modelloTesto = null
		caricaListaModelli()
	}

	@NotifyChange(["selectedRecord", "listaModelli"])
	@Command
	void onCambiaTipologia() {
		selectedRecord.codice = null
		selectedRecord.modelloTesto = null
		listaModelli = []
	}

	void caricaListaModelli() {
		listaModelli = []
		listaModelli = GestioneTestiModello.createCriteria().list {
			eq("valido", true)
			tipoModello {
				like("codice", selectedRecord.tipologia + "%")
			}
			fetchMode("tipoModello", FetchMode.JOIN)
		}.toDTO()
	}
}