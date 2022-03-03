package it.finmatica.atti.commons

import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.documenti.SoggettoNotifica
import it.finmatica.atti.documenti.storico.StoricoService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAttore
import it.finmatica.gestioneiter.motore.WkfAttoreStep
import it.finmatica.gestioneiter.motore.WkfIter
import it.finmatica.gestioneiter.motore.WkfStep
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubb

class SoggettiAttoriService {

	StrutturaOrganizzativaService strutturaOrganizzativaService
	StoricoService				  storicoService

	List<WkfAttoreStep> getListaAttoriFlusso (WkfIter iter) {
		//gli atti trascodificati non hanno iter associato. Ma è capitato che un atto nuovo vada ad annullare un atto vecchio gestito in gs4
		//in questo caso le notifiche danno errore
		if (iter == null) {
			return []
		}

		return WkfAttoreStep.createCriteria().list {
			step {
				eq ("iter.id", iter.id)
			}
		}
	}

	Collection<SoggettoNotifica> calcolaSoggettiNotificaStepCorrente (WkfIter iter) {
		if (iter == null) {
			return []
		}

		return calcolaSoggettiNotificaStep (iter.stepCorrente)
	}

	Collection<SoggettoNotifica> calcolaSoggettiNotificaFlusso (WkfIter iter) {
		List<WkfAttoreStep> attoriStep = getListaAttoriFlusso(iter)

		List<SoggettoNotifica> soggettiAs4 = []
		for (WkfAttoreStep attore : attoriStep) {
			soggettiAs4.addAll(calcolaSoggettiNotifica(attore))
		}

		return soggettiAs4
	}

	Collection<SoggettoNotifica> calcolaSoggettiNotificaTuttiFlussi (IDocumentoIterabile documento) {
		List<SoggettoNotifica> soggettiAs4 = calcolaSoggettiNotificaFlusso(documento.iter)
		List iterStorico = storicoService.getIterStorico(documento);

		for (WkfIter iter: iterStorico) {
			List<WkfAttoreStep> attoriStep = getListaAttoriFlusso(iter)

			for (WkfAttoreStep attore : attoriStep) {
				soggettiAs4.addAll(calcolaSoggettiNotifica(attore))
			}
		}

		return soggettiAs4
	}

	/**
	 * Ritorna l'elenco dei soggetti che corrispondono agli attori del flusso, meno i soggetti degli attori dello step corrente.
	 *
	 * @param iter
	 * @return 	l'elenco dei soggetti che corrispondono agli attori del flusso, meno i soggetti degli attori dello step corrente.
	 */
	Collection<SoggettoNotifica> calcolaSoggettiNotificaFlussoTranneStepCorrente (WkfIter iter) {
		if (iter == null) {
			return []
		}
		
		List<SoggettoNotifica> soggettiStepCorrente = calcolaSoggettiNotificaStep(iter.stepCorrente)
		List<SoggettoNotifica> soggettiAttoriFlusso = calcolaSoggettiNotificaFlusso(iter)

		// prendo solo gli id dei soggetti che non sono attori dello step corrente
		// posso usare la 'removeAll' perché SoggettoNotifica implementa l'interfaccia Comparable
		soggettiAttoriFlusso.removeAll (soggettiStepCorrente)

		return soggettiAttoriFlusso
	}

	Collection<SoggettoNotifica> calcolaSoggettiNotificaStep (WkfStep step) {
		List<SoggettoNotifica> soggettiAs4 = []
		for (WkfAttoreStep attore : step.attori) {
			soggettiAs4.addAll(calcolaSoggettiNotifica(attore))
		}

		return soggettiAs4
	}

	/**
	 * Questa funzione calcola tutti i SoggettiNotifica per un Attore.
	 * Ritorna la classe SoggettoNotifica perché in fase di assegnazione delle competenze dal JWorklistService ho bisogno di sapere
	 * se devo assegnare le competenze per utente, unità, ruolo o ruolo + unità.
	 * 
	 * -> http://svi-redmine/issues/14719
	 * 
	 * @param attore
	 * @return
	 */
	private Collection<SoggettoNotifica> calcolaSoggettiNotifica (WkfAttoreStep attore) {
		List<SoggettoNotifica> soggettiAs4 = []
		List<So4ComponentePubb> listaComponenti = null
		if (attore.utenteAd4) {
			As4SoggettoCorrente sog = As4SoggettoCorrente.findByUtenteAd4(attore.utenteAd4)

			// in teoria è sempre != null
			if (sog != null) {
				soggettiAs4.add(new SoggettoNotifica(email: sog.indirizzoWeb, soggetto:sog, utente:attore.utenteAd4))
			}

		} else if (attore.unitaSo4 && attore.ruoloAd4) {
			listaComponenti = strutturaOrganizzativaService.getComponentiConRuoloInUnita (attore.ruoloAd4.ruolo, attore.unitaSo4.progr, Impostazioni.OTTICA_SO4.valore)
			
			for (So4ComponentePubb c : listaComponenti) {
				if (c.soggetto != null) {
					soggettiAs4.add(new SoggettoNotifica(email: c.soggetto.indirizzoWeb, soggetto:c.soggetto, utente:c.soggetto.utenteAd4, ruoloAd4: attore.ruoloAd4, unitaSo4: attore.unitaSo4))
				}
			}

		} else if (attore.unitaSo4) {
			listaComponenti = So4ComponentePubb.componentiUnitaPubb(attore.unitaSo4.progr, Impostazioni.OTTICA_SO4.valore, new Date()).list()
			
			for (So4ComponentePubb c : listaComponenti) {
				if (c.soggetto != null) {
					soggettiAs4.add(new SoggettoNotifica(email: c.soggetto.indirizzoWeb, soggetto:c.soggetto, utente:c.soggetto.utenteAd4, unitaSo4: attore.unitaSo4))
				}
			}

		} else if (attore.ruoloAd4) {
			listaComponenti = strutturaOrganizzativaService.getComponentiConRuoloInOttica(attore.ruoloAd4.ruolo, Impostazioni.OTTICA_SO4.valore)
			
			for (So4ComponentePubb c : listaComponenti) {
				if (c.soggetto != null) {
					soggettiAs4.add(new SoggettoNotifica(email: c.soggetto.indirizzoWeb, soggetto:c.soggetto, utente:c.soggetto.utenteAd4, ruoloAd4: attore.ruoloAd4))
				}
			}
		}

		return soggettiAs4
	}
}
