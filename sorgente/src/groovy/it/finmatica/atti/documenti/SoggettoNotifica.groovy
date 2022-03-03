package it.finmatica.atti.documenti

import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.ad4.dto.autenticazione.Ad4RuoloDTO
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.dto.documenti.DestinatarioNotificaDTO
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

/*
 * Classe per la raccolta dei dati di notifica.
 */
class SoggettoNotifica implements Comparable {

	Ad4RuoloDTO 			ruoloAd4DTO
	Ad4UtenteDTO 			utenteDTO
	DestinatarioNotificaDTO destinatarioNotificaDTO
	So4UnitaPubbDTO 		unitaSo4DTO
	As4SoggettoCorrenteDTO 	soggettoDTO

	String funzione
	String assegnazione = DestinatarioNotificaAttivita.NOTIFICA_UTENTE
	String competenza   = DestinatarioNotifica.TIPO_NOTIFICA_CONOSCENZA
	String email
	String idAttivita

    SoggettoNotifica () {

    }

    SoggettoNotifica (DestinatarioNotifica destinatarioNotifica) {
        setDestinatarioNotifica(destinatarioNotifica)
		setIdAttivita(destinatarioNotifica.attivita?.idAttivita)
        if (destinatarioNotifica.email != null) {
            setEmail(destinatarioNotifica.email.indirizzoEmail)
        } else if (destinatarioNotifica.soggettoCorrente != null) {
            setSoggetto(destinatarioNotifica.soggettoCorrente)
			setEmail(destinatarioNotifica.soggettoCorrente.indirizzoWeb)
            setUtente(destinatarioNotifica.soggettoCorrente.utenteAd4)
        }
    }


    Ad4Ruolo getRuoloAd4() {
		return ruoloAd4DTO?.domainObject
	}

	Ad4Utente getUtente() {
		return utenteDTO?.domainObject
	}

	DestinatarioNotifica getDestinatarioNotifica() {
		return destinatarioNotificaDTO?.domainObject
	}

	So4UnitaPubb getUnitaSo4() {
		return unitaSo4DTO?.domainObject
	}

	As4SoggettoCorrente getSoggetto() {
		return soggettoDTO?.domainObject
	}

	void setRuoloAd4(Ad4Ruolo ruoloAd4) {
		ruoloAd4DTO = ruoloAd4?.toDTO()
	}

	void setUtente(Ad4Utente utenteAd4) {
		utenteDTO = utenteAd4?.toDTO()
	}

	void setDestinatarioNotifica(DestinatarioNotifica destinatarioNotifica) {
		destinatarioNotificaDTO = destinatarioNotifica?.toDTO(["email", "utente", "unitaSo4"])
	}

	void setUnitaSo4(So4UnitaPubb unitaSo4) {
        unitaSo4DTO = unitaSo4?.toDTO()
	}

	void setSoggetto(As4SoggettoCorrente soggetto) {
		soggettoDTO = soggetto?.toDTO()
	}

	@Override
	int compareTo(Object o) {
		if (!(o instanceof SoggettoNotifica)) {
			return 1
		}

		SoggettoNotifica s = (SoggettoNotifica)o

		if (this.utenteDTO != null && s.utenteDTO != null && this.utenteDTO.id == s.utenteDTO.id) {
			return 0
		}

		if (this.soggettoDTO != null && s.soggettoDTO != null && this.soggettoDTO.id == s.soggettoDTO.id) {
			return 0
		}

		if (this.email != null && s.email != null && this.email == s.email) {
			return 0
		}

		return 1
	}

	boolean equals (Object o) {
		return (compareTo(o) == 0)
	}

    String getDenominazione () {
        if (destinatarioNotificaDTO != null) {
            return destinatarioNotificaDTO.denominazione
        }

        if (utenteDTO != null) {
            return utenteDTO.nominativoSoggetto
        }

        if (soggettoDTO != null) {
            return soggettoDTO.denominazione
        }

        if (unitaSo4DTO != null) {
            return unitaSo4DTO.descrizione
        }

        return email
    }
}