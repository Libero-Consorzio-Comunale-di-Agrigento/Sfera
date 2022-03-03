package it.finmatica.atti.integrazioni.odg

import groovy.transform.CompileStatic
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.lookup.LookupTipologiaDelibera
import it.finmatica.atti.integrazioni.parametri.ModuloIntegrazione
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
@Lazy
@CompileStatic
class SedutaConfig implements ModuloIntegrazione {

    public static final String INTEGRAZIONE_SEDUTA = "EXPORT_SEDUTA"
    public static final String PARAMETRO_TIPOLOGIA = "TIPOLOGIA"

    @Override
    boolean isVisibile() {
        return true;
    }

    @Override
    String getCodice() {
        return INTEGRAZIONE_SEDUTA
    }

    @Override
    String getDescrizione() {
        return "Export Seduta"
    }

    @Override
    List<ParametroIntegrazione> getListaParametri() {
        return [new ParametroIntegrazione(PARAMETRO_TIPOLOGIA, "Tipologia Proposta", LookupTipologiaDelibera.INSTANCE)]
    }

    String getTipologia(long idTipologia) {
        return MappingIntegrazione.getValoreEsterno(INTEGRAZIONE_SEDUTA, PARAMETRO_TIPOLOGIA, Long.toString(idTipologia), "")
    }
}
