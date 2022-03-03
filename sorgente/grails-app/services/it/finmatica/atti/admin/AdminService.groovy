package it.finmatica.atti.admin

import atti.actions.commons.CampiDocumentoAction
import grails.plugin.springsecurity.SpringSecurityService
import groovy.sql.Sql
import it.finmatica.ad4.autenticazione.Ad4Ruolo
import it.finmatica.atti.dizionari.*
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.documenti.tipologie.*
import it.finmatica.atti.impostazioni.*
import it.finmatica.atti.odg.Commissione
import it.finmatica.atti.odg.dizionari.*
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.impostazioni.WkfImpostazione
import it.finmatica.gestioneiter.serializer.WkfCfgIterXMLSerializer
import it.finmatica.gestionetesti.reporter.GestioneTestiModello
import it.finmatica.gestionetesti.reporter.GestioneTestiTipoModello
import it.finmatica.so4.login.detail.Amministrazione
import it.finmatica.so4.login.detail.Ottica
import it.finmatica.so4.struttura.So4Ottica
import org.codehaus.groovy.grails.commons.GrailsApplication

import javax.sql.DataSource

class AdminService {

    GrailsApplication grailsApplication

	AttiGestoreCompetenze   gestoreCompetenze
	RegistroService         registroService
	SpringSecurityService   springSecurityService
	ImpostazioneService     impostazioneService
	CampiDocumentoAction    campiDocumentoAction
	WkfCfgIterXMLSerializer wkfCfgIterXMLSerializer
	NotificheService        notificheService
	AggiornamentoService    aggiornamentoService

	DataSource dataSource

    public So4Ottica installazioneEnte (String codiceOttica, String pathConfigurazioneStandard) {
		// Metto findByCodice perché da alcuni clienti il codice dell'ottica Istituzionale è 1
		// e groovy/grails si incartano cercando di trasformarlo in number (riuscendoci) e poi da errore.
		So4Ottica ottica = So4Ottica.findByCodice(codiceOttica);
		impostazioneService.installaImpostazioniEnte(ottica.amministrazione.codice, ottica.codice);

		// se non ho amministrazioni per l'utente corrente, le carico:
		Amministrazione amministrazione = new Amministrazione(codice: 	ottica.amministrazione.codice
																 , descrizione: ottica.amministrazione.soggetto.cognome
																 , ottiche:[new Ottica ( codice: 			ottica.codice
																						, descrizione: 		ottica.descrizione
																					    , unitaOrganizzative:[])])

		springSecurityService.principal.amministrazioni = [amministrazione]

		// infine, ho tutti i parametri che mi servivano:
		springSecurityService.principal.setAmministrazioneOtticaCorrente (amministrazione.codice, ottica.codice)

		// aggiorno le impostazioni di gestione iter
		// FIXME: per ora faccio la cosa più becera. Poi andrà migliorato. (e ripensate le impostazioni del configuratore iter)
		if (WkfImpostazione.findAllByEnte(ottica.amministrazione.codice).size() == 0) {
			WkfImpostazione entiSo4 = WkfImpostazione.findByCodiceAndEnte("ENTI_SO4", "*");
			if (entiSo4 == null) {
				new WkfImpostazione(ente: "*"
								  , codice:"ENTI_SO4"
								  , valore:ottica.amministrazione.codice
								  , predefinito: "FINMATICA"
								  , descrizione: "Elenco enti (separati da #) gestiti dall'applicativo per installazione"
								  , etichetta:"ENTI SO4").save();
			} else {
				entiSo4.valore += Impostazioni.SEPARATORE+ottica.amministrazione.codice;
				entiSo4.save()
			}

			new WkfImpostazione(ente: ottica.amministrazione.codice
				, codice:"OTTICA_SO4"
				, valore:ottica.codice
				, predefinito: "IST"
				, descrizione: "Ottica di SO4 utilizzata per l'amministrazione"
				, etichetta:"OTTICA SO4").save();

			new WkfImpostazione(ente: ottica.amministrazione.codice
				, codice:"PREFISSO_RUOLO_AD4"
				, valore:"AGD"
				, predefinito: "AGD"
				, descrizione: "Prefisso per il filtro dei ruoli"
				, etichetta:"PREFISSO RUOLI AD4").save();
		}

		// installo le configurazioni per l'iter:

		installaConfigurazioniIter(pathConfigurazioneStandard+"/flussi");
		aggiornamentoService.aggiornaAzioni();

		aggiornamentoService.aggiornaTipiModelloTesto(pathConfigurazioneStandard+"/modelliTesto/xml")
		installaModelliTesto(pathConfigurazioneStandard+"/modelliTesto/odt")

		inizializzaDizionari();

		return ottica;
	}

	public def getAzioniVecchie () {
		Sql sql = new Sql (dataSource);
		def azioniVecchie = [];
		sql.eachRow("""select distinct a.id_azione as id_azione, a.tipo_oggetto, a.nome as nome, a.descrizione, a.categoria, a.nome_bean, a.nome_metodo, pa.id_pulsante, sai.id_cfg_step step_azione_in, s.id_cfg_step step_condizione
  from wkf_diz_azioni a
     , wkf_diz_pulsanti_azioni pa
     , wkf_cfg_step_azioni_in sai
     , wkf_cfg_step s
     , wkf_diz_pulsanti p
     , wkf_diz_attori att
 where a.valido = 'N'
   and pa.id_azione(+) = a.id_azione
   and sai.id_azione_in(+) = a.id_azione
   and s.id_azione_condizione(+) = a.id_azione
   and p.id_condizione_visibilita(+) = a.id_azione
   and att.id_azione_calcolo(+) = a.id_azione
   and (sai.id_cfg_step is not null
   or pa.id_pulsante is not null
   or s.id_cfg_step is not null
   or p.id_condizione_visibilita is not null
   or att.id_azione_calcolo is not null)""") { row ->
			azioniVecchie << [nome: "${row.tipo_oggetto} | ${row.nome_bean}.${row.nome_metodo}() >> "+row.nome+": "+row.descrizione, id: row.id_azione]
		}

		return azioniVecchie;
	}

