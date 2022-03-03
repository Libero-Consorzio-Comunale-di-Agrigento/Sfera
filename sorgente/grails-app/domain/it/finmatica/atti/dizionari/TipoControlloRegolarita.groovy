package it.finmatica.atti.dizionari

import it.finmatica.so4.struttura.So4Amministrazione;
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.Delibera
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
class TipoControlloRegolarita {

	Long				sequenza
	String  			titolo
	String				ambito
	So4Amministrazione 	ente
	boolean 			valido = true
	Long 				version

	static mapping = {
		table 		'tipi_controllo_reg'
		id 			column: 'id_tipo_controllo_regolarita'
		ente 		column: 'ente'
		ambito		inList: [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO]
		valido 		type: 'yes_no'
		version		false
	}

	private SpringSecurityService getSpringSecurityService () {		return Holders.applicationContext.getBean("springSecurityService")    }

	def beforeValidate() {
		ente		=	ente?:springSecurityService.principal.amministrazione
	}

	def beforeInsert() {
		ente		=	ente?:springSecurityService.principal.amministrazione
	}

	def beforeUpdate() {
	}

	static hibernateFilters = {
		multiEnteFilter (condition: 'ente = :enteCorrente', type:'string')
	}
}