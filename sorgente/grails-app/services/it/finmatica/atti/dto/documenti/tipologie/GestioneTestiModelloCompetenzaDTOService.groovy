package it.finmatica.atti.dto.documenti.tipologie

import it.finmatica.atti.documenti.tipologie.GestioneTestiModelloCompetenza
import it.finmatica.atti.exceptions.AttiRuntimeException

class GestioneTestiModelloCompetenzaDTOService {

	public GestioneTestiModelloCompetenzaDTO salva (GestioneTestiModelloCompetenzaDTO gestioneTestiModelloCompetenzaDto) {
		GestioneTestiModelloCompetenza gestioneTestiModelloCompetenza = new GestioneTestiModelloCompetenza()
		gestioneTestiModelloCompetenza.utenteAd4 = gestioneTestiModelloCompetenzaDto?.utenteAd4?.getDomainObject()
		gestioneTestiModelloCompetenza.ruoloAd4 = gestioneTestiModelloCompetenzaDto?.ruoloAd4?.getDomainObject()
		gestioneTestiModelloCompetenza.unitaSo4 = gestioneTestiModelloCompetenzaDto?.unitaSo4?.getDomainObject()
		gestioneTestiModelloCompetenza.gestioneTestiModello = gestioneTestiModelloCompetenzaDto.gestioneTestiModello.getDomainObject()
		gestioneTestiModelloCompetenza.titolo = gestioneTestiModelloCompetenzaDto.titolo
		gestioneTestiModelloCompetenza.lettura = gestioneTestiModelloCompetenzaDto.lettura
		/*controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore*/
		if(gestioneTestiModelloCompetenza.version != gestioneTestiModelloCompetenzaDto.version) throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		gestioneTestiModelloCompetenza = gestioneTestiModelloCompetenza.save ()

		return 	gestioneTestiModelloCompetenza.toDTO()
	}

	public void elimina (GestioneTestiModelloCompetenzaDTO gestioneTestiModelloCompetenzaDto) {
		GestioneTestiModelloCompetenza gestioneTestiModelloCompetenza = GestioneTestiModelloCompetenza.get(gestioneTestiModelloCompetenzaDto.id)
		/*controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore*/
		if(gestioneTestiModelloCompetenza.version != gestioneTestiModelloCompetenzaDto.version) throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		gestioneTestiModelloCompetenza.delete(failOnError: true)
	}

}
