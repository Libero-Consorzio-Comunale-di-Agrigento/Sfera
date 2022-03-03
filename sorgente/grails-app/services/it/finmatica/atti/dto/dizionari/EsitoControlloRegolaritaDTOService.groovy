package it.finmatica.atti.dto.dizionari

import it.finmatica.atti.dizionari.EsitoControlloRegolarita
import it.finmatica.atti.exceptions.AttiRuntimeException

class EsitoControlloRegolaritaDTOService {

    public EsitoControlloRegolaritaDTO salva(EsitoControlloRegolaritaDTO esitoControlloRegolaritaDTO) {
		EsitoControlloRegolarita esito 	= EsitoControlloRegolarita.get(esitoControlloRegolaritaDTO.id)?:new EsitoControlloRegolarita()
    	if(esito.version != esitoControlloRegolaritaDTO.version) {
			throw new AttiRuntimeException(AttiRuntimeException.ERRORE_MODIFICA_CONCORRENTE)
		}
    	esito.titolo					= esitoControlloRegolaritaDTO.titolo
		esito.descrizione				= esitoControlloRegolaritaDTO.descrizione
		esito.valido					= esitoControlloRegolaritaDTO.valido
		esito.ambito					= esitoControlloRegolaritaDTO.ambito
		esito.save()
		return esito.toDTO()
	}

	public void elimina(EsitoControlloRegolaritaDTO esitoControlloRegolaritaDTO) {
		EsitoControlloRegolarita.get(esitoControlloRegolaritaDTO.id).delete()
	}

	public List<EsitoControlloRegolaritaDTO> getListaEsiti (String ambito) {
		return EsitoControlloRegolarita.createCriteria().list() {
			eq ("ambito", ambito)
			eq ("valido", true)

			order('titolo', 	 'asc')
			order('descrizione', 'asc')
		}.toDTO()
	}
}

