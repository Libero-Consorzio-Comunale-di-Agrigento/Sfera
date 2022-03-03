package it.finmatica.atti.dto.documenti.tipologie

import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.atti.dizionari.OggettoRicorrente
import it.finmatica.atti.documenti.tipologie.*
import it.finmatica.atti.dto.dizionari.DelegaDTOService
import it.finmatica.atti.dto.dizionari.OggettoRicorrenteDTO
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.protocollo.ProtocolloGdm
import it.finmatica.atti.integrazioni.protocollo.ProtocolloGdmConfig
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO

class TipoDeterminaDTOService {

	ParametroTipologiaService parametroTipologiaService
	DelegaDTOService		  delegaDTOService

    TipoDeterminaDTO salva (TipoDeterminaDTO tipoDeterminaDTO, def listaParametri, String tipoDocumentoEsterno) {

		TipoDetermina tipoDetermina = tipoDeterminaDTO.getDomainObject()?:new TipoDetermina()
		boolean update = tipoDeterminaDTO.id > 0

		tipoDetermina.valido 				    = tipoDeterminaDTO.valido
		tipoDetermina.titolo 				    = tipoDeterminaDTO.titolo
		tipoDetermina.titoloNotifica 		    = tipoDeterminaDTO.titoloNotifica?:tipoDeterminaDTO.titolo
		tipoDetermina.descrizione 			    = tipoDeterminaDTO.descrizione
        tipoDetermina.descrizioneNotifica       = tipoDeterminaDTO.descrizioneNotifica

		tipoDetermina.vistiPareri               = tipoDeterminaDTO.vistiPareri
		tipoDetermina.registroUnita             = tipoDeterminaDTO.registroUnita
		tipoDetermina.conservazioneSostitutiva  = tipoDeterminaDTO.conservazioneSostitutiva
		tipoDetermina.funzionarioObbligatorio   = tipoDeterminaDTO.funzionarioObbligatorio
		tipoDetermina.notificaOrganiControllo   = tipoDeterminaDTO.notificaOrganiControllo
		tipoDetermina.categoriaObbligatoria  	= tipoDeterminaDTO.categoriaObbligatoria
		tipoDetermina.testoObbligatorio  		= tipoDeterminaDTO.testoObbligatorio
		tipoDetermina.codiceGara     		    = tipoDeterminaDTO.codiceGara
		tipoDetermina.codiceGaraObbligatorio    = tipoDeterminaDTO.codiceGaraObbligatorio
		tipoDetermina.codiceEsterno     		= tipoDeterminaDTO.codiceEsterno

		tipoDetermina.pubblicazione						= tipoDeterminaDTO.pubblicazione
		tipoDetermina.secondaPubblicazione				= tipoDeterminaDTO.secondaPubblicazione
		tipoDetermina.manuale							= tipoDeterminaDTO.manuale
		tipoDetermina.pubblicaAllegati					= tipoDeterminaDTO.pubblicaAllegati
		tipoDetermina.giorniPubblicazione      			= tipoDeterminaDTO.giorniPubblicazione
		tipoDetermina.giorniPubblicazioneModificabile	= tipoDeterminaDTO.giorniPubblicazioneModificabile
		tipoDetermina.pubblicazioneFutura				= tipoDeterminaDTO.pubblicazioneFutura
		tipoDetermina.pubblicazioneFinoARevoca			= tipoDeterminaDTO.pubblicazioneFinoARevoca
		tipoDetermina.pubblicazioneTrasparenza			= tipoDeterminaDTO.pubblicazioneTrasparenza

		tipoDetermina.tipoCertPubb				= tipoDeterminaDTO?.tipoCertPubb?.domainObject
		tipoDetermina.tipoCertAvvPubb			= tipoDeterminaDTO?.tipoCertAvvPubb?.domainObject
		tipoDetermina.tipoCertPubb2				= tipoDeterminaDTO?.tipoCertPubb2?.domainObject
		tipoDetermina.tipoCertAvvPubb2			= tipoDeterminaDTO?.tipoCertAvvPubb2?.domainObject
		tipoDetermina.tipoCertEsec				= tipoDeterminaDTO?.tipoCertEsec?.domainObject
        tipoDetermina.tipoCertImmEseg           = tipoDeterminaDTO?.tipoCertImmEseg?.domainObject

		tipoDetermina.progressivoCfgIterPubblicazione = tipoDeterminaDTO.progressivoCfgIterPubblicazione
		tipoDetermina.progressivoCfgIter		= tipoDeterminaDTO.progressivoCfgIter
		tipoDetermina.tipoRegistro  			= tipoDeterminaDTO.tipoRegistro?.domainObject
		tipoDetermina.tipoRegistro2  			= tipoDeterminaDTO.tipoRegistro2?.domainObject
		tipoDetermina.caratteristicaTipologia 	= tipoDeterminaDTO.caratteristicaTipologia?.domainObject
		tipoDetermina.modelloTesto            	= tipoDeterminaDTO.modelloTesto?.domainObject
		tipoDetermina.modelloTestoFrontespizio 	= tipoDeterminaDTO.modelloTestoFrontespizio?.domainObject
		tipoDetermina.modelloTestoAnnullamento  = tipoDeterminaDTO.modelloTestoAnnullamento?.domainObject

		tipoDetermina.movimentiContabili		= tipoDeterminaDTO.movimentiContabili
		tipoDetermina.esecutivitaMovimenti 		= tipoDeterminaDTO.esecutivitaMovimenti
		tipoDetermina.scritturaMovimentiContabili	= tipoDeterminaDTO.scritturaMovimentiContabili
        tipoDetermina.queryMovimenti            = tipoDeterminaDTO.queryMovimenti
		
		tipoDetermina.tipoPubblicazioneAlbo		= tipoDeterminaDTO.tipoPubblicazioneAlbo
		tipoDetermina.diventaEsecutiva			= tipoDeterminaDTO.diventaEsecutiva
		tipoDetermina.incaricatoObbligatorio	= tipoDeterminaDTO.incaricatoObbligatorio
		tipoDetermina.pubblicaAllegatiDefault	= tipoDeterminaDTO.pubblicaAllegatiDefault && tipoDeterminaDTO.pubblicaAllegati
		tipoDetermina.pubblicaVisualizzatore	= tipoDeterminaDTO.pubblicaVisualizzatore
        tipoDetermina.ruoloRiservato            = tipoDeterminaDTO.ruoloRiservato
        tipoDetermina.cupVisibile               = tipoDeterminaDTO.cupVisibile
        tipoDetermina.cupObbligatorio           = tipoDeterminaDTO.cupObbligatorio && tipoDeterminaDTO.cupVisibile
        tipoDetermina.eseguibilitaImmediata     = tipoDeterminaDTO.eseguibilitaImmediata
        tipoDetermina.pubblicaAllegatiVisualizzatore = tipoDeterminaDTO.pubblicaAllegatiVisualizzatore

		parametroTipologiaService.aggiornaParametri (tipoDetermina, listaParametri);

		tipoDetermina.save()

		if (tipoDeterminaDTO.id <= 0) {
			new TipoDeterminaCompetenza ( ruoloAd4:Ad4Ruolo.get(Impostazioni.RUOLO_ACCESSO_APPLICATIVO.valore)
										, titolo: "Visibile a Tutti"
										, tipoDetermina: tipoDetermina
										, lettura: true).save();
		}

        if (tipoDocumentoEsterno?.trim()?.length() > 0) {
            MappingIntegrazione mappingIntegrazione = MappingIntegrazione.findByCategoriaAndCodiceAndValoreInterno(ProtocolloGdmConfig.MAPPING_CATEGORIA, ProtocolloGdmConfig.MAPPING_CODICE_TIPO_DOCUMENTO, tipoDetermina.id.toString())
            if (mappingIntegrazione == null) {
                mappingIntegrazione = new MappingIntegrazione(categoria: ProtocolloGdmConfig.MAPPING_CATEGORIA, codice: ProtocolloGdmConfig.MAPPING_CODICE_TIPO_DOCUMENTO, valoreInterno: tipoDetermina.id.toString())
            }
            mappingIntegrazione.valoreEsterno = tipoDocumentoEsterno
            mappingIntegrazione.save()
        }

		if (Impostazioni.DELEGHE.abilitato) delegaDTOService.inserisciTipologiaDelega(tipoDetermina, update)
		return tipoDetermina.toDTO()
    }

