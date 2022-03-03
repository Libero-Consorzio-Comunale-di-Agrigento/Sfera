package it.finmatica.atti.integrazioni.lookup

import it.finmatica.atti.dizionari.TipoAllegato
import it.finmatica.atti.documenti.tipologie.TipoDelibera
import it.finmatica.atti.documenti.tipologie.TipoDetermina
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.LookupValori

class LookupTipoAllegato implements LookupValori {

    public final static LookupTipoAllegato INSTANCE = new LookupTipoAllegato()

    private LookupTipoAllegato() {

    }

    List<CodiceDescrizione> getValori() {
        return (TipoAllegato.findAllByValido(true, [sort: "titolo", order: "asc"])).collect {
            new CodiceDescrizione(it.id.toString(), it.titolo)
        }
    }
}
