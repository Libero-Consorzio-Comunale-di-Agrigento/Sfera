package it.finmatica.atti.dto.dizionari

import it.finmatica.atti.dizionari.RegistroUnita
import it.finmatica.atti.exceptions.AttiRuntimeException
import org.zkoss.util.resource.Labels

class RegistroUnitaDTOService {

    public RegistroUnitaDTO salva (RegistroUnitaDTO registroUnitaDto) {
		RegistroUnita registroUnita = registroUnitaDto.getDomainObject()?: new RegistroUnita()

		// controllo concorrenza
		if (registroUnita.version != registroUnitaDto.version) {
			throw new RuntimeException (Labels.getLabel("dizionario.recordModificatoAltroUtente"))
		}

		// controllo che ci sia un solo registroUnitaDto per la tripla registro-unita-caratteristica
		if (!controllaTipoRegistroPerUnita(registroUnitaDto)) {
			throw new AttiRuntimeException(Labels.getLabel("dizionario.soloUnTipoRegistroValidoPerUnitaOrganizzativa"))
		}

		registroUnita.tipoRegistro 	 = registroUnitaDto.tipoRegistro.domainObject;
		registroUnita.unitaSo4 		 = registroUnitaDto.unitaSo4.domainObject;
		registroUnita.valido		 = registroUnitaDto.valido
		registroUnita.caratteristica = registroUnitaDto.caratteristica?.domainObject;
		registroUnita = registroUnita.save()

		return registroUnita.toDTO()
	}

	public boolean controllaTipoRegistroPerUnita (RegistroUnitaDTO registroUnitaDto) {
		boolean controlloCorretto = false

		RegistroUnita regUnita = RegistroUnita.createCriteria().get {
			eq ("valido", true)

			unitaSo4 {
				eq("progr", 		registroUnitaDto.unitaSo4.progr)
				eq("dal", 			registroUnitaDto.unitaSo4.dal)
				eq("ottica.codice", registroUnitaDto.unitaSo4.ottica.codice)
			}

			if (registroUnitaDto.caratteristica != null) {
				eq ("caratteristica.id", registroUnitaDto.caratteristica.id)
			} else {
				isNull ("caratteristica")
			}
		}

		if (regUnita == null || regUnita.id == registroUnitaDto.id) {
			controlloCorretto = true
		}
		return controlloCorretto
	}

   public void elimina(RegistroUnitaDTO registroUnitaDto) {
		RegistroUnita registroUnita = registroUnitaDto.getDomainObject()
		// controllo concorrenza
		if (registroUnita.version != registroUnitaDto.version) {
			throw new AttiRuntimeException (Labels.getLabel("dizionario.recordModificatoAltroUtente"))
		}

		registroUnita.delete()
	}
}
