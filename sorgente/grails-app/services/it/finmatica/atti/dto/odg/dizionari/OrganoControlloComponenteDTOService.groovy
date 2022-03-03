package it.finmatica.atti.dto.odg.dizionari

import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.odg.dizionari.OrganoControllo
import it.finmatica.atti.odg.dizionari.OrganoControlloComponente
import it.finmatica.atti.odg.dizionari.OrganoControlloRuolo

class OrganoControlloComponenteDTOService {

    public OrganoControlloComponenteDTO salva(OrganoControlloComponenteDTO organoControlloComponenteDTO , OrganoControlloDTO organoControlloDTO) {
		OrganoControlloComponente organoControlloComponente = OrganoControlloComponente.get(organoControlloComponenteDTO.id)?:new OrganoControlloComponente()
		if(organoControlloComponente.version != organoControlloComponenteDTO.version) {
			throw new AttiRuntimeException(AttiRuntimeException.ERRORE_MODIFICA_CONCORRENTE)
		}
		OrganoControllo organoControllo 					= OrganoControllo.get(organoControlloDTO.id)?:new OrganoControllo()
		organoControlloComponente.componente				= As4SoggettoCorrente.get(organoControlloComponenteDTO.componente.id)
		organoControlloComponente.organoControlloRuolo		= OrganoControlloRuolo.get(organoControlloComponenteDTO.organoControlloRuolo?.id)
		organoControlloComponente.valido 					= organoControlloComponenteDTO.valido
		organoControlloComponente.organoControllo 			= organoControllo
		organoControlloComponente.save (failOnError: true)
		return organoControlloComponente.toDTO()
    }

	public void elimina (OrganoControlloComponenteDTO organoControlloComponenteDTO) {
		OrganoControlloComponente.get(organoControlloComponenteDTO.id).delete()
	}
}
