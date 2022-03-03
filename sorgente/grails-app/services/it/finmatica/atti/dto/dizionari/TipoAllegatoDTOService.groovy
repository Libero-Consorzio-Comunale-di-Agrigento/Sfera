package it.finmatica.atti.dto.dizionari

import it.finmatica.atti.dizionari.TipoAllegato
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.documenti.AllegatiObbligatori

class TipoAllegatoDTOService {

    TipoAllegatoDTO salva (TipoAllegatoDTO tipoAllegatoDto) {
        TipoAllegato tipoAllegato = TipoAllegato.get(tipoAllegatoDto.id) ?: new TipoAllegato()
        tipoAllegato.titolo = tipoAllegatoDto.titolo
        tipoAllegato.descrizione = tipoAllegatoDto.descrizione
        tipoAllegato.valido = tipoAllegatoDto.valido
        tipoAllegato.pubblicaCasaDiVetro = tipoAllegatoDto.pubblicaCasaDiVetro
        tipoAllegato.pubblicaAlbo = tipoAllegatoDto.pubblicaAlbo
        tipoAllegato.pubblicaVisualizzatore = tipoAllegatoDto.pubblicaVisualizzatore
        tipoAllegato.tipologia = tipoAllegatoDto.tipologia
        tipoAllegato.codice = tipoAllegatoDto.codice
        tipoAllegato.modelloTesto = tipoAllegatoDto.modelloTesto?.domainObject
        tipoAllegato.modificabile = tipoAllegatoDto.modificabile
        tipoAllegato.modificaCampi = tipoAllegatoDto.modificaCampi
        tipoAllegato.stampaUnica = tipoAllegatoDto.stampaUnica
        tipoAllegato.statoFirma = tipoAllegatoDto.statoFirma
        tipoAllegato.codiceEsterno = tipoAllegatoDto.codiceEsterno

        if (tipoAllegato.version != tipoAllegatoDto.version) {
            throw new AttiRuntimeException("Un altro utente ha modificato il dato sottostante, operazione annullata!")
        }
        tipoAllegato = tipoAllegato.save()

        return tipoAllegato.toDTO()
    }

    void elimina (TipoAllegatoDTO tipoAllegatoDto) {
        TipoAllegato tipoAllegato = TipoAllegato.get(tipoAllegatoDto.id)
        if (MappingIntegrazione.countByCategoriaAndValoreEsterno(AllegatiObbligatori.MAPPING_CATEGORIA, tipoAllegatoDto.id.toString())> 0){
            throw new AttiRuntimeException("Tipo Allegato utilizzato come allegato obbligatorio da una Tipologia o da una Categoria, operazione annullata!")
        }
        if (tipoAllegato.version != tipoAllegatoDto.version) {
            throw new AttiRuntimeException(
                    "Un altro utente ha modificato il dato sottostante, operazione annullata!")
        }
        tipoAllegato.delete(failOnError: true)
    }

}
