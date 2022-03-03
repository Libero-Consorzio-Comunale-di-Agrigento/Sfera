package it.finmatica.atti.documenti;

import it.finmatica.ad4.autenticazione.Ad4Utente;
import it.finmatica.atti.impostazioni.TipoSoggetto;
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb;

public interface ISoggettoDocumento {

    Long getId();

	void setTipoSoggetto(TipoSoggetto tipoSoggetto);
	TipoSoggetto getTipoSoggetto();

	void setUtenteAd4(Ad4Utente utenteAd4);
	Ad4Utente getUtenteAd4();

	void setUnitaSo4(So4UnitaPubb unitaSo4);
	So4UnitaPubb getUnitaSo4();

	/**
	 * La sequenza del soggetto è 0-based
	 * @return
	 */
	int getSequenza();
	void setSequenza(int sequenza);

	boolean isAttivo();
	void setAttivo(boolean attivo);

	IDocumento getDocumentoPrincipale();
}
