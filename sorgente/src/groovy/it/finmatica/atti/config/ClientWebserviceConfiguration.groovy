package it.finmatica.atti.config

import grails.plugin.springsecurity.SpringSecurityService
import groovy.transform.CompileStatic
import it.finmatica.atti.commons.TokenIntegrazioneService
import it.finmatica.atti.impostazioni.Ad4TokenAuthorizationPolicy
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.conservazione.JConsConfig
import it.finmatica.atti.integrazioni.contabilita.IntegrazioneContabilitaCe4Config
import it.finmatica.atti.integrazioni.contabilita.IntegrazioneContabilitaComuneModenaConfig
import it.finmatica.atti.integrazioni.jworklist.JWorklistConfig
import it.finmatica.atti.integrazioni.l190.CasaDiVetroConfig
import it.finmatica.atti.integrazioni.lettera.IntegrazioneLetteraAgsprConfig
import it.finmatica.atti.integrazioni.pec.IntegrazionePecDucdConfig
import it.finmatica.atti.integrazioni.protocollo.ProtocolloIrideStringConfig
import it.finmatica.atti.integrazioni.protocollo.ProtocolloModenaConfig
import it.finmatica.atti.integrazioniws.ads.agspr.Protocollo
import it.finmatica.atti.integrazioniws.ads.ce4.Ce4PortType
import it.finmatica.atti.integrazioniws.ads.ducd.PecSOAPImpl
import it.finmatica.atti.integrazioniws.ads.jcons.JConsService
import it.finmatica.atti.integrazioniws.ads.jworkflow.JWorklistService
import it.finmatica.atti.integrazioniws.ads.l190.PubblicaAttoService
import it.finmatica.atti.integrazioniws.ads.l190.PubblicaAttoServiceName
import it.finmatica.atti.integrazioniws.ads.smartdesktop.JWorklist
import it.finmatica.atti.integrazioniws.comunemodena.contabilita.AttiAmministrativi
import it.finmatica.atti.integrazioniws.comunemodena.fascicolo.WSFascicoloSoap
import it.finmatica.atti.integrazioniws.comunemodena.protocollo.ProtocolloSoap
import it.finmatica.atti.integrazioniws.comunemodena.titolario.ProtocolForADSPortType
import org.apache.cxf.configuration.security.AuthorizationPolicy
import org.apache.cxf.endpoint.Client
import org.apache.cxf.frontend.ClientProxy
import org.apache.cxf.interceptor.LoggingInInterceptor
import org.apache.cxf.interceptor.LoggingOutInterceptor
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.cxf.message.Message
import org.apache.cxf.transport.http.HTTPConduit
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy
import org.springframework.beans.factory.annotation.Autowire
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Lazy

import javax.xml.ws.BindingProvider
import javax.xml.ws.soap.SOAPBinding

/**
 * Created by esasdelli on 21/11/2017.
 */
@CompileStatic
@Configuration
class ClientWebserviceConfiguration {

    @Autowired
    private ProtocolloModenaConfig protocolloModenaConfig
    @Autowired
    private IntegrazioneContabilitaComuneModenaConfig integrazioneContabilitaComuneModenaConfig
    @Autowired
    private IntegrazioneLetteraAgsprConfig integrazioneLetteraAgsprConfig
    @Autowired
    private IntegrazionePecDucdConfig integrazionePecDucdConfig
    @Autowired
    private ProtocolloIrideStringConfig protocolloIrideStringConfig
    @Autowired
    private JWorklistConfig JWorklistConfig
    @Autowired
    private JConsConfig JConsConfig
    @Autowired
    private CasaDiVetroConfig casaDiVetroConfig
    @Autowired
    private IntegrazioneContabilitaCe4Config integrazioneContabilitaCe4Config

