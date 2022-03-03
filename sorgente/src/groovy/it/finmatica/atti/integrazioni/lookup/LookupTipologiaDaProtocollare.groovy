package it.finmatica.atti.integrazioni.lookup

import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.LookupValori
import it.finmatica.atti.odg.CommissioneStampa

class LookupTipologiaDaProtocollare implements LookupValori {

    public static final LookupTipologiaDaProtocollare INSTANCE = new LookupTipologiaDaProtocollare()

    private LookupTipologiaDaProtocollare() {}

    List<CodiceDescrizione> getValori() {
        return (LookupTipologiaAtto.INSTANCE.getValori() + CommissioneStampa.findAllByValido(true, [sort: "commissione.titolo", order: "asc"], [sort: "titolo", order: "asc"]).collect {
            new CodiceDescrizione(it.id.toString(), "${it.commissione.titolo}: ${it.titolo}")
        })
    }
}
