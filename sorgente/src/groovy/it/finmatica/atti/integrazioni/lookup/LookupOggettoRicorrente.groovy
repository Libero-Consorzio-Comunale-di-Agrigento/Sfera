package it.finmatica.atti.integrazioni.lookup

import it.finmatica.atti.dizionari.OggettoRicorrente
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.LookupValori

class LookupOggettoRicorrente implements LookupValori {

    public final static LookupOggettoRicorrente INSTANCE = new LookupOggettoRicorrente()

    private LookupOggettoRicorrente() {

    }

    List<CodiceDescrizione> getValori() {
        return ((OggettoRicorrente.findAllByValido(true, [sort: "codice", order: "asc"])).collect {
            new CodiceDescrizione(it.id.toString(), (it.codice ? it.codice + ": " : "") + it.oggetto)
        })
    }
}
