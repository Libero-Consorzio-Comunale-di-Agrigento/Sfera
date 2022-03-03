package dizionari.atti

import afc.AfcAbstractRecord
import it.finmatica.atti.dizionari.RegistroUnita
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.dto.dizionari.RegistroUnitaDTO
import it.finmatica.atti.dto.dizionari.RegistroUnitaDTOService
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.impostazioni.CaratteristicaTipologiaDTO
import it.finmatica.atti.impostazioni.CaratteristicaTipologia
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO
import org.hibernate.FetchMode
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class RegistroUnitaDettaglioViewModel extends AfcAbstractRecord {

	// services
	RegistroUnitaDTOService	registroUnitaDTOService
	def so4UnitaPubbService
	def springSecurityService

	// dati
	RegistroUnitaDTO 	  selectedRecord
	List<TipoRegistroDTO> listaTipiRegistroDto
	List<So4UnitaPubbDTO> listaSo4UnitaPubDto
	List<CaratteristicaTipologiaDTO> listaCaratteristiche

	@NotifyChange(["selectedRecord"])
    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("id") Long id) {
		this.self = w

		if (id != null) {
			selectedRecord = caricaRegistroUnitaDto(id)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica (selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		} else {
			selectedRecord = new RegistroUnitaDTO(valido:true)
		}
		listaTipiRegistroDto = caricaListaTipiRegistro();
		listaSo4UnitaPubDto  = caricaListaUnitaSo4();
		listaCaratteristiche = caricaCaratteristiche();
    }

	private List<CaratteristicaTipologiaDTO> caricaCaratteristiche () {
		return [new CaratteristicaTipologiaDTO(titolo:"-- valido per tutte le caratteristiche --")] + CaratteristicaTipologia.findAllByTipoOggettoInList([WkfTipoOggetto.get(Determina.TIPO_OGGETTO), WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO)]).toDTO();
	}

	private RegistroUnitaDTO caricaRegistroUnitaDto (Long idRegistroUnita) {
		RegistroUnita regUnita = RegistroUnita.createCriteria().get {
			eq("id", idRegistroUnita)
			fetchMode("tipoRegistro", FetchMode.JOIN)
			fetchMode("unitaSo4", FetchMode.JOIN)
		}
		return regUnita.toDTO()
	}

	private List<TipoRegistroDTO> caricaListaTipiRegistro() {
		List<TipoRegistro> lista = TipoRegistro.createCriteria().list{
			eq("valido", true)
			order("descrizione", "asc")
		}
		List<TipoRegistroDTO> listaDto = new ArrayList<TipoRegistroDTO>()
		listaDto.addAll(lista.toDTO())
		if (selectedRecord.tipoRegistro != null) {
			boolean presente = false
			for(i in listaDto) {
				if (i.codice == selectedRecord.tipoRegistro.codice) {
					presente = true
					break
				}
			}
			if (presente == false)
				listaDto.add(selectedRecord.tipoRegistro)
		}

		return listaDto
	}

	private List<So4UnitaPubbDTO> caricaListaUnitaSo4() {
		String ente = springSecurityService.principal.amm().codice
		String ottica = springSecurityService.principal.ottica().codice
		List<So4UnitaPubbDTO> listaDto = so4UnitaPubbService.cercaUnitaPubb(ente, ottica, new Date()).toDTO()
		if (selectedRecord.unitaSo4 != null) {
			// controllo che unitaSo4 non sia già presente nella lista
			boolean presente = false
			for(i in listaDto) {
				if (i.progr == selectedRecord.unitaSo4.progr) {
					presente = true
					break
				}
			}
			if (presente == false) {
				// vado ad aggiungere alla lista estratta anche la so4UnitaPubbService che è assegnata
				listaDto.add(selectedRecord.unitaSo4)
			}
		}
		return listaDto
	}

	private boolean controllaCorrettezzaAssociazione() {
		if (registroUnitaDTOService.controllaTipoRegistroPerUnita(selectedRecord) == false)	{
			Clients.showNotification(Labels.getLabel("dizionario.soloUnTipoRegistroValidoPerUnitaOrganizzativa"), Clients.NOTIFICATION_TYPE_ERROR, null, "before_center", 3000, true);
			return false;
		}

		return true;
	}

	// Estendo i metodi abstract di AfcAbstractRecord

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalva () {
		if (controllaCorrettezzaAssociazione()) {
			selectedRecord = registroUnitaDTOService.salva(selectedRecord)
			aggiornaDatiCreazione(selectedRecord.utenteIns.id, selectedRecord.dateCreated)
			aggiornaDatiModifica(selectedRecord.utenteUpd.id, selectedRecord.lastUpdated)
		}
	}

	@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
	@Command onSalvaChiudi() {
		if (controllaCorrettezzaAssociazione()) {
			onSalva()
			onChiudi ()
		}
	}

	@Command onSettaValido(@BindingParam("valido") boolean valido) {
		if (controllaCorrettezzaAssociazione()) {
			Messagebox.show(Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTesto",[valido?"valido":"non valido"].toArray()), Labels.getLabel("dizionario.cambiaValiditaRecordMessageBoxTitolo"),
				Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
				new org.zkoss.zk.ui.event.EventListener() {
					@NotifyChange(["selectedRecord", "datiCreazione", "datiModifica"])
					public void onEvent(Event e) {
						if (Messagebox.ON_OK.equals(e.getName())) {
							selectedRecord.valido = valido
							onSalva()
							BindUtils.postNotifyChange(null, null, RegistroUnitaDettaglioViewModel.this, "selectedRecord")
							BindUtils.postNotifyChange(null, null, RegistroUnitaDettaglioViewModel.this, "datiCreazione")
							BindUtils.postNotifyChange(null, null, RegistroUnitaDettaglioViewModel.this, "datiModifica")
						}
					}
				}
			)
		}
	}
}
