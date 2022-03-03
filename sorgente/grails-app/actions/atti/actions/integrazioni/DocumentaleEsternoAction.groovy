package atti.actions.integrazioni

import it.finmatica.atti.IDocumentaleEsterno
import it.finmatica.atti.documenti.*
import it.finmatica.gestioneiter.annotations.Action
import it.finmatica.gestioneiter.annotations.Action.TipoAzione

/**
 * Contiene le azioni per la comunicazione con un documentale esterno.
 */
class DocumentaleEsternoAction {

    IDocumentaleEsterno gestoreDocumentaleEsterno

    @Action(tipo = TipoAzione.AUTOMATICA,
            tipiOggetto = [Determina.TIPO_OGGETTO, Certificato.TIPO_OGGETTO, VistoParere.TIPO_OGGETTO, Delibera.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO],
            nome = "Salva il documento su GDM",
            descrizione = "Salva il documento sul documentale esterno, (GDM)")
    def salvaDocumentoEsterno (def documento) {
        gestoreDocumentaleEsterno.salvaDocumento(documento)
        return documento;
    }
}
