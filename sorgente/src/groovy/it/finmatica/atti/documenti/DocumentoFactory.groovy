package it.finmatica.atti.documenti

import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.documenti.storico.DeliberaStorico
import it.finmatica.atti.documenti.storico.DeterminaStorico
import it.finmatica.atti.documenti.storico.PropostaDeliberaStorico
import it.finmatica.atti.documenti.storico.SedutaStampaStorico
import it.finmatica.atti.documenti.storico.VistoParereStorico
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto

/**
 * Factory per i documenti gestiti dagli atti sulla base del TIPO_OGGETTO.
 *
 * @author esasdelli
 *
 */
class DocumentoFactory {

    static WkfTipoOggetto getTipoOggetto (def documento) {
        return WkfTipoOggetto.get(documento.TIPO_OGGETTO)
    }

    /**
     * Ritorna la lista di documenti come ritornati dalla maschera di ricerca
     * @param documenti
     * @return
     */
    static List getDocumenti (List documenti) {
        return documenti.collect { DocumentoFactory.getDocumento(it.idDocumentoPrincipale, it.tipoDocumentoPrincipale) }
    }

    static Object getDocumento (long id, String tipoOggetto) {
        switch (tipoOggetto) {
            case Determina.TIPO_OGGETTO:
                return Determina.get(id)

            case Delibera.TIPO_OGGETTO:
                return Delibera.get(id)

            case PropostaDelibera.TIPO_OGGETTO:
                return PropostaDelibera.get(id)

            case VistoParere.TIPO_OGGETTO:
                return VistoParere.get(id)

            case VistoParere.TIPO_OGGETTO_PARERE:
                return VistoParere.get(id)

            case Certificato.TIPO_OGGETTO:
                return Certificato.get(id)

            case Allegato.TIPO_OGGETTO:
                return Allegato.get(id)

            case SedutaStampa.TIPO_OGGETTO:
                return SedutaStampa.get(id)

            default:
                throw new AttiRuntimeException("Attenzione! Il tipo di documento: ${tipoOggetto} non è supportato!")
        }
    }

    static Object getDocumento (long id) {
        if (Determina.exists(id))
            return Determina.get(id)

        else if (Delibera.exists(id))
            return Delibera.get(id)

        else if(PropostaDelibera.exists(id))
            return PropostaDelibera.get(id)

        else if(VistoParere.exists(id))
            return VistoParere.get(id)

        else if(VistoParere.exists(id))
            return VistoParere.get(id)

        else if(Certificato.exists(id))
            return Certificato.get(id)

        else if(Allegato.exists(id))
            return Allegato.get(id)

        else if(SedutaStampa.exists(id))
            return SedutaStampa.get(id)

        else return null;
    }

    static Object getDocumentoStorico (long id, String tipoOggetto) {
        switch (tipoOggetto) {
            case Determina.TIPO_OGGETTO:
                return DeterminaStorico.get(id)

            case Delibera.TIPO_OGGETTO:
                return DeliberaStorico.get(id)

            case PropostaDelibera.TIPO_OGGETTO:
                return PropostaDeliberaStorico.get(id)

            case VistoParere.TIPO_OGGETTO:
                return VistoParereStorico.get(id)

            case SedutaStampa.TIPO_OGGETTO:
                return SedutaStampaStorico.get(id)

            default:
                throw new AttiRuntimeException("Attenzione! Il tipo di documento: ${tipoOggetto} non è supportato!")
        }
    }
}