    @Bean
    @Lazy
    MultiEnteProxyFactoryBean<ProtocolloSoap> protocolloIride(SpringSecurityService springSecurityService) {
        return new MultiEnteProxyFactoryBean<ProtocolloSoap>(springSecurityService, ProtocolloSoap, {
            JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean()
            jaxWsProxyFactoryBean.setServiceClass(ProtocolloSoap.class)
            jaxWsProxyFactoryBean.setAddress(protocolloIrideStringConfig.getUrlWebservice())

            ProtocolloSoap client = (ProtocolloSoap) jaxWsProxyFactoryBean.create()

            Client cl = ClientProxy.getClient(client)
            cl.getRequestContext().put(Message.RECEIVE_TIMEOUT, 0);

            return client
        })
    }

    @Bean
    @Lazy
    MultiEnteProxyFactoryBean<ProtocolloSoap> protocolloComuneModenaServiceClient(SpringSecurityService springSecurityService) {
        return new MultiEnteProxyFactoryBean<ProtocolloSoap>(springSecurityService, ProtocolloSoap, {
            JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean()
            jaxWsProxyFactoryBean.setServiceClass(ProtocolloSoap.class)
            jaxWsProxyFactoryBean.setAddress(protocolloModenaConfig.getUrlWebService())
            ProtocolloSoap client = (ProtocolloSoap) jaxWsProxyFactoryBean.create()

            Client cl = ClientProxy.getClient(client)
            cl.getRequestContext().put(Message.RECEIVE_TIMEOUT, 0);

            return client
        })
    }

    @Bean
    @Lazy
    MultiEnteProxyFactoryBean<PecSOAPImpl> ducdPecClient(SpringSecurityService springSecurityService) {
        return new MultiEnteProxyFactoryBean<PecSOAPImpl>(springSecurityService, PecSOAPImpl, {
            AuthorizationPolicy authorizationPolicy = new AuthorizationPolicy()
            authorizationPolicy.setUserName(integrazionePecDucdConfig.getUtenteWebService())
            authorizationPolicy.setPassword(integrazionePecDucdConfig.getPasswordWebService())
            authorizationPolicy.setAuthorizationType("Basic")

            JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean()
            jaxWsProxyFactoryBean.setServiceClass(PecSOAPImpl.class)
            jaxWsProxyFactoryBean.setAddress(integrazionePecDucdConfig.getUrlWebService())

            PecSOAPImpl client = (PecSOAPImpl) jaxWsProxyFactoryBean.create()
            Client cl = ClientProxy.getClient(client)
            HTTPConduit httpConduit = (HTTPConduit) cl.getConduit()
            httpConduit.setAuthorization(authorizationPolicy)
            cl.getRequestContext().put(Message.RECEIVE_TIMEOUT, 0);

            return client
        })
    }

    @Bean
    @Lazy
    MultiEnteProxyFactoryBean<Protocollo> letteraAgsprClient(SpringSecurityService springSecurityService) {
        return new MultiEnteProxyFactoryBean<Protocollo>(springSecurityService, Protocollo, {
            AuthorizationPolicy authorizationPolicy = new AuthorizationPolicy()
            authorizationPolicy.setUserName(integrazioneLetteraAgsprConfig.getUtenteWebService())
            authorizationPolicy.setPassword(integrazioneLetteraAgsprConfig.getPasswordWebService())
            authorizationPolicy.setAuthorizationType("Basic")

            JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean()
            jaxWsProxyFactoryBean.setServiceClass(Protocollo.class)
            jaxWsProxyFactoryBean.setAddress(integrazioneLetteraAgsprConfig.getUrlWebService())

            Protocollo client = (Protocollo) jaxWsProxyFactoryBean.create()
            Client cl = ClientProxy.getClient(client)
            HTTPConduit httpConduit = (HTTPConduit) cl.getConduit()
            httpConduit.setAuthorization(authorizationPolicy)

            return client
        })
    }

