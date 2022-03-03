package it.finmatica.atti.integrazioni.conservazione

import grails.util.Holders
import groovy.transform.CompileStatic
import it.finmatica.atti.config.AbstractWebServiceConfig
import it.finmatica.atti.config.MultiEnteProxyFactoryBean
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import it.finmatica.atti.integrazioniws.ads.jcons.JConsService
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
@Lazy
@CompileStatic
class JConsConfig extends AbstractWebServiceConfig {

    @Override
    String getCodice() {
        return "JCONS"
    }

    @Override
    String getDescrizione() {
        return "Integrazione JCons"
    }

    @Override
    List<ParametroIntegrazione> getListaParametriAggiuntivi() {
        return [new ParametroIntegrazione("NOME_ITER", "Nome Iter Conservazione"),
                new ParametroIntegrazione("URL_SERVER", "Url Server in cui visualizzare il log di conservazione"),
                new ParametroIntegrazione("CONTEXT_PATH", "Nome del contesto in cui visualizzare il log di conservazione")]
    }

    @Override
    void ricaricaParametri() {
        MultiEnteProxyFactoryBean<JConsService> factoryBean = (MultiEnteProxyFactoryBean<JConsService>) Holders.getApplicationContext().getBean("&jconsServiceClient")
        factoryBean.invalidateCache()
    }

    @Override
    void testWebservice() {
        // fa un po' schifino così ma pazienza.
        JConsService service = (JConsService) Holders.getApplicationContext().getBean("jconsServiceClient")
        service.markDocumentsToStore([], "", "", "", "");
    }

    String getNomeIter() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "NOME_ITER", MappingIntegrazione.VALORE_INTERNO_TUTTI, "JSUITE_CONSERVAZIONE_STD")
    }

    String getUrlServer() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "URL_SERVER", MappingIntegrazione.VALORE_INTERNO_TUTTI, "http://localhost:8080")
    }

    String getContextPath() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "CONTEXT_PATH", MappingIntegrazione.VALORE_INTERNO_TUTTI, "/jdms")
    }
}