	public def inizializzaDizionari () {
		log.debug ("installo i dizionari")

		if (TipoRegistro.list().size() == 0) {
			log.debug("Installo i Tipi Registro");

			new TipoRegistro (codice: "PROP", 	descrizione: "Registro per le Proposte", 	automatico: true, valido: true).save()
			new TipoRegistro (codice: "DETE", 	descrizione: "Registro per le Determine", 	automatico: true, valido: true).save()
			new TipoRegistro (codice: "DELG", 	descrizione: "Registro per le Delibere di Giunta", 		automatico: true, valido: true).save()
			new TipoRegistro (codice: "DELC", 	descrizione: "Registro per le Delibere di Consiglio", 	automatico: true, valido: true).save()
			new TipoRegistro (codice: "SEDUG", 	descrizione: "Registro per le Sedute di Giunta", 		automatico: true, valido: true).save()
			new TipoRegistro (codice: "SEDUC", 	descrizione: "Registro per le Sedute di Consiglio", 	automatico: true, valido: true).save()

			["DETE", "PROP", "DELC", "DELG", "SEDUG", "SEDUC"].each {
				registroService.rinnovaRegistro(TipoRegistro.get(it), Calendar.getInstance().get(Calendar.YEAR))
			}
		}

		if (EsitoStandard.list().size() == 0) {
			log.debug("Installo gli Esiti Standard");

			new EsitoStandard (codice: EsitoStandard.ADOTTATO, 		    titolo: "Adottato", 		 	creaDelibera: true,  prossimaSeduta: false, determina: false).save()
			new EsitoStandard (codice: EsitoStandard.RINVIO_UFFICIO, 	titolo: "Rinvio all'Ufficio",  	creaDelibera: false, prossimaSeduta: false, determina: true).save()
			new EsitoStandard (codice: EsitoStandard.PARZIALE, 			titolo: "Parziale", 			creaDelibera: false, prossimaSeduta: true,  determina: false).save()
			new EsitoStandard (codice: EsitoStandard.NON_ADOTTATO, 		titolo: "Non adottato", 		creaDelibera: false, prossimaSeduta: false, determina: false).save()
			new EsitoStandard (codice: EsitoStandard.INVIA_COMMISSIONE, titolo: "Invia a Commissione", 	creaDelibera: false, prossimaSeduta: false, determina: false).save()
			new EsitoStandard (codice: EsitoStandard.CONCLUSO, 			titolo: "Concluso", 			creaDelibera: false, prossimaSeduta: false, determina: false).save()
		}

		log.debug("Installo i Tipi Soggetto");

		TipoSoggetto.findOrSaveWhere (codice: TipoSoggetto.REDATTORE, 			categoria: TipoSoggetto.CATEGORIA_COMPONENTE, 	titolo: "Redattore")
		TipoSoggetto.findOrSaveWhere (codice: TipoSoggetto.FUNZIONARIO, 		categoria: TipoSoggetto.CATEGORIA_COMPONENTE, 	titolo: "Funzionario")
		TipoSoggetto.findOrSaveWhere (codice: TipoSoggetto.DIRIGENTE, 			categoria: TipoSoggetto.CATEGORIA_COMPONENTE, 	titolo: "Dirigente")
		TipoSoggetto.findOrSaveWhere (codice: TipoSoggetto.FIRMATARIO, 			categoria: TipoSoggetto.CATEGORIA_COMPONENTE,  	titolo: "Firmatario")
		TipoSoggetto.findOrSaveWhere (codice: TipoSoggetto.UO_PROPONENTE, 		categoria: TipoSoggetto.CATEGORIA_UNITA, 		titolo: "Uo Proponente")
        TipoSoggetto.findOrSaveWhere (codice: TipoSoggetto.UO_DESTINATARIA, categoria: TipoSoggetto.CATEGORIA_UNITA, titolo: "Uo Destinataria")
		TipoSoggetto.findOrSaveWhere (codice: TipoSoggetto.PRESIDENTE, 			categoria: TipoSoggetto.CATEGORIA_COMPONENTE,	titolo: "Presidente")
		TipoSoggetto.findOrSaveWhere (codice: TipoSoggetto.SEGRETARIO,			categoria: TipoSoggetto.CATEGORIA_COMPONENTE,	titolo: "Segretario")
		TipoSoggetto.findOrSaveWhere (codice: TipoSoggetto.INCARICATO,			categoria: TipoSoggetto.CATEGORIA_COMPONENTE,	titolo: "Incaricato")

		TipoSoggetto.findOrSaveWhere (codice: TipoSoggetto.UO_FIRMATARIO,		categoria: TipoSoggetto.CATEGORIA_UNITA,		titolo: "Unità del Firmatario")
		TipoSoggetto.findOrSaveWhere (codice: TipoSoggetto.UO_CONTROLLO,		categoria: TipoSoggetto.CATEGORIA_UNITA,		titolo: "Unità di Controllo")

		// soggetti per le ASL
		TipoSoggetto.findOrSaveWhere (codice: TipoSoggetto.DIRETTORE_AMMINISTRATIVO, categoria: TipoSoggetto.CATEGORIA_COMPONENTE,	titolo: "Direttore Amministrativo")
		TipoSoggetto.findOrSaveWhere (codice: TipoSoggetto.DIRETTORE_GENERALE, 		 categoria: TipoSoggetto.CATEGORIA_COMPONENTE,	titolo: "Direttore Generale")
        TipoSoggetto.findOrSaveWhere (codice: TipoSoggetto.DIRETTORE_SANITARIO, 	 categoria: TipoSoggetto.CATEGORIA_COMPONENTE,	titolo: "Direttore Sanitario")
        TipoSoggetto.findOrSaveWhere (codice: TipoSoggetto.DIRETTORE_SOCIO_SANITARIO,categoria: TipoSoggetto.CATEGORIA_COMPONENTE,	titolo: "Direttore Socio Sanitario")

		if (TipoSeduta.list().size() == 0) {
			log.debug("Installo i Tipi Seduta");

			new TipoSeduta(titolo: 'ORDINARIA', 	sequenza: 1).save()
			new TipoSeduta(titolo: 'STRAORDINARIA', sequenza: 2).save()
		}

		if (Voto.list().size() == 0) {
			log.debug("Installo i Tipi di Voto");

			new Voto(valore: Voto.VOTO_FAVOREVOLE, codice: Voto.VOTO_FAVOREVOLE, sequenza: 1).save()
			new Voto(valore: Voto.VOTO_CONTRARIO,  codice: Voto.VOTO_CONTRARIO,  sequenza: 2).save()
			new Voto(valore: Voto.VOTO_ASTENUTO,   codice: Voto.VOTO_ASTENUTO, 	 sequenza: 3).save()
		}

		if (RuoloPartecipante.list().size() == 0) {
			log.debug("Installo i Ruoli dei Partecipanti");

			new RuoloPartecipante(codice: RuoloPartecipante.CODICE_PRESIDENTE,  descrizione: 'PRESIDENTE').save()
			new RuoloPartecipante(codice: RuoloPartecipante.CODICE_SEGRETARIO,  descrizione: 'SEGRETARIO').save()
			new RuoloPartecipante(codice: RuoloPartecipante.CODICE_SCRUTATORE,  descrizione: 'SCRUTATORE').save()
			new RuoloPartecipante(codice: RuoloPartecipante.CODICE_INVITATO, 	descrizione: 'INVITATO'  ).save()
			new RuoloPartecipante(codice: RuoloPartecipante.CODICE_DIRETTORE_SANITARIO, 		descrizione: "DIRETTORE SANITARIO").save()
			new RuoloPartecipante(codice: RuoloPartecipante.CODICE_DIRETTORE_AMMINISTRATIVO, 	descrizione: "DIRETTORE AMMINISTRATIVO").save()
            new RuoloPartecipante(codice: RuoloPartecipante.CODICE_DIRETTORE_GENERALE, 			descrizione: "DIRETTORE GENERALE").save()
            new RuoloPartecipante(codice: RuoloPartecipante.CODICE_DIRETTORE_SOCIO_SANITARIO, 	descrizione: "DIRETTORE SOCIO SANITARIO").save()
			
//			new RuoloPartecipante(codice: RuoloPartecipante.CODICE_VICE_SEGRETARIO, descrizione: 'VICE SEGRETARIO').save()
//			new RuoloPartecipante(codice: RuoloPartecipante.CODICE_VICE_PRESIDENTE, descrizione: 'VICE PRESIDENTE').save()
		}

		if (Commissione.list().size() == 0) {
			log.debug("Installo le Commissioni");

			new Commissione ( titolo:				"CONSIGLIO COMUNALE"
							, descrizione:			"Commissione per il Consiglio Comunale"
							, tipoRegistroSeduta:	TipoRegistro.get("SEDUC")
							, tipoRegistro:			TipoRegistro.get("DELC")
							, progressivoCfgIter:	WkfCfgIter.findByNome("STANDARD: DELIBERA").progressivo
							, ruoloCompetenze:		Ad4Ruolo.get(Impostazioni.RUOLO_SO4_ODG.valore)
							, ruoloVisualizza:		null
							, secondaConvocazione:	true
							, pubblicaWeb:			true
							, ruoliObbligatori:		true
							, votoPresidente:		true
							, controlloRegolarita:  true).save();

			new Commissione ( titolo:				"GIUNTA COMUNALE"
							, descrizione:			"Commissione per la Giunta Comunale"
							, tipoRegistroSeduta:	TipoRegistro.get("SEDUG")
							, tipoRegistro:			TipoRegistro.get("DELG")
							, progressivoCfgIter:	WkfCfgIter.findByNome("STANDARD: DELIBERA").progressivo
							, ruoloCompetenze:		Ad4Ruolo.get(Impostazioni.RUOLO_SO4_ODG.valore)
							, ruoloVisualizza:		null
							, secondaConvocazione:	true
							, pubblicaWeb:			true
							, ruoliObbligatori:		true
							, votoPresidente:		true
							, controlloRegolarita:  true).save();
		}

		if (TipoOrganoControllo.list().size() == 0) {
			log.debug("Installo i Tipi di Organo di Controllo");

			new TipoOrganoControllo(codice: "CONSIGLIERI", titolo: "Consiglieri").save()
			new TipoOrganoControllo(codice: "CAPI_GRUPPO", titolo: "Capi Gruppo").save()
		}

		if (Notifica.list().size() == 0) {
			log.debug("Installo le Notifiche");
			
			new Notifica (tipoNotifica:	TipoNotifica.ASSEGNAZIONE
						, oggetto: 		"[TIPOLOGIA] [ESTREMI_DOCUMENTO] - [STATO]"
						, testo:		"[OGGETTO]"
						, titolo:		"Notifica di Cambio Step"
						, modalitaInvio: Notifica.MODALITA_JWORKLIST
						, oggetti: 		"DETERMINA#DELIBERA#PROPOSTA_DELIBERA#VISTO_PARERE#CERTIFICATO"
						, allegati: 	null).addToNotificheEmail(new NotificaEmail(funzione:"ATTORI_STEP_CORRENTE")).save();
					
			new Notifica (tipoNotifica:	TipoNotifica.ESECUTIVITA
		            	  , oggetto:		"L'atto [ESTREMI_DOCUMENTO] è diventato ESECUTIVO"
	            		  , testo:			"Si notifica l'esecutività della [TIPO_ATTO] nr.: [NUMERO] / [ANNO]\nOggetto:[OGGETTO]\nUnita Proponente: [UNITA_PROPONENTE]\n\nQuesta comunicazione è inviata con un sistema di inoltro automatico, si prega di non rispondere a questa mail."
            			  , titolo:			"Esecutività"
			              , modalitaInvio: 	Notifica.MODALITA_JWORKLIST
			              , oggetti: 		"DETERMINA#DELIBERA"
			              , allegati: 		null).addToNotificheEmail(new NotificaEmail(funzione:"ATTORI_FLUSSO_TRANNE_STEP_CORRENTE")).addToNotificheEmail(new NotificaEmail(funzione:"INTERNI")).addToNotificheEmail(new NotificaEmail(funzione:"ESTERNI")).save();
					  
			new Notifica (tipoNotifica:	TipoNotifica.NON_ESECUTIVITA
			              , oggetto:		"L'atto [ESTREMI_DOCUMENTO] NON è diventato ESECUTIVO"
		            	  , testo:			"Si notifica la NON adozione della [TIPO_ATTO] nr.: [NUMERO] / [ANNO]\nOggetto:[OGGETTO]\nUnita Proponente: [UNITA_PROPONENTE]\n\nQuesta comunicazione è inviata con un sistema di inoltro automatico, si prega di non rispondere a questa mail."
	            		  , titolo:			"Non Esecutività"
            			  , modalitaInvio: 	Notifica.MODALITA_JWORKLIST
            			  , oggetti: 		"DETERMINA#DELIBERA#PROPOSTA_DELIBERA"
        				  , allegati: 		null).addToNotificheEmail(new NotificaEmail(funzione:"DIRIGENTE")).addToNotificheEmail(new NotificaEmail(funzione:"ATTORI_FLUSSO")).save();
					  
			new Notifica (tipoNotifica:	TipoNotifica.VERBALIZZAZIONE_PROPOSTA
			              , oggetto:		"La proposta [N_PROPOSTA] / [ANNO_PROPOSTA] è stata discussa nella seduta del [DATA_SEDUTA]"
			           	  , testo:			"La proposta [OGGETTO] ha ottenuto: [ESITO_PROPOSTA]\n\n[NOTE_VERBALIZZAZIONE]"
			          	  , titolo:			"Verbalizzazione Proposta"
			          	  , modalitaInvio: 	Notifica.MODALITA_EMAIL
			          	  , oggetti: 		"DETERMINA#DELIBERA#PROPOSTA_DELIBERA"
						  , allegati: 		"TESTO").addToNotificheEmail(new NotificaEmail(funzione:"UTENTI_VERBALIZZAZIONE_IN_UO_PROPONENTE")).addToNotificheEmail(new NotificaEmail(funzione:"ATTORI_FLUSSO_TRANNE_STEP_CORRENTE")).addToNotificheEmail(new NotificaEmail(funzione:"ATTORI_VISTI")).save()
			
			new Notifica (tipoNotifica:	TipoNotifica.DA_FIRMARE
            			  , oggetto:		"Atto in attesa di sottoscrizione"
            			  , testo:			"Si comunica che è in attesa di sottoscrizione il documento avente all'oggetto: [OGGETTO] redatto dal Servizio [UNITA_PROPONENTE]\n\nQuesta comunicazione è inviata con un sistema di inoltro automatico, si prega di non rispondere a questa mail."
            			  , titolo:			"Notifica Atto da firmare"
						  , modalitaInvio: 	Notifica.MODALITA_EMAIL
            		  	  , oggetti: 		"DETERMINA#DELIBERA#PROPOSTA_DELIBERA"
            			  , allegati: 		"TESTO").addToNotificheEmail(new NotificaEmail(funzione:"ATTORI_STEP_CORRENTE")).save()
		}

		if (Esito.list().size() == 0) {
			log.debug("Installo gli Esiti");

			new Esito (titolo:"ADOTTATO", 			descrizione:"", esitoStandard:EsitoStandard.get(EsitoStandard.ADOTTATO), 	 	gestioneEsecutivita:true , notificaVerbalizzazione:true, sequenza:0).save();
			new Esito (titolo:"PARERE FAVOREVOLE", 	descrizione:"", esitoStandard:EsitoStandard.get(EsitoStandard.NON_ADOTTATO), 	gestioneEsecutivita:false, notificaVerbalizzazione:true, sequenza:1).save();
			new Esito (titolo:"RINVIATO", 			descrizione:"", esitoStandard:EsitoStandard.get(EsitoStandard.PARZIALE), 		gestioneEsecutivita:false, notificaVerbalizzazione:true, sequenza:2).save();
			new Esito (titolo:"RINVIATO A UFFICIO", descrizione:"", esitoStandard:EsitoStandard.get(EsitoStandard.RINVIO_UFFICIO), 	gestioneEsecutivita:false, notificaVerbalizzazione:true, sequenza:3).save();
		}

		if (TipoAllegato.list().size() == 0) {
			log.debug("Installo i Tipi di Allegato");

			new TipoAllegato(titolo:"Verbale", 			 descrizione:"Verbale"			).save();
			new TipoAllegato(titolo:"Vari", 			 descrizione:"Vari"				).save();
			new TipoAllegato(titolo:"Bando di gara", 	 descrizione:"Bando di gara"	).save();
			new TipoAllegato(titolo:"Ordine del giorno", descrizione:"Ordine del giorno").save();
			new TipoAllegato(titolo:"Planimetrie", 		 descrizione:"Planimetrie"		).save();
		}

		installaTipologie();
	}

