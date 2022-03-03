package atti.actions.commons

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.AbstractViewModel
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.tipologie.ParametroTipologia
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.CaratteristicaTipologiaService
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.gestioneiter.Attore
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione
import it.finmatica.gestioneiter.motore.WkfIterService
import org.zkoss.zk.ui.Executions
import org.zkoss.zul.Window

/**
 * Contiene le azioni per il calcolo degli attori della determina
 */
class SoggettiAction {

	static final String METODO_GET_UNITA	= "getUnita"
	static final String METODO_GET_UTENTE	= "getUtente"
	static final String METODO_HA_UTENTE 	= "haUtente"
	static final String METODO_HA_UNITA 	= "haUnita"

	CaratteristicaTipologiaService 	caratteristicaTipologiaService
	SpringSecurityService 			springSecurityService
	WkfIterService 					wkfIterService

	def methodMissing(String name, args) {
		// i nomi sono della forma:
		// getUnitaTIPO_SOGGETTO oppure getUtenteTIPO_SOGGETTO oppure addFirmatarioTIPO_SOGGETTO

		if (name.startsWith (METODO_GET_UNITA)) {
			Attore attore 			= new Attore ();
			String codiceSoggetto 	= name.substring(METODO_GET_UNITA.size());
			attore.unitaSo4 		= args[0].getSoggetto(codiceSoggetto)?.unitaSo4;
			if (attore.unitaSo4 == null) {
				throw new AttiRuntimeException ("Non è stata trovata l'unità per il soggetto con codice ${codiceSoggetto} sul documento con id ${args[0].id}")
			}
			return attore;

		} else if (name.startsWith(METODO_GET_UTENTE)) {
			Attore attore 			= new Attore ()
			String codiceSoggetto 	= name.substring(METODO_GET_UTENTE.size())
			attore.utenteAd4 		= args[0].getSoggetto(codiceSoggetto)?.utenteAd4
			if (attore.utenteAd4 == null) {
				throw new AttiRuntimeException ("Non è stato trovato l'utente per il soggetto con codice ${codiceSoggetto} sul documento con id ${args[0].id}")
			}
			return attore;

		} else if (name.startsWith(METODO_HA_UTENTE)) {
			String codiceSoggetto 	= name.substring(METODO_HA_UTENTE.size())
			def utente = args[0].getSoggetto(codiceSoggetto)?.utenteAd4
			return (utente != null);

		} else if (name.startsWith(METODO_HA_UNITA)) {
			String codiceSoggetto 	= name.substring(METODO_HA_UNITA.size())
			def unita = args[0].getSoggetto(codiceSoggetto)?.unitaSo4
			return (unita != null);
		}

		throw new MissingMethodException(name, args)
	}

    /**
     * Questa funzione serve per evitare che si generi l'eccezione di soggetto non trovato per il caso del funzionario siccome c'è un parametro specifico
     * per indicare la presenza del funzionario.
     *
     * @param documento
     * @return
     */
	boolean haUtenteFUNZIONARIO (IDocumento documento) {
		return (documento.controlloFunzionario && documento.getSoggetto(TipoSoggetto.FUNZIONARIO)?.utenteAd4 != null);
	}

    /**
     * Questa funzione serve per evitare che si generi l'eccezione di soggetto non trovato per il caso del funzionario siccome c'è un parametro specifico
     * per indicare la presenza del funzionario.
     * In particolare questa esigenza è stata trovata nei flussi del SIAR.
     * #25361
     *
     * @param documento
     * @return
     */
	List<Attore> getUtenteFUNZIONARIO (IDocumento documento) {
		if (!documento.controlloFunzionario) {
			return []
		}

		Attore attore 			= new Attore ()
		attore.utenteAd4 		= documento.getSoggetto(TipoSoggetto.FUNZIONARIO)?.utenteAd4
		if (attore.utenteAd4 == null) {
			throw new AttiRuntimeException ("Non è stato trovato l'utente per il soggetto con codice ${TipoSoggetto.FUNZIONARIO} sul documento con id ${documento.id}")
		}

		return [attore]
	}
	
