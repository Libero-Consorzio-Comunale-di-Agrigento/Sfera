package it.finmatica.atti.config

import groovy.transform.CompileStatic
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.parametri.ModuloIntegrazione
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione

@CompileStatic
abstract class AbstractWebServiceConfig implements ModuloIntegrazione {

    public static final String MAPPING_URL       = "URL_WEBSERVICE"
    public static final String MAPPING_UTENTE    = "UTENTE_WEBSERVICE"
    public static final String MAPPING_PASSWORD  = "PASSWORD_WEBSERVICE"
    public static final String GESTIONE_INTERNA  = "GESTIONE_INTERNA"

    @Override
    final List<ParametroIntegrazione> getListaParametri() {
        List<ParametroIntegrazione> parametri = [new ParametroIntegrazione(MAPPING_URL, "Url Webservice Endpoint")
                                                 , new ParametroIntegrazione(MAPPING_UTENTE, "Utente webservice")
                                                 , new ParametroIntegrazione(MAPPING_PASSWORD, "Password webservice")
                                                ]
        parametri.addAll(getListaParametriAggiuntivi())
        return parametri
    }

    /**
     * Ricarica i parametri del webservice
     */
    abstract void ricaricaParametri()

    /**
     * Esegue un test del webservice, scatena un'eccezione in caso fallisca
     */
    abstract void testWebservice()

    abstract List<ParametroIntegrazione> getListaParametriAggiuntivi()

    String getUrlWebService() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), MAPPING_URL).trim()
    }

    String getUtenteWebService() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), MAPPING_UTENTE).trim()
    }

    String getPasswordWebService() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), MAPPING_PASSWORD).trim()
    }

    boolean isVisibile() {
        return true;
    }
}