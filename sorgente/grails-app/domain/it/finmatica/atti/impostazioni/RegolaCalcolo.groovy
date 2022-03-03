package it.finmatica.atti.impostazioni

/**
 * Rappresenta le possibili regole di calcolo utilizzabili per definire i dirigenti, firmatari,...
 * in base alla codifica su so4
 *
 * Per questa domain non verrà realizzata un'interfaccia grafica
 * @author mfrancesconi
 *
 */
class RegolaCalcolo {

	public static final transient String TIPO_DEFAULT 	= "DEFAULT"
	public static final transient String TIPO_LISTA		= "LISTA"

	String categoria	// indica se è per unità o componenti
	String tipo			// indica se ritorna un risultato singolo o multiplo

	String titolo
	String descrizione
	String nomeMetodo
	String nomeBean

	static mapping = {
		table 	'regole_calcolo'
		id 		column: 'id_regola'
		version false
	}

	static constraints = {
		descrizione nullable: true
		categoria	inList: [ TipoSoggetto.CATEGORIA_UNITA, TipoSoggetto.CATEGORIA_COMPONENTE ]
		tipo		inList: [ RegolaCalcolo.TIPO_DEFAULT, 	RegolaCalcolo.TIPO_LISTA ]
	}
}