	/** 
	 * Questa funzione è stata aggiunta per l'ESU di Padova per poter gestire i pulsanti in un nodo di attesa visti in una determina.
	 */
	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Ritorna i firmatari dei visti/pareri",
		descrizione = "Calcola i firmatari del visti/pareri",
		codiciParametri = ["CODICE_VISTO"],
		descrizioniParametri = ["Codice del visto/parere da valutare."])
	List<Attore> getFirmatariVisti (IProposta p) {
		List<Attore> attori = [];
		String codiceVisto = ParametroTipologia.getValoreParametro (p.tipologiaDocumento, p.iter.stepCorrente.cfgStep, "CODICE_VISTO");

		for (VistoParere v : p.visti) {
			if (v.valido && v.tipologia.codice == codiceVisto && v.stato == StatoDocumento.PROCESSATO) {
				attori << new Attore (utenteAd4: v.firmatario);
			}
		}

		return attori;
	}
	
	// http://svi-redmine/issues/12620 Proposta di delibera firmata dal relatore
	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
		nome		= "Ritorna true se la Proposta ha un Relatore (con un utente)",
		descrizione = "Ritorna true se la Proposta ha un Relatore (con un utente)")
	boolean haRelatore (PropostaDelibera documento) {
		return (documento.delega?.assessore?.utenteAd4 != null)
	}
	
	// http://svi-redmine/issues/12620 Proposta di delibera firmata dal relatore
	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
		tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Ritorna l'utente Relatore della Proposta",
		descrizione = "Ritorna l'utente Relatore della Proposta (se presente)")
	List<Attore> getRelatore (def documento) {
		def proposta = documento instanceof IAtto? documento.proposta : documento;
		if (!haRelatore(proposta)) {
			return []
		}
		
		return [new Attore(utenteAd4: proposta.delega.assessore.utenteAd4)]
	}

    @Action(tipo	= TipoAzione.AUTOMATICA,
        tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO, Determina.TIPO_OGGETTO],
        nome		= "Controlla il DIRIGENTE/FIRMATARIO della Proposta",
        descrizione = "Controlla il DIRIGENTE/FIRMATARIO della Proposta, se è NULL, scatena un'eccezione. ")
	Ad4Utente controllaUtenteDIRIGENTE (IProposta proposta) {
        Ad4Utente utenteAd4 = proposta.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4
		if (utenteAd4 == null) {
			throw new AttiRuntimeException ("Non è stato trovato l'utente per il soggetto con codice ${TipoSoggetto.DIRIGENTE} sul documento con id ${proposta.id}")
		}

		return utenteAd4
	}

	@Action(tipo	= TipoAzione.CLIENT,
		tipiOggetto = [Determina.TIPO_OGGETTO],
		nome		= "Apre la popup di scelta dell'unità destinataria a cui inoltrare il documento.",
		descrizione	= "Apre la popup di scelta dell'unità destinataria a cui inoltrare il documento.")
	def apriPopupSceltaUnitaDestinataria (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
		String tipoSoggetto 	 = TipoSoggetto.UO_DESTINATARIA
		String categoriaSoggetto = TipoSoggetto.CATEGORIA_UNITA

		apriPopupSceltaSoggetto(viewModel, idCfgPulsante, idAzioneClient, tipoSoggetto, categoriaSoggetto, true)
	}

	@Action(tipo	= TipoAzione.CLIENT,
		tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Apre la popup di scelta dell'unità di controllo a cui inoltrare il documento.",
		descrizione	= "Apre la popup di scelta dell'unità di controllo a cui inoltrare il documento.")
	def apriPopupSceltaUnitaControllo (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
		String tipoSoggetto 	 = TipoSoggetto.UO_CONTROLLO
		String categoriaSoggetto = TipoSoggetto.CATEGORIA_UNITA

		apriPopupSceltaSoggetto(viewModel, idCfgPulsante, idAzioneClient, tipoSoggetto, categoriaSoggetto, false)
	}

	@Action(tipo	= TipoAzione.CLIENT,
		tipiOggetto = [Determina.TIPO_OGGETTO],
		nome		= "Apre la popup di scelta dell'incaricato a cui inoltrare il documento.",
		descrizione	= "Apre la popup di scelta dell'incaricato a cui inoltrare il documento.")
	def apriPopupSceltaIncaricato (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
		String tipoSoggetto 	 = TipoSoggetto.INCARICATO
		String categoriaSoggetto = TipoSoggetto.CATEGORIA_COMPONENTE

		apriPopupSceltaSoggetto(viewModel, idCfgPulsante, idAzioneClient, tipoSoggetto, categoriaSoggetto, false)
	}

	@Action(tipo	= TipoAzione.CLIENT,
		tipiOggetto = [Determina.TIPO_OGGETTO],
		nome		= "Apre la popup di scelta dell'unità del firmatario a cui inoltrare il documento.",
		descrizione	= "Apre la popup di scelta dell'unità del firmatario a cui inoltrare il documento.")
	def apriPopupSceltaUnitaFirmatario (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
		String tipoSoggetto 	 = TipoSoggetto.UO_FIRMATARIO
		String categoriaSoggetto = TipoSoggetto.CATEGORIA_UNITA

		apriPopupSceltaSoggetto(viewModel, idCfgPulsante, idAzioneClient, tipoSoggetto, categoriaSoggetto, true)
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto = [Determina.TIPO_OGGETTO],
		nome		= "Disattiva i firmatari congiunti.",
		descrizione	= "Disattiva i soggetti UO_FIRMATARIO e FIRMATARIO.")
	def disattivaFirmatariCongiunti (Determina determina) {
		for (DeterminaSoggetto ds : determina.soggetti) {
			if (ds.tipoSoggetto.codice == TipoSoggetto.UO_FIRMATARIO ||
				ds.tipoSoggetto.codice == TipoSoggetto.FIRMATARIO) {
				ds.attivo = false
			}
		}

		determina.save()

		return determina
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto = [Determina.TIPO_OGGETTO],
		nome		= "Disattiva le unità destinatarie.",
		descrizione	= "Disattiva i soggetti UO_DESTINATARIA.")
	def disattivaUnitaDestinatarie (Determina determina) {
		for (DeterminaSoggetto ds : determina.soggetti) {
			if (ds.tipoSoggetto.codice == TipoSoggetto.UO_DESTINATARIA) {
				ds.attivo = false
			}
		}

		determina.save()

		return determina
	}

	@Action(tipo	= TipoAzione.AUTOMATICA_CALCOLO_ATTORE,
		tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Imposta il funzionario con l'utente corrente.",
		descrizione	= "Imposta il funzionario con l'utente corrente.")
	def setFunzionarioUtenteCorrente (IProposta proposta) {

		proposta.setSoggetto (TipoSoggetto.FUNZIONARIO, springSecurityService.currentUser, null)
		proposta.save()

		return proposta
	}

	private void apriPopupSceltaSoggetto (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient, String tipoSoggetto, String categoriaSoggetto, boolean aggiungiNuoviSoggetti) {
		def soggetti = viewModel.soggetti

		// apro la popup di scelta dell'unità
		Window w = Executions.createComponents ("/atti/documenti/popupSceltaSoggetto.zul", viewModel.self, [idCaratteristicaTipologia: viewModel.documentoDTO.tipologia.caratteristicaTipologia.id
			, documento: 			viewModel.documentoDTO
			, soggetti: 			soggetti
			, tipoSoggetto: 		tipoSoggetto
			, categoriaSoggetto:	categoriaSoggetto])

		// alla chiusura, se ho scelto una unità, la salvo e proseguo con il pulsante, altrimenti no.
		w.onClose { event ->
			// se ho annullato la modifica, non faccio niente:
			if (event.data == null) {
				return
			}

            def doc = viewModel.getDocumentoIterabile(false)

            caratteristicaTipologiaService.salvaSoggettiModificati(doc, soggetti, aggiungiNuoviSoggetti)

            // aggiorno il n. di versione del dto per evitare i problemi di concorrenza.
            viewModel.documentoDTO.version = doc.version

			// continuo l'esecuzione del pulsante su una nuova transazione (in questo modo, in caso di errore, non avrò un successivo errore di controllo di concorrenza)
			viewModel.eseguiPulsante(idCfgPulsante, idAzioneClient)
		}

		w.doModal()
	}
}