    @Bean
    @Lazy
    MultiEnteProxyFactoryBean<JWorklist> smartDesktopClient(SpringSecurityService springSecurityService, TokenIntegrazioneService tokenIntegrazioneService) {
        return new MultiEnteProxyFactoryBean<JWorklist>(springSecurityService, JWorklist, {
            AuthorizationPolicy authorizationPolicy = new Ad4TokenAuthorizationPolicy(tokenIntegrazioneService, JWorklistConfig.getUtenteWebService())

            JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean()
            jaxWsProxyFactoryBean.setServiceClass(JWorklist.class)
            jaxWsProxyFactoryBean.setAddress(JWorklistConfig.getUrlWebService())

            JWorklist client = (JWorklist) jaxWsProxyFactoryBean.create()
            Client cl = ClientProxy.getClient(client)
            HTTPConduit httpConduit = (HTTPConduit) cl.getConduit()
            httpConduit.setAuthorization(authorizationPolicy)

            cl.getInInterceptors().add(new LoggingInInterceptor());
            cl.getOutInterceptors().add(new LoggingOutInterceptor());

            return client
        })
    }

    @Bean
    @Lazy
    MultiEnteProxyFactoryBean<JWorklistService> jworklistServiceClient(SpringSecurityService springSecurityService, TokenIntegrazioneService tokenIntegrazioneService) {
        return new MultiEnteProxyFactoryBean<JWorklistService>(springSecurityService, JWorklistService, {
            AuthorizationPolicy authorizationPolicy = new Ad4TokenAuthorizationPolicy(tokenIntegrazioneService, JWorklistConfig.getUtenteWebService())

            JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean()
            jaxWsProxyFactoryBean.setServiceClass(JWorklistService.class)
            jaxWsProxyFactoryBean.setAddress(JWorklistConfig.getUrlWebService())

            JWorklistService client = (JWorklistService) jaxWsProxyFactoryBean.create()
            Client cl = ClientProxy.getClient(client)
            HTTPConduit httpConduit = (HTTPConduit) cl.getConduit()
            httpConduit.setAuthorization(authorizationPolicy)

            return client
        })
    }

    @Bean
    @Lazy
    MultiEnteProxyFactoryBean<AttiAmministrativi> integrazioneContabilitaComuneModenaWebService(SpringSecurityService springSecurityService) {
        return new MultiEnteProxyFactoryBean<AttiAmministrativi>(springSecurityService, AttiAmministrativi, {
            JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean()
            jaxWsProxyFactoryBean.setServiceClass(AttiAmministrativi.class)
            jaxWsProxyFactoryBean.setAddress(integrazioneContabilitaComuneModenaConfig.getUrlWebService())
            AttiAmministrativi client = (AttiAmministrativi) jaxWsProxyFactoryBean.create()

            return client
        })
    }

    @Bean
    @Lazy
    MultiEnteProxyFactoryBean<ProtocolForADSPortType> titolarioComuneModenaServiceClient(SpringSecurityService springSecurityService) {
        return new MultiEnteProxyFactoryBean<ProtocolForADSPortType>(springSecurityService, ProtocolForADSPortType, {
            JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean()
            jaxWsProxyFactoryBean.setServiceClass(ProtocolForADSPortType.class)
            jaxWsProxyFactoryBean.setAddress(protocolloModenaConfig.getUrlWebserviceTitolario())
            ProtocolForADSPortType client = (ProtocolForADSPortType) jaxWsProxyFactoryBean.create()

            Client cl = ClientProxy.getClient(client)
            cl.getRequestContext().put(Message.RECEIVE_TIMEOUT, 0);

            return client
        })
    }

