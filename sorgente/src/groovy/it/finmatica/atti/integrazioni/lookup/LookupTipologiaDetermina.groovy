package it.finmatica.atti.integrazioni.lookup

import it.finmatica.atti.documenti.tipologie.TipoDetermina
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.LookupValori

class LookupTipologiaDetermina implements LookupValori {

    public final static LookupTipologiaDetermina INSTANCE = new LookupTipologiaDetermina()

    private LookupTipologiaDetermina() {

    }

    List<CodiceDescrizione> getValori() {
        return (TipoDetermina.findAllByValido(true, [sort: "titolo", order: "asc"])).collect {
            new CodiceDescrizione(it.id.toString(), it.titolo)
        }
    }
}
