package it.finmatica.atti.documenti.storico

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.IDocumentoStorico;
import it.finmatica.atti.IDocumentoStoricoEsterno;
import it.finmatica.atti.commons.FileAllegatoStorico;
import it.finmatica.atti.dizionari.Categoria
import it.finmatica.atti.dizionari.Delega
import it.finmatica.atti.dizionari.IndirizzoDelibera
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.PropostaDelibera;
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.StatoOdg
import it.finmatica.atti.documenti.tipologie.TipoDelibera
import it.finmatica.atti.odg.Commissione
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.gestioneiter.motore.WkfIter
import it.finmatica.gestioneiter.motore.WkfStep
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.so4.struttura.So4Amministrazione
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders

class PropostaDeliberaStorico implements IDocumentoStoricoEsterno, IDocumentoStorico {
	// viene usato con accesso dinamico dal AttiFileDownloader
	public static final String TIPO_OGGETTO = PropostaDelibera.TIPO_OGGETTO

	/**
	 * Contiene un xml con i soggetti della determina al momento della storicizzazione:
	 * <soggetti>
	 * 	<soggetto tipo="UNITA/UTENTE" cognomeNome="COGNOME_NOME" descrizione="DESCR UNITA" utente="CODICE_UTENTE_AD4" progrUo="PROGRESSIVO_UO" ottica="CODICE_OTTICA" dal="DD/MM/YYYY" />
	 *  ..
	 * </soggetti>
	 */
	String xmlSoggetti

	/**
	 * Contiene un xml con le determine collegate:
	 * <delibere>
	 * 	<delibera id="" operazione="INTEGRAZIONE/ANNULLAMENTO">
	 * 		<oggetto>OGGETTO_DELIBERA</oggetto>
	 * 		<numeroProposta numero="NUMERO" anno="ANNO" tipoRegistro="TIPO_REG" data="DD/MM/YYYY" />
	 * 		<numeroDelibera numero="NUMERO" anno="ANNO" tipoRegistro="TIPO_REG" data="DD/MM/YYYY" />
	 * 	</delibera>
	 *  ..
	 * </delibere>
	 */
	String xmlDelibereCollegate

	/**
	 * Contiene un xml con i dati aggiuntivi della determina:
	 * <datiAggiuntivi>
	 *     <dato id="" codice="" valore="" idTipoValore="" tipoValore=""/>
	 * </datiAggiuntivi>
	 */
	String xmlDatiAggiuntivi

	long idPropostaDelibera 	// riferimento alla PropostaDelibera "originale"
	long revisione		// indice di revisione della PropostaDelibera storico
	WkfStep	step		// lo step in cui è stata storicizzata

	WkfIter 			iter
	TipoDelibera 		tipologia

	// stati del documento
	StatoDocumento 		stato
	StatoFirma			statoFirma
	StatoOdg 			statoOdg 		// stato della PropostaDelibera per la sua gestione in odg

	// dati odg
	Categoria 			categoria
	Commissione			commissione			// commissione che dovrà discutere la PropostaDelibera
	Delega				delega
	OggettoSeduta		oggettoSeduta

	// testi
	FileAllegatoStorico	testo
	FileAllegatoStorico stampaUnica

	// modelli testo
	GestioneTestiModello modelloTesto				// modello di stampa predefinito
	GestioneTestiModello modelloTestoAnnullamento	// modello di stampa predefinito per l'annullamento TODO: da gestire!

	// dati proposta
	String				oggetto
	IndirizzoDelibera 	indirizzo

	// data modificabile dall'utente sulla base della quale si sceglie l'anno del registro su cui numerare la delibera (FIXME: o della proposta? o entrambe? o boh?)
	Date dataProposta

	Date 			dataNumeroProposta
	Integer 		numeroProposta
	Integer 		annoProposta
	TipoRegistro 	registroProposta

	boolean controlloFunzionario = false
	boolean riservato 			= false
	boolean	fuoriSacco 			= false
	boolean parereRevisoriConti = false

	// note
	String note
	String noteTrasmissione
	String noteContabili
	String noteCommissione

	// dati di protocollo
	String 	classificaCodice
	Date 	classificaDal
	String 	classificaDescrizione
	Integer fascicoloAnno
	String 	fascicoloNumero
	String 	fascicoloOggetto

