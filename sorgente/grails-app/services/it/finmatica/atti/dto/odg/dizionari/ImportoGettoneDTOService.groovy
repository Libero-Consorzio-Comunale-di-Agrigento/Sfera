package it.finmatica.atti.dto.odg.dizionari


import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.odg.dizionari.ImportoGettone


class ImportoGettoneDTOService {

    public ImportoGettoneDTO salva (ImportoGettoneDTO importoGettoneDto) {
		ImportoGettone importoGettone = ImportoGettone.get(importoGettoneDto.id)?:new ImportoGettone()
		importoGettone.importo = importoGettoneDto.importo
		importoGettone.commissione = importoGettoneDto.commissione.getDomainObject()
		importoGettone.valido = importoGettoneDto.valido
		importoGettone.validoAl = importoGettoneDto.validoAl
		importoGettone.validoDal = importoGettoneDto.validoDal
		/*controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore*/
		if(importoGettone.version != importoGettoneDto.version) throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		importoGettone = importoGettone.save ()

		return 	importoGettone.toDTO()
    }

	public void elimina (ImportoGettoneDTO importoGettoneDto) {
		ImportoGettone importoGettone = ImportoGettone.get(importoGettoneDto.id)
		/*controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore*/
		if(importoGettone.version != importoGettoneDto.version) throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		importoGettone.delete(failOnError: true)
	}

}
