package it.finmatica.atti.documenti.tipologie

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.dizionari.OggettoRicorrente
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.documenti.ITipologia;
import it.finmatica.atti.documenti.ITipologiaPubblicazione;
import it.finmatica.atti.impostazioni.CaratteristicaTipologia
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.so4.struttura.So4Amministrazione

/**
 * Definisce la tipologia di determina
 *
 */
class TipoDetermina implements ITipologiaPubblicazione {

    Long progressivoCfgIter
	Long progressivoCfgIterPubblicazione 		// progressivo dell'iter di pubblicazione per la determina.

	TipoRegistro 			tipoRegistro  		// registro di numerazione per la determina
	TipoRegistro 			tipoRegistro2  		// registro per la seconda numerazione della determina, vedi: http://svi-redmine/issues/22205
	CaratteristicaTipologia caratteristicaTipologia

	String titolo
    String titoloNotifica
    String descrizioneNotifica
	String descrizione

	// il codice che identifica questa tipologia negli applicativi esterni.
	String codiceEsterno
	
	// Titolo del tipo di pubblicazione da mostrare all'albo JMessi.
	// Se non impostato, verrà usato il valore di default scritto nell'impostazione ALBO_COSTANTI_DETERMINA
	String tipoPubblicazioneAlbo

	boolean impattoGenerazionale = false

	GestioneTestiModello modelloTesto
	GestioneTestiModello modelloTestoAnnullamento
	GestioneTestiModello modelloTestoFrontespizio

	boolean vistiPareri					= false	// prevede l'aggiunta run-time di visti
	boolean registroUnita				= false // stabilisce se utilizzare il registro associato all'unità proponente
	boolean conservazioneSostitutiva	= false // se la delibera deve andare in conservazione
	boolean funzionarioObbligatorio 	= false
	boolean notificaOrganiControllo		= false
	boolean categoriaObbligatoria		= false
    boolean cupObbligatorio             = false
    boolean cupVisibile                 = false
	boolean testoObbligatorio			= true  // indica se il testo deve essere presente tra uno step e l'altro.
    boolean eseguibilitaImmediata		= false
	boolean pndNotifica                 = false //indica se la determina prevede la notifica tramite PND
	//boolean pndPagamento                = false //indica se la notifica PND prevede pagamento
	boolean visDataControlloGestione    = false //indica se la determina visualizza il campo della data di controllo di gestione
	boolean visDataScadenzaDisposizione = false //indica se la determina visualizza il campo della data di scadenza della disposizione
	boolean impegnoObbligatorio         = false

	//riservatezza: flag per stabilire se gli atti hanno il flag riservato modificabile
	boolean gestioneRiservato				= true
	boolean gestioneRiservatoAllegati		= true

	// dati di pubblicazione
	boolean pubblicazione					= false
	boolean secondaPubblicazione			= false
	boolean manuale							= false
	boolean pubblicaAllegati				= false
	boolean pubblicaAllegatiDefault			= false
	Integer giorniPubblicazione
	boolean giorniPubblicazioneModificabile	= true
	boolean pubblicazioneFutura				= false
	boolean pubblicazioneFinoARevoca		= false
	boolean pubblicazioneTrasparenza		= false	// indica se l'atto DEVE andare in casa di vetro. Il controllo viene effettuato nell'azione casaDiVetroAction.controllaAttoPresenteObbligatorio che va aggiunta nel flusso
	boolean codiceGara              		= false
	boolean codiceGaraObbligatorio	    	= false
	boolean visImportoTotale				= false
	boolean importoTotaleObbligatorio		= false
	boolean pubblicaVisualizzatore			= false
    boolean pubblicaAllegatiVisualizzatore	= true

	// con movimenti contabili:
	boolean movimentiContabili			= true  // con movimenti contabili
	boolean scritturaMovimentiContabili	= false // i movimenti contabili sono inseribili / modificabili
	boolean esecutivitaMovimenti 		= true  // i movimenti contabili diventano esecutivi all'esecutività del documento
    boolean queryMovimenti              = true  // effettuare le query dei movimenti contabili nei modelli
	boolean contabilitaCheckRiservato	= false // se l'atto è riservato, solo chi accede al testo può accedere alla sezione contabilità

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
	
	boolean diventaEsecutiva
	boolean incaricatoObbligatorio = false
    String ruoloRiservato

	String tipoFirma //se valorizzato indica il tipo di firma da usare per i documenti (PADES o CADES)

	//PiTre
	String fascicoloNumero
	Long fascicoloAnno
	String fascicoloOggetto
	String idFascicoloEsterno

