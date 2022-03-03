package it.finmatica.atti.integrazioni.protocollo

import grails.util.Holders
import groovy.transform.CompileStatic
import it.finmatica.atti.config.AbstractWebServiceConfig
import it.finmatica.atti.config.MultiEnteProxyFactoryBean
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.lookup.LookupTipoDocumento
import it.finmatica.atti.integrazioni.lookup.LookupUfficio
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import it.finmatica.atti.integrazioniws.comunemodena.fascicolo.WSFascicoloSoap
import it.finmatica.atti.integrazioniws.comunemodena.protocollo.ProtocolloSoap
import it.finmatica.atti.integrazioniws.comunemodena.titolario.ProtocolForADSPortType
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

/**
 * Integrazione del protocollo con Modena.
 */
@CompileStatic
@Component
@Lazy
class ProtocolloModenaConfig extends AbstractWebServiceConfig {

    public static final String MAPPING_CATEGORIA = "PROTOCOLLO_MODENA"
    public static final String MAPPING_CODICE_UNITA = "CODICE_UNITA"
    public static final String MAPPING_CODICE_TIPO_DOCUMENTO = "TIPO_DOCUMENTO"

    @Override
    String getCodice() {
        return MAPPING_CATEGORIA
    }

    @Override
    List<ParametroIntegrazione> getListaParametriAggiuntivi() {
        return [new ParametroIntegrazione(MAPPING_CODICE_UNITA, "Unità", LookupUfficio.INSTANCE),
                new ParametroIntegrazione(MAPPING_CODICE_TIPO_DOCUMENTO, "Tipo Documento", LookupTipoDocumento.INSTANCE),
                new ParametroIntegrazione("URL_WEBSERVICE_TITOLARIO", "Url Webservice Titolario"),
                new ParametroIntegrazione("URL_WEBSERVICE_FASCICOLO", "Url Webservice Fascicolo")]
    }

    @Override
    String getDescrizione() {
        return "Protocollo Modena"
    }

    @Override
    void ricaricaParametri() {
        MultiEnteProxyFactoryBean<ProtocolloSoap> factoryBean = (MultiEnteProxyFactoryBean<ProtocolloSoap>) Holders.getApplicationContext().getBean("&protocolloComuneModenaServiceClient")
        factoryBean.invalidateCache()
        MultiEnteProxyFactoryBean<ProtocolForADSPortType> factoryBean2 = (MultiEnteProxyFactoryBean<ProtocolForADSPortType>) Holders.getApplicationContext().getBean("&fascicoloComuneModenaServiceClient")
        factoryBean2.invalidateCache()
        MultiEnteProxyFactoryBean<WSFascicoloSoap> factoryBean3 = (MultiEnteProxyFactoryBean<WSFascicoloSoap>) Holders.getApplicationContext().getBean("&titolarioComuneModenaServiceClient")
        factoryBean3.invalidateCache()
    }

    @Override
    void testWebservice() {
        // fa un po' schifino così ma pazienza.
        ProtocolloSoap service = (ProtocolloSoap) Holders.getApplicationContext().getBean("protocolloComuneModenaServiceClient")
        service.login("", "", "")
        ProtocolForADSPortType service2 = (ProtocolForADSPortType) Holders.getApplicationContext().getBean("titolarioComuneModenaServiceClient")
        service2.getClassifications("nessuna", "nessuna", "nessuna")
        WSFascicoloSoap service3 = (WSFascicoloSoap) Holders.getApplicationContext().getBean("fascicoloComuneModenaServiceClient")
        service3.leggiFascicoloString("", "", "", "", "", "", "")
    }

    String getCodiceUnitaProponente(String codiceUnitaProponente) {
        return MappingIntegrazione.getValoreEsterno(getCodice(), MAPPING_CODICE_UNITA, codiceUnitaProponente)
    }

    String getUrlWebserviceTitolario() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "URL_WEBSERVICE_TITOLARIO")
    }

    String getUrlWebserviceFascicolo() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "URL_WEBSERVICE_FASCICOLO")
    }
}
