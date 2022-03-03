package atti.actions.delibera

import it.finmatica.atti.AbstractViewModel
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.DeliberaService
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.competenze.DeliberaCompetenze
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione

import org.zkoss.zk.ui.Executions
import org.zkoss.zul.Window

import atti.actions.propostadelibera.PropostaDeliberaAction
import atti.documenti.PropostaDeliberaViewModel

class DeliberaAction {

	PropostaDeliberaAction 	propostaDeliberaAction
	DeliberaService 		deliberaService

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Delibera.TIPO_OGGETTO],
		nome		= "Imposta diritti di sola lettura sulla delibera",
		descrizione	= "Imposta i diritti di sola lettura sulla delibera e i suoi documenti collegati per tutte le righe di competenza.")
	public Delibera impostaDirittiSolaLettura (Delibera d) {
		DeliberaCompetenze.executeUpdate ("update DeliberaCompetenze    c set c.modifica = false, c.cancellazione = false, c.lettura = true where c.delibera.id = :delibera", [delibera: d.id])
		DeliberaCompetenze.executeUpdate ("update CertificatoCompetenze c set c.modifica = false, c.cancellazione = false, c.lettura = true where c.certificato.id in (select id from Certificato where delibera.id = :delibera)", [delibera: d.id])
		DeliberaCompetenze.executeUpdate ("update VistoParereCompetenze c set c.modifica = false, c.cancellazione = false, c.lettura = true where c.vistoParere.id in (select id from VistoParere where delibera.id = :delibera)", [delibera: d.id])
		DeliberaCompetenze.executeUpdate ("update AllegatoCompetenze    c set c.modifica = false, c.cancellazione = false, c.lettura = true where c.allegato.id    in (select id from Allegato    where delibera.id = :delibera)", [delibera: d.id])
		propostaDeliberaAction.impostaDirittiSolaLettura(d.proposta)
		return d
	}
	
	@Action(tipo	= TipoAzione.CLIENT,
		tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
		nome		= "Apre la popup della Delibera",
		descrizione = "Apre la popup della Delibera")
	public void apriPopupDelibera (PropostaDeliberaViewModel viewModel, long idCfgPulsante, long idAzioneClient) {
		viewModel.onApriAtto();
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto	= [Delibera.TIPO_OGGETTO],
			nome		= "Aggiunge i firmatari specificati in commissione.",
			descrizione = "Aggiunge i firmatari specificati in commissione alla coda di firma, solo se non ci sono già dei firmatari presenti.")
	public def addFirmatariCommissione (Delibera documento) {
		deliberaService.aggiungiFirmatari(documento)
		return documento
	}

    @Action(tipo	= TipoAzione.AUTOMATICA,
        tipiOggetto	= [Delibera.TIPO_OGGETTO],
        nome		= "Aggiunge i firmatari specificati nella delibera.",
        descrizione = "Aggiunge i firmatari specificati nella delibera secondo l'ordine riportato in commissione.")
    public def addFirmatariDelibera (Delibera documento) {
        deliberaService.aggiungiFirmatariDelibera(documento)
        return documento
    }

    @Action(tipo	= TipoAzione.AUTOMATICA,
        tipiOggetto	= [Delibera.TIPO_OGGETTO],
        nome		= "Ricalcola i firmatari specificati nella delibera.",
        descrizione = "Ricalcola i firmatari specificati nella delibera secondo l'ordine riportato in commissione.")
    public def ricalcolaFirmatariDelibera (Delibera documento) {
        deliberaService.ricalcolaFirmatariDelibera(documento)
        return documento
    }

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
		nome		= "Crea e attiva la delibera.",
		descrizione = "Crea la delibera partendo dalla proposta, ne attiva il flusso.")
	public def creaDelibera (PropostaDelibera proposta) {
		Delibera delibera = deliberaService.creaDelibera(proposta)
		
		// Il testo normalmente viene creato in ODG.
		// può però esserci l'impostazione sulla tipologia che dice che il testo della delibera
		// è quello della proposta e in questo caso viene creato dalla "deliberaService.creaDelibera".
		// In caso quindi il testo non ci sia lascio che sia l'utente con il primo click su "edita-testo" a crearlo.
		// calcolo però il modello testo, in caso non l'avessi fatto prima.
		if (delibera.testo == null) {
			delibera.modelloTesto = deliberaService.calcolaModelloTesto(delibera)
		}
		deliberaService.attivaDelibera(delibera)
		return proposta
	}
	
	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Delibera.TIPO_OGGETTO],
		nome		= "Numera la delibera.",
		descrizione = "Numera la delibera.")
	public def numeraDelibera (Delibera delibera) {
		deliberaService.adottaDelibera(delibera)
		return delibera
	}
	
	@Action(tipo	= TipoAzione.PULSANTE,
		tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
		nome		= "Apre la Delibera.",
		descrizione = "Apre la maschera della delibera.")
	public def apriDelibera (PropostaDelibera proposta, AbstractViewModel<? extends IDocumentoIterabile> v) {
		Window w = Executions.createComponents("/atti/documenti/delibera.zul", v.self, [id: proposta.atto.id])
		w.doModal()
		return proposta
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Delibera.TIPO_OGGETTO],
		nome		= "Rigenera la delibera",
		descrizione = "Elimina i firmatari che non hanno firmato, ripristina i file originali non firmati, ricrea i pareri se presenti.")
	public def rigeneraDelibera (Delibera delibera) {
		deliberaService.rigeneraDelibera(delibera)
		return delibera
	}

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Delibera.TIPO_OGGETTO],
		nome		= "La Delibera è Immediatamente Eseguibile?",
		descrizione	= "Ritorna TRUE se la Delibera è Immediatamente Eseguibile, FALSE altrimenti.")
	public boolean isImmediatamenteEseguibile (Delibera d) {
		return d.eseguibilitaImmediata
	}
	
	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Delibera.TIPO_OGGETTO],
		nome		= "Rende esecutiva la delibera",
		descrizione	= "Rende esecutiva la delibera. Crea il certificato di eseguibilità se richiesto dalla tipologia.")
	public Delibera rendiEsecutiva (Delibera delibera) {
		deliberaService.rendiEsecutiva(delibera)
		return delibera
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Delibera.TIPO_OGGETTO],
		nome		= "Rende immediatamente esecutiva la delibera",
		descrizione	= "Se la delibera è 'Immediatamente Eseguibile', imposta la data di esecutività sulla Delibera. La Data di Esecutività corrisponderà alla data di discussione della seduta se presente, altrimenti la data corrente.")
	public Delibera rendiImmediatamenteEsecutiva (Delibera delibera) {
		deliberaService.rendiImmediatamenteEsecutiva(delibera)
		return delibera
	}
}
