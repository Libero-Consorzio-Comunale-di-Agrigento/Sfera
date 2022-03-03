package it.finmatica.atti.dizionari

import it.finmatica.atti.exceptions.AttiRuntimeException

class RegistroService {

	void numera (TipoRegistro tipoRegistro, Date data, Closure assegnaNumero) {
		Registro registro 			= getRegistroValido (tipoRegistro, data)
		registro.ultimoNumero 		= registro.ultimoNumero + 1;
		registro.dataUltimoNumero 	= Calendar.getInstance().getTime()
		registro.save()

		assegnaNumero (registro.ultimoNumero, registro.anno, registro.dataUltimoNumero, registro)
	}

    void numera (TipoRegistro tipoRegistro, Closure assegnaNumero) {
		numera (tipoRegistro, Calendar.getInstance().getTime(), assegnaNumero)
    }

	/**
	 * Ritorna il registro valido per la data richiesta.
	 * 
	 * Se il registro richiesto è presente ed è valido, viene ritornato.
	 * Se il registro richiesto è presente ma non valido, viene restituito errore.
	 * Se il registro richiesto non è presente ed è configurato per essere creato automaticamente, viene creato e ritornato, altrimenti viene lanciato un errore. 
	 * 
	 * @param tipoRegistro
	 * @param data
	 * @return
	 */
	Registro getRegistroValido (TipoRegistro tipoRegistro, Date data) {
		
		// l'anno per cui voglio il registro
		int annoRegistro = data.toCalendar().get(Calendar.YEAR)

		// cerco il registro valido per l'anno richiesto
		Registro registro = (Registro) Registro.createCriteria().get {
			eq ("tipoRegistro", tipoRegistro)
			eq ("anno", annoRegistro)
			eq ("valido", true)
			delegate.lock(true)	// utilizzo la select for update
		}
		
		// se il registro non esiste, lo creo se possibile altrimenti do' errore
		if (registro == null) {
			registro = (Registro) Registro.createCriteria().get {
				eq ("tipoRegistro", tipoRegistro)
				eq ("anno", annoRegistro)
				eq ("valido", false)
				delegate.lock(true)	// utilizzo la select for update
			}

            // se il registro è presente ma non è valido, ritorno un errore
            if (registro != null) {
                throw new AttiRuntimeException("Il registro ${tipoRegistro.codice} per l'anno ${annoRegistro} esiste ma è chiuso.")
            } else if (tipoRegistro.automatico) {
				return rinnovaRegistro (tipoRegistro, annoRegistro)
			} else {
				throw new AttiRuntimeException("Non esiste nessun registro con codice ${tipoRegistro.codice} per l'anno ${annoRegistro}, e il tipo di registro non è rinnovabile automaticamente.")
			}
		}
		
		// infine, se il registro è presente e valido, lo ritorno
		return registro
	}

	Registro rinnovaRegistro (TipoRegistro tipoRegistro, int anno) {
		chiudiUltimoRegistro (tipoRegistro)

		return apriRegistro (tipoRegistro, anno, 0, new Date())
	}

	Registro apriRegistro (TipoRegistro tipoRegistro, int anno, int numero, Date data) {
		return new Registro (tipoRegistro: 		tipoRegistro
							, anno:				anno
							, ultimoNumero: 	numero
							, valido:			true
							, dataUltimoNumero: data).save()
	}

	Registro chiudiRegistro (TipoRegistro tipoRegistro, int anno) {
		Registro registro = Registro.createCriteria().get {
			eq ("tipoRegistro", tipoRegistro)
			eq ("valido", true)
			eq ("anno", anno)
			lock true	// utilizzo la select for update
		}

		if (registro != null) {
			registro.valido = false;
			registro.save()
		}

		return registro
	}

	private Registro chiudiUltimoRegistro (TipoRegistro tipoRegistro) {

		// se il registro non ha la chiusura automatica, non lo chiudo.
		if (!tipoRegistro.chiusuraAutomatica) {
			return null;
		}

		int ultimoAnno = Registro.createCriteria().get {
			projections {
				max("anno")
			}
			eq ("tipoRegistro", tipoRegistro)
			eq ("valido", true)
		}?:-1

		// se non c'è nessun registro da chiudere:
		if (ultimoAnno < 0)
			return null;

		return chiudiRegistro (tipoRegistro, ultimoAnno);
	}

	Registro riapriRegistro (Registro registro) {
		Registro r = Registro.createCriteria().get {
			eq ("anno", registro.anno)
			eq ("tipoRegistro", registro.tipoRegistro)
			eq ("valido", true)
		}

		if (r != null) {
			throw new AttiRuntimeException("Non è possibile riaprire il registro richiesto perché esiste già un registro valido per l'anno ${registro.anno}.")
		}

		registro.valido = true
		registro.save(failOnError: true)

		return registro
	}
}
