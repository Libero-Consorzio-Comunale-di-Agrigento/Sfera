package it.finmatica.atti.integrazioni

import org.apache.commons.lang.builder.HashCodeBuilder

class PropostaDeliberaEsterna implements Serializable {

    // dati "legacy" dovuti alla tabella pre-esistente in AGSDE "vecchio"
	String applicativoEsterno   
	String idDocumentoEsterno
	Long annoClassificazione
	boolean apriRegistro		
	String classificazione
	String codiceModello		
	Date dataIns				
	Long idPropostaDelibera
	String documentiFascicolo
	String ente 				
	byte[] fileDocumento		
	String firmatari			
	String formatoFile			
	String idFatture			
	boolean impegno				
	String movimento			
	String nomeFile				
	String note					
	String numeroClassificazione
	String oggetto				
	String spesa				
	String statoAcquisizione	
	String tipologia			
	String tipoAtto				
	String tipoRegistro			
	String unitaControllo		
	String unitaEsibente		
	String unitaProtocollante	
	String utenteDirigente		
	String utenteInserimento	
	String utenteProtocollante

    // dati di classificazione
    String classificaCodice
    Date classificaDal
    String classificaDescrizione
    Integer fascicoloAnno
    String fascicoloNumero
    String fascicoloOggetto
	
	static mapping = {	
		table 		'ag_acquisizione_delibere'
		id 			column: 'id_proposta_delibera_esterna'
		
		annoClassificazione 	column: 'anno_cla'
		apriRegistro			type:   'yes_no'
		idPropostaDelibera 		column: 'proposta_delibera_id'
		documentiFascicolo		length: 4000
		fileDocumento			sqlType: 'Blob'
		firmatari				length: 1000
		idDocumentoEsterno		column: 'id_doc_esterno'
		idFatture				length: 1000
		impegno					type: 'yes_no'
		nomeFile				column: 'nomefile'
		numeroClassificazione 	column: 'numero_cla'
		oggetto					length: 1000
        fascicoloOggetto length: 4000
	}
	
	static constraints = {
		idDocumentoEsterno     nullable: true
		annoClassificazione    nullable: true
		apriRegistro		   nullable: true
		classificazione		   nullable: true
		codiceModello		   nullable: true
		dataIns				   nullable: true
		idPropostaDelibera	   nullable: true
		documentiFascicolo	   nullable: true
		ente 				   nullable: true
		fileDocumento		   nullable: true
		firmatari			   nullable: true
		formatoFile			   nullable: true
		idFatture			   nullable: true
		impegno				   nullable: true
		movimento			   nullable: true
		nomeFile		       nullable: true
		note			       nullable: true
		numeroClassificazione  nullable: true
		spesa				   nullable: true
		tipologia			   nullable: true
		tipoAtto			   nullable: true
		tipoRegistro		   nullable: true
		unitaControllo		   nullable: true
		unitaEsibente		   nullable: true
		unitaProtocollante	   nullable: true
		utenteInserimento	   nullable: true
		utenteProtocollante	   nullable: true

        classificaCodice nullable: true
        classificaDal nullable: true
        classificaDescrizione nullable: true
        fascicoloAnno nullable: true
        fascicoloNumero nullable: true
        fascicoloOggetto nullable: true
	}
}
