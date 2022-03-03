package it.finmatica.atti.documenti.storico

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.IDocumentoStorico
import it.finmatica.atti.IDocumentoStoricoEsterno
import it.finmatica.atti.commons.FileAllegatoStorico
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.StatoConservazione
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.gestioneiter.motore.WkfIter
import it.finmatica.gestioneiter.motore.WkfStep
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.so4.struttura.So4Amministrazione

class DeliberaStorico implements IDocumentoStoricoEsterno, IDocumentoStorico {

    // viene usato con accesso dinamico dal AttiFileDownloader
    public static final String TIPO_OGGETTO = Delibera.TIPO_OGGETTO

	/**
	 * Contiene un xml con i soggetti della delibera al momento della storicizzazione:
	 * <soggetti>
	 * 	<soggetto tipo="UNITA/UTENTE" cognomeNome="COGNOME_NOME" descrizione="DESCR UNITA" utente="CODICE_UTENTE_AD4" progrUo="PROGRESSIVO_UO" ottica="CODICE_OTTICA" dal="DD/MM/YYYY" />
	 *  ..
	 * </soggetti>
	 */
	String xmlSoggetti

	/**
	 * Contiene un xml con i dati aggiuntivi della determina:
	 * <datiAggiuntivi>
	 *     <dato id="" codice="" valore="" idTipoValore="" tipoValore=""/>
	 * </datiAggiuntivi>
	 */
	String xmlDatiAggiuntivi

	long idDelibera 	// riferimento alla delibera "originale"
	long revisione		// indice di revisione della delibera storico
	WkfStep	step		// lo step in cui è stata storicizzata

	WkfIter 			iter
    PropostaDelibera 	proposta
	OggettoSeduta 		oggettoSeduta

	// stati del documento
	StatoDocumento 		stato
	StatoFirma			statoFirma
	StatoConservazione	statoConservazione

	// testi
	FileAllegatoStorico		testo
	FileAllegatoStorico		stampaUnica
	GestioneTestiModello 	modelloTesto

	// dati della delibera
	String 	oggetto

	Date 	dataNumeroDelibera
	Integer numeroDelibera
	Integer annoDelibera
	TipoRegistro registroDelibera

	// dati di protocollo
	Date 	dataNumeroProtocollo
	Integer numeroProtocollo
	Integer annoProtocollo
	TipoRegistro registroProtocollo

	// esecutività
	Date 	dataEsecutivita
	Date 	dataAdozione

	//Eseguibilità immediata
	boolean eseguibilitaImmediata = false
	String motivazioniEseguibilita

	// indica l'id del documento sul documentale esterno (ad es. GDM)
	Long idDocumentoEsterno
	Long versioneDocumentoEsterno

	// dati di pubblicazione
	boolean riservato = false;
	boolean pubblicaRevoca = false // resta in pubblicazione fino a revoca
	Integer	giorniPubblicazione
	Date 	dataPubblicazione
	Date 	dataFinePubblicazione
	Date 	dataPubblicazione2
	Date 	dataFinePubblicazione2

	// note
	String note
	String noteTrasmissione

	// indica se il documento è valido o no, cioè se è stato "cancellato" oppure no
	boolean 	valido = true

	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd

    static constraints = {
		iter					nullable: true
		step					nullable: true
		oggetto 				nullable: true
		oggettoSeduta			nullable: true
		numeroProtocollo 		nullable: true
		annoProtocollo			nullable: true
		dataNumeroProtocollo	nullable: true
		registroProtocollo		nullable: true
		giorniPubblicazione		nullable: true
		dataEsecutivita			nullable: true
		dataAdozione			nullable: true
		giorniPubblicazione     nullable: true
		dataPubblicazione       nullable: true
		dataFinePubblicazione   nullable: true
		dataPubblicazione2      nullable: true
		dataFinePubblicazione2  nullable: true
		idDocumentoEsterno		nullable: true
		versioneDocumentoEsterno nullable: true
		stato					nullable: true
		statoFirma				nullable: true
		statoConservazione		nullable: true
		testo					nullable: true
		stampaUnica				nullable: true
		modelloTesto			nullable: true
		note                    nullable: true
		noteTrasmissione		nullable: true
		dataNumeroDelibera		nullable: true
		numeroDelibera			nullable: true
		annoDelibera			nullable: true
		registroDelibera		nullable: true
		motivazioniEseguibilita nullable: true
        xmlDatiAggiuntivi nullable: true
    }

	static mapping = {
		table 		 	'delibere_storico'
		id 	 			column: 'id_delibera_storico'
		iter			column: 'id_engine_iter'
		idDelibera		column: 'id_delibera',			index: 'delsto_del_fk'
		step			column: 'id_engine_step'
		proposta 		column: 'id_proposta_delibera'
		oggettoSeduta 	column: 'id_oggetto_seduta'
		testo			column: 'id_file_allegato'
		stampaUnica		column: 'id_file_allegato_stampa_unica'
		modelloTesto	column: 'id_modello_testo'
		motivazioniEseguibilita column: 'motivazioni_eseguibilita'

		registroDelibera    	column: 'registro_delibera'
		registroProtocollo  	column: 'registro_protocollo'
		pubblicaRevoca			type: 'yes_no'
		riservato				type: 'yes_no'
		eseguibilitaImmediata 	type: 'yes_no'

        xmlDatiAggiuntivi sqlType: 'clob'
		xmlSoggetti			sqlType: 'clob'
		note                length: 4000
		noteTrasmissione	length: 4000
		oggetto				length: 4000

		dataPubblicazione2		column: 'data_pubblicazione_2'
		dataFinePubblicazione2	column: 'data_fine_pubblicazione_2'

		valido			type: 	'yes_no'
		ente 			column: 'ente'
		dateCreated 	column: 'data_ins'
		utenteIns 		column: 'utente_ins'
		lastUpdated 	column: 'data_upd'
		utenteUpd 		column: 'utente_upd'
	}

	private SpringSecurityService getSpringSecurityService () {
		return Holders.applicationContext.getBean("springSecurityService")
	}

	def beforeValidate () {
		utenteIns = utenteIns?:springSecurityService.currentUser
		utenteUpd = springSecurityService.currentUser
		ente	  = ente?:springSecurityService.principal.amministrazione
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


	@Override
	public transient Object getDocumentoOriginale() {
		return Delibera.get(idDelibera)
	}
}
