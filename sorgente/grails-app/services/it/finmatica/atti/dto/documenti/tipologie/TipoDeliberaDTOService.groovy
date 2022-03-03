package it.finmatica.atti.dto.documenti.tipologie

import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.atti.dizionari.OggettoRicorrente
import it.finmatica.atti.documenti.tipologie.*
import it.finmatica.atti.dto.dizionari.DelegaDTOService
import it.finmatica.atti.dto.dizionari.OggettoRicorrenteDTO
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.protocollo.ProtocolloGdm
import it.finmatica.atti.integrazioni.protocollo.ProtocolloGdmConfig
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO

class TipoDeliberaDTOService {

	ParametroTipologiaService parametroTipologiaService;
	DelegaDTOService		  delegaDTOService

    public TipoDeliberaDTO salva (TipoDeliberaDTO tipoDeliberaDTO, def listaParametri, String tipoDocumentoEsterno) {
		TipoDelibera tipoDelibera = tipoDeliberaDTO.domainObject?:new TipoDelibera()
		boolean update = tipoDelibera.id > 0

		tipoDelibera.valido 					= tipoDeliberaDTO.valido
		tipoDelibera.titolo 					= tipoDeliberaDTO.titolo
		tipoDelibera.titoloNotifica 			= tipoDeliberaDTO.titoloNotifica?:tipoDeliberaDTO.titolo
		tipoDelibera.descrizione 				= tipoDeliberaDTO.descrizione
        tipoDelibera.descrizioneNotifica        = tipoDeliberaDTO.descrizioneNotifica
        tipoDelibera.descrizioneNotificaDelibera= tipoDeliberaDTO.descrizioneNotificaDelibera

		tipoDelibera.delega 					= tipoDeliberaDTO.delega
		tipoDelibera.delegaObbligatoria 		= tipoDeliberaDTO.delega?tipoDeliberaDTO.delegaObbligatoria:false

		tipoDelibera.vistiPareri              	= tipoDeliberaDTO.vistiPareri
		tipoDelibera.conservazioneSostitutiva 	= tipoDeliberaDTO.conservazioneSostitutiva
		tipoDelibera.funzionarioObbligatorio  	= tipoDeliberaDTO.funzionarioObbligatorio
		tipoDelibera.tipoCertPubb				= tipoDeliberaDTO?.tipoCertPubb?.domainObject
		tipoDelibera.tipoCertAvvPubb			= tipoDeliberaDTO?.tipoCertAvvPubb?.domainObject
		tipoDelibera.tipoCertEsec				= tipoDeliberaDTO?.tipoCertEsec?.domainObject
		tipoDelibera.tipoCertImmEseg			= tipoDeliberaDTO?.tipoCertImmEseg?.domainObject
		tipoDelibera.tipoCertPubb2				= tipoDeliberaDTO?.tipoCertPubb2?.domainObject
		tipoDelibera.tipoCertAvvPubb2			= tipoDeliberaDTO?.tipoCertAvvPubb2?.domainObject
		tipoDelibera.notificaOrganiControllo 	= tipoDeliberaDTO.notificaOrganiControllo
		tipoDelibera.eseguibilitaImmediata 		= tipoDeliberaDTO.eseguibilitaImmediata
		tipoDelibera.categoriaObbligatoria 		= tipoDeliberaDTO.categoriaObbligatoria
		tipoDelibera.testoObbligatorio  		= tipoDeliberaDTO.testoObbligatorio
		tipoDelibera.adottabile  				= tipoDeliberaDTO.adottabile
		tipoDelibera.codiceEsterno     			= tipoDeliberaDTO.codiceEsterno

		tipoDelibera.pubblicazione				= tipoDeliberaDTO.pubblicazione
		tipoDelibera.secondaPubblicazione		= tipoDeliberaDTO.pubblicazione ? tipoDeliberaDTO.secondaPubblicazione : false;
		tipoDelibera.manuale					= tipoDeliberaDTO.manuale
		tipoDelibera.pubblicaAllegati			= tipoDeliberaDTO.pubblicaAllegati
		tipoDelibera.giorniPubblicazione        = tipoDeliberaDTO.giorniPubblicazione
		tipoDelibera.giorniPubblicazioneModificabile = tipoDeliberaDTO.giorniPubblicazioneModificabile
		tipoDelibera.pubblicazioneFutura 		= tipoDeliberaDTO.pubblicazioneFutura
		tipoDelibera.pubblicazioneFinoARevoca	= tipoDeliberaDTO.pubblicazioneFinoARevoca
		tipoDelibera.pubblicazioneTrasparenza	= tipoDeliberaDTO.pubblicazioneTrasparenza
		tipoDelibera.pubblicaVisualizzatore		= tipoDeliberaDTO.pubblicaVisualizzatore
        tipoDelibera.pubblicaAllegatiVisualizzatore = tipoDeliberaDTO.pubblicaAllegatiVisualizzatore

		tipoDelibera.progressivoCfgIter					= tipoDeliberaDTO.progressivoCfgIter			  > 0 ? tipoDeliberaDTO.progressivoCfgIter 				: null 
		tipoDelibera.progressivoCfgIterFuoriSacco	 	= tipoDeliberaDTO.progressivoCfgIterFuoriSacco 	  > 0 ? tipoDeliberaDTO.progressivoCfgIterFuoriSacco 	: null 
		tipoDelibera.progressivoCfgIterPubblicazione	= tipoDeliberaDTO.progressivoCfgIterPubblicazione > 0 ? tipoDeliberaDTO.progressivoCfgIterPubblicazione : null 

		tipoDelibera.commissione						= tipoDeliberaDTO.commissione?.domainObject
		tipoDelibera.tipoRegistroDelibera				= tipoDeliberaDTO.tipoRegistroDelibera?.domainObject
		tipoDelibera.caratteristicaTipologia 			= tipoDeliberaDTO.caratteristicaTipologia?.domainObject
		tipoDelibera.caratteristicaTipologiaFuoriSacco 	= tipoDeliberaDTO.caratteristicaTipologiaFuoriSacco?.domainObject
		tipoDelibera.modelloTesto						= tipoDeliberaDTO.modelloTesto?.domainObject
		tipoDelibera.modelloTestoFrontespizio			= tipoDeliberaDTO.modelloTestoFrontespizio?.domainObject
		tipoDelibera.copiaTestoProposta					= tipoDeliberaDTO.copiaTestoProposta
        tipoDelibera.allegatoTestoProposta              = tipoDeliberaDTO.allegatoTestoProposta

		tipoDelibera.progressivoCfgIterDelibera			= tipoDeliberaDTO.progressivoCfgIterDelibera > 0 ? tipoDeliberaDTO.progressivoCfgIterDelibera : null 
		tipoDelibera.modelloTestoDelibera				= tipoDeliberaDTO.modelloTestoDelibera?.domainObject
		tipoDelibera.modelloTestoFrontespizio			= tipoDeliberaDTO.modelloTestoFrontespizio?.domainObject
		tipoDelibera.caratteristicaTipologiaDelibera	= tipoDeliberaDTO.caratteristicaTipologiaDelibera?.domainObject

		tipoDelibera.movimentiContabili					= tipoDeliberaDTO.movimentiContabili
		tipoDelibera.scritturaMovimentiContabili		= tipoDeliberaDTO.scritturaMovimentiContabili
        tipoDelibera.queryMovimenti                     = tipoDeliberaDTO.queryMovimenti
		tipoDelibera.tipoPubblicazioneAlbo				= tipoDeliberaDTO.tipoPubblicazioneAlbo
		tipoDelibera.diventaEsecutiva					= tipoDeliberaDTO.diventaEsecutiva
		tipoDelibera.esecutivitaMovimenti				= tipoDeliberaDTO.esecutivitaMovimenti
		tipoDelibera.incaricatoObbligatorio				= tipoDeliberaDTO.incaricatoObbligatorio
		tipoDelibera.sequenza							= tipoDeliberaDTO.sequenza
		tipoDelibera.pubblicaAllegatiDefault			= tipoDeliberaDTO.pubblicaAllegatiDefault && tipoDeliberaDTO.pubblicaAllegati
        tipoDelibera.ruoloRiservato                     = tipoDeliberaDTO.ruoloRiservato

		parametroTipologiaService.aggiornaParametri (tipoDelibera, listaParametri)

		if (!tipoDelibera.adottabile) {
			tipoDelibera.tipoRegistroDelibera 	= null;
		}

		tipoDelibera.save()

		if (tipoDeliberaDTO.id <= 0) {
			new TipoDeliberaCompetenza (ruoloAd4:Ad4Ruolo.get(Impostazioni.RUOLO_ACCESSO_APPLICATIVO.valore), titolo: "Visibile a Tutti", tipoDelibera: tipoDelibera, lettura: true).save();
		}

		if (tipoDocumentoEsterno?.trim()?.length() > 0) {
			MappingIntegrazione mappingIntegrazione = MappingIntegrazione.findByCategoriaAndCodiceAndValoreInterno(ProtocolloGdmConfig.MAPPING_CATEGORIA, ProtocolloGdmConfig.MAPPING_CODICE_TIPO_DOCUMENTO, tipoDelibera.id.toString())
			if (mappingIntegrazione == null) {
				mappingIntegrazione = new MappingIntegrazione(categoria: ProtocolloGdmConfig.MAPPING_CATEGORIA, codice: ProtocolloGdmConfig.MAPPING_CODICE_TIPO_DOCUMENTO, valoreInterno: tipoDelibera.id.toString())
			}
			mappingIntegrazione.valoreEsterno = tipoDocumentoEsterno
			mappingIntegrazione.save()
		}

		if (Impostazioni.DELEGHE.abilitato) delegaDTOService.inserisciTipologiaDelega(tipoDelibera, update)
		return tipoDelibera.toDTO()
    }

