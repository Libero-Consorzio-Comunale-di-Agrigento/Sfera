package odg.seduta

import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.dto.odg.*
import it.finmatica.atti.dto.odg.dizionari.IncaricoDTO
import it.finmatica.atti.dto.odg.dizionari.RuoloPartecipanteDTO
import it.finmatica.atti.odg.OggettoPartecipante
import it.finmatica.atti.odg.SedutaPartecipante
import it.finmatica.atti.odg.dizionari.Incarico
import it.finmatica.atti.odg.dizionari.RuoloPartecipante
import org.apache.commons.lang.StringUtils
import org.hibernate.criterion.CriteriaSpecification
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.Executions
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Window

class PopupPartecipantiViewModel {

	private static final String SEZIONE_CONVOCATI 		= "CONVOCATI";
	private static final String SEZIONE_PARTECIPANTI 	= "PARTECIPANTI";
	private static final String SEZIONE_OGGETTO_SEDUTA 	= "OGGETTO_SEDUTA";

	// componenti
	Window self

	// services
	ConvocatiSedutaDTOService convocatiSedutaDTOService
	OggettoPartecipanteDTOService oggettoPartecipanteDTOService

	// dati
	def partecipante
	As4SoggettoCorrenteDTO		soggetto
	IncaricoDTO					incarico
	List<IncaricoDTO>			listaIncarichi
	List<RuoloPartecipanteDTO>	listaRuoli

	// stato
	boolean ricercaInterna 	= true
	String sezione; // può essere "convocato", "partecipante", "proposta"

	/*
	 * Questa popup può funzionare per i tre tipi di partecipante:
	 * - convocato
	 * - partecipante alla seduta
	 * - partecipante del singolo oggetto seduta
	 *
	 * I primi due corrispondono alla stessa classe: SedutaPartecipante
	 * Il terzo invece è OggettoPartecipante
	 *
	 * La differenza sostanziale è che nei primi due è presente l'incarico, nel terzo invece è solo questione di ruolo e sequenza.
	 *
	 */
	@Init init (@ContextParam(ContextType.COMPONENT) Window w
			  , @ExecutionArgParam("id") long idPartecipante
			  , @ExecutionArgParam("seduta") SedutaDTO seduta
			  , @ExecutionArgParam("oggettoSeduta") OggettoSedutaDTO oggettoSeduta
			  , @ExecutionArgParam("sezione")String sezione)  {
		this.self = w
		this.sezione = sezione;

		if (sezione == SEZIONE_CONVOCATI) {
			if (idPartecipante > 0) {
				this.partecipante = SedutaPartecipante.get(idPartecipante).toDTO(["componenteEsterno", "commissioneComponente.componente"]);
				this.incarico = this.partecipante?.incarico;
				this.soggetto = this.partecipante?.componenteEsterno?:this.partecipante.commissioneComponente.componente;
			} else {
				this.partecipante = new SedutaPartecipanteDTO(id:-1, convocato:true, seduta:seduta, presente:false);
			}
        } else if (sezione == SEZIONE_PARTECIPANTI) {
            if (idPartecipante > 0) {
                this.partecipante = SedutaPartecipante.get(idPartecipante).toDTO(["componenteEsterno", "commissioneComponente.componente", "commissioneComponente.incarico"]);
                this.incarico = this.partecipante?.incarico ?: this.partecipante?.commissioneComponente?.incarico;
                this.soggetto = this.partecipante?.componenteEsterno?:this.partecipante.commissioneComponente.componente;
            } else {
                this.partecipante = new SedutaPartecipanteDTO(id:-1, convocato:false, seduta:seduta, presente:true);
            }
        } else if (sezione == SEZIONE_OGGETTO_SEDUTA) {
            if (idPartecipante > 0) {
                this.partecipante = OggettoPartecipante.get(idPartecipante).toDTO(["sedutaPartecipante.commissioneComponente.componente", "sedutaPartecipante.componenteEsterno"]);
                this.incarico = this.partecipante?.sedutaPartecipante?.incarico ?: this.partecipante.sedutaPartecipante?.commissioneComponente?.incarico;
                this.soggetto = this.partecipante?.sedutaPartecipante?.componenteEsterno?:this.partecipante.sedutaPartecipante.commissioneComponente.componente;
            } else {
                this.partecipante = new OggettoPartecipanteDTO(id:-1, oggettoSeduta:oggettoSeduta, presente:true);
            }
		}

		listaIncarichi = Incarico.findAllByValido(true, [sort:'titolo', order:'desc']).toDTO()
		listaIncarichi.add(0, new IncaricoDTO(id:-1, titolo:"-- nessuno --"));

		listaRuoli = RuoloPartecipante.findAllByValido(true, [sort:"descrizione", order:"asc"]).toDTO()
		listaRuoli.add(0, new RuoloPartecipanteDTO(codice:"", descrizione:"-- nessuno --"));
	}

