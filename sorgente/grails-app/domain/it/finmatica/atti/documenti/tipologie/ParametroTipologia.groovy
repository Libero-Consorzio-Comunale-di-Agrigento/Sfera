package it.finmatica.atti.documenti.tipologie

import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.gestioneiter.configuratore.dizionari.WkfGruppoStep;
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgStep

class ParametroTipologia {

//	WkfCfgStep 		cfgStep
	WkfGruppoStep	gruppoStep

	String codice
	String valore

	static belongsTo = [tipoDetermina: TipoDetermina, tipoDelibera: TipoDelibera]

	static mapping = {
		table 					'parametri_tipologie'
		id 				column: 'id_parametro_tipologia'
		tipoDetermina	column: 'id_tipo_determina'
		tipoDelibera	column: 'id_tipo_delibera'
		gruppoStep		column: 'id_gruppo_step'
	}

	static constraints = {
		valore 			nullable: true  // Può essere null per gestire il caso in cui un flusso
										// prevede un parametro, ma non si vuole utilizzarlo in
										// una determinata tipologia (perchè il documento non passerà
										// mai nello step in cui il parametro è utilizzato)
		tipoDetermina	nullable: true
		tipoDelibera	nullable: true
		gruppoStep		nullable: true  // FIXME: solo per test!!!
	}

	public static String getValoreParametro (def tipologia, WkfCfgStep cfgStep, String codice) {
		String propertyName = null;
		if (tipologia instanceof TipoDelibera) {
			propertyName = "tipoDelibera"
		} else if (tipologia instanceof TipoDetermina) {
			propertyName = "tipoDetermina"
		} else {
			throw new AttiRuntimeException ("Attenzione! Tipologia di documento non riconosciuta: ${tipologia?.class}")
		}

		if (cfgStep.gruppoStep == null) {
			throw new AttiRuntimeException ("Attenzione! Configurazione Errata! È necessario specificare un Gruppo Step per ottenere i parametri delle azioni!")
		}

		ParametroTipologia p = ParametroTipologia.createCriteria().get {
			eq (propertyName, 	tipologia)
			eq ("gruppoStep.id",cfgStep.gruppoStep.id)
			eq ("codice", 		codice)
		}

		return p?.valore
	}

	public static Collection<String> getValoriParametri (def tipologia, String codice) {
		String propertyName = null;
		if (tipologia instanceof TipoDelibera) {
			propertyName = "tipoDelibera"
		} else if (tipologia instanceof TipoDetermina) {
			propertyName = "tipoDetermina"
		} else {
			throw new AttiRuntimeException ("Attenzione! Tipologia di documento non riconosciuta: ${tipologia?.class}")
		}

		return ParametroTipologia.createCriteria().list {
			projections {
				distinct("valore")
			}
			eq (propertyName, 	tipologia)
			eq ("codice", 		codice)
		}
	}
}
