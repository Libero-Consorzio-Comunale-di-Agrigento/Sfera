package odg.seduta

import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.DeliberaService
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.dto.documenti.DeliberaDTO
import it.finmatica.atti.dto.documenti.PropostaDeliberaDTO
import it.finmatica.atti.dto.odg.SedutaDTO
import it.finmatica.atti.dto.odg.SedutaDTOService
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.odg.OggettoPartecipante
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import org.apache.commons.lang.StringUtils
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zul.Label
import org.zkoss.zul.Listbox
import org.zkoss.zul.Messagebox
import org.zkoss.zul.Window

class OdgTestiSedutaViewModel {

 	// service
	DeliberaService deliberaService
	SedutaDTOService sedutaDTOService
	String ordinamento

	// componenti
	Window self

	// dati
	SedutaDTO			seduta
	DeliberaDTO			selectedDelibera
	List<DeliberaDTO>	listaDelibere

	// stato
	boolean attivaDelibera		= false
	boolean abilitaCreaTesto 	= false

	int delibereAttivate 	= 0
	int delibereDaAttivare 	= 0

    @Init init(@ContextParam(ContextType.COMPONENT) Window w, @ExecutionArgParam("seduta") SedutaDTO seduta)  {
        this.self = w
		this.seduta = seduta
		ordinamento = "ANNO_NUMERO"
		caricaListaDelibere ()
	}

	@GlobalCommand
	public void onRefreshTesti (Event e) {
		caricaListaDelibere ();
	}

	private void caricaListaDelibere () {
		listaDelibere = Delibera.createCriteria().list() {
			oggettoSeduta {
				eq ("seduta.id", seduta.id)

				if (!ordinamento?.equals("ANNO_NUMERO")){
					order("sequenzaDiscussione")
				}
			}
			if (ordinamento?.equals("ANNO_NUMERO")){
				order("annoDelibera", "asc")
				order("numeroDelibera", "asc")
			}


		}?.toDTO(["propostaDelibera.tipologia", "oggettoSeduta.delega.assessore", "oggettoSeduta.esito.esitoStandard", "registroDelibera"]) // è più veloce così piuttosto che fare le fetch

		// concateno i firmatari previsti
		for (DeliberaDTO deliberaDto in listaDelibere) {
			List<OggettoPartecipante> firmatari = deliberaService.getListaFirmatariOggetto(deliberaDto.oggettoSeduta.id)

			def elencoFirmatariOggetto = [];
			for (OggettoPartecipante f : firmatari) {
				elencoFirmatariOggetto.add(f.sequenzaFirma+ ". "+ (f.sedutaPartecipante.componenteEsterno?.denominazione?:f.sedutaPartecipante.commissioneComponente?.componente?.denominazione))
			}

			deliberaDto.firmatariOggetto = StringUtils.join(elencoFirmatariOggetto, "\n")
		}
		calcolaNumeroDelibere()

		BindUtils.postNotifyChange(null,null, this, "listaDelibere")
    }

	@NotifyChange(["attivaDelibera"])
	@Command settaSelezione (@BindingParam("lista") Listbox lista) {
		
		// il pulsante "crea testo" deve essere cliccabile se tutte le delibere selezionate sono senza testo:
		abilitaCreaTesto = (lista.getSelectedItems().size() > 0);
		for (DeliberaDTO delibera : lista.getSelectedItems().value) {
			// alla prima delibera che trovo con il testo, disabilito il pulsante "crea testo".
			if (delibera.testo != null) {
				abilitaCreaTesto = false;
				break;
			}
		}
		
		// il pulsante "Attiva Delibera" deve essere cliccabile se:
		// * tutte le delibere selezionate hanno il testo
		// * almeno una delle delibere selezionate non è ancora attivata.
		
		attivaDelibera = true
		for (DeliberaDTO delibera : lista.getSelectedItems().value) {
			// alla prima delibera che trovo senza testo, disabilito il pulsante attivaDelibera.
			if (delibera.testo == null) {
				attivaDelibera = false
				return;	
			}
		}
		
		attivaDelibera = false
		for (DeliberaDTO delibera : lista.getSelectedItems().value) {
			// alla prima delibera che trovo da attivare, metto true:
			if (delibera.iter == null) {
				attivaDelibera = true
				return;
			}
		}
	}

