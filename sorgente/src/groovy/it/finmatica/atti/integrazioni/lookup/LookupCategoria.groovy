package it.finmatica.atti.integrazioni.lookup

import it.finmatica.atti.dizionari.Categoria
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.LookupValori

class LookupCategoria implements LookupValori {

    public final static LookupCategoria INSTANCE = new LookupCategoria()

    private LookupCategoria() {

    }

    List<CodiceDescrizione> getValori() {
        return Categoria.createCriteria().list() {
            eq("valido", true)
            order("tipoOggetto", "asc")
            order("codice", "asc")
        }.collect {
            new CodiceDescrizione(it.codice, (it.tipoOggetto == Categoria.TIPO_OGGETTO_DETERMINA ? 'Determina: ' : 'Delibera: ') + it.codice)
        }
    }
}
