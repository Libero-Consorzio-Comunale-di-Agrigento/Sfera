package it.finmatica.atti.documenti.viste

import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.documenti.Certificato
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.gestioneiter.motore.WkfStep
import it.finmatica.so4.struttura.So4Amministrazione
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class DocumentoStep {

	String stato
	String statoFirma
	String statoConservazione
	String statoOdg
	String tipoOggetto
	String tipoRegistro

	Long idDocumento
	Long idPadre

	Determina 		 determina
	Delibera 		 delibera
	PropostaDelibera propostaDelibera
	VistoParere 	 vistoParere
	Certificato 	 certificato

	Long   idTipologia
	String titoloTipologia
	String descrizioneTipologia

	Integer annoProposta
	Integer numeroProposta
	Integer anno
	Integer numero

	String 	oggetto
	String 	unitaProponente

	WkfStep step

	Ad4Utente 		stepUtente
	So4UnitaPubb   	stepUnita
	Ad4Ruolo 		stepRuolo

	String stepNome
	String stepDescrizione
	String stepTitolo

	So4Amministrazione ente
	boolean riservato
	
	Date dataAdozione
	String statoVistiPareri
	Date dataScadenza
	Integer priorita
	Date dataOrdinamento

	static mapping = {
		table 	'documenti_step'
		version false
		id 		generator: 'assigned', name: 'idDocumento', column: 'id_documento'

		determina         column: 'id_determina'
		delibera          column: 'id_delibera'
		propostaDelibera  column: 'id_proposta_delibera'
		vistoParere       column: 'id_visto_parere'
		certificato       column: 'id_certificato'

		step	column: 'id_step'
		ente	column: 'ente'

		riservato type: 'yes_no'

		stepUtente column: 'step_utente'
		stepRuolo  column: 'step_ruolo'
		columns {
			stepUnita {
				column name: 'step_unita_progr'
				column name: 'step_unita_dal'
				column name: 'step_unita_ottica'
			}
		}
		dataAdozione column: 'data_adozione'
		dataScadenza column: 'data_scadenza'
		priorita	 column: 'priorita'
		dataOrdinamento column: 'data_ordinamento'
	}

	static hibernateFilters = {
		multiEnteFilter (condition: "ente = :enteCorrente", types: 'string')
	}
}
