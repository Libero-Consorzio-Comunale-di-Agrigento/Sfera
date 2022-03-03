package it.finmatica.atti.integrazioni.lookup

import it.finmatica.atti.documenti.tipologie.TipoDelibera
import it.finmatica.atti.documenti.tipologie.TipoDetermina
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.LookupValori

class LookupTipologiaAtto implements LookupValori {

    public static final LookupTipologiaAtto INSTANCE = new LookupTipologiaAtto()

    private LookupTipologiaAtto() {}

    List<CodiceDescrizione> getValori() {
        return ((TipoDetermina.findAllByValido(true, [sort: "titolo", order: "asc"])).collect {
            new CodiceDescrizione(it.id.toString(), "Determina: " + it.titolo)

        } + (TipoDelibera.findAllByValido(true, [sort: "titolo", order: "asc"])).collect {
            new CodiceDescrizione(it.id.toString(), "Delibera: " + it.titolo)
        })
    }
}
