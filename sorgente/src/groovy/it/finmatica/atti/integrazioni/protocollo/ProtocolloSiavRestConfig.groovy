package it.finmatica.atti.integrazioni.protocollo

import groovy.transform.CompileStatic
import integrazione.siav.ClientService
import integrazione.siav.ParametriSiav
import it.finmatica.affarigenerali.manager.DbManager
import it.finmatica.atti.config.AbstractWebServiceConfig
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import it.finmatica.atti.integrazioniws.siav.archiflow.login.*
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean
import org.apache.log4j.Logger
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

import javax.xml.ws.Holder

@CompileStatic
@Component
@Lazy
class ProtocolloSiavRestConfig extends AbstractWebServiceConfig {
	private static final Logger log = Logger.getLogger(ProtocolloSiavRestConfig.class)

	@Override
	List<ParametroIntegrazione> getListaParametriAggiuntivi() {
		return [new ParametroIntegrazione("ARCHIVIO", "Archivio Protocollo"),
				new ParametroIntegrazione("CODICE_ENTE", "Codice Ente"),
				new ParametroIntegrazione("CODICE_AOO", "Codice AOO"),
				new ParametroIntegrazione("DOCUMENT_TYPE_ID", "DocumentTypeId")]
	}

	@Override
	String getCodice() {
		return "PROTOCOLLO_SIAV_REST"
	}

	@Override
	String getDescrizione() {
		return "Integrazione con Webservice SIAV Rest Archiflow"
	}

	@Override
	void ricaricaParametri() {
	}

	@Override
	void testWebservice() {
		log.info("Test Web Service REST")
		ProtocolloSiavRest psr = new ProtocolloSiavRest()
		integrazione.siav.classes.SessionInfo sessionInfo=  psr.login()
		psr.logout(sessionInfo)
		log.info("Test Web Service REST Fine")
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

	String getDocumentTypeId() {
		return MappingIntegrazione.getValoreEsterno(getCodice(), "DOCUMENT_TYPE_ID")
	}
}
