package it.finmatica.atti.contabilita

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.documenti.DocumentoFactory
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.so4.struttura.So4Amministrazione
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders

class MovimentoContabile {

    public static final String STATO_INVIATO        = "INVIATO"
    public static final String STATO_DA_INVIARE     = "DA_INVIARE"
    public static final String STATO_DA_NON_INVIARE = "DA_NON_INVIARE"
    public static final String STATO_ERRORE         = "ERRORE"
    public static final String STATO_NON_INVIATO    = "NON_INVIATO"

	public static final String TIPO_ENTRATA = "ENTRATA"
	public static final String TIPO_USCITA 	= "USCITA"

	// legame con delibera/determina/visto/etc.
	long   idDocumento
	String tipoDocumento
    String stato            = STATO_DA_INVIARE
    String statoDescrizione

	// tutti questi campi sono necessari per l'integrazione con la contabilità di modena.
	// alcuni di questi sono usati invece per la gestione "minimal" della contabilità all'interno di Sfera.

									 // CAMPI INTEGRAZIONE MODENA			// CAMPI INTEGRAZIONE GENOVA (STANDARD SFERA)

    Integer annoCompetenza 		 	 // ANNO_COMPETENZA                     // ANNO_IMPEGNO
    Integer annoCrono			 	 // ANNO_CRONO                          // ANNO_PRENOTAZIONE
    Integer annoEsercizio			 // ANNO_ESERCIZIO                      // ANNO_ACCERTAMENTO
    String articolo				 	 // ARTICOLO                            //
    String capitolo				 	 // CAPITOLO                            // CAPITOLO
    String codiceFinanziamento1	 	 // CODICE_FINANZIAMENTO_1              //
    String codiceFinanziamento2	 	 // CODICE_FINANZIAMENTO_2              //
    String codiceFinanziamento3	 	 // CODICE_FINANZIAMENTO_3              //
    String codiceSoggetto		 	 // CODICE_SOGGETTO                     //
    Date dataDettaglio			 	 // DATA_DETTAGLIO                      //
    String descrizione			 	 // DESCRIZIONE                         // NOTE
    String descrizioneCapitolo	 	 // DESCRIZIONE_CAPITOLO                //
    String descrizioneFinanziamento1 // DESCRIZIONE_FINANZIAMENTO_1         // CIG
    String descrizioneFinanziamento2 // DESCRIZIONE_FINANZIAMENTO_2         // CUP
    String descrizioneFinanziamento3 // DESCRIZIONE_FINANZIAMENTO_3         //
    String descrizioneInvestimento 	 // DESCRIZIONE_INVESTIMENTO            //
    String descrizioneSoggetto	 	 // DESCRIZIONE_SOGGETTO                //
    BigDecimal disposizioneMandati	 // DISPOSIZIONE_MANDATI                //
    BigDecimal importo				 // IMPORTO                             // IMPORTO
    BigDecimal importoCassa			 // IMPORTO_CASSA                       //
    BigDecimal importoFinanziamento1 // IMPORTO_FINANZIAMENTO_1             //
    BigDecimal importoFinanziamento2 // IMPORTO_FINANZIAMENTO_2             //
    BigDecimal importoFinanziamento3 // IMPORTO_FINANZIAMENTO_3             //
    String investimento			 	 // INVESTIMENTO                        //
    String missione				 	 // MISSIONE                            //
    Integer numero				 	 // NUMERO                              // NUMERO_IMPEGNO
    Integer numeroCrono			 	 // NUMERO_CRONO                        // NUMERO_PRENOTAZIONE
    Integer numeroDet			 	 // NUMERO_DET                          // NUMERO_ACCERTAMENTO
    Integer numeroVariazione	 	 // NUMERO_VARIAZIONE                   //
    String opera				 	 // OPERA                               // AZIONE
    String pdcLiv1			 		 // PDC_LIV_1                           //
    String pdcLiv2			 		 // PDC_LIV_2                           //
    String pdcLiv3			 		 // PDC_LIV_3                           //
    String pdcLiv4			 		 // PDC_LIV_4                           //
    String pdcLiv5			 		 // PDC_LIV_5                           //
    boolean prenotazione 		 	 // PRENOTAZIONE                        //
    String progetto				 	 // PROGETTO                            //
    String programma			 	 // PROGRAMMA                           //
    Integer numeroSub			 	 // SUB_NUMERO                          //
    String tipo					 	 // TIPO							    // SPESA / ENTRATA
    String tipoDettaglio		 	 // TIPO_DETTAGLIO                      // CODICE
    String tipoVariazioneDiBilancio	 // TIPO_VARIAZIONE_DI_BILANCIO         //

