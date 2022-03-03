package it.finmatica.atti.admin

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.impostazioni.Impostazione
import it.finmatica.atti.impostazioni.ImpostazioneService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.ImpostazioniMap
import it.finmatica.atti.integrazioni.CasaDiVetroService
import it.finmatica.atti.integrazioni.l190.CasaDiVetroConfig
import it.finmatica.atti.integrazioniws.ads.jworkflow.ExternalTaskServiceResponse
import it.finmatica.atti.jobs.AttiJob
import it.finmatica.atti.mail.Mail
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAzione
import it.finmatica.so4.struttura.So4Ottica

import org.hibernate.FetchMode

class AdminController {

	public static final String DIRECTORY_CONFIGURAZIONE_STANDARD = "configurazioneStandard"

	ImpostazioniMap			impostazioniMap
	AdminService 			adminService
	ImpostazioneService 	impostazioneService
	SpringSecurityService 	springSecurityService
	it.finmatica.atti.integrazioniws.ads.jworkflow.JWorklistService jworklistServiceClient
	CasaDiVetroService		casaDiVetroService
	AggiornamentoService	aggiornamentoService
	AttiJob					attiJob
	CasaDiVetroConfig 		casaDiVetroConfig

	public def index () {
		if (params.installazione == "1") {
			forward(action:"installazioneEnte", params:params)
			return;
		}

		if (Impostazione.getImpostazione (Impostazioni.ENTI_SO4.toString(), ImpostazioniMap.ENTE_FALLBACK).get() == null) {
			forward (action:'installazione', params:params)
		} else {
			render view: 'aggiornamento'
		}
	}

//	public def aggiornaSfera () {
//		adminService.aggiornaSfera();
//
//		render "Aggiornamento iniziato."
//	}

	public def checkForUpdates () {
		if (grailsApplication.metadata['pending.updates']) {
			flash.message = "Ci sono aggiornamenti installati che diverranno dispoinibili al prossimo riavvio del server.";
			render view:"aggiornamento"
		}

		def updates = aggiornamentoService.getAvailableUpdates();
		if (updates?.size() > 0) {
			flash.message = "Ci sono ${updates.size()} aggiornamenti disponibili."
			render view:"aggiornamento", model:[aggiornamentiDisponibili: true]
		} else {
			flash.message = "Non ci sono aggiornamenti disponibili."
			render view:"aggiornamento"
		}
	}

	public def runUpdates () {
		aggiornamentoService.runUpdates();
		flash.message = "Aggiornamenti Installati con successo. Diventeranno operativi al prossimo riavvio.";
		render view:"aggiornamento", model:[aggiornamentiDisponibili: false]
	}

	public def runUpdate () {
		def f = request.getFile('patch')
	    if (f.empty) {
	        flash.message = 'Il file di patch è vuoto!';
	        render view:"aggiornamento"
	        return
	    }
		File tempDir = new File("temp");
		if (!tempDir.exists()) {
			tempDir.mkdir();
		}
		File updateFile = new File(tempDir, 'update.zip');
	    f.transferTo(updateFile);

		aggiornamentoService.runUpdate(updateFile.toURI().toString().replaceAll("file:", "file://"), "oneshot");

		flash.message = "Aggiornamento installato con successo. Diventerà operativo al prossimo riavvio.";
		render view:"aggiornamento", model:[aggiornamentiDisponibili: false]
	}

	public def aggiornamento () {
		render view: 'aggiornamento', model:[azioniVecchie:adminService.getAzioniVecchie()]
	}

	public def installazione () {
		def ottiche = So4Ottica.createCriteria().list {
			amministrazione {
				eq "ente", true
			}
			fetchMode("amministrazione", FetchMode.JOIN)
		}

		render view: 'installazione', model: [ottiche: ottiche]
	}

	public def aggiornaAtti () {
		aggiornamentoService.update(session.servletContext.getRealPath("WEB-INF/configurazioneStandard/modelliTesto/xml"));
		flash.message = "Impostazioni Aggiornate."
		if (springSecurityService.isLoggedIn()) {
			render (view: "aggiornamento")
		} else {
			render flash.message
		}
	}

	public def aggiornaImpostazioni () {
		impostazioneService.aggiornaImpostazioni();
		flash.message = "Impostazioni Aggiornate."
		if (springSecurityService.isLoggedIn()) {
			render (view: "aggiornamento")
		} else {
			render flash.message
		}
	}

	public def installazioneEnte () {
		try {
			So4Ottica ottica = adminService.installazioneEnte(params.ottica, session.servletContext.getRealPath ("WEB-INF/configurazioneStandard"));
			flash.message = "Impostazioni installate per l'ente ${ottica.amministrazione.soggetto.denominazione} - ${ottica.descrizione}"
		} catch (Throwable t) {
			impostazioniMap.clear();
			throw t;
		}
		render (view: "aggiornamento")
	}

