package it.finmatica.atti.integrazioni.protocollo

import groovy.transform.CompileStatic
import it.finmatica.atti.config.AbstractWebServiceConfig
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import it.finmatica.atti.integrazioniws.siav.archiflow.login.ConnectionInfo
import it.finmatica.atti.integrazioniws.siav.archiflow.login.ILoginServiceContract
import it.finmatica.atti.integrazioniws.siav.archiflow.login.Language
import it.finmatica.atti.integrazioniws.siav.archiflow.login.ResultInfo
import it.finmatica.atti.integrazioniws.siav.archiflow.login.SessionInfo
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.log4j.Logger
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

import javax.xml.ws.Holder

@CompileStatic
@Component
@Lazy
class ProtocolloSiavConfig extends AbstractWebServiceConfig {
	private static final Logger log = Logger.getLogger(ProtocolloSiavConfig.class)

	@Override
	List<ParametroIntegrazione> getListaParametriAggiuntivi() {
		return [new ParametroIntegrazione("ARCHIVIO", "Archivio Protocollo"),
				new ParametroIntegrazione("CODICE_ENTE", "Codice Ente"),
                new ParametroIntegrazione("CODICE_AOO", "Codice AOO")]
	}

	@Override
	String getCodice() {
		return "PROTOCOLLO_SIAV"
	}

	@Override
	String getDescrizione() {
		return "Integrazione con Webservice SIAV Archiflow"
	}

    @Override
    void ricaricaParametri() {
    }

    @Override
    void testWebservice() {
		log.info("Test Web Service")
		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
		factory.setServiceClass(ILoginServiceContract.class);
		factory.setAddress(getUrlWebService() + "/Login.svc");
		def loginServiceContract = (ILoginServiceContract) factory.create();

		ConnectionInfo connectionInfo = new ConnectionInfo()
		connectionInfo.dateFormat = "dd/mm/yyyy"
		connectionInfo.setLanguage(Language.ITALIAN)
		connectionInfo.workflowDomain= "SIAV"
		Holder<ResultInfo> resultInfo = new Holder<ResultInfo>();
		Holder<SessionInfo> sessionInfo = new Holder<SessionInfo>();
		loginServiceContract.login(getUtenteWebService(), getPasswordWebService(), connectionInfo, resultInfo, sessionInfo);
		String idConn = sessionInfo.value.getSessionId()
		loginServiceContract.logout(sessionInfo.value);
    }

	String getCodiceEnte () {
		return MappingIntegrazione.getValoreEsterno(getCodice(), "CODICE_ENTE")
	}

	String getCodiceAoo () {
		return MappingIntegrazione.getValoreEsterno(getCodice(), "CODICE_AOO")
	}

	String getArchivio () {
		return MappingIntegrazione.getValoreEsterno(getCodice(), "ARCHIVIO")
	}
}
