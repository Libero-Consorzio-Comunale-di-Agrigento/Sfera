package it.finmatica.atti.integrazioni.protocollo

import groovy.transform.CompileStatic
import it.finmatica.atti.config.AbstractWebServiceConfig
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.lookup.LookupSiNo
import it.finmatica.atti.integrazioni.lookup.LookupTutti
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import org.apache.log4j.Logger
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@CompileStatic
@Component
@Lazy
class ProtocolloAurigaConfig extends AbstractWebServiceConfig {
	public static final String MAPPING_CATEGORIA = "PROTOCOLLO_AURIGA"

	@Override
	String getCodice() {
		return MAPPING_CATEGORIA
	}

	/*
	PARAMETRI AMBIENTE TEST PARMA
	codiceApplicazione: AURIGA
	username: AURIGATEST
	password: Settembre2020?!
	ws protocollazione: https://aurigatest.comune.parma.it/AurigaBusiness/soap/WSAddUd
	ws classifiche: https://aurigatest.comune.parma.it/AurigaBusiness/soap/WSTrovaClassifTitolario
	ws fascicoli: https://aurigatest.comune.parma.it/AurigaBusiness/soap/WSTrovaDocFolder
	*/
	@Override
	List<ParametroIntegrazione> getListaParametriAggiuntivi() {
		return [new ParametroIntegrazione("URL_WEBSERVICE_TITOLARIO", "Url Webservice Titolario"),
				new ParametroIntegrazione("URL_WEBSERVICE_FASCICOLO", "Url Webservice Fascicolo"),
				new ParametroIntegrazione("CODICE_APPLICAZIONE", "Codice Applicazione"),
				new ParametroIntegrazione("CATEGORIA_DOCUMENTI_DA_PROTOCOLARE", "Categoria documenti da protocollare"),
				new ParametroIntegrazione("CATEGORIA_DOCUMENTI_DA_NON_PROTOCOLARE", "Categoria documenti da non protocollare"),
				new ParametroIntegrazione("USARE_PASSWORD_UNICA", "Usare la stessa password per tutti gli utenti ?", LookupTutti.INSTANCE, LookupSiNo.INSTANCE),
				new ParametroIntegrazione("PASSWORD_UNICA", "Password unica")]
	}

	@Override
	String getDescrizione() {
		return  "Integrazione con Webservice e-Grammata - Auriga di Engineering"
	}

	@Override
	void ricaricaParametri() {
	}

	@Override
	void testWebservice() {
	}

	String getUrlWebserviceTitolario() {
		return MappingIntegrazione.getValoreEsterno(getCodice(), "URL_WEBSERVICE_TITOLARIO")
	}

	String getUrlWebserviceFascicolo() {
		return MappingIntegrazione.getValoreEsterno(getCodice(), "URL_WEBSERVICE_FASCICOLO")
	}

	String getCodiceApplicazione() {
		return MappingIntegrazione.getValoreEsterno(getCodice(), "CODICE_APPLICAZIONE")
	}

	String getCategoriaProtocollare() {
		return MappingIntegrazione.getValoreEsterno(getCodice(), "CATEGORIA_DOCUMENTI_DA_PROTOCOLARE")
	}

	String getCategoriaNonProtocollare() {
		return MappingIntegrazione.getValoreEsterno(getCodice(), "CATEGORIA_DOCUMENTI_DA_NON_PROTOCOLARE")
	}

	boolean isPasswordUnica() {
		return (MappingIntegrazione.getValoreEsternoBoolean(MAPPING_CATEGORIA, "USARE_PASSWORD_UNICA", MappingIntegrazione.VALORE_INTERNO_TUTTI, "N"))
	}

	String getPasswordUnica() {
		return MappingIntegrazione.getValoreEsterno(getCodice(), "PASSWORD_UNICA")
	}

	private static final Logger log = Logger.getLogger(ProtocolloAurigaConfig.class)
}
