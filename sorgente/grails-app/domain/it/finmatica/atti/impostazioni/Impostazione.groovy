package it.finmatica.atti.impostazioni

import org.apache.commons.lang.builder.HashCodeBuilder

class Impostazione implements Serializable {

	public static final transient String PREFISSO_RUOLO 	= "AGD"

	String  codice
	String 	descrizione
	String 	etichetta
	String 	valore
	String 	predefinito
	String 	caratteristiche
	byte[]	risorsa
	boolean modificabile	= false
	String	ente

    static constraints = {
		caratteristiche nullable: true
		risorsa			nullable: true
    }

	static mapping = {
		table 			'impostazioni'
		id 				composite: ['codice', 'ente'], generator: 'assigned'
		risorsa			sqlType: 'Blob'
		ente 			column:	'ente'
		modificabile 	type: 'yes_no'
		caratteristiche length: 4000
	}

	public boolean equals(other) {
		if (!(other instanceof Impostazione)) {
			return false
		}

		return (other.codice == codice && other.ente == ente)
	}

	public int hashCode() {
		def builder = new HashCodeBuilder()
		if (codice) builder.append(codice)
		if (ente) 	builder.append(ente)
		builder.toHashCode()
	}

	static namedQueries = {
		getImpostazione { String codiceImpostazione, String codiceEnte ->
			or {
				eq ("ente", codiceEnte)
				eq ("ente", ImpostazioniMap.ENTE_FALLBACK)
			}

			eq ("codice", codiceImpostazione)

			order ("ente", "desc") // metto per ultimi i valori con "*"

			maxResults (1)  // se c'è il valore dell'ente giusto, prendo quello, se c'è quello e il "default" *, prendo sempre il primo (cioè quello giusto)
							// se invece non c'è il valore dell'ente specificato, prendo quello con valore "default", se c'è.
		}
	}
}


