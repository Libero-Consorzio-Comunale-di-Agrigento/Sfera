package it.finmatica.atti.documenti;

public enum StatoDocumento {
	  MODIFICATO
	, NON_ESECUTIVO
    , ESECUTIVO
	, ATTESA_ESECUTIVITA_MANUALE	// flag introdotto per gestire l'esecutività manuale della delibera: http://svi-redmine/issues/17326
    , ADOTTATO
    , INTEGRATO
    , DA_ANNULLARE
	, ANNULLATO

	, PROPOSTA

	, DA_PROCESSARE
	, PROCESSATO
	, CONCLUSO

	, PUBBLICATO
}
