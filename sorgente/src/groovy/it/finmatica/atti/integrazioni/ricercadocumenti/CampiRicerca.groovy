package it.finmatica.atti.integrazioni.ricercadocumenti

/**
 * Created by esasdelli on 02/10/2017.
 */
class CampiRicerca {
    Map<String, Object> filtri
    Map<String, String> ordinamento
    int startFrom
    int maxResults

    CampiRicerca () {
        filtri = [:]
        ordinamento = [:]
        startFrom = 0
        maxResults = 30
    }
}
