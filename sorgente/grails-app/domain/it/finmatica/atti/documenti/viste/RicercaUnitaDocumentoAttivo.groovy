package it.finmatica.atti.documenti.viste

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.gestioneiter.motore.WkfIter
import it.finmatica.so4.struttura.So4Amministrazione
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class RicercaUnitaDocumentoAttivo {

	String 	tipoDocumento
	Long	idDocumento
	Long 	idDocumentoPadre
	Long	idAtto
	Long	idProposta
	WkfIter iter
	Long	annoProposta
	Long	numeroProposta
	TipoRegistro registroProposta

	Long	annoAtto
	Long	numeroAtto
	TipoRegistro registroAtto

    String oggetto
    String stato

    Date dataEsecutivita
    Date dataPubblicazioneDal
    Date dataPubblicazioneAl

    TipoSoggetto tipoSoggetto
    Ad4Utente    utenteSoggetto
    So4UnitaPubb unitaSoggetto

    Ad4Utente utenteAttore
    So4UnitaPubb unitaAttore

	So4Amministrazione ente

	static mapping = {
		table 	'RICERCA_UNITA_DOCUMENTI_ATTIVI'
		version false
		id 		generator: 'assigned', name: 'idDocumento', column: 'id_documento'

        iter                column: 'id_iter'
        registroAtto        column: 'registro_atto'
        registroProposta    column: 'registro_proposta'
        tipoSoggetto        column: 'tipo_soggetto'
        utenteSoggetto      column: 'soggetto_utente'
        utenteAttore        column: 'attore_utente'
		ente 	            column: 'ente'

		columns {
			unitaSoggetto {
				column name: 'soggetto_unita_progr'
				column name: 'soggetto_unita_dal'
				column name: 'soggetto_unita_ottica'
			}
			unitaAttore {
				column name: 'attore_unita_progr'
				column name: 'attore_unita_dal'
				column name: 'attore_unita_ottica'
			}
		}
	}

	static hibernateFilters = {
		multiEnteFilter (condition: 'ente = :enteCorrente', types: 'string')
	}
}