	static mapping = {
		table 							'tipi_determina'
		id 						column: 'id_tipo_determina'
		descrizione 			length: 4000


		progressivoCfgIter				column: 'progressivo_cfg_iter'
		progressivoCfgIterPubblicazione column: 'progressivo_cfg_iter_pubb'

		tipoRegistro  				column: 'id_tipo_registro'
		tipoRegistro2  				column: 'id_tipo_registro_2'

		caratteristicaTipologia		column: 'id_caratteristica_tipologia'
		funzionarioObbligatorio 	column: 'funz_obbligatorio', type: 'yes_no'
        pubblicaAllegatiVisualizzatore column: 'pubblica_allegati_vis', type: 'yes_no'
		impattoGenerazionale			column: 'impatto_generazionale', type: 'yes_no'

		modelloTesto				column: 'id_modello_testo'
		modelloTestoAnnullamento	column: 'id_modello_testo_annulla'
		modelloTestoFrontespizio	column: 'id_modello_testo_frontespizio'

		codiceGara   	            column: 'cig'
		codiceGaraObbligatorio	    column: 'cig_obbligatorio'

        giorniPubblicazioneModificabile column: 'giorni_pubb_modificabile'
		pubblicazioneFutura			column: 'pubblicazione_futura'

		modelliTesto joinTable: [name: 	'TIPI_DETERMINA_MODELLI_TESTO',
								column: 'ID_MODELLO',
								key: 	'ID_TIPO_DETERMINA']

		oggettiRicorrenti		joinTable: [name: 'tipi_determina_ogg_ric', key: 'id_tipo_determina', column: 'id_oggetto_ricorrente']
		tipiVisto				joinTable: [name: 'tipi_determina_visti_pareri', key: 'id_tipo_determina', column:'id_tipo_vistoparere']

		vistiPareri					type: 'yes_no'
		registroUnita				type: 'yes_no'
		conservazioneSostitutiva	type: 'yes_no'
		notificaOrganiControllo		type: 'yes_no'
		categoriaObbligatoria		type: 'yes_no'
		testoObbligatorio			type: 'yes_no'
        cupObbligatorio             type: 'yes_no'
        cupVisibile                 type: 'yes_no'
        eseguibilitaImmediata       type: 'yes_no'
		pndNotifica					column: 'pnd_notifica', type: 'yes_no'
		visDataControlloGestione	column: 'vis_data_controllo_gestione', type: 'yes_no'
		visDataScadenzaDisposizione column: 'vis_data_scad_disposizione', type: 'yes_no'
		impegnoObbligatorio         column: 'impegno_obbligatorio', type: 'yes_no'
//		pndPagamento			    column: 'pnd_pagamento', type: 'yes_no'

		pubblicazione					type: 'yes_no'
		secondaPubblicazione			type: 'yes_no'
		manuale							type: 'yes_no'
		pubblicaAllegati				type: 'yes_no'
		pubblicazioneFinoARevoca		type: 'yes_no'
		pubblicazioneTrasparenza		type: 'yes_no'
		giorniPubblicazioneModificabile type: 'yes_no'
		pubblicazioneFutura				type: 'yes_no'
		codiceGara   	            	type: 'yes_no'
		codiceGaraObbligatorio      	type: 'yes_no'
		visImportoTotale				column: 'vis_importo_totale', type: 'yes_no'
		importoTotaleObbligatorio		type: 'yes_no'
		pubblicaAllegatiDefault			type: 'yes_no'
		pubblicaVisualizzatore			type: 'yes_no'

		gestioneRiservato				type: 'yes_no'
		gestioneRiservatoAllegati		type: 'yes_no'

		movimentiContabili			type: 'yes_no'
		esecutivitaMovimenti		type: 'yes_no'
		scritturaMovimentiContabili	type: 'yes_no'
        queryMovimenti              type: 'yes_no'
		contabilitaCheckRiservato	type: 'yes_no'
		
		diventaEsecutiva			type: 'yes_no'
		incaricatoObbligatorio		type: 'yes_no'

		tipoCertPubb                column: 'id_tipo_cert_pubb'
		tipoCertPubb2               column: 'id_tipo_cert_pubb2'
		tipoCertAvvPubb             column: 'id_tipo_cert_avv_pubb'
		tipoCertAvvPubb2            column: 'id_tipo_cert_avv_pubb2'
		tipoCertEsec                column: 'id_tipo_cert_esec'
        tipoCertImmEseg             column: 'id_tipo_cert_imm_eseg'

		ente 				column: 'ente'
		valido 				type:   'yes_no'
		dateCreated 		column: 'data_ins'
		utenteIns 			column: 'utente_ins'
		lastUpdated 		column: 'data_upd'
		utenteUpd 			column: 'utente_upd'

		fascicoloNumero		column: 'fascicolo_numero'
		fascicoloAnno		column: 'fascicolo_anno'
		fascicoloOggetto	column: 'fascicolo_oggetto'
		idFascicoloEsterno	column: 'id_fascicolo_esterno'

	}

	static constraints = {
		progressivoCfgIter				nullable: true
		progressivoCfgIterPubblicazione nullable: true
		tipoRegistro 					nullable: true // è nullo quando si utilizza il mapping dell'unità (registro_unita = Y)
		tipoRegistro2 					nullable: true
		descrizione						nullable: true
		validoAl						nullable: true
		modelloTesto					nullable: true
		modelloTestoAnnullamento		nullable: true
		modelloTestoFrontespizio		nullable: true
		giorniPubblicazione				nullable: true
		tipoCertPubb    				nullable: true
		tipoCertAvvPubb     			nullable: true
		tipoCertPubb2    				nullable: true
		tipoCertAvvPubb2     			nullable: true
        tipoCertImmEseg                 nullable: true
		tipoCertEsec        			nullable: true
		codiceEsterno					nullable: true
		tipoPubblicazioneAlbo			nullable: true
		incaricatoObbligatorio			nullable: true
        ruoloRiservato                  nullable: true
        descrizioneNotifica             nullable: true
		tipoFirma						nullable: true
		fascicoloAnno                   nullable: true
		fascicoloNumero                 nullable: true
		fascicoloOggetto                nullable: true
		idFascicoloEsterno				nullable: true
	}

	transient WkfCfgIter getCfgIter () {
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
				eq ("tipoCertPubb.id"     , idTipoCertificato)
				eq ("tipoCertAvvPubb.id"  , idTipoCertificato)
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
				eq ("modelloTesto.id"     			, idModelloTesto)
				eq ("modelloTestoAnnullamento.id"  	, idModelloTesto)
				eq ("modelloTestoFrontespizio.id"   , idModelloTesto)

				modelliTesto {
					eq ("id", idModelloTesto)
				}
			}

			eq ("valido", true)
		}

		inUsoPerCaratteristicaTipologia { long idCaratteristica ->
			eq ("caratteristicaTipologia.id", idCaratteristica)
			eq ("valido", true)
		}
	}
}
