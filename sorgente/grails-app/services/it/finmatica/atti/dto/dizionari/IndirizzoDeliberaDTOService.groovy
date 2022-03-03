package it.finmatica.atti.dto.dizionari


import it.finmatica.atti.dizionari.IndirizzoDelibera
import it.finmatica.atti.exceptions.AttiRuntimeException


class IndirizzoDeliberaDTOService {

    public IndirizzoDeliberaDTO salva (IndirizzoDeliberaDTO indirizzoDeliberaDto) {
		IndirizzoDelibera indirizzoDelibera = IndirizzoDelibera.get(indirizzoDeliberaDto.id)?:new IndirizzoDelibera()
		indirizzoDelibera.titolo = indirizzoDeliberaDto.titolo
		indirizzoDelibera.descrizione = indirizzoDeliberaDto.descrizione
		indirizzoDelibera.valido = indirizzoDeliberaDto.valido
		/*controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore*/
		if(indirizzoDelibera.version != indirizzoDeliberaDto.version) throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		indirizzoDelibera = indirizzoDelibera.save ()

		return 	indirizzoDelibera.toDTO()
    }

	public void elimina (IndirizzoDeliberaDTO indirizzoDeliberaDto) {
		IndirizzoDelibera indirizzoDelibera = IndirizzoDelibera.get(indirizzoDeliberaDto.id)
		/*controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore*/
		if(indirizzoDelibera.version != indirizzoDeliberaDto.version) throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		indirizzoDelibera.delete(failOnError: true)
	}

}
