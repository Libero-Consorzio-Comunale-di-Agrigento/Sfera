package it.finmatica.atti.integrazioni.protocollo

import groovy.transform.CompileStatic
import it.finmatica.atti.config.AbstractWebServiceConfig
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@CompileStatic
@Component
@Lazy
class ProtocolloTrevisoConfig extends AbstractWebServiceConfig {

	public static final String PARAMETRO_TIPOLOGIA = "TIPOLOGIA"
	public static final String PROTOCOLLO_TREVISO = "PROTOCOLLO_TREVISO"

	@Override
	List<ParametroIntegrazione> getListaParametriAggiuntivi() {
		return [new ParametroIntegrazione("FTP_SERVER_URL", "Indirizzo del Server FTP"),
                new ParametroIntegrazione("FTP_USER", "Utente del Server FTP"),
                new ParametroIntegrazione("FTP_PASSWORD", "Password del Server FTP"),
                new ParametroIntegrazione("FTP_DIRECTORY", "Directory del Server FTP"),
                new ParametroIntegrazione("CODICE_ENTE", "Codice Ente"),
                new ParametroIntegrazione("CODICE_AOO", "Codice AOO"),
				new ParametroIntegrazione("TIPO_DOCUMENTO_DETERMINA", "Codice Tipo Documento Determina"),
				new ParametroIntegrazione("TIPO_DOCUMENTO_DELIBERA", "Codice Tipo Documento Delibera"),
				new ParametroIntegrazione(PARAMETRO_TIPOLOGIA, "Tipologia Proposta Delibera", it.finmatica.atti.integrazioni.lookup.LookupTipologiaDelibera.INSTANCE)]
	}

	@Override
	String getCodice() {
		return PROTOCOLLO_TREVISO
	}

	@Override
	String getDescrizione() {
		return "Integrazione con Webservice Treviso"
	}

    @Override
    void ricaricaParametri() {
    }

    @Override
    void testWebservice() {
    }

	String getFtpServerUrl () {
		return MappingIntegrazione.getValoreEsterno(getCodice(), "FTP_SERVER_URL")
	}

	String getFtpUser () {
		return MappingIntegrazione.getValoreEsterno(getCodice(), "FTP_USER")
	}

	String getFtpPassword () {
		return MappingIntegrazione.getValoreEsterno(getCodice(), "FTP_PASSWORD")
	}

	String getFtpDirectory () {
		return MappingIntegrazione.getValoreEsterno(getCodice(), "FTP_DIRECTORY")
	}

	String getCodiceEnte () {
		return MappingIntegrazione.getValoreEsterno(getCodice(), "CODICE_ENTE")
	}

	String getCodiceAoo () {
		return MappingIntegrazione.getValoreEsterno(getCodice(), "CODICE_AOO")
	}

	String getCodiceTipoDocumentoDetermina() {
		return MappingIntegrazione.getValoreEsterno(getCodice(), "TIPO_DOCUMENTO_DETERMINA")
	}

	String getCodiceTipoDocumentoDelibera(Long idTipologia) {
		return MappingIntegrazione.getValoreEsterno(PROTOCOLLO_TREVISO, PARAMETRO_TIPOLOGIA, Long.toString(idTipologia), MappingIntegrazione.getValoreEsterno(getCodice(), "TIPO_DOCUMENTO_DELIBERA"))
	}

}
