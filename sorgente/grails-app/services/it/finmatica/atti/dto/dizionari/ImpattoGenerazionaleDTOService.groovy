package it.finmatica.atti.dto.dizionari

import it.finmatica.atti.dizionari.ImpattoGenerazionale
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.exceptions.AttiRuntimeException

class ImpattoGenerazionaleDTOService {

    public ImpattoGenerazionaleDTO salva (ImpattoGenerazionaleDTO impattoGenerazionaleDto) {
		ImpattoGenerazionale impattoGenerazionale = ImpattoGenerazionale.get(impattoGenerazionaleDto.id)?:new ImpattoGenerazionale()

		/*controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore*/
		if(impattoGenerazionale.version != impattoGenerazionaleDto.version) {
			throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		}

		impattoGenerazionale.titolo = impattoGenerazionaleDto.titolo?.toUpperCase()
		impattoGenerazionale.determina = impattoGenerazionaleDto.determina
		impattoGenerazionale.delibera = impattoGenerazionaleDto.delibera
		impattoGenerazionale.valido = impattoGenerazionaleDto.valido
		impattoGenerazionale.descrizione = impattoGenerazionaleDto.descrizione
		impattoGenerazionale.frase = impattoGenerazionaleDto.frase

		impattoGenerazionale = impattoGenerazionale.save (failOnError: true)

		return 	impattoGenerazionale.toDTO()
    }

	public void elimina (ImpattoGenerazionaleDTO impattoGenerazionaleDto) {
		ImpattoGenerazionale impattoGenerazionale = ImpattoGenerazionale.get(impattoGenerazionaleDto.id)
		if (impattoGenerazionale.version != impattoGenerazionaleDto.version) throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		/*controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore*/

		if (!isUtilizzato(impattoGenerazionaleDto)) {
			impattoGenerazionale.delete(failOnError: true)
		}
		else {
			impattoGenerazionale.valido = false
			impattoGenerazionale.save (failOnError: true)
		}
	}

	/*public boolean isUtilizzato(ImpattoGenerazionaleDTO impattoGenerazionaleDTO){
		if (impattoGenerazionaleDTO.delibera) {
			def proposte = PropostaDelibera.createCriteria().list {
				eq("impattoGenerazionale", impattoGenerazionaleDTO.domainObject)
			}
			if (proposte.size() > 0) return true;
		}
		if (impattoGenerazionaleDTO.determina) {
			def proposte = Determina.createCriteria().list {
				eq("impattoGenerazionale", impattoGenerazionaleDTO.domainObject)
			}
			if (proposte.size() > 0) return true;
		}
		return false;
	}*/

}
