package it.finmatica.atti.documenti

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

class SoggettoDocumento implements ISoggettoDocumento {
	Long id
	int sequenza
	IDocumento documentoPrincipale
	So4UnitaPubb unitaSo4
	Ad4Utente utenteAd4
	boolean attivo
	TipoSoggetto tipoSoggetto
}
