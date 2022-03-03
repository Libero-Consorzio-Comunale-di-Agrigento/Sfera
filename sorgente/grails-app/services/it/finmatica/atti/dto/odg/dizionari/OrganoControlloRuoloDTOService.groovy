package it.finmatica.atti.dto.odg.dizionari

import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.odg.dizionari.OrganoControllo
import it.finmatica.atti.odg.dizionari.OrganoControlloRuolo

class OrganoControlloRuoloDTOService {

    public OrganoControlloRuoloDTO salva(OrganoControlloRuoloDTO organoControlloRuoloDTO , OrganoControlloDTO organoControlloDTO) {
		OrganoControlloRuolo organoControlloRuolo = OrganoControlloRuolo.get(organoControlloRuoloDTO.id)?:new OrganoControlloRuolo()
		if(organoControlloRuolo.version != organoControlloRuoloDTO.version) {
			throw new AttiRuntimeException(AttiRuntimeException.ERRORE_MODIFICA_CONCORRENTE)
		}
		OrganoControllo organoControllo 		  = OrganoControllo.get(organoControlloDTO.id)?:new OrganoControllo()
		organoControlloRuolo.titolo 			  = organoControlloRuoloDTO.titolo
		organoControlloRuolo.descrizione		  = organoControlloRuoloDTO.descrizione
		organoControlloRuolo.valido 			  = organoControlloRuoloDTO.valido
		organoControlloRuolo.organoControllo 	  = organoControllo
		organoControlloRuolo.save (failOnError: true)
		return organoControlloRuolo.toDTO()
    }

	public void elimina (OrganoControlloRuoloDTO organoControlloRuoloDTO) {
		OrganoControlloRuolo.get(organoControlloRuoloDTO.id).delete()
	}
}