	// campi aggiuntivi per integrazione Modena con scrittura dei movimenti contabili
    String tipoCodiceStatistico1
    String tipoCodiceStatistico2
    String tipoCodiceStatistico3
    String tipoCodiceStatistico4
    String tipoCodiceStatistico5
    String codiceStatistico1
    String codiceStatistico2
    String codiceStatistico3
    String codiceStatistico4
    String codiceStatistico5
    String codiceIdEuropeo
    String codiceVoceMinisteriale
    String codiceSiope
    String tipoUscita
    String cig
    String cup
    Date dataScadenza

	// campi aggiuntivi per integrazione Ascot
	Integer esercizioEsterno
	Integer progressivoEsterno
	Integer annoEsercizioEsterno
	Integer numeroMovimento
	Date 	dataMovimento
	String 	codicePdcf
	String 	descrizionePdcf
	String 	esecutivita

	// i soliti dati...
	So4Amministrazione ente
	Date 		dateCreated
	Ad4Utente 	utenteIns
	Date 		lastUpdated
	Ad4Utente 	utenteUpd

	static mapping = {
		id				column: 'id_movimento_contabile'
		table			'movimenti_contabili'

		annoCompetenza 		 	 	column: 'ANNO_COMPETENZA'
		annoCrono			 	 	column: 'ANNO_CRONO'
		annoEsercizio			 	column: 'ANNO_ESERCIZIO'
		articolo				 	column: 'ARTICOLO'
		capitolo				 	column: 'CAPITOLO'
		codiceFinanziamento1	 	column: 'CODICE_FINANZIAMENTO_1'
		codiceFinanziamento2	 	column: 'CODICE_FINANZIAMENTO_2'
		codiceFinanziamento3	 	column: 'CODICE_FINANZIAMENTO_3'
		codiceSoggetto		 	 	column: 'CODICE_SOGGETTO'
		dataDettaglio			 	column: 'DATA_DETTAGLIO'
		descrizione			 	 	column: 'DESCRIZIONE'
		descrizioneCapitolo	 	 	column: 'DESCRIZIONE_CAPITOLO'
		descrizioneFinanziamento1 	column: 'DESCRIZIONE_FINANZIAMENTO_1'
		descrizioneFinanziamento2 	column: 'DESCRIZIONE_FINANZIAMENTO_2'
		descrizioneFinanziamento3 	column: 'DESCRIZIONE_FINANZIAMENTO_3'
		descrizioneInvestimento 	column: 'DESCRIZIONE_INVESTIMENTO'
		descrizioneSoggetto	 	 	column: 'DESCRIZIONE_SOGGETTO'
		disposizioneMandati		 	column: 'DISPOSIZIONE_MANDATI'     , scale: 2
		importo				 	 	column: 'IMPORTO'                  , scale: 2
		importoCassa			 	column: 'IMPORTO_CASSA'            , scale: 2
		importoFinanziamento1		column: 'IMPORTO_FINANZIAMENTO_1'  , scale: 2
		importoFinanziamento2		column: 'IMPORTO_FINANZIAMENTO_2'  , scale: 2
		importoFinanziamento3		column: 'IMPORTO_FINANZIAMENTO_3'  , scale: 2
		investimento			 	column: 'INVESTIMENTO'
		missione				 	column: 'MISSIONE'
		numero				 	 	column: 'NUMERO'
		numeroCrono			 	 	column: 'NUMERO_CRONO'
		numeroDet			 	 	column: 'NUMERO_DET'
		numeroVariazione	 	 	column: 'NUMERO_VARIAZIONE'
		opera				 	 	column: 'OPERA'
		pdcLiv1			 		 	column: 'PDC_LIV_1'
		pdcLiv2			 		 	column: 'PDC_LIV_2'
		pdcLiv3			 		 	column: 'PDC_LIV_3'
		pdcLiv4			 		 	column: 'PDC_LIV_4'
		pdcLiv5			 		 	column: 'PDC_LIV_5'
		prenotazione 		 	 	column: 'PRENOTAZIONE', type: 'yes_no'
		progetto				 	column: 'PROGETTO'
		programma			 	 	column: 'PROGRAMMA'
		numeroSub			 	 	column: 'SUB_NUMERO'
		tipo					 	column: 'TIPO'
		tipoDettaglio		 	 	column: 'TIPO_DETTAGLIO'
		tipoVariazioneDiBilancio	column: 'TIPO_VARIAZIONE_DI_BILANCIO'

        tipoCodiceStatistico1       column: 'TIPO_CODICE_STATISTICO_1'
        tipoCodiceStatistico2       column: 'TIPO_CODICE_STATISTICO_2'
        tipoCodiceStatistico3       column: 'TIPO_CODICE_STATISTICO_3'
        tipoCodiceStatistico4       column: 'TIPO_CODICE_STATISTICO_4'
        tipoCodiceStatistico5       column: 'TIPO_CODICE_STATISTICO_5'
        codiceStatistico1           column: 'CODICE_STATISTICO_1'
        codiceStatistico2           column: 'CODICE_STATISTICO_2'
        codiceStatistico3           column: 'CODICE_STATISTICO_3'
        codiceStatistico4           column: 'CODICE_STATISTICO_4'
        codiceStatistico5           column: 'CODICE_STATISTICO_5'
        codiceIdEuropeo             column: 'CODICE_ID_EUROPEO'
        codiceVoceMinisteriale      column: 'CODICE_VOCE_MINISTERIALE'
        codiceSiope                 column: 'CODICE_SIOPE'
        tipoUscita                  column: 'TIPO_USCITA'
        cig                         column: 'CIG'
        cup                         column: 'CUP'
        dataScadenza                column: 'DATA_SCADENZA'

		esercizioEsterno			column: 'ESERCIZIO_ESTERNO'
		progressivoEsterno			column: 'PROGRESSIVO_ESTERNO'
		annoEsercizioEsterno		column: 'ANNO_ESERCIZIO_ESTERNO'
		numeroMovimento				column: 'NUMERO_MOVIMENTO'
		dataMovimento				column: 'DATA_MOVIMENTO'
		codicePdcf					column: 'CODICE_PDCF'
		descrizionePdcf				column: 'DESCRIZIONE_PDCF'
		esecutivita					column: 'ESECUTIVITA'

		ente 			column: 'ente'
		dateCreated 	column: 'data_ins'
		utenteIns 		column: 'utente_ins'
		lastUpdated 	column: 'data_upd'
		utenteUpd 		column: 'utente_upd'
	}

