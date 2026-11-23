package it.finmatica.atti.integrazioni.protocollo

import groovy.transform.CompileStatic
import it.finmatica.atti.config.AbstractWebServiceConfig
import it.finmatica.atti.documenti.ITipologia
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.lookup.LookupTipologiaAtto
import it.finmatica.atti.integrazioni.lookup.LookupUfficio
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import it.finmatica.atti.integrazioniws.pitre.PiTreConfig
import org.apache.log4j.Logger
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@CompileStatic
@Component
@Lazy
class ProtocolloPiTreConfig extends AbstractWebServiceConfig{
    public static final String MAPPING_CATEGORIA = "PROTOCOLLO_PITRE"
    public static final String MAPPING_UNITA_USERNAME = "UNITA_USERNAME"
    public static final String MAPPING_UNITA_RUBRICA = "UNITA_RUBRICA"
    public static final String MAPPING_TIPOLOGIA_REGISTRO = "TIPOLOGIA_REGISTRO"
    public static final String MAPPING_TIPOLOGIA_TEMPLATE = "TIPOLOGIA_TEMPLATE"

    public PiTreConfig piTreConfig

    @Override
    void ricaricaParametri() {

    }

    @Override
    void testWebservice() {

    }

    @Override
    List<ParametroIntegrazione> getListaParametriAggiuntivi() {
        return [new ParametroIntegrazione("PATH_CACERT", "Path del file cacert"),
                new ParametroIntegrazione("PSWD_CACERT", "Password del file di certificazione"),
                new ParametroIntegrazione("PATH_CERTIFICATO", "Path del file di certificazione"),
                new ParametroIntegrazione("PSWD_CERTIFICATO", "Password del file di certificazione"),
                new ParametroIntegrazione("CODE_APPLICATION", "CodeApplication PITRE"),
                new ParametroIntegrazione("CODE_ADM", "CodeAdm PiTRE"),
                new ParametroIntegrazione("USERNAME", "Username PiTRE"),
                new ParametroIntegrazione("CODE_ROLE", ""),
                new ParametroIntegrazione("CODICE_REGISTRO", "Codice Registro PiTre"),
                new ParametroIntegrazione("RUBRICA_ENTE", "Rubrica dell'ente"),
                new ParametroIntegrazione("RUBRICA_PROVINCIALE", "Rubrica provinciale"),
                new ParametroIntegrazione("RUBRICA_NAZIONALE", "Rubrica nazionale"),
                new ParametroIntegrazione("SUPER_USER_FASC_TIPO", "Super Utente per Fascicolo Tipologia"),
                new ParametroIntegrazione("SUPER_CODE_ROLE_FASC_TIPO", "Super Code Role per Fascicolo Tipologia"),
                new ParametroIntegrazione(MAPPING_UNITA_USERNAME, "Unità - Ufficio Proponente", LookupUfficio.INSTANCE),
                new ParametroIntegrazione(MAPPING_UNITA_RUBRICA, "Unità - RF", LookupUfficio.INSTANCE),
                new ParametroIntegrazione(MAPPING_TIPOLOGIA_REGISTRO, "Tipologia - Registro", LookupTipologiaAtto.INSTANCE),
                new ParametroIntegrazione(MAPPING_TIPOLOGIA_TEMPLATE, "Tipologia - Template", LookupTipologiaAtto.INSTANCE),
                ]
    }

    @Override
    String getCodice() {
        return MAPPING_CATEGORIA
    }

    @Override
    String getDescrizione() {
        return  "Integrazione con Webservice PiTre"
    }

    String getPathCacert() {
       //return "C:\\Program Files\\Java\\jdk1.8.0_201\\jre\\lib\\security\\cacerts"
       return MappingIntegrazione.getValoreEsterno(getCodice(), "PATH_CACERT")
    }

    String getPswdCacert() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "PSWD_CACERT")
    }

    String getPathCertificato() {
        //return "C:\\Users\\AMarinelli\\Desktop\\xTrento\\client-t.ADScomTN-to-restpitre.tn.it.pfx"
        return MappingIntegrazione.getValoreEsterno(getCodice(), "PATH_CERTIFICATO")
    }

    String getPswdCertificato() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "PSWD_CERTIFICATO")
    }

    String getCodeApplication() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "CODE_APPLICATION")
    }

    String getCodeAdm() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "CODE_ADM")
    }

    String getUsername() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "USERNAME")
    }

    String getCodeRole() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "CODE_ROLE")
    }

    String getCodiceRegistro() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "CODICE_REGISTRO")
    }

    String getUnitaUsername(String codiceUnitaProponente) {
        return MappingIntegrazione.getValoreEsterno(getCodice(), MAPPING_UNITA_USERNAME, codiceUnitaProponente)
    }

    String getUsernameByCodiceUnita(String codiceUnitaProponente) {
        return MappingIntegrazione.getValoreEsterno(getCodice(), MAPPING_UNITA_USERNAME, codiceUnitaProponente)
    }

    String getRfeByCodiceUnita(String codiceUnitaProponente) {
        return MappingIntegrazione.getValoreEsterno(getCodice(), MAPPING_UNITA_RUBRICA, codiceUnitaProponente)
    }

    String getRegistroByTypo(String idTipoAtto) {
        return MappingIntegrazione.getValoreEsterno(getCodice(), MAPPING_TIPOLOGIA_REGISTRO, idTipoAtto)
    }

    String getTemplateByTypo(String idTipoAtto) {
        return MappingIntegrazione.getValoreEsterno(getCodice(), MAPPING_TIPOLOGIA_TEMPLATE, idTipoAtto)
    }

    String getRubricaEnte() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "RUBRICA_ENTE")
    }
    String getRubricaProvinciale() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "RUBRICA_PROVINCIALE")
    }
    String getRubricaNazionale() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "RUBRICA_NAZIONALE")
    }

    String getSuperUserFascicoloTipologia() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "SUPER_USER_FASC_TIPO")
    }
    String getSuperCodeRoleFascicoloTipologia() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "SUPER_CODE_ROLE_FASC_TIPO")
    }


    private static final Logger log = Logger.getLogger(ProtocolloPiTreConfig.class)
}
