package it.finmatica.atti.documenti

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class DeterminaSoggetto implements ISoggettoDocumento {

	Determina 		determina
	TipoSoggetto 	tipoSoggetto
	Ad4Utente 		utenteAd4
	So4UnitaPubb	unitaSo4

	// campi per la gestione di più soggetti.
	int sequenza;
	boolean attivo;

	static belongsTo = [determina : Determina]

	static mapping = {
		table 			'determine_soggetti'
		id 			 	column: 'id_determina_soggetto'
		determina		column: 'id_determina',  index: 'detsog_det_fk'
		tipoSoggetto	column: 'tipo_soggetto', index: 'detsog_tipsog_fk'
		utenteAd4  	 	column: 'utente'
		attivo			type: 'yes_no'
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
	public Determina getDocumentoPrincipale () {
		return this.determina;
	}
}
