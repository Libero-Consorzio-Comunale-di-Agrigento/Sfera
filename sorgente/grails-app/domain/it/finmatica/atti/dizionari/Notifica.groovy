package it.finmatica.atti.dizionari

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders

import it.finmatica.ad4.autenticazione.Ad4Utente;
import it.finmatica.atti.odg.Commissione;
import it.finmatica.atti.odg.dizionari.OrganoControllo;
import it.finmatica.so4.struttura.So4Amministrazione;

/**
 * Rappresenta una notifica (testo e corpo dell'email) gestita da applicativo
 *
 * Ogni notifica ha questi attributi:
 * - destinatari
 * - quando inviare la notifica
 * - oggetto, testo e allegati
 * - sistema di invio (jwf o mail)
 * - campi disponibili
 *
 */
class Notifica {

	// Modalità di invio della notifica:
	public static final String MODALITA_EMAIL 		= "EMAIL"
	public static final String MODALITA_JWORKLIST 	= "JWORKLIST"
	public static final String MODALITA_PEC		 	= "PEC"

	String 				tipoNotifica
	Commissione 		commissione 	// alcune tipologie di notifica richiedono una commissione di odg
	OrganoControllo 	organoControllo // alcune tipologie di notifica richiedono un organo di controllo
	String 				titolo 			// titolo della notifica

	String 				oggetto   		// oggetto della mail
	String 				testo     		// testo della mail
	String				allegati		// elenco degli allegati della notifica

	String				oggetti			// elenco degli oggetti (separati da # ) per cui questa notifica è disponibile.
	String				modalitaInvio	// come devo inviare la notifica?
	// possibilità di invio della notifica:
	// - email
	// - jworklist


	So4Amministrazione 	ente
	boolean 			valido = true
	Date 				validoDal 	 // da valorizzare alla creazione del record
	Date 				validoAl   	 // deve essere valorizzato con la data di sistema quando valido = false
									 // quando valido = true deve essere null
	Date 				dateCreated
	Ad4Utente 			utenteIns
	Date 				lastUpdated
	Ad4Utente 			utenteUpd

	// a discapito del nome, contiene l'elenco dei vari destinatari, non solo di tipo "email" ma anche di tipo "unità", "ruolo", "soggetto" e "funzione"
	static hasMany = [notificheEmail : NotificaEmail]

	static mapping = {
		table 			'notifiche'
		id column: 		'id_notifica'
		ente 			column: 'ente'
		commissione 	column: 'id_commissione'
		organoControllo column: 'id_organo_controllo'
		oggetto 		length: 4000
		testo 			length: 4000
		valido 			type:   'yes_no'
		dateCreated 	column: 'data_ins'
		utenteIns 		column: 'utente_ins'
		lastUpdated 	column: 'data_upd'
		utenteUpd 		column: 'utente_upd'
	}

	static constraints = {
		commissione 	nullable: true
		organoControllo nullable: true
		testo 			nullable: true
		validoAl 		nullable: true
		allegati		nullable: true
		oggetti			nullable: true
	}

	private SpringSecurityService getSpringSecurityService () {		return Holders.applicationContext.getBean("springSecurityService")    }

	def beforeValidate() {
		validoDal 	= 	validoDal?:new Date()
		utenteIns	=	utenteIns?:springSecurityService.currentUser
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
		ente		=	ente?:springSecurityService.principal.amministrazione

		notificheEmail*.beforeValidate();
	}

	def beforeInsert() {
		validoAl 	= 	valido?null:(validoAl?:new Date())
		validoDal 	= 	new Date()
		utenteIns	=	utenteIns?:springSecurityService.currentUser
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
		ente		=	ente?:springSecurityService.principal.amministrazione
	}

	def beforeUpdate() {
		validoAl 	= 	valido?null:(validoAl?:new Date())
		utenteUpd	=	utenteUpd?:springSecurityService.currentUser
	}

	static hibernateFilters = {
		multiEnteFilter (condition: "ente = :enteCorrente", type:'string')
	}

	static namedQueries = {
		perTipo { String tipoNotifica, String oggetto = null ->
			eq ("tipoNotifica", tipoNotifica)
			eq ("valido", 		true)
			if (oggetto != null) {
				// che bruttura... ma è per evitare brutture ancora peggiori...
				sqlRestriction "'#'||oggetti||'#' like '%#${oggetto}#%'"
			}
		}
	}

	public List<String> getListaAllegati () {
		return allegati?.split("#")?:[];
	}

	public List<String> getListaOggetti () {
		return oggetti?.split("#")?:[];
	}
}
