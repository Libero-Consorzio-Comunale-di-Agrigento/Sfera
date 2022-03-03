package it.finmatica.atti.dto.dizionari

import it.finmatica.atti.documenti.tipologie.TipoCertificato
import it.finmatica.atti.dto.documenti.tipologie.TipoCertificatoDTO
import it.finmatica.atti.exceptions.AttiRuntimeException

class TipoCertificatoDTOService {

    public TipoCertificatoDTO salva (TipoCertificatoDTO tipoCertificatoDto) {
		TipoCertificato tipoCertificato 		= TipoCertificato.get(tipoCertificatoDto.id)?:new TipoCertificato()
		tipoCertificato.titolo 					= tipoCertificatoDto.titolo
		tipoCertificato.descrizione 			= tipoCertificatoDto.descrizione
        tipoCertificato.descrizioneNotifica     = tipoCertificatoDto.descrizioneNotifica
		tipoCertificato.caratteristicaTipologia = tipoCertificatoDto?.caratteristicaTipologia?.getDomainObject()
		tipoCertificato.modelloTesto 			= tipoCertificatoDto?.modelloTesto?.getDomainObject()
		tipoCertificato.progressivoCfgIter 		= tipoCertificatoDto?.progressivoCfgIter
		tipoCertificato.valido 					= tipoCertificatoDto.valido

		// controllo che la versione del DTO sia uguale a quella appena letta su db: se uguali ok, altrimenti errore
		if (tipoCertificato.version >= 0 && tipoCertificato.version != tipoCertificatoDto.version) {
			throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		}

		tipoCertificato = tipoCertificato.save ()
		return tipoCertificato.toDTO()
    }

	public void elimina (TipoCertificatoDTO tipoCertificatoDto) {
		tipoCertificatoDto.domainObject.delete ();
	}

	public TipoCertificatoDTO duplica (TipoCertificatoDTO tipoCertificatoDTO) {
		tipoCertificatoDTO.id 		= -1;
		tipoCertificatoDTO.version 	= 0;
		tipoCertificatoDTO.titolo  += " (duplica)";
		TipoCertificato duplica 	= salva(tipoCertificatoDTO).domainObject;
		return duplica.toDTO();
	}
}
