package atti.actions.determina

import atti.documenti.DeterminaViewModel
import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.DeterminaService
import it.finmatica.atti.documenti.NotificheService
import it.finmatica.atti.documenti.competenze.DeterminaCompetenze
import it.finmatica.atti.documenti.tipologie.ParametroTipologia
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.gestioneiter.Attore
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgStep
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import it.finmatica.zkutils.SuccessHandler
import org.zkoss.util.resource.Labels
import org.zkoss.zk.ui.Executions
import org.zkoss.zul.Window

class DeterminaAction {

	// services
	DeterminaService determinaService
	NotificheService notificheService
	SpringSecurityService springSecurityService

	// altre azioni
	DeterminaCondizioniAction 	determinaCondizioniAction

	// bean generici
	SuccessHandler 	successHandler

	/*
	 * Operazioni sulla determina e proposta di determina
	 */

	@Action(tipo		= TipoAzione.PULSANTE,
			tipiOggetto	= [Determina.TIPO_OGGETTO],
			nome		= "Salva",
			descrizione	= "Salva la proposta di determina.")
	public Determina salva (Determina d, DeterminaViewModel v) {
		d.save()
		successHandler.addMessage("Documento salvato")
		return d
	}

	@Action(tipo		= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO],
		nome		= "Numera Proposta",
		descrizione	= "Numera la Proposta Determina")
	public Determina numeraProposta (Determina d) {
		if (determinaCondizioniAction.isPropostaDeterminaNumerata(d)) {
			throw new AttiRuntimeException ("Non è possibile numerare un documento già numerato!")
		}

		determinaService.numeraProposta(d)

		successHandler.addMessage("Documento numerato: ${d.numeroProposta} / ${d.annoProposta}")
		return d
	}

	@Action(tipo		= TipoAzione.AUTOMATICA,
			tipiOggetto	= [Determina.TIPO_OGGETTO],
			nome		= "Numera Determina",
			descrizione	= "Numera la Determina")
	public Determina numeraDetermina (Determina d) {
		if (!determinaCondizioniAction.isDeterminaNumerata(d)) {
			determinaService.numeraDetermina(d)
			successHandler.addMessage("Atto numerato: ${d.numeroDetermina} / ${d.annoDetermina}")
		}

		return d
	}

	@Action(tipo		= TipoAzione.AUTOMATICA,
			tipiOggetto	= [Determina.TIPO_OGGETTO],
			nome		= "Seconda numerazione Determina",
			descrizione	= "Da' un secondo numero alla Determina usando il registro di seconda numerazione specificato in tipologia.")
	public Determina numeraDetermina2 (Determina d) {
		if (!determinaCondizioniAction.isDeterminaNumerata2(d)) {
			determinaService.numeraDetermina2(d)
			successHandler.addMessage("Seconda numerazione atto: ${d.numeroDetermina} / ${d.annoDetermina}")
		}

		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO],
		nome		= "Annulla Proposta di Determina",
		descrizione	= "Annulla la proposta di determina, chiude gli iter aperti dei visti collegati. Annulla i movimenti contabili e il documento in Casa di Vetro")
	public Determina annullaProposta (Determina d) {
		determinaService.annullaProposta(d);
		return d
	}

	@Action(tipo		= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO],
		nome		= "Elimina Determina",
		descrizione	= "Elimina logicamente la determina")
	public Determina eliminaDetermina (Determina d) {
		d.valido = false
        notificheService.eliminaNotifiche(d, TipoNotifica.ASSEGNAZIONE);
		return d
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO],
		nome		= "Rende esecutiva la determina",
		descrizione	= "Rende esecutiva la determina, annulla o integra le determine collegate.")
	public Determina rendiEsecutiva (Determina d) {
		determinaService.rendiEsecutiva(d, new Date())
		return d
	}

    @Action(tipo	= TipoAzione.AUTOMATICA,
        tipiOggetto	= [Determina.TIPO_OGGETTO],
        nome		= "Rende immediatamente esecutiva la determina",
        descrizione	= "Rende immediatamente esecutiva la determina, annulla o integra le determine collegate.")
    public Determina rendiImmediatamenteEsecutiva (Determina d) {
        if (!d.eseguibilitaImmediata){
            return d;
        }

        determinaService.rendiEsecutiva(d, new Date())
        return d
    }

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO],
		nome		= "Rende NON esecutiva la determina",
		descrizione	= "Rende NON esecutiva la determina")
	public Determina rendiNonEsecutiva (Determina d) {
		determinaService.rendiNonEsecutiva(d)
		return d
	}

	@Action(tipo		= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO],
		nome		= "Imposta diritti di sola lettura sulla determina",
		descrizione	= "Imposta i diritti di sola lettura sulla determina e i suoi documenti collegati per tutte le righe di competenza.")
	public Determina impostaDirittiSolaLettura (Determina d) {
		DeterminaCompetenze.executeUpdate ("update DeterminaCompetenze   c set c.modifica = false, c.cancellazione = false, c.lettura = true where c.determina.id = :determina", [determina: d.id])
		DeterminaCompetenze.executeUpdate ("update CertificatoCompetenze c set c.modifica = false, c.cancellazione = false, c.lettura = true where c.certificato.id in (select id from Certificato where determina.id = :determina)", [determina: d.id])
		DeterminaCompetenze.executeUpdate ("update VistoParereCompetenze c set c.modifica = false, c.cancellazione = false, c.lettura = true where c.vistoParere.id in (select id from VistoParere where determina.id = :determina)", [determina: d.id])
		DeterminaCompetenze.executeUpdate ("update AllegatoCompetenze    c set c.modifica = false, c.cancellazione = false, c.lettura = true where c.allegato.id    in (select id from Allegato    where determina.id = :determina)", [determina: d.id])
		return d
	}

	@Action(tipo	= TipoAzione.CALCOLO_ATTORE,
		tipiOggetto	= [Determina.TIPO_OGGETTO],
		nome		= "Ritorna l'unità scritta nel parametro PROGR_UO nei TipoDeterminaParametro",
		descrizione = "Calcola l'unità scritta nel parametro PROGR_UO nei TipoDeterminaParametro",
		codiciParametri = ["PROGR_UO"],
		descrizioniParametri = ["Unità a cui far passare la determina"] )
	public Attore getUoParametro (Determina d) {
		Attore a 	= new Attore()
		WkfCfgStep 	cfgStep = d.iter?.stepCorrente?.cfgStep

		if (cfgStep == null)
			return a

		String valore = ParametroTipologia.getValoreParametro(d.tipologia, cfgStep, "PROGR_UO");

		if (valore == null)
			return a

		a.unitaSo4 = So4UnitaPubb.getUnita(Long.parseLong(valore), springSecurityService.principal.ottica().codice).get()
		return a
	}
	
	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto	= [Determina.TIPO_OGGETTO],
		nome		= "Crea una copia della determina",
		descrizione	= "Crea una copia della determina")
	public Determina duplica (Determina d) {
		determinaService.duplica(d, false, false, true)
		return d
	}
	
	/*
	 * Operazioni sulla determina e proposta di determina
	 */

	@Action(tipo		= TipoAzione.PULSANTE,
			tipiOggetto	= [Determina.TIPO_OGGETTO],
			nome		= "Crea una copia del documento determina e la visualizza",
			descrizione	= "Crea una copia del documento determina e la visualizza")
	public Determina duplicaEApri (Determina d, DeterminaViewModel v) {
		v.self.detach()
		Determina copia = determinaService.duplica(d, false, false, true)

		Window w = Executions.createComponents("/atti/documenti/determina.zul", null, [id: copia.id])
		w.doModal()
		return d
	}
}

