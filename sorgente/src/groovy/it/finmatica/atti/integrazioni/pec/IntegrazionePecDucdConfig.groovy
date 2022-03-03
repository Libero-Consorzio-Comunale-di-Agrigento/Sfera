package it.finmatica.atti.integrazioni.pec

import grails.util.Holders
import groovy.transform.CompileStatic
import it.finmatica.atti.config.AbstractWebServiceConfig
import it.finmatica.atti.config.MultiEnteProxyFactoryBean
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import it.finmatica.atti.integrazioniws.ads.ducd.ParametriIngresso
import it.finmatica.atti.integrazioniws.ads.ducd.PecSOAPImpl
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

/**
 * Created by esasdelli on 08/11/2017.
 */
@Component
@Lazy
@CompileStatic
class IntegrazionePecDucdConfig extends AbstractWebServiceConfig {

    public static final String MAPPING_CATEGORIA = "PEC_DUCD"

    @Override
    String getCodice() {
        return MAPPING_CATEGORIA
    }

    @Override
    String getDescrizione() {
        return "Integrazione Invio PEC"
    }

    @Override
    List<ParametroIntegrazione> getListaParametriAggiuntivi() {
        return []
    }

    @Override
    void ricaricaParametri() {
        MultiEnteProxyFactoryBean<PecSOAPImpl> factoryBean = (MultiEnteProxyFactoryBean<PecSOAPImpl>) Holders.getApplicationContext().getBean("&ducdPecClient")
        factoryBean.invalidateCache()
    }

    @Override
    void testWebservice() {
        // fa un po' schifino così ma pazienza.
        PecSOAPImpl service = (PecSOAPImpl) Holders.getApplicationContext().getBean("ducdPecClient")
        ParametriIngresso p = new ParametriIngresso()

        p.numero = "-1"
        p.anno = "1981"
        p.idDocumento = -1
        p.listaDestinatari = "test@test.it"
        p.utenteCreazione = getUtenteWebService()
        p.tipoRegistro = "TEST"
        service.invioPec(p)
    }
}
