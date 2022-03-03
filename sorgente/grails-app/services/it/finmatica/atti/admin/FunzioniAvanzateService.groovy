package it.finmatica.atti.admin

import grails.util.GrailsNameUtils
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.viste.RicercaUnitaDocumentoAttivo
import it.finmatica.atti.dto.documenti.viste.RicercaUnitaDocumentoAttivoDTO
import it.finmatica.gestioneiter.Attore
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestioneiter.motore.WkfAttoreStep
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil

class FunzioniAvanzateService {

	AttiGestoreCompetenze 	gestoreCompetenze
	GrailsApplication		grailsApplication
	NotificheService 	  	notificheService

	public void cambiaUtenteDocumenti (def documenti, Ad4UtenteDTO utentePrecedenteDto, Ad4UtenteDTO utenteFirmatarioDto, String tipoSoggetto) {
		Ad4Utente utentePrecedente = utentePrecedenteDto.domainObject;
		Ad4Utente utenteFirmatario = utenteFirmatarioDto.domainObject;
		for (def documento : documenti) {
			def d = DocumentoFactory.getDocumento(documento.idDocumento, documento.tipoDocumento);
			if (null == d && documento.tipoDocumento == Delibera.TIPO_OGGETTO){
				d = Delibera.findByPropostaDelibera(PropostaDelibera.get(documento.idDocumento));
			}
			cambiaUtente (d, utentePrecedente, utenteFirmatario, tipoSoggetto);
		}
	}

	public void cambiaUtente (def documento, Ad4Utente utentePrecedente, Ad4Utente nuovoUtente, String tipoSoggetto) {
		// per cambiare il firmatario, il procedimento è:
		// 1) cambiare la tabella firmatari
		// 2) cambiare le competenze
		// 3) cambiare il soggetto sul documento
		// 4) eliminare dalla jworklist (se presente) le notifiche per l'utente vecchio, quindi mettere quelle per l'utente nuovo.

		// 1) cambiare il firmatario sulla tabella dei firmatari (se presente)
		def firmatari = Firmatario.createCriteria().list {
			eq (GrailsNameUtils.getPropertyName(GrailsHibernateUtil.unwrapIfProxy(documento).class), documento)
			eq ("firmato", 		 false)
			eq ("firmatario.id", utentePrecedente.id)
		}

		for (def f : firmatari) {
			f.firmatario = nuovoUtente
			f.save()
		}

		// 2) cambiare le competenze:
		// rimuovo le competenze in scrittura dell'utente precedente:
		def tipoOggetto = WkfTipoOggetto.get(documento.TIPO_OGGETTO)
		gestoreCompetenze.rimuoviCompetenze (documento, tipoOggetto, new Attore(utenteAd4:utentePrecedente), true, true, false, null)

		// le riassegno in lettura:
		gestoreCompetenze.assegnaCompetenze (documento, tipoOggetto, new Attore(utenteAd4:utentePrecedente), true, false, false, null)

		// assegno le competenze in scrittura all'utente richiesto:
		gestoreCompetenze.assegnaCompetenze (documento, tipoOggetto, new Attore(utenteAd4:nuovoUtente), true, true, false, null)

		// 3) cambio il soggetto:
		if (tipoSoggetto == "UTENTE_IN_CARICO") {
			// se sto cambiando l'utente "in carico", allora ciclo su tutti i soggetti del documento e cambio anche quel soggetto, altrimenti
			// potrei ritrovarmi in situazioni spiacevoli (ad es: il documento è in carico al redattore, ma cambio solo l'incarico e non il soggetto redattore, quindi quando il nuovo utente aprirà il documento
			// avrà il doc in modifica ma non vedrà i pulsanti perché non verrà riconosciuto come l'attore giusto.
			for (ISoggettoDocumento soggetto : documento.soggetti) {
				if (soggetto.utenteAd4?.id == nuovoUtente.id) {
					documento.setSoggetto (soggetto.tipoSoggetto, nuovoUtente, null, soggetto.sequenza)
				}
			}
		} else {
			documento.setSoggetto (tipoSoggetto, nuovoUtente, null)
		}

		// 4) cambio l'attore dello step:
		def attori = documento.iter.stepCorrente.attori
		for (def a : attori) {
			if (a.utenteAd4.id == utentePrecedente.id) {
				a.utenteAd4 = nuovoUtente
				a.save()
			}
		}

		// elimino le eventuali notifiche esistenti
		notificheService.eliminaNotifica(documento, utentePrecedente, TipoNotifica.ASSEGNAZIONE)

		// ricreo le notifiche per il nuovo utente
		notificheService.notifica (TipoNotifica.ASSEGNAZIONE, documento);

		documento.save()
	}

