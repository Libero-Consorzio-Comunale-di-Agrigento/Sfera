package it.finmatica.atti.integrazioni.lookup

import groovy.transform.CompileStatic
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.LookupValori

@CompileStatic
class LookupSiNo implements LookupValori {

    static final LookupSiNo INSTANCE = new LookupSiNo()

    private static final List<CodiceDescrizione> VALORI = [
            new CodiceDescrizione("Y", "Si")
            , new CodiceDescrizione("N", "No")]

    private LookupSiNo() {

    }

    List<CodiceDescrizione> getValori() {
        return VALORI
    }
}