	public def cercaAzioniNuove () {
		def azioniNuove = [[nome: "-- svuota azione --", id:-1]]
		azioniNuove.addAll(WkfAzione.createCriteria().list {
			eq ("valido", true)
			or {
				ilike ("nome", "%${params.filtroAzioniNuove}%")
				ilike ("descrizione", "%${params.filtroAzioniNuove}%")
			}

			order ("tipoOggetto.codice", "asc")
			order ("nomeBean", "asc")
			order ("nomeMetodo", "asc")
		}.collect {
			[nome: "${it.tipoOggetto.codice} | ${it.nomeBean}.${it.nomeMetodo}() >> ${it.nome}: ${it.descrizione}", id:it.id]
		})
		render view: 'aggiornamento', model: [azioniVecchie: adminService.getAzioniVecchie(), azioniNuove: azioniNuove, filtroAzioniNuove: params.filtroAzioniNuove]
	}

	public def sostituisciVecchioConNuovo () {
		aggiornamentoService.sostituisciVecchieAzioniConNuove(params);
		render view: 'aggiornamento', model: [azioniVecchie: adminService.getAzioniVecchie(), filtroAzioniNuove: params.filtroAzioniNuove]
	}

	public def eliminaAzioni () {
		aggiornamentoService.eliminaAzioni();
		render view: 'aggiornamento', model: [azioniVecchie: adminService.getAzioniVecchie()]
	}

	public def aggiornaRegoleCalcolo () {
		aggiornamentoService.aggiornaRegoleCalcolo();

		flash.message = "Regole di calcolo aggiornate"
		render view: 'aggiornamento'
	}

	public def inizializzaDizionari () {
		adminService.inizializzaDizionari();

		flash.message = "Dizionari popolati"
		render view: 'aggiornamento'
	}

	public def aggiornaAzioni () {
		aggiornamentoService.aggiornaAzioni()
		flash.message = "Azioni Aggiornate"
		forward (action:"aggiornamento")
	}

	public def aggiornaTipiModelloTesto () {
		aggiornamentoService.aggiornaTipiModelloTesto(session.servletContext.getRealPath("WEB-INF/configurazioneStandard/modelliTesto/xml"))
		flash.message = "Tipi Modelli Testo Standard importati."
		forward (action:"aggiornamento")
	}

	public def installaConfigurazioniIter () {
		adminService.installaConfigurazioniIter(session.servletContext.getRealPath("WEB-INF/configurazioneStandard/flussi"))
		flash.message = "Flussi Standard importati."
		forward (action:"aggiornamento")
	}

	public def attivaJob () {
		attiJob.job ()
		flash.message = "Job Attivato."
		forward (action:"aggiornamento")
	}

	public def attivaJobConservazioneAutomatica () {
		attiJob.jobConservazioneAutomatica ()
		flash.message = "Job Conservazione Automatica Attivato."
		forward (action:"aggiornamento")
	}

    public def attivaJobAggiornaConservazione () {
        attiJob.jobAggiornaConservazione ()
        flash.message = "Job Aggiorna Stati Conservazione Attivato."
        forward (action:"aggiornamento")
    }

    public def attivaJobNotifiche () {
        attiJob.jobInviaNotiche ()
        flash.message = "Job Invia Notifiche Attivato."
        forward (action:"aggiornamento")
    }

    /* FUNZIONI PER IL TEST APPLICATIVO */
	public def testIntegrazioneJWorklist () {
		ExternalTaskServiceResponse resp = jworklistServiceClient.deleteExternalTask("", "", "", "");

		if (resp != null) {
			flash.message = "TUTTO OK";
		}

		forward (action:"aggiornamento")
	}

	public def testInvioEmail () {
		def e = Mail.invia(Impostazioni.ALIAS_INVIO_MAIL.valore, Impostazioni.MITTENTE_INVIO_MAIL.valore, [params.email], "TEST INVIO EMAIL", "TEST INVIO EMAIL", null)
		if (e == null) {
			flash.message = "Email inviata con successo"
		} else {
			throw e;
		}
		forward (action:"aggiornamento")
	}

	public def testInvioEmailCert () {
		def e = Mail.invia(Impostazioni.ALIAS_INVIO_MAIL_CERT.valore, Impostazioni.MITTENTE_INVIO_MAIL_CERT.valore, [params.email], "TEST INVIO EMAIL", "TEST INVIO EMAIL", null)
		if (e == null) {
			flash.message = "Email inviata con successo"
		} else {
			throw e;
		}
		forward (action:"aggiornamento")
	}

	public def testCasaDiVetro () {
		String token = casaDiVetroService.login (casaDiVetroConfig.getUtenteWebService());
		if (token != null) {
			flash.message = "TUTTO OK";
		}
		forward (action:"aggiornamento")
	}
}
