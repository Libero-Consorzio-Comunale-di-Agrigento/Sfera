package it.finmatica.atti.integrazioni.parametri

import groovy.transform.CompileStatic;

@CompileStatic
class CodiceDescrizione {

	private final String codice
	private final String descrizione

	CodiceDescrizione (String codice, String descrizione) {
		this.codice = codice
		this.descrizione = descrizione
	}

	String getCodice () {
		return codice
	}

	String getDescrizione () {
		return descrizione
	}
}
