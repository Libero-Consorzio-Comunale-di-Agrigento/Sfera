package it.finmatica.atti.impostazioni

/**
 * Rappresenta i tipi soggetti che si potranno gestire per una Delibera / Determina
 * Es: Proponente - Funzionario - Secondo Funzionario - Dirigente - Firmatario - UO proponente - UO secondaria
 *
 * Di questa tabella non si prevede interfaccia grafica
 * @author MFrancesconi
 *
 */
class TipoSoggetto {

	public static final transient String REDATTORE 	 	= "REDATTORE"
	public static final transient String UO_PROPONENTE  = "UO_PROPONENTE"
	public static final transient String FUNZIONARIO 	= "FUNZIONARIO"
	public static final transient String DIRIGENTE 	 	= "DIRIGENTE"
	public static final transient String PRESIDENTE	 	= "PRESIDENTE"
	public static final transient String SEGRETARIO  	= "SEGRETARIO"
	public static final transient String INCARICATO  	= "INCARICATO" 		// soggetto generico inserito inizialmente per SIAR

	// Per il visto è il firmatario che deve firmare il visto.
	// Per la proposta, viene usato come firmatario aggiuntivo della proposta. Vedi richiesta di Treviso: #6921, http://svi-redmine/issues/6921
	public static final transient String FIRMATARIO  	= "FIRMATARIO"
	
	// Per la proposta, viene usata come unità a cui inoltrare la proposta. Vedi richiesta di Treviso: #6100, http://svi-redmine/issues/6100
	// Utilizzato anche per Pistoia: http://svi-redmine/issues/17041
	// Per il visto è l'unità che deve predere in carico il visto,
	public static final transient String UO_DESTINATARIA = "UO_DESTINATARIA"

	// Aggiunto per Treviso: #6921, http://svi-redmine/issues/6921
	// Utilizzato anche per Pistoia: http://svi-redmine/issues/17041
	// Serve per poter gestire il passaggio a più dirigenti firmatari. Va di pari passo con il soggetto FIRMATARIO.
	public static final transient String UO_FIRMATARIO 	= "UO_FIRMATARIO"
	
	// Aggiunti per la gestione delle ASL:
	public static final transient String DIRETTORE_GENERALE 		= "DIRETTORE_GENERALE"
	public static final transient String DIRETTORE_AMMINISTRATIVO 	= "DIRETTORE_AMMINISTRATIVO"
    public static final transient String DIRETTORE_SANITARIO 		= "DIRETTORE_SANITARIO"
    public static final transient String DIRETTORE_SOCIO_SANITARIO	= "DIRETTORE_SOCIO_SANITARIO"

	// Aggiunto per Treviso: #6920, http://svi-redmine/issues/6920
	// indica l'unità di controllo della ragioneria a cui la proposta deve passare prima della vidima del dirigente.
	public static final transient String UO_CONTROLLO 			= "UO_CONTROLLO"

	public static final transient String CATEGORIA_UNITA		= "UNITA"
	public static final transient String CATEGORIA_COMPONENTE	= "COMPONENTE"

	String categoria	// indica se è unità o componente

	String codice
	String titolo
	String descrizione

	static mapping = {
		table 		'tipi_soggetti' // FIXME: questa tabella si chiama al plurale perché altrimenti va in conflitto di nomi su AS4. BISOGNA MIGLIORARE IL PLUGIN!!
		id 			column: 'tipo_soggetto', name: 'codice', generator: 'assigned'
		descrizione length: 4000
		version 	false
	}

	static constraints = {
		descrizione 	nullable: true
		categoria		inList: [ TipoSoggetto.CATEGORIA_UNITA, TipoSoggetto.CATEGORIA_COMPONENTE ]
	}
}