package dizionari.atti

import afc.AfcAbstractRecord
import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.documenti.tipologie.TipoDelibera
import it.finmatica.atti.documenti.tipologie.TipoDetermina
import it.finmatica.atti.documenti.tipologie.TipoVistoParere
import it.finmatica.atti.dto.documenti.tipologie.TipoVistoParereDTO
import it.finmatica.atti.dto.documenti.tipologie.TipoVistoParereDTOService
import it.finmatica.atti.dto.impostazioni.CaratteristicaTipologiaDTO
import it.finmatica.atti.impostazioni.CaratteristicaTipologia
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.dto.configuratore.iter.WkfCfgIterDTO
import it.finmatica.gestionetesti.ui.dizionari.GestioneTestiModelloDTOService
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import it.finmatica.zkutils.SuccessHandler
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class TipoVistoParereDettaglioViewModel extends AfcAbstractRecord {

	// services
	SpringSecurityService          springSecurityService
	TipoVistoParereDTOService      tipoVistoParereDTOService
	GestioneTestiModelloDTOService gestioneTestiModelloDTOService
	SuccessHandler                 successHandler

	// componenti

	// dati
	TipoVistoParereDTO 	selectedRecord
	List<WkfCfgIterDTO> listaCfgIter
	List<CaratteristicaTipologiaDTO> listaCaratteristiche
	List<So4UnitaPubbDTO> listaUnita
	def listaModelliTesto

	// stato

	@NotifyChange("selectedRecord")
    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") long id) {
		this.self = w
		if (id > 0) {
			selectedRecord = TipoVistoParere.get(id).toDTO()
		} else {
			selectedRecord = new TipoVistoParereDTO(id: -1, valido:true)
		}

		listaCaratteristiche 	= CaratteristicaTipologia.findAllByTipoOggettoAndValido(WkfTipoOggetto.get(VistoParere.TIPO_OGGETTO), true, [sort: "titolo", order: "asc"]).toDTO()
		listaCfgIter 			= WkfCfgIter.iterValidi.findAllByTipoOggetto(WkfTipoOggetto.get(VistoParere.TIPO_OGGETTO), [sort: "nome", order: "asc"]).toDTO()
		listaModelliTesto 		= gestioneTestiModelloDTOService.getListaModelli([VistoParere.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO_PARERE, "VISTO_CONTABILE_MODENA"])

		aggiornaUnitaDestinatarie ()
    }

	/*
	 *  Gestione unità destinatarie
	 */

	private void aggiornaUnitaDestinatarie () {
		def progr = selectedRecord.getListaUnitaDestinatarie()

		listaUnita = []
		if (progr.size() > 0) {
			listaUnita = So4UnitaPubb.allaData(new Date()).perOttica(springSecurityService.principal.ottica().codice) {
				'in' ("progr", progr)
			}.toDTO()
		}

		BindUtils.postNotifyChange(null, null, this, "listaUnita")
	}

	@Command onSceltaUnita () {
		Window w = Executions.createComponents("/commons/popupUnitaOrganizzativa.zul", self, null)
		w.onClose { event ->
			if (event.data?.unita != null) {
				selectedRecord.addUnitaDestinataria(event.data.unita.progr)
				aggiornaUnitaDestinatarie()
				BindUtils.postNotifyChange(null, null, this, "selectedRecord")
			}
		}
		w.doModal()
	}

	@Command onRimuoviUnitaDestinataria (@ContextParam(ContextType.TRIGGER_EVENT) Event event, @BindingParam("unita") So4UnitaPubbDTO unita) {
		Messagebox.show("Eliminare l'unità selezionata dalla gestione del visto?", "Attenzione!",
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				public void onEvent (Event e) {
					if (Messagebox.ON_OK.equals(e.getName())) {
						selectedRecord.removeUnitaDestinataria(unita.progr)
						aggiornaUnitaDestinatarie()
						BindUtils.postNotifyChange(null, null, TipoVistoParereDettaglioViewModel.this, "selectedRecord")
					}
				}
			}
		)
	}

	/*
	 * Implementazione dei metodi per AfcAbstractRecord
	 */

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		boolean isNuovoTipoDetermina = (selectedRecord.id == null)
		selectedRecord = tipoVistoParereDTOService.salva (selectedRecord)
		if (isNuovoTipoDetermina) {
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
		}
		aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		successHandler.showMessage("Tipo visto/parere salvato")
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalvaChiudi() {
		onSalva()
		onChiudi()
	}

	@Command onSettaValido(@BindingParam("valido") boolean valido) {
		// se voglio disattivare la tipologia di visto, prima verifico che non sia usato da nessuna tipologia di determina/delibera ancora valida.
		if (selectedRecord.valido && valido == false) {
			def tipologie = [];
			tipologie.addAll(TipoDetermina.inUsoPerTipoVisto(selectedRecord.id).list())
			tipologie.addAll(TipoDelibera.inUsoPerTipoVisto(selectedRecord.id).list())

			if (tipologie.size() > 0) {
				Clients.showNotification ("Non è possibile disattivare la tipologia di visto/parere perché è usata da altre tipologie ancora attive:\n" +
										  tipologie.collect { (it instanceof TipoDetermina)?"Tipologia di Determina \"${it.titolo}\"":"Tipologia di Delibera \"${it.titolo}\"" }.join("\n"), Clients.NOTIFICATION_TYPE_WARNING, self, "before_center", tipologie.size()*3000, true);
				return;
			}
		}

		Messagebox.show(Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTesto",[valido?"valido":"non valido"].toArray()), Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTitolo"),
			Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			new org.zkoss.zk.ui.event.EventListener() {
				@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
				public void onEvent(Event e){
					if(Messagebox.ON_OK.equals(e.getName())) {
						TipoVistoParereDettaglioViewModel.this.selectedRecord.valido = valido
						onSalva()
						BindUtils.postNotifyChange(null, null, TipoVistoParereDettaglioViewModel.this, "selectedRecord")
						BindUtils.postNotifyChange(null, null, TipoVistoParereDettaglioViewModel.this, "datiCreazione")
						BindUtils.postNotifyChange(null, null, TipoVistoParereDettaglioViewModel.this, "datiModifica")
					}
				}
			}
		)
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onDuplica () {
		selectedRecord = tipoVistoParereDTOService.duplica(selectedRecord);
		Clients.showNotification("Tipologia duplicata.", Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 3000, true)
	}
}
