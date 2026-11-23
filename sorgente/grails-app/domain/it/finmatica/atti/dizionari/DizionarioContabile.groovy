package it.finmatica.atti.dizionari

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.so4.struttura.So4Amministrazione
import org.apache.commons.lang.builder.HashCodeBuilder

/**
 * Rappresenta i dizionari contabili (definiti per ATS PAVIA)
 *
 * @author mfrancesconi
 *
 */
class DizionarioContabile implements Serializable {

	public static final String BILANCIO="BILANCIO"
	public static final String CONTO="CONTO"
	public static final String SOTTOBUDGET="SOTTOBUDGET"

	String tipo
	String codice
	String descrizione

	boolean valido = true
	int     sequenza = 1

	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd

	static mapping = {
		table 			'dizionari_contabili'
		version 	false
		id 				generator: 'assigned', composite: ['tipo', 'codice', 'ente']
		codice			column: 'codice'

		descrizione 	column: 'descrizione', length: 4000
		valido 			type:   'yes_no'
		sequenza		column: 'sequenza'
		ente 			column: 'ente'
		dateCreated 	column: 'data_ins'
		utenteIns 		column: 'utente_ins'
		lastUpdated 	column: 'data_upd'
		utenteUpd 		column: 'utente_upd'
	}

    static constraints = {
		descrizione 	nullable: true
		sequenza 		nullable: true
    }

	private SpringSecurityService getSpringSecurityService () {
		return Holders.applicationContext.getBean("springSecurityService")
	}

	def beforeValidate() {
		utenteIns	=	utenteIns?:springSecurityService.currentUser
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
		ente		=	ente?:springSecurityService.principal.amministrazione
	}

	def beforeInsert() {
		utenteIns	=	utenteIns?:springSecurityService.currentUser
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
		ente		=	ente?:springSecurityService.principal.amministrazione
	}

	def beforeUpdate() {
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}

	static hibernateFilters = {
		multiEnteFilter (condition: 'ente = :enteCorrente', type:'string')
	}

	int hashCode() {
		def builder = new HashCodeBuilder()
		if (tipo) builder.append(tipo)
		if (codice) builder.append(codice)
		if (ente) builder.append(ente)
		builder.toHashCode()
	}
}
