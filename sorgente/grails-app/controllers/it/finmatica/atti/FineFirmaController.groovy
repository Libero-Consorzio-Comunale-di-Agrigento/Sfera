package it.finmatica.atti

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.documenti.beans.AttiGestoreTransazioneFirma
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.integrazioni.AttiFirmaService
import it.finmatica.grails.firmadigitale.FirmaDigitaleFile
import it.finmatica.grails.firmadigitale.FirmaDigitaleService
import it.finmatica.grails.firmadigitale.FirmaDigitaleTransazione
import it.finmatica.zkutils.SuccessHandler
import org.apache.log4j.Logger
import org.hibernate.SessionFactory

class FineFirmaController {

	private static final Logger log = Logger.getLogger(FineFirmaController.class);

	SpringSecurityService 	springSecurityService
	FirmaDigitaleService 	firmaDigitaleService
	AttiFirmaService 		attiFirmaService
    SessionFactory          sessionFactory
	SuccessHandler			successHandler

	AttiGestoreTransazioneFirma attiGestoreTransazioneFirma
	
	def index () {
		// se non ho l'utente corrente, lo riautentico usando i parametri
		if (springSecurityService.currentUser == null) {
			attiGestoreTransazioneFirma.autenticaConToken (params.long("idTransazioneFirma"), params.utente, params.ente, params.token)
		}
		
		render (view: 'index', model:[finito:false])
	}

	def termina () {
		// se non ho l'utente corrente, lo riautentico usando i parametri
		if (springSecurityService.currentUser == null) {
			attiGestoreTransazioneFirma.autenticaConToken (params.long("idTransazioneFirma"), params.utente, params.ente, params.token)
		}
		
		if (params.idTransazioneFirma == null) {
			throw new AttiRuntimeException ("Nessun idTransazioneFirma nella request!")
		}

		long idTransazioneFirma = params.long('idTransazioneFirma')
		try {

			// per prima cosa, salvo tutti file firmati sui relativi documenti:
			attiFirmaService.fineFirmaSalvaFileFirmati(idTransazioneFirma)

			// per ogni file firmato, se è un documento da sbloccare, lo sblocco:
			List<FirmaDigitaleFile> fileFirmati = firmaDigitaleService.getFileTransazione(idTransazioneFirma)
			for (FirmaDigitaleFile fileFirmato : fileFirmati) {
				// creo una nuova sessione hibernate per ogni documento.
				// Questo lo faccio perché potrei dover processare molti documenti e non voglio che si generi
				// troppo casino con una sola sessione hibernate più grande e più transazioni diverse
				// (siccome ogni invocazione al metodo fineFirmaSbloccaFlusso è una nuova transazione)
                sessionFactory.currentSession.clear()

                attiFirmaService.fineFirmaSbloccaFlusso(idTransazioneFirma, fileFirmato.id)
			}

			// elimino i vari dati di questa transazione di firma.
			firmaDigitaleService.eliminaTransazione(idTransazioneFirma)

		} finally {
			// svuoto i messaggi del message handler:
			successHandler.clearMessages();
		}

		render (view: 'index', model:[finito:true])
	}
}