	private void installaTipologie () {

		aggiornamentoService.aggiornaRegoleCalcolo();

		if (CaratteristicaTipologia.list().size() == 0) {
			log.debug("Installo le Caretteristiche Tipologia");

			CaratteristicaTipologia c = new CaratteristicaTipologia (titolo: "Determina", 	descrizione: "Determina", 	tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 	layoutSoggetti: "/atti/documenti/determina/determina_standard.zul");
			c.addToCaratteristicheTipiSoggetto(createCaratteristicaTipoSoggetto(TipoSoggetto.REDATTORE, 	"getComponentePerUtenteCorrente", "getListaComponentiPerUtenteConRuoloInOttica", null, "AGDRED"));
			c.addToCaratteristicheTipiSoggetto(createCaratteristicaTipoSoggetto(TipoSoggetto.UO_PROPONENTE, "getUnitaSoggettoConRuolo", "getListaUnitaOttica", TipoSoggetto.REDATTORE, "AGDRED"));
			c.addToCaratteristicheTipiSoggetto(createCaratteristicaTipoSoggetto(TipoSoggetto.FUNZIONARIO, 	"getResponsabileConRuoloInUnita", "getListaComponentiConRuoloInOttica", TipoSoggetto.UO_PROPONENTE, "AGDFUNZ"));
			c.addToCaratteristicheTipiSoggetto(createCaratteristicaTipoSoggetto(TipoSoggetto.DIRIGENTE, 	"getResponsabileConRuoloInUnita", "getListaComponentiConRuoloInOttica", TipoSoggetto.UO_PROPONENTE, "AGDFIRMA"));
			c.save();

			c = new CaratteristicaTipologia (titolo: "Ordinanza sindacale", 		descrizione: "Ordinanza del sindaco", 	tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 	layoutSoggetti: "/atti/documenti/determina/determina_standard.zul");
			c.addToCaratteristicheTipiSoggetto(createCaratteristicaTipoSoggetto(TipoSoggetto.REDATTORE, 	"getComponentePerUtenteCorrente", "getListaComponentiPerUtenteConRuoloInOttica", null, "AGDRED"));
			c.addToCaratteristicheTipiSoggetto(createCaratteristicaTipoSoggetto(TipoSoggetto.UO_PROPONENTE, "getUnitaSoggettoConRuolo", "getListaUnitaOttica", TipoSoggetto.REDATTORE, "AGDRED"));
			c.addToCaratteristicheTipiSoggetto(createCaratteristicaTipoSoggetto(TipoSoggetto.FUNZIONARIO, 	"getResponsabileConRuoloInUnita", "getListaComponentiConRuoloInOttica",  TipoSoggetto.UO_PROPONENTE, "AGDFUNZ"));
			c.addToCaratteristicheTipiSoggetto(createCaratteristicaTipoSoggetto(TipoSoggetto.DIRIGENTE, 	"getResponsabileConRuoloInOttica", "getListaComponentiConRuoloInOttica", TipoSoggetto.UO_PROPONENTE, "AGDDECF"));
			c.save();

			c = new CaratteristicaTipologia (titolo: "Proposta Delibera", 	descrizione: "Proposta Delibera", 	tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	layoutSoggetti: "/atti/documenti/propostaDelibera/propostaDelibera_standard.zul").save()
			c.addToCaratteristicheTipiSoggetto(createCaratteristicaTipoSoggetto(TipoSoggetto.REDATTORE, 	"getComponentePerUtenteCorrente", "getListaComponentiPerUtenteConRuoloInOttica", null, "AGDRED"));
			c.addToCaratteristicheTipiSoggetto(createCaratteristicaTipoSoggetto(TipoSoggetto.UO_PROPONENTE, "getUnitaSoggettoConRuolo", "getListaUnitaOttica", TipoSoggetto.REDATTORE, "AGDRED"));
			c.addToCaratteristicheTipiSoggetto(createCaratteristicaTipoSoggetto(TipoSoggetto.FUNZIONARIO, 	"getResponsabileConRuoloInUnita", "getListaComponentiConRuoloInOttica", TipoSoggetto.UO_PROPONENTE, "AGDFUNZ"));
			c.addToCaratteristicheTipiSoggetto(createCaratteristicaTipoSoggetto(TipoSoggetto.DIRIGENTE, 	"getResponsabileConRuoloInUnita", "getListaComponentiConRuoloInOttica", TipoSoggetto.UO_PROPONENTE, "AGDFIRMA"));
			c.save();

			c = new CaratteristicaTipologia (titolo: "Parere", 				descrizione: "Parere", 				tipoOggetto: WkfTipoOggetto.get(VistoParere.TIPO_OGGETTO), 	layoutSoggetti: "--nessuno--").save()
			c.addToCaratteristicheTipiSoggetto(createCaratteristicaTipoSoggetto(TipoSoggetto.UO_DESTINATARIA, 	"getUnitaInTipologia", "getListaUnitaInTipologia", null, null));
			c.addToCaratteristicheTipiSoggetto(createCaratteristicaTipoSoggetto(TipoSoggetto.FIRMATARIO, 		"getComponenteConRuoloInUnita", "getListaComponentiConRuoloInOttica", TipoSoggetto.UO_DESTINATARIA, "AGDVISF"));
			c.save();

			c = new CaratteristicaTipologia (titolo: "Parere Tecnico", 				descrizione: "Parere Tecnico", 				tipoOggetto: WkfTipoOggetto.get(VistoParere.TIPO_OGGETTO), 	layoutSoggetti: "--nessuno--").save()
			c.addToCaratteristicheTipiSoggetto(createCaratteristicaTipoSoggetto(TipoSoggetto.UO_DESTINATARIA, 	"getUnitaProponenteDocumentoPrincipale", "getListaUnitaInTipologiaInRamoUoProponente", null, null));
			c.addToCaratteristicheTipiSoggetto(createCaratteristicaTipoSoggetto(TipoSoggetto.FIRMATARIO, 		"getDirigenteDocumentoPrincipale", "getListaComponentiConRuoloInOttica", TipoSoggetto.UO_DESTINATARIA, "AGDFIRMA"));
			c.save();

			c = new CaratteristicaTipologia (titolo: "Certificato Esecutività", 		descrizione: "Certificato Esecutività", 		tipoOggetto: WkfTipoOggetto.get(Certificato.TIPO_OGGETTO), 	layoutSoggetti: "--nessuno--").save()
			c.addToCaratteristicheTipiSoggetto(createCaratteristicaTipoSoggetto(TipoSoggetto.FIRMATARIO, 	"getComponentePerUtenteCorrente", "getListaComponentiConRuoloInOttica", null, "AGDESECF"));
			c.save();

			c = new CaratteristicaTipologia (titolo: "Certificato", 		descrizione: "Certificato", 		tipoOggetto: WkfTipoOggetto.get(Certificato.TIPO_OGGETTO), 	layoutSoggetti: "--nessuno--").save()
			c.addToCaratteristicheTipiSoggetto(createCaratteristicaTipoSoggetto(TipoSoggetto.FIRMATARIO, 	"getComponentePerUtenteCorrente", "getListaComponentiConRuoloInOttica", null, "AGDCERTF"));
			c.save();

			c = new CaratteristicaTipologia (titolo: "Visto", 				descrizione: "Visto", 				tipoOggetto: WkfTipoOggetto.get(VistoParere.TIPO_OGGETTO), 	layoutSoggetti: "--nessuno--").save()
			c.addToCaratteristicheTipiSoggetto(createCaratteristicaTipoSoggetto(TipoSoggetto.UO_DESTINATARIA, 	"getUnitaInTipologia", "getListaUnitaInTipologia", null, null));
			c.addToCaratteristicheTipiSoggetto(createCaratteristicaTipoSoggetto(TipoSoggetto.FIRMATARIO, 		"getComponenteConRuoloInUnita", "getListaComponentiConRuoloInOttica", TipoSoggetto.UO_DESTINATARIA, "AGDVISF"));
			c.save();
		}

		if (TipoCertificato.list().size() == 0) {
			log.debug("Installo le Tipologie di Certificato");

			TipoCertificato t = new TipoCertificato();
			t.titolo 				= "Certificato di Pubblicazione Determina";
			t.descrizione 			= "Certificato di Pubblicazione Determina";
			t.progressivoCfgIter	= WkfCfgIter.findByNome("STANDARD: CERTIFICATO PUBBLICAZIONE").progressivo;
			t.caratteristicaTipologia = CaratteristicaTipologia.findByTitolo("Certificato");
			t.modelloTesto			= GestioneTestiModello.findByNome("Certificato di Pubblicazione Determina");
			t.save();

			t = new TipoCertificato();
			t.titolo 				= "Certificato di Avvenuta Pubblicazione Determina";
			t.descrizione 			= "Certificato di Avvenuta Pubblicazione Determina";
			t.progressivoCfgIter	= WkfCfgIter.findByNome("STANDARD: CERTIFICATO AVVENUTA PUBBLICAZIONE").progressivo;
			t.caratteristicaTipologia = CaratteristicaTipologia.findByTitolo("Certificato");
			t.modelloTesto			= GestioneTestiModello.findByNome("Certificato di Avvenuta Pubblicazione Determina");
			t.save();

			t = new TipoCertificato();
			t.titolo 				= "Certificato di Pubblicazione Delibera";
			t.descrizione 			= "Certificato di Pubblicazione Delibera";
			t.progressivoCfgIter	= WkfCfgIter.findByNome("STANDARD: CERTIFICATO PUBBLICAZIONE").progressivo;
			t.caratteristicaTipologia = CaratteristicaTipologia.findByTitolo("Certificato");
			t.modelloTesto			= GestioneTestiModello.findByNome("Certificato di Pubblicazione Delibera");
			t.save();

			t = new TipoCertificato();
			t.titolo 				= "Certificato di Avvenuta Pubblicazione Delibera";
			t.descrizione 			= "Certificato di Avvenuta Pubblicazione Delibera";
			t.progressivoCfgIter	= WkfCfgIter.findByNome("STANDARD: CERTIFICATO AVVENUTA PUBBLICAZIONE").progressivo;
			t.caratteristicaTipologia = CaratteristicaTipologia.findByTitolo("Certificato");
			t.modelloTesto			= GestioneTestiModello.findByNome("Certificato di Avvenuta Pubblicazione Delibera");
			t.save();

			t = new TipoCertificato();
			t.titolo 				= "Certificato di Esecutività";
			t.descrizione 			= "Certificato di Esecutività";
			t.progressivoCfgIter	= WkfCfgIter.findByNome("STANDARD: CERTIFICATO ESECUTIVITA").progressivo;
			t.caratteristicaTipologia = CaratteristicaTipologia.findByTitolo("Certificato Esecutività");
			t.modelloTesto			= GestioneTestiModello.findByNome("Certificato Esecutività");
			t.save();
		}

		if (TipoVistoParere.list().size() == 0) {
			log.debug("Installo le Tipologie di Visti e Pareri");

			TipoVistoParere t = new TipoVistoParere();
			t.titolo 		= "Visto Contabile";
			t.descrizione 	= "Visto Contabile";
			t.codice		= "VCONT";
			t.caratteristicaTipologia = CaratteristicaTipologia.findByTitolo("Visto")
			t.modelloTesto	= GestioneTestiModello.findByNome("Visto Contabile");
			t.contabile 	= true;
			t.conFirma		= true;
			t.conRedazioneUnita 	= true;
			t.conRedazioneDirigente = true;
			t.progressivoCfgIter 	= WkfCfgIter.findByNome("STANDARD: VISTO/PARERE").progressivo;
			t.save();

			t = new TipoVistoParere();
			t.titolo 		= "Visto Ragioneria";
			t.descrizione 	= "Visto Ragioneria";
			t.codice		= "VRAG";
			t.caratteristicaTipologia = CaratteristicaTipologia.findByTitolo("Visto");
			t.modelloTesto	= GestioneTestiModello.findByNome("Visto Preventivo");
			t.contabile 	= false;
			t.conFirma		= true;
			t.conRedazioneUnita 	= true;
			t.conRedazioneDirigente = true;
			t.progressivoCfgIter 	= WkfCfgIter.findByNome("STANDARD: VISTO/PARERE").progressivo;
			t.save();

			t = new TipoVistoParere();
			t.titolo 		= "Visto Segretario";
			t.descrizione 	= "Visto Segretario";
			t.codice		= "VSEGR";
			t.caratteristicaTipologia = CaratteristicaTipologia.findByTitolo("Visto");
			t.modelloTesto	= GestioneTestiModello.findByNome("Visto Preventivo");
			t.contabile 	= false;
			t.conFirma		= true;
			t.conRedazioneUnita 	= true;
			t.conRedazioneDirigente = true;
			t.progressivoCfgIter 	= WkfCfgIter.findByNome("STANDARD: VISTO/PARERE").progressivo;
			t.save();

			t = new TipoVistoParere();
			t.titolo 		= "Visto di Competenza";
			t.descrizione 	= "Visto di Competenza";
			t.codice		= "VCOMP";
			t.caratteristicaTipologia = CaratteristicaTipologia.findByTitolo("Visto");
			t.modelloTesto	= GestioneTestiModello.findByNome("Visto Preventivo");
			t.contabile 	= false;
			t.conFirma		= true;
			t.conRedazioneUnita 	= true;
			t.conRedazioneDirigente = true;
			t.progressivoCfgIter 	= WkfCfgIter.findByNome("STANDARD: VISTO/PARERE").progressivo;
			t.save();

			t = new TipoVistoParere();
			t.titolo 		= "Parere di Regolarità Tecnica";
			t.descrizione 	= "Parere di Regolarità Tecnica";
			t.codice		= "PTECN";
			t.caratteristicaTipologia = CaratteristicaTipologia.findByTitolo("Parere Tecnico")
			t.modelloTesto	= GestioneTestiModello.findByNome("Parere Tecnico");
			t.contabile 	= false;
			t.conFirma		= true;
			t.conRedazioneUnita 	= true;
			t.conRedazioneDirigente = true;
			t.progressivoCfgIter 	= WkfCfgIter.findByNome("STANDARD: PARERE TECNICO").progressivo;
			t.save();

			t = new TipoVistoParere();
			t.titolo 		= "Parere di Regolarità Contabile";
			t.descrizione 	= "Parere di Regolarità Contabile";
			t.codice		= "PCONT";
			t.caratteristicaTipologia = CaratteristicaTipologia.findByTitolo("Parere")
			t.modelloTesto	= GestioneTestiModello.findByNome("Parere Contabile");
			t.contabile 	= true;
			t.conFirma		= true;
			t.conRedazioneUnita 	= true;
			t.conRedazioneDirigente = true;
			t.progressivoCfgIter 	= WkfCfgIter.findByNome("STANDARD: VISTO/PARERE").progressivo;
			t.save();
		}

		if (TipoDetermina.list().size() == 0) {
			log.debug("Installo le Tipologie di Determina");

			TipoDetermina t = new TipoDetermina (titolo: "DETERMINA ESECUTIVA ALLA NUMERAZIONE", titoloNotifica: "DETERMINA ESECUTIVA ALLA NUMERAZIONE", descrizione: "DETERMINA ESECUTIVA ALLA NUMERAZIONE", tipoRegistro: TipoRegistro.get("DETE"), caratteristicaTipologia: CaratteristicaTipologia.findByTitolo("Determina"), vistiPareri: false, registroUnita: false, conservazioneSostitutiva: false, funzionarioObbligatorio: false, pubblicazione: true)
			t.modelloTesto	= GestioneTestiModello.findByNome("Determina senza Impegno di Spesa");
			t.giorniPubblicazione = 15;
			t.progressivoCfgIter = WkfCfgIter.findByNome("STANDARD: DETERMINA").progressivo;
			t.progressivoCfgIterPubblicazione = WkfCfgIter.findByNome("STANDARD: PUBBLICAZIONE DETERMINA").progressivo;
			t.tipoCertPubb  	= TipoCertificato.findByTitolo("Certificato di Pubblicazione Determina");
			t.tipoCertAvvPubb  	= TipoCertificato.findByTitolo("Certificato di Avvenuta Pubblicazione Determina");
			t.addToModelliTesto(t.modelloTesto);
			t.save();

			new TipoDeterminaCompetenza (ruoloAd4:Ad4Ruolo.get(Impostazioni.RUOLO_ACCESSO_APPLICATIVO.valore), titolo: "Visibile a Tutti", tipoDetermina: t, lettura: true).save();

			t = new TipoDetermina (titolo: "DETERMINA CON IMPEGNO DI SPESA", titoloNotifica: "DETERMINA CON IMPEGNO DI SPESA", descrizione: "DETERMINA CON IMPEGNO DI SPESA", tipoRegistro: TipoRegistro.get("DETE"), caratteristicaTipologia: CaratteristicaTipologia.findByTitolo("Determina"), vistiPareri: false, registroUnita: false, conservazioneSostitutiva: false, funzionarioObbligatorio: false, pubblicazione: true)
			t.modelloTesto	= GestioneTestiModello.findByNome("Determina senza Impegno di Spesa");
			t.giorniPubblicazione = 15;
			t.progressivoCfgIter = WkfCfgIter.findByNome("STANDARD: DETERMINA").progressivo;
			t.progressivoCfgIterPubblicazione = WkfCfgIter.findByNome("STANDARD: PUBBLICAZIONE DETERMINA").progressivo;
			t.tipoCertPubb  	= TipoCertificato.findByTitolo("Certificato di Pubblicazione Determina");
			t.tipoCertAvvPubb  	= TipoCertificato.findByTitolo("Certificato di Avvenuta Pubblicazione Determina");
			t.addToModelliTesto(t.modelloTesto);
			t.addToTipiVisto (TipoVistoParere.findByTitolo("Visto Contabile"));
			t.save();

			new TipoDeterminaCompetenza (ruoloAd4:Ad4Ruolo.get(Impostazioni.RUOLO_ACCESSO_APPLICATIVO.valore), titolo: "Visibile a Tutti", tipoDetermina: t, lettura: true).save();
		}

		if (TipoDelibera.list().size() == 0) {
			log.debug("Installo le Tipologie di Delibera");

			TipoDelibera t = new TipoDelibera (titolo: "DELIBERA DI GIUNTA", titoloNotifica: "DELIBERA DI GIUNTA", descrizione: "DELIBERA DI GIUNTA", tipoRegistro: TipoRegistro.get("DELG"), caratteristicaTipologia: CaratteristicaTipologia.findByTitolo("Proposta Delibera"), vistiPareri: false, registroUnita: false, conservazioneSostitutiva: false, funzionarioObbligatorio: false, pubblicazione: true)
			t.modelloTesto	= GestioneTestiModello.findByNome("Proposta di Delibera");
			t.tipoRegistroDelibera = TipoRegistro.findByDescrizione("Registro per le Delibere di Giunta");
			t.giorniPubblicazione = 15;
			t.progressivoCfgIter = WkfCfgIter.findByNome("STANDARD: PROPOSTA DI DELIBERA").progressivo;
			t.progressivoCfgIterPubblicazione = WkfCfgIter.findByNome("STANDARD: PUBBLICAZIONE DELIBERA").progressivo;
			t.tipoCertPubb  	= TipoCertificato.findByTitolo("Certificato di Pubblicazione Delibera");
			t.tipoCertAvvPubb  	= TipoCertificato.findByTitolo("Certificato di Avvenuta Pubblicazione Delibera");
			t.tipoCertEsec  	= TipoCertificato.findByTitolo("Certificato di Esecutività");
			t.commissione 		= Commissione.findByTitolo("GIUNTA COMUNALE")
			t.addToModelliTesto(t.modelloTesto);
			t.addToTipiVisto (TipoVistoParere.findByTitolo("Parere di Regolarità Contabile"));
			t.addToTipiVisto (TipoVistoParere.findByTitolo("Parere di Regolarità Tecnica"));
			t.save();

			new TipoDeliberaCompetenza (ruoloAd4:Ad4Ruolo.get(Impostazioni.RUOLO_ACCESSO_APPLICATIVO.valore), titolo: "Visibile a Tutti", tipoDelibera: t, lettura: true).save();

			t = new TipoDelibera (titolo: "DELIBERA DI CONSIGLIO", titoloNotifica: "DELIBERA DI CONSIGLIO", descrizione: "DELIBERA DI CONSIGLIO", tipoRegistro: TipoRegistro.get("DELC"), caratteristicaTipologia: CaratteristicaTipologia.findByTitolo("Proposta Delibera"), vistiPareri: false, registroUnita: false, conservazioneSostitutiva: false, funzionarioObbligatorio: false, pubblicazione: true)
			t.modelloTesto	= GestioneTestiModello.findByNome("Proposta di Delibera");
			t.tipoRegistroDelibera = TipoRegistro.findByDescrizione("Registro per le Delibere di Consiglio");
			t.giorniPubblicazione = 15;
			t.progressivoCfgIter = WkfCfgIter.findByNome("STANDARD: PROPOSTA DI DELIBERA").progressivo;
			t.progressivoCfgIterPubblicazione = WkfCfgIter.findByNome("STANDARD: PUBBLICAZIONE DELIBERA").progressivo;
			t.tipoCertPubb  	= TipoCertificato.findByTitolo("Certificato di Pubblicazione Delibera");
			t.tipoCertAvvPubb  	= TipoCertificato.findByTitolo("Certificato di Avvenuta Pubblicazione Delibera");
			t.tipoCertEsec  	= TipoCertificato.findByTitolo("Certificato di Esecutività");
			t.commissione 		= Commissione.findByTitolo("CONSIGLIO COMUNALE")
			t.addToModelliTesto(t.modelloTesto);
			t.addToTipiVisto (TipoVistoParere.findByTitolo("Parere di Regolarità Contabile"));
			t.addToTipiVisto (TipoVistoParere.findByTitolo("Parere di Regolarità Tecnica"));
			t.save();

			new TipoDeliberaCompetenza (ruoloAd4:Ad4Ruolo.get(Impostazioni.RUOLO_ACCESSO_APPLICATIVO.valore), titolo: "Visibile a Tutti", tipoDelibera: t, lettura: true).save();
		}
	}

