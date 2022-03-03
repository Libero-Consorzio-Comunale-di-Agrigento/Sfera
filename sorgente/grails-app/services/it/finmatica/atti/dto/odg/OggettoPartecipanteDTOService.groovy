package it.finmatica.atti.dto.odg

import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.dto.odg.dizionari.IncaricoDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.odg.OggettoPartecipante
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.atti.odg.Seduta
import it.finmatica.atti.odg.SedutaPartecipante
import org.hibernate.NonUniqueResultException
import org.hibernate.criterion.CriteriaSpecification

class OggettoPartecipanteDTOService {

    public OggettoPartecipanteDTO salva (OggettoPartecipanteDTO oggettoPartecipanteDto, As4SoggettoCorrenteDTO soggetto=null, IncaricoDTO incarico=null) {

		// se non ho il partecipante della seduta e se non è già presente nella seduta, lo devo aggiungere
		if (oggettoPartecipanteDto.sedutaPartecipante == null) {

			// prima di inserire il nuovo partecipante, cerco se non è già presente nella seduta:
			SedutaPartecipante sedutaPartecipante = null;
			try {
    			sedutaPartecipante = SedutaPartecipante.createCriteria().get {
					createAlias ("commissioneComponente", "comp", CriteriaSpecification.LEFT_JOIN)
					
    				eq ("seduta.id", oggettoPartecipanteDto.oggettoSeduta.seduta.id)
    				
    				or {
    					eq ("componenteEsterno.id", soggetto.id)
						eq ("comp.componente.id",   soggetto.id)
    				}
    			}
			} catch (NonUniqueResultException e) {
				throw new AttiRuntimeException ("Non è possibile collegare il partecipante richiesto a quello presente in Seduta perché in Seduta sono presenti più partecipanti legati allo stesso soggetto: ${soggetto.denominazione}. Andare nella sezione Partecipanti Seduta ed eliminare una delle occorrenze di questo soggetto.")
			}
			
			if (sedutaPartecipante == null) {
				sedutaPartecipante 							= new SedutaPartecipante();
				sedutaPartecipante.seduta					= oggettoPartecipanteDto.oggettoSeduta.seduta.domainObject
				sedutaPartecipante.componenteEsterno		= soggetto.domainObject
				sedutaPartecipante.ruoloPartecipante  		= oggettoPartecipanteDto.ruoloPartecipante?.domainObject
				sedutaPartecipante.incarico  				= incarico?.domainObject
				// #29945: deve essere inserito il partecipante in seduta come assente
				sedutaPartecipante.presente					= false //oggettoPartecipanteDto.presente
				sedutaPartecipante.assenteNonGiustificato	= oggettoPartecipanteDto.assenteNonGiustificato
				sedutaPartecipante.sequenzaPartecipante		= SedutaPartecipante.countBySeduta(sedutaPartecipante.seduta)+1; // conto tutti i PARTECIPANTI e aggiungo 1 perché la sequenza è 1-based
				sedutaPartecipante.sequenza    				= sedutaPartecipante.sequenzaPartecipante;	// siccome non è un convocato, gli metto la stessa sequenza del partecipante.
				sedutaPartecipante.sequenzaFirma			= oggettoPartecipanteDto.sequenzaFirma
				sedutaPartecipante.firmatario				= oggettoPartecipanteDto.firmatario
				sedutaPartecipante.convocato				= false;	// false perché questo è sicuramente un PARTECIPANTE e non un CONVOCATO
				sedutaPartecipante.save()
			}
			
			oggettoPartecipanteDto.sedutaPartecipante = sedutaPartecipante.toDTO();

			// siccome sono in creazione di un nuovo partecipante, setto anche la sua sequenza (aggiungo 1 perché 1-based)
			oggettoPartecipanteDto.sequenza = OggettoPartecipante.countByOggettoSeduta(oggettoPartecipanteDto.oggettoSeduta.domainObject) + 1;
		}

		// creo il partecipante per l'oggetto seduta
		OggettoPartecipante oggettoPartecipante 	= oggettoPartecipanteDto.domainObject?:new OggettoPartecipante()
		oggettoPartecipante.oggettoSeduta 			= oggettoPartecipanteDto.oggettoSeduta.domainObject
		oggettoPartecipante.sedutaPartecipante		= oggettoPartecipanteDto.sedutaPartecipante.domainObject
		oggettoPartecipante.ruoloPartecipante  		= oggettoPartecipanteDto.ruoloPartecipante?.domainObject
		oggettoPartecipante.presente				= oggettoPartecipanteDto.presente
		oggettoPartecipante.sequenza    			= oggettoPartecipanteDto.sequenza
		oggettoPartecipante.assenteNonGiustificato	= oggettoPartecipanteDto.assenteNonGiustificato
		oggettoPartecipante.sequenzaFirma			= oggettoPartecipanteDto.sequenzaFirma
		oggettoPartecipante.firmatario				= oggettoPartecipanteDto.firmatario
		oggettoPartecipante.voto					= oggettoPartecipanteDto.voto?.domainObject

		oggettoPartecipante.save()

		oggettoPartecipanteDto.id 					= oggettoPartecipante.id
		oggettoPartecipanteDto.version				= oggettoPartecipante.version

		return oggettoPartecipanteDto;
    }