	public void cambiaUnitaDocumenti (List<RicercaUnitaDocumentoAttivoDTO> documenti, So4UnitaPubbDTO unitaSo4Old, So4UnitaPubbDTO unitaSo4New) {
		for (RicercaUnitaDocumentoAttivoDTO documento : documenti) {
			def d = DocumentoFactory.getDocumento(documento.idDocumento, documento.tipoDocumento)
			cambiaUnita (d, unitaSo4Old.domainObject, unitaSo4New.domainObject)
			if (documento.tipoDocumento.equals("DELIBERA")){
				cambiaUnita (d.propostaDelibera, unitaSo4Old.domainObject, unitaSo4New.domainObject)
			}
		}
	}

	public void cambiaUnita (def documento, So4UnitaPubb unitaPrecedente, So4UnitaPubb nuovaUnita) {
		// per cambiare l'unità di un documento, il procedimento è:
		// 1) cambiare il soggetto sul documento
		// 2) cambiare le competenze
		// 3) cambiare l'attore dello step
		// 4) eliminare dalla jworklist (se presente) le notifiche per l'utente vecchio
		// 5) ricreo le notifiche per la nuova unità quindi mettere quelle per l'utente nuovo

		// 1) cambio il soggetto:
		for (ISoggettoDocumento soggetto : documento.soggetti) {
			if (unitaPrecedente.equals(soggetto.unitaSo4)) {
				documento.setSoggetto (soggetto.tipoSoggetto.codice, soggetto.utenteAd4, nuovaUnita)
			}
		}

		// 2) cambiare le competenze:
		// rimuovo le competenze in lettura e scrittura all'unità precedente:
		def tipoOggetto = WkfTipoOggetto.get(documento.TIPO_OGGETTO);
		gestoreCompetenze.rimuoviCompetenze (documento, tipoOggetto, new Attore(unitaSo4:unitaPrecedente), true, true, false, null);

		// assegno le competenze in scrittura all'utente richiesto:
		gestoreCompetenze.assegnaCompetenze (documento, tipoOggetto, new Attore(unitaSo4:nuovaUnita), true, true, false, null);

		//cambio l'attore dell'iter e aggiorno le notifiche solo se l'iter non è concluso
		if (documento.iter.dataFine == null) {
			// 3) cambio l'attore dello step:
			for (WkfAttoreStep a : documento.iter.stepCorrente.attori) {

				// per ogni attore dello step che ha l'unità vecchia, cambio l'unità con quella nuova.
				if (unitaPrecedente.equals(a.unitaSo4)) {

					// imposto la nuova unità
					a.unitaSo4 = nuovaUnita;
					a.save();
				}
			}

			documento.save()

			// 4) Elimino le eventuali notifiche di cambio step esistenti
			if (notificheService.isNotificaPresente(documento, unitaPrecedente, TipoNotifica.ASSEGNAZIONE)) {

				// elimino le notifiche legate all'unità
				notificheService.eliminaNotifica(documento, unitaPrecedente, TipoNotifica.ASSEGNAZIONE)

				// ricreo le notifiche per l'unità usando i destinatari appena calcolati
				notificheService.notifica(TipoNotifica.ASSEGNAZIONE, documento)
			}
		}

		documento.save()
	}
}
