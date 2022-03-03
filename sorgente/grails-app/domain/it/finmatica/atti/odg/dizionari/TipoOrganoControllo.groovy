package it.finmatica.atti.odg.dizionari

import it.finmatica.so4.struttura.So4Amministrazione;
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
class TipoOrganoControllo {

	String  			codice
	String  			titolo
	So4Amministrazione 	ente
	boolean 			valido = true

	static mapping = {
		table 		'tipi_organo_controllo'
		id 			column: 'tipo_organo_controllo', name:'codice', generator: 'assigned'
		codice 		column: 'tipo_organo_controllo'
		ente 		column: 'ente'
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