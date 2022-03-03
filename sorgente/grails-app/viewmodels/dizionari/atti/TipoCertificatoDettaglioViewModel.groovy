package dizionari.atti

import afc.AfcAbstractRecord
import it.finmatica.atti.documenti.Certificato
import it.finmatica.atti.documenti.tipologie.TipoCertificato
import it.finmatica.atti.documenti.tipologie.TipoDelibera
import it.finmatica.atti.documenti.tipologie.TipoDetermina
import it.finmatica.atti.dto.dizionari.TipoCertificatoDTOService
import it.finmatica.atti.dto.documenti.tipologie.TipoCertificatoDTO
import it.finmatica.atti.impostazioni.CaratteristicaTipologia
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.dto.configuratore.iter.WkfCfgIterDTO
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.zkutils.SuccessHandler
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class TipoCertificatoDettaglioViewModel extends AfcAbstractRecord {

	// services
	TipoCertificatoDTOService 		tipoCertificatoDTOService
	SuccessHandler 					successHandler

	TipoCertificatoDTO 	selectedRecord
	List<WkfCfgIterDTO> listaCfgIter
	def listaFirmatari
	List<GestioneTestiModelloDTO> listaModelliTesto
	def listaCaratteristicaTipologia

	@NotifyChange(["selectedRecord"])
    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w

		if (id != null) {
			selectedRecord = caricaTipoCertificatoDto(id)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		} else {
			selectedRecord = new TipoCertificatoDTO(valido:true)
		}

		listaCfgIter 	 = WkfCfgIter.iterValidi.findAllByTipoOggettoAndStatoAndVerificato(WkfTipoOggetto.get(Certificato.TIPO_OGGETTO), WkfCfgIter.STATO_IN_USO, true, [sort: "nome", order: "asc"]).toDTO()

		leggiListaModelloTesto ()
		leggiListaCaratteristicaTipologia ()
    }

	private TipoCertificatoDTO caricaTipoCertificatoDto (Long idTipoCertificato){
		TipoCertificato tipoCertificato = TipoCertificato.createCriteria().get {
			eq("id", idTipoCertificato)
		}
		return tipoCertificato.toDTO()
	}

	// Estendo i metodi abstract di AfcAbstractRecord

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		selectedRecord = tipoCertificatoDTOService.salva(selectedRecord)
		aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
		aggiornaDatiModifica (selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		successHandler.showMessage("Tipo certificato salvato")
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalvaChiudi() {
		onSalva()
		onChiudi ()
	}

	@Command onSettaValido(@BindingParam("valido") boolean valido) {
		// se voglio disattivare la tipologia di certificato, prima verifico che non sia usato da nessuna tipologia di determina/delibera ancora valida.
		if (selectedRecord.valido && valido == false) {
    		def tipologie = [];
    		tipologie.addAll(TipoDetermina.inUsoPerTipoCertificato(selectedRecord.id).list())
    		tipologie.addAll(TipoDelibera.inUsoPerTipoCertificato(selectedRecord.id).list())

    		if (tipologie.size() > 0) {
    			Clients.showNotification ("Non è possibile disattivare la tipologia di certificato perché è usata da altre tipologie ancora attive:\n" +
										  tipologie.collect { (it instanceof TipoDetermina)?"Tipologia di Determina \"${it.titolo}\"":"Tipologia di Delibera \"${it.titolo}\"" }.join("\n"), Clients.NOTIFICATION_TYPE_WARNING, self, "before_center", tipologie.size()*3000, true);
    			return;
    		}
		}

		Messagebox.show(Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTesto", [valido?"valido":"non valido"].toArray()), Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
				public void onEvent(Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						TipoCertificatoDettaglioViewModel.this.selectedRecord.valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, TipoCertificatoDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, TipoCertificatoDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, TipoCertificatoDettaglioViewModel.this, "datiModifica")
					}
				}
			}
		)
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onDuplica () {
		selectedRecord = tipoCertificatoDTOService.duplica(selectedRecord);
		successHandler.showMessage("Tipologia duplicata")
	}

	private void leggiListaModelloTesto () {
		listaModelliTesto = GestioneTestiModello.createCriteria().list() {
		  or {
			  eq ("tipoModello.codice", TipoCertificato.CERT_DETE)
			  eq ("tipoModello.codice", TipoCertificato.CERT_DELI)
		  }

		}.toDTO()
		BindUtils.postNotifyChange(null, null, this, "listaModelliTesto")
	}

	private void leggiListaCaratteristicaTipologia () {
		listaCaratteristicaTipologia = CaratteristicaTipologia.createCriteria().list() {
			eq("tipoOggetto.codice", Certificato.TIPO_OGGETTO)
		}.toDTO()
	}
}