	private CaratteristicaTipoSoggetto createCaratteristicaTipoSoggetto (String tipoSoggetto, String nomeMetodoDefault, String nomeMetodoLista, String tipoSoggettoPartenza, String ruolo) {
		return new CaratteristicaTipoSoggetto ( tipoSoggetto:			TipoSoggetto.findByCodice(tipoSoggetto)
											  , regolaCalcoloDefault: 	RegolaCalcolo.findByNomeMetodo(nomeMetodoDefault)
											  , regolaCalcoloLista:		RegolaCalcolo.findByNomeMetodo(nomeMetodoLista)
											  , tipoSoggettoPartenza:	(tipoSoggettoPartenza == null)? null : TipoSoggetto.findByCodice(tipoSoggettoPartenza)
											  , ruolo:					(ruolo == null ? null : Ad4Ruolo.findByRuolo(ruolo)));
	}


	public void installaModelliTesto (String pathModelli) {
		log.debug ("installo i modelli testo")
		File modelliTestoDir = new File(pathModelli);
		modelliTestoDir.eachFile { file ->
			log.debug ("installo il modello testo: ${file}")
			String codiceTipoModello 	= file.name.substring(0, file.name.indexOf("."));
			String titolo 				= file.name.substring(file.name.indexOf(".")+1, file.name.lastIndexOf("."));
			String tipo 				= file.name.substring(file.name.lastIndexOf(".")+1);

			GestioneTestiModello modello = GestioneTestiModello.findByNomeAndTipoModello(titolo, GestioneTestiTipoModello.get(codiceTipoModello))?:new GestioneTestiModello(nome:titolo, tipoModello:GestioneTestiTipoModello.get(codiceTipoModello));
			modello.tipo		 = tipo;
			modello.fileTemplate = file.getBytes();
			modello.save()

			new GestioneTestiModelloCompetenza(gestioneTestiModello: modello
											, lettura: 	true
											, modifica: true
											, titolo: 	"Visibile a Tutti"
											, ruoloAd4: Ad4Ruolo.get(Impostazioni.RUOLO_ACCESSO_APPLICATIVO.valore)).save();
		}
	}

