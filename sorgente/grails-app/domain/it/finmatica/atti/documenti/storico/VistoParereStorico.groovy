package it.finmatica.atti.documenti.storico

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.IDocumentoStorico
import it.finmatica.atti.IDocumentoStoricoEsterno
import it.finmatica.atti.commons.FileAllegatoStorico
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.EsitoVisto
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.documenti.tipologie.TipoVistoParere
import it.finmatica.gestioneiter.motore.WkfIter
import it.finmatica.gestioneiter.motore.WkfStep
import it.finmatica.so4.struttura.So4Amministrazione
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders

class VistoParereStorico implements IDocumentoStoricoEsterno, IDocumentoStorico {

	// viene usato con accesso dinamico dal AttiFileDownloader
	public static final String TIPO_OGGETTO = VistoParere.TIPO_OGGETTO

	long idVistoParere 	// riferimento alla idVistoParere "originale"
	long revisione		// indice di revisione della idVistoParere storico
	WkfStep	step		// lo step in cui è stata storicizzata

	WkfIter 		iter
	TipoVistoParere tipologia

	// il firmatario di default
	Ad4Utente 		firmatario

	// unità di redazione
	So4UnitaPubb 	unitaSo4

	// testo
	FileAllegatoStorico	testo

	// dati visto/parere
	String 			note
	String 			noteTrasmissione
	Date 			dataAdozione
	boolean automatico = false
	EsitoVisto esito

	// indica lo stato del Visto: se è da processare, processato o annullato (ad es per reinoltro della proposta)
	StatoDocumento 	stato
	StatoFirma		statoFirma

	// campo che contiene la lista dei campi non modificabili
	String campiProtetti

	static belongsTo =  [ determina			: 	Determina
						, propostaDelibera 	: 	PropostaDelibera
						, delibera			:	Delibera]

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

	static constraints = {
		tipologia	nullable: true
		iter		nullable: true
		step		nullable: true
		firmatario 	nullable: true
		unitaSo4	nullable: true
		testo		nullable: true
		note		nullable: true
		noteTrasmissione 	nullable: true
		campiProtetti		nullable: true
		determina		    nullable: true
		propostaDelibera	nullable: true
		delibera			nullable: true
		idDocumentoEsterno	nullable: true
		versioneDocumentoEsterno nullable: true
		stato				nullable: true
		statoFirma			nullable: true
		dataAdozione		nullable: true
    }

	static mapping = {
		table		'visti_pareri_storico'
		id			column: 'id_visto_parere_storico'
		iter		column: 'id_engine_iter'
		step		column: 'id_engine_step'
		tipologia 	column: 'id_tipologia'
		testo		column: 'id_file_allegato_testo'
		automatico	type: 'yes_no'
		note 		length: 4000
		noteTrasmissione 		length: 4000
		campiProtetti		length: 4000

		determina			column: "id_determina", 		index: 'vispar_sto_det_fk'
		propostaDelibera	column: "id_proposta_delibera", index: 'vispar_sto_prodel_fk'
		delibera			column: "id_delibera", 			index: 'vispar_sto_del_fk'

		firmatario	column: 'utente_firmatario'
		columns {
			unitaSo4 {
				column name: 'unita_progr'
				column name: 'unita_dal'
				column name: 'unita_ottica'
			}
		}

		valido			type: 	'yes_no'
		ente 			column: 'ente'
		dateCreated 	column: 'data_ins'
		utenteIns 		column: 'utente_ins'
		lastUpdated 	column: 'data_upd'
		utenteUpd 		column: 'utente_upd'
	}

	private SpringSecurityService getSpringSecurityService () {		return Holders.applicationContext.getBean("springSecurityService")    }

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

	@Override
	public transient Object getDocumentoOriginale() {
		return VistoParere.get(idVistoParere)
	}
}
