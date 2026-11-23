package it.finmatica.atti.dto.odg.dizionari

import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.odg.dizionari.GruppoPolitico
import it.finmatica.atti.odg.dizionari.GruppoPoliticoComponente
import it.finmatica.atti.dto.odg.dizionari.GruppoPoliticoComponenteDTO
import it.finmatica.atti.dto.odg.dizionari.GruppoPoliticoDTO

class GruppoPoliticoComponenteDTOService {

    public GruppoPoliticoComponenteDTO salva(GruppoPoliticoComponenteDTO gruppoPoliticoComponenteDTO , GruppoPoliticoDTO gruppoPoliticoDTO) {
		GruppoPoliticoComponente gruppoPoliticoComponente = GruppoPoliticoComponente.get(gruppoPoliticoComponenteDTO.id)?:new GruppoPoliticoComponente()
		if(gruppoPoliticoComponente.version != gruppoPoliticoComponenteDTO.version) {
			throw new AttiRuntimeException(AttiRuntimeException.ERRORE_MODIFICA_CONCORRENTE)
		}
		GruppoPolitico gruppoPolitico 					= GruppoPolitico.get(gruppoPoliticoDTO.id)?:new GruppoPolitico()
		gruppoPoliticoComponente.componente				= As4SoggettoCorrente.get(gruppoPoliticoComponenteDTO.componente.id)
		gruppoPoliticoComponente.valido 					= gruppoPoliticoComponenteDTO.valido
		gruppoPoliticoComponente.gruppoPolitico 			= gruppoPolitico
		gruppoPoliticoComponente.save (failOnError: true)
		return gruppoPoliticoComponente.toDTO()
    }

	public void elimina (GruppoPoliticoComponenteDTO gruppoPoliticoComponenteDTO) {
		GruppoPoliticoComponente.get(gruppoPoliticoComponenteDTO.id).delete()
	}
}
