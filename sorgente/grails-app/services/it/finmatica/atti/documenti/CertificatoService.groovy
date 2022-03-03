package it.finmatica.atti.documenti

import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.documenti.beans.AttiGestioneTesti
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.tipologie.TipoCertificato
import it.finmatica.gestioneiter.Attore
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.motore.WkfIterService

class CertificatoService {

	AttiGestoreCompetenze	gestoreCompetenze
	WkfIterService 			wkfIterService
	AttiGestioneTesti 		gestioneTesti
	IGestoreFile 			gestoreFile

	Certificato creaCertificato (IAtto documentoPrincipale, TipoCertificato tipologia, String tipoCertificato, boolean secondaPubblicazione) {
		// creo il certificato
		Certificato c 	= new Certificato()
		c.tipologia		= tipologia
		c.tipo 			= tipoCertificato
		c.stato			= StatoDocumento.DA_PROCESSARE
		c.modelloTesto	= tipologia.modelloTesto
		c.secondaPubblicazione = secondaPubblicazione

		// lo aggiungo al documento
		documentoPrincipale.addToCertificati(c)
		documentoPrincipale.save()

		// creo il testo del certificato
		gestioneTesti.generaTestoDocumento(c, true)

		// do le competenze che ci sono sul documento principale (ma solo in lettura)
		List<Attore> attori = gestoreCompetenze.getAttoriCompetenze(documentoPrincipale)
		WkfTipoOggetto tipoOggettoCertificato = WkfTipoOggetto.get(Certificato.TIPO_OGGETTO)
		for (Attore attore : attori) {
			gestoreCompetenze.assegnaCompetenze(c, tipoOggettoCertificato, attore, true, false, false, null)
		}

		// istanzio l'iter del certificato
		WkfCfgIter iterPubblicazione = WkfCfgIter.getIterIstanziabile(tipologia.progressivoCfgIter).get()
		wkfIterService.istanziaIter(iterPubblicazione, c)

		return c
	}
	
	boolean isCertificatoPresenteEConcluso (IAtto atto, TipoCertificato tipologia, String tipo, boolean secondaPubblicazione = false) {
		// tutti i certificati validi, con la tipologia richiesta, del tipo richiesto devono avere stato = CONCLUSO.
		int certificatiTrovati = 0
		for (Certificato certificato : atto.certificati) {
			if (certificato.valido && certificato.tipologia.id == tipologia.id && certificato.tipo == tipo && certificato.secondaPubblicazione == secondaPubblicazione) {
				if (certificato.stato != StatoDocumento.CONCLUSO) {
					return false
				}
				
				certificatiTrovati++
				continue
			}
		}
		
		return (certificatiTrovati > 0)
	}

    /**
     * Ritorna TRUE se tutti i certificati previsti dalla tipologia sono presenti, validi e conclusi.
     *
     * @param atto
     * @return
     */
	boolean isTuttiCertificatiConclusi (Determina determina) {
        if (determina.tipologia.tipoCertPubb 	!= null && !isCertificatoPresenteEConcluso(determina, determina.tipologia.tipoCertPubb, Certificato.CERTIFICATO_PUBBLICAZIONE)) {
            return false
        }

        if (determina.tipologia.tipoCertAvvPubb != null && !isCertificatoPresenteEConcluso(determina, determina.tipologia.tipoCertAvvPubb, Certificato.CERTIFICATO_AVVENUTA_PUBBLICAZIONE)) {
            return false
        }

        if (determina.tipologia.tipoCertPubb2 	!= null && !isCertificatoPresenteEConcluso(determina, determina.tipologia.tipoCertPubb2, Certificato.CERTIFICATO_PUBBLICAZIONE, true)) {
            return false
        }

        if (determina.tipologia.tipoCertAvvPubb2!= null && !isCertificatoPresenteEConcluso(determina, determina.tipologia.tipoCertAvvPubb2, Certificato.CERTIFICATO_AVVENUTA_PUBBLICAZIONE, true)) {
            return false
        }

        if (determina.eseguibilitaImmediata) {
            if (determina.tipologia.tipoCertImmEseg != null && !isCertificatoPresenteEConcluso(determina, determina.tipologia.tipoCertImmEseg, Certificato.CERTIFICATO_IMMEDIATA_ESEGUIBILITA)) {
                return false
            }
        }
        else if (determina.tipologia.tipoCertEsec 	!= null && !isCertificatoPresenteEConcluso(determina, determina.tipologia.tipoCertEsec, Certificato.CERTIFICATO_ESECUTIVITA)) {
            return false
        }

        return true
	}

    /**
     * Ritorna TRUE se tutti i certificati previsti dalla tipologia sono presenti, validi e conclusi.
     *
     * @param atto
     * @return
     */
	boolean isTuttiCertificatiConclusi (Delibera delibera) {
        if (delibera.propostaDelibera.tipologia.tipoCertPubb 	!= null && !isCertificatoPresenteEConcluso(delibera, delibera.propostaDelibera.tipologia.tipoCertPubb, Certificato.CERTIFICATO_PUBBLICAZIONE)) {
            return false
        }

        if (delibera.propostaDelibera.tipologia.tipoCertAvvPubb != null && !isCertificatoPresenteEConcluso(delibera, delibera.propostaDelibera.tipologia.tipoCertAvvPubb, Certificato.CERTIFICATO_AVVENUTA_PUBBLICAZIONE)) {
            return false
        }

        if (delibera.propostaDelibera.tipologia.tipoCertPubb2 	!= null && !isCertificatoPresenteEConcluso(delibera, delibera.propostaDelibera.tipologia.tipoCertPubb2, Certificato.CERTIFICATO_PUBBLICAZIONE, true)) {
            return false
        }

        if (delibera.propostaDelibera.tipologia.tipoCertAvvPubb2!= null && !isCertificatoPresenteEConcluso(delibera, delibera.propostaDelibera.tipologia.tipoCertAvvPubb2, Certificato.CERTIFICATO_AVVENUTA_PUBBLICAZIONE, true)) {
            return false
        }

		if (delibera.eseguibilitaImmediata) {
			if (delibera.propostaDelibera.tipologia.tipoCertImmEseg != null && !isCertificatoPresenteEConcluso(delibera, delibera.propostaDelibera.tipologia.tipoCertImmEseg, Certificato.CERTIFICATO_IMMEDIATA_ESEGUIBILITA)) {
				return false
			}
		}
		else {
			if (delibera.propostaDelibera.tipologia.tipoCertEsec != null && !isCertificatoPresenteEConcluso(delibera, delibera.propostaDelibera.tipologia.tipoCertEsec, Certificato.CERTIFICATO_ESECUTIVITA)) {
				return false
			}
		}

        return true
	}
}
