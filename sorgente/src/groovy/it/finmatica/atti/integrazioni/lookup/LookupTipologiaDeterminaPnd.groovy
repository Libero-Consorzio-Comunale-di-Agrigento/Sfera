package it.finmatica.atti.integrazioni.lookup

import it.finmatica.atti.documenti.tipologie.TipoDetermina
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.LookupValori

class LookupTipologiaDeterminaPnd implements LookupValori {

    public final static LookupTipologiaDeterminaPnd INSTANCE = new LookupTipologiaDeterminaPnd()

    private LookupTipologiaDeterminaPnd() {

    }

    List<CodiceDescrizione> getValori() {
        return (TipoDetermina.findAllByValidoAndPndNotifica(true, true, [sort: "titolo", order: "asc"])).collect {
            new CodiceDescrizione(it.id.toString(), it.titolo)
        }
    }
}
