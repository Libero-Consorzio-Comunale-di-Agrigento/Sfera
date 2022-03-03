package it.finmatica.atti.dto.documenti.tipologie

import it.finmatica.atti.dto.impostazioni.CaratteristicaTipoSoggettoDTO
import it.finmatica.atti.dto.impostazioni.CaratteristicaTipologiaDTO
import it.finmatica.atti.impostazioni.CaratteristicaTipoSoggetto
import it.finmatica.atti.impostazioni.CaratteristicaTipologia
import it.finmatica.atti.impostazioni.TipoSoggetto

class CaratteristicaTipologiaDTOService {

    public CaratteristicaTipologiaDTO salva (CaratteristicaTipologiaDTO caratteristicaDTO, List<CaratteristicaTipoSoggettoDTO> tipiSoggettoDTO = null) {
		CaratteristicaTipologia caratteristica = CaratteristicaTipologia.get(caratteristicaDTO.id)?:new CaratteristicaTipologia()

		caratteristica.titolo 		= caratteristicaDTO.titolo
		caratteristica.descrizione 	= caratteristicaDTO.descrizione
		caratteristica.tipoOggetto 	= caratteristicaDTO.tipoOggetto?.domainObject
		caratteristica.valido 		= caratteristicaDTO.valido
		caratteristica.layoutSoggetti = caratteristicaDTO.layoutSoggetti

		// devo inserire/aggiornare/eliminare i tipiSoggetto di questa caratteristica
		// elimino da caratteristica tutti quelli che non ci sono in tipiSoggettoDTO
		caratteristica.caratteristicheTipiSoggetto.findAll { s -> tipiSoggettoDTO.find { it.tipoSoggetto.codice == s.tipoSoggetto.codice } == null }.each { ts ->
			caratteristica.removeFromCaratteristicheTipiSoggetto (ts)
			ts.delete()
		}

		// aggiorno / inserisco su caratteristica tutti quelli che sono in tipiSoggettoDTO
		for (CaratteristicaTipoSoggettoDTO s : tipiSoggettoDTO) {
			TipoSoggetto tipo = s.tipoSoggetto?.domainObject
			CaratteristicaTipoSoggetto ts = caratteristica.caratteristicheTipiSoggetto.find { it.tipoSoggetto.codice == tipo.codice }

			if (ts == null) {
				ts = new CaratteristicaTipoSoggetto(tipoSoggetto: tipo)
				caratteristica.addToCaratteristicheTipiSoggetto (ts)
			}

			ts.ruolo				= s.ruolo?.domainObject
			ts.regolaCalcoloLista	= s.regolaCalcoloLista?.domainObject
			ts.regolaCalcoloDefault = s.regolaCalcoloDefault?.domainObject
			ts.tipoSoggettoPartenza = s.tipoSoggettoPartenza?.domainObject
		}

		caratteristica.save (failOnError: true)

		return caratteristica.toDTO()
    }

	public void elimina (CaratteristicaTipologiaDTO caratteristicaDTO) {
		caratteristicaDTO.domainObject.delete(failOnError: true)
	}

}
