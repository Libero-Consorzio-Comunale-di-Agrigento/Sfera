package atti.actions.vistoparere

import atti.documenti.VistoViewModel
import grails.plugin.springsecurity.SpringSecurityService
import grails.util.GrailsNameUtils
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.AbstractViewModel
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.documenti.*
import it.finmatica.atti.documenti.beans.AttiGestioneTesti
import it.finmatica.atti.documenti.tipologie.ParametroTipologia
import it.finmatica.atti.documenti.tipologie.ParametroTipologiaService
import it.finmatica.atti.documenti.tipologie.TipoVistoParere
import it.finmatica.atti.dto.documenti.VistoParereDTOService
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.codehaus.groovy.grails.orm.hibernate.cfg.GrailsHibernateUtil
import org.zkoss.zk.ui.Executions
import org.zkoss.zul.Window

class VistoParereAction {

	SpringSecurityService 	springSecurityService
	VistoParereDTOService 	vistoParereDTOService
	VistoParereService 		vistoParereService
	AttiGestioneTesti		gestioneTesti
	WkfIterService 			wkfIterService
	IGestoreFile 			gestoreFile
	AllegatoService			allegatoService

	@Action(tipo		  	 = TipoAzione.PULSANTE,
		tipiOggetto 		 = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome				 = "Crea e apre il testo del visto/parere",
		descrizione			 = "Crea e Apre il visto/parere con il codice specificato in tipologia. Il parere viene creato solo se non ne esiste già uno valido e non CONCLUSO.",
		codiciParametri 	 = ["CODICE_VISTO"],
		descrizioniParametri = ["Codice del visto/parere"])
	public def creaEApriVistoParere (IDocumento documento, AbstractViewModel viewModel) {
		
		String codiceVisto = ParametroTipologia.getValoreParametro (documento.tipologiaDocumento, documento.iter.stepCorrente.cfgStep, "CODICE_VISTO")
		
		// se trovo già un visto/parere con il codice richiesto e statodocumento non concluso, apro quello.
		VistoParere visto = null;
		for (VistoParere v : documento.visti) {
			if (v.valido && v.tipologia.codice == codiceVisto && v.stato != StatoDocumento.CONCLUSO) {
				visto = v;
				break;
			}
		}

		// se non ho il visto ne creo e richiedo uno nuovo
		if (visto == null) {
    		visto = vistoParereService.creaVistoParere(documento, TipoVistoParere.findByCodiceAndValido(codiceVisto, true), false, springSecurityService.currentUser, documento.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4)
			vistoParereService.richiediVisto(visto);
		}
		
		if (visto.testo == null) {
			gestioneTesti.generaTestoDocumento (visto);
		}
		
		// apro la popup del visto/parere:
		String urlPopup = "/atti/documenti/parere.zul";
		if (documento instanceof Determina) {
			urlPopup = "/atti/documenti/visto.zul"
		}
		
		Window w = Executions.createComponents(urlPopup, viewModel.self, [id: visto.id])
		w.onClose {
			viewModel.aggiornaMaschera(viewModel.getDocumentoIterabile(false))
			viewModel.aggiornaPulsanti()
		}
		
		def viewModelVisto = w.attributes.vm
		
		// apro la finestra del visto
		w.doModal()

		// apro il testo
		viewModelVisto.editaTesto()
		
		return documento
	}
	
	/*
	 * Operazioni sul visto
	 */
	@Action(tipo		= TipoAzione.PULSANTE,
			tipiOggetto = [VistoParere.TIPO_OGGETTO],
			nome		= "Salva il visto",
			descrizione	= "Salva il visto (tranne l'unità destinataria)")
	public VistoParere salva (VistoParere vistoParere, VistoViewModel v) {
		vistoParere.save(failOnError: true)
		return vistoParere
	}

