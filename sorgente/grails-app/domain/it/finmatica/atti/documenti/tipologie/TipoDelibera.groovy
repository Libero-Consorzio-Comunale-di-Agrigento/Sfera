package it.finmatica.atti.documenti.tipologie

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.dizionari.OggettoRicorrente
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.ITipologiaPubblicazione;
import it.finmatica.atti.impostazioni.CaratteristicaTipologia
import it.finmatica.atti.odg.Commissione
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.so4.struttura.So4Amministrazione

class TipoDelibera implements ITipologiaPubblicazione {
	Long	 				progressivoCfgIter				// progressivo dell'iter di proposta delibera
	Long	 				progressivoCfgIterFuoriSacco	// progressivo dell'iter di proposta delibera fuori sacco
	Long	 				progressivoCfgIterPubblicazione // progressivo dell'iter di pubblicazione per la delibera.
	Long	 				progressivoCfgIterDelibera		// progressivo dell'iter della delibera

	TipoRegistro 			tipoRegistroDelibera
	CaratteristicaTipologia caratteristicaTipologia
	CaratteristicaTipologia caratteristicaTipologiaFuoriSacco
	CaratteristicaTipologia caratteristicaTipologiaDelibera
	
	Commissione				commissione					// commissione che dovrà discutere la delibera

	GestioneTestiModello modelloTesto
	GestioneTestiModello modelloTestoDelibera
	GestioneTestiModello modelloTestoFrontespizio

	String titolo
	String titoloNotifica
    String descrizioneNotifica
    String descrizioneNotificaDelibera
	String descrizione

	// il codice che identifica questa tipologia negli applicativi esterni.
	String codiceEsterno
	
	// Titolo del tipo di pubblicazione da mostrare all'albo JMessi.
	// Se non impostato, verrà usato il valore di default scritto nell'impostazione ALBO_COSTANTI_DELIBERA
	String tipoPubblicazioneAlbo

	boolean copiaTestoProposta			= false // indica se il testo della delibera sarà la copia del testo firmato della proposta
    boolean allegatoTestoProposta       = false // indica se il testo della proposta sarà il primo allegato della delibera
	boolean delega  					= false // indica se la tipologia prevede o meno delega
	boolean delegaObbligatoria 			= false	// indica se la delega è obbligatoria
	boolean vistiPareri					= false	// prevede l'aggiunta run-time di visti
	boolean conservazioneSostitutiva 	= false // se la delibera deve andare in conservazione
	boolean funzionarioObbligatorio 	= false
	boolean notificaOrganiControllo		= false
	boolean categoriaObbligatoria		= false
	boolean eseguibilitaImmediata		= false
	boolean diventaEsecutiva
	boolean testoObbligatorio			= true  // indica se il testo deve essere presente tra uno step e l'altro.
	boolean adottabile					= true  // indica se la proposta di delibera può essere numerata o no.

	// dati di pubblicazione
	boolean pubblicazione					= false
	boolean secondaPubblicazione			= false
	boolean manuale							= false
	boolean pubblicaAllegati				= false
	Integer giorniPubblicazione
	boolean giorniPubblicazioneModificabile	= true
	boolean pubblicazioneFutura				= false
	boolean pubblicazioneFinoARevoca		= false
	boolean pubblicazioneTrasparenza    	= false
	boolean pubblicaAllegatiDefault			= false
	boolean pubblicaVisualizzatore			= false
    boolean pubblicaAllegatiVisualizzatore	= true

	boolean movimentiContabili			= true 	// con movimenti contabili
	boolean scritturaMovimentiContabili	= false // i movimenti contabili sono inseribili / modificabili
	boolean esecutivitaMovimenti 		= true	// i movimenti contabili diventano esecutivi all'esecutività del documento
    boolean queryMovimenti              = true  // effettuare le query dei movimenti contabili nei modelli

	// certificati
	TipoCertificato tipoCertPubb
	TipoCertificato tipoCertPubb2
	TipoCertificato tipoCertAvvPubb
	TipoCertificato tipoCertAvvPubb2
	TipoCertificato tipoCertEsec
	TipoCertificato tipoCertImmEseg

	static hasMany   = [parametri: ParametroTipologia, tipiVisto: TipoVistoParere, modelliTesto : GestioneTestiModello, oggettiRicorrenti: OggettoRicorrente]

	boolean valido = true
	Date validoDal  // da valorizzare alla creazione del record
	Date validoAl   // deve essere valorizzato con la data di sistema quando valido = false
					// quando valido = true deve essere null
	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd
	boolean 	incaricatoObbligatorio = false
	Long 		sequenza
    String      ruoloRiservato

