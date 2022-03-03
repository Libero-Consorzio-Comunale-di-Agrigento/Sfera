package it.finmatica.atti.dto.dizionari


import it.finmatica.atti.dizionari.OggettoRicorrente
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.exceptions.AttiRuntimeException


class OggettoRicorrenteDTOService {

    public OggettoRicorrenteDTO salva (OggettoRicorrenteDTO oggettoRicorrenteDto) {
		OggettoRicorrente oggettoRicorrente = OggettoRicorrente.get(oggettoRicorrenteDto.id)?:new OggettoRicorrente()

		/*controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore*/
		if(oggettoRicorrente.version != oggettoRicorrenteDto.version) {
			throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		}

		oggettoRicorrente.oggetto = oggettoRicorrenteDto.oggetto?.toUpperCase()
		oggettoRicorrente.determina = oggettoRicorrenteDto.determina
		oggettoRicorrente.delibera = oggettoRicorrenteDto.delibera
		oggettoRicorrente.valido = oggettoRicorrenteDto.valido
		oggettoRicorrente.codice = oggettoRicorrenteDto.codice?.toUpperCase()
		oggettoRicorrente.cigObbligatorio = oggettoRicorrenteDto.cigObbligatorio
		oggettoRicorrente.servizioFornitura = oggettoRicorrenteDto.servizioFornitura
		oggettoRicorrente.tipo = oggettoRicorrenteDto.tipo
		oggettoRicorrente.norma = oggettoRicorrenteDto.norma
		oggettoRicorrente.modalita = oggettoRicorrenteDto.modalita

		oggettoRicorrente = oggettoRicorrente.save (failOnError: true)

		return 	oggettoRicorrente.toDTO()
    }

	public void elimina (OggettoRicorrenteDTO oggettoRicorrenteDto) {
		OggettoRicorrente oggettoRicorrente = OggettoRicorrente.get(oggettoRicorrenteDto.id)
		if (oggettoRicorrente.version != oggettoRicorrenteDto.version) throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
		/*controllo che la versione del DTO sia = a quella appena letta su db: se uguali ok, altrimenti errore*/

		if (!isUtilizzato(oggettoRicorrenteDto)) {
			oggettoRicorrente.delete(failOnError: true)
		}
		else {
			oggettoRicorrente.valido = false
			oggettoRicorrente.save (failOnError: true)
		}
	}

	public boolean isUtilizzato(OggettoRicorrenteDTO oggettoRicorrenteDTO){
		if (oggettoRicorrenteDTO.delibera) {
			def proposte = PropostaDelibera.createCriteria().list {
				eq("oggettoRicorrente", oggettoRicorrenteDTO.domainObject)
			}
			if (proposte.size() > 0) return true;
		}
		if (oggettoRicorrenteDTO.determina) {
			def proposte = Determina.createCriteria().list {
				eq("oggettoRicorrente", oggettoRicorrenteDTO.domainObject)
			}
			if (proposte.size() > 0) return true;
		}
		return false;
	}

}