	@Action(tipo	= TipoAzione.PULSANTE,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "Salva l'unità e il dirigente scelti.",
		descrizione	= "Salva l'unità e il dirigente scelti.")
	public VistoParere cambiaUnitaEDirigente (VistoParere vistoParere, VistoViewModel v) {

		vistoParere.unitaSo4   = v.soggetti[TipoSoggetto.UO_DESTINATARIA]?.unita?.domainObject
		vistoParere.firmatario = v.soggetti[TipoSoggetto.FIRMATARIO]?.utente?.domainObject

		vistoParere.save(failOnError: true)
		return vistoParere
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Imposta l'utente corrente come firmatario del visto.",
		descrizione = "Imposta l'utente corrente come firmatario del visto.",
		codiciParametri 	 = ["CODICE_VISTO"],
		descrizioniParametri = ["Codice del visto/parere"])
	public def setFirmatarioVistoUtenteCorrente (def d) {
		String codiceVisto = ParametroTipologia.getValoreParametro (d.tipologia, d.iter.stepCorrente.cfgStep, "CODICE_VISTO")

		for (VistoParere v : d.visti) {
			if (v.valido && v.tipologia.codice == codiceVisto) {
				v.setSoggetto(TipoSoggetto.FIRMATARIO, springSecurityService.currentUser, null)
				v.save()
			}
		}

		return d;
	}
	
	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "Forza l'avanzamento del documento principale al nodo successivo",
		descrizione = "Se il documento principale è in un nodo di attesa, forza l'avanzamento del documento al nodo successivo senza valutare la condizione di attesa.")
    public VistoParere avanzaAttoPrincipale (VistoParere vistoParere) {
		IDocumento documentoPrincipale = vistoParere.documentoPrincipale;
		if (documentoPrincipale.iter.stepCorrente.cfgStep.cfgStepSuccessivoSblocco != null) {
			wkfIterService.proseguiStep(documentoPrincipale, documentoPrincipale.iter, documentoPrincipale.iter.stepCorrente.cfgStep.cfgStepSuccessivoSblocco)
		}
    	return vistoParere
    }

	@Action(tipo		= TipoAzione.AUTOMATICA,
			tipiOggetto = [VistoParere.TIPO_OGGETTO],
			nome		= "Sblocca l'iter dell'atto principale",
			descrizione = "Sblocca l'iter dell'atto principale sia esso una determina o una proposta di delibera")
	public VistoParere sbloccaAttoPrincipale (VistoParere vistoParere) {
		wkfIterService.sbloccaDocumento(vistoParere.documentoPrincipale)
		return vistoParere
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Sblocca l'iter dei visti richiesti",
		descrizione = "Sblocca l'iter dei visti con il codice CODICE_VISTO specificato in tipologia.",
		codiciParametri 	 = ["CODICE_VISTO"],
		descrizioniParametri = ["Codice del visto/parere"])
	public def sbloccaVisti (def documento) {
		// Recupero del parametro dalla tipologia
		String codiceVisto = ParametroTipologia.getValoreParametro (documento.tipologia, documento.iter.stepCorrente.cfgStep, "CODICE_VISTO")

		for (VistoParere vp : documento.visti) {
			if (vp.valido && vp.tipologia.codice == codiceVisto) {
				wkfIterService.sbloccaDocumento(vp)
			}
		}

		return documento
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "Imposta l'esito FAVOREVOLE",
		descrizione = "Imposta l'esito FAVOREVOLE")
	public VistoParere setEsitoFavorevole (VistoParere vistoParere) {
		vistoParere.esito = EsitoVisto.FAVOREVOLE
		vistoParere.stato = StatoDocumento.PROCESSATO
		return vistoParere
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "Imposta l'esito CONTRARIO",
		descrizione = "Imposta l'esito CONTRARIO")
	public VistoParere setEsitoContrario (VistoParere vistoParere) {
		vistoParere.esito = EsitoVisto.CONTRARIO
		vistoParere.stato = StatoDocumento.PROCESSATO
		return vistoParere
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "Imposta l'esito NON_APPOSTO",
		descrizione = "Imposta l'esito NON_APPOSTO")
	public VistoParere setEsitoNonApposto (VistoParere vistoParere) {
		vistoParere.esito = EsitoVisto.NON_APPOSTO
		vistoParere.stato = StatoDocumento.PROCESSATO
		return vistoParere
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "Imposta l'esito DA VALUTARE",
		descrizione = "Imposta l'esito DA VALUTARE")
	public VistoParere setEsitoDaValutare (VistoParere vistoParere) {
		vistoParere.esito = EsitoVisto.DA_VALUTARE;
		vistoParere.stato = StatoDocumento.PROCESSATO;
		return vistoParere
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "Imposta l'esito RIMANDA INDIETRO",
		descrizione = "Imposta l'esito RIMANDA INDIETRO")
	public VistoParere setEsitoRimandaIndietro (VistoParere vistoParere) {
		vistoParere.esito = EsitoVisto.RIMANDA_INDIETRO;
		vistoParere.stato = StatoDocumento.PROCESSATO;
		return vistoParere
	}
	
	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto = [VistoParere.TIPO_OGGETTO],
		nome		= "Imposta l'esito FAVOREVOLE CON PRESCRIZIONI",
		descrizione = "Imposta l'esito FAVOREVOLE CON PRESCRIZIONI")
	public VistoParere setEsitoFavorevoleConPrescrizioni (VistoParere vistoParere) {
		vistoParere.esito = EsitoVisto.FAVOREVOLE_CON_PRESCRIZIONI;
		vistoParere.stato = StatoDocumento.PROCESSATO;
		return vistoParere
	}

	@Action(tipo		= TipoAzione.CLIENT,
			tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
			nome		= "Apre popup scelta visto contabile",
			descrizione	= "Apre la popup per la scelta del visto contabile")
	public void apriPopupSceltaVistoContabile (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
		// per ora lancio subito la esegui pulsante così va subito sullo step successivo.
//		wkfIterService.eseguiPulsante (idCfgPulsante, viewModel, idAzioneClient);

		Window w = Executions.createComponents("/commons/popupSceltaVistoContabile.zul", viewModel.self, null)
		w.onClose { event ->
			// in teoria dovrei fare questo:
			// wkfIterService.eseguiPulsante (idCfgPulsante, viewModel, idAzioneClient);
			// in pratica non lo faccio perché questa azione sarà sempre in uno step di ATTESA e sarà la funzione di firma a sbloccare il flusso.
			if (event.data != null) {
				//se restituisce un visto parere allora esso verrà aggiunto al documento
				def documento = vistoParereDTOService.aggiungiVistoContabile(viewModel, event.data)
				viewModel.aggiornaMaschera(documento)
				viewModel.aggiornaPulsanti()
			}
		}
		w.doModal()
	}

	@Action(tipo	= TipoAzione.CLIENT,
		tipiOggetto = [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Elimina il visto contabile",
		descrizione	= "Elimina il visto contabile associato al documento")
	public void eliminaVistoContabile (AbstractViewModel<? extends IDocumentoIterabile> viewModel, long idCfgPulsante, long idAzioneClient) {
		// se restituisce un visto parere allora esso verrà aggiunto al documento
		def documento = vistoParereDTOService.eliminaVistoContabile(viewModel)
		viewModel.aggiornaMaschera(documento)
		viewModel.aggiornaPulsanti()
	}
	
	/*
	 * operazioni sui documenti che contengono visti.
	 */

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Crea Visti Automatici",
		descrizione	= "Crea i visti automatici scritti in tipologia se questi non esistono già e non sono annullati.")
	public def creaVistiAutomatici (def d) {
		vistoParereService.creaVistiAutomatici (d);
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Richiede i visti",
		descrizione	= "Richiede i visti con il codice CODICE_VISTO specificato in tipologia.",
		codiciParametri 	 = ["CODICE_VISTO"],
		descrizioniParametri = ["Codice del visto/parere da istanziare."])
	public def richiediVisti (IDocumento documento) {
		// Recupero del parametro dalla tipologia
		String codiceVisto = ParametroTipologia.getValoreParametro (documento.tipologiaDocumento, documento.iter.stepCorrente.cfgStep, "CODICE_VISTO")
		vistoParereService.richiediVisti(documento, codiceVisto);
		return documento
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Delibera.TIPO_OGGETTO],
		nome		= "Crea e richiede tutti i visti della proposta di delibera",
		descrizione	= "Crea e richiede tutti i visti della proposta di delibera usando l'iter per la Delibera specificato nella loro tipologia.")
	public def richiediVistiProposta (Delibera delibera) {
		for (VistoParere v : delibera.proposta.visti) {
			// richiedo tutti i visti validi.
			// TODO: ci metto il controllo che se non hanno l'iter configurato non fa nulla?
			if (v.valido == true) {
				// Recupero del parametro dalla tipologia
				VistoParere parereDelibera = vistoParereService.creaVistoParere(delibera, v.tipologia, true, v.firmatario, v.unitaSo4);
				vistoParereService.richiediVisto(delibera, parereDelibera, parereDelibera.tipologia.progressivoCfgIterDelibera);
			}
		}

		delibera.save()

		return delibera
	}
	
	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto	= [Delibera.TIPO_OGGETTO],
			nome		= "Crea tutti i visti della proposta di delibera",
			descrizione	= "Crea tutti i visti della proposta di delibera usando l'iter per la Delibera specificato nella loro tipologia.")
	public def creaVistiProposta (Delibera delibera) {
		for (VistoParere v : delibera.proposta.visti) {
			// richiedo tutti i visti validi.
			// TODO: ci metto il controllo che se non hanno l'iter configurato non fa nulla?
			if (v.valido == true) {
				//verifico che non ci sia già un visto valido (con lo stesso codice) per la delibera
				if (!(vistoParereService.esisteAlmenoUnVisto (delibera.visti, v.tipologia.codice, null, null, null))) {
					// Recupero del parametro dalla tipologia
					VistoParere parereDelibera = vistoParereService.creaVistoParere(delibera, v.tipologia, true, v.firmatario, v.unitaSo4);
				}
			}
		}
		
		delibera.save()
		
		return delibera
	}
	
	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
			nome		= "Crea il visto specificato in tipologia, se non già presente",
			descrizione	= "Crea il visto specificato nella tipologia, se non già presente.",
			codiciParametri  = ["CODICE_VISTO"],
		descrizioniParametri = ["Codice del visto/parere da istanziare."])
	public def creaVistoParere (IDocumento documento) {
		// Questa funzione è stata creata inizialmente per Provincia di Pistoia: http://svi-redmine/issues/17041
		
		// Recupero del parametro dalla tipologia
		String codiceVisto = ParametroTipologia.getValoreParametro (documento.tipologiaDocumento, documento.iter.stepCorrente.cfgStep, "CODICE_VISTO")
		
		// recuper la tipologia collegata
		TipoVistoParere tipologia = TipoVistoParere.findByCodiceAndValido(codiceVisto, true)
		
		if (tipologia == null) {
			throw new AttiRuntimeException ("Non è possibile proseguire: è necessaria una tipologia di visto/parere valida con codice ${codiceVisto} come richiesto dalla tipologia '${documento.tipologiaDocumento.titolo}'.")
		}

		def visti = VistoParere.createCriteria().list {
			eq(GrailsNameUtils.getPropertyName(GrailsHibernateUtil.unwrapIfProxy(documento).class), documento)
			eq("valido", true)
			eq("tipologia", tipologia)
		}
		if (visti.isEmpty()) {
			// creo il visto/parere
			vistoParereService.creaVistoParere(documento, tipologia, false)
		}

		return documento
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
		nome		= "Crea o Aggiorna il visto del funzionario",
		descrizione	= "Crea il visto del funzionario o ne aggiorna il firmatario di default se viene selezionato il passaggio al funzionario sulla proposta.",
		codiciParametri = ["CODICE_VISTO_FUNZIONARIO"],
		descrizioniParametri = ["Codice del visto/parere del funzionario."])
	public def creaVistoFunzionario (IProposta proposta) {
		// Recupero del parametro dalla tipologia
		String codiceVisto = ParametroTipologia.getValoreParametro (proposta.tipologiaDocumento, proposta.iter.stepCorrente.cfgStep, "CODICE_VISTO_FUNZIONARIO")

		// cerco il visto del funzionario
		VistoParere vistoFunzionario = vistoParereService.getVisto(proposta, codiceVisto);

		// se ho selezionato il passaggio al funzionario devo aggiungere o modificare il visto al funzionario
		if (proposta.controlloFunzionario) {

			Ad4Utente funzionario = proposta.getSoggetto(TipoSoggetto.FUNZIONARIO)?.utenteAd4;
			So4UnitaPubb unitaFunzionario = proposta.getSoggetto(TipoSoggetto.FUNZIONARIO)?.unitaSo4;

			// se non ho il visto del funzionario, ne creo uno nuovo:
			if (vistoFunzionario == null) {
				vistoFunzionario = vistoParereService.creaVistoParere(proposta, TipoVistoParere.findByCodiceAndValido(codiceVisto, true), true, funzionario, unitaFunzionario)
			} else {
				vistoFunzionario.firmatario = funzionario;
				vistoFunzionario.unitaSo4 	= unitaFunzionario;
				vistoFunzionario.save()
			}
		} else if (vistoFunzionario != null) {
			// se invece non ho il passaggio al funzionario, allora elimino il visto che ho aggiunto (se c'è)
			vistoParereService.elimina(vistoFunzionario);
		}

		return proposta;
	}
	
	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO],
		nome		= "Verifica la presenza dei firmatari per tutti i visti/pareri",
		descrizione	= "Verifica che per l'atto sia valorizzato il firmatario per ogni visto/parere presente. Interrompe l'esecuzione nel caso in cui non sia presente.")
	public void verificaFirmatariVistiPareri (IDocumento d) {
		if (!vistoParereService.verificaFirmatariVistiPareri (d)) {
			throw new AttiRuntimeException ("Non è possibile proseguire. Esiste un visto/parere privo di firmatario.")
		}
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto = [VistoParere.TIPO_OGGETTO],
			nome		= "Sposta gli allegati di un visto precedente già concluso in questo visto",
			descrizione = "Sposta gli allegati di un visto precedente già concluso in questo visto. Vengono spostati solamente gli allegati con codice SCHEDA_CONTABILE e ALLEGATO_MODIFICABILE")
	public VistoParere spostaAllegati (VistoParere vistoParere) {
		vistoParereService.spostaAllegati(vistoParere)
		return vistoParere
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto = [VistoParere.TIPO_OGGETTO],
			nome		= "Sposta gli allegati di un visto sul documento principale",
			descrizione = "Sposta gli allegati di un visto sul documento principale. Vengono spostati solamente gli allegati con codice ALLEGATO_MODIFICABILE")
	public VistoParere spostaAllegatiModificabili (VistoParere vistoParere) {
		vistoParereService.spostaAllegatiModificabili(vistoParere)
		return vistoParere
	}


	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto = [VistoParere.TIPO_OGGETTO],
			nome		= "Copia gli allegati di un visto sul documento principale",
			descrizione = "Copia gli allegati di un visto sul documento principale. Vengono copiati solamente gli allegati con codice ALLEGATO_MODIFICABILE")
	public VistoParere copiaAllegatiModificabili (VistoParere vistoParere) {
		vistoParereService.spostaAllegatiModificabili(vistoParere, false)
		return vistoParere
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto = [VistoParere.TIPO_OGGETTO],
			nome		= "Crea un allegato modificabile",
			descrizione = "Crea un allegato modificabile")
	public VistoParere creaAllegatoModificabile (VistoParere vistoParere) {
		allegatoService.creaAllegatoModificabile(vistoParere)
		return vistoParere
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto = [VistoParere.TIPO_OGGETTO],
			nome		= "Crea un ulteriore visto/parere",
			descrizione = "Crea un visto/parere in base alla configurazione del Gruppo Step. La UO_DESTINATARIA verrà ereditata dal visto corrente, mentre il firmatario verrà calcolato in base alla regola di calcolo presente nella sua caratteristica.")
	public VistoParere creaVisto (VistoParere vistoParere) {
		String codiceVisto = ParametroTipologia.getValoreParametro (vistoParere.documentoPrincipale.tipologiaDocumento, vistoParere.iter.stepCorrente.cfgStep, "CODICE_VISTO")
		TipoVistoParere tipologia = TipoVistoParere.findByCodiceAndValido(codiceVisto, true)

		if (tipologia == null) {
			throw new AttiRuntimeException ("Non è possibile proseguire: è necessaria una tipologia di visto/parere valida con codice ${codiceVisto} come richiesto dalla tipologia '${vistoParere.documentoPrincipale.tipologiaDocumento.titolo}'.")
		}

		vistoParereService.creaVistoParere (vistoParere.documentoPrincipale, tipologia, false, null, vistoParere.getSoggetto(TipoSoggetto.UO_DESTINATARIA).unitaSo4)
		return vistoParere
	}

    @Action(tipo	= TipoAzione.AUTOMATICA,
        tipiOggetto	= [Delibera.TIPO_OGGETTO],
        nome		= "Crea Pareri della Delibera",
        descrizione	= "Crea i pareri della delibera scritti in tipologia se questi non esistono già e non sono annullati.")
    public def creaVistiDelibera (Delibera delibera) {
        vistoParereService.creaPareriDelibera(delibera)
        return delibera
    }


}