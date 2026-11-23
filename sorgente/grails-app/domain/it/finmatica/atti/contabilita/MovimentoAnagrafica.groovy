package it.finmatica.atti.contabilita

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.documenti.*
import it.finmatica.so4.struttura.So4Amministrazione

class MovimentoAnagrafica {
	Determina determina
	PropostaDelibera propostaDelibera
	long idEsterno  //chiave applicativo contabilità
	String ragioneSociale
	String cognome
	String nome
	String codiceFiscale
	String partitaIva
	String tipo //persona fisica - giuridica

	Date dataNascita
	String comuneNascita
	String siglaProvinciaNascita

	String comune
	String cap
	String siglaProvincia
	String email
	String telefono
	String cellulare
	String indirizzo
	String civico
	String tipoIndirizzo // DOMICILIO  o RESIDENZA
	String idApplicativoEsterno
	String codiceApplicativoEsterno

	// i soliti dati...
	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd

	static mapping = {
		id							column: 'id_movimento_anagrafica'
		table						 'movimenti_anagrafica'
		determina 					column: 'id_determina'
		propostaDelibera 			column: 'id_proposta_delibera'

		idEsterno 					column: 'id_esterno'
		ragioneSociale 				column: 'ragione_sociale'
		cognome 					column: 'cognome'
		nome 						column: 'nome'
		tipo						column: 'tipo'
		codiceFiscale 				column: 'codice_fiscale'
		partitaIva 					column: 'partita_iva'
		dataNascita					column: 'data_nascita'
		comuneNascita				column: 'comune_nascita'
		siglaProvinciaNascita 		column: 'sigla_provincia_nascita'
		comune 						column: 'comune'
		cap 						column: 'cap'
		siglaProvincia 				column: 'sigla_provincia'
		email 						column: 'email'
		telefono 					column: 'telefono'
		cellulare 					column: 'cellulare'
		tipoIndirizzo				column: 'tipo_indirizzo'
		indirizzo					column:	'indirizzo'
		civico						column: 'civico'
		idApplicativoEsterno 		column: 'id_applicativo_esterno'
		codiceApplicativoEsterno	column: 'codice_applicativo_esterno'
		ente 						column: 'ente'
		dateCreated 				column: 'data_ins'
		utenteIns 					column: 'utente_ins'
		lastUpdated 				column: 'data_upd'
		utenteUpd 					column: 'utente_upd'
	}

	static constraints = {
		determina 					nullable: true
		propostaDelibera 			nullable: true

		idEsterno 					nullable: true
		ragioneSociale 				nullable: true
		cognome 					nullable: true
		nome 						nullable: true
		tipo						nullable: true
		codiceFiscale 				nullable: true
		partitaIva 					nullable: true
		dataNascita					nullable: true
		comuneNascita				nullable: true
		siglaProvinciaNascita		nullable: true
		comune 						nullable: true
		cap 						nullable: true
		siglaProvincia 				nullable: true
		email 						nullable: true
		telefono 					nullable: true
		cellulare 					nullable: true
		tipoIndirizzo				nullable: true
		indirizzo					nullable: true
		civico						nullable: true
		idApplicativoEsterno 		nullable: true
		codiceApplicativoEsterno 	nullable: true
    }

	private SpringSecurityService getSpringSecurityService() {
		return Holders.getApplicationContext().getBean("springSecurityService")
	}

	def beforeValidate () {
		utenteIns = utenteIns?:springSecurityService.currentUser
		ente	  = ente?:springSecurityService.principal.amministrazione
		utenteUpd = utenteUpd?:springSecurityService.currentUser
	}

	def beforeInsert () {
		utenteIns = springSecurityService.currentUser
		utenteUpd = springSecurityService.currentUser
		ente	  = springSecurityService.principal.amministrazione
	}

	def beforeUpdate () {
		utenteUpd = springSecurityService.currentUser
	}

	static hibernateFilters = {
		multiEnteFilter (condition: "ente = :enteCorrente", types: 'string')
	}

	public IProposta getProposta() {
		determina ?: propostaDelibera
	}

	public IAtto getAtto() {
		determina ?: Delibera.findByPropostaDelibera(propostaDelibera)
	}
}
