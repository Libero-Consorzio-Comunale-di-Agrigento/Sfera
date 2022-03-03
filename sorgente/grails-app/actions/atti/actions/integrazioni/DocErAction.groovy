package atti.actions.integrazioni

import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.DocErService
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione

class DocErAction {

	DocErService docErService

	@Action(tipo	= TipoAzione.CONDIZIONE,
		tipiOggetto	= [Determina.TIPO_OGGETTO],
		nome		= "Esiste l'integrazione con Doc/Er?",
		descrizione = "Ritorna TRUE se esiste l'integrazione con doc/Er altrimenti false")
	public boolean isSalvatoDocEr (def d) {
		return 	((Impostazioni.DOCER.abilitato) && (d?.idDocumentoEsterno?true:false))
	}

	@Action(tipo	= TipoAzione.AUTOMATICA,
		tipiOggetto = [Determina.TIPO_OGGETTO],
		nome		= "Stato di sincronizzazione con Doc/Er",
		descrizione	= "Controlla lo stato di sincronizzazione della determina su Doc/Er.")
	public IAtto controllaStatoSincronizzazione (IAtto documento) {
		docErService.controllaStatoSincronizzazione(documento)
		return documento
	}
}
