package it.finmatica.atti.dto.dizionari

import it.finmatica.atti.dizionari.TipoControlloRegolarita
import it.finmatica.atti.exceptions.AttiRuntimeException

class TipoControlloRegolaritaDTOService {

    public TipoControlloRegolaritaDTO salva (TipoControlloRegolaritaDTO tipoControlloRegolaritaDto) {
		TipoControlloRegolarita tipoControlloRegolarita = TipoControlloRegolarita.get(tipoControlloRegolaritaDto.id)?:new TipoControlloRegolarita()
		tipoControlloRegolarita.id = tipoControlloRegolaritaDto.id
		tipoControlloRegolarita.titolo = tipoControlloRegolaritaDto.titolo
		tipoControlloRegolarita.sequenza = tipoControlloRegolaritaDto.sequenza
		tipoControlloRegolarita.ambito = tipoControlloRegolaritaDto.ambito
		tipoControlloRegolarita.valido = tipoControlloRegolaritaDto.valido

		if(tipoControlloRegolarita.version != null && tipoControlloRegolarita.version != tipoControlloRegolaritaDto.version) throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		tipoControlloRegolarita = tipoControlloRegolarita.save ()

		return 	tipoControlloRegolarita.toDTO()
    }

	public void elimina (TipoControlloRegolaritaDTO tipoControlloRegolaritaDto) {
		TipoControlloRegolarita tipoControlloRegolarita = TipoControlloRegolarita.get(tipoControlloRegolaritaDto.id)
		/*controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore*/
		if(tipoControlloRegolarita.version != tipoControlloRegolaritaDto.version) throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		tipoControlloRegolarita.delete(failOnError: true)
	}

}
