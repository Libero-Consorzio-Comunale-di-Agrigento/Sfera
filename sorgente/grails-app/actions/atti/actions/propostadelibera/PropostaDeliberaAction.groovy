package atti.actions.propostadelibera

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.NotificheService
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.PropostaDeliberaService
import it.finmatica.atti.documenti.VistoParereService
import it.finmatica.atti.documenti.competenze.PropostaDeliberaCompetenze
import it.finmatica.atti.dto.documenti.DestinatarioNotificaDTOService
import it.finmatica.atti.dto.documenti.PropostaDeliberaDTO
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.CaratteristicaTipologiaService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.zkutils.SuccessHandler
import atti.actions.vistoparere.VistoParereAction
import atti.documenti.PropostaDeliberaViewModel
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zul.Window

class PropostaDeliberaAction {

	// services
	PropostaDeliberaService         propostaDeliberaService
	NotificheService                notificheService
    DestinatarioNotificaDTOService  destinatarioNotificaDTOService

	// bean generici
	SuccessHandler successHandler

	// altre azioni
	PropostaDeliberaCondizioniAction propostaDeliberaCondizioniAction

	/*
	 * Azioni sulla Proposta di Delibera
	 */

	@Action(tipo		= TipoAzione.PULSANTE,
			tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
			nome		= "Salva",
			descrizione	= "Salva la proposta di propostaDelibera.")
	public PropostaDelibera salva (PropostaDelibera d, PropostaDeliberaViewModel v) {
		d.save()
		successHandler.addMessage("Documento salvato")
		return d
	}

	@Action(tipo		= TipoAzione.AUTOMATICA,
			tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
			nome		= "Numera Proposta di Delibera",
			descrizione	= "Numera la Proposta di Delibera")
	public PropostaDelibera numeraPropostaDelibera (PropostaDelibera d) {
		if (propostaDeliberaCondizioniAction.isPropostaDeliberaNumerata(d)) {
			throw new AttiRuntimeException ("Non è possibile numerare una PropostaDelibera già numerata!")
		}
		propostaDeliberaService.numeraProposta(d)
		successHandler.addMessage("Documento numerato: ${d.numeroProposta} / ${d.annoProposta}")
		return d;
	}

	@Action(tipo		= TipoAzione.AUTOMATICA,
		tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
		nome		= "Elimina Proposta",
		descrizione	= "Elimina logicamente la Proposta PropostaDelibera")
	public PropostaDelibera eliminaProposta (PropostaDelibera d) {
		d.valido = false
        notificheService.eliminaNotifiche(d, TipoNotifica.ASSEGNAZIONE);
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
		nome		= "Annulla Proposta di Delibera",
		descrizione	= "Annulla la proposta di delibera, chiude gli iter aperti dei pareri collegati.")
	public PropostaDelibera annullaProposta (PropostaDelibera d) {
		propostaDeliberaService.annullaProposta(d);
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
		nome		= "Imposta diritti di sola lettura sulla proposta di delibera",
		descrizione	= "Imposta i diritti di sola lettura sulla proposta di delibera e i suoi documenti collegati per tutte le righe di competenza.")
	public PropostaDelibera impostaDirittiSolaLettura (PropostaDelibera d) {
		PropostaDeliberaCompetenze.executeUpdate ("update PropostaDeliberaCompetenze  c set c.modifica = false, c.cancellazione = false, c.lettura = true where c.propostaDelibera.id = :propostaDelibera", [propostaDelibera: d.id])
		PropostaDeliberaCompetenze.executeUpdate ("update VistoParereCompetenze c set c.modifica = false, c.cancellazione = false, c.lettura = true where c.vistoParere.id in (select id from VistoParere where propostaDelibera.id = :propostaDelibera)", [propostaDelibera: d.id])
		PropostaDeliberaCompetenze.executeUpdate ("update AllegatoCompetenze    c set c.modifica = false, c.cancellazione = false, c.lettura = true where c.allegato.id    in (select id from Allegato    where propostaDelibera.id = :propostaDelibera)", [propostaDelibera: d.id])
		return d
	}

    @Action(tipo		= TipoAzione.AUTOMATICA,
        tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
        nome		= "Controlla la presenza dei destinatari interni",
        descrizione	= "Controlla la presenza dei destinatari interni, a meno che sia stato specificato di non effettuare il controllo")
    public PropostaDelibera controllaDestinatariPropostaDelibera (PropostaDelibera d) {
        def listaDestinatariInterni = destinatarioNotificaDTOService.getListaDestinatariInterni(d.toDTO())
        if (d.controllaDestinatari && listaDestinatariInterni?.isEmpty()) {
            throw new AttiRuntimeException (Labels.getLabel("message.propostaDelibera.controlloDestinatari"))
        }
        return d;
    }

	@Action(tipo	= TipoAzione.AUTOMATICA,
			tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
			nome		= "Crea una copia della proposta di delibera",
			descrizione	= "Crea una copia della proposta di delibera")
	public PropostaDelibera duplica (PropostaDelibera d) {
		propostaDeliberaService.duplica(d, false, false, true)
		return d
	}

	@Action(tipo		= TipoAzione.PULSANTE,
			tipiOggetto	= [PropostaDelibera.TIPO_OGGETTO],
			nome		= "Crea una copia della proposta di delibera e la visualizza",
			descrizione	= "Crea una copia della proposta di delibera e la visualizza")
	public PropostaDelibera duplicaEApri (PropostaDelibera d, PropostaDeliberaViewModel v) {
		v.self.detach()
		PropostaDelibera copia = propostaDeliberaService.duplica(d, false, false, true)

		Window w = Executions.createComponents("/atti/documenti/propostaDelibera.zul", null, [id: copia.id])
		w.doModal()
		return d
	}

}