	@Command onLinkProposta (@BindingParam("proposta") PropostaDeliberaDTO proposta) {
		Window w = Executions.createComponents("/atti/documenti/propostaDelibera.zul", self, [id : proposta.id])
		w.onClose {
			caricaListaDelibere()
		}
		w.doModal()
	}

	@Command onLinkDelibera (@BindingParam("delibera") DeliberaDTO delibera) {
		Window w = Executions.createComponents("/atti/documenti/delibera.zul", self, [id : delibera.id])
		w.onClose {
			caricaListaDelibere()
		}
		w.doModal()
	}

	@Command onGeneraTesto(@BindingParam("lista") Listbox lista) {
		Window w = Executions.createComponents("/commons/popupModelloDelibera.zul", self, [idCommissione : seduta.commissione.id])
		w.onClose { event ->
			if (event.data.esito == "OK") {
				GestioneTestiModelloDTO gestioneTestiModelloDto = event.data.modello;
				for (def deliberaDto : lista.getSelectedItems()*.value) {
					if (gestioneTestiModelloDto.id == -1 && deliberaDto.domainObject.tipologiaDocumento?.modelloTestoDelibera != null) {
						gestioneTestiModelloDto = deliberaDto.domainObject.tipologiaDocumento?.modelloTestoDelibera.toDTO()
					}
					sedutaDTOService.generaTestoDelibera (deliberaDto, gestioneTestiModelloDto);
				}
				caricaListaDelibere();
			}
		}
		w.doModal()
	}

	@Command onAttivaDelibere(@BindingParam("lista") Listbox lista) {
		for (DeliberaDTO delibera : lista.getSelectedItems()*.value) {
			
			Delibera deli = delibera.domainObject;
			
			// se una delibera non ha il testo, do' errore:
			if (deli.testo == null) {
				throw new AttiRuntimeException ("Non è possibile attivare l'atto n. ${delibera.numeroDelibera} / ${delibera.annoDelibera} perché non ha il Testo.")
			}
			if (deliberaService.numeroFirmatariDaVerificare(deli)){
				Messagebox.show("Attenzione: il numero di firmatari previsto dalla commissione è diverso dal numero dei firmatari specificato.", "Vuoi continuare?", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
					new org.zkoss.zk.ui.event.EventListener() {
						public void onEvent(Event e) {
							if (Messagebox.ON_OK.equals(e.getName())) {
								attivaDelibera(deli.toDTO())
							}
						}
					}
				);

			}
			else {
				attivaDelibera(deli.toDTO())
			}
		}
	}

	@Command getUnita(@ContextParam(ContextType.COMPONENT) Label lc, @BindingParam("proposta")  PropostaDeliberaDTO proposta ) {
		lc.value = "Unità Proponente: "+(PropostaDelibera.get(proposta.id).getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.descrizione?:"")
	}

	@NotifyChange(["delibereAttivate", "delibereDaAttivare"])
	private void calcolaNumeroDelibere() {
		int attive=0, nonAttive=0;

		for (DeliberaDTO d : listaDelibere) {
			if (d.iter != null)
				attive++;
			else
				nonAttive++;
		}

		delibereAttivate = attive
		delibereDaAttivare = nonAttive

		BindUtils.postNotifyChange(null, null, this, "delibereAttivate")
		BindUtils.postNotifyChange(null, null, this, "delibereDaAttivare")
	}

	private void attivaDelibera(DeliberaDTO deliberaDTO){
		deliberaService.attivaDelibera(deliberaDTO.domainObject)
		caricaListaDelibere();
	}

	@NotifyChange(["listaDelibere"])
	@Command onCambiaOrdinamento () {
		caricaListaDelibere()
	}

}
