package it.finmatica.atti.integrazioni

import org.apache.commons.lang.builder.HashCodeBuilder

class AllegatoEsterno implements Serializable {

    DeterminaEsterna determinaEsterna
    PropostaDeliberaEsterna propostaDeliberaEsterna

    // questi sono campi "legacy" dovuti all'integrazione "originale" di lettura di determine da tabella. Probabilmente non più usato da nessuno.
	String idDocumentoAllegato
	String tipoDocumento        // il tipo del documento principale
	String applicativoEsterno
	String idDocumentoEsterno
	String applicativoEsternoDocumento

	String tipoAllegato // identifica il codice della tipologia dell'allegato

	String descrizione
	byte[] fileDocumento
	String nomeFile

	static mapping = {
		table 		'ag_acquisizione_allegati'
		id			column: 'id_allegato_esterno'
		
		applicativoEsternoDocumento column: 'applicativo_esterno'
		applicativoEsterno 			column: 'applicativo_esterno_allegato'
		fileDocumento				sqlType: 'Blob'
		idDocumentoAllegato			column: 'id_doc_allegato'
		idDocumentoEsterno 			column: 'id_doc_esterno'
		nomeFile					column: 'nomefile'
		tipoDocumento				column: 'tipo_documento'
        tipoAllegato                column: 'tipo_allegato'

        determinaEsterna            column: 'id_determina_esterna'
        propostaDeliberaEsterna     column: 'id_proposta_delibera_esterna'
	}
	
	static constraints = {
        idDocumentoAllegato nullable: true
        determinaEsterna    nullable: true
        propostaDeliberaEsterna nullable: true
		idDocumentoEsterno  nullable: true
		applicativoEsterno	nullable: true
		descrizione 	    nullable: true
		fileDocumento		nullable: true
		nomeFile		    nullable: true
		tipoDocumento		nullable: true
		tipoAllegato		nullable: true
	}
	
	public boolean equals(other) {
		if (!(other instanceof AllegatoEsterno)) {
			return false
		}

		return (other.applicativoEsternoDocumento?.equals(applicativoEsternoDocumento) &&
				other.idDocumentoAllegato?.equals(idDocumentoAllegato) &&
				other.idDocumentoEsterno?.equals(idDocumentoEsterno))
	}

	public int hashCode() {
		def builder = new HashCodeBuilder()
		if (idDocumentoEsterno) builder.append(idDocumentoEsterno)
		if (applicativoEsternoDocumento) builder.append(applicativoEsternoDocumento)
		if (idDocumentoAllegato) builder.append(idDocumentoAllegato)
		return builder.toHashCode()
	}
}
