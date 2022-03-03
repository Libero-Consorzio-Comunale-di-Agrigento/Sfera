package it.finmatica.atti.documenti.beans

import org.springframework.scheduling.annotation.Async

class NotificheAsyncDispatcher {

	NotificheAsyncService notificheAsyncService

	@Async
	void inviaNotificheAsync (String utente, String ente, String ottica, def codaOperazioni) {
		notificheAsyncService.inviaNotifiche (codaOperazioni, utente, ente);
	}

}
