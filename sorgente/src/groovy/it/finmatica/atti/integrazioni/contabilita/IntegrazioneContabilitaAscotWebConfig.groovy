package it.finmatica.atti.integrazioni.contabilita

import groovyx.net.http.ContentType
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.RESTClient
import it.finmatica.atti.config.AbstractWebServiceConfig
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.integrazioni.lookup.LookupEnti
import it.finmatica.atti.integrazioni.lookup.LookupStatoEsecutivo
import it.finmatica.atti.integrazioni.lookup.LookupUfficio
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import net.sf.json.JSONObject
import org.springframework.beans.factory.annotation.Autowire
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
@Lazy
class IntegrazioneContabilitaAscotWebConfig extends AbstractWebServiceConfig {

    public static final String MAPPING_CATEGORIA = "CONTABILITA_ASCOT"
    @Autowired IntegrazioneContabilitaAscotWeb integrazioneContabilitaAscotWeb

    /*
     * Implementazione interfaccia ModuloIntegrazione
     */

    @Override
    String getCodice() {
        return MAPPING_CATEGORIA
    }

    @Override
    List<ParametroIntegrazione> getListaParametriAggiuntivi() {
        return [new ParametroIntegrazione(GESTIONE_INTERNA, "Gestione Interna dei movimenti contabili")
        ]
    }

    @Override
    String getDescrizione() {
        return "Contabilità AscotWeb"
    }

    @Override
    void ricaricaParametri() {
    }

    @Override
    void testWebservice() {
        if (integrazioneContabilitaAscotWeb.getToken() == null){
            throw new AttiRuntimeException("Connessione Fallita");
        }
    }
}
