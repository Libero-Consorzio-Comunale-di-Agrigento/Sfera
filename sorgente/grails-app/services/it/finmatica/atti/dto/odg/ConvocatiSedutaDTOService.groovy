package it.finmatica.atti.dto.odg

import it.finmatica.atti.dto.odg.SedutaPartecipanteDTO
import it.finmatica.atti.odg.OggettoPartecipante
import it.finmatica.atti.odg.SedutaPartecipante
import it.finmatica.atti.odg.dizionari.RuoloPartecipante

class ConvocatiSedutaDTOService {

    public SedutaPartecipanteDTO salva (SedutaPartecipanteDTO sedutaPartecipanteDto) {

		// se sto creando il partecipante, ne setto le sequenze:
		if (!(sedutaPartecipanteDto.id > 0)) {
			sedutaPartecipanteDto.sequenza = SedutaPartecipante.countBySedutaAndConvocato(sedutaPartecipanteDto.seduta.domainObject, true) + 1;
			sedutaPartecipanteDto.sequenzaPartecipante = SedutaPartecipante.countBySeduta(sedutaPartecipanteDto.seduta.domainObject) + 1;
		}

		SedutaPartecipante sedutaPartecipante 	    	= sedutaPartecipanteDto?.domainObject?:new SedutaPartecipante()
		sedutaPartecipante.seduta 				    	= sedutaPartecipanteDto.seduta?.domainObject;
		sedutaPartecipante.commissioneComponente    	= sedutaPartecipanteDto.commissioneComponente?.domainObject;
		sedutaPartecipante.incarico 					= sedutaPartecipanteDto.incarico?.domainObject;
		sedutaPartecipante.ruoloPartecipante 			= sedutaPartecipanteDto.ruoloPartecipante?.domainObject;

		sedutaPartecipante.componenteEsterno	    	= sedutaPartecipanteDto.componenteEsterno?.domainObject;
		sedutaPartecipante.firmatario			    	= sedutaPartecipanteDto.firmatario
		sedutaPartecipante.delega				    	= sedutaPartecipanteDto.delega
		if (sedutaPartecipante.delega) {
			sedutaPartecipante.sedutaPartecipanteDelegato	= sedutaPartecipanteDto.sedutaPartecipanteDelegato?.domainObject;
			sedutaPartecipante.dataNumeroProtocolloDelega 	= sedutaPartecipanteDto.dataNumeroProtocolloDelega
			sedutaPartecipante.numeroProtocolloDelega	  	= sedutaPartecipanteDto.numeroProtocolloDelega
			sedutaPartecipante.annoProtocolloDelega		 	= sedutaPartecipanteDto.annoProtocolloDelega
		}
		else {
			sedutaPartecipante.sedutaPartecipanteDelegato	= null
			sedutaPartecipante.dataNumeroProtocolloDelega 	= null
			sedutaPartecipante.numeroProtocolloDelega	  	= null
			sedutaPartecipante.annoProtocolloDelega		 	= null
		}
		sedutaPartecipante.sequenzaFirma		    	= sedutaPartecipanteDto.sequenzaFirma
		sedutaPartecipante.sequenza				    	= sedutaPartecipanteDto.sequenza
		sedutaPartecipante.sequenzaPartecipante	    	= sedutaPartecipanteDto.sequenzaPartecipante
		sedutaPartecipante.convocato			    	= sedutaPartecipanteDto.convocato
		sedutaPartecipante.presente				    	= sedutaPartecipanteDto.presente
		sedutaPartecipante.assenteNonGiustificato		= sedutaPartecipanteDto.assenteNonGiustificato

		sedutaPartecipante.dataEntrata=sedutaPartecipanteDto.dataEntrata
		sedutaPartecipante.oraEntrata=sedutaPartecipanteDto.oraEntrata
		sedutaPartecipante.dataUscita=sedutaPartecipanteDto.dataUscita
		sedutaPartecipante.oraUscita=sedutaPartecipanteDto.oraUscita

		sedutaPartecipante.save (failOnError: true)
		sedutaPartecipanteDto.id 		= sedutaPartecipante.id
		sedutaPartecipanteDto.version	= sedutaPartecipante.version
		return sedutaPartecipanteDto
    }

	public void impostaTuttiPresenti (SedutaDTO sedutaDto) {
		def partecipanti = SedutaPartecipante.findAllBySeduta(sedutaDto.domainObject);
		for (SedutaPartecipante p : partecipanti) {
			p.assenteNonGiustificato = false;
			p.presente = true;
			p.save()
		}
	}

	public void spostaConvocatoSu (SedutaPartecipanteDTO a, SedutaPartecipanteDTO b, int index) {
		SedutaPartecipante target = a.domainObject
		target.sequenza = index

		SedutaPartecipante prev = b.domainObject
		prev.sequenza = index+1

		// se verbalizzazione non attiva, copio la nuova sequenza convocato sulla sequenza partecipante
		if(!checkAbilitazioneTabVerbalizzazione(target)) {
			target.sequenzaPartecipante = target.sequenza
			prev.sequenzaPartecipante = prev.sequenza
		}

		target.save()
		prev.save()
   }

	public void spostaConvocatoGiu (SedutaPartecipanteDTO a, SedutaPartecipanteDTO b, int index) {
		SedutaPartecipante target = a.domainObject
		target.sequenza = index+2

		SedutaPartecipante next = b.domainObject
		next.sequenza = index+1

		// se verbalizzazione non attiva, copio la nuova sequenza convocato sulla sequenza partecipante
		if(!checkAbilitazioneTabVerbalizzazione(target)) {
			target.sequenzaPartecipante = target.sequenza
			next.sequenzaPartecipante = next.sequenza
		}

		target.save()
		next.save()
	}

