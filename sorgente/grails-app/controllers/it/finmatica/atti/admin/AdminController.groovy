package it.finmatica.atti.admin

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.IntegrazioneAlbo
import it.finmatica.atti.dizionari.StatoIterService
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.DocumentoService
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.impostazioni.Impostazione
import it.finmatica.atti.impostazioni.ImpostazioneService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.ImpostazioniMap
import it.finmatica.atti.integrazioni.AlboJMessi
import it.finmatica.atti.integrazioni.CasaDiVetroService
import it.finmatica.atti.integrazioni.l190.CasaDiVetroConfig
import it.finmatica.atti.integrazioni.pitre.PiTreTemplate
import it.finmatica.atti.integrazioni.protocollo.ProtocolloPiTre
import it.finmatica.atti.integrazioniws.ads.jworkflow.ExternalTaskServiceResponse
import it.finmatica.atti.integrazioniws.pitre.classes.Template
import it.finmatica.atti.jobs.AttiJob
import it.finmatica.atti.jobs.AttiJobExecutor
import it.finmatica.atti.mail.Mail
import it.finmatica.atti.utility.InvioMail
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
	AttiJobExecutor			attiJobExecutor
	StatoIterService		statoIterService
	DocumentoService		documentoService
	IntegrazioneAlbo integrazioneAlbo

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

	public def testInvioMailErrori(){
		int totale = Delibera.count()
		if (totale == 0) {
			render text: "Nessuna Delibera presente in archivio"
			return
		}
		int offset = new Random().nextInt(totale)
		Delibera d = Delibera.createCriteria().get {
			maxResults(1)
			firstResult(offset)
			order("id", "asc")
		}
		integrazioneAlbo.pubblicaAtto(d)


		/*String testo="TESTO MAIL"
		String oggetto="OGGETTO MAIL"
		String errorString="CHE ERRORACCI BRUTTI CHE CI SONO"
		InvioMail.inviaMailErrori(testo,oggetto,null,errorString)*/
		render view:"testInvioMailErrori"
	}

	public def trascoDelibere() {
		def listaAtti = []
		listaAtti = Delibera.createCriteria().list() {
			//createAlias('soggetti', 'ds', CriteriaSpecification.INNER_JOIN)
			eq("valido", true)
			eq("annoDelibera",2025)
			isNotNull("numeroDelibera")
			isNull("annoAlbo")
			isNull("numeroAlbo")
			isNotNull("dataEsecutivita")
			eq("stato", StatoDocumento.ESECUTIVO)
			isNotNull("iter")
			eq("id",Long.valueOf(16334))
			order("dataEsecutivita", 	 'asc')
		}
		int conta=1;
		for(Delibera delibera : listaAtti){
			documentoService.attivaPubblicazione(delibera)
			//integrazioneAlbo.trascoAtto(delibera,conta)
			conta++;
		}
		String a=""
		render view:"aggiornamento"
	}


	def trascoDetermine(){
		def listaAtti = []
		/*Albo.createCriteria().count(){
			eq(a)
		}*/
		listaAtti = Determina.createCriteria().list() {
			//createAlias('soggetti', 'ds', CriteriaSpecification.INNER_JOIN)
			eq("valido", true)
			eq("annoDetermina",2021)
			isNotNull("numeroDetermina")
			isNull("annoAlbo")
			isNull("numeroAlbo")
			isNotNull("dataEsecutivita")
			eq("stato", StatoDocumento.ESECUTIVO)
			isNull("iter")
			//eq("id",Long.valueOf(1404346))
			order("dataEsecutivita", 	 'desc')
		}
		int conta=1117;
		for(Determina determina : listaAtti){
			integrazioneAlbo.trascoAtto(determina,conta)
			conta--;
		}
		String a=""
		render view:"aggiornamento"
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


	public def testAlex() {

		//Template temp = ProtocolloPiTre.elaboraTemplate(Determina.get(804007),"334")
		String a="";
		/*ProtocolloPiTre pippo = new ProtocolloPiTre()
		pippo.testAlex()*/
	}
//	public def lanciaSmartPnd() {
//		SmartPnd smartPndService = new SmartPnd()
//		/*
//		questo è la prova di creaComunicazione
//
//		faccio la creacomunicazione...prendo l'id e faccio l'upload
//
//
//		String json="{\"inSospeso\":\"S\",\"applicativo\":\"MANNELLA\",\"idRifApplicativo\":\"788asd\",\"codEnte\":\"XXXX\",\"cfiscPiva\":\"MNNLGS73T18C351H\",\"cognomeRgs\":\"MANNELLA\",\"nome\":\"LUCA GIUSEPPE\",\"data\":\"04/04/2023\",\"tipoPratica\":\"ANA-CID\",\"dataPratica\":\"04/04/2023\",\"annoPratica\":\"2023\",\"numeroPratica\":\"7\",\"labelPratica\":\"Accertamento IMU\",\"codiceIUV\":\"7dasdas\",\"cfiscPivaCeditore\":\"lkjdklasjdk33\",\"oggetto\":\"Accertamento IMU\",\"tipoComunicazione\":\"PROVA\",\"tipoComunicazioneDescr\":\"Accertamento IMU\",\"daProtocollare\":\"N\",\"daFirmare\":\"NF\",\"daTimbrare\":\"N\",\"classCod\":\"I_07_01\",\"fascicoloAnno\":\"2023\",\"fascicoloNumero\":\"1\",\"utenteAd4\":\"DEPAG\",\"exportFS\":\"Y\",\"dettagliExtra\":[{\"campo\":\"n° iscrizione albo\",\"valore\":\"55\",\"url\":\"N\"},{\"campo\":\"Url verso servizio\",\"valore\":\"http://www.servizio.it\",\"url\":\"S\"}],\"allegati\":[{\"descrizione\":\"Primo allegato url\",\"url\":\"http://porova.it\",\"escludiDaSpedizione\":\"Y\"},{\"descrizione\":\"Secondo allegato\",\"filename\":\"secondo.txt\",\"firmare\":\"N\"},{\"descrizione\":\"Terzo allegato da firmare\",\"filename\":\"terzo_da_firmare.txt\",\"firmare\":\"Y\"},{\"descrizione\":\"Quarto allegato da non firmare\",\"filename\":\"Quarto_da_non_firmare.txt\",\"firmare\":\"N\"}],\"invioPND\":[{\"oggetto\":\"Scadenza Carta identità\",\"notificationFeePolicy\":\"DELIVERY_MODE\",\"physicalCommunicationType\":\"AR_REGISTERED_LETTER\",\"destinatari\":[{\"tipoDestinatario\":\"PF\",\"cfiscPiva\":\"MNNLGS73T18C351H\",\"cognome\":\"MANNELLA\",\"nome\":\"LUCA GIUSEPPE\",\"email\":\"gmannella@ads.it\",\"indirizzo\":\"Via messina 10\",\"comune\":\"CATANIA\",\"cap\":\"95100\",\"codiceAvviso\":\"ujd9a0\",\"cfiscPivaCreditore\":\"09039403940490\"}]}],\"invioMail\":[{\"mailMittente\":\"m.nanni@ads.it\",\"nominativoMittente\":\"Nanni Marco\",\"oggetto\":\"Saluti\",\"testo\":\"Ciao come stai?\",\"dataSpedizione\":\"01/01/2020 00:00:00\",\"dataAccettazione\":\"01/01/2021 00:00:00\",\"dataNonAccettazione\":\"\",\"destinatari\":[{\"mail\":\"g.mannella@ads.it\",\"cognomeRgs\":\"Mannella\",\"nome\":\"Luca Giuseppe\",\"tipo\":\"TO\"},{\"mail\":\"m.bonforte@ads.it\",\"cognomeRgs\":\"Bonforte Marco SRL\"}]}]}"
//		Comunicazione comunicazione =new ObjectMapper().readValue(json, Comunicazione.class)
//		try {
//			GenericReponse resp = smartPndService.creaComunicazione(comunicazione, "http://svi-ag-fin-as3:18110/smartpnd/api/v1", "SMARTPND", "SMARTPND")
//			println resp?.idComunicazione
//		}catch (Exception ex){
//			println ex.message
//		}*/
//		try {
//			ComunicazioneResponse comunicazioneResp = smartPndService.getComunicazione(12, "http://svi-ag-fin-as3:18110/smartpnd/api/v1", "SMARTPND", "SMARTPND")
//			if (comunicazioneResp?.nome != null) {
//				String a = "M"
//			}
//
//			File test = new File("C:\\BCK\\FILE\\TestFile.pdf")
//			InputStream inputStream= new FileInputStream(test)
//			byte[] sourceBytes = IOUtils.toByteArray(inputStream);
//			String encodedString = Base64.getEncoder().encodeToString(sourceBytes);
//
//			GenericResponse response = smartPndService.uploadFileComunicazione(12, "04_inquinamento_atmosferico.pdf", encodedString, "http://svi-ag-fin-as3:18110/smartpnd/api/v1", "SMARTPND", "SMARTPND")
//			if (response.status.equals(GenericResponse.OK)) {
//				String a = ";"
//			}
//		}catch(Exception ex){
//			println ex.message
//		}
//
//	}

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

	public def aggiornaStatiIter () {
		statoIterService.aggiornaStatiIter()
		flash.message = "Aggiornamento stati iter terminato."
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
