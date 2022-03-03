package it.finmatica.atti.integrazioni.parametri

import groovy.transform.CompileStatic
import it.finmatica.atti.integrazioni.lookup.LookupTutti

@CompileStatic
class ParametroIntegrazione {

    // indica se il parametro rappresenta una lista di valori e quindi può assumere più valori per codice. Ad esempio serve per mappare gli utenti di due applicativi.
    private final boolean multiplo

    // descrizione del campo
    private final String titolo

    // codice del campo
    private final String codice

    // Funzione per il calcolo dei possibili valori interni a sfera
    private final LookupValori lookup

    // Funzione per il calcolo dei possibili valori esterni a sfera
    private final LookupValori lookupValoriEsterni

    ParametroIntegrazione(String codice, String titolo) {
        this(codice, titolo, LookupTutti.INSTANCE, LookupTutti.INSTANCE)
    }

    ParametroIntegrazione(String codice, String titolo, LookupValori lookupValoriInterni) {
        this(codice, titolo, lookupValoriInterni, LookupTutti.INSTANCE)
    }

    ParametroIntegrazione(String codice, String titolo, LookupValori lookupValoriInterni, LookupValori lookupValoriEsterni) {
        this.codice = codice
        this.titolo = titolo
        this.lookup = lookupValoriInterni
        this.lookupValoriEsterni = lookupValoriEsterni
        this.multiplo = (lookupValoriInterni != LookupTutti.INSTANCE && lookupValoriInterni != null)
    }

    boolean isMultiplo() {
        return multiplo
    }

    String getTitolo() {
        return titolo
    }

    String getCodice() {
        return codice
    }

    LookupValori getLookup() {
        return lookup
    }

    LookupValori getLookupValoriEsterni() {
        return lookupValoriEsterni
    }

    boolean isValoreInternoLibero() {
        return lookup == LookupTutti.INSTANCE
    }

    boolean isValoreEsternoLibero() {
        return lookupValoriEsterni == LookupTutti.INSTANCE
    }
}
