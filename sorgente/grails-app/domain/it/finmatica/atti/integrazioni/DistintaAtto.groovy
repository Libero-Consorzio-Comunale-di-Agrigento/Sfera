package it.finmatica.atti.integrazioni

class DistintaAtto implements Serializable {

	Integer annoDistinta
	Integer numeroDistinta
	Date dataDistinta
	String tipoDist
	String tipoDistDescri
	Date scadenzaDal
	Date scadenzaAl
	Integer annoDel
	Integer numeroDel
	Long idProposta
	String unitaProponente
	Integer annoProposta
	Integer numeroProposta
	String elencoImpegni
	String elencoFornitori
	BigDecimal importo

	static mapping = {	
		table 		'distinte_atti'
		version false
		id 		generator: 'assigned', composite: ['annoProposta', 'numeroProposta', 'unitaProponente']
	}

}
