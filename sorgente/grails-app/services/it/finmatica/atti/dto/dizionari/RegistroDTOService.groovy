package it.finmatica.atti.dto.dizionari

import it.finmatica.atti.dizionari.Registro
import it.finmatica.atti.dizionari.TipoRegistro
import org.zkoss.util.resource.Labels

class RegistroDTOService {

	def registroService

    public RegistroDTO salva(RegistroDTO registroDto){
		Registro registro = Registro.get(registroDto.id)?:new Registro()

		//controllo concorrenza
		if (registro.version != registroDto.version)
			throw new RuntimeException (Labels.getLabel("dizionario.recordModificatoAltroUtente"))


		registro.anno = registroDto.anno
		registro.ultimoNumero = registroDto.ultimoNumero
		registro.dataUltimoNumero = new Date()
		registro.valido = registroDto.valido
		registro.tipoRegistro = TipoRegistro.get(registroDto.tipoRegistro.codice)
		registro = registro.save()
		return registro.toDTO()
	}

	public void elimina(RegistroDTO registroDto){
		Registro registro = Registro.get(registroDto.id)
		//controllo concorrenza
		if (registro.version != registroDto.version)
			throw new RuntimeException (Labels.getLabel("dizionario.recordModificatoAltroUtente"))

		registro.delete()
	}

	public void chiudiRegistro (RegistroDTO registro) {
		Registro r = registro.domainObject
		registroService.chiudiRegistro(r.tipoRegistro, registro.anno)
	}

	public void riapriRegistro (RegistroDTO registro) {
		Registro r = registro.domainObject
		registroService.riapriRegistro(r)
	}
}
