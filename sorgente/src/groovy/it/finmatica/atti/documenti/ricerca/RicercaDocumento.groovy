package it.finmatica.atti.documenti.ricerca

import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.dizionari.Categoria
import it.finmatica.atti.dizionari.TipoBudget
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.atti.documenti.StatoMarcatura
import it.finmatica.gestioneiter.motore.WkfStep
import it.finmatica.so4.struttura.So4Amministrazione
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

abstract class RicercaDocumento {

	// dati identificativi del documento
	long 	idDocumento
	String 	tipoDocumento
	Long 	idDocumentoPrincipale
	String 	tipoDocumentoPrincipale
	So4Amministrazione ente

	// dati del documento
	String 			oggetto
	boolean 		riservato
	String 			stato
	Categoria 		categoria
	FileAllegato	testo
	FileAllegato	allegato
	
	// dati dei soggetti
	So4UnitaPubb 	uoProponente
	String 			uoProponenteDescrizione
	Ad4Utente 		utenteSoggetto
	String 			tipoSoggetto

	// dati per le competenze
	Long 			idCompetenza
	Ad4Utente 		compUtente
	So4UnitaPubb 	compUnita
	Ad4Ruolo 		compRuolo
	boolean 		compLettura
	boolean 		compModifica
	boolean 		compCancellazione

	// dati per lo step
	WkfStep 		step
	Ad4Utente 		stepUtente
	So4UnitaPubb	stepUnita
	Ad4Ruolo 		stepRuolo
	String 			stepNome
	String 			stepDescrizione

	// dati dell'atto
	Long 	numeroAtto
	Long 	annoAtto
	String 	registroAtto
	Date 	dataAtto
	Date 	dataAdozione
	Date 	dataEsecutivita
	Date	dataScadenza

    // dati di seconda numerazione della determina
    Long 	numeroAtto2
    Long 	annoAtto2
    String 	registroAtto2
	Date    dataNumeroAtto2

	// dati della proposta
	Long 	numeroProposta
	Long 	annoProposta
	String 	registroProposta
	Date 	dataProposta

	// dati del protocollo
	Long 	numeroProtocollo
	Long 	annoProtocollo
	String 	registroProtocollo

	// dati della tipologia
	Long 	idTipologia
	String 	titoloTipologia
	String 	descrizioneTipologia
	boolean conImpegnoSpesa

	// dati di pubblicazione
	Long numeroAlbo
	Long annoAlbo
	Date dataPubblicazione
	Date dataFinePubblicazione

	// dati di conservazione
	String logConservazione
	String statoConservazione
	Date dataConservazione

	// gestione corte dei conti
	boolean daInviareCorteConti
	Date 	dataInvioCorteConti

	StatoMarcatura statoMarcatura
	StatoFirma statoFirma
	boolean attoConcluso
    String cup
    String codiceProgetto
    String contoEconomico
    BigDecimal importo
    TipoBudget tipoBudget

}
