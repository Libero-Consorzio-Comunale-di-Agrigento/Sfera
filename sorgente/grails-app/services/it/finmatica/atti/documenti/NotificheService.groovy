package it.finmatica.atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.GrailsNameUtils
import grails.util.Holders
import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.commons.SoggettiAttoriService
import it.finmatica.atti.commons.StrutturaOrganizzativaService
import it.finmatica.atti.dizionari.Notifica
import it.finmatica.atti.dizionari.NotificaEmail
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.documenti.beans.AttiGestioneTesti
import it.finmatica.atti.documenti.beans.NotificheDispatcher
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.DistintaAtto
import it.finmatica.atti.integrazioni.jworklist.JWorklistDispatcher
import it.finmatica.atti.mail.Allegato
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.atti.odg.Seduta
import it.finmatica.atti.odg.SedutaPartecipante
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.atti.odg.dizionari.OrganoControlloComponente
import it.finmatica.atti.odg.dizionari.RuoloPartecipante
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestionetesti.GestioneTestiService
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubb
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.hibernate.FetchMode
import org.hibernate.criterion.CriteriaSpecification

import java.text.DecimalFormat
import java.text.SimpleDateFormat

/**
 * Questa classe si occupa di tutta la gestione di invio e calcolo delle notifiche sia email che jworklist.
 *
 * Offre una serie di metodi pubblici a disposizione per l'invio, il calcolo e la gestione delle notifiche.
 * Ogni notifica è composta di tre parti:
 * 1) testo e oggetto: possono contenere una serie di "tag" che vanno popolati alla creazione della notifica (ad es, il n. della proposta)
 * 2) allegati:	possono essere il testo del documento principale piuttosto che una stampa, etc.
 * 3) destinatari: i vari soggetti cui inviare la notifica, possono essere semplici indirizzi email, soggetti di AS4, utenti di AD4, componenti di SO4.
 *
 * L'idea generale è che questa classe offre una serie di metodi privati per il calcolo di ciascuna di queste parti.
 * Ci sono quindi una serie di metodi per il calcolo dei soggetti (tutti questi metodi ritornano un array di SoggettoNotifica, vedi in fondo a questa classe),
 * una serie di metodi per il calcolo dei "tag" inseribili nel testo e nell'oggetto ed una serie di metodi per il calcolo degli allegati.
 *
 * Siccome le notifiche vengono inviate dai più disparati oggetti (Seduta, OggettoSeduta, Determina, Delibera, PropostaDelibera, VistoParere, Certificato etc),
 * vengono utilizzate diverse tecniche per la gestione di questa diversità, in particolare viene utilizzato l'overloading dei metodi (cioè più metodi con lo stesso nome
 * ma con parametri diversi) oppure con dei cast interni al metodo.
 *
 * Questa struttura ha alcuni pregi ma anche alcuni svantaggi. I pregi:
 * - le notifiche sono gestite tutte alla stessa maniera: si indica l'oggetto che si vuole notificare (Determina, Seduta, etc), a quali destinatari inviare (un array delle costanti DESTINATARI*)
 *   e quali allegati inviare (un array di ALLEGATO*)
 * - qualunque combinazione è possibile al minimo sforzo.
 * - per aggiungere nuovi tag, allegati o destinatari è sufficiente mapparli come costanti e creare il relativo metodo.
 *
 * Gli svantaggi:
 * - al momento tutti questi metodi di calcolo sono racchiusi in questo service che quindi perde un po' di leggibilità.
 * - il sistema è così molto modulare e flessibile, ma si perde un po' in prestazioni siccome alcune query verranno ripetute più volte.
 *
 * Regole generali per le notifiche:
 * - quando si invia una notifica: se c'è integrazione con jworklist, si invia tramite jworklist.
 * - si inviano sempre per email quelle definite nel dizionario della notifica.
 * - se la jworklist non è abilitata, si inviano le notifiche tramite email.
 * - FIXME: quando vanno create le attività sulla nostra tabella notifiche?
 *
 * Ci sono quattro sezioni principali:
 *
 * 1) metodi pubblici che possono essere usati "dall'esterno"
 * 2) metodi di calcolo dei soggetti a cui inviare le notifiche
 * 3) metodi di calcolo degli allegati da inviare
 * 4) metodi di calcolo dei campi da inserire nel testo e nell'oggetto delle notifiche.
 *
 * @author esasdelli
 *
 */
class NotificheService {

	public static final String OGGETTO_NOTIFICA_CAMBIO_STEP = "[OGGETTO]"
    public static final String TESTO_NOTIFICA_CAMBIO_STEP   = "[TIPOLOGIA] [ESTREMI_DOCUMENTO] - [STATO]"

    public static final def DESTINATARI = [
  CONVOCATI_SEDUTA 			: [nomeMetodo: "getConvocatiSeduta"				, descrizione: "I soggetti convocati alla seduta."    		]
, DIRIGENTE 	 			: [nomeMetodo: "getUtenteDirigente"				, descrizione: "Il dirigente dell'atto/proposta"            ]
, INTERNI   	 			: [nomeMetodo: "getUtentiDestinatariInterni"	, descrizione: "Destinatari interni della proposta/atto"    ]
, ESTERNI   	 			: [nomeMetodo: "getEmailDestinatariEsterni"		, descrizione: "Destinatari esterni della proposta/atto"    ]
, ATTORI_FLUSSO 			: [nomeMetodo: "getUtentiAttoriFlusso"			, descrizione: "Gli attori del flusso"                      ]
, ATTORI_FLUSSI_DOCUMENTO	: [nomeMetodo: "getUtentiAttoriTuttiFlussi"		, descrizione: "Gli attori di tutti i flussi, anche quelli chiusi"]
, ATTORI_VISTI  			: [nomeMetodo: "getUtentiAttoriVisti"			, descrizione: "Gli attori dei flussi dei visti/pareri"     ]
, ATTORI_STEP_CORRENTE 		: [nomeMetodo: "getUtentiAttoriStepCorrente"	, descrizione: "Gli attori dello step corrente"             ]
, ATTORI_STEP_DESTINAZIONE	: [nomeMetodo: "getUtentiAttoriStepCorrente"    , descrizione: "Gli attori dello step in cui si fermerà il flusso.", afterCommit:true]
, SEGRETARIO 	 			: [nomeMetodo: "getUtenteSegretario"			, descrizione: "Il segretario dell'oggetto seduta"          ]
, ORGANI_CONTROLLO			: [nomeMetodo: "getEmailOrganiControllo" 		, descrizione: "I destinatari degli organi di controllo"    ]
, RELATORE_PROPOSTA			: [nomeMetodo: "getRelatoreProposta"			, descrizione: "Relatore della proposta"            		]
, ATTORI_FLUSSO_TRANNE_STEP_CORRENTE 		: [nomeMetodo: "getUtentiAttoriFlussoTranneStepCorrente"	, descrizione: "Gli attori del flusso eccetto quelli del nodo corrente"]
, SOGGETTO_REDATTORE						: [nomeMetodo: "getSoggettoREDATTORE" 						, descrizione: "Il Redattore della Proposta."]
, SOGGETTO_FUNZIONARIO						: [nomeMetodo: "getSoggettoFUNZIONARIO" 					, descrizione: "Il Funzionario della Proposta."]
, SOGGETTO_DIRIGENTE						: [nomeMetodo: "getSoggettoDIRIGENTE" 						, descrizione: "Il Dirigente Firmatario della Proposta/Determina."]
, SOGGETTO_INCARICATO						: [nomeMetodo: "getSoggettoINCARICATO" 						, descrizione: "L'incaricato della Proposta/Determina."]
, FIRMATARIO_VISTO_CONTABILE				: [nomeMetodo: "getFirmatarioVistoContabile" 				, descrizione: "Il Firmatario del Visto Contabile."]
, FIRMATARIO_VISTO_NON_CONTABILE			: [nomeMetodo: "getFirmatarioVistoNonContabile" 			, descrizione: "Il Firmatario di tutti i visti NON contabili."]
, UTENTI_IN_UO_PROPONENTE 					: [nomeMetodo: "getUtentiInUoProponente" 					, descrizione: "Gli utenti che appartengono alla unità proponente."]
, UTENTI_IN_UO_PROPONENTE_E_FIGLIE			: [nomeMetodo: "getUtentiInUoProponenteEFiglie" 			, descrizione: "Gli utenti che appartengono alla unità proponente e sue figlie."]
, UTENTI_NOTIFICA_IN_UO_PROPONENTE 			: [nomeMetodo: "getUtentiNotificaInUoProponente" 			, descrizione: "Gli utenti che appartengono alla unità proponente che hanno il ruolo specificato nell'impostazione RUOLO_SO4_NOTIFICHE"]
, UTENTI_VERBALIZZAZIONE_IN_UO_PROPONENTE 	: [nomeMetodo: "getUtentiVerbalizzazioneInUoProponente" 	, descrizione: "Gli utenti che appartengono alla unità proponente che hanno il ruolo specificato nell'impostazione ODG_RUOLI_NOTIFICA_VERBALIZZAZIONE"]
, SOGGETTI_DIRIGENTI_IN_UO_E_PADRI          : [nomeMetodo: "getResposabiliInUoProponenteEPadri"         , descrizione: "Gli utenti RESPONSABILI e con RUOLO nell'unità del soggetto e nelle sue unità padri."]
]

    public static final def ALLEGATI = [
  TESTO 				: [nomeMetodo: 'getTesto', 				descrizione:'Il testo del documento']
, TESTO_PDF				: [nomeMetodo: 'getTestoPdf', 			descrizione:'Il testo del documento trasformato in PDF']
, STAMPA_UNICA 			: [nomeMetodo: 'getStampaUnica', 		descrizione:'La stampa unica']
, STAMPA_CONVOCAZIONE 	: [nomeMetodo: 'getStampaConvocazione',	descrizione:'La stampa della convocazione']
, TUTTI 				: [nomeMetodo: 'getTuttiFile',			descrizione:'Il testo dell\'atto e tutti i files ad esso associati (allegati, certificati, pareri e visti)']
, ZIP_TUTTI				: [nomeMetodo: 'getZipTuttiFile',		descrizione:'Zip del testo dell\'atto e di tutti i files ad esso associati (allegati, certificati, pareri e visti)']
]

