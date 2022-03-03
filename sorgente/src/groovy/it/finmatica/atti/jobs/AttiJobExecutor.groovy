package it.finmatica.atti.jobs

import grails.util.Holders
import it.finmatica.atti.IDocumentaleEsterno
import it.finmatica.atti.IProtocolloEsterno
import it.finmatica.atti.IntegrazioneAlbo
import it.finmatica.atti.IntegrazioneContabilita
import it.finmatica.atti.commons.AttiUtils
import it.finmatica.atti.commons.TokenIntegrazione
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.dizionari.NotificaErrore
import it.finmatica.atti.documenti.*
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.AttiFirmaService
import it.finmatica.atti.integrazioni.ConservazioneService
import it.finmatica.atti.integrazioni.lettera.IntegrazioneLetteraAgspr
import it.finmatica.gestioneiter.motore.WkfIterService
import org.apache.log4j.Logger
import org.springframework.transaction.annotation.Transactional

@Transactional
class AttiJobExecutor {

	private static final Logger log = Logger.getLogger(AttiJobExecutor.class);

	IntegrazioneLetteraAgspr integrazioneLetteraAgspr
	TokenIntegrazioneService tokenIntegrazioneService
	IntegrazioneContabilita integrazioneContabilita
	ConservazioneService conservazioneService
	IProtocolloEsterno protocolloEsterno
	AttiFirmaService attiFirmaService
	DeterminaService determinaService
	DeliberaService deliberaService
	WkfIterService wkfIterService
	IntegrazioneAlbo integrazioneAlbo
	DocumentoService documentoService
	FileFirmatoDettaglioService fileFirmatoDettaglioService
    IDocumentaleEsterno gestoreDocumentaleEsterno
    NotificheErroreService notificheErroreService

	String[] eseguiAutenticazione(String utente) {
		AttiUtils.eseguiAutenticazione(Holders.config.grails.plugins.anagrafesoggetti.utenteBatch)
		return Impostazioni.ENTI_SO4.valori
	}

	boolean lock(String codiceEnte) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

		tokenIntegrazioneService.rimuoviVecchioToken("JOB_NOTTURNO", "ATTI_JOB")

		// ottengo il lock sulla tabella TOKEN_INTEGRAZIONI in modo tale di essere sicuro che con più tomcat ne parta uno solo:
		TokenIntegrazione token = tokenIntegrazioneService.beginTokenTransaction("JOB_NOTTURNO", "ATTI_JOB")
		if (!token.statoInCorso) {
			log.info("C'è già un job che sta girando per l'ente con codice: ${codiceEnte}, esco e non faccio nulla.")
			return false
		}

