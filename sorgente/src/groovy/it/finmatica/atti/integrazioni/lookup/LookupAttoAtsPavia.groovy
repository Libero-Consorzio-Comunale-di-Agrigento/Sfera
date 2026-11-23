package it.finmatica.atti.integrazioni.lookup

import groovy.transform.CompileStatic
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.LookupValori

@CompileStatic
class LookupAttoAtsPavia implements LookupValori {

    static final LookupAttoAtsPavia INSTANCE = new LookupAttoAtsPavia()

    public static final String ATTO_IMPEGNO_SPESA   = "ATTO_IMPEGNO_SPESA"
    public static final String ATTO_VARIAZIONE      = "ATTO_VARIAZIONE"
    public static final String ATTO_TROVA_COPERTURA = "ATTO_TROVA_COPERTURA"

    private static final List<CodiceDescrizione> VALORI = [
            new CodiceDescrizione(ATTO_IMPEGNO_SPESA, "Atto di impegno di spesa")
            , new CodiceDescrizione(ATTO_VARIAZIONE, "Atto di variazione")
            , new CodiceDescrizione(ATTO_TROVA_COPERTURA, "Atto per trovare copertura")]

    private LookupAttoAtsPavia() {

    }

    List<CodiceDescrizione> getValori() {
        return VALORI
    }
}