	// dati di pubblicazione
	boolean pubblicaRevoca = false // resta in pubblicazione fino a revoca
	Integer	giorniPubblicazione

	// campo che contiene la lista dei campi non modificabili
	String campiProtetti

	// indica i codici dei visti che ho già trattato (serve per gestire eventuali ciclicità sui visti nel flusso)
	String codiciVistiTrattati

	// indica l'id del documento sul documentale esterno (ad es. GDM)
	Long idDocumentoEsterno
	Long versioneDocumentoEsterno

	// indica se il documento è valido o no, cioè se è stato "cancellato" oppure no
	boolean 	valido = true

    So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd

	String motivazione
	Integer priorita

	static mapping = {
		table				'proposte_delibera_storico'
		id					column: 'id_proposta_delibera_storico'
		tipologia			column: 'id_tipo_delibera'
		iter				column: 'id_engine_iter'
		idPropostaDelibera	column: 'id_proposta_delibera',  	index: 'prodelsto_prodel_fk'
		step				column: 'id_engine_step'
		testo				column: 'id_file_allegato_testo'
		stampaUnica			column: 'id_file_allegato_stampa_unica'
		categoria			column: 'id_categoria'
		commissione 		column: 'id_commissione'
		oggettoSeduta		column: 'id_oggetto_seduta'
		delega		 		column: 'id_delega'
		indirizzo			column: 'id_indirizzo_delibera'
		registroProposta	column: 'registro_proposta'

		controlloFunzionario	type: 'yes_no'
		riservato				type: 'yes_no'
		pubblicaRevoca			type: 'yes_no'
		fuoriSacco				type: 'yes_no'
		parereRevisoriConti		type: 'yes_no'

		oggetto 				length: 4000
		note 					length: 4000
		noteTrasmissione 		length: 4000
		noteContabili 			length: 4000
		fascicoloOggetto		length: 4000
		campiProtetti			length: 4000
		classificaDescrizione 	length: 4000
		xmlSoggetti				sqlType: 'clob'
		xmlDelibereCollegate	sqlType: 'clob'
        xmlDatiAggiuntivi sqlType: 'clob'

		modelloTesto				column: 'id_modello_testo'
		modelloTestoAnnullamento	column: 'id_modello_testo_annullamento'

		valido			type: 	'yes_no'
		ente 			column: 'ente'
		dateCreated 	column: 'data_ins'
		utenteIns 		column: 'utente_ins'
		lastUpdated 	column: 'data_upd'
		utenteUpd 		column: 'utente_upd'
	}

    static constraints = {
		iter               	nullable: true
		step				nullable: true
		tipologia          	nullable: true
		testo				nullable: true
		stampaUnica			nullable: true
		categoria			nullable: true
		delega				nullable: true
		indirizzo			nullable: true

		commissione 		nullable: true
		oggettoSeduta		nullable: true

		dataNumeroProposta     nullable: true
		numeroProposta         nullable: true
		annoProposta           nullable: true
		registroProposta	   nullable: true
        xmlDatiAggiuntivi nullable: true

		note               nullable: true
		noteTrasmissione   nullable: true
		noteContabili      nullable: true
		noteCommissione    nullable: true

		classificaCodice           nullable: true
		classificaDal              nullable: true
		classificaDescrizione      nullable: true
		fascicoloAnno              nullable: true
		fascicoloNumero            nullable: true
		fascicoloOggetto           nullable: true

		giorniPubblicazione       	nullable: true
		modelloTesto				nullable: true
		modelloTestoAnnullamento	nullable: true

		codiciVistiTrattati		 nullable: true
		campiProtetti			 nullable: true
		idDocumentoEsterno		 nullable: true
		versioneDocumentoEsterno nullable: true

		stato					nullable: true
		statoFirma				nullable: true
		statoOdg				nullable: true

		dataNumeroProposta 		nullable: true
		numeroProposta 			nullable: true
		annoProposta 			nullable: true
		registroProposta 		nullable: true

		motivazione				nullable: true
		priorita				nullable: true
    }

	private SpringSecurityService getSpringSecurityService () {		return Holders.applicationContext.getBean("springSecurityService")    }

	def beforeValidate () {
		utenteIns = utenteIns?:springSecurityService.currentUser
		utenteUpd = utenteUpd?:springSecurityService.currentUser
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
		return PropostaDelibera.get(idPropostaDelibera)
	}
}
