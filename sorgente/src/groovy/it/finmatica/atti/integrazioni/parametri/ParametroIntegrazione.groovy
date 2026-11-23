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

    //Indica se il parametro deve essere visibile all'apertura della pagina (serve solo se per l'integrazione non ho parametri con multiplo = false)
    private final boolean visibilePrimoTab = false

    ParametroIntegrazione(String codice, String titolo) {
        this(codice, titolo, LookupTutti.INSTANCE, LookupTutti.INSTANCE)
    }

    ParametroIntegrazione(String codice, String titolo, LookupValori lookupValoriInterni, boolean visibilePrimoTab = false) {
        this(codice, titolo, lookupValoriInterni, LookupTutti.INSTANCE, visibilePrimoTab)
    }

    ParametroIntegrazione(String codice, String titolo, LookupValori lookupValoriInterni, LookupValori lookupValoriEsterni, boolean visibilePrimoTab = false) {
        this.codice = codice
        this.titolo = titolo
        this.lookup = lookupValoriInterni
        this.lookupValoriEsterni = lookupValoriEsterni
        this.multiplo = (lookupValoriInterni != LookupTutti.INSTANCE && lookupValoriInterni != null)
        this.visibilePrimoTab = visibilePrimoTab
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

    boolean isVisibilePrimoTab() {
        return visibilePrimoTab
    }
}
