package it.finmatica.atti.impostazioni

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import org.apache.commons.lang.builder.HashCodeBuilder

class PreferenzaUtente {

	Long id
	Preferenza preferenza
	Ad4Utente utente
	String valore

    static constraints = {
    }

	static mapping = {
		table 			'preferenze_utente'
		id 				column: 'id'
		utente			column: 'utente'
		preferenza		column: 'id_preferenza'
		valore 			length: 4000
		version			false
	}

	public boolean equals(other) {
		if (!(other instanceof PreferenzaUtente)) {
			return false
		}

		return (other.id == id)
	}

	public int hashCode() {
		def builder = new HashCodeBuilder()
		builder.toHashCode()
	}
}