		return true
	}

	void unlock(String codiceEnte) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

		tokenIntegrazioneService.endTokenTransaction("JOB_NOTTURNO", "ATTI_JOB")
	}

	void eliminaTransazioniFirmaVecchie(String codiceEnte) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

		attiFirmaService.eliminaTransazioniVecchie()
	}

	void sbloccaDocumento(String codiceEnte, Class<?> DomainClass, long id) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

		wkfIterService.sbloccaDocumento(DomainClass.get(id))
	}

	List<IDocumento> getDocumentiDaSbloccare(String codiceEnte) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

		return ((integrazioneContabilita.getDocumentiDaSbloccare() ?: []) + // documenti con movimenti contabili pagati (in contabilità cfa)
				determinaService.getDetermineFinePubblicazione() +            // determine in attesa di conclusione della pubblicazione
				deliberaService.getDelibereFinePubblicazione())
		// delibere in attesa di conclusione della pubblicazione
	}

	List<Determina> getDetermineDaRendereEsecutive(String codiceEnte) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

		return determinaService.getDetermineDaRendereEsecutive()
	}

	List<Delibera> getDelibereDaRendereEsecutive(String codiceEnte) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

		return deliberaService.getDelibereDaRendereEsecutive()
	}

	List<Determina> getDetermineInConservazione(String codiceEnte) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

		return conservazioneService.getDetermineInConservazione()
	}

	List<Delibera> getDelibereInConservazione(String codiceEnte) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

		return conservazioneService.getDelibereInConservazione()
	}

	void inviaDocumentiInConservazione(String codiceEnte) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

		conservazioneService.inviaDocumentiInConservazione()
	}

	void rendiEsecutivaDetermina(String codiceEnte, long id) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

        Determina determina = Determina.get(id)
		determinaService.rendiEsecutiva(determina)
        gestoreDocumentaleEsterno.salvaDocumento(determina)
	}

	void rendiEsecutivaDelibera(String codiceEnte, long id) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

		Delibera delibera = Delibera.get(id)
        deliberaService.rendiEsecutiva(delibera)
        gestoreDocumentaleEsterno.salvaDocumento(delibera);

    }

	void aggiornaStatoConservazioneDetermina(String codiceEnte, long id) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

		Determina determina = Determina.get(id)
		conservazioneService.aggiornaStatoConservazione(determina)
	}

	void aggiornaStatoConservazioneDelibera(String codiceEnte, long id) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

		Delibera delibera = Delibera.get(id)
		conservazioneService.aggiornaStatoConservazione(delibera)
	}

	void aggiornaClassificazioni(String codiceEnte) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

		protocolloEsterno.sincronizzaClassificazioniEFascicoli()
	}

	void aggiornaDatePubblicazioni(String codiceEnte) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

		integrazioneAlbo.allineaDatePubblicazioni()
	}

	List<Determina> getDetermineInPubblicazione(String codiceEnte) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

		return determinaService.getDetermineDaPubblicare()
	}

	void pubblicaDetermina(String codiceEnte, long id) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

        Determina determina = Determina.get(id)
		documentoService.pubblicazione(determina)
        gestoreDocumentaleEsterno.salvaDocumento(determina);
	}

	List<Delibera> getDelibereInPubblicazione(String codiceEnte) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

		return deliberaService.getDeliberaDaPubblicare()
	}

	void aggiornaDatiProtocolloSedutaStampa(String codiceEnte) {
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

		integrazioneLetteraAgspr.aggiornaDatiProtocolloSedutaStampa()
	}

	void pubblicaDelibera(String codiceEnte, long id) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

        Delibera delibera = Delibera.get(id)
		documentoService.pubblicazione(delibera)
        gestoreDocumentaleEsterno.salvaDocumento(delibera);
	}

	void cancellaLockDocumenti(String codiceEnte) {
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)
		tokenIntegrazioneService.rimuoviLockDocumenti(Certificato.TIPO_OGGETTO);
		tokenIntegrazioneService.rimuoviLockDocumenti(Determina.TIPO_OGGETTO);
		tokenIntegrazioneService.rimuoviLockDocumenti(Delibera.TIPO_OGGETTO);
		tokenIntegrazioneService.rimuoviLockDocumenti(PropostaDelibera.TIPO_OGGETTO);
		tokenIntegrazioneService.rimuoviLockDocumenti(VistoParere.TIPO_OGGETTO);
	}


	void estraiInformazioniFileFirmati(String codiceEnte) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

		fileFirmatoDettaglioService.estraiInformazioniFileFirmati()
	}

	boolean controllaDocumentiAlboConErrore(String codiceEnte) {
		// imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
		AttiUtils.abilitaMultiEnteFilter(codiceEnte)
		AttiUtils.setAmministrazioneOttica(codiceEnte)

		return integrazioneAlbo.controllaDocumentiAlboConErrore()
	}

    List<NotificaErrore> getErroriNotifiche(String codiceEnte){
        // imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
        AttiUtils.abilitaMultiEnteFilter(codiceEnte)
        AttiUtils.setAmministrazioneOttica(codiceEnte)
        return notificheErroreService.getErroriNotifiche()
    }

    void gestisciErroriNotifica(String codiceEnte, Long id) {
        // imposto il filtro dell'ente per la sessione hibernate e seleziono l'amministrazione di login
        AttiUtils.abilitaMultiEnteFilter(codiceEnte)
        AttiUtils.setAmministrazioneOttica(codiceEnte)

        NotificaErrore notificaErrore = NotificaErrore.get(id)
        notificheErroreService.gestisciErroriNotifica(notificaErrore)
    }
}