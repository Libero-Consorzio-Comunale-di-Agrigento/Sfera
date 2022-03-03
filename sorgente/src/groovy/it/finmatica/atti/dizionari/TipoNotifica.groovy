package it.finmatica.atti.dizionari

import grails.compiler.GrailsCompileStatic
import it.finmatica.atti.exceptions.AttiRuntimeException

/**
 * Rappresenta le tipologie di notifiche gestite da applicativo
 *
 * Per questa domainClass non è prevista una interfaccia di gestione
 */
@GrailsCompileStatic
class TipoNotifica {

	// usata dal cambio step per indicare che c'è un atto assegnato all'utente
	// questa è una notifica particolare perché non è modificabile da interfaccia.
	public static final String ASSEGNAZIONE 			= "ASSEGNAZIONE"

	// notifiche "generiche"
	public static final String GENERICA_1				= "GENERICA_1"	// era: NOTIFICA_ATTO
	public static final String GENERICA_2				= "GENERICA_2" // era: RELATORE_PROPOSTA
	public static final String GENERICA_3				= "GENERICA_3" // era: UTENTI_CON_RUOLO
	public static final String GENERICA_4				= "GENERICA_4" // notifica generica
	public static final String GENERICA_5				= "GENERICA_5" // notifica generica

	// notifiche di cambio stato del documento:
	public static final String ESECUTIVITA				= "ESECUTIVITA_ATTO"
	public static final String NON_ESECUTIVITA			= "NON_ESECUTIVITA_ATTO"
	public static final String ADOZIONE					= "ADOZIONE_ATTO"
	public static final String DA_FIRMARE				= "NOTIFICA_DA_FIRMARE"
	public static final String ATTO_ANNULLATO			= "ATTO_ANNULLATO"
	public static final String PROPOSTA_ANNULLATA		= "PROPOSTA_ANNULLATA"

	// notifiche per la gestione degli organi di controllo:
	public static final String ORGANI_CONTROLLO 		= "NOTIFICA_ORGANI_CONTROLLO"
	
	// notifiche per la gestione del controllo di regolarità:
	public static final String CONTROLLO_REGOLARITA 	= "NOTIFICA_CONTROLLO_REGOLARITA"

	// notifiche per le commissioni
	public static final String VERBALIZZAZIONE_PROPOSTA = "VERBALIZZAZIONE_PROPOSTA"
	public static final String CONVOCAZIONE_SEDUTA 		= "CONVOCAZIONE_SEDUTA"
	public static final String VERBALE_SEDUTA 			= "VERBALE_SEDUTA"

	// notifiche dell'ordine del giorno:
	public static final String NOTIFICHE_ODG 			= "NOTIFICHE_ODG"
	public static final String DELIBERA_SEGRETARIO 		= "DELIBERA_SEGRETARIO"

