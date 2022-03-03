package it.finmatica.gestionedocumenti.soggetti

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.ISoggettoDocumento
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.gestionedocumenti.documenti.Documento
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class DocumentoSoggetto implements ISoggettoDocumento {

	TipoSoggetto tipoSoggetto
	Ad4Utente    utenteAd4
	So4UnitaPubb unitaSo4

	// campi per la gestione di più soggetti.
	int 	sequenza = 0
	boolean attivo   = true

    Documento documento
	static    belongsTo = [documento : Documento]

	static mapping = {
		table 			'gdo_documenti_soggetti'
		id 			 	column: 'id_documento_soggetto'
		documento		column: 'id_documento'
		tipoSoggetto	column: 'tipo_soggetto'
		utenteAd4  	 	column: 'utente'
		attivo			type: 'yes_no'
		columns {
			unitaSo4 {
				column name: 'unita_progr', index: 'unita_idx'
				column name: 'unita_dal', index: 'unita_idx'
				column name: 'unita_ottica', index: 'unita_idx'
			}
		}
	}

    static constraints = {
		utenteAd4 	nullable: true
		unitaSo4	nullable: true
    }

	@Override
	IDocumento getDocumentoPrincipale () {
		return documento
	}
}
