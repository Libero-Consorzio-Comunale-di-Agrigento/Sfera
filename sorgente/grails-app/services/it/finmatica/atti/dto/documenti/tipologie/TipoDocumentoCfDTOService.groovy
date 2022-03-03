package it.finmatica.atti.dto.documenti.tipologie

import it.finmatica.atti.documenti.tipologie.TipoDelibera
import it.finmatica.atti.documenti.tipologie.TipoDetermina
import it.finmatica.atti.documenti.tipologie.TipoDocumentoCf
import it.finmatica.atti.exceptions.AttiRuntimeException

class TipoDocumentoCfDTOService {

	TipoDocumentoCfDTO salva (TipoDocumentoCfDTO tipoDocumentoCfDTO) {
		TipoDocumentoCf tipoDocumentoCf 	= tipoDocumentoCfDTO.getDomainObject()?:new TipoDocumentoCf()
		tipoDocumentoCf.tipoDetermina		= TipoDetermina.get(tipoDocumentoCfDTO.tipoDetermina?.id)
		tipoDocumentoCf.tipoDelibera		= TipoDelibera.get(tipoDocumentoCfDTO.tipoDelibera?.id)
		tipoDocumentoCf.cfTipoDocumentoCodice = tipoDocumentoCfDTO.cfTipoDocumentoCodice;

		// controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore
		if (tipoDocumentoCf.version != tipoDocumentoCfDTO.version) {
			throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		}

		tipoDocumentoCf.save()

		return tipoDocumentoCf?.toDTO()
    }

	void elimina (TipoDocumentoCfDTO tipoDocumentoCfDTO) {
		TipoDocumentoCf tipoDocumentoCf = TipoDocumentoCf.get(tipoDocumentoCfDTO.id)
		// controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore
		if (tipoDocumentoCf.version != tipoDocumentoCfDTO.version) {
			throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		}
		tipoDocumentoCf.delete(failOnError: true)
	}

}
