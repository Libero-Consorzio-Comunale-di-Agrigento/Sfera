package it.finmatica.atti.documenti

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class PropostaDeliberaSoggetto implements ISoggettoDocumento {

	PropostaDelibera 	propostaDelibera
	TipoSoggetto 		tipoSoggetto
	Ad4Utente 			utenteAd4
	So4UnitaPubb 		unitaSo4

	// campi per la gestione di più soggetti.
	int sequenza;
	boolean attivo;

	static belongsTo = [propostaDelibera : PropostaDelibera]

	static mapping = {
		table 				'proposte_delibera_soggetti'
		id 			 		column: 'id_proposta_delibera_soggetto'
		propostaDelibera	column: 'id_proposta_delibera', index: 'prodelsog_prodel_fk'
		tipoSoggetto		column: 'tipo_soggetto', 		index: 'prodelsog_tipsog_fk'
		utenteAd4  	 		column: 'utente'
		attivo				type: 'yes_no'

		columns {
			unitaSo4 {
				column name: 'unita_progr'
				column name: 'unita_dal'
				column name: 'unita_ottica'
			}
		}
	}

	static constraints = {
		utenteAd4 	nullable: true
		unitaSo4	nullable: true
	}

	// metodi di interfaccia:
	public PropostaDelibera getDocumentoPrincipale () {
		return this.propostaDelibera;
	}
}