	void eliminaTipologiaVisto (TipoDeterminaDTO tipoDeterminaDTO, TipoVistoParereDTO tipoVistoDTO) {
		TipoDetermina tipoDetermina = tipoDeterminaDTO.getDomainObject()
		TipoVistoParere tipoVisto   = tipoDetermina.tipiVisto.find { it.id == tipoVistoDTO.id }
		tipoDetermina.removeFromTipiVisto (tipoVisto)
		tipoDetermina.save()
	}

	void aggiungiTipologiaVisto (TipoDeterminaDTO tipoDeterminaDTO, TipoVistoParereDTO tipoVistoDTO) {
		TipoDetermina tipoDetermina = tipoDeterminaDTO.getDomainObject()
		TipoVistoParere tipoVisto   = tipoDetermina.tipiVisto.find { it.id == tipoVistoDTO.id }	// controllo di non aggiungere due volte la stessa tipologia di visto
		if (tipoVisto == null)
			tipoDetermina.addToTipiVisto (tipoVistoDTO.getDomainObject())

		tipoDetermina.save()
	}

	void elimina (TipoDeterminaDTO tipoDeterminaDto) {
		TipoDetermina tipoDete = tipoDeterminaDto.getDomainObject()
		TipoDeterminaCompetenza.findAllByTipoDetermina(tipoDete)*.delete();
		ParametroTipologia.findAllByTipoDetermina(tipoDete)*.delete();
		if (Impostazioni.DELEGHE.abilitato) delegaDTOService.eliminaTipologiaDelega(tipoDete)
		tipoDete.delete()
	}

