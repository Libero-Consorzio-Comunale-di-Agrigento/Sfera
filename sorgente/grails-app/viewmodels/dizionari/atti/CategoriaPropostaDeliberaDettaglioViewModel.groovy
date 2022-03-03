package dizionari.atti

import afc.AfcAbstractRecord
import it.finmatica.atti.dizionari.Categoria
import it.finmatica.atti.dizionari.TipoAllegato
import it.finmatica.atti.dto.dizionari.CategoriaDTO
import it.finmatica.atti.dto.dizionari.CategoriaDTOService
import it.finmatica.atti.dto.dizionari.MappingIntegrazioneDTOService
import it.finmatica.atti.dto.dizionari.TipoAllegatoDTO
import it.finmatica.atti.dto.impostazioni.MappingIntegrazioneDTO
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.documenti.AllegatiObbligatori
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class CategoriaPropostaDeliberaDettaglioViewModel extends AfcAbstractRecord {

	CategoriaDTO 	selectedRecord
	def 			listaAllegatiObbligatori

	// services
	CategoriaDTOService				categoriaDTOService
	MappingIntegrazioneDTOService 	mappingIntegrazioneDTOService


	@NotifyChange(["selectedRecord"])
    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w


		if (id != null) {
			selectedRecord = caricaCategoriaDto(id)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
			caricaAllegatiObbligatori()
		} else {
			selectedRecord = new CategoriaDTO(valido:true, sequenza:1, tipoOggetto:Categoria.TIPO_OGGETTO_PROPOSTA_DELIBERA)
		}
    }

	private CategoriaDTO caricaCategoriaDto (Long idCategoria){
		Categoria categoria = Categoria.createCriteria().get {
			eq("id", idCategoria)
		}
		return categoria.toDTO()
	}





	//Estendo i metodi abstract di AfcAbstractRecord

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		selectedRecord = categoriaDTOService.salva(selectedRecord)
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
						BindUtils.postNotifyChange(null, null, CategoriaPropostaDeliberaDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, CategoriaPropostaDeliberaDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, CategoriaPropostaDeliberaDettaglioViewModel.this, "datiModifica")
					} else if(Messagebox.ON_CANCEL.equals(e.getName())) {
						//Cancel is clicked
					}
				}
			}
		)
	}

	private void caricaAllegatiObbligatori (){
		def lista = MappingIntegrazione.findAllByCategoriaAndCodiceAndValoreInterno(AllegatiObbligatori.MAPPING_CATEGORIA, AllegatiObbligatori.MAPPING_CODICE_CATEGORIA, String.valueOf(selectedRecord.id)).toDTO()
		listaAllegatiObbligatori = TipoAllegato.findAllByIdInList(lista*.valoreEsterno).toDTO()
	}

	@Command onAggiungiAllegatoObbligatorio() {
		Window w = Executions.createComponents("/dizionari/atti/popupSceltaTipoAllegato.zul", self, [tipologia: 'DELIBERA', id: selectedRecord.id, tipoAllegato: null])
		w.onClose { event ->
			if (event.data != null) {
				TipoAllegatoDTO tipoAllegatoDTO = event.data;
				MappingIntegrazioneDTO integrazione = new MappingIntegrazioneDTO()
				integrazione.categoria = AllegatiObbligatori.MAPPING_CATEGORIA
				integrazione.codice = AllegatiObbligatori.MAPPING_CODICE_CATEGORIA
				integrazione.valoreInterno = selectedRecord.id.toString()
				integrazione.valoreEsterno = tipoAllegatoDTO.id.toString()
				mappingIntegrazioneDTOService.salva(integrazione)
				caricaAllegatiObbligatori()
				BindUtils.postNotifyChange(null, null, this, "listaAllegatiObbligatori")
				BindUtils.postNotifyChange(null, null, this, "selectedRecord")
			}
		}
		w.doModal()
	}

	@Command onEliminaAllegatoObbligatorio(@BindingParam("allegatoObbligatorio") TipoAllegatoDTO allegatoObbligatorio) {
		org.zkoss.zhtml.Messagebox.show("Vuoi cancellare il tipo allegato obbligatorio?", "Attenzione",
				org.zkoss.zhtml.Messagebox.OK | org.zkoss.zhtml.Messagebox.CANCEL, org.zkoss.zhtml.Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					public void onEvent(Event e) {
						if (org.zkoss.zhtml.Messagebox.ON_OK.equals(e.getName())) {
							MappingIntegrazioneDTO integrazione = MappingIntegrazione.findByCategoriaAndCodiceAndValoreInternoAndValoreEsterno(AllegatiObbligatori.MAPPING_CATEGORIA, AllegatiObbligatori.MAPPING_CODICE_CATEGORIA, selectedRecord.id.toString(), allegatoObbligatorio.id.toString()).toDTO()
							mappingIntegrazioneDTOService.elimina(integrazione)
							caricaAllegatiObbligatori()
							BindUtils.postNotifyChange(null, null, CategoriaPropostaDeliberaDettaglioViewModel.this, "listaAllegatiObbligatori")
							BindUtils.postNotifyChange(null, null, CategoriaPropostaDeliberaDettaglioViewModel.this, "selectedRecord")
						}
					}
				});
	}


}
