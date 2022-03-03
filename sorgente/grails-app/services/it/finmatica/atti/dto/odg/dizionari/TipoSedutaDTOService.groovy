package it.finmatica.atti.dto.odg.dizionari

import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.odg.dizionari.TipoSeduta

class TipoSedutaDTOService {

    public TipoSedutaDTO salva(TipoSedutaDTO tipoSedutaDTO) {
		TipoSeduta tipoSeduta 	= TipoSeduta.get(tipoSedutaDTO.id)?:new TipoSeduta()
		if(tipoSeduta.version != tipoSedutaDTO.version) {
			throw new AttiRuntimeException(AttiRuntimeException.ERRORE_MODIFICA_CONCORRENTE)
		}
		tipoSeduta.sequenza 	= tipoSedutaDTO.sequenza
		tipoSeduta.titolo 		= tipoSedutaDTO.titolo
		tipoSeduta.descrizione	= tipoSedutaDTO.descrizione
		tipoSeduta.valido 		= tipoSedutaDTO.valido
		tipoSeduta.save (failOnError: true)
		return tipoSeduta.toDTO()
    }

	public void elimina (TipoSedutaDTO tipoSedutaDTO) {
		TipoSeduta.get(tipoSedutaDTO.id).delete()
	}
}