	void eliminaModelloTesto (TipoDeterminaDTO tipoDeterminaDTO, GestioneTestiModelloDTO gestioneTestiModelloDto) {
		TipoDetermina tipoDetermina = tipoDeterminaDTO.getDomainObject()
		GestioneTestiModello gestioneTestiModello   = tipoDetermina.modelliTesto.find { it.id == gestioneTestiModelloDto.id }
		tipoDetermina.removeFromModelliTesto (gestioneTestiModello)
		tipoDetermina.save()
	}

	void aggiungiModelloTesto (TipoDeterminaDTO tipoDeterminaDTO, GestioneTestiModelloDTO gestioneTestiModelloDto) {
		TipoDetermina tipoDetermina = tipoDeterminaDTO.getDomainObject()
		GestioneTestiModello gestioneTestiModello   = tipoDetermina.tipiVisto.find { it.id == gestioneTestiModelloDto.id }
		if (gestioneTestiModello == null)
			tipoDetermina.addToModelliTesto (gestioneTestiModelloDto.getDomainObject())

		tipoDetermina.save()
	}

	TipoDeterminaCompetenzaDTO salva (TipoDeterminaCompetenzaDTO tipoDeterminaCompetenzaDto) {
		TipoDeterminaCompetenza tipoDeterminaCompetenza = new TipoDeterminaCompetenza()
		tipoDeterminaCompetenza.utenteAd4 		= tipoDeterminaCompetenzaDto?.utenteAd4?.getDomainObject()
		tipoDeterminaCompetenza.ruoloAd4 		= tipoDeterminaCompetenzaDto?.ruoloAd4?.getDomainObject()
		tipoDeterminaCompetenza.unitaSo4 		= tipoDeterminaCompetenzaDto?.unitaSo4?.getDomainObject()
		tipoDeterminaCompetenza.tipoDetermina 	= tipoDeterminaCompetenzaDto.tipoDetermina.getDomainObject()
		tipoDeterminaCompetenza.titolo 			= tipoDeterminaCompetenzaDto.titolo
		tipoDeterminaCompetenza.lettura 		= tipoDeterminaCompetenzaDto.lettura
		tipoDeterminaCompetenza = tipoDeterminaCompetenza.save ()

		return tipoDeterminaCompetenza.toDTO()
	}

	void elimina (TipoDeterminaCompetenzaDTO tipoDeterminaCompetenzaDto) {
		tipoDeterminaCompetenzaDto?.domainObject?.delete(failOnError: true)
	}

	TipoDeterminaDTO duplica (TipoDeterminaDTO tipoDeterminaDTO) {
		TipoDetermina tipoDetermina = tipoDeterminaDTO.domainObject;
		def listaParametri = tipoDetermina.parametri?.collect { [idGruppoStep:it.gruppoStep?.id, codice:it.codice, valore:it.valore] };
		tipoDeterminaDTO.id = -1;
		tipoDeterminaDTO.titolo += " (duplica)";
		TipoDetermina duplica = salva(tipoDeterminaDTO, listaParametri, null).domainObject;

		// duplico anche le competenze:
		TipoDeterminaCompetenza.findAllByTipoDetermina (duplica)*.delete();
		def comp = TipoDeterminaCompetenza.findAllByTipoDetermina (tipoDetermina);
		for (TipoDeterminaCompetenza c : comp) {
			new TipoDeterminaCompetenza(tipoDetermina: duplica, titolo:c.titolo, lettura: c.lettura, modifica: c.modifica, cancellazione: c.cancellazione, ruoloAd4: c.ruoloAd4, unitaSo4: c.unitaSo4, utenteAd4: c.utenteAd4).save()
		}

		return duplica.toDTO();
	}

	void eliminaOggettoRicorrente (TipoDeterminaDTO tipoDeterminaDTO, OggettoRicorrenteDTO oggettoRicorrenteDto) {
		TipoDetermina tipoDetermina = tipoDeterminaDTO.getDomainObject()
		OggettoRicorrente oggettoRicorrente  = tipoDetermina.oggettiRicorrenti.find { it.id == oggettoRicorrenteDto.id }
		tipoDetermina.removeFromOggettiRicorrenti (oggettoRicorrente)
		tipoDetermina.save()
	}

	void aggiungiOggettoRicorrente (TipoDeterminaDTO tipoDeterminaDTO, OggettoRicorrenteDTO oggettoRicorrenteDto) {
		TipoDetermina tipoDetermina = tipoDeterminaDTO.getDomainObject()
		OggettoRicorrente oggettoRicorrente = tipoDetermina.oggettiRicorrenti.find { it.id == oggettoRicorrenteDto.id }
		if (oggettoRicorrente == null)
			tipoDetermina.addToOggettiRicorrenti (oggettoRicorrenteDto.getDomainObject())

		tipoDetermina.save()
	}
}