	@Command onCercaSoggetto () {
		Window w = Executions.createComponents("/commons/popupRicercaSoggetti.zul", self, [id : -1])
		w.onClose { Event event ->
			if (event.data != null) {
				this.soggetto = event.data;
				if (sezione == SEZIONE_CONVOCATI || sezione == SEZIONE_PARTECIPANTI) {
					partecipante.componenteEsterno = soggetto;
				} else if (sezione == SEZIONE_OGGETTO_SEDUTA) {
					// in questo caso non faccio nulla perché ancora non ho creato il sedutaPartecipante. mi segno solo il soggetto e nella
					// "onSalva" andrò a passarlo al service.
				}
				BindUtils.postNotifyChange(null, null, this, "soggetto")
			}
		}
		w.doModal()
	}

	@NotifyChange("partecipante")
	@Command onSalvaChiudi() {
		if (onSalva()) {
			onChiudi()
		}
	}

	@Command onChiudi () {
		Events.postEvent(Events.ON_CLOSE, self, null)
	}

	@NotifyChange(["partecipante"])
	@Command onSalva() {
		if (!validaMaschera()) {
			return false;
		}

		if (sezione == SEZIONE_CONVOCATI || sezione == SEZIONE_PARTECIPANTI) {
			partecipante.incarico = this.incarico;
			convocatiSedutaDTOService.salva(partecipante);
		} else if (sezione == SEZIONE_OGGETTO_SEDUTA) {
			oggettoPartecipanteDTOService.salva (partecipante, soggetto, incarico);
		}

		return true;
	}

	private boolean validaMaschera () {
		def messaggi = ["Impossibile salvare il partecipante:"];

		// controllo che il soggetto selezionato non sia già presente tra i partecipanti:
		switch (sezione) {
			case SEZIONE_CONVOCATI:
    			if (!(partecipante.id > 0) && SedutaPartecipante.createCriteria().count {
    				createAlias ("commissioneComponente", "comp", CriteriaSpecification.LEFT_JOIN)
    				eq ("seduta.id", partecipante.seduta.id)
    				
    				or {
    					eq ("componenteEsterno.id", soggetto.id)
    					eq ("comp.componente.id",   soggetto.id)
    				}
    			} > 0) {
    				messaggi << "${soggetto.denominazione} è già presente come Partecipante alla Seduta. Per aggiungerlo come Convocato, va prima rimosso come Partecipante.";
    			}

				if (this.partecipante.firmatario && partecipante.sequenzaFirma > 0 && SedutaPartecipante.countBySedutaAndFirmatarioAndPresenteAndSequenzaFirmaAndIdNotEqual(partecipante.seduta.domainObject, true, true, partecipante.sequenzaFirma, partecipante.id) > 0){
					messaggi << "Esiste un altro firmatario con la stessa 'Sequenza Firma'"
				}
				break;
			case SEZIONE_PARTECIPANTI:
    			if (!(partecipante.id > 0) && SedutaPartecipante.createCriteria().count {
    				createAlias ("commissioneComponente", "comp", CriteriaSpecification.LEFT_JOIN)
    				eq ("seduta.id", partecipante.seduta.id)
    				
    				or {
    					eq ("componenteEsterno.id", soggetto.id)
    					eq ("comp.componente.id",   soggetto.id)
    				}
    			} > 0) {
    				messaggi << "${soggetto.denominazione} è già presente come Partecipante alla Seduta. Non è possibile aggiungerlo nuovamente.";
    			}

				if (this.partecipante.firmatario && partecipante.sequenzaFirma > 0 && SedutaPartecipante.countBySedutaAndFirmatarioAndPresenteAndSequenzaFirmaAndIdNotEqual(partecipante.seduta.domainObject, true, true, partecipante.sequenzaFirma, partecipante.id) > 0){
					messaggi << "Esiste un altro firmatario con la stessa 'Sequenza Firma'"
				}
			break;
			case SEZIONE_OGGETTO_SEDUTA:
				if (!(partecipante.id > 0) && OggettoPartecipante.createCriteria().count {
					createAlias ("sedutaPartecipante", "part", CriteriaSpecification.LEFT_JOIN)
					createAlias ("part.commissioneComponente", "comp", CriteriaSpecification.LEFT_JOIN)
					
					eq ("oggettoSeduta.id", partecipante.oggettoSeduta.id)
					
					or {
						eq ("part.componenteEsterno.id", soggetto.id)
						eq ("comp.componente.id",   	 soggetto.id)
					}
				} > 0) {
					messaggi << "${soggetto.denominazione} è già presente come Partecipante alla discussione di questo oggetto. Non è possibile aggiungerlo nuovamente.";
				}

				if (this.partecipante.firmatario && partecipante.sequenzaFirma > 0 && OggettoPartecipante.countByOggettoSedutaAndFirmatarioAndPresenteAndSequenzaFirmaAndIdNotEqual(partecipante.oggettoSeduta.domainObject, true, true, partecipante.sequenzaFirma, partecipante.id) > 0){
					messaggi << "Esiste un altro firmatario con la stessa 'Sequenza Firma'"
				}
			break;
		}

		if (this.partecipante.firmatario && partecipante.sequenzaFirma <= 0) {
			messaggi << "Il campo 'Sequenza Firma' è obbligatorio"
		}

		if (messaggi.size() > 1) {
			Clients.showNotification(StringUtils.join(messaggi, "\n"), Clients.NOTIFICATION_TYPE_ERROR, self, "before_center", 5000, true);
			return false;
		}

		return true;
	}
}
