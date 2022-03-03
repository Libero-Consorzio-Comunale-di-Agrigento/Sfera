package it.finmatica.atti.dto.odg.dizionari

import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.odg.dizionari.Voto

class VotoDTOService {

    public VotoDTO salva(VotoDTO votoDto){
		Voto voto 		 = Voto.get(votoDto.id)?:new Voto()
		if(voto.version != votoDto.version) {
			throw new AttiRuntimeException(AttiRuntimeException.ERRORE_MODIFICA_CONCORRENTE)
		}
		voto.codice		 = votoDto.codice
		voto.descrizione = votoDto.descrizione
		voto.predefinito = votoDto.predefinito
		voto.valore 	 = votoDto.valore
		voto.sequenza 	 = votoDto.sequenza
		voto.valido 	 = votoDto.valido
		voto.save (failOnError: true)
		return voto.toDTO()
	}

	public void elimina(VotoDTO votoDto){
		Voto.get(votoDto.id).delete()
	}

}
