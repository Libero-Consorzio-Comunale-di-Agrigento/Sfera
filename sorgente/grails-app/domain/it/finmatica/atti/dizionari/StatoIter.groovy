package it.finmatica.atti.dizionari

import it.finmatica.so4.struttura.So4Amministrazione
import org.apache.commons.lang.builder.HashCodeBuilder

class StatoIter implements Serializable {
	String tipoDocumento
	String stato
	So4Amministrazione ente

	static mapping = {
		table 			'stati_iter'
		version 	false
		id 				generator: 'assigned', composite: ['tipoDocumento', 'stato', 'ente']
		tipoDocumento	column: 'tipo_documento'
		stato			column: 'stato'
		ente 			column: 'ente'
	}

	int hashCode() {
		def builder = new HashCodeBuilder()
		if (tipoDocumento) builder.append(tipoDocumento)
		if (stato) builder.append(stato)
		if (ente) builder.append(ente)
		builder.toHashCode()
	}
}
