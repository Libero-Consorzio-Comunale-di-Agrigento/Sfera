package it.finmatica.atti.integrazioni.albo

import groovy.transform.CompileStatic
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.lookup.LookupTipologiaDaProtocollare
import it.finmatica.atti.integrazioni.parametri.ModuloIntegrazione
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
@Lazy
@CompileStatic
class AlboEsternoConfig implements ModuloIntegrazione {

    public static final String INTEGRAZIONE_ALBO    = "ALBO_ESTERNO"
    public static final String ALBO_ESTERNO_USER    = "ALBO_ESTERNO_USER"
    public static final String ALBO_ESTERNO_DSN     = "ALBO_ESTERNO_DSN"
    public static final String ALBO_ESTERNO_PWD     = "ALBO_ESTERNO_PWD"

    @Override
    boolean isVisibile() {
        return true;
    }

    @Override
    String getCodice() {
        return INTEGRAZIONE_ALBO
    }

    @Override
    String getDescrizione() {
        return "Albo Esterno"
    }

    @Override
    List<ParametroIntegrazione> getListaParametri() {
        return [new ParametroIntegrazione(ALBO_ESTERNO_DSN, "DNS esterno"), new ParametroIntegrazione(ALBO_ESTERNO_USER, "Utente dns esterno"), new ParametroIntegrazione(ALBO_ESTERNO_PWD, "Password dns esterno")]
    }

    public String getDNS() {
        return MappingIntegrazione.getValoreEsterno(INTEGRAZIONE_ALBO, ALBO_ESTERNO_DSN)
    }

    public String getUser() {
        return MappingIntegrazione.getValoreEsterno(INTEGRAZIONE_ALBO, ALBO_ESTERNO_USER)
    }

    public String getPassword() {
        return MappingIntegrazione.getValoreEsterno(INTEGRAZIONE_ALBO, ALBO_ESTERNO_PWD)
    }

    public String getDriver() {
        return "oracle.jdbc.driver.OracleDriver"
    }


}