	public void eliminaTipologiaVisto (TipoDeliberaDTO tipoDeliberaDTO, TipoVistoParereDTO tipoVistoDTO) {
		TipoDelibera tipoDelibera = tipoDeliberaDTO.domainObject
		TipoVistoParere tipoVisto   = tipoDelibera.tipiVisto.find { it.id == tipoVistoDTO.id }
		tipoDelibera.removeFromTipiVisto (tipoVisto)
		tipoDelibera.save()
	}

	public void aggiungiTipologiaVisto (TipoDeliberaDTO tipoDeliberaDTO, TipoVistoParereDTO tipoVistoDTO) {
		TipoDelibera tipoDelibera = tipoDeliberaDTO.domainObject
		TipoVistoParere tipoVisto   = tipoDelibera.tipiVisto.find { it.id == tipoVistoDTO.id }	// controllo di non aggiungere due volte la stessa tipologia di visto
		if (tipoVisto == null)
			tipoDelibera.addToTipiVisto (tipoVistoDTO.domainObject)

		tipoDelibera.save()
	}

	public void elimina(TipoDeliberaDTO tipoDeliberaDto){
		TipoDelibera tipoDelibera = tipoDeliberaDto.domainObject;
		TipoDeliberaCompetenza.findAllByTipoDelibera (tipoDelibera)*.delete();
		ParametroTipologia.findAllByTipoDelibera (tipoDelibera)*.delete();
		if (Impostazioni.DELEGHE.abilitato) delegaDTOService.eliminaTipologiaDelega(tipoDelibera)
		tipoDelibera.delete()
	}
	