	public void elimina (OggettoPartecipanteDTO oggettoPartecipanteDto) {
		OggettoPartecipante o = oggettoPartecipanteDto.domainObject;
		elimina (o)
	}
	
	public void elimina (OggettoPartecipante o) {
		OggettoSeduta oggettoSeduta = o.oggettoSeduta;
		o.delete()
		
		riordinaPartecipantiOggettoSeduta(oggettoSeduta)
	}
	
	public void riordinaPartecipantiOggettoSeduta (OggettoSeduta oggettoSeduta) {
		// riordino i soggetti:
		def partecipanti = OggettoPartecipante.findAllByOggettoSeduta(oggettoSeduta, [sort:"sequenza", order:"asc"]);
		for (int i=0; i<partecipanti.size(); i++) {
			partecipanti[i].sequenza = (i+1);
			partecipanti[i].save()
		}
	}

	public void eliminaPartecipante (SedutaPartecipanteDTO sedutaPartecipanteDto) {
		// devo eliminare il partecipante da tutti gli oggetti seduta:
		SedutaPartecipante sedutaPartecipante = sedutaPartecipanteDto.domainObject;
		
		// per ogni partecipante per singolo oggetto seduta che trovo, elimino l'oggettopartecipante
		for (def o : OggettoPartecipante.findAllBySedutaPartecipante(sedutaPartecipante)) {
			elimina (o);
		}

		// prima di eliminare il partecipante dalla seduta, mi segno la seduta		
		Seduta seduta = sedutaPartecipante.seduta;
		
		// elimino il partecipante dalla seduta
		sedutaPartecipante.delete();
		
		// riordino i partecipanti alla seduta per sequenza di convocazione
		def partecipanti = SedutaPartecipante.findAllBySeduta(seduta, [sort:"sequenza", order:"asc"]);
		for (int i=0; i<partecipanti.size(); i++) {
			partecipanti[i].sequenza = (i+1);
			partecipanti[i].save()
		}
		
		// riordino i partecipanti alla seduta per sequenza di "partecipazione"
		partecipanti = SedutaPartecipante.findAllBySeduta(seduta, [sort:"sequenzaPartecipante", order:"asc"]);
		for (int i=0; i<partecipanti.size(); i++) {
			partecipanti[i].sequenzaPartecipante = (i+1);
			partecipanti[i].save()
		}
	}

	public void spostaPartecipanteSu (OggettoPartecipanteDTO a, OggettoPartecipanteDTO b, int index) {
		OggettoPartecipante target = a.domainObject
		target.sequenza = index

		OggettoPartecipante prev = b.domainObject
		prev.sequenza = index+1

		target.save()
		prev.save()
	}

	public void spostaPartecipanteGiu (OggettoPartecipanteDTO a, OggettoPartecipanteDTO b, int index) {
		OggettoPartecipante target = a.domainObject
		target.sequenza = index+2
		target.save()

		OggettoPartecipante next = b.domainObject
		next.sequenza = index+1
		next.save()
	}
}
