package it.finmatica.atti.impostazioni

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import org.apache.commons.lang.builder.HashCodeBuilder

class Preferenza implements Serializable {

	public static final transient String UNITA_DEFAULT 	= "UNITA_DEFAULT"

	Long id
	String codice
	String etichetta
	String descrizione
	String ente
	String nomeMetodo
	String valoreDefault

    static constraints = {
		valoreDefault 	nullable: true
		descrizione 	nullable: true
    }

	static mapping = {
		table 			'preferenze'
		id 				column: 'id'
		nomeMetodo		column: 'nome_metodo'
		descrizione 	length: 4000
		version			false
	}

	public boolean equals(other) {
		if (!(other instanceof Preferenza)) {
			return false
		}

		return (other.id == id)
	}
	private SpringSecurityService getSpringSecurityService () {
		return Holders.applicationContext.getBean("springSecurityService")
	}

	def beforeValidate () {
		ente = ente ?: springSecurityService.principal.amministrazione
	}

	def beforeInsert () {
		ente = springSecurityService.principal.amministrazione
	}

	public int hashCode() {
		def builder = new HashCodeBuilder()
		builder.toHashCode()
	}
}