	static mapping = {
		table 				'tipi_delibera'
		id 			column: 'id_tipo_delibera'
		descrizione length: 4000

		progressivoCfgIter				column: 'progressivo_cfg_iter'
		progressivoCfgIterDelibera		column: 'progressivo_cfg_iter_delibera'
		progressivoCfgIterFuoriSacco	column: 'progr_cfg_iter_fuori_sacco'
		progressivoCfgIterPubblicazione column: 'progressivo_cfg_iter_pubb'

		giorniPubblicazioneModificabile column: 'giorni_pubb_modificabile'
		pubblicazioneFutura				column: 'pubblicazione_futura'

		tipoRegistroDelibera					column: 'id_tipo_registro_delibera'
		modelloTesto							column: 'id_modello_testo'
		modelloTestoDelibera					column: 'id_modello_testo_delibera'
		modelloTestoFrontespizio				column: 'id_modello_testo_frontespizio'
		caratteristicaTipologia					column: 'id_caratteristica_tipologia'
		caratteristicaTipologiaFuoriSacco		column: 'id_caratteristica_fuori_sacco'
		caratteristicaTipologiaDelibera			column: 'id_caratteristica_delibera'
		funzionarioObbligatorio 				column: 'funz_obbligatorio', type: 'yes_no'
        pubblicaAllegatiVisualizzatore          column: 'pubblica_allegati_vis', type: 'yes_no'

		tipoCertPubb                column: 'id_tipo_cert_pubb'
		tipoCertPubb2               column: 'id_tipo_cert_pubb2'
		tipoCertAvvPubb             column: 'id_tipo_cert_avv_pubb'
		tipoCertAvvPubb2            column: 'id_tipo_cert_avv_pubb2'
		tipoCertEsec                column: 'id_tipo_cert_esec'
		tipoCertImmEseg             column: 'id_tipo_cert_imm_eseg'

		tipiVisto					joinTable: [name: 'tipi_delibera_visti_pareri', key: 'id_tipo_delibera', column:'id_tipo_vistoparere']
		oggettiRicorrenti			joinTable: [name: "tipi_delibera_ogg_ric", key: "id_tipo_delibera", column: "id_oggetto_ricorrente"]

		commissione					column: 'id_commissione'
		copiaTestoProposta			type: 'yes_no'
        allegatoTestoProposta       type: 'yes_no'
		delega						type: 'yes_no'
		delegaObbligatoria			type: 'yes_no'

		vistiPareri					type: 'yes_no'
		conservazioneSostitutiva	type: 'yes_no'
		notificaOrganiControllo		type: 'yes_no'
		categoriaObbligatoria		type: 'yes_no'
		eseguibilitaImmediata       type: 'yes_no'
		testoObbligatorio			type: 'yes_no'

		pubblicazione					type: 'yes_no'
		secondaPubblicazione			type: 'yes_no'
		manuale							type: 'yes_no'
		adottabile						type: 'yes_no'
		pubblicaAllegati				type: 'yes_no'
		pubblicazioneFinoARevoca		type: 'yes_no'
		pubblicazioneTrasparenza		type: 'yes_no'
		giorniPubblicazioneModificabile type: 'yes_no'
		pubblicazioneFutura				type: 'yes_no'

		movimentiContabili			type: 'yes_no'
		scritturaMovimentiContabili	type: 'yes_no'
        esecutivitaMovimenti    	type: 'yes_no'
        queryMovimenti              type: 'yes_no'
		diventaEsecutiva			type: 'yes_no'
		incaricatoObbligatorio		type: 'yes_no'
		pubblicaAllegatiDefault		type: 'yes_no'
		pubblicaVisualizzatore		type: 'yes_no'

		ente 		column: 'ente'
		valido 		type:   'yes_no'
		dateCreated column: 'data_ins'
		utenteIns 	column: 'utente_ins'
		lastUpdated column: 'data_upd'
		utenteUpd 	column: 'utente_upd'

		modelliTesto joinTable: [name: 'TIPI_DELIBERA_MODELLI_TESTO', column: 'ID_MODELLO', key: 'ID_TIPO_DELIBERA']
	}

	static constraints = {
		progressivoCfgIter					nullable: true
		progressivoCfgIterDelibera			nullable: true
		progressivoCfgIterFuoriSacco		nullable: true
		progressivoCfgIterPubblicazione 	nullable: true
		caratteristicaTipologiaDelibera		nullable: true
		caratteristicaTipologiaFuoriSacco 	nullable: true
		tipoRegistroDelibera				nullable: true
		commissione							nullable: true
		descrizione							nullable: true
		validoAl							nullable: true
		modelloTestoFrontespizio			nullable: true
		modelloTestoDelibera				nullable: true
		modelloTesto						nullable: true
		giorniPubblicazione					nullable: true
		tipoCertPubb    			nullable: true
		tipoCertAvvPubb     		nullable: true
		tipoCertPubb2    			nullable: true
		tipoCertAvvPubb2     		nullable: true
		tipoCertEsec        		nullable: true
		tipoCertImmEseg        		nullable: true
		codiceEsterno				nullable: true
		tipoPubblicazioneAlbo		nullable: true
		incaricatoObbligatorio		nullable: true
		sequenza					nullable: true
        ruoloRiservato              nullable: true
        descrizioneNotifica         nullable: true
        descrizioneNotificaDelibera nullable: true
    }

	public transient WkfCfgIter getCfgIter () {
		return WkfCfgIter.getIterIstanziabile (progressivoCfgIter).get()
	}

	private SpringSecurityService getSpringSecurityService () {
		return Holders.applicationContext.getBean("springSecurityService")
	}

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
		inUsoPerTipoCertificato { long idTipoCertificato ->
			or {
				eq ("tipoCertPubb.id"     , idTipoCertificato)
				eq ("tipoCertAvvPubb.id"  , idTipoCertificato)
				eq ("tipoCertPubb2.id"    , idTipoCertificato)
				eq ("tipoCertAvvPubb2.id" , idTipoCertificato)
				eq ("tipoCertEsec.id" 	  , idTipoCertificato)
				eq ("tipoCertImmEseg.id"  , idTipoCertificato)

			}
			eq ("valido", true)
		}

		inUsoPerTipoVisto { long idTipoVisto ->
			tipiVisto {
				eq ("id", idTipoVisto)
			}
			eq ("valido", true)
		}

		inUsoPerModelloTesto { long idModelloTesto ->
			or {
				eq ("modelloTesto.id", idModelloTesto)
				modelliTesto {
					eq ("id", idModelloTesto)
				}
			}
			eq ("valido", true)
		}

		inUsoPerCaratteristicaTipologia { long idCaratteristica ->
			or {
				eq ("caratteristicaTipologia.id", idCaratteristica)
				eq ("caratteristicaTipologiaFuoriSacco.id", idCaratteristica)
			}
			eq ("valido", true)
		}
	}
}
