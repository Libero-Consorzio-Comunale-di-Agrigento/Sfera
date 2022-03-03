package it.finmatica.so4

import it.finmatica.atti.impostazioni.Impostazione
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.so4.login.detail.Amministrazione
import it.finmatica.so4.login.detail.Ottica
import it.finmatica.so4.struttura.So4Amministrazione
import it.finmatica.so4.struttura.So4Ottica

import javax.servlet.http.Cookie

import grails.plugin.springsecurity.SpringSecurityUtils
import org.hibernate.FetchMode

class AttiMultiEnteController {

	def springSecurityService

    def index () {
		if (params.installazione == "1") {
			forward (controller:'admin', action:'installazioneEnte', params:params)
			return
		}

		// TODO: manca il caso da gestire: l'utente non appartiene agli enti disponibili da impostazioni.

		def labelBottone = (message(code: "conferma")?:"conferma")

		String codiceAmm = params.amministrazione
		String codiceOtt = null

		// se non ho il codice amministrazione, lo recupero:
		if (codiceAmm == null || codiceAmm.length() == 0) {
			def codiciAmm = Impostazioni.ENTI_SO4.valori

			// la classe Impostazioni ritorna il valore di default se non viene trovato su db.
			// Il valore di default per ENTI_SO4 è NESSUNO
			// Se viene recuperato il valore NESSUNO, significa che siamo in fase di prima installazione.
			if (codiciAmm == ['NESSUNO']) {
				def ottiche = So4Ottica.createCriteria().list {
					amministrazione {
						eq "ente", true
					}
					fetchMode("amministrazione", FetchMode.JOIN)
				}

				render (view: "../admin/installazione", model:[ottiche:ottiche])
				return
			} else {
				// se ho una sola amministrazione, la seleziono:
				if (codiciAmm.size() > 1) {
					// se ho più di una amministrazione, ripropongo la scelta all'utente:
					def amministrazioni = So4Amministrazione.createCriteria().list {
						'in'("codice", codiciAmm)
						fetchMode("soggetto", FetchMode.JOIN)
					}.collect { new Amministrazione(codice: it.codice, descrizione:it.soggetto.cognome) }
					
					render (view: "sceltaEnte", model: [amministrazioni: amministrazioni, ottica:codiceOtt, amministrazione: codiceAmm, labelBottone: labelBottone])
					return
				}

				// seleziono l'unica amministrazione
				codiceAmm = codiciAmm[0]
			}
		}

		// se non ho l'ottica, la seleziono dalle impostazioni.
		if (codiceOtt == null) {
			codiceOtt = Impostazione.getImpostazione(Impostazioni.OTTICA_SO4.toString(), codiceAmm).get()?.valore

			// se non trovo l'ottica configurata, segnalo il problema.
			if (codiceOtt == null || codiceOtt.length() == 0) {
				flash.message = "ERRORE: NON HO L'OTTICA DI DEFAULT CONFIGURATA, BISOGNA AGGIUNGERE IL CAMPO OTTICA_SO4 ALLE IMPOSTAZIONI PER L'ENTE ${codiceAmm}"
				render (view: "sceltaEnte")
				return
			}
		}

		// se non ho amministrazioni per l'utente corrente, le carico:
		if (!(springSecurityService.principal.amministrazioni?.size() > 0)) {
			So4Ottica ottica = So4Ottica.createCriteria().get {
				amministrazione {
					'in' ("codice", codiceAmm)
				}

				eq ("codice", codiceOtt)

				fetchMode ("amministrazione", 			FetchMode.JOIN)
				fetchMode ("amministrazione.soggetto", 	FetchMode.JOIN)
			}

			Amministrazione amministrazione = new Amministrazione (codice: 		ottica.amministrazione.codice
																 , descrizione: ottica.amministrazione.soggetto.cognome
																 , ottiche:[new Ottica ( codice: 			ottica.codice
																	 				   , descrizione: 		ottica.descrizione
																					   , unitaOrganizzative:[])])

			springSecurityService.principal.amministrazioni = [amministrazione]

		}

		// infine, ho tutti i parametri che mi servivano:
		springSecurityService.principal.setAmministrazioneOtticaCorrente (codiceAmm, codiceOtt)

		// se ho impostato il cookie grails_remember_me, significa che devo registrare anche i valori di codice amm e ottica.
		Cookie grails_remember_me = request.cookies.find { it.name == SpringSecurityUtils.securityConfig.rememberMe.cookieName }
		if (grails_remember_me != null) {
			Cookie multi_ente_amm = new Cookie("multi_ente_amm", codiceAmm)
			multi_ente_amm.maxAge = SpringSecurityUtils.securityConfig.rememberMe.tokenValiditySeconds
			multi_ente_amm.path   = request.contextPath

			Cookie multi_ente_ott = new Cookie("multi_ente_ott", codiceOtt)
			multi_ente_ott.maxAge = SpringSecurityUtils.securityConfig.rememberMe.tokenValiditySeconds
			multi_ente_ott.path   = request.contextPath

			response.addCookie(multi_ente_amm)
			response.addCookie(multi_ente_ott)
		}

		if (session['TARGET_ACTION_VISUALIZZATORE']?.length() > 0) {
			redirect (uri:session['TARGET_ACTION_VISUALIZZATORE'])
		} else {
			redirect(uri: session['TARGET_URL'])
		}
	}
}