	static constraints = {
        statoDescrizione            nullable: true
		annoCompetenza 		 	    nullable: true
        annoCrono			 	    nullable: true
        annoEsercizio			    nullable: true
        articolo				    nullable: true
        capitolo				    nullable: true
        codiceFinanziamento1	    nullable: true
        codiceFinanziamento2	    nullable: true
        codiceFinanziamento3	    nullable: true
        codiceSoggetto		 	    nullable: true
        dataDettaglio			    nullable: true
        descrizione			 	    nullable: true
        descrizioneCapitolo	 	    nullable: true
        descrizioneFinanziamento1   nullable: true
        descrizioneFinanziamento2   nullable: true
        descrizioneFinanziamento3   nullable: true
        descrizioneInvestimento 	nullable: true
        descrizioneSoggetto	 	    nullable: true
        disposizioneMandati		    nullable: true
        importo				 	    nullable: true
        importoCassa			 	nullable: true
        importoFinanziamento1		nullable: true
        importoFinanziamento2		nullable: true
        importoFinanziamento3		nullable: true
        investimento			    nullable: true
        missione				    nullable: true
        numero				 	    nullable: true
        numeroCrono			 	    nullable: true
        numeroDet			 	    nullable: true
        numeroVariazione	 	    nullable: true
        opera				 	    nullable: true
        pdcLiv1			 		    nullable: true
        pdcLiv2			 		    nullable: true
        pdcLiv3			 		    nullable: true
        pdcLiv4			 		    nullable: true
        pdcLiv5			 		    nullable: true
        progetto				    nullable: true
        programma			 	    nullable: true
        numeroSub			 	    nullable: true
        tipo					 	nullable: true
        tipoDettaglio		 	 	nullable: true
        tipoVariazioneDiBilancio	nullable: true
        tipoCodiceStatistico1       nullable: true
        tipoCodiceStatistico2       nullable: true
        tipoCodiceStatistico3       nullable: true
        tipoCodiceStatistico4       nullable: true
        tipoCodiceStatistico5       nullable: true
        codiceStatistico1           nullable: true
        codiceStatistico2           nullable: true
        codiceStatistico3           nullable: true
        codiceStatistico4           nullable: true
        codiceStatistico5           nullable: true
        codiceIdEuropeo             nullable: true
        codiceVoceMinisteriale      nullable: true
        codiceSiope                 nullable: true
        tipoUscita                  nullable: true
        cig                         nullable: true
        cup                         nullable: true
        dataScadenza                nullable: true
		esercizioEsterno			nullable: true
		progressivoEsterno			nullable: true
		annoEsercizioEsterno		nullable: true
		numeroMovimento				nullable: true
		dataMovimento				nullable: true
		codicePdcf					nullable: true
		descrizionePdcf				nullable: true
		esecutivita					nullable: true
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

	static transients = ['documentoPrincipale', 'CUP', 'CIG', 'note', 'codice', 'numeroPrenotazione', 'annoPrenotazione', 'numeroImpegno', 'annoImpegno', 'entrata', 'numeroAccertamento', 'annoAccertamento', 'azione']

	transient IDocumento getDocumentoPrincipale () {
		return DocumentoFactory.getDocumento (idDocumento, tipoDocumento);
	}

	transient void setDocumentoPrincipale (IDocumento documentoPrincipale) {
		idDocumento 	= documentoPrincipale.id
		tipoDocumento 	= documentoPrincipale.TIPO_OGGETTO
	}

	// Questi sono giusto degli "alias" per i campi dell'integrazione per "Genova" che diventerà poi la nostra soluzione "basic" di contabilità integrata in sfera.

	public String getAzione () {
		return opera;
	}

	public void setAzione (String value) {
		opera = value;
	}

	public boolean isEntrata () {
		return TIPO_ENTRATA.equals(tipo);
	}

	public void setEntrata (boolean value) {
		tipo = (value ? TIPO_ENTRATA : TIPO_USCITA);
	}

	public String getCodice () {
		return this.tipoDettaglio;
	}

	public void setCodice (String value) {
		this.tipoDettaglio = value;
	}

	public Integer getNumeroPrenotazione () {
		return numeroCrono;
	}

	public void setNumeroPrenotazione (Integer value) {
		numeroCrono = value;
	}

	public Integer getAnnoPrenotazione () {
		return annoCrono;
    }

    public void setAnnoPrenotazione (Integer value) {
    	annoCrono = value;
    }

	public Integer getNumeroAccertamento () {
		return numeroDet;
	}

	public void setNumeroAccertamento (Integer value) {
		numeroDet = value;
	}

	public Integer getAnnoAccertamento () {
		return annoEsercizio;
	}

	public void setAnnoAccertamento (Integer value) {
		annoEsercizio = value;
	}

	public Integer getNumeroImpegno () {
		return numero;
	}

	public void setNumeroImpegno (Integer value) {
		numero = value;
	}

	public Integer getAnnoImpegno () {
		return annoCompetenza;
	}

	public void setAnnoImpegno (Integer value) {
		annoCompetenza = value;
	}

	public String getCIG () {
		return descrizioneFinanziamento1;
	}

	public void setCIG (String value) {
		descrizioneFinanziamento1 = value;
	}

	public String getCUP () {
		return descrizioneFinanziamento2;
	}

	public void setCUP (String value) {
		descrizioneFinanziamento2 = value;
	}

	public String getNote () {
		return descrizione;
	}

	public void setNote (String value) {
		descrizione = value;
	}
	
	public MovimentoContabile clone () {
		MovimentoContabile c = new MovimentoContabile()
		
		c.idDocumento                  = this.idDocumento
		c.tipoDocumento                = this.tipoDocumento
		c.annoCompetenza 		 	   = this.annoCompetenza 		 	
		c.annoCrono			 	       = this.annoCrono			 	
		c.annoEsercizio			       = this.annoEsercizio			
		c.articolo				       = this.articolo				
		c.capitolo				       = this.capitolo				
		c.codiceFinanziamento1	       = this.codiceFinanziamento1	
		c.codiceFinanziamento2	       = this.codiceFinanziamento2	
		c.codiceFinanziamento3	       = this.codiceFinanziamento3	
		c.codiceSoggetto		 	   = this.codiceSoggetto		 	
		c.dataDettaglio			 	   = this.dataDettaglio			 	
		c.descrizione			 	   = this.descrizione			 	
		c.descrizioneCapitolo	 	   = this.descrizioneCapitolo	 	
		c.descrizioneFinanziamento1    = this.descrizioneFinanziamento1 
		c.descrizioneFinanziamento2    = this.descrizioneFinanziamento2 
		c.descrizioneFinanziamento3    = this.descrizioneFinanziamento3 
		c.descrizioneInvestimento 	   = this.descrizioneInvestimento 	
		c.descrizioneSoggetto	 	   = this.descrizioneSoggetto	 	
		c.disposizioneMandati	       = this.disposizioneMandati	
		c.importo				       = this.importo				
		c.importoCassa			       = this.importoCassa			
		c.importoFinanziamento1        = this.importoFinanziamento1 
		c.importoFinanziamento2        = this.importoFinanziamento2 
		c.importoFinanziamento3        = this.importoFinanziamento3 
		c.investimento			       = this.investimento			
		c.missione				       = this.missione				
		c.numero				 	   = this.numero				 	
		c.numeroCrono			 	   = this.numeroCrono			 	
		c.numeroDet			 	       = this.numeroDet			 	 
		c.numeroVariazione	 	       = this.numeroVariazione	 	 
		c.opera				 	       = this.opera				 	 
		c.pdcLiv1			 		   = this.pdcLiv1			 		
		c.pdcLiv2			 		   = this.pdcLiv2			 		
		c.pdcLiv3			 		   = this.pdcLiv3			 		
		c.pdcLiv4			 		   = this.pdcLiv4			 		
		c.pdcLiv5			 		   = this.pdcLiv5			 		
		c.prenotazione 		 	       = this.prenotazione 		 	 
		c.progetto				       = this.progetto				 
		c.programma			 	       = this.programma			 	 
		c.numeroSub			 	       = this.numeroSub			 	 
		c.tipo					       = this.tipo					
		c.tipoDettaglio		 	       = this.tipoDettaglio		 	 
		c.tipoVariazioneDiBilancio	   = this.tipoVariazioneDiBilancio
		c.dataScadenza             	   = this.dataScadenza
		c.esercizioEsterno			   = this.esercizioEsterno
		c.progressivoEsterno		   = this.progressivoEsterno
		c.annoEsercizioEsterno		   = this.annoEsercizioEsterno
		c.numeroMovimento			   = this.numeroMovimento
		c.dataMovimento				   = this.dataMovimento
		c.codicePdcf				   = this.codicePdcf
		c.descrizionePdcf			   = this.descrizionePdcf
		c.esecutivita				   = this.esecutivita
		return c;
	}
}
