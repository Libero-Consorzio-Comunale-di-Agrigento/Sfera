package dizionari.odg

import afc.AfcAbstractRecord
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.atti.dto.odg.dizionari.EsitoDTO
import it.finmatica.atti.dto.odg.dizionari.EsitoDTOService
import it.finmatica.atti.dto.odg.dizionari.EsitoStandardDTO
import it.finmatica.atti.odg.Commissione
import it.finmatica.atti.odg.dizionari.Esito
import it.finmatica.atti.odg.dizionari.EsitoStandard
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.dto.configuratore.iter.WkfCfgIterDTO
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class EsitoDettaglioViewModel extends AfcAbstractRecord {

    // service
	EsitoDTOService esitoDTOService

	// dati

	List<EsitoStandardDTO> 	listaEsitoStandard
	List<CommissioneDTO>	listaCommissionePartenza
	List<CommissioneDTO>	listaCommissioneArrivo
	List<WkfCfgIterDTO>		listaIter
	def listaTipiRegistro

	// stato
	boolean	inModifica   = false
	boolean	commArrivo   = false
	boolean	creaDelibera = false

	@NotifyChange(["selectedRecord",  "listaEsitoStandard", "listaCommissione", "totalSize"])
	@Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w

		if (id != null) {
			selectedRecord = caricaEsitoDto(id)

			commArrivo   = (selectedRecord.esitoStandard.titolo == "Invia a Commissione")
			creaDelibera = (selectedRecord.esitoStandard.creaDelibera)

			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica (selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		} else {
			selectedRecord = new EsitoDTO(valido:true, sequenza:1)
		}

		listaEsitoStandard 			= EsitoStandard.list().toDTO()
		listaCommissioneArrivo 		= Commissione.list().toDTO()
		listaCommissionePartenza 	= Commissione.list().toDTO()
		listaIter = [new WkfCfgIterDTO(nome:"-- usa iter scritto in commissione --", progressivo:-1)] + WkfCfgIter.iterValidi.findAllByTipoOggetto(WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), [sort: "nome", order: "asc"]).toDTO()
		listaTipiRegistro 			= [new TipoRegistroDTO(codice:"", descrizione:"-- usa registro specificato in tipologia --")] + TipoRegistro.findAllByValido(true, [sort:"descrizione", order:"asc"]).toDTO()
	}

	private EsitoDTO caricaEsitoDto (Long idEsito){
		Esito esito = Esito.createCriteria().get {
			eq("id", idEsito)
			
			fetchMode("esitoStandard", 		FetchMode.JOIN)
			fetchMode("commissione", 		FetchMode.JOIN)
			fetchMode("commissioneArrivo", 	FetchMode.JOIN)
			fetchMode("utenteIns", 			FetchMode.JOIN)
			fetchMode("utenteUpd", 			FetchMode.JOIN)
		}
		return esito.toDTO()
	}

	//////////////////////////////////////////
	//				SALVATAGGIO				//
	//////////////////////////////////////////

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		boolean isNuovoEsito = (selectedRecord.id == null)
		def idEsito = esitoDTOService.salva(selectedRecord).id
		selectedRecord = caricaEsitoDto(idEsito)
		if (isNuovoEsito){
		  aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
		}
		aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalvaChiudi() {
		onSalva()
		onChiudi()
	}

	@NotifyChange(["commArrivo", "creaDelibera"])
	@Command onCheckEsitoStandard() {
		if (selectedRecord.esitoStandard?.titolo == "Invia a Commissione") {
			commArrivo = true
		} else {
			commArrivo = false
			selectedRecord.commissioneArrivo = null
		}

		creaDelibera = selectedRecord.esitoStandard?.creaDelibera?:false;
	}

	@Command onSettaValido(@BindingParam("valido") boolean valido) {
		Messagebox.show("Modificare la validità dell'esito?", "Modifica validità",
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						super.getSelectedRecord().valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, EsitoDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, EsitoDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, EsitoDettaglioViewModel.this, "datiModifica")
					}
				}
			}
		)
	}

}
