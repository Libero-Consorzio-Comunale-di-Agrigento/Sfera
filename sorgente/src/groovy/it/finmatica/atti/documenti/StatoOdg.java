package it.finmatica.atti.documenti;

public enum StatoOdg {

	  INIZIALE  // valore di default in creazione della proposta, prima di entrare
				// in ODG.
	, DA_COMPLETARE // la proposta ha finito l'istruttoria (cioè il flusso prima
					// del nodo di odg) ed è all'attenzione della segreteria che
					// la deve controllare
	, IN_ISTRUTTORIA    // la proposta non ha finito l'istruttoria ed è
						// all'attenzione della segreteria che la deve
						// controllare
	, COMPLETO_IN_ISTRUTTORIA   // la proposta non ha finito l'istruttoria ma è
								// stata controllata dalla segreteria che l'ha
								// predisposta per l'odg.
	, NON_VERBALIZZARE  // la proposta non ha finito l'istruttoria ed è stata
						// controllata dalla segreteria ed è inserita in seduta.
						// Inoltre, questo valore è valido anche per le proposte
						// già inserite in ODG che hanno quindi oggettoSeduta !=
						// null.
	, COMPLETO  // la proposta è stata controllata dalla segreteria e può essere
				// inserita in seduta
	, INSERITO  // la proposta è inserita in seduta
	, CONCLUSO; // la proposta ha concluso il nodo di ODG e ne è uscita.

	/*
	 * Le fasi del documento sono:
	 * 1) il documento è in gestione ai vari utenti durante l'istruttoria
	 * 2b) mentre il documento è ancora in istruttoria, viene posto all'attenzione della segreteria
	 * 2) il documento termina la sua istruttoria e va all'attenzione della segreteria
	 * 3b) mentre il documento è ancora in istruttoria, la segreteria lo predispone per l'inserimento in odg.
	 * 3) la segreteria verifica il documento e lo predispone per l'inserimento in odg.
	 * 4b) mentre il documento è ancora in istruttoria, viene inserito in una proposta in odg.
	 * 4) il documento viene inserito in una seduta dell'odg
	 * 5) il documento conclude la gestione dell'odg (viene concluso solo se prima ha terminato anche l'istruttoria).
	 */

	public static void iniziaIstruttoria(IProposta proposta) {
		proposta.setStatoOdg(INIZIALE);
	}

	public static void mandaInSegreteriaIstruttoriaInCorso(IProposta proposta) {
		proposta.setStatoOdg(IN_ISTRUTTORIA);
	}

	public static void mandaInSegreteria(IProposta proposta) {
// TODO: se continuano gli errori sul campo statoODG, conviene inserire un controllo per verificare se la proposta è attualmente in odg, e in questo caso non fare nulla.
// 		Il controllo si può basare sul campo oggettoSeduta della proposta
		switch (proposta.getStatoOdg()) {
		case COMPLETO_IN_ISTRUTTORIA:
			proposta.setStatoOdg(COMPLETO);
			break;
		case NON_VERBALIZZARE:
			proposta.setStatoOdg(INSERITO);
			break;
		case IN_ISTRUTTORIA:
			proposta.setStatoOdg(DA_COMPLETARE);
			break;
		case INSERITO:
			//se la proposta è già nello stato INSERITO...vuol dire che è già in una seduta e non devo fare nulla
			break;
		case CONCLUSO:
			//se la proposta è già nello stato CONCLUSO...vuol dire che è terminata la discussione e non devo fare nulla
			break;
		default:
			proposta.setStatoOdg(DA_COMPLETARE);
			break;
		}
	}

	public static void mandaInOdg(IProposta proposta) {
		if (isInIstruttoria(proposta)) {
			proposta.setStatoOdg(COMPLETO_IN_ISTRUTTORIA);
		} else {
			proposta.setStatoOdg(COMPLETO);
		}
	}

	public static void togliDaOdg(IProposta proposta) {
		if (isInIstruttoria(proposta)) {
			proposta.setStatoOdg(IN_ISTRUTTORIA);
		} else {
			proposta.setStatoOdg(DA_COMPLETARE);
		}
	}

	public static void inserisciInSeduta(IProposta proposta) {
		if (isInIstruttoria(proposta)) {
			proposta.setStatoOdg(NON_VERBALIZZARE);
		} else {
			proposta.setStatoOdg(INSERITO);
		}
	}

	public static void togliDaSeduta(IProposta proposta) {
		if (isInIstruttoria(proposta)) {
			proposta.setStatoOdg(COMPLETO_IN_ISTRUTTORIA);
		} else {
			proposta.setStatoOdg(COMPLETO);
		}
	}

	public static void concludiOdg(IProposta proposta) {
		proposta.setStatoOdg(CONCLUSO);
	}

	public static boolean isInOdg(StatoOdg statoOdg) {
		return (statoOdg == COMPLETO || statoOdg == COMPLETO_IN_ISTRUTTORIA);
	}

	public static boolean isInIstruttoria(IProposta proposta) {
		return isInIstruttoria(proposta.getStatoOdg());
	}

	public static boolean isInIstruttoria(StatoOdg statoOdg) {
		for (StatoOdg inIstruttoria : getStatiIstruttoria()) {
			if (inIstruttoria == statoOdg)
				return true;
		}
		return false;
	}

	public static StatoOdg[] getStatiIstruttoria() {
		return new StatoOdg[] { IN_ISTRUTTORIA, COMPLETO_IN_ISTRUTTORIA, NON_VERBALIZZARE };
	}
}
