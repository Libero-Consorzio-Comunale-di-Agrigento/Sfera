package it.finmatica.atti.dto.dizionari

import it.finmatica.atti.dizionari.RegistroUnita
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.exceptions.AttiRuntimeException
import org.zkoss.util.resource.Labels

class TipoRegistroDTOService {

	public TipoRegistroDTO salva(TipoRegistroDTO tipoRegistroDto) {
		TipoRegistro tipoRegistro = TipoRegistro.get(tipoRegistroDto.codice)?:new TipoRegistro()

		// controllo concorrenza
		if (tipoRegistro.version != tipoRegistroDto.version)
			throw new AttiRuntimeException (Labels.getLabel("dizionario.recordModificatoAltroUtente"))

		if (!tipoRegistroDto.valido && tipoRegistro.valido == true) {
			// se valido passa da true a false => vado a impostare valido = false per tutti i record di registroUnita che utilizzano quel tipoRegistro
			List<RegistroUnita> registroUnitaList = RegistroUnita.findAllByTipoRegistro(tipoRegistro);
			registroUnitaList*.valido = false
			registroUnitaList*.save()
		}

		tipoRegistro.codice 			= tipoRegistroDto.codice
		tipoRegistro.descrizione 		= tipoRegistroDto.descrizione
		tipoRegistro.delibera 			= tipoRegistroDto.delibera
		tipoRegistro.determina 			= tipoRegistroDto.determina
		tipoRegistro.registroEsterno 	= tipoRegistroDto.registroEsterno
		tipoRegistro.visualizzatore		= tipoRegistroDto.visualizzatore
		tipoRegistro.paginaUnica		= tipoRegistroDto.paginaUnica
		tipoRegistro.automatico 		= tipoRegistroDto.automatico
		tipoRegistro.chiusuraAutomatica = tipoRegistroDto.automatico ? tipoRegistroDto.chiusuraAutomatica : false;

		tipoRegistro.valido 			= tipoRegistroDto.valido

		tipoRegistro = tipoRegistro.save()

		return tipoRegistro.toDTO()
	}

	public void elimina(TipoRegistroDTO tipoRegistroDto){
		TipoRegistro tipoRegistro = TipoRegistro.get(tipoRegistroDto.codice)

		// controllo concorrenza
		if (tipoRegistro.version != tipoRegistroDto.version)
			throw new AttiRuntimeException (Labels.getLabel("dizionario.recordModificatoAltroUtente"))

		tipoRegistro.delete()
	}
}