	public def installaConfigurazioniIter (String path) {
		log.debug("installo le configurazioni iter")
		if (WkfTipoOggetto.list().size() == 0) {
			new WkfTipoOggetto(codice: Determina.TIPO_OGGETTO, 			nome: "Determina", 				descrizione: "Documento iterabile di tipo Determina",			 iterabile: true, valido: true, oggettiFigli:"ALLEGATO#VISTO#CERTIFICATO").save()
			new WkfTipoOggetto(codice: PropostaDelibera.TIPO_OGGETTO, 	nome: "Proposta di delibera", 	descrizione: "Documento iterabile di tipo Proposta di delibera", iterabile: true, valido: true, oggettiFigli:"ALLEGATO#VISTO").save()
			new WkfTipoOggetto(codice: Delibera.TIPO_OGGETTO, 			nome: "Delibera", 				descrizione: "Documento iterabile di tipo Delibera", 			 iterabile: true, valido: true, oggettiFigli:"ALLEGATO#CERTIFICATO").save()
			new WkfTipoOggetto(codice: VistoParere.TIPO_OGGETTO, 		nome: "Visto/Parere",			descrizione: "Documento iterabile di tipo Visto o Parere", 		 iterabile: true, valido: true).save()
			new WkfTipoOggetto(codice: Certificato.TIPO_OGGETTO,		nome: "Certificato", 			descrizione: "Documento iterabile di tipo Certificato",			 iterabile: true, valido: true).save()
			new WkfTipoOggetto(codice: Allegato.TIPO_OGGETTO,			nome: "Allegato", 				descrizione: "Documento di tipo Allegato",			 			 iterabile: false, valido: true).save()
		}

		// eseguo solo se non ho iter configurati:
		File modelliTestoDir = new File(path);
		modelliTestoDir.eachFile({ file ->
			log.debug("installo la configurazione: ${file}")

			String xml = file.getText();
			def cfgIter = wkfCfgIterXMLSerializer.importFromXml(xml, -1);
			cfgIter.stato = WkfCfgIter.STATO_IN_USO;
			cfgIter.save(failOnError:true, flush:true)
		});
	
	}
}
