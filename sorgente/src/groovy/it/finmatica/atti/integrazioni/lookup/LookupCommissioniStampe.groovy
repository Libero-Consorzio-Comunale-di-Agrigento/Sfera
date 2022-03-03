package it.finmatica.atti.integrazioni.lookup

import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.LookupValori
import it.finmatica.atti.odg.CommissioneStampa

class LookupCommissioniStampe implements LookupValori {

    public final static LookupCommissioniStampe INSTANCE = new LookupCommissioniStampe()

    private LookupCommissioniStampe() {

    }

    List<CodiceDescrizione> getValori() {
        return CommissioneStampa.findAllByCodiceIsNotNullAndValido(true, [sort: "titolo", order: "asc"]).collect {
            new CodiceDescrizione(it.id.toString(), "${it.commissione.titolo} - ${it.titolo}")
        }
    }
}