	public void eliminaModelloTesto (TipoDeliberaDTO tipoDeliberaDTO, GestioneTestiModelloDTO gestioneTestiModelloDto) {
		TipoDelibera tipoDelibera = tipoDeliberaDTO.domainObject
		if (tipoDelibera.modelloTesto?.id == gestioneTestiModelloDto.id) {
			tipoDelibera.modelloTesto = null;
		}
		
		if (tipoDelibera.modelloTestoDelibera?.id == gestioneTestiModelloDto.id) {
			tipoDelibera.modelloTestoDelibera = null;
		}
		
		// devo fare così perché la .domainObject ritorna un oggetto "diverso" da quello nella lista e grails non capisce che deve eliminarlo.
		GestioneTestiModello gestioneTestiModello   = tipoDelibera.modelliTesto.find { it.id == gestioneTestiModelloDto.id }
		tipoDelibera.removeFromModelliTesto (gestioneTestiModello)
		tipoDelibera.save()
	}

	public void aggiungiModelloTesto (TipoDeliberaDTO tipoDeliberaDTO, GestioneTestiModelloDTO gestioneTestiModelloDto) {
		TipoDelibera tipoDelibera = tipoDeliberaDTO.domainObject
		GestioneTestiModello gestioneTestiModello   = tipoDelibera.tipiVisto.find { it.id == gestioneTestiModelloDto.id }
		if (gestioneTestiModello == null)
			tipoDelibera.addToModelliTesto (gestioneTestiModelloDto.domainObject)

		tipoDelibera.save()
	}

