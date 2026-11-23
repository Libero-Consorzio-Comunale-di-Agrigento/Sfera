package it.finmatica.atti.integrazioni.lookup

import it.finmatica.atti.dizionari.TipoAllegato
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.LookupValori

class LookupTipoAllegatoCodiceEsterno implements LookupValori {

    public final static LookupTipoAllegatoCodiceEsterno INSTANCE = new LookupTipoAllegatoCodiceEsterno()

    private LookupTipoAllegatoCodiceEsterno() {

    }

    List<CodiceDescrizione> getValori() {
        return (TipoAllegato.findAllByValidoAndCodiceEsternoIsNotNull(true, [sort: "titolo", order: "asc"])).collect {
            new CodiceDescrizione(it.codiceEsterno, it.titolo)
        }
    }
}