    @Bean
    @Lazy
    MultiEnteProxyFactoryBean<WSFascicoloSoap> fascicoloComuneModenaServiceClient(SpringSecurityService springSecurityService) {
        return new MultiEnteProxyFactoryBean<WSFascicoloSoap>(springSecurityService, WSFascicoloSoap, {
            JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean()
            jaxWsProxyFactoryBean.setServiceClass(WSFascicoloSoap.class)
            jaxWsProxyFactoryBean.setAddress(protocolloModenaConfig.getUrlWebserviceFascicolo())
            WSFascicoloSoap client = (WSFascicoloSoap) jaxWsProxyFactoryBean.create()

            Client cl = ClientProxy.getClient(client)
            cl.getRequestContext().put(Message.RECEIVE_TIMEOUT, 0);

            return client
        })
    }

    @Bean
    @Lazy
    MultiEnteProxyFactoryBean<JConsService> jconsServiceClient(SpringSecurityService springSecurityService, TokenIntegrazioneService tokenIntegrazioneService) {
        return new MultiEnteProxyFactoryBean<JConsService>(springSecurityService, JConsService, {
            String nominativoUtente = ((it.finmatica.ad4.autenticazione.Ad4Utente)springSecurityService.currentUser).nominativo
            AuthorizationPolicy authorizationPolicy = new Ad4TokenAuthorizationPolicy(tokenIntegrazioneService, nominativoUtente)

            JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean()
            jaxWsProxyFactoryBean.setServiceClass(JConsService.class)
            jaxWsProxyFactoryBean.setAddress(JConsConfig.getUrlWebService())

            JConsService client = (JConsService) jaxWsProxyFactoryBean.create()
            Client cl = ClientProxy.getClient(client)
            HTTPConduit httpConduit = (HTTPConduit) cl.getConduit()
            httpConduit.setAuthorization(authorizationPolicy)

            return client
        })
    }

    @Bean
    @Lazy
    MultiEnteProxyFactoryBean<PubblicaAttoService> l190ServiceClient(SpringSecurityService springSecurityService, TokenIntegrazioneService tokenIntegrazioneService) {
        return new MultiEnteProxyFactoryBean<PubblicaAttoService>(springSecurityService, PubblicaAttoService, {
            JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean()
            jaxWsProxyFactoryBean.setServiceClass(PubblicaAttoService.class)
            jaxWsProxyFactoryBean.setAddress(casaDiVetroConfig.getUrlWebService())

            PubblicaAttoService client = (PubblicaAttoService) jaxWsProxyFactoryBean.create()
            Client cl = ClientProxy.getClient(client)
            HTTPConduit http = (HTTPConduit) cl.getConduit();
            HTTPClientPolicy httpClientPolicy = http.getClient();
            if (httpClientPolicy == null) {
                httpClientPolicy = new HTTPClientPolicy();
                http.setClient(httpClientPolicy);
            }

            // timeout a dieci ore siccome può succedere che il ws di casa di vetro sia molto lento (ad es con molti file)
            httpClientPolicy.setConnectionTimeout(36_000)
            httpClientPolicy.setReceiveTimeout(36_000)

            BindingProvider binding = (BindingProvider) client;
            SOAPBinding b = (SOAPBinding) binding.getBinding();
            b.setMTOMEnabled(true);

            return client
        })
    }

    @Bean
    @Lazy
    MultiEnteProxyFactoryBean<Ce4PortType> ce4Client(SpringSecurityService springSecurityService, TokenIntegrazioneService tokenIntegrazioneService) {
        return new MultiEnteProxyFactoryBean<Ce4PortType>(springSecurityService, Ce4PortType, {

            JaxWsProxyFactoryBean jaxWsProxyFactoryBean = new JaxWsProxyFactoryBean()
            jaxWsProxyFactoryBean.setServiceClass(Ce4PortType.class)
            jaxWsProxyFactoryBean.setAddress(integrazioneContabilitaCe4Config.getUrlWebService())
            Ce4PortType client = (Ce4PortType) jaxWsProxyFactoryBean.create()

            Client cl = ClientProxy.getClient(client)
            cl.getRequestContext().put(Message.RECEIVE_TIMEOUT, 0);

            return client
        })
    }
}
