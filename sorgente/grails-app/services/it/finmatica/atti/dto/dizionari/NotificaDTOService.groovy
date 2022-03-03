package it.finmatica.atti.dto.dizionari

import it.finmatica.atti.dizionari.Notifica
import it.finmatica.atti.dizionari.NotificaEmail
import it.finmatica.atti.exceptions.AttiRuntimeException

class NotificaDTOService {

    public NotificaDTO salva (NotificaDTO notificaDto) {
		Notifica notifica = Notifica.get(notificaDto.id)?:new Notifica()
		if (notifica.version != notificaDto.version) {
			throw new AttiRuntimeException(AttiRuntimeException.ERRORE_MODIFICA_CONCORRENTE)
		}
		notifica.tipoNotifica 	= notificaDto.tipoNotifica
		notifica.oggetti		= notificaDto.oggetti
		notifica.oggetto		= notificaDto.oggetto
		notifica.titolo			= notificaDto.titolo
		notifica.testo	 		= notificaDto.testo
		notifica.valido			= notificaDto.valido
		notifica.commissione	= notificaDto.commissione?.domainObject
		notifica.modalitaInvio  = notificaDto.modalitaInvio
		notifica.allegati		= notificaDto.allegati

		notifica.save (failOnError: true)

		return Notifica.get(notifica.id).toDTO()
    }

	public void elimina (NotificaDTO notificaDto) {
		Notifica.get(notificaDto.id).delete(failOnError: true)
	}

	public NotificaEmailDTO salva (NotificaEmailDTO notificaEmailDto) {
		NotificaEmail notificaEmail = notificaEmailDto.domainObject?:new NotificaEmail()
		notificaEmail.notifica 	= notificaEmailDto.notifica.domainObject
		notificaEmail.email 	= notificaEmailDto.email?.domainObject
		notificaEmail.ruolo 	= notificaEmailDto.ruolo?.domainObject
		notificaEmail.soggetto	= notificaEmailDto.soggetto?.domainObject
		notificaEmail.unita		= notificaEmailDto.unita?.domainObject
		notificaEmail.funzione	= notificaEmailDto.funzione

		notificaEmail.save (failOnError: true)

		return notificaEmail.get(notificaEmail.id).toDTO()
	}

	public NotificaEmailDTO elimina (NotificaEmailDTO notificaEmailDto) {
		NotificaEmail.get(notificaEmailDto.id).delete(failOnError: true)
		notificaEmailDto.id 	 = -1
		notificaEmailDto.version = 0
		return notificaEmailDto
	}

	public NotificaDTO duplica (NotificaDTO notificaDto) {
		Notifica notificaOriginale = Notifica.get(notificaDto.id);

		Notifica notificaDuplicata = new Notifica();
		notificaDuplicata.tipoNotifica 	= notificaOriginale.tipoNotifica
		notificaDuplicata.oggetti		= notificaOriginale.oggetti
		notificaDuplicata.oggetto		= notificaOriginale.oggetto
		notificaDuplicata.titolo		= notificaOriginale.titolo+" - Duplica"
		notificaDuplicata.testo	 		= notificaOriginale.testo
		notificaDuplicata.valido		= notificaOriginale.valido
		notificaDuplicata.commissione	= notificaOriginale.commissione
		notificaDuplicata.modalitaInvio = notificaOriginale.modalitaInvio
		notificaDuplicata.allegati		= notificaOriginale.allegati

		for (NotificaEmail notificaEmail : notificaOriginale.notificheEmail) {
			NotificaEmail notificaEmailDuplicata = new NotificaEmail ();

			notificaEmailDuplicata.email 	= notificaEmail.email
			notificaEmailDuplicata.ruolo 	= notificaEmail.ruolo
			notificaEmailDuplicata.soggetto	= notificaEmail.soggetto
			notificaEmailDuplicata.unita	= notificaEmail.unita
			notificaEmailDuplicata.funzione	= notificaEmail.funzione

			notificaDuplicata.addToNotificheEmail(notificaEmailDuplicata)
		}

		notificaDuplicata.save();

		return notificaDuplicata.toDTO();
	}
}
