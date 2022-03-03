package dizionari.atti

import afc.AfcAbstractRecord
import it.finmatica.atti.dizionari.OggettoRicorrente
import it.finmatica.atti.dto.dizionari.OggettoRicorrenteDTO
import it.finmatica.atti.dto.dizionari.OggettoRicorrenteDTOService
import it.finmatica.atti.dto.impostazioni.MappingIntegrazioneDTO
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.l190.CasaDiVetroConfig
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class OggettoRicorrenteDettaglioViewModel extends AfcAbstractRecord {

	OggettoRicorrenteDTO 	selectedRecord
	boolean oggettiRicorrentiControllo
	def listaMappingIntegrazioni
	CasaDiVetroConfig casaDiVetroConfig

	// services
	OggettoRicorrenteDTOService	oggettoRicorrenteDTOService


	@NotifyChange(["selectedRecord"])
    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w

		oggettiRicorrentiControllo = Impostazioni.OGGETTI_RICORRENTI_CONTROLLO.abilitato

		if (id != null) {
			selectedRecord = caricaOggettoRicorrenteDto(id)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		} else {
			selectedRecord = new OggettoRicorrenteDTO(valido:true)
		}
    }

	private OggettoRicorrenteDTO caricaOggettoRicorrenteDto (Long idOggettoRicorrente){
		OggettoRicorrente oggettoRicorrente = OggettoRicorrente.createCriteria().get {
			eq("id", idOggettoRicorrente)
		}
		listaMappingIntegrazioni = MappingIntegrazione.findAllByCodiceAndValoreInterno(CasaDiVetroConfig.MAPPING_CODICE_OGGETTO_RICORRENTE, String.valueOf(idOggettoRicorrente)).toDTO()
		return oggettoRicorrente.toDTO()
	}





	//Estendo i metodi abstract di AfcAbstractRecord

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		selectedRecord = oggettoRicorrenteDTOService.salva(selectedRecord)
		aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
		aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalvaChiudi() {
		onSalva()
		onChiudi ()
	}


	@Command onSettaValido(@BindingParam("valido") boolean valido) {
		Messagebox.show(Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTesto",[valido?"valido":"non valido"].toArray()), Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						selectedRecord.valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, OggettoRicorrenteDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, OggettoRicorrenteDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, OggettoRicorrenteDettaglioViewModel.this, "datiModifica")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)
	}

	@Command onAggiungiMappingIntegrazioni() {
		Window w = Executions.createComponents("/dizionari/impostazioni/mappingIntegrazioniDettaglio.zul", self, [id: null, locked: true, integrazione: casaDiVetroConfig, tabSelected: 'Oggetto Ricorrente', valoreInterno:String.valueOf(selectedRecord.id)])
		w.onClose {
			caricaOggettoRicorrenteDto(selectedRecord.id)
			BindUtils.postNotifyChange(null, null, this, "listaMappingIntegrazioni")
			BindUtils.postNotifyChange(null, null, this, "selectedRecord")
		}
		w.doModal()
	}

	@Command onModificaMappingIntegrazioni(@BindingParam("mappingIntegrazione") MappingIntegrazioneDTO mappingIntegrazioneDto) {
		Window w = Executions.createComponents("/dizionari/impostazioni/mappingIntegrazioniDettaglio.zul", self, [id: mappingIntegrazioneDto.id, locked: true, integrazione: casaDiVetroConfig, tabSelected: 'Oggetto Ricorrente', valoreInterno:String.valueOf(selectedRecord.id)])
		w.onClose {
			caricaOggettoRicorrenteDto(selectedRecord.id)
			BindUtils.postNotifyChange(null, null, this, "listaMappingIntegrazioni")
			BindUtils.postNotifyChange(null, null, this, "selectedRecord")
		}
		w.doModal()
	}

}