	// i campi che sono disponibili nelle notifiche:
    public static final def CAMPI = [ TIPO_ATTO				: [metodo: "getTipoAtto"                , oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA"]																				, descrizione: "Il nome del tipo di atto, ad esempio 'determinazione' per le determine, 'deliberazione per le delibere', 'proposta di determina' e 'proposta di delibera'." ]
									, NUMERO				: [metodo: "getNumero"                  , oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA"]																				, descrizione: "Il numero dell'atto oppure della proposta se il documento non è definitivo." ]
									, ANNO					: [metodo: "getAnno"                    , oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA"]																				, descrizione: "L'anno dell'atto oppure della proposta se il documento non è definitivo." ]
									, REGISTRO				: [metodo: "getRegistro"                , oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA"]																				, descrizione: "Il codice del registro dell'atto se numerato oppure della proposta." ]
									, OGGETTO				: [metodo: "getOggetto"                 , oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA", "OGGETTO_SEDUTA"]																, descrizione: "L'oggetto dell'atto/proposta." ]
									, REDATTORE				: [metodo: "getRedattore"               , oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA", "OGGETTO_SEDUTA"]																, descrizione: "Il Redattore dell'atto/proposta." ]
									, NUMERO_ALBO			: [metodo: "getNumeroAlbo"              , oggetti: ["DETERMINA", "DELIBERA"]																									, descrizione: "Il Numero dell'albo, se presente." ]
									, ANNO_ALBO				: [metodo: "getAnnoAlbo"                , oggetti: ["DETERMINA", "DELIBERA"]																									, descrizione: "L'anno dell'albo, se presente." ]
									, DATA_INIZIO_PUBB		: [metodo: "getDataInizioPubblicazione" , oggetti: ["DETERMINA", "DELIBERA"]																									, descrizione: "La data di inizio pubblicazione, se presente." ]
									, DATA_FINE_PUBB		: [metodo: "getDataFinePubblicazione"   , oggetti: ["DETERMINA", "DELIBERA"]																									, descrizione: "La data di fine pubblicazione o 'fino a revoca', se presente oppure se fine a revoca." ]
									, UNITA_PROPONENTE		: [metodo: "getUnitaProponente"         , oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA"]																				, descrizione: "La descrizione dell'Unità Proponente." ]
									, N_PROPOSTA			: [metodo: "getNumeroProposta"          , oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA", "OGGETTO_SEDUTA"]																, descrizione: "Il numero della Proposta." ]
									, ANNO_PROPOSTA			: [metodo: "getAnnoProposta"            , oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA", "OGGETTO_SEDUTA"]																, descrizione: "L'anno della Proposta." ]
									, ESITO_PROPOSTA		: [metodo: "getEsitoProposta"           , oggetti: ["PROPOSTA_DELIBERA", "DELIBERA", "OGGETTO_SEDUTA"]																			, descrizione: "L'esito della proposta ottenuto in seduta." ]
									, NOTE_VERBALIZZAZIONE	: [metodo: "getNoteVerbalizzazione"     , oggetti: ["PROPOSTA_DELIBERA", "DELIBERA", "OGGETTO_SEDUTA"]																			, descrizione: "Le Note di Verbalizzazione scritte in seduta." ]
									, DATA_SEDUTA			: [metodo: "getDataSeduta"              , oggetti: ["PROPOSTA_DELIBERA", "DELIBERA", "OGGETTO_SEDUTA", "SEDUTA"]																, descrizione: "Data della Seduta di discussione." ]
									, ORA_SEDUTA			: [metodo: "getOraSeduta"               , oggetti: ["PROPOSTA_DELIBERA", "DELIBERA", "OGGETTO_SEDUTA", "SEDUTA"]																, descrizione: "Ora delle Seduta di discussione." ]
									, COMMISSIONE			: [metodo: "getCommissione"             , oggetti: ["PROPOSTA_DELIBERA", "DELIBERA", "SEDUTA", "OGGETTO_SEDUTA"]																, descrizione: "Commissione di discussione." ]
									, GIORNO_SEDUTA			: [metodo: "getGiornoSeduta"            , oggetti: ["PROPOSTA_DELIBERA", "DELIBERA", "SEDUTA", "OGGETTO_SEDUTA"]																, descrizione: "Nome del giorno per esteso (ad es. 'lunedì') di della seduta di discussione." ]
									, N_DELIBERA			: [metodo: "getNumero"                  , oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA"]																				, descrizione: "Numero della Delibera" ]
									, ANNO_DELIBERA			: [metodo: "getAnno"                    , oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA"]																				, descrizione: "Anno della Delibera" ]
									, DATA_INVIO 			: [metodo: "getDataInvio"               , oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA", "OGGETTO_SEDUTA", "SEDUTA", "ORGANI_DI_CONTROLLO", "CONTROLLO_REGOLARITA"]	, descrizione: "La data di invio della notifica." ]
									, DATA_PUBBLICAZIONE	: [metodo: "getDataPubblicazione"       , oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA", "ORGANI_DI_CONTROLLO"]														, descrizione: "Periodo di date di pubblicazione scelto nella notifica agli organi di controllo." ]
									, GIORNI_PUBB			: [metodo: "getGiorniPubblicazione"     , oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA"]																				, descrizione: "Numero dei giorni di pubblicazione se presenti le date di pubblicazione, oppure 'fino a revoca' se la pubblicazione è fino a revoca." ]
									, TIPOLOGIA				: [metodo: "getTipologia"               , oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA"]																				, descrizione: "Titolo per la notifica della Tipologia del documento se presente, altrimenti il nome della tipologia del documento." ]
									, TIPOLOGIA_ATTO		: [metodo: "getTipologiaDocumentoPrincipale", oggetti: ["VISTO_PARERE", "CERTIFICATI"]																							, descrizione: "Titolo per la notifica della Tipologia del documento principale se presente, altrimenti il nome della tipologia del documento principale." ]
									, ESTREMI_DOCUMENTO		: [metodo: "getEstremiDocumento"        , oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA"]																				, descrizione: "La dicitura: ATTO N.[N_ATTO]/[N_PROPOSTA] PROPOSTA N.[N_PROP]/[ANNO_PROP] se l'atto è numerato, altrimenti solo la parte relativa alla proposta." ]
									, STATO					: [metodo: "getStato"                   , oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA"]																				, descrizione: "Il nome del nodo corrente in cui si trova il documento." ]
									, URL_DOCUMENTO			: [metodo: "getUrlDocumento"            , oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA"]																				, descrizione: "Url a cui è possibile aprire direttamente il documento." ]
									, ELENCO_FORNITORI		: [metodo: "getElencoFornitoriDistintaAtto", oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA"]																			, descrizione: "Elenco dei fornitori associati al documento." ]
									, ELENCO_IMPEGNI		: [metodo: "getElencoImpegniDistintaAtto", oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA"]																				, descrizione: "Elenco degli impegni associati al documento." ]
									, IMPORTO				: [metodo: "getImportoDistintaAtto" 	, oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA"]																				, descrizione: "Importo totale della distinta associata al documento." ]
									, SCADENZA_DAL			: [metodo: "getScadenzaDalDistintaAtto" , oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA"]																				, descrizione: "Data minima di scadenza delle fatture associate al documento." ]
									, SCADENZA_AL			: [metodo: "getScadenzaAlDistintaAtto"  , oggetti: ["PROPOSTA_DELIBERA", "DETERMINA", "DELIBERA"]																				, descrizione: "Data massima di scadenza delle fatture associate al documento." ]
									, ELENCO_CONTROLLI		: [metodo: "getElencoAttiControllati"   , oggetti: ["CONTROLLO_REGOLARITA"]																										, descrizione: "Elenco degli atti sottoposti a controllo di regolarità." ]
									]

	// services
	StampaUnicaService              stampaUnicaService
	SpringSecurityService 			springSecurityService
    StrutturaOrganizzativaService 	strutturaOrganizzativaService
    SoggettiAttoriService         	soggettiAttoriService
    GestioneTestiService          	gestioneTestiService
    JWorklistDispatcher           	jworklistDispatcher
	AttiGestioneTesti               gestioneTesti

	// beans
	NotificheDispatcher notificheDispatcher
	IGestoreFile 		gestoreFile

	void notifica (String codiceNotifica, def documento, List<SoggettoNotifica> destinatari = null) {

		// ottengo tutte le notifiche valide per il codice richiesto.
		List<Notifica> notifiche = Notifica.perTipo(codiceNotifica, getTipoDocumento(documento)).list()

		for (Notifica notifica : notifiche) {
			this.notifica(notifica, documento, destinatari)
		}
	}

	private String getTipoDocumento (def documento) {
		if (documento instanceof Determina) {
			return Determina.TIPO_OGGETTO
		} else if (documento instanceof Delibera) {
			return Delibera.TIPO_OGGETTO
		} else if (documento instanceof PropostaDelibera) {
			return PropostaDelibera.TIPO_OGGETTO
		} else if (documento instanceof VistoParere) {
			return "VISTO_PARERE"
		} else if (documento instanceof Certificato) {
			return Certificato.TIPO_OGGETTO
		} else if (documento instanceof Seduta) {
			return "SEDUTA"
		} else if (documento instanceof OggettoSeduta) {
			return "OGGETTO_SEDUTA"
		} else if (documento instanceof OrganoControlloNotifica) {
			return "ORGANI_DI_CONTROLLO"
		} else if (documento instanceof ControlloRegolarita) {
			return "CONTROLLO_REGOLARITA"
		} else if (documento instanceof SedutaStampa) {
			return SedutaStampa.TIPO_OGGETTO
		}

		throw new AttiRuntimeException ("Tipo di documento non riconosciuto: ${documento}")
    }

	void notifica (Notifica notifica, def documento, List<SoggettoNotifica> destinatari = null, List<Allegato> listaAllegati = null) {
		// se devo inviare solo per email:
		switch (notifica.modalitaInvio) {
			case Notifica.MODALITA_EMAIL:
				notificaEmail (notifica, documento, destinatari, listaAllegati)
			break

			case Notifica.MODALITA_JWORKLIST:
				if (!Impostazioni.JWORKLIST.isDisabilitato()) {
					notificaJWorklist (notifica, documento, destinatari)
				} else {
					notificaEmail (notifica, documento, destinatari, listaAllegati)
				}
			break

            case Notifica.MODALITA_PEC:
                notificaPec(notifica, documento, destinatari)
            break

			default:
				throw new AttiRuntimeException ("Modalità di invio notifica non riconosciuta: ${notifica.modalitaInvio}")
        }
	}

    private void notificaPec (Notifica notifica, def documento, List<SoggettoNotifica> destinatari = null) {
        String testoNotifica 	= stampaUnione (documento, notifica.testo)
        String oggettoNotifica 	= stampaUnione (documento, notifica.oggetto)

        // se mi vengono passati i destinatari, invio la notifica solo a loro:
        if (destinatari == null) {
            destinatari = calcolaSoggettiNotifica (notifica, documento)
        }
        if (destinatari.size() > 0) {
            // la notifica pec è di default ASINCRONA siccome è invocata dall'utente.
            notificheDispatcher.notificaPec (documento, notifica.tipoNotifica, destinatari, testoNotifica, oggettoNotifica, false)
        }
    }

	private void notificaEmail (Notifica notifica, def documento, List<SoggettoNotifica> destinatari = null, List<Allegato> listaAllegati) {
		String testoNotifica 	= stampaUnione (documento, notifica.testo)
        String oggettoNotifica 	= stampaUnione (documento, notifica.oggetto)

        // se mi vengono passati i destinatari, invio la notifica solo a loro:
		List<String> listaIndirizzi = (destinatari?:calcolaSoggettiNotifica (notifica, documento)).findAll { it.email != null }?.email?.unique()
        if (listaIndirizzi?.size() > 0) {
			List<Allegato> allegatiEmail = listaAllegati ?: calcolaAllegati(documento, notifica.listaAllegati)
            invioNotificaEmail (oggettoNotifica, testoNotifica, listaIndirizzi, allegatiEmail)
		}
	}

	private void notificaJWorklist (Notifica notifica, def documento, List<SoggettoNotifica> destinatari = null) {
		String testoNotifica 	= notifica.testo
		String oggettoNotifica  = notifica.oggetto
		String priorita			= TipoNotifica.ASSEGNAZIONE == notifica.tipoNotifica ? JWorklistDispatcher.PRIORITA_ALTA : JWorklistDispatcher.PRIORITA_NORMALE
		String stepCorrente		= TipoNotifica.ASSEGNAZIONE == notifica.tipoNotifica ? documento.iter.stepCorrente.cfgStep.titolo : "Comunicazione"

		// calcolo il testo della notifica:
		testoNotifica 	= stampaUnione (documento, testoNotifica)
		oggettoNotifica = stampaUnione (documento, oggettoNotifica)

		// se mi vengono passati i destinatari, invio la notifica solo a loro
		def destinatariJWorklist = (destinatari?:calcolaSoggettiNotifica(notifica, documento))

		// invio la notifica jworklist ai destinatari che hanno almeno l'utente
		notificheDispatcher.notificaJWorklist(documento,
											  notifica,
											  oggettoNotifica,
											  testoNotifica,
											  destinatariJWorklist.findAll { it.utente != null && (
																			 it.utente?.ruoli*.ruolo?.contains(Holders.grailsApplication.config.grails.plugins.amministrazionedatabase.modulo + "_AGDAMMI") ||
																			 it.utente?.ruoli*.ruolo?.contains(Holders.grailsApplication.config.grails.plugins.amministrazionedatabase.modulo +"_"+ Impostazioni.RUOLO_ACCESSO_APPLICATIVO.valore)
											  								)},
											  priorita,
											  stepCorrente)

		// invio la notifica per email a tutti i soggetti di cui non ho utente ma email:
		List<String> listaIndirizzi = destinatariJWorklist.findAll { it.utente == null && it.email != null }?.email?.unique()
		if (listaIndirizzi?.size() > 0) {
			List<Allegato> allegatiEmail = calcolaAllegati(documento, notifica.listaAllegati)
			invioNotificaEmail (oggettoNotifica, testoNotifica, listaIndirizzi, allegatiEmail)
		}
	}

	/**
	 * Elimina tutte le notifiche di un certo tipo associate al documento non considerando gli utenti.
	 * (usata nel cambio step)
	 *
	 * Le notifiche che vengono eliminate sono di due tipi: ASSEGNAZIONE e "tutte le altre".
	 * Quindi passare come tipoNotifica=ASSEGNAZIONE eliminerà tutte le notifiche di tipo ASSEGNAZIONE per questo documento,
	 * passare invece tipoNotifica=
	 *
	 * @param documento 	il documento di cui eliminare le notifiche
	 * @param tipoNotifica 	il tipo di notifica da eliminare
	 */
	void eliminaNotifiche (def documento, String tipoNotifica = TipoNotifica.GENERICA_1) {
		// se l'integrazione non è abilitata, esco
		if (Impostazioni.JWORKLIST.isDisabilitato()) {
			return
		}

		String idRiferimento = jworklistDispatcher.getIdRiferimento(documento, tipoNotifica)
		notificheDispatcher.eliminaNotifiche (idRiferimento)
	}

	/**
	 * Elimina le notifiche di un certo utente o unità a seconda dell'impostazione.
	 *
	 * @param documento il documento di cui eliminare le notifiche
	 * @param utente	l'utente proprietario della notifica
	 * @param tipoNotifica il tipo di notifica da eliminare.
	 */
	void eliminaNotifica (def documento, Ad4Utente utente, String tipoNotifica = TipoNotifica.GENERICA_1) {
		// se l'integrazione non è abilitata, esco
		if (Impostazioni.JWORKLIST.isDisabilitato()) {
			return
        }

		String idRiferimento = jworklistDispatcher.getIdRiferimento(documento, tipoNotifica)
        notificheDispatcher.eliminaNotificheUtente(idRiferimento, utente)
	}

	/**
	 * Elimina le notifiche assegnate agli utenti di una certa unità.
	 * Viene invocata in caso di cambio unità dalla pagina di assistenza.
	 *
	 * @param documento il documento di cui eliminare le notifiche
	 * @param unitaSo4	l'unità di cui si vogliono eliminare le notifiche
	 * @param tipoNotifica il tipo di notifica da eliminare.
	 */
	void eliminaNotifica (def documento, So4UnitaPubb unitaSo4, String tipoNotifica = TipoNotifica.GENERICA_1) {
		// se l'integrazione non è abilitata, esco
		if (Impostazioni.JWORKLIST.isDisabilitato()) {
			return
        }

		String idRiferimento = jworklistDispatcher.getIdRiferimento(documento, tipoNotifica)
        notificheDispatcher.eliminaNotificheUnita(idRiferimento, unitaSo4)
	}

	/**
	 * Ritorna true se esiste una notifica del tipo richiesto per l'utente.
	 *
	 * Nota che le notifiche sono di due macro-tipi:
	 * - per competenza: sono le notifiche DA_FIRMARE e ASSEGNAZIONE
	 * - per conoscenza: tutte le altre notifiche.
	 *
	 * Queste due categorie hanno degli id_riferimento generati diversi per una retrocompatibilità col passato (che andrebbe smantellata).
	 *
	 * @param documento
	 * @param utente
	 * @return
	 */
	boolean isNotificaPresente (def documento, Ad4Utente utente, String tipoNotifica = TipoNotifica.GENERICA_1) {
		return jworklistDispatcher.esisteNotificaJWorklist(jworklistDispatcher.getIdRiferimento(documento, tipoNotifica), utente)
    }

	boolean isNotificaPresente (def documento, So4UnitaPubb unitaSo4, String tipoNotifica = TipoNotifica.GENERICA_1) {
		return jworklistDispatcher.esisteNotificaJWorklist(jworklistDispatcher.getIdRiferimento(documento, tipoNotifica), unitaSo4)
    }

	void invioNotificaEmail (def documento, Notifica notifica, List<String> indirizziEmail, List<Allegato> allegatiEmail = null) {
		String testoNotifica 	= stampaUnione (documento, notifica.testo)
        String oggettoNotifica 	= stampaUnione (documento, notifica.oggetto)

        invioNotificaEmail(oggettoNotifica, testoNotifica, indirizziEmail, allegatiEmail)
    }

	void invioNotificaEmail (String oggettoNotifica, String testoNotifica, List<String> destinatariEmail, List<Allegato> allegatiEmail = null) {
		if (destinatariEmail.size() == 0) {
			// non ho email da inviare, non faccio neanche la fatica di provarci.
			return
        }

		notificheDispatcher.notificaEMail(Impostazioni.ALIAS_INVIO_MAIL.valore, Impostazioni.MITTENTE_INVIO_MAIL.valore, destinatariEmail, testoNotifica, oggettoNotifica, allegatiEmail)
    }

	List<Allegato> calcolaAllegati (def documento, List<String> allegati) {
		List<Allegato> allegatiMail = []

        if (allegati == null)
			return allegatiMail

        for (String allegato : allegati) {
			String metodo = ALLEGATI[allegato]?.nomeMetodo
            if (metodo == null) {
				continue
            }
			def a = "${metodo}"(documento)
            if (a != null) {
				if (a instanceof Allegato){
					allegatiMail.add(a)
                }
				else return a
            }
		}

		return allegatiMail
    }

	String stampaUnione (def documento, String testo) {
        if (testo == null) {
            return ""
        }

		// ciclo sui campi presenti nel testo e li sostituisco
		for (def match : (testo =~ /\[([\w]+)\]/)) {

			String matchTotale 	= match[0] // il nome del campo con le quadre, ad es: [N_PROPOSTA]
			String campo   		= match[1] // il nome del campo senza le quadre, ad es: N_PROPOSTA

			// se non trovo il campo, sparo un errore:
			if (CAMPI[campo] == null) {
				throw new AttiRuntimeException ("Non è possibile sostituire il campo $campo.")
			}

			// eseguo la funzione per ottenere il valore da sostituire
			String value = "${CAMPI[campo].metodo}"(documento)

			if (value == null) {
				value = ""
			}

			// lo sostituisco
			testo = testo.replace(matchTotale, value)
		}

		return testo
	}

	String getElencoImpegniDistintaAtto(IDocumentoIterabile documentoIterabile){
		DistintaAtto distinta = getDistinta(documentoIterabile)
		return distinta?.elencoImpegni ? "IMPEGNI: "+ distinta?.elencoImpegni : ""
	}

	String getElencoFornitoriDistintaAtto(IDocumentoIterabile documentoIterabile){
		DistintaAtto distinta = getDistinta(documentoIterabile)
		return distinta?.elencoFornitori ? "FORNITORI: "+distinta?.elencoFornitori : ""
	}

	String getScadenzaDalDistintaAtto(IDocumentoIterabile documentoIterabile){
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy")
		DistintaAtto distinta = getDistinta(documentoIterabile)
		return distinta?.scadenzaDal ? "DATE: "+dateFormatter.format(distinta.scadenzaDal): ""
	}

	String getScadenzaAlDistintaAtto(IDocumentoIterabile documentoIterabile){
		SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy")
		DistintaAtto distinta = getDistinta(documentoIterabile)
		return distinta?.scadenzaAl ? dateFormatter.format(distinta.scadenzaAl): ""
	}

	String getImportoDistintaAtto(IDocumentoIterabile documentoIterabile){
		DistintaAtto distinta = getDistinta(documentoIterabile)
		DecimalFormat formatter = new DecimalFormat("#,###.00");
		return distinta?.importo ? "IMPORTO EURO: "+formatter.format(distinta.importo): ""
	}

	private DistintaAtto getDistinta(IDocumentoIterabile documentoIterabile){
		def proposta = null
		if (documentoIterabile instanceof IDocumentoCollegato) {
			proposta =  documentoIterabile.documentoPrincipale.proposta
		} else if (documentoIterabile instanceof IAtto) {
			proposta =  documentoIterabile.proposta
		} else if (documentoIterabile instanceof IProposta) {
			proposta = documentoIterabile
		}
		if (proposta != null) {
			return DistintaAtto.findByAnnoPropostaAndNumeroPropostaAndUnitaProponente(proposta.annoProposta, proposta.numeroProposta, (proposta.registroProposta?.registroEsterno ?: proposta.registroProposta?.codice))
		}
		return null;
	}

	List<SoggettoNotifica> calcolaSoggettoNotifica (NotificaEmail notificaEmail, def documento, String funzione) {
		String metodo = DESTINATARI[funzione].nomeMetodo
        def dest = "${metodo}"(documento)

        // filtro i soggetti per ruolo/unità
		dest = filtraSoggetti (notificaEmail, dest)

        return dest
    }

	List<SoggettoNotifica> calcolaSoggettiNotifica (Notifica notifica, def documento) {
		def soggettiNotifica = []

        // calcolo i soggetti a cui mandare la notifica
		for (NotificaEmail notificaEmail : notifica.notificheEmail) {

			// se la notifica email contiene un indirizzo email, uso quello:
			if (notificaEmail.email?.indirizzoEmail != null) {
				soggettiNotifica << new SoggettoNotifica (email: notificaEmail.email.indirizzoEmail)
				continue
			}

			// se la notifica email contiene una funzione, la eseguo:
			if (notificaEmail.funzione != null) {
				// se la funzione è di tipo "after commit", non la eseguo subito, ma registro che andrà eseguita.
				// Questo serve per gestire il caso delle notifiche che vanno valutate solo quando la navigazione del flusso è terminata.
				if (DESTINATARI[notificaEmail.funzione].afterCommit) {
					notificheDispatcher.notificaAfterCommit (notificaEmail.id, documento)
				} else {
    				def dest = calcolaSoggettoNotifica (notificaEmail, documento, notificaEmail.funzione)
                    soggettiNotifica.addAll(dest)
                }

				// in ogni caso, dopo aver valutato, proseguo il for.
				continue
			}

			// se la notifica email contiene un soggetto, lo aggiungo:
			if (notificaEmail.soggetto != null) {
				soggettiNotifica.add(new SoggettoNotifica (email: notificaEmail.soggetto?.indirizzoWeb, soggetto: notificaEmail.soggetto, utente:  notificaEmail.soggetto.utenteAd4))
				continue
			}

			// se la notifica email contiene un ruolo e una unità, aggiungo tutti gli utenti di quella unità con quel ruolo:
			if (notificaEmail.ruolo != null && notificaEmail.unita != null) {
				def componenti = getUtentiConRuoloInUnita(notificaEmail.ruolo.ruolo, notificaEmail.unita)
				soggettiNotifica.addAll(componenti)
				continue
			}

			// se la notifica email contiene solo un ruolo, aggiungo gli utenti con quel ruolo:
			if (notificaEmail.ruolo != null) {
				def componenti = strutturaOrganizzativaService.getComponentiConRuoloInOttica(notificaEmail.ruolo.ruolo, Impostazioni.OTTICA_SO4.valore)
                for (def c : componenti) {
					soggettiNotifica << new SoggettoNotifica( utente: 	c.soggetto.utenteAd4
															, email: 	c.soggetto.indirizzoWeb
															, soggetto:	c.soggetto
															, ruoloAd4:	notificaEmail.ruolo
															, assegnazione: DestinatarioNotificaAttivita.NOTIFICA_UTENTE);
				}

				continue
            }

			// se la notifica email contiene solo una unità, aggiungo gli utenti di quella unità:
			if (notificaEmail.unita != null) {
				def componenti = So4ComponentePubb.componentiUnitaPubb (notificaEmail.unita.progr, notificaEmail.unita.ottica.codice, new Date()).list()
                for (def c:componenti) {
					if (c.soggetto.utenteAd4 != null) {
						soggettiNotifica << new SoggettoNotifica( utente: 		c.soggetto.utenteAd4
																, email: 		c.soggetto.indirizzoWeb
																, soggetto:		c.soggetto
																, assegnazione: DestinatarioNotificaAttivita.NOTIFICA_UNITA
																, unitaSo4: 	notificaEmail.unita)
                    }
				}
				continue
            }
		}

		// elimino i doppi.
		soggettiNotifica.unique()

        return soggettiNotifica
    }

	private List<SoggettoNotifica> filtraSoggetti (NotificaEmail notifica, List<SoggettoNotifica> destinatari) {
		if (notifica.ruolo == null && notifica.unita == null) {
			return destinatari
        }

		List<SoggettoNotifica> destinatariFiltrati = []

        // ottengo tutti i soggetti che hanno il ruolo richiesto per l'unità richiesta
		for (SoggettoNotifica d : destinatari) {

			if (notifica.ruolo != null || notifica.unita != null) {
				boolean result = strutturaOrganizzativaService.soggettoHaRuoloPerUnita(d.soggetto.id, notifica.ruolo?.ruolo, notifica.unita?.progr?:-1, Impostazioni.OTTICA_SO4.valore)

				if (result) {
					destinatariFiltrati << d
                }
			}
		}

		return destinatariFiltrati
    }

	/* *****************************************
	 *
	 *  Metodi di calcolo dei valori per la "stampa unione"
	 *
	 * *****************************************/

	String getNumeroAlbo (IAtto atto) {
		return atto.numeroAlbo?.toString()?:""
	}

	String getAnnoAlbo (IAtto atto) {
		return atto.annoAlbo?.toString()?:""
	}

	String getDataInizioPubblicazione (IAtto documento) {
		// se non ho la data di pubblicazione, la calcolo al volo.
		return (documento.dataPubblicazione?:new Date()).format("dd/MM/yyyy")
	}

	String getDataFinePubblicazione (IAtto documento) {
		if (documento.pubblicaRevoca && documento.dataFinePubblicazione == null) {
			return "fino a revoca"
		}
		
		if (documento.dataFinePubblicazione != null) {
			return documento.dataFinePubblicazione.format("dd/MM/yyyy")
		}
		
		// se non ho la data di fine pubblicazione, faccio un "guess", calcolando dalla data di inizio:
		return ((documento.dataPubblicazione?:new Date()) + (documento.giorniPubblicazione)).format("dd/MM/yyyy")
	}

	String getGiorniPubblicazione (IAtto documento) {
		if (documento.pubblicaRevoca) {
			return "fino a revoca"
		}

		if (documento.giorniPubblicazione != null) {
			return Integer.toString(documento.giorniPubblicazione)
		}

		return ""
	}

	String getTipoAtto (IDocumentoIterabile documentoIterabile) {
		if (documentoIterabile instanceof Determina) {
			return (documentoIterabile.numeroDetermina > 0 ? "determinazione" : "proposta di determina")
		}

		if (documentoIterabile instanceof Delibera) {
			return "deliberazione"
		}

		if (documentoIterabile instanceof VistoParere) {
			return documentoIterabile.tipologia.titolo
        }

		if (documentoIterabile instanceof PropostaDelibera) {
			return "proposta di delibera"
		}

		if (documentoIterabile instanceof Certificato) {
			return documentoIterabile.tipologia.titolo
        }

		return ""
    }

	String getNumeroProposta (OggettoSeduta oggettoSeduta) {
		return getNumeroProposta(oggettoSeduta.propostaDelibera)
    }

	String getAnnoProposta (OggettoSeduta oggettoSeduta) {
		return getAnnoProposta(oggettoSeduta.propostaDelibera)
    }

	String getEsitoProposta (OggettoSeduta oggettoSeduta) {
		return oggettoSeduta.esito?.titolo?:""
    }

	String getOggetto(OggettoSeduta oggettoSeduta) {
		return getOggetto(oggettoSeduta.propostaDelibera)
    }
	
	String getNoteVerbalizzazione (OggettoSeduta oggettoSeduta) {
		return oggettoSeduta.note?:""
    }

	String getNumeroProposta (IDocumentoIterabile documento) {
		def d = documento

        if (documento instanceof IDocumentoCollegato) {
			d = documento.getDocumentoPrincipale()
        }

		if (d instanceof IAtto) {
			return d.proposta.numeroProposta?:""
        } else if (d instanceof IProposta) {
			return d.numeroProposta?:""
        }
	}

	String getAnnoProposta (IDocumentoIterabile documento) {
		def d = documento

        if (documento instanceof IDocumentoCollegato) {
			d = documento.getDocumentoPrincipale()
        }

		if (d instanceof IAtto) {
			return d.proposta.annoProposta?:""
        } else if (d instanceof IProposta) {
			return d.annoProposta?:""
        }
	}

	String getNumero(IDocumentoIterabile documentoIterabile) {
		if (documentoIterabile instanceof IDocumentoCollegato) {
			documentoIterabile = documentoIterabile.documentoPrincipale
        }

		// se ho un documento di tipo proposta, do precedenza all'anno dell'atto collegato
		if (documentoIterabile instanceof IProposta && documentoIterabile.atto?.numeroAtto > 0) {
			return documentoIterabile.atto.numeroAtto?:""
        }

		if (documentoIterabile instanceof IAtto) {
			return (documentoIterabile.numeroAtto > 0 ? documentoIterabile.numeroAtto : documentoIterabile.proposta.numeroProposta)?:""
        }

		if (documentoIterabile instanceof IProposta) {
			return documentoIterabile.numeroProposta?:""
        }

		return ""
    }

	String getNumero (OggettoSeduta oggettoSeduta) {
		return getNumero(oggettoSeduta.propostaDelibera)
	}

	String getAnno (IDocumentoIterabile documentoIterabile) {
		if (documentoIterabile instanceof IDocumentoCollegato) {
			documentoIterabile = documentoIterabile.documentoPrincipale
        }

		// se ho un documento di tipo proposta, do precedenza all'anno dell'atto collegato
		if (documentoIterabile instanceof IProposta && documentoIterabile.atto?.annoAtto > 0) {
			return documentoIterabile.atto.annoAtto?:""
        }

		if (documentoIterabile instanceof IAtto) {
			return (documentoIterabile.annoAtto > 0 ? documentoIterabile.annoAtto : documentoIterabile.proposta.annoProposta)?:""
        }

		if (documentoIterabile instanceof IProposta) {
			return documentoIterabile.annoProposta?:""
        }

		return ""
    }

	String getRegistro (IDocumentoIterabile documentoIterabile) {
		if (documentoIterabile instanceof IDocumentoCollegato) {
			documentoIterabile = documentoIterabile.documentoPrincipale
        }

		if (documentoIterabile instanceof IAtto) {
			return (documentoIterabile.annoAtto > 0 ? documentoIterabile.registroAtto.codice : documentoIterabile.proposta.registroProposta?.codice)
        }

		if (documentoIterabile instanceof IProposta) {
			return documentoIterabile.registroProposta?.codice
        }

		return ""
    }

	String getOggetto (IDocumentoIterabile documentoIterabile) {
		if (documentoIterabile instanceof IDocumentoCollegato) {
			documentoIterabile = documentoIterabile.documentoPrincipale
		}

		return documentoIterabile.oggetto
	}

	String getUnitaProponente(IDocumentoIterabile documentoIterabile) {
		if (documentoIterabile instanceof IDocumentoCollegato) {
			documentoIterabile = documentoIterabile.documentoPrincipale
        }

		if (documentoIterabile instanceof IAtto) {
			return documentoIterabile.proposta.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.descrizione
		} else {
			return documentoIterabile.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.descrizione
		}
	}

	String getRedattore (IDocumentoIterabile documentoIterabile) {
		if (documentoIterabile instanceof IDocumentoCollegato) {
			documentoIterabile = documentoIterabile.documentoPrincipale
		}

		if (documentoIterabile instanceof IAtto) {
			return documentoIterabile.proposta.getSoggetto(TipoSoggetto.REDATTORE)?.utenteAd4?.nominativoSoggetto
		} else {
			return documentoIterabile.getSoggetto(TipoSoggetto.REDATTORE)?.utenteAd4?.nominativoSoggetto
		}
	}

	String getRedattore (OggettoSeduta oggettoSeduta) {
        return getRedattore(oggettoSeduta.propostaDelibera)
	}

	String getEsitoProposta (IDocumentoIterabile documento) {
		return documento.oggettoSeduta?.esito?.titolo?:""
    }

	String getNoteVerbalizzazione (IDocumentoIterabile documento) {
		return documento.oggettoSeduta?.note?:""
    }
	
	String getCommissione (IDocumentoIterabile documento) {
		return getCommissione(documento.oggettoSeduta)
    }
	
	String getCommissione (OggettoSeduta oggettoSeduta) {
		return getCommissione(oggettoSeduta?.seduta)
    }

	String getCommissione (Seduta seduta) {
		return seduta?.commissione?.titolo?:""
    }

	String getGiornoSeduta (IDocumentoIterabile documento) {
		return getGiornoSeduta(documento.oggettoSeduta)
	}
	
	String getGiornoSeduta (OggettoSeduta oggettoSeduta) {
		return getGiornoSeduta(oggettoSeduta?.seduta)
    }
	
	String getGiornoSeduta (Seduta seduta) {
		return seduta?.dataSeduta?.format("EEEE")?:""
    }
	
	String getDataInvio (def documento) {
		return new Date().format("dd/MM/yyyy")
    }
	
	String getDataSeduta (IDocumentoIterabile documento) {
		String valore = ""
		if (documento.oggettoSeduta)
			valore = getDataSeduta (documento.oggettoSeduta)
		return valore
    }
	
	String getDataSeduta (OggettoSeduta oggettoSeduta) {
		return getDataSeduta(oggettoSeduta?.seduta)
	}
	
	String getDataSeduta (Seduta seduta) {
		return seduta?.dataSeduta?.format("dd/MM/yyyy")?:""
    }
	
	String getOraSeduta (IDocumentoIterabile documento) {
		return documento.oggettoSeduta?.getOraSeduta(documento.oggettoSeduta)?:""
    }
	
	String getOraSeduta (OggettoSeduta oggettoSeduta) {
		return getOraSeduta(oggettoSeduta?.seduta)
	}
	
	String getOraSeduta (Seduta seduta) {
		return seduta?.oraSeduta?:""
    }
	
	String getDataPubblicazione (OrganoControlloNotifica o) {
		def minmax = OrganoControlloNotificaDocumento.createCriteria().get {
			createAlias("determina", "dete", CriteriaSpecification.LEFT_JOIN)
			createAlias("delibera",  "deli", CriteriaSpecification.LEFT_JOIN)
			projections {
				max("dete.dataPubblicazione")   // 0
				min("dete.dataPubblicazione")   // 1
				max("deli.dataPubblicazione")   // 2
				min("deli.dataPubblicazione")   // 3
			}

			eq ("organoControlloNotifica", o)
		}

		if (minmax == null) {
			return ""
        }

		Date min = minmax[1]?:minmax[3]
		Date max = minmax[0]?:minmax[2]

		if (min.compareTo(max) == 0) {
			return min.format("dd/MM/yyyy")
        } else {
			return "dal ${min.format("dd/MM/yyyy")} al ${max.format("dd/MM/yyyy")}"
		}
	}

	String getDataPubblicazione (IDocumentoIterabile documento) {
		if (documento.dataPubblicazione2 != null) {
			return documento.dataPubblicazione2.format("dd/MM/yyyy")
        } else {
			return documento.dataPubblicazione?.format("dd/MM/yyyy")?:""
        }
	}

	String getUrlDocumento (IDocumentoIterabile documento) {
		return jworklistDispatcher.getUrlDocumento(documento)
	}

	String getStato (IDocumentoIterabile documento) {
		String testo = ""
		if (documento.iter != null)
			testo = documento.iter.stepCorrente.cfgStep.titolo
		return testo
	}

	String getStato (OggettoSeduta oggettoSeduta) {
		String testo = ""
		if (oggettoSeduta.propostaDelibera != null)
			testo = oggettoSeduta.propostaDelibera.iter.stepCorrente.cfgStep.titolo
		return testo
	}

	String getEstremiDocumento (IDocumentoIterabile documento) {
		String testoAtto	 = "ATTO N.[NUMERO]/[ANNO] "
        String testoProposta = "PROPOSTA N.[N_PROPOSTA]/[ANNO_PROPOSTA]"
        String testo = ""
        if (documento instanceof IDocumentoCollegato) {
			documento = documento.documentoPrincipale
        }

		if (documento instanceof IAtto && documento.numeroAtto > 0) {
			testo += stampaUnione(documento, testoAtto)
            testo += stampaUnione(documento.proposta, testoProposta)
            return testo
        }

		if (documento instanceof IProposta && documento.numeroProposta > 0) {
			testo += stampaUnione(documento, testoProposta)
            return testo
        }

		return testo
    }

	String getEstremiDocumento (OggettoSeduta oggettoSeduta) {
		if (oggettoSeduta.delibera != null) {
			return getEstremiDocumento(oggettoSeduta.propostaDelibera)
		} else if (oggettoSeduta.propostaDelibera != null) {
			return getEstremiDocumento(oggettoSeduta.propostaDelibera)
		} else if (oggettoSeduta.determina != null) {
			return getEstremiDocumento(oggettoSeduta.determina)
		}
		return "";
	}

	String getTipologia (OggettoSeduta oggettoSeduta) {
		return getTipologia(oggettoSeduta.propostaDelibera)
	}

	String getTipologia (IDocumentoIterabile documento) {
		if (documento.tipologiaDocumento.hasProperty("titoloNotifica") && documento.tipologiaDocumento.titoloNotifica != null) {
			return documento.tipologiaDocumento.titoloNotifica
		} else {
			return documento.tipologiaDocumento.titolo
        }
	}

	String getTipologiaDocumentoPrincipale (def documento){
		if (documento instanceof IDocumentoCollegato) {
			return getTipologia(documento.documentoPrincipale);
		}
		return "";
	}

	/* *****************************************
	 *
	 *  Metodi di calcolo degli indirizzi email.
	 *
	 * *****************************************/

	List<SoggettoNotifica> getEmailDestinatariEsterni (def documento) {
        List<SoggettoNotifica> destinatari = []
        def destinatariNotifiche = (documento instanceof Delibera) ? documento.propostaDelibera.destinatariNotifiche : documento.destinatariNotifiche
        for (DestinatarioNotifica destinatarioNotifica : destinatariNotifiche) {
            if (destinatarioNotifica.tipoDestinatario == DestinatarioNotifica.TIPO_DESTINATARIO_ESTERNO) {
                if (destinatarioNotifica.email?.indirizzoEmail?.trim()?.length() > 0) {
                    destinatari << new SoggettoNotifica(email: destinatarioNotifica.email.indirizzoEmail, destinatarioNotifica:destinatarioNotifica)
                }
            }
        }

        return destinatari
	}

	List<SoggettoNotifica> getUtenteDirigente (def documento) {
		if (documento instanceof IDocumentoCollegato) {
			documento = documento.documentoPrincipale
		}

		def utente = documento.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4

        if (utente == null) {
			return []
        } else {
			def soggetto = As4SoggettoCorrente.findByUtenteAd4(utente)
            return [new SoggettoNotifica(utente:utente, email:soggetto?.indirizzoWeb, soggetto:soggetto)]
        }
	}

	List<SoggettoNotifica> getRelatoreProposta (PropostaDelibera propostaDelibera) {
		if (propostaDelibera.delega?.assessore != null) {
			return [new SoggettoNotifica(email:	propostaDelibera.delega?.assessore.indirizzoWeb
									, soggetto:	propostaDelibera.delega?.assessore)]
		}

		return []
	}

	List<SoggettoNotifica> getUtenteSegretario (OggettoSeduta oggettoSeduta) {
		SedutaPartecipante segretario = SedutaPartecipante.createCriteria().get() {
			eq("seduta.id", oggettoSeduta.seduta.id)
			ruoloPartecipante {
				eq("codice", RuoloPartecipante.CODICE_SEGRETARIO)
			}
			fetchMode("commissioneComponente", FetchMode.JOIN)
			fetchMode("ruoloPartecipante", FetchMode.JOIN)
			fetchMode("commissioneComponente.componente", FetchMode.JOIN)
			fetchMode("componenteEsterno", FetchMode.JOIN)
		}

		if (segretario == null) {
			return []
		}

		As4SoggettoCorrente soggetto = (segretario.componenteEsterno)?segretario.componenteEsterno:segretario.commissioneComponente.componente
		return [new SoggettoNotifica(soggetto:soggetto, email:soggetto.indirizzoWeb, utente:soggetto.utenteAd4)]
    }

    List<SoggettoNotifica> getDestinatariInterni (def documento) {
        List<SoggettoNotifica> soggetti = []

        for (DestinatarioNotifica destinatario : documento.destinatariNotifiche) {
            if (destinatario.tipoDestinatario == DestinatarioNotifica.TIPO_DESTINATARIO_ESTERNO) {
                continue
            }

            if (destinatario.utente != null) {
                As4SoggettoCorrente soggetto = As4SoggettoCorrente.findByUtenteAd4(destinatario.utente)
                soggetti << new SoggettoNotifica(utente:destinatario.utente, competenza: destinatario.tipoNotifica, email: soggetto?.indirizzoWeb, soggetto:soggetto, destinatarioNotifica: destinatario)
                continue
            }

            // aggiungo i soggetti per le unità
            if (destinatario.unitaSo4 != null) {
                soggetti.addAll(getSoggettiNotificaUnita(destinatario.unitaSo4))
            }
        }

        return soggetti
    }

	List<SoggettoNotifica> getUtentiDestinatariInterni (def documento) {
		def utenti = []

		def destinatari =  DestinatarioNotifica.createCriteria().list() {
			if (documento instanceof Delibera) {
				eq(GrailsNameUtils.getPropertyName(GrailsHibernateUtil.unwrapIfProxy(documento.propostaDelibera).class) + '.id', documento.propostaDelibera.id)
			}
			else {
				eq(GrailsNameUtils.getPropertyName(GrailsHibernateUtil.unwrapIfProxy(documento).class) + '.id', documento.id)
			}
			eq ('tipoDestinatario', DestinatarioNotifica.TIPO_DESTINATARIO_INTERNO)
		}

		for (DestinatarioNotifica d : destinatari) {
			if (d.utente != null) {
				// aggiungo l'utente solo se ha il ruolo definito nelle impostazioni:
				boolean hasRuolo = (strutturaOrganizzativaService.utenteHasRuoloDaImpostazioni(d.utente.id, Impostazioni.OTTICA_SO4.toString(), Impostazioni.RUOLO_SO4_NOTIFICHE.toString()))
				if (hasRuolo) {
					def soggetto = As4SoggettoCorrente.findByUtenteAd4(d.utente)
                    utenti.add(new SoggettoNotifica(utente:d.utente, competenza: d.tipoNotifica, email: soggetto?.indirizzoWeb, soggetto:soggetto, destinatarioNotifica: d))
				}
				continue
            }

			// aggiungo i soggetti per le unità
			if (d.unitaSo4 != null) {
				utenti.addAll(getUtentiConRuoloInUnita(Impostazioni.RUOLO_SO4_NOTIFICHE.valore, d.unitaSo4, d))
            }
		}

		return utenti
    }

	List<SoggettoNotifica> getUtentiConRuoloInUnita (String ruolo, So4UnitaPubb unitaSo4, DestinatarioNotifica destinatarioNotifica = null) {
		def utenti = []
        def componenti = strutturaOrganizzativaService.getComponentiConRuoloInUnita(ruolo, unitaSo4.progr, unitaSo4.ottica.codice)

        for (def c : componenti) {
			// seleziono solo i componenti che hanno un utente ad4.
			if (c.soggetto.utenteAd4 != null) {
				utenti.add(new SoggettoNotifica(  utente: 		c.soggetto.utenteAd4
												, email: 		c.soggetto.indirizzoWeb
												, soggetto:		c.soggetto
												, competenza: 	destinatarioNotifica?.tipoNotifica?:DestinatarioNotifica.TIPO_NOTIFICA_CONOSCENZA
												, destinatarioNotifica: destinatarioNotifica
												, assegnazione: DestinatarioNotificaAttivita.NOTIFICA_UNITA
												, unitaSo4: 	unitaSo4
												, ruoloAd4:		Ad4Ruolo.get(ruolo)))
            }
		}

		return utenti
    }

	List<SoggettoNotifica> getUtentiAttoriVisti (IDocumentoIterabile documento) {
		def soggetti = []

		for (VistoParere v : documento.visti) {
			if (v.valido && v.iter != null) {
				soggetti.addAll(getUtentiAttoriFlusso(v))
			}
		}

		return soggetti
	}

	List<SoggettoNotifica> getUtentiAttoriVisti (OggettoSeduta oggettoSeduta) {
		return getUtentiAttoriVisti(oggettoSeduta.propostaDelibera)
    }

	List<SoggettoNotifica> getUtentiAttoriFlusso(IDocumentoIterabile documento) {
		return soggettiAttoriService.calcolaSoggettiNotificaFlusso(documento.iter)
	}

	List<SoggettoNotifica> getUtentiAttoriTuttiFlussi(IDocumentoIterabile documento) {
		return soggettiAttoriService.calcolaSoggettiNotificaTuttiFlussi(documento)
	}

	List<SoggettoNotifica> getUtentiAttoriFlussoTranneStepCorrente(OggettoSeduta oggettoSeduta) {
		return getUtentiAttoriFlussoTranneStepCorrente(oggettoSeduta.propostaDelibera)
	}

	List<SoggettoNotifica> getUtentiAttoriFlussoTranneStepCorrente(IDocumentoIterabile documento) {
		return soggettiAttoriService.calcolaSoggettiNotificaFlussoTranneStepCorrente(documento.iter)
	}

	List<SoggettoNotifica> getUtentiAttoriStepCorrente(IDocumentoIterabile documento) {
		return soggettiAttoriService.calcolaSoggettiNotificaStepCorrente(documento.iter)
    }

	List<SoggettoNotifica> getUtentiNotificaInUoProponente (IDocumentoIterabile documento) {
		return getUtentiConRuoloInUoProponente(documento, Impostazioni.RUOLO_SO4_NOTIFICHE.valore)
    }

	List<SoggettoNotifica> getUtentiVerbalizzazioneInUoProponente (OggettoSeduta oggettoSeduta) {
		return getUtentiVerbalizzazioneInUoProponente(oggettoSeduta.propostaDelibera)
    }

	List<SoggettoNotifica> getUtentiVerbalizzazioneInUoProponente (IDocumentoIterabile documento) {
		def soggettiNotifica = []
        for (String ruolo : Impostazioni.ODG_RUOLI_NOTIFICA_VERBALIZZAZIONE.valori) {
			soggettiNotifica.addAll(getUtentiConRuoloInUoProponente (documento, ruolo))
        }
		return soggettiNotifica
    }

	List<SoggettoNotifica> getFirmatarioVistoContabile (IDocumentoIterabile documento) {
		List<SoggettoNotifica> firmatari = []

        for (VistoParere v : documento.visti) {
			if (v.valido && v.firmatario != null && v.tipologia.contabile) {
				As4SoggettoCorrente soggetto = As4SoggettoCorrente.findByUtenteAd4(v.firmatario)
                firmatari << new SoggettoNotifica(utente: v.firmatario, email:soggetto?.indirizzoWeb, soggetto:soggetto)
            }
		}

		return firmatari
    }

	List<SoggettoNotifica> getFirmatarioVistoNonContabile (IDocumentoIterabile documento) {
		List<SoggettoNotifica> firmatari = []

        for (VistoParere v : documento.visti) {
			if (v.valido && v.firmatario != null && !v.tipologia.contabile) {
				As4SoggettoCorrente soggetto = As4SoggettoCorrente.findByUtenteAd4(v.firmatario)
                firmatari << new SoggettoNotifica(utente: v.firmatario, email:soggetto?.indirizzoWeb, soggetto:soggetto)
            }
		}

		return firmatari
    }

	List<SoggettoNotifica> getUtentiConRuoloInUoProponente (IDocumentoIterabile documento, String ruolo) {
		if (documento instanceof IAtto) {
			documento = documento.proposta
        }

		def uoProponente = documento.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4
		def componenti   = strutturaOrganizzativaService.getComponentiConRuoloInUnita (ruolo, uoProponente.progr, Impostazioni.OTTICA_SO4.valore)
		return componenti.collect { new SoggettoNotifica(utente: it.soggetto.utenteAd4, unitaSo4: uoProponente, email:it.soggetto.indirizzoWeb, soggetto:it.soggetto, assegnazione: DestinatarioNotificaAttivita.NOTIFICA_UNITA) }
	}

	List<SoggettoNotifica> getUtentiInUoProponente (IDocumentoIterabile documento) {
		if (documento instanceof IDocumentoCollegato) {
			documento = documento.documentoPrincipale
		}

		if (documento instanceof IAtto) {
			documento = documento.proposta
		}

		So4UnitaPubb uoProponente = documento.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4
        return getSoggettiNotificaUnita(uoProponente)
	}

    List<SoggettoNotifica> getSoggettiNotificaUnita (So4UnitaPubb unita) {
        def componenti   = strutturaOrganizzativaService.getComponentiInUnita (unita)
        return componenti.collect { new SoggettoNotifica(utente: it.soggetto.utenteAd4, unitaSo4: unita, email:it.soggetto.indirizzoWeb, soggetto:it.soggetto, assegnazione: DestinatarioNotificaAttivita.NOTIFICA_UNITA) }
    }

	List<SoggettoNotifica> getSoggettoREDATTORE (IDocumentoIterabile documento) {
		return getSoggetto(documento, TipoSoggetto.REDATTORE)
    }

	List<SoggettoNotifica> getSoggettoREDATTORE (OggettoSeduta documento) {
		return getSoggetto(documento.propostaDelibera, TipoSoggetto.REDATTORE)
    }

	List<SoggettoNotifica> getSoggettoFUNZIONARIO (IDocumentoIterabile documento) {
		return getSoggetto(documento, TipoSoggetto.FUNZIONARIO)
    }

    List<SoggettoNotifica> getSoggettoDIRIGENTE (IDocumentoIterabile documento) {
        return getSoggetto(documento, TipoSoggetto.DIRIGENTE)
    }

    List<SoggettoNotifica> getSoggettoINCARICATO (IDocumentoIterabile documento) {
        return getSoggetto(documento, TipoSoggetto.INCARICATO)
    }

    List<SoggettoNotifica> getResposabiliInUoProponenteEPadri (IDocumentoIterabile documento) {
        List<SoggettoNotifica> soggetti = []
        if (documento instanceof IDocumentoCollegato) {
            documento = documento.documentoPrincipale
        }

        if (documento instanceof IAtto) {
            documento = documento.proposta
        }

        So4UnitaPubb uoProponente = documento.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4

        List<So4ComponentePubb> componenti = strutturaOrganizzativaService.getComponentiConRuoloInUnitaPadri(Impostazioni.RUOLO_SO4_RESPONSABILE.valore, uoProponente.progr, uoProponente.ottica.codice, uoProponente.dal)
        for (So4ComponentePubb comp : componenti) {
            As4SoggettoCorrente soggetto = comp.soggetto
            Ad4Utente utente = soggetto.utenteAd4
            if (utente == null ){
                continue
            }

            soggetti << new SoggettoNotifica(utente: utente, email:soggetto?.indirizzoWeb, soggetto:soggetto)
        }

        return soggetti
    }

	List<SoggettoNotifica> getSoggetto (IDocumentoIterabile documento, String tipoSoggetto) {
		def soggettoDocumento = documento.getSoggetto(tipoSoggetto)
		// Nelle delibere posso mandare notifiche ai soggetti della proposta. Quindi se non ho trovato il soggetto, lo vado a cercare nella proposta.
		if (soggettoDocumento == null) {
			if (documento instanceof IAtto)
				soggettoDocumento = documento.proposta.getSoggetto(tipoSoggetto)
			else if (documento instanceof IDocumentoCollegato)
				soggettoDocumento = documento.documentoPrincipale.getSoggetto(tipoSoggetto)
		}
		
		// se non trovo il soggetto sul documento, 
		if (soggettoDocumento == null) {
			return []
		}
	
		Ad4Utente utente = soggettoDocumento.utenteAd4
		if (utente == null) {
			return []
		}

		As4SoggettoCorrente soggetto = As4SoggettoCorrente.findByUtenteAd4(utente)

		return [new SoggettoNotifica(utente: utente, email:soggetto?.indirizzoWeb, soggetto:soggetto)]
	}

	List<SoggettoNotifica> getUtentiInUoProponenteEFiglie (IDocumentoIterabile documento) {
		if (documento instanceof IDocumentoCollegato) {
			documento = documento.documentoPrincipale
        }

		if (documento instanceof IAtto) {
			documento = documento.proposta
        }

		def uoProponente = documento.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4
        def componenti   = strutturaOrganizzativaService.getComponentiInUnitaEFiglie (uoProponente.progr, uoProponente.ottica.codice, uoProponente.al?:new Date())
        return componenti.collect { new SoggettoNotifica(utente: it.soggetto.utenteAd4, unitaSo4: uoProponente, email:it.soggetto.indirizzoWeb, soggetto:it.soggetto, assegnazione: DestinatarioNotificaAttivita.NOTIFICA_UNITA) }
	}

	List<SoggettoNotifica> getEmailOrganiControllo (def organoControlloNotifica) {
		return OrganoControlloComponente.createCriteria().list() {
			projections {
				componente {
					property ("indirizzoWeb")
				}
			}

			organoControllo {
				eq ("tipo.codice", organoControlloNotifica.tipoOrganoControllo.codice)
				eq ("valido", true)
			}

			componente {
				isNotNull ("indirizzoWeb")
			}

			eq ("valido", true)
		}.collect { new SoggettoNotifica(email: it) }
	}

	List<SoggettoNotifica> getConvocatiSeduta (SedutaStampa sedutaStampa) {
        return getConvocatiSeduta(sedutaStampa.seduta)
    }

	List<SoggettoNotifica> getConvocatiSeduta (Seduta seduta) {
        List<SoggettoNotifica> soggetti = []
        for (SedutaPartecipante sedutaPartecipante : seduta.partecipanti) {
            if (sedutaPartecipante.convocato) {
                As4SoggettoCorrente s = sedutaPartecipante.componenteEsterno?:sedutaPartecipante.commissioneComponente.componente
                soggetti << new SoggettoNotifica(email: s.indirizzoWeb, soggetto:s, utente:s.utenteAd4)
            }
        }
        return soggetti
	}

	/*
	 *  Metodi di calcolo dei valori per gli allegati
	 */

	Allegato getTesto (OggettoSeduta oggettoSeduta) {

		// questo if serve perché alcune alcuni oggetti seduta sono legati a proposte di delibera che non hanno
		// il testo perché sono "fuori sacco" che poi non diventano delibere o cose così.
		if (oggettoSeduta.propostaDelibera.testo != null) {
			return new Allegato(oggettoSeduta.propostaDelibera.testo.nome, new ByteArrayInputStream (IOUtils.toByteArray(gestoreFile.getFile(oggettoSeduta.propostaDelibera, oggettoSeduta.propostaDelibera.testo))))
		}

		return null
	}

	Allegato getTesto (def documento) {
		if (documento.testo == null)
			return null

		// questo if serve perché alcuni allegati (come il testo della stampa all'organo di controllo) non risiedono su gdm (sarebbe meglio se fosse gestito dal bean...)
        // TODO: portare anche il testo della stampa agli organi di controllo su GDM.
		if (documento.testo.idFileEsterno > 0) {
			return new Allegato(documento.testo.nome, new ByteArrayInputStream (IOUtils.toByteArray(gestoreFile.getFile(documento, documento.testo))))
		} else {
			return new Allegato(documento.testo.nome, new ByteArrayInputStream (documento.testo.allegato))
		}
	}
	
	Allegato getTestoPdf (def documento) {
		Allegato testo = getTesto (documento)
		if (testo == null) {
			return null
		}
		
		// se il testo è già pdf o p7m, lo ritorno così com'è
		if (testo.nome.toLowerCase().endsWith("p7m") || testo.nome.toLowerCase().endsWith("pdf")) {
			return testo
		}
		
		// converto il testo in pdf:
		testo.nome = testo.nome.replaceAll(/\..+$/, ".pdf")
		InputStream testoPdf = gestioneTesti.convertiStreamInPdf(is, testo.nome, documento)
		testo.testo = new ByteArrayInputStream (IOUtils.toByteArray(testoPdf))
		
		return testo
	}

	Allegato getStampaUnica (def documento) {
		if (!documento.hasProperty ("stampaUnica") || documento.stampaUnica == null) {
			return null
        }

		return new Allegato(documento.stampaUnica.nome, new ByteArrayInputStream(IOUtils.toByteArray(gestoreFile.getFile(documento, documento.stampaUnica))))
	}

	List<Allegato> getTuttiFile (def documento) {
		List<Allegato> allegatiMail = []

		try {
			log.debug("getTuttiFile: aggiungo il frontespizio")
			aggiungiAllegati(allegatiMail, it.finmatica.atti.documenti.Allegato.ALLEGATO_FRONTESPIZIO, documento)

			log.debug("getTuttiFile: aggiungo il testo")
			allegatiMail.add(getTesto(documento))

			log.debug("getTuttiFile: aggiungo l'allegato omissis")
			aggiungiAllegati(allegatiMail, it.finmatica.atti.documenti.Allegato.ALLEGATO_OMISSIS, documento)

			log.debug("getTuttiFile: aggiungo l'allegato riassuntivo delle firme")
			aggiungiAllegati(allegatiMail, it.finmatica.atti.documenti.Allegato.ALLEGATO_RIASSUNTIVO_FIRME, documento)

			log.debug("getTuttiFile: aggiungo la scheda contabile")
			aggiungiAllegati(allegatiMail, it.finmatica.atti.documenti.Allegato.ALLEGATO_SCHEDA_CONTABILE, documento)

			log.debug("getTuttiFile: cerco gli allegati del documento")
			List<it.finmatica.atti.documenti.Allegato> allegati = it.finmatica.atti.documenti.Allegato.createCriteria().list {
				if (documento instanceof Determina) {
					eq("determina", documento)
				} else if (documento instanceof Delibera) {
					eq("delibera", documento)
				} else {
					eq("propostaDelibera", documento)
				}
				eq("valido", true)
				or {
					isNull("codice")
					not {
						'in'("codice", [it.finmatica.atti.documenti.Allegato.ALLEGATO_FRONTESPIZIO, it.finmatica.atti.documenti.Allegato.ALLEGATO_OMISSIS, it.finmatica.atti.documenti.Allegato.ALLEGATO_SCHEDA_CONTABILE, it.finmatica.atti.documenti.Allegato.ALLEGATO_RIASSUNTIVO_FIRME])
					}
				}
				order("sequenza", "asc")
			}

			log.debug("getTuttiFile: aggiungo i vari allegati")
			for (it.finmatica.atti.documenti.Allegato a : allegati) {
				a.fileAllegati?.sort { it.id }
				for (FileAllegato f : a.fileAllegati) {
					InputStream inputStream = gestoreFile.getFile(a, f)
					byte[] bytes = IOUtils.toByteArray(inputStream)
					allegatiMail.add(new Allegato(f.nome, new ByteArrayInputStream(bytes)))
				}
			}

			log.debug("getTuttiFile: cerco i visti del documento")
			List<VistoParere> visti = VistoParere.createCriteria().list {
				if (documento instanceof Determina) {
					eq("determina", documento)
				} else if (documento instanceof Delibera) {
					or {
						eq("propostaDelibera", documento.proposta)
						eq("delibera", documento)
					}
				} else if (documento instanceof PropostaDelibera) {
					eq("propostaDelibera", documento)
				} else {
					eq("id", -1)
				}

				eq("valido", true)
				eq("stato", StatoDocumento.CONCLUSO)

				isNotNull("testo")
				tipologia {
					order("sequenzaStampaUnica", "asc")
				}
			}

			// se il documento è una delibera, allora verifico se ha dei pareri. Se ne ha, metto quelli in stampa unica.
			List<VistoParere> pareriDelibera = visti.findAll { it.delibera != null }
			if (pareriDelibera.size() > 0) {
				visti = pareriDelibera
			}

			log.debug("getTuttiFile: aggiungo solo i visti firmati (digitalmente o no)")
			for (VistoParere v : visti) {
				if (v.testo != null) {
					InputStream inputStream = gestoreFile.getFile(v, v.testo)
					byte[] bytes = IOUtils.toByteArray(inputStream)
					allegatiMail.add(new Allegato(v.testo.nome, new ByteArrayInputStream(bytes)))
				}

				//aggiungo gli allegati del visto in stampa unica
				List<it.finmatica.atti.documenti.Allegato> allegatiVisto = it.finmatica.atti.documenti.Allegato.createCriteria().list {
					eq("vistoParere", v)
					eq("valido", true)
					order("sequenza", "asc")
				}
				log.debug("getTuttiFile: aggiungo i vari allegati del visto " + v.tipologia.codice)
				for (it.finmatica.atti.documenti.Allegato a : allegatiVisto) {
					a.fileAllegati?.sort { it.id }
					for (FileAllegato f : a.fileAllegati) {
						InputStream inputStream = gestoreFile.getFile(a, f)
						byte[] bytes = IOUtils.toByteArray(inputStream)
						allegatiMail.add(new Allegato(f.nome, new ByteArrayInputStream(bytes)))
					}
				}
			}

			if (documento instanceof IAtto) {
				log.debug("getTuttiFile: ordino i certificati")
				// l'ordinamento è:
				def ordineCertificati = [[tipo: Certificato.CERTIFICATO_IMMEDIATA_ESEGUIBILITA, secondaPubblicazione: false]
										 , [tipo: Certificato.CERTIFICATO_PUBBLICAZIONE, secondaPubblicazione: false]
										 , [tipo: Certificato.CERTIFICATO_ESECUTIVITA, secondaPubblicazione: false]
										 , [tipo: Certificato.CERTIFICATO_AVVENUTA_PUBBLICAZIONE, secondaPubblicazione: false]
										 , [tipo: Certificato.CERTIFICATO_IMMEDIATA_ESEGUIBILITA, secondaPubblicazione: true]
										 , [tipo: Certificato.CERTIFICATO_PUBBLICAZIONE, secondaPubblicazione: true]
										 , [tipo: Certificato.CERTIFICATO_ESECUTIVITA, secondaPubblicazione: true]
										 , [tipo: Certificato.CERTIFICATO_AVVENUTA_PUBBLICAZIONE, secondaPubblicazione: true]]

				// seleziono solo quelli validi.
				for (Certificato c : documento.certificati) {
					if (!c.valido) {
						continue
					}

					// uso il tipo di certificato e la seconda pubblicazione come "chiave" di ordineCertificati e ad esso assegno il certificato corrente.
					ordineCertificati.find({
						c.tipo == it.tipo && c.secondaPubblicazione == it.secondaPubblicazione
					})?.certificato = c
				}

				def certificati = ordineCertificati.findAll { it.certificato != null }.certificato

				log.debug("getTuttiFile: aggiungo i certificati")
				for (Certificato c : certificati) {
					if (c.testo != null && c.stato == StatoDocumento.CONCLUSO) {
						InputStream inputStream = gestoreFile.getFile(c, c.testo)
						byte[] bytes = IOUtils.toByteArray(inputStream)
						allegatiMail.add(new Allegato(c.testo.nome, new ByteArrayInputStream(bytes)))
					}
				}
			}

			log.debug("getTuttiFile: aggiungo la stampa unica")
			if (documento.hasProperty("stampaUnica") && documento.stampaUnica != null) {
				allegatiMail.add(getStampaUnica(documento))
			}

		} catch (Throwable e) {
			// in caso di eccezione non eliminare i file (per evenutale debug)
			throw e
		}

		return allegatiMail
	}

	List<Allegato> getZipTuttiFile (def documento) {
		List<Allegato> allegatiMail = []

		try {
			if (documento instanceof Seduta) {
				List<OggettoSeduta> listaOggettiSeduta = PropostaDelibera.createCriteria().list {
					projections {
						property("oggettoSeduta")
					}
					oggettoSeduta {
						eq("seduta.id", documento.id)
						order("sequenzaDiscussione", "asc")
					}
					fetchMode("oggettoSeduta", FetchMode.JOIN)
					fetchMode("delibera", FetchMode.JOIN)
					fetchMode("propostaDelibera", FetchMode.JOIN)
				}

				for (OggettoSeduta oggettoSeduta in listaOggettiSeduta) {
					PropostaDelibera propostaDelibera = oggettoSeduta.propostaDelibera

					if (propostaDelibera) {
						log.debug("getZipTuttiFile: comprimo testo e allegati della proposta delibera")
						File zipAllegati = stampaUnicaService.creaZipAllegati(propostaDelibera)

						Allegato allegatoZip = new Allegato(propostaDelibera.nomeFile + ".zip", new ByteArrayInputStream(FileUtils.readFileToByteArray(zipAllegati)))

						allegatiMail.add(allegatoZip)
					}
				}
			} else {
				log.debug("getZipTuttiFile: comprimo testo e allegati")
				File zipAllegati = stampaUnicaService.creaZipAllegati(documento)

				Allegato allegatoZip = new Allegato("documenti.zip", new ByteArrayInputStream(FileUtils.readFileToByteArray(zipAllegati)))

				allegatiMail.add(allegatoZip)
			}
		} catch (Throwable e) {
			// in caso di eccezione non eliminare i file (per evenutale debug)
			throw e
		}

		return allegatiMail
	}

	Allegato getStampaConvocazione (Seduta seduta) {
		/*CommissioneStampa stampa = CommissioneStampa.createCriteria().get () {
			eq("commissione.id", seduta.commissione.id)
			eq("codice", CommissioneStampa.CONVOCAZIONE)
			modelloTesto {
				eq("tipoModello.codice", Seduta.MODELLO_TESTO_CONVOCAZIONE)
			}
			fetchMode("modelloTesto", FetchMode.JOIN)
		}

		// se non trovo la stampa, esco:
		if (stampa?.modelloTesto == null) {
			return null
		}

		InputStream testo = gestioneTestiService.stampaUnione(stampa.modelloTesto, [id: seduta.id, id_seduta_stampa:-1], TipoFile.PDF.estensione, true)
		return new Allegato(stampa.modelloTesto.nome+".pdf", new ByteArrayInputStream(IOUtils.toByteArray(testo)))
		 */
		return null
	}
	
	
	String getElencoAttiControllati (def documento) {
		String descrizione = ""
		documento.each {
			if (it.delibera != null){
				descrizione += getEstremiDocumento(it.delibera) + "\n"
			}
			else if (it.determina != null){
				descrizione += getEstremiDocumento(it.determina) + "\n"
			}
			if (it.esitoControlloRegolarita != null){
				descrizione += "Esito: " +it.esitoControlloRegolarita.titolo+"\n"
				if (it.note != null){
					descrizione += "Motivazione: " + it.note+"\n"
				}
			}	
			descrizione +="\n"
		}
		return descrizione
	}

	private void aggiungiAllegati(ArrayList<Allegato> files, String codice, IDocumento atto) {
		List<it.finmatica.atti.documenti.Allegato> allegati = it.finmatica.atti.documenti.Allegato.createCriteria().list {
			if (atto instanceof Determina) {
				eq("determina", atto)
			} else if (atto instanceof Delibera) {
				eq ("delibera",  atto)
			} else {
				eq ("propostaDelibera",  atto)
			}
			eq ("valido", 		true)
			eq ("codice", 		codice)
			order ("sequenza", 	"asc")
		}

		for (it.finmatica.atti.documenti.Allegato a : allegati) {
			for (FileAllegato f : a.fileAllegati) {
				InputStream inputStream = gestoreFile.getFile(a, f)
                byte[] bytes = IOUtils.toByteArray(inputStream)
				files.add(new Allegato(f.nome,  new ByteArrayInputStream (bytes)))
            }
		}
	}

	void aggiorna (def documento) {
		def lista = DestinatarioNotificaAttivita.createCriteria().list {
			fetchMode("notifica", 	FetchMode.JOIN)

			or {
				eq ("idRiferimento", "NOTIFICA_"+ documento.getProperties().TIPO_OGGETTO + "_" + documento.id)
				eq ("idRiferimento", 			  documento.getProperties().TIPO_OGGETTO + "_" + documento.id)
			}

			eq ("modalitaInvio", Notifica.MODALITA_JWORKLIST)
			isNotNull("notifica")

			projections {
				distinct "notifica"
			}
		}

		for (Notifica notifica : lista) {
			String idRiferimento = jworklistDispatcher.getIdRiferimento(documento, notifica.tipoNotifica)
			def notificheDaEliminare = jworklistDispatcher.getNotificheDaEliminare(idRiferimento, notifica)
			for (def notificaDaEliminare : notificheDaEliminare) {
				jworklistDispatcher.eliminaNotifica(notificaDaEliminare.id)
				def soggettiNotifica  = new ArrayList<SoggettoNotifica>()
				soggettiNotifica.add(new SoggettoNotifica(
						utente: 		notificaDaEliminare.utente
						, assegnazione: notificaDaEliminare.soggettoNotifica
						, unitaSo4: 	notificaDaEliminare.unitaSo4))
				this.notifica(notifica, documento, soggettiNotifica)
			}
		}

	}
}
