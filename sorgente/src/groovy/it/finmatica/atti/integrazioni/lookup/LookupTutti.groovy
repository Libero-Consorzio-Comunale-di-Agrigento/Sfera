package it.finmatica.atti.integrazioni.lookup

import groovy.transform.CompileStatic
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.LookupValori

@CompileStatic
class LookupTutti implements LookupValori {

    static final LookupTutti INSTANCE = new LookupTutti()

    private static final List<CodiceDescrizione> VALORI = [
            new CodiceDescrizione(
                    MappingIntegrazione.VALORE_INTERNO_TUTTI
                    , MappingIntegrazione.VALORE_INTERNO_TUTTI)]

    private LookupTutti() {

    }

    List<CodiceDescrizione> getValori() {
        return VALORI
    }
}
