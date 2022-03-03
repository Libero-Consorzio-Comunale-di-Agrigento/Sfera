package it.finmatica.atti.integrazioni.lookup

import it.finmatica.atti.documenti.tipologie.TipoDelibera
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.LookupValori

class LookupTipologiaDelibera implements LookupValori {

    public final static LookupTipologiaDelibera INSTANCE = new LookupTipologiaDelibera()

    private LookupTipologiaDelibera() {

    }

    List<CodiceDescrizione> getValori() {
        return (TipoDelibera.findAllByValido(true, [sort: "titolo", order: "asc"])).collect {
            new CodiceDescrizione(it.id.toString(), it.titolo)
        }
    }
}