	void eliminaOggettoRicorrente (TipoDeliberaDTO tipoDeliberaDTO, OggettoRicorrenteDTO oggettoRicorrenteDto) {
		TipoDelibera tipoDelibera = tipoDeliberaDTO.getDomainObject()
		OggettoRicorrente oggettoRicorrente  = tipoDelibera.oggettiRicorrenti.find { it.id == oggettoRicorrenteDto.id }
		tipoDelibera.removeFromOggettiRicorrenti (oggettoRicorrente)
		tipoDelibera.save()
	}

	void aggiungiOggettoRicorrente (TipoDeliberaDTO tipoDeliberaDTO, OggettoRicorrenteDTO oggettoRicorrenteDto) {
		TipoDelibera tipoDelibera = tipoDeliberaDTO.getDomainObject()
		OggettoRicorrente oggettoRicorrente = tipoDelibera.oggettiRicorrenti.find { it.id == oggettoRicorrenteDto.id }
		if (oggettoRicorrente == null)
			tipoDelibera.addToOggettiRicorrenti (oggettoRicorrenteDto.getDomainObject())

		tipoDelibera.save()
	}

	public TipoDeliberaCompetenzaDTO salva (TipoDeliberaCompetenzaDTO tipoDeliberaCompetenzaDto) {
		TipoDeliberaCompetenza tipoDeliberaCompetenza = new TipoDeliberaCompetenza()
		tipoDeliberaCompetenza.utenteAd4	= tipoDeliberaCompetenzaDto?.utenteAd4?.domainObject
		tipoDeliberaCompetenza.ruoloAd4 	= tipoDeliberaCompetenzaDto?.ruoloAd4?.domainObject
		tipoDeliberaCompetenza.unitaSo4 	= tipoDeliberaCompetenzaDto?.unitaSo4?.domainObject
		tipoDeliberaCompetenza.tipoDelibera = tipoDeliberaCompetenzaDto.tipoDelibera.domainObject
		tipoDeliberaCompetenza.titolo 		= tipoDeliberaCompetenzaDto.titolo
		tipoDeliberaCompetenza.lettura 		= tipoDeliberaCompetenzaDto.lettura
		tipoDeliberaCompetenza = tipoDeliberaCompetenza.save ()

		return tipoDeliberaCompetenza.toDTO()
	}

	public void elimina (TipoDeliberaCompetenzaDTO tipoDeliberaCompetenzaDto) {
		tipoDeliberaCompetenzaDto?.domainObject?.delete(failOnError: true)
	}

	public TipoDeliberaDTO duplica (TipoDeliberaDTO tipoDeliberaDTO) {
		TipoDelibera tipoDelibera = tipoDeliberaDTO.domainObject;
		def listaParametri = tipoDelibera.parametri?.collect { [idGruppoStep:it.gruppoStep?.id, codice:it.codice, valore:it.valore] };
		tipoDeliberaDTO.id = -1;
		tipoDeliberaDTO.titolo += " (duplica)";
		TipoDelibera duplica = salva(tipoDeliberaDTO, listaParametri, "").domainObject;

		// duplico anche le competenze:
		TipoDeliberaCompetenza.findAllByTipoDelibera (duplica)*.delete();
		def comp = TipoDeliberaCompetenza.findAllByTipoDelibera (tipoDelibera);
		for (TipoDeliberaCompetenza c : comp) {
			new TipoDeliberaCompetenza(tipoDelibera: duplica, titolo:c.titolo, lettura: c.lettura, modifica: c.modifica, cancellazione: c.cancellazione, ruoloAd4: c.ruoloAd4, unitaSo4: c.unitaSo4, utenteAd4: c.utenteAd4).save()
		}

		return duplica.toDTO();
	}
}