	public void spostaConvocatoSu (SedutaPartecipanteDTO corrente, SedutaPartecipanteDTO precedente) {
		SedutaPartecipante target = corrente.domainObject
		target.sequenza = corrente.sequenza

		SedutaPartecipante prev = precedente.domainObject
		prev.sequenza = precedente.sequenza

		// se verbalizzazione non attiva, copio la nuova sequenza convocato sulla sequenza partecipante
		if(!checkAbilitazioneTabVerbalizzazione(target)) {
			target.sequenzaPartecipante = target.sequenza
			prev.sequenzaPartecipante = prev.sequenza
		}

		target.save()
		prev.save()
	}

	public void spostaConvocatoGiu (SedutaPartecipanteDTO corrente, SedutaPartecipanteDTO successivo) {
		SedutaPartecipante target = corrente.domainObject
		target.sequenza = corrente.sequenza

		SedutaPartecipante next = successivo.domainObject
		next.sequenza = successivo.sequenza

		// se verbalizzazione non attiva, copio la nuova sequenza convocato sulla sequenza partecipante
		if(!checkAbilitazioneTabVerbalizzazione(target)) {
			target.sequenzaPartecipante = target.sequenza
			next.sequenzaPartecipante = next.sequenza
		}

		target.save()
		next.save()
	}

	public void spostaPartecipanteSu (SedutaPartecipanteDTO a, SedutaPartecipanteDTO b, int index) {
		SedutaPartecipante target = a.domainObject
		target.sequenzaPartecipante = index

		SedutaPartecipante prev = b.domainObject
		prev.sequenzaPartecipante = index+1

		target.save()
		prev.save()
	}

	public void spostaPartecipanteGiu (SedutaPartecipanteDTO a, SedutaPartecipanteDTO b, int index) {
		SedutaPartecipante target = a.domainObject
		target.sequenzaPartecipante = index+2

		SedutaPartecipante next = b.domainObject
		next.sequenzaPartecipante = index+1

		target.save()
		next.save()
	}

	public void spostaPartecipanteSu (SedutaPartecipanteDTO corrente, SedutaPartecipanteDTO precedente) {
		SedutaPartecipante target = corrente.domainObject
		target.sequenzaPartecipante = corrente.sequenzaPartecipante

		SedutaPartecipante prev = precedente.domainObject
		prev.sequenzaPartecipante = precedente.sequenzaPartecipante

		target.save()
		prev.save()
	}

	public void spostaPartecipanteGiu (SedutaPartecipanteDTO corrente, SedutaPartecipanteDTO successivo) {
		SedutaPartecipante target = corrente.domainObject
		target.sequenzaPartecipante = corrente.sequenzaPartecipante

		SedutaPartecipante next = successivo.domainObject
		next.sequenzaPartecipante = successivo.sequenzaPartecipante

		target.save()
		next.save()
	}

	private boolean checkAbilitazioneTabVerbalizzazione(SedutaPartecipante sedutaPartecipante) {
		Date dataSedutaFormatted = sedutaPartecipante.seduta.dataSeduta.clearTime()
		Date oggi = new Date().clearTime();

		return (dataSedutaFormatted.compareTo(oggi) <= 0 && sedutaPartecipante.seduta.oraSeduta != null)
	}

	public List<String> checkRuoliObbligatori (SedutaDTO seduta) {
		def messages = [];
		boolean presenti = SedutaPartecipante.countBySedutaAndPresente(seduta.domainObject, true) > 0;

		int nPresidenti = contaSoggettiRuoli(seduta, RuoloPartecipante.CODICE_PRESIDENTE, presenti);
		int nSegretari  = contaSoggettiRuoli(seduta, RuoloPartecipante.CODICE_SEGRETARIO, presenti);

		if (seduta.commissione.ruoliObbligatori && nPresidenti == 0) {
			messages << "È necessario specificare un Presidente"
		}

		if (seduta.commissione.ruoliObbligatori && nSegretari == 0) {
			messages << "È necessario specificare un Segretario"
		}

		if (nPresidenti > 1 && seduta.commissione.singoloPresidente) {
			messages << "È necessario specificare un solo Presidente"
		}

		if (nSegretari > 1) {
			messages << "È necessario specificare un solo Segretario"
		}

		return messages;
	}

	public int contaSoggettiRuoli (SedutaDTO seduta, String ruolo, Boolean presenti = null) {
		if (presenti == null) {
			presenti = SedutaPartecipante.countBySedutaAndPresente(seduta.domainObject, true) > 0;
		}

		return SedutaPartecipante.createCriteria().count {
			eq('seduta.id', seduta.id)
			if (presenti) {
				eq('presente', true)
			}
			ruoloPartecipante {
				eq("codice", ruolo)
			}
		}
	}

	public int contaAltriSoggettiRuoliOggettoSeduta (OggettoSedutaDTO oggettoSeduta, String ruolo, long partecipanteId) {
		boolean presenti = OggettoPartecipante.countByOggettoSedutaAndPresente(oggettoSeduta.domainObject, true) > 0;

		return OggettoPartecipante.createCriteria().count {
			eq('oggettoSeduta.id', oggettoSeduta.id)
			notEqual('id',partecipanteId)
			if (presenti) {
				eq('presente', true)
			}
			ruoloPartecipante {
				eq("codice", ruolo)
			}
		}
	}
}
