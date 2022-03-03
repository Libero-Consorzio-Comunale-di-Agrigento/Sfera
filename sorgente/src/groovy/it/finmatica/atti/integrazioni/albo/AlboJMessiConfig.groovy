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
class AlboJMessiConfig implements ModuloIntegrazione {

    public static final String INTEGRAZIONE_ALBO = "ALBO_JMESSI"
    public static final String PARAMETRO_TITOLO_PUBBLICAZIONE = "TITOLO_PUBBLICAZIONE"

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
        return "Albo JMessi"
    }

    @Override
    List<ParametroIntegrazione> getListaParametri() {
        return [new ParametroIntegrazione(PARAMETRO_TITOLO_PUBBLICAZIONE, "Titolo di Pubblicazione", LookupTipologiaDaProtocollare.INSTANCE)]
    }

    String getTitoloPubblicazione(long idTipologia) {
        return MappingIntegrazione.getValoreEsterno(INTEGRAZIONE_ALBO, PARAMETRO_TITOLO_PUBBLICAZIONE, Long.toString(idTipologia))
    }
}
