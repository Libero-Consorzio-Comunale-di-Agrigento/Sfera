package it.finmatica.atti.documenti.tipologie

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.documenti.ITipologia;
import it.finmatica.atti.impostazioni.CaratteristicaTipologia
import it.finmatica.gestionetesti.reporter.GestioneTestiModello;
import it.finmatica.so4.struttura.So4Amministrazione
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders

class TipoVistoParere implements ITipologia {

	public static final transient String SEPARATORE = "#"

    Long progressivoCfgIter
	Long progressivoCfgIterDelibera 	// iter che verrà usato dal visto/parere quando verrà istanziato come delibera.

	String titolo
	String descrizione
    String descrizioneNotifica
	String codice
	CaratteristicaTipologia caratteristicaTipologia
	int sequenzaStampaUnica = 0			// indica la sequenza che il visto deve rispettare quando inserito in stampa unica

	GestioneTestiModello modelloTesto

	boolean contabile 	 			= false // è un visto contabile o no.
	boolean conFirma  	 			= false // deve essere firmato (quindi passa al dirigente per forza)
	boolean conRedazioneUnita 		= false // con passaggio in uo di redazione
	boolean conRedazioneDirigente 	= false // con passaggio in redazione dal dirigente
	boolean stampaUnica 			= false // deve andare in stampa unica
	boolean pubblicazione			= false // deve andare in pubblicazione all'albo?
	boolean testoObbligatorio		= false // il testo è obbligatorio?
	boolean pubblicaAllegati		= false
	boolean pubblicaAllegatiDefault = false

    boolean queryMovimenti          = true  // effettuare le query dei movimenti contabili nei modelli

	// contiene i progressivi delle unità concatenati con # che potranno gestire i visti di questa tipologia.
	String unitaDestinatarie

	boolean valido = true
	Date validoDal  // da valorizzare alla creazione del record
	Date validoAl   // deve essere valorizzato con la data di sistema quando valido = false
					// quando valido = true deve essere null

	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd

	static mapping = {
		table 			'tipi_visto_parere'
		id 				column: 'id_tipo_visto_parere'
		descrizione 	length: 4000
		caratteristicaTipologia	column: 'id_caratteristica_tipologia'
		modelloTesto			column: 'id_modello_testo'

		valido 					type: 'yes_no'
		contabile               type: 'yes_no'
		conFirma                type: 'yes_no'
		conRedazioneUnita       type: 'yes_no'
		conRedazioneDirigente	type: 'yes_no'
		stampaUnica             type: 'yes_no'
		pubblicazione           type: 'yes_no'
		testoObbligatorio		type: 'yes_no'
		pubblicaAllegati		type: 'yes_no'
		pubblicaAllegatiDefault type: 'yes_no'
        queryMovimenti          type: 'yes_no'

		ente 			column: 'ente'
		dateCreated 	column: 'data_ins'
		utenteIns 		column: 'utente_ins'
		lastUpdated 	column: 'data_upd'
		utenteUpd 		column: 'utente_upd'
	}

	static constraints = {
		progressivoCfgIter	nullable: true
		unitaDestinatarie	nullable: true
		descrizione			nullable: true
		validoAl			nullable: true
		modelloTesto		nullable: true
		progressivoCfgIterDelibera	nullable:true
        descrizioneNotifica nullable: true
	}

	public void addUnitaDestinataria (long progressivo) {
		String progr = Long.toString(progressivo)
		if (unitaDestinatarie == null)
			unitaDestinatarie = ""
		unitaDestinatarie = (unitaDestinatarie.tokenize(SEPARATORE) << progr).unique().join(SEPARATORE)
	}

	public void removeUnitaDestinataria (long progressivo) {
		String progr = Long.toString(progressivo)
		if (unitaDestinatarie == null)
			unitaDestinatarie = ""
		def ud = unitaDestinatarie.tokenize(SEPARATORE)
		ud.remove(progr)
		unitaDestinatarie = ud.join(SEPARATORE)
	}

	public long[] getListaUnitaDestinatarie () {
		if (unitaDestinatarie == null)
			unitaDestinatarie = ""
		return unitaDestinatarie.tokenize(SEPARATORE).collect { Long.parseLong(it) }.toArray()
	}

	private SpringSecurityService getSpringSecurityService () {		return Holders.applicationContext.getBean("springSecurityService")    }

	def beforeValidate () {
		validoDal = validoDal?:new Date()
		utenteIns = utenteIns?:springSecurityService.currentUser
		ente	  = ente?:springSecurityService.principal.amministrazione
		utenteUpd = springSecurityService.currentUser
	}

	def beforeInsert () {
		validoAl  = valido ? null : (validoAl?:new Date())
		validoDal = new Date()
		utenteIns = utenteIns?:springSecurityService.currentUser
		utenteUpd = utenteUpd?:springSecurityService.currentUser
		ente	  = ente?:springSecurityService.principal.amministrazione
	}

	def beforeUpdate () {
		validoAl  = valido ? null : (validoAl?:new Date())
		utenteUpd = springSecurityService.currentUser
	}

	static hibernateFilters = {
		multiEnteFilter (condition: 'ente = :enteCorrente', types: 'string')
	}

	static namedQueries = {
		inUsoPerModelloTesto { long idModelloTesto ->
			eq ("modelloTesto.id", idModelloTesto)
			eq ("valido", true)
		}

		inUsoPerCaratteristicaTipologia { long idCaratteristica ->
			eq ("caratteristicaTipologia.id", idCaratteristica)
			eq ("valido", true)
		}
	}
}
