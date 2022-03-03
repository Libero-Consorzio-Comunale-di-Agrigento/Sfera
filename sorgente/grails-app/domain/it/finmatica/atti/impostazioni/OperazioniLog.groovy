package it.finmatica.atti.impostazioni

import it.finmatica.ad4.autenticazione.Ad4Utente
import org.apache.commons.lang.builder.HashCodeBuilder

class OperazioniLog implements Serializable {

	Long idDocumento
	String tipoOggetto
	Date dataOperazione
	String pagina
	String operazione
	String descrizione
	Ad4Utente utente

    static constraints = {
		descrizione nullable: true
    }

	static mapping = {
		table 			'operazioni_log'
		id 				column: 'id_log'
		utente			column: 'utente'
		descrizione length: 4000
	}

	public boolean equals(other) {
		if (!(other instanceof OperazioniLog)) {
			return false
		}

		return (other.id == id)
	}

	public int hashCode() {
		def builder = new HashCodeBuilder()
		builder.toHashCode()
	}
}


