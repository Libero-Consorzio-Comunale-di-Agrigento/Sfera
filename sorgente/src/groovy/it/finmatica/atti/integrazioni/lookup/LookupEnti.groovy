package it.finmatica.atti.integrazioni.lookup

import groovy.transform.CompileStatic
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.LookupValori
import it.finmatica.so4.struttura.So4Amministrazione

@CompileStatic
class LookupEnti implements LookupValori {

	public final static LookupEnti INSTANCE = new LookupEnti()

	private LookupEnti () {

	}

	List<CodiceDescrizione> getValori () {
		return Impostazioni.ENTI_SO4.valori.collect { String it -> new CodiceDescrizione(it, So4Amministrazione.get(it).soggetto.denominazione) }
	}
}
