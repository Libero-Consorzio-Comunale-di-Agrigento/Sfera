package it.finmatica.atti.odg

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.dizionari.Delega
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.odg.dizionari.Esito

/**
 * Rappresenta un oggetto (proposta di delibera o determina) discusso in una seduta di una commissione
 *
 * @author mfrancesconi
 *
 */
class OggettoSeduta {
	Seduta 				seduta
	Determina 			determina
	PropostaDelibera 	propostaDelibera

	static hasOne = [delibera: Delibera]

	Esito 			esito
	Delega 			delega
	FileAllegato 	fileAllegato

	Date 	dataDiscussione
	String 	oraDiscussione
	String 	note

	int sequenzaConvocazione
	int sequenzaDiscussione

	boolean eseguibilitaImmediata = false
	String motivazioniEseguibilita

	boolean notificato 			 = false
	boolean oggettoAggiuntivo 	 = false
	boolean confermaEsito	 	 = false

	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd

	static mapping = {
		table 'odg_oggetti_seduta'
		id 				 		column: 'id_oggetto_seduta'
		seduta 			 		column: 'id_seduta'			, index: 'odgoggsed_sed_fk'
		determina 		 		column: 'id_determina'			, index: 'odgoggsed_det_fk'
		propostaDelibera 		column: 'id_proposta_delibera'	, index: 'odgoggsed_prodel_fk'
		esito 			 		column: 'id_esito'
		delega 			 		column: 'id_delega'			, index: 'odgoggsed_deleghe_fk'
		fileAllegato 	 		column: 'id_allegato'
		motivazioniEseguibilita column: 'motivazioni_eseguibilita'

		oraDiscussione 	length: 5
		note 			length: 4000

		eseguibilitaImmediata type: 'yes_no'
		notificato 			 type: 'yes_no'
		oggettoAggiuntivo 	 type: 'yes_no'
		confermaEsito	 	 type: 'yes_no'

		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'
	}

    static constraints = {
		determina 		 		nullable: true
		propostaDelibera 		nullable: true
		esito 			 		nullable: true
		delega 			 		nullable: true
		fileAllegato 	 		nullable: true
		dataDiscussione  		nullable: true
		oraDiscussione 	 		nullable: true
		note 			 		nullable: true
		delibera         		nullable: true
		motivazioniEseguibilita nullable: true
    }

	static transients = ['dataOraDiscussione']
	private SpringSecurityService getSpringSecurityService () {
		return Holders.applicationContext.getBean("springSecurityService")
	}

	Date getDataOraDiscussione () {
		return AttiUtils.dataOra(dataDiscussione, oraDiscussione);
	}

	void setDataOraDiscussione (Date data, String ora) {
		this.dataDiscussione = AttiUtils.dataOra(data, ora);
		this.oraDiscussione	 = ora;
	}

	def beforeValidate() {
		utenteIns	=	utenteIns?:springSecurityService.currentUser
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}

	def beforeInsert() {
		utenteIns	=	utenteIns?:springSecurityService.currentUser
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}

	def beforeUpdate() {
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}
}
