package it.finmatica.atti.impostazioni

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.so4.struttura.So4Amministrazione

class MappingIntegrazione {

	public static final String VALORE_INTERNO_TUTTI = "*"

	// codice che aiuta a identificare l'ambito/integrazione relativo, ad es: CONTABILITA_COMUNE_MODENA, PROTOCOLLO_COMUNE_MODENA
	String categoria

	// codice che identifica il valore da mappare
	String codice

	// indica il codice o "id" interno di Sfera (ad es il codice dell'unità di so4) a cui corrisponde un valore esterno
	String valoreInterno

	// il valore dell'applicativo esterno da associare al nostro codice interno. (ad es. l'id dell'unità presso l'applicativo esterno)
	String valoreEsterno

	// sequenza in cui ordinare i risultati per i campi che possono assumere più valori
	int sequenza

	// descrizione del valore per i campi che possono assumere più valori
	String descrizione

	So4Amministrazione ente

	static mapping = {
		table 'mapping_integrazioni'
		version false

		id column: 'id_mapping_integrazione'

		ente 		column: 'ente'
	}

    static constraints = {
		descrizione nullable: true
    }

	private SpringSecurityService getSpringSecurityService () {
		Holders.grailsApplication.mainContext.getBean('springSecurityService')
	}

	def beforeValidate () {
		ente = ente?:springSecurityService.principal.amministrazione
	}

	def beforeInsert () {
		ente = springSecurityService.principal.amministrazione
	}

	def beforeUpdate () {
		ente = springSecurityService.principal.amministrazione
	}

	static hibernateFilters = {
		multiEnteFilter (condition: "ente = :enteCorrente", types: 'string')
	}

	static int getValoreEsternoInt (String categoria, String codice, String valoreInterno = VALORE_INTERNO_TUTTI) {
		String value = getValoreEsterno (categoria, codice, valoreInterno);
		return Integer.parseInt(value);
	}

	static boolean getValoreEsternoBoolean (String categoria, String codice, String valoreInterno = VALORE_INTERNO_TUTTI, String valoreDefault = null) {
		String value = getValoreEsterno (categoria, codice, valoreInterno, valoreDefault);
		return "Y".equalsIgnoreCase(value)
	}

	static String getValoreEsterno (String categoria, String codice, String valoreInterno = VALORE_INTERNO_TUTTI) {
		MappingIntegrazione map = MappingIntegrazione.findByCategoriaAndCodiceAndValoreInterno(categoria, codice, valoreInterno)

		if (map == null) {			
			throw new AttiRuntimeException ("Non ho trovato il valore corrispondente al codice ${categoria}.${codice} per il valore interno $valoreInterno")
		}

		return map.valoreEsterno
	}

	static String getValoreEsterno (String categoria, String codice, String valoreInterno, String valoreDefault) {
		MappingIntegrazione map = MappingIntegrazione.findByCategoriaAndCodiceAndValoreInterno(categoria, codice, valoreInterno)

		if (map == null) {
			return valoreDefault
		}

		return map.valoreEsterno
	}

    static List<CodiceDescrizione> getValoriEsterni (String categoria, String codice, String valoreInterno = VALORE_INTERNO_TUTTI) {
        return MappingIntegrazione.findAllByCategoriaAndCodiceAndValoreInterno(categoria, codice, valoreInterno, [sort:"sequenza", order:"asc"]).collect { new CodiceDescrizione(codice:it.valoreEsterno, descrizione:it.descrizione) }
    }
}
