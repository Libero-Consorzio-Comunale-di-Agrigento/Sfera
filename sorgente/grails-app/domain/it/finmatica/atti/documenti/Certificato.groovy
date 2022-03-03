package it.finmatica.atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.documenti.tipologie.TipoCertificato
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.gestioneiter.motore.WkfIter
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.so4.struttura.So4Amministrazione
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class Certificato implements IDocumentoCollegato, IDocumento {

	public static final transient String TIPO_OGGETTO						= "CERTIFICATO"

	public static final transient String CERTIFICATO_PUBBLICAZIONE 			= "PUBBLICAZIONE"
	public static final transient String CERTIFICATO_AVVENUTA_PUBBLICAZIONE = "AVVENUTA_PUBBLICAZIONE"
	public static final transient String CERTIFICATO_ESECUTIVITA 			= "ESECUTIVITA"
	public static final transient String CERTIFICATO_IMMEDIATA_ESEGUIBILITA = "IMMEDIATA_ESEGUIBILITA"

	WkfIter	iter
	TipoCertificato tipologia
	String 	tipo
	boolean	secondaPubblicazione = false

	// firmatario di default
	Ad4Utente		firmatario

	// gestione testi
	FileAllegato 	testo
	FileAllegato	testoOdt
	GestioneTestiModello modelloTesto

	// stati
	StatoDocumento 	stato
	StatoFirma		statoFirma
	StatoMarcatura statoMarcatura

	// indica l'id del documento sul documentale esterno (ad es. GDM)
	Long idDocumentoEsterno

	static belongsTo = 	[ determina : Determina
						, delibera 	: Delibera]

	// i reali firmatari
	static hasMany = 	[firmatari  : Firmatario]

	// indica se il documento è valido o no, cioè se è stato "cancellato" oppure no
	boolean 	valido = true

	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd

	static mapping = {
		table 				'certificati'
		id 					column: 'id_certificato'
		tipologia			column: 'id_tipologia'
		iter				column: 'id_engine_iter',			index: 'cer_wkfengite_fk'
		testo				column: 'id_file_allegato_testo'
		testoOdt			column: 'id_file_allegato_testo_odt'
		determina			column: 'id_determina',				index: 'cer_det_fk'
		delibera			column: 'id_delibera',				index: 'cer_del_fk'
		modelloTesto		column: 'id_modello_testo'
		firmatario			column: 'firmatario'

		valido				type: 	'yes_no'
		ente 				column: 'ente'
		dateCreated 		column: 'data_ins'
		utenteIns 			column: 'utente_ins'
		lastUpdated 		column: 'data_upd'
		utenteUpd 			column: 'utente_upd'

		secondaPubblicazione	type: 'yes_no'
	 }

    static constraints = {
		iter		nullable: true
		testo		nullable: true
		testoOdt	nullable: true
		determina   nullable: true
		delibera    nullable: true
		firmatario  nullable: true
		modelloTesto 		nullable: true
		statoFirma			nullable: true
		statoMarcatura		nullable: true
		idDocumentoEsterno 	nullable: true

		tipo 		inList: [ Certificato.CERTIFICATO_PUBBLICAZIONE
							, Certificato.CERTIFICATO_AVVENUTA_PUBBLICAZIONE
							, Certificato.CERTIFICATO_ESECUTIVITA
							, Certificato.CERTIFICATO_IMMEDIATA_ESEGUIBILITA]
    }

	private SpringSecurityService getSpringSecurityService () {
		return Holders.applicationContext.getBean("springSecurityService")
	}

	def beforeValidate () {
		utenteIns = utenteIns?:springSecurityService.currentUser
		ente	  = ente?:springSecurityService.principal.amministrazione
		utenteUpd = utenteUpd?:springSecurityService.currentUser

		firmatari*.beforeValidate()
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
		multiEnteFilter (condition: "ente = :enteCorrente and valido = 'Y'", types: 'string')
	}

	/*
	 * Funzioni utili e di interfaccia
	 */
	static transients = ['documentoPrincipale', 'soggetto']

	long getIdDocumento () {
		return id?:-1
	}

	transient String getTipoOggetto () {
		return Certificato.TIPO_OGGETTO
	}

	transient So4UnitaPubb getUnitaProponente () {
		return documentoPrincipale.unitaProponente
	}

	transient boolean isTipoPubblicazione () {
		return Certificato.CERTIFICATO_PUBBLICAZIONE.equals(tipo)
	}

	transient boolean isTipoAvvenutaPubblicazione () {
		return Certificato.CERTIFICATO_AVVENUTA_PUBBLICAZIONE.equals(tipo)
	}

	transient boolean isTipoEsecutivita () {
		return Certificato.CERTIFICATO_ESECUTIVITA.equals(tipo)
	}

	transient ITipologia getTipologiaDocumento () {
		return this.tipologia;
	}

	transient IDocumento getDocumentoPrincipale () {
		return this.determina?:this.delibera
	}

	transient void setDocumentoPrincipale (IDocumento documentoPrincipale) {
		if (documentoPrincipale instanceof Determina) {
			this.determina = documentoPrincipale;
		} else if (documentoPrincipale instanceof Delibera) {
			this.delibera = documentoPrincipale;
		}
	}

	transient String getNomeFileTestoPdf () {
		return getNomeFile()+".pdf";
	}
	
	transient String getNomeFile () {
		return "${(isTipoPubblicazione()?"CP" : (isTipoAvvenutaPubblicazione()?"CAP":"CE"))}${secondaPubblicazione?"2":""}_${getDocumentoPrincipale().getNomeFile()}"
	}

	transient Set<Allegato> getAllegati () {
		return new HashSet<Allegato>()
	}

	/**
	 * Setta il soggetto con del tipo richiesto con l'utente e/o l'unità so4
	 * @param tipoSoggetto 	il tipo soggetto da settare
	 * @param utenteAd4		l'utente del soggetto
	 * @param unitaSo4		l'unità del soggetto
	 */
	transient void setSoggetto (String tipoSoggetto, Ad4Utente utenteAd4, So4UnitaPubb unitaSo4) {
		setSoggetto(tipoSoggetto, utenteAd4, unitaSo4, 0);
	}

	/**
	 * Setta il soggetto con del tipo richiesto con l'utente e/o l'unità so4
	 * @param tipoSoggetto 	il tipo soggetto da settare
	 * @param utenteAd4		l'utente del soggetto
	 * @param unitaSo4		l'unità del soggetto
	 */
	transient void setSoggetto (String tipoSoggetto, Ad4Utente utenteAd4, So4UnitaPubb unitaSo4, int sequenza) {
		switch (tipoSoggetto) {
			case TipoSoggetto.FIRMATARIO:
				this.firmatario = utenteAd4
			break
		}
	}

	/**
	 * Ritorna il soggetto della delibera
	 *
	 * @param tipoSoggetto	il codice del soggetto da ritornare
	 * @return	il soggetto trovato, null altrimenti.
	 */
	transient ISoggettoDocumento getSoggetto (String tipoSoggetto) {
		switch (tipoSoggetto) {
			case TipoSoggetto.FIRMATARIO:
                if (this.firmatario != null) {
				    return new SoggettoDocumento (tipoSoggetto:TipoSoggetto.get(tipoSoggetto), utenteAd4:this.firmatario, unitaSo4:null, sequenza:0, attivo:true, documentoPrincipale:this)
                }
			break
		}

		return null
	}

	transient List<ISoggettoDocumento> getSoggetti () {
		ISoggettoDocumento s = getSoggetto (TipoSoggetto.FIRMATARIO)
		if (s != null) {
			return [s]
		}

		return []
	}

	transient boolean isRiservato () {
		return false
	}
}
