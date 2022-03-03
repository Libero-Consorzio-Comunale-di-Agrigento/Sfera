package it.finmatica.atti.admin

import atti.actions.commons.CampiDocumentoAction
import atti.actions.commons.SoggettiAction
import grails.plugin.springsecurity.SpringSecurityService
import groovy.sql.Sql
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.*
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.gestioneiter.annotations.Action.TipoAzione
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAzione
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAzioneService
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.gestionetesti.reporter.GestioneTestiTipoModello
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.plugins.VersionComparator

import javax.servlet.ServletContext
import javax.sql.DataSource
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

class AggiornamentoService {

    public static def AGGIORNAMENTO_AZIONI = ["V2.0.1.0": [[vecchia: "determinaCondizioniAction.esisteFunzionario", nuova: "soggettiAction.haUtenteFUNZIONARIO"]
                                             , [vecchia: "propostaDeliberaCondizioniAction.esisteFunzionario", nuova: "soggettiAction.haUtenteFUNZIONARIO"]
                                             , [vecchia: "determinaAction.notificaDestinatariEsecutivita", nuova: "notificheAction.notificaEsecutivita"]
                                             , [vecchia: "determinaCondizioniAction.isVisionataNotifica", nuova: "notificheAction.isNotificaPresente"]
                                             , [vecchia: "determinaAction.visionataNotifica", nuova: "notificheAction.presaVisione"]
                                             , [vecchia: "vistoParereAttoriAction.getUnitaRedazione", nuova: "soggettiAction.getUnitaUO_DESTINATARIA"]
                                             , [vecchia: "vistoParereAttoriAction.getFirmatario", nuova: "soggettiAction.getUtenteFIRMATARIO"]
                                             , [vecchia: "contabilitaAction.getSchedaContabile", nuova: "contabilitaAction.getSchedaContabile"]
                                             , [vecchia: "determinaAction.notificaDestinatariGenerica", nuova: "notificheAction.notificaGenerica"]
                                             , [vecchia: "determinaAction.notificaDestinatariNonEsecutivita", nuova: "notificheAction.notificaNonEsecutivita"]
                                             , [vecchia: "vistoParereAction.setStatoConcluso", nuova: "statoDocumentoAction.setStatoConcluso"]]]

    TokenIntegrazioneService tokenIntegrazioneService
    SpringSecurityService    springSecurityService
    CampiDocumentoAction     campiDocumentoAction
    ImpostazioneService      impostazioneService
    GrailsApplication        grailsApplication
    WkfAzioneService         wkfAzioneService
    DataSource               dataSource

    void doPendingUpdate (ServletContext servletContext) {

        // quesot esegue l'update delle impostazioni/azioni/etc solo dopo il passaggio da una versione all'altra
        // cioè quando viene aggiornato sfera usando l'installante
        doPendingUpdateImpostazioni(servletContext)

        // questo serve per lanciare l'update in fase di aggiornamento del solo war e non del db.
        // cioè quando viene aggiornato il solo war dalla build jenkins
        doPendingUpdateWar(servletContext)
    }

    void doPendingUpdateImpostazioni (ServletContext servletContext) {

        log.info ("Controllo se ci sono aggiornamenti da fare")

        Impostazione i = Impostazione.createCriteria().get {
            eq "codice", Impostazioni.AGGIORNAMENTO_IN_CORSO.toString()
            eq "valore", "DA_AGGIORNARE"
            lock true
        }

        // se non trovo il record, significa che non devo aggiornare perché è già stato fatto.
        if (i == null) {
            log.info("Nessun aggiornamento da fare.")
            return
        }

        log.info("Procedo con l'aggiornamento di impostazioni, azioni, modelli testo, regole di calcolo.")

        // altrimenti aggiorno tutto
        update(servletContext.getRealPath("WEB-INF/configurazioneStandard/modelliTesto/xml"))

        // infine segnalo che ho già aggiornato:
        i.valore = "N"
        i.save()
    }

    void doPendingUpdateWar (ServletContext servletContext) {

        log.info ("Controllo se ci sono aggiornamenti da fare")
        File file = new File(servletContext.getRealPath("pending.update"))

        // se non trovo il record, significa che non devo aggiornare perché è già stato fatto.
        if (!file.exists()) {
            log.info("Nessun aggiornamento da fare.")
            return
        }

        log.info("Procedo con l'aggiornamento di impostazioni, azioni, modelli testo, regole di calcolo.")

        // altrimenti aggiorno tutto
        update(servletContext.getRealPath("WEB-INF/configurazioneStandard/modelliTesto/xml"))

        // infine segnalo che ho già aggiornato:
        file.delete()
    }

    void update (String path) {
        impostazioneService.aggiornaImpostazioni()
        aggiornaTipiModelloTesto(path)
        aggiornaAzioni()
        eliminaAzioni()
        aggiornaRegoleCalcolo()
    }