	public static final List<TipoNotifica> lista = [

		// Notifiche per i documenti:
		new TipoNotifica (codice: 		TipoNotifica.ADOZIONE
						, titolo: 		"Adozione Atto"
            			, oggetti: 		["DELIBERA"]
            			, descrizione: 	"Notifica che viene inviata al momento dell'adozione della Delibera. Quando cioè viene creata in seduta."),

		new TipoNotifica (codice: 		TipoNotifica.ESECUTIVITA
            			, titolo: 		"Esecutività Atto"
            			, oggetti: 		["DELIBERA", "DETERMINA"]
            			, descrizione: 	"Notifica che viene inviata al momento dell'esecutività dell'atto."),

		new TipoNotifica (codice: 		TipoNotifica.NON_ESECUTIVITA
            			, titolo: 		"Non esecutività Atto"
            			, oggetti: 		["DETERMINA"]
            			, descrizione: 	"Notifica che viene inviata quando una Determina diventa non esecutiva."),

		new TipoNotifica (codice: 		TipoNotifica.DA_FIRMARE
            			, titolo: 		"Atto da Firmare"
            			, oggetti: 		["DETERMINA", "DELIBERA", "PROPOSTA_DELIBERA", "CERTIFICATO", "VISTO_PARERE"]
						, azioni:		"notificheAction.notificaFirmatario"
            			, descrizione: 	"Notifica che viene inviata quando un atto è disponibile per la firma."),

		new TipoNotifica (codice: 		TipoNotifica.ATTO_ANNULLATO
            			, titolo: 		"Atto Annullato"
            			, oggetti: 		["DETERMINA", "DELIBERA"]
            			, descrizione: 	"Notifica che viene inviata quando un atto viene annullato."),

		new TipoNotifica (codice: 		TipoNotifica.PROPOSTA_ANNULLATA
            			, titolo: 		"Proposta Annullata"
            			, oggetti: 		["DETERMINA", "PROPOSTA_DELIBERA"]
            			, descrizione: 	"Notifica che viene inviata quando una proposta viene annullata."),

		// Notifiche per l'ODG
		new TipoNotifica (codice: 		TipoNotifica.CONVOCAZIONE_SEDUTA
            			, titolo: 		"Convocazione Seduta"
            			, oggetti: 		["SEDUTA", "SEDUTA_STAMPA"]
            			, descrizione: 	"Notifica disponibile per l'invio della Convocazione di un Ordine del Giorno per una Seduta."),

		new TipoNotifica (codice: 		TipoNotifica.VERBALE_SEDUTA
            			, titolo: 		"Verbale Seduta"
            			, oggetti: 		["SEDUTA", "SEDUTA_STAMPA"]
            			, descrizione: 	"Notifica disponibile per l'invio del Verbale della Seduta."),

		new TipoNotifica (codice: 		TipoNotifica.DELIBERA_SEGRETARIO
            			, titolo: 		"Invio delibera al segretario"
            			, oggetti: 		["PROPOSTA_DELIBERA"]
            			, descrizione: 	"Notifica disponibile dal singolo oggetto seduta per l'invio al Segretario."),

		new TipoNotifica (codice: 		TipoNotifica.NOTIFICHE_ODG
            			, titolo: 		"Notifica ordine del giorno"
            			, oggetti: 		["SEDUTA"]
            			, descrizione: 	"Ulteriore notifica dei punti dell'Ordine del Giorno disponibile per l'invio dalla Seduta."),

		new TipoNotifica (codice: 		TipoNotifica.VERBALIZZAZIONE_PROPOSTA
            			, titolo: 		"Notifica verbalizzazione della proposta"
            			, oggetti: 		["PROPOSTA_DELIBERA"]
            			, descrizione: 	"Notifica che viene inviata alla conferma dell'esito dopo che la proposta è stata verbalizzata."),

		// Notifiche per gli Organi di Controllo:
		new TipoNotifica (codice: 		TipoNotifica.ORGANI_CONTROLLO
            			, titolo: 		"Notifica agli organi di controllo"
            			, oggetti: 		["ORGANI_DI_CONTROLLO"]
            			, descrizione: 	"Notifica disponibile per l'invio agli organi di controllo dall'apposita sezione."),
					
		// Notifiche per il Controllo di Regolarità:
		new TipoNotifica (codice: 		TipoNotifica.CONTROLLO_REGOLARITA
						, titolo: 		"Notifica per il controllo di regolarità"
						, oggetti: 		["CONTROLLO_REGOLARITA"]
						, descrizione: 	"Notifica disponibile per il controllo di regolarità dall'apposita sezione."),

		// Notifiche Generiche:
		new TipoNotifica (codice: 		TipoNotifica.GENERICA_1
            			, titolo: 		"Notifica Generica 1"
            			, oggetti: 		["DETERMINA", "DELIBERA", "PROPOSTA_DELIBERA"]
						, azioni:		"notificheAction.notificaGenerica1"
            			, descrizione: 	"Notifica disponibile come personalizzazione da inserire a piacimento nel flusso."),

		new TipoNotifica (codice: 		TipoNotifica.GENERICA_2
            			, titolo: 		"Notifica Generica 2"
            			, oggetti: 		["DETERMINA", "DELIBERA", "PROPOSTA_DELIBERA", "VISTO_PARERE", "CERTIFICATO"]
						, azioni:		"notificheAction.notificaGenerica2"
            			, descrizione: 	"Notifica disponibile come personalizzazione da inserire a piacimento nel flusso."),

		new TipoNotifica (codice: 		TipoNotifica.GENERICA_3
            			, titolo:		"Notifica Generica 3"
            			, oggetti: 		["DETERMINA", "DELIBERA", "PROPOSTA_DELIBERA"]
						, azioni:		"notificheAction.notificaGenerica3"
            			, descrizione: 	"Notifica disponibile come personalizzazione da inserire a piacimento nel flusso."),
		
		new TipoNotifica (codice: 		TipoNotifica.GENERICA_4
            			, titolo:		"Notifica Generica 4"
            			, oggetti: 		["DETERMINA", "DELIBERA", "PROPOSTA_DELIBERA", "VISTO_PARERE", "CERTIFICATO"]
						, azioni:		"notificheAction.notificaGenerica4"
            			, descrizione: 	"Notifica disponibile come personalizzazione da inserire a piacimento nel flusso."),
					
		new TipoNotifica (codice: 		TipoNotifica.GENERICA_5
            			, titolo:		"Notifica Generica 5"
            			, oggetti: 		["DETERMINA", "DELIBERA", "PROPOSTA_DELIBERA", "VISTO_PARERE", "CERTIFICATO"]
						, azioni:		"notificheAction.notificaGenerica5"
            			, descrizione: 	"Notifica disponibile come personalizzazione da inserire a piacimento nel flusso.")
	]

	String codice
	String titolo
	String descrizione
	List<String> oggetti
	String azioni

	static String getTitolo (String tipoNotifica) {
		for (TipoNotifica tipo : lista) {
            if (tipo.codice == tipoNotifica) {
                return tipo.titolo
            }
        }

        throw new AttiRuntimeException("Nessuna notifica trovata con codice ${tipoNotifica}")
	}
}
