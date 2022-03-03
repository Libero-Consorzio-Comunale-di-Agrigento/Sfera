package it.finmatica.atti.integrazioni.lookup

import groovy.transform.CompileStatic
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.LookupValori

@CompileStatic
class LookupTestoOStampaUnica implements LookupValori {

    static final LookupTestoOStampaUnica INSTANCE = new LookupTestoOStampaUnica()

    private static final List<CodiceDescrizione> VALORI = [
            new CodiceDescrizione("STAMPA_UNICA", "Stampa Unica")
            , new CodiceDescrizione("TESTO", "Testo")]

    private LookupTestoOStampaUnica() {

    }

    List<CodiceDescrizione> getValori() {
        return VALORI
    }
}
