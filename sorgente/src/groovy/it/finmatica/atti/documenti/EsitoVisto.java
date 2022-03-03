package it.finmatica.atti.documenti;

public enum EsitoVisto {
	  DA_VALUTARE		// indica che il visto non ha un esito.
	, NON_APPOSTO
	, CONTRARIO
	, FAVOREVOLE
	, RIMANDA_INDIETRO	// il visto non ha un esito e rimanda la proposta indietro (tipicamente alla redazione),
	, FAVOREVOLE_CON_PRESCRIZIONI
}
