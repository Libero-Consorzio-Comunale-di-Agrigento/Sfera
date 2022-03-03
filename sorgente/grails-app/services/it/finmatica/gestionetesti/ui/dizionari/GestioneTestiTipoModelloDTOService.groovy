package it.finmatica.gestionetesti.ui.dizionari

import it.finmatica.gestionetesti.reporter.GestioneTestiTipoModello
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiTipoModelloDTO

class GestioneTestiTipoModelloDTOService {


	public GestioneTestiTipoModelloDTO salva(GestioneTestiTipoModelloDTO gestioneTestiTipoModelloDto, byte[] file = null){
		GestioneTestiTipoModello gestTesti = gestioneTestiTipoModelloDto.getDomainObject()?:new GestioneTestiTipoModello()
		gestTesti.codice 		= gestioneTestiTipoModelloDto.codice
		gestTesti.descrizione 	= gestioneTestiTipoModelloDto.descrizione
		gestTesti.valido 		= gestioneTestiTipoModelloDto.valido

		if(file != null){
			// vuol dire che è stato fatto l'upload e quindi devo inserire il nuovo file nel campo query
			gestTesti.query = file
		}

		// controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore
		if (gestTesti.version != gestioneTestiTipoModelloDto.version)
			throw new RuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")

		gestTesti.save ()

		GestioneTestiTipoModelloDTO gestTestiDto = gestTesti.toDTO()
	    gestTestiDto.query = null
	    return 	gestTestiDto
	}


	public byte[] getFileAllegato (String codice){
		byte[] result = GestioneTestiTipoModello.createCriteria().get(){
			projections {
				property("query")
			}
			eq("codice", codice)
		}
		return result
	}

	public void elimina(GestioneTestiTipoModelloDTO gestioneTestiTipoModelloDto){
		GestioneTestiTipoModello gestTesti = gestioneTestiTipoModelloDto.getDomainObject()
		// controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore
		if (gestTesti.version != gestioneTestiTipoModelloDto.version)
			throw new RuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		gestTesti = gestTesti.delete ()
	}
}