    void aggiornaTipiModelloTesto (String pathXml) {
        log.info("aggiorno i tipi modelli testo:")
        File modelliTestoDir = new File(pathXml);

        modelliTestoDir.eachFile { file ->

            log.info("aggiorno il tipo modello: ${file}")

            String codiceTipoModello = file.name.substring(0, file.name.length() - 4);
            String descrizione = new XmlSlurper().parse(file).descrizione.text();

            GestioneTestiTipoModello tipoModello = GestioneTestiTipoModello.get(
                    codiceTipoModello) ?: new GestioneTestiTipoModello(codice: codiceTipoModello);
            tipoModello.descrizione = descrizione;
            tipoModello.query = file.getBytes();
            tipoModello.save();
        }
    }

    def aggiornaAzioni () {
        log.info("Aggiorno le azioni")
        // per prima cosa valido le azioni così poi quando ricreo quelle "fittizie" le rimetto a Y.
        wkfAzioneService.validaAzioni()

        CampiDocumento.list()*.delete()

		// creo i campi dei documenti:
		def campidocumenti = [[tipoOggetto: WkfTipoOggetto.get(VistoParere.TIPO_OGGETTO), 		blocco:"SOGGETTI",   		campo:"FIRMATARIO"],
							  [tipoOggetto: WkfTipoOggetto.get(VistoParere.TIPO_OGGETTO), 		blocco:"SOGGETTI",   		campo:"UO_DESTINATARIA"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"SOGGETTI",  		campo:"DIRIGENTE"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"SOGGETTI",   		campo:"UO_PROPONENTE"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"SOGGETTI",   		campo:"UO_CONTROLLO"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"SOGGETTI",   		campo:"UO_DESTINATARIA"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"SOGGETTI",   		campo:"UO_FIRMATARIO"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"SOGGETTI",   		campo:"FIRMATARIO"],
                              [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"SOGGETTI",   		campo:"FUNZIONARIO"],
                              [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"SOGGETTI",   		campo:"INCARICATO"],

							  [tipoOggetto: WkfTipoOggetto.get(SedutaStampa.TIPO_OGGETTO), 		blocco:"SOGGETTI",   		campo:"UO_PROPONENTE"],
							  [tipoOggetto: WkfTipoOggetto.get(SedutaStampa.TIPO_OGGETTO), 		blocco:"SOGGETTI",   		campo:"FIRMATARIO"],

							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"SOGGETTI",   		campo:"DIRIGENTE"],
							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"SOGGETTI",   		campo:"UO_PROPONENTE"],
							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"SOGGETTI",   		campo:"FUNZIONARIO"],
							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"SOGGETTI",   		campo:"FIRMATARIO"],
                              [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"SOGGETTI",   		campo:"UO_CONTROLLO"],
                              [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"SOGGETTI",   		campo:"INCARICATO"],
                              [tipoOggetto: WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), 	        blocco:"SOGGETTI",   		campo:"DIRETTORE_AMMINISTRATIVO"],
                              [tipoOggetto: WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), 	        blocco:"SOGGETTI",   		campo:"DIRETTORE_SANITARIO"],
                              [tipoOggetto: WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), 	        blocco:"SOGGETTI",   		campo:"DIRETTORE_GENERALE"],
                              [tipoOggetto: WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), 	        blocco:"SOGGETTI",   		campo:"DIRETTORE_SOCIO_SANITARIO"],
                              [tipoOggetto: WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), 	        blocco:"SOGGETTI",   		campo:"FIRMATARIO"],
                              [tipoOggetto: WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), 	        blocco:"SOGGETTI",   		campo:"PRESIDENTE"],
                              [tipoOggetto: WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), 	        blocco:"SOGGETTI",   		campo:"SEGRETARIO"],
                              [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"CONTABILITA",		campo:"CONTABILITA"],
							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"CONTABILITA",		campo:"CONTABILITA"],
							  [tipoOggetto: WkfTipoOggetto.get(VistoParere.TIPO_OGGETTO), 		blocco:"CONTABILITA",		campo:"CONTABILITA"],
							  [tipoOggetto: WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), 			blocco:"CONTABILITA",		campo:"CONTABILITA"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"TESTO_MANUALE", 	campo:"TESTO_MANUALE"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"TESTO_E_ALLEGATI", 	campo:"TESTO"],
							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"TESTO_E_ALLEGATI", 	campo:"TESTO"],
							  [tipoOggetto: WkfTipoOggetto.get(VistoParere.TIPO_OGGETTO), 		blocco:"TESTO_E_ALLEGATI", 	campo:"TESTO"],
							  [tipoOggetto: WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), 			blocco:"TESTO_E_ALLEGATI", 	campo:"TESTO"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"TESTO_E_ALLEGATI", 	campo:"ALLEGATI"],
							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"TESTO_E_ALLEGATI", 	campo:"ALLEGATI"],
							  [tipoOggetto: WkfTipoOggetto.get(VistoParere.TIPO_OGGETTO), 		blocco:"TESTO_E_ALLEGATI", 	campo:"ALLEGATI"],
							  [tipoOggetto: WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), 			blocco:"TESTO_E_ALLEGATI", 	campo:"ALLEGATI"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"TESTO_E_ALLEGATI", 	campo:"CATEGORIA"],
							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"TESTO_E_ALLEGATI", 	campo:"CATEGORIA"],
							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"TESTO_E_ALLEGATI", 	campo:"DELEGA"],
							  [tipoOggetto: WkfTipoOggetto.get(VistoParere.TIPO_OGGETTO), 		blocco:"TESTO_E_ALLEGATI", 	campo:"CATEGORIA"],
							  [tipoOggetto: WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), 			blocco:"TESTO_E_ALLEGATI", 	campo:"CATEGORIA"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"RISERVATO", 		campo:"RISERVATO"],
							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"RISERVATO", 		campo:"RISERVATO"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"RIFERIMENTI", 		campo:"DOCUMENTI_COLLEGATI"],
							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"RIFERIMENTI", 		campo:"DOCUMENTI_COLLEGATI"],
							  [tipoOggetto: WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), 			blocco:"RIFERIMENTI", 		campo:"DOCUMENTI_COLLEGATI"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"RIFERIMENTI", 		campo:"DATI_PROTOCOLLO"],
							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"RIFERIMENTI", 		campo:"DATI_PROTOCOLLO"],
							  [tipoOggetto: WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), 			blocco:"RIFERIMENTI", 		campo:"DATI_PROTOCOLLO"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"VISTI_E_PARERI", 	campo:"VISTI_E_PARERI"],
							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"VISTI_E_PARERI", 	campo:"VISTI_E_PARERI"],
							  [tipoOggetto: WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), 			blocco:"VISTI_E_PARERI", 	campo:"VISTI_E_PARERI"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"COMUNICAZIONI", 	campo:"DESTINATARI"],
							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"COMUNICAZIONI", 	campo:"DESTINATARI"],
							  [tipoOggetto: WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), 			blocco:"COMUNICAZIONI", 	campo:"DESTINATARI"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"PUBBLICAZIONE", 	campo:"PUBBLICAZIONE"],
							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"PUBBLICAZIONE", 	campo:"PUBBLICAZIONE"],
							  [tipoOggetto: WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), 			blocco:"PUBBLICAZIONE",		campo:"PUBBLICAZIONE"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"NOTE", 				campo:"NOTE"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"NOTE", 				campo:"NOTE_TRASMISSIONE"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"NOTE", 				campo:"NOTE_CONTABILI"],
							  [tipoOggetto: WkfTipoOggetto.get(VistoParere.TIPO_OGGETTO), 		blocco:"NOTE", 				campo:"NOTE"],
							  [tipoOggetto: WkfTipoOggetto.get(VistoParere.TIPO_OGGETTO), 		blocco:"NOTE", 				campo:"NOTE_TRASMISSIONE"],
							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"NOTE", 				campo:"NOTE"],
							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"NOTE", 				campo:"NOTE_TRASMISSIONE"],
							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"NOTE", 				campo:"NOTE_CONTABILI"],
							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"NOTE", 				campo:"NOTE_COMMISSIONE"],
							  [tipoOggetto: WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), 			blocco:"NOTE", 				campo:"NOTE"],
                              [tipoOggetto: WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), 			blocco:"NOTE", 				campo:"NOTE_TRASMISSIONE"],
                              [tipoOggetto: WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), 			blocco:"NOTE", 				campo:"ESTRATTO"],
                              [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"NOTE", 				campo:"ESTRATTO"],
                              [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO),			blocco:"NOTE", 				campo:"ESTRATTO"],
                              [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"SCADENZA", 			campo:"DATA_SCADENZA"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"SCADENZA", 			campo:"MOTIVAZIONE"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"PRIORITA", 			campo:"PRIORITA"],
							  [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"PRIORITA", 			campo:"MOTIVAZIONE"],
							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"PRIORITA", 			campo:"PRIORITA"],
							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"PRIORITA", 			campo:"MOTIVAZIONE"],
                              [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 		blocco:"DOPPIA_FIRMA", 		campo:"DOPPIA_FIRMA"],
                              [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"DOPPIA_FIRMA", 		campo:"DOPPIA_FIRMA"],
                              [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"ISTANZA", 		    campo:"DATA_ISTANZA"],
                              [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"ISTANZA", 		    campo:"COMMISSIONE_CONSILIARE"],
                              [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"ISTANZA", 		    campo:"NOTE_CONVOCAZIONE"],
							  [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"PARERE_REVISORI_CONTI", campo:"PARERE_REVISORI_CONTI"],
                              [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"ESEGUIBILITA_IMMEDIATA", campo:"ESEGUIBILITA_IMMEDIATA"],
                              [tipoOggetto: WkfTipoOggetto.get(Delibera.TIPO_OGGETTO), 	        blocco:"ESEGUIBILITA_IMMEDIATA", campo:"ESEGUIBILITA_IMMEDIATA"],
                              [tipoOggetto: WkfTipoOggetto.get(Determina.TIPO_OGGETTO), 	    blocco:"BUDGET",            campo:"BUDGET"],
                              [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"BUDGET",            campo:"BUDGET"],
                              [tipoOggetto: WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO), 	blocco:"COMMISSIONE",       campo:"COMMISSIONE"]
        ]

        for (def c : campidocumenti) {
            CampiDocumento.findOrSaveWhere(c)
        }

        campiDocumentoAction.aggiornaAzioni();

        // creo le azioni per il calcolo dei soggetti:
        def soggetti = [[tipoOggetto     : WkfTipoOggetto.get(Determina.TIPO_OGGETTO)
                         , soggettiUtente: [TipoSoggetto.REDATTORE, TipoSoggetto.FUNZIONARIO, TipoSoggetto.DIRIGENTE, TipoSoggetto.FIRMATARIO, TipoSoggetto.INCARICATO]
                         , soggettiUnita : [TipoSoggetto.UO_PROPONENTE, TipoSoggetto.UO_DESTINATARIA, TipoSoggetto.UO_FIRMATARIO, TipoSoggetto.UO_CONTROLLO]],

                        [tipoOggetto     : WkfTipoOggetto.get(VistoParere.TIPO_OGGETTO)
                         , soggettiUtente: [TipoSoggetto.FIRMATARIO]
                         , soggettiUnita : [TipoSoggetto.UO_DESTINATARIA]],

                        [tipoOggetto     : WkfTipoOggetto.get(PropostaDelibera.TIPO_OGGETTO)
                         , soggettiUtente: [TipoSoggetto.REDATTORE, TipoSoggetto.FUNZIONARIO, TipoSoggetto.DIRIGENTE, TipoSoggetto.INCARICATO]
                         , soggettiUnita : [TipoSoggetto.UO_PROPONENTE, TipoSoggetto.UO_CONTROLLO]],

                        [tipoOggetto     : WkfTipoOggetto.get(SedutaStampa.TIPO_OGGETTO)
                         , soggettiUtente: [TipoSoggetto.REDATTORE, TipoSoggetto.FIRMATARIO]
                         , soggettiUnita : []],

                        [tipoOggetto     : WkfTipoOggetto.get(Delibera.TIPO_OGGETTO)
                         , soggettiUtente: [TipoSoggetto.PRESIDENTE, TipoSoggetto.SEGRETARIO, TipoSoggetto.DIRETTORE_AMMINISTRATIVO, TipoSoggetto.DIRETTORE_SANITARIO, TipoSoggetto.DIRETTORE_GENERALE, TipoSoggetto.FIRMATARIO, TipoSoggetto.DIRETTORE_SOCIO_SANITARIO]
                         , soggettiUnita : []]]

        for (def doc : soggetti) {

            for (def s : doc.soggettiUtente) {
                wkfAzioneService.insertOrUpdate(new WkfAzione([nome         : "Ritorna l'utente ${s}"
                                                               , descrizione: "Ritorna l'utente ${s} del documento ${doc.tipoOggetto.nome}"
                                                               , nomeBean   : "soggettiAction"
                                                               , nomeMetodo : "${SoggettiAction.METODO_GET_UTENTE}${s}"
                                                               , tipoOggetto: doc.tipoOggetto
                                                               , tipo       : TipoAzione.CALCOLO_ATTORE
                                                               , ente       : springSecurityService.principal.amministrazione]),
                                                doc.tipoOggetto.codice)

                wkfAzioneService.insertOrUpdate(new WkfAzione([nome         : "Ritorna TRUE se è presente il soggetto ${s}"
                                                               , descrizione: "Ritorna TRUE se è presente il soggetto ${s} del documento ${doc.tipoOggetto.nome}"
                                                               , nomeBean   : "soggettiAction"
                                                               , nomeMetodo : "${SoggettiAction.METODO_HA_UTENTE}${s}"
                                                               , tipoOggetto: doc.tipoOggetto
                                                               , tipo       : TipoAzione.CONDIZIONE
                                                               , ente       : springSecurityService.principal.amministrazione]),
                                                doc.tipoOggetto.codice)
            }

            for (def s : doc.soggettiUnita) {
                wkfAzioneService.insertOrUpdate(new WkfAzione([nome         : "Ritorna l'unità ${s}"
                                                               , descrizione: "Ritorna l'unità ${s} del documento ${doc.tipoOggetto.nome}"
                                                               , nomeBean   : "soggettiAction"
                                                               , nomeMetodo : "${SoggettiAction.METODO_GET_UNITA}${s}"
                                                               , tipoOggetto: doc.tipoOggetto
                                                               , tipo       : TipoAzione.CALCOLO_ATTORE
                                                               , ente       : springSecurityService.principal.amministrazione]),
                                                doc.tipoOggetto.codice)

                wkfAzioneService.insertOrUpdate(new WkfAzione([nome         : "Ritorna TRUE se è presente il soggetto ${s}"
                                                               , descrizione: "Ritorna TRUE se è presente il soggetto ${s} del documento ${doc.tipoOggetto.nome}"
                                                               , nomeBean   : "soggettiAction"
                                                               , nomeMetodo : "${SoggettiAction.METODO_HA_UNITA}${s}"
                                                               , tipoOggetto: doc.tipoOggetto
                                                               , tipo       : TipoAzione.CONDIZIONE
                                                               , ente       : springSecurityService.principal.amministrazione]),
                                                doc.tipoOggetto.codice)
            }
        }

        // aggiorno le azioni:
        wkfAzioneService.aggiornaAzioni();
    }

    def sostituisciVecchieAzioniConNuove (def params) {
        def azioniVecchie = null;
        if (params.azioneVecchia instanceof String) {
            azioniVecchie = [];
            azioniVecchie << params.long("azioneVecchia")
        } else {
            azioniVecchie = params.azioneVecchia.collect { Long.parseLong(it) }
        }
        long azioneNuova = params.long("azioneNuova")

        Sql sql = new Sql(dataSource)
        for (long idAzioneVecchia : azioniVecchie) {
            if (azioneNuova < 0) {
                // elimino le azioni
                sql.executeUpdate("delete from wkf_diz_pulsanti_azioni where id_azione 	= ?", idAzioneVecchia);
                sql.executeUpdate("delete from wkf_cfg_step_azioni_in  where id_azione_in	= ?", idAzioneVecchia)
                sql.executeUpdate("delete from wkf_cfg_step_azioni_out where id_azione_out = ?", idAzioneVecchia)

                // correggo le sequenze:
                sql.call("""begin
for c in (
 SELECT pa.id_pulsante, pa.id_azione,
            (ROW_NUMBER ()
            OVER (
               PARTITION BY pa.id_pulsante
               ORDER BY pa.id_pulsante, pa.azioni_idx ASC))-1
           sequenza, pa.azioni_idx
   FROM wkf_diz_pulsanti_azioni pa
ORDER BY pa.id_pulsante)
loop
    update wkf_diz_pulsanti_azioni pa set pa.azioni_idx = c.sequenza where pa.id_pulsante = c.id_pulsante and pa.id_azione = c.id_azione;
    commit;
end loop;

for c in (
 SELECT pa.id_cfg_step, pa.id_azione_in,
            (ROW_NUMBER ()
            OVER (
               PARTITION BY pa.id_cfg_step
               ORDER BY pa.id_cfg_step, pa.azioni_ingresso_idx ASC))-1
           sequenza, pa.azioni_ingresso_idx
   FROM wkf_cfg_step_azioni_in pa
ORDER BY pa.id_cfg_step)
loop
    update wkf_cfg_step_azioni_in pa set pa.azioni_ingresso_idx = c.sequenza where pa.id_cfg_step = c.id_cfg_step and pa.id_azione_in = c.id_azione_in;
    commit;
end loop;

for c in (
 SELECT pa.id_cfg_step, pa.id_azione_out,
            (ROW_NUMBER ()
            OVER (
               PARTITION BY pa.id_cfg_step
               ORDER BY pa.id_cfg_step, pa.azioni_uscita_idx ASC))-1
           sequenza, pa.azioni_uscita_idx
   FROM wkf_cfg_step_azioni_out pa
ORDER BY pa.id_cfg_step)
loop
    update wkf_cfg_step_azioni_out pa set pa.azioni_uscita_idx = c.sequenza where pa.id_cfg_step = c.id_cfg_step and pa.id_azione_out = c.id_azione_out;
    commit;
end loop;

end;""")
                // elimino le azioni "singole"
                sql.executeUpdate(
                        "update wkf_cfg_step 		   set id_azione_condizione 	= null where id_azione_condizione 		= ?",
                        idAzioneVecchia)
                sql.executeUpdate(
                        "update wkf_diz_pulsanti		   set id_condizione_visibilita = null where id_condizione_visibilita 	= ?",
                        idAzioneVecchia)
                sql.executeUpdate(
                        "update wkf_diz_attori		   set id_azione_calcolo		= null where id_azione_calcolo 			= ?",
                        idAzioneVecchia)
            } else {
                sql.executeUpdate(
                        "update wkf_diz_pulsanti_azioni set id_azione 				= ? where id_azione 				= ?",
                        azioneNuova, idAzioneVecchia)
                sql.executeUpdate(
                        "update wkf_cfg_step_azioni_in  set id_azione_in 			= ? where id_azione_in				= ?",
                        azioneNuova, idAzioneVecchia)
                sql.executeUpdate(
                        "update wkf_cfg_step_azioni_out set id_azione_out			= ? where id_azione_out 			= ?",
                        azioneNuova, idAzioneVecchia)
                sql.executeUpdate(
                        "update wkf_cfg_step 		   set id_azione_condizione 	= ? where id_azione_condizione 		= ?",
                        azioneNuova, idAzioneVecchia)
                sql.executeUpdate(
                        "update wkf_diz_pulsanti		   set id_condizione_visibilita = ? where id_condizione_visibilita 	= ?",
                        azioneNuova, idAzioneVecchia)
                sql.executeUpdate(
                        "update wkf_diz_attori		   set id_azione_calcolo		= ? where id_azione_calcolo 		= ?",
                        azioneNuova, idAzioneVecchia)
            }
        }
    }

    def eliminaAzioni () {
        Sql sql = new Sql(dataSource)
        sql.executeUpdate(
                "delete from wkf_diz_azioni_parametri p where exists(select * from wkf_diz_azioni a where p.id_azione = a.id_azione and valido = 'N')")
        sql.executeUpdate("delete from wkf_diz_azioni where valido = 'N'")
    }

    def aggiornaRegoleCalcolo () {
        log.info("Installo le Regole di Calcolo");

        def regole = [];
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "vistoParereSoggettoService", nomeMetodo: "getUnitaInTipologia", titolo: "Unità scelta in tipologia visto"]
        regole << [tipo: RegolaCalcolo.TIPO_LISTA, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "vistoParereSoggettoService", nomeMetodo: "getListaUnitaInTipologia", titolo: "Lista delle unità scelte in tipologia visto."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "vistoParereSoggettoService", nomeMetodo: "getUnitaInTipologiaInRamoUoProponente", titolo: "Unità scelta in tipologia visto nel ramo dell'unità proponente della determina"]
        regole << [tipo: RegolaCalcolo.TIPO_LISTA, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "vistoParereSoggettoService", nomeMetodo: "getListaUnitaInTipologiaInRamoUoProponente", titolo: "Lista delle unità scelte in tipologia visto nel ramo dell'unità proponente della determina"]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getComponentePerUtenteCorrente", titolo: "Utente corrente."]
        regole << [tipo: RegolaCalcolo.TIPO_LISTA, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getListaComponentiPerUtenteConRuoloInOttica", titolo: "Utenti con ruolo nell'ottica per l'utente corrente."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getComponenteConRuoloInOttica", titolo: "Utente con ruolo nell'ottica."]
        regole << [tipo: RegolaCalcolo.TIPO_LISTA, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getListaComponentiConRuoloInOttica", titolo: "Utenti con ruolo nell'ottica."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getComponenteConRuoloInUnita", titolo: "Utente con ruolo in unità."]
        regole << [tipo: RegolaCalcolo.TIPO_LISTA, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getListaComponentiConRuoloInUnita", titolo: "Utenti con ruolo in unità."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getComponenteConRuoloInUnitaPadri", titolo: "Utente con ruolo in unità padri."]
        regole << [tipo: RegolaCalcolo.TIPO_LISTA, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getListaComponentiConRuoloInUnitaPadri", titolo: "Utenti con ruolo in unità padri."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getComponenteConRuoloInSuddivisione", titolo: "Utente con ruolo in suddivisione."]
        regole << [tipo: RegolaCalcolo.TIPO_LISTA, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getListaComponentiConRuoloInSuddivisione", titolo: "Utenti con ruolo in suddivisione."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getComponenteConRuoloInArea", titolo: "Utente con ruolo in area."]
        regole << [tipo: RegolaCalcolo.TIPO_LISTA, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getListaComponentiConRuoloInArea", titolo: "Utenti con ruolo in area."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getComponenteConRuoloInUnitaFiglie", titolo: "Utente con ruolo in unità figlie."]
        regole << [tipo: RegolaCalcolo.TIPO_LISTA, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getListaComponentiConRuoloInUnitaFiglie", titolo: "Utenti con ruolo in unità figlie."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "regolaCalcoloService", nomeMetodo: "getUnitaSoggetto", titolo: "Unità del soggetto."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "regolaCalcoloService", nomeMetodo: "getUnitaPreferitaSoggetto", titolo: "Unità preferita del soggetto."]
        regole << [tipo: RegolaCalcolo.TIPO_LISTA, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "regolaCalcoloService", nomeMetodo: "getListaUnitaSoggetto", titolo: "Lista delle unità possibili del soggetto."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "regolaCalcoloService", nomeMetodo: "getUnitaSoggettoConRuolo", titolo: "Unità del soggetto con ruolo."]
        regole << [tipo: RegolaCalcolo.TIPO_LISTA, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "regolaCalcoloService", nomeMetodo: "getListaUnitaSoggettoConRuolo", titolo: "Lista delle unità possibili del soggetto con ruolo."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "regolaCalcoloService", nomeMetodo: "getUnitaOttica", titolo: "Prima unità nell'ottica."]
        regole << [tipo: RegolaCalcolo.TIPO_LISTA, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "regolaCalcoloService", nomeMetodo: "getListaUnitaOttica", titolo: "Lista delle unità nell'ottica."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getResponsabileConRuoloInUnita", titolo: "L'utente RESPONSABILE e con RUOLO nell'unità del soggetto e nelle sue unità padri."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "regolaCalcoloService", nomeMetodo: "getUnitaArea", titolo: "L'unità AREA del soggetto"]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "regolaCalcoloService", nomeMetodo: "getUnitaPreferitaArea", titolo: "L'unità preferita AREA del soggetto"]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "regolaCalcoloService", nomeMetodo: "getUnitaServizio", titolo: "L'unità SERVIZIO del soggetto"]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "regolaCalcoloService", nomeMetodo: "getUnitaPreferitaServizio", titolo: "L'unità preferita SERVIZIO del soggetto"]
        regole << [tipo: RegolaCalcolo.TIPO_LISTA, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "regolaCalcoloService", nomeMetodo: "getListaUnitaFiglie", titolo: "Le unità figlie del soggetto"]
        regole << [tipo: RegolaCalcolo.TIPO_LISTA, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "regolaCalcoloService", nomeMetodo: "getUnitaPadriUtenteCorrente", titolo: "Le unità padri delle unità dell'utente corrente."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getDirigenteDocumentoPrincipale", titolo: "Il dirigente del documento principale."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getIncaricatoDocumentoPrincipale", titolo: "L'incaricato del documento principale."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getComponenteConRuoloInAreaPadre", titolo: "L'utente con ruolo nell'unità AREA padre dell'unità di riferimento."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "regolaCalcoloService", nomeMetodo: "getUnitaProponenteDocumentoPrincipale", titolo: "L'unità proponente del documento principale."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getComponenteCertificatoEsecutivita", titolo: "L'utente con RUOLO CERTIFICATO ESECUTIVITA'"]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getResponsabileConRuoloInOttica", titolo: "L'utente RESPONSABILE e con RUOLO nell'ottica"]
        regole << [tipo: RegolaCalcolo.TIPO_LISTA, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "regolaCalcoloService", nomeMetodo: "getListaUnitaServizio", titolo: "Lista dei SERVIZI delle unità a cui appartiene il soggetto con RUOLO"]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getFirmatarioDecretiInOttica", titolo: "Utente con ruolo firmatario dei decreti nell'ottica"]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "regolaCalcoloService", nomeMetodo: "getUnitaFunzionarioDocumentoPrincipale", titolo: "L'unità del funzionario sul documento principale."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getComponenteRelatore", titolo: "Il relatore della Proposta di Delibera"]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getFunzionarioDocumentoPrincipale", titolo: "Il funzionario del documento principale."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "regolaCalcoloService", nomeMetodo: "getUnitaDestinatariaDocumentoPrincipale", titolo: "L'unità destinataria del documento principale."]
        regole << [tipo: RegolaCalcolo.TIPO_LISTA, categoria: TipoSoggetto.CATEGORIA_UNITA, nomeBean: "regolaCalcoloService", nomeMetodo: "getListaUnitaConRuolo", titolo: "Le unità appartenenti all'ottica con almeno un componente con il RUOLO specificato"]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getComponenteConRuoloInUnitaEUnitaPadri", titolo: "L'utente con ruolo nell'unità del soggetto e nelle sue unità padri."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getFirmatariSeduta", titolo: "I firmatari delle delibere in seduta."]

        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getComponentePresidente", titolo: "Il Presidente della Seduta o quello dichiarato in Commissione."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getComponenteSegretario", titolo: "Il Segretario della Seduta o quello dichiarato in Commissione."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getComponenteDirettoreAmministrativo", titolo: "Il Direttore Amministrativo in Seduta o quello dichiarato in Commissione."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getComponenteDirettoreSanitario", titolo: "Il Direttore Sanitario in Seduta o quello dichiarato in Commissione."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getComponenteDirettoreGenerale", titolo: "Il Direttore Generale in Seduta o quello dichiarato in Commissione."]
        regole << [tipo: RegolaCalcolo.TIPO_DEFAULT, categoria: TipoSoggetto.CATEGORIA_COMPONENTE, nomeBean: "regolaCalcoloService", nomeMetodo: "getComponenteDirettoreSocioSanitario", titolo: "Il Direttore Socio Sanitario in Seduta o quello dichiarato in Commissione."]

        log.info("aggiorno le regole esistenti")
        for (def regola : regole) {
            RegolaCalcolo r = RegolaCalcolo.findByTipoAndCategoriaAndNomeBeanAndNomeMetodo(regola.tipo,
                                                                                           regola.categoria,
                                                                                           regola.nomeBean,
                                                                                           regola.nomeMetodo)
            if (r == null) {
                new RegolaCalcolo(regola).save()
            } else {
                r.titolo = regola.titolo;
                r.save()
            }
        }

        log.info("elimino le regole non più presenti")
        RegolaCalcolo.list().findAll { r ->
            (regole.find {
                it.nomeBean == r.nomeBean && it.tipo == r.tipo && it.nomeMetodo == r.nomeMetodo && it.categoria == r.categoria
            } == null)
        }.each {
            it.delete()
        }
    }

    void sostituisciVecchieAzioniConVersione (String versione) {
        def azioni = AGGIORNAMENTO_AZIONI[versione];

        for (def azione : azioni) {
            String nomeBean = azione.vecchia.substring(0, azione.vecchia.indexOf("."));
            String nomeMetodo = azione.vecchia.substring(azione.vecchia.indexOf(".") + 1);

            // ottengo le azioni vecchie:
            def azioniVecchie = WkfAzione.findAllByNomeBeanAndNomeMetodo(nomeBean, nomeMetodo);

            for (def azioneVecchia : azioniVecchie) {
                // ottengo l'azione nuova:
                String nomeBeanNuova = azione.nuova.substring(0, azione.nuova.indexOf("."));
                String nomeMetodoNuova = azione.nuova.substring(azione.nuova.indexOf(".") + 1);

                def azioneNuova = WkfAzione.findByTipoOggettoAndNomeBeanAndNomeMetodoAndValido(
                        azioneVecchia.tipoOggetto, nomeBeanNuova, nomeMetodoNuova, true);

                if (azioneNuova != null) {
                    Sql sql = new Sql(dataSource)
                    sql.executeUpdate(
                            "update wkf_diz_pulsanti_azioni set id_azione 				= ? where id_azione 				= ?",
                            azioneNuova.id, azioneVecchia.id)
                    sql.executeUpdate(
                            "update wkf_cfg_step_azioni_in  set id_azione_in 			= ? where id_azione_in				= ?",
                            azioneNuova.id, azioneVecchia.id)
                    sql.executeUpdate(
                            "update wkf_cfg_step 		   set id_azione_condizione 	= ? where id_azione_condizione 		= ?",
                            azioneNuova.id, azioneVecchia.id)
                    sql.executeUpdate(
                            "update wkf_diz_pulsanti		   set id_condizione_visibilita = ? where id_condizione_visibilita 	= ?",
                            azioneNuova.id, azioneVecchia.id)
                    sql.executeUpdate(
                            "update wkf_diz_attori		   set id_azione_calcolo		= ? where id_azione_calcolo 		= ?",
                            azioneNuova.id, azioneVecchia.id)
                }
            }
        }
    }

    // per auto-aggiornamento micro-patch:
    void runUpdates () {
        // connettiti al server di update.
        def availableUpdates = getAvailableUpdates();

        // scarica le nuove patch
        for (def update : availableUpdates) {
            runUpdate(update.url, update.version);
        }
    }

    def getAvailableUpdates () {
        String minVersion = grailsApplication.metadata['app.version'].replaceAll("-b", ".");

        def max = minVersion.split("\\.");
        max[2] = (Integer.parseInt(max[2]) + 1).toString();
        for (int i = 3; i < max.size(); i++) {
            max[i] = "0";
        }
        String maxVersion = max.join(".");
        def repositories = grailsApplication.config.autoupdate.repositories;

        for (String repository : repositories) {
            def updates = getAvailableUpdates(repository, minVersion, maxVersion);
            if (updates != null && updates.size() > 0) {
                return updates;
            }
        }

        return [];
    }

    def getAvailableUpdates (String repository, String minVersion, String maxVersion) {
        log.info("Controllo aggiornamenti sul repository: $repository per $minVersion < versione < $maxVersion");
        URL url = new URL(repository + "/updates.xml");
        InputStream is = null;
        VersionComparator comparator = new VersionComparator();
        try {
            is = url.openStream();
            def root = new XmlParser().parse(is);

            // ottengo tutte le versioni maggiori di quella corrente:
            def availableUpdates = root.update.@version.findAll { version ->
                (comparator.compare(version, minVersion) > 0 && comparator.compare(version, maxVersion) < 0)
            }.sort { a, b -> comparator.compare(a, b) };

            log.info("Versioni disponibili: $availableUpdates")

            // riordino le versioni e ritorno l'url:
            return availableUpdates.collect { [url: repository + "/" + it + ".zip", version: it] };
        } finally {
            try {
                is?.close();
            } catch (Exception e) {
            }
        }
    }

    void runUpdate (String updateUrl, String version) {
        File destDir = new File(grailsApplication.mainContext.servletContext.getRealPath(""));
        // scarica lo zip:
        URL url = new URL(updateUrl);
        InputStream is = null;
        try {
            is = url.openStream();

            // scarico e unzippo la patch:
            unzip(is, "temp/updates/$version");

            // eseguo l'aggiornamento:
            FileUtils.copyDirectory(new File("temp/updates/$version"), destDir);

            // mi segno che ho fatto un aggiornamento:
            grailsApplication.metadata['pending.updates'] = true;

        } finally {
            try {
                is?.close();
            } catch (Exception e) {
            }

            // elimino tutto quanto ho creato nella directory temporanea:
            FileUtils.deleteDirectory(new File("temp/updates/$version"));
        }
    }

    void unzip (InputStream zipStream, String outputFolder) {
        // create output directory is not exists
        File folder = new File(outputFolder);
        if (!folder.exists()) {
            folder.mkdir();
        }

        // get the zip file content
        ZipInputStream zis = new ZipInputStream(zipStream);

        // get the zipped file list entry
        ZipEntry ze = zis.getNextEntry();

        while (ze != null) {
            String fileName = ze.getName();
            File file = new File(outputFolder + File.separator + fileName);

            if (ze.isDirectory()) {
                if (!file.exists()) {
                    file.mkdirs();
                }
            } else {
                if (!(file.getParentFile().exists())) {
                    file.getParentFile().mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(file);
                IOUtils.copy(zis, fos);

                fos.close();
            }

            ze = zis.getNextEntry();
        }

        zis.closeEntry();
        zis.close();
    }
}