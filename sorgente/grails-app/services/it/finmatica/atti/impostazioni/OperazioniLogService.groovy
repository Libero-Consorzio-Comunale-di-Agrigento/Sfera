package it.finmatica.atti.impostazioni

import grails.plugin.springsecurity.SpringSecurityService

class OperazioniLogService {
	SpringSecurityService springSecurityService

	void creaLog (Long id, String tipoOggetto, String pagina, String operazione, String descrizione) {
		OperazioniLog assistenzaLog = new OperazioniLog()
		assistenzaLog.dataOperazione = new Date()
		assistenzaLog.pagina = pagina
		assistenzaLog.descrizione = descrizione
		assistenzaLog.operazione = operazione
		assistenzaLog.utente = springSecurityService.currentUser
		assistenzaLog.idDocumento = id
		assistenzaLog.tipoOggetto = tipoOggetto
		assistenzaLog.save()
	}

}
