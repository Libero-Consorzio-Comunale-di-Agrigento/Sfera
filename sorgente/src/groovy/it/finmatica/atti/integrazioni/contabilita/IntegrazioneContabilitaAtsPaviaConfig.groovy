package it.finmatica.atti.integrazioni.contabilita

import groovy.transform.CompileStatic
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.lookup.LookupAttoAtsPavia
import it.finmatica.atti.integrazioni.lookup.LookupTipologiaAtto
import it.finmatica.atti.integrazioni.parametri.ModuloIntegrazione
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
@Lazy
@CompileStatic
class IntegrazioneContabilitaAtsPaviaConfig implements ModuloIntegrazione {

    public static final String MAPPING_CATEGORIA = "CONTABILITA_ATS_PAVIA"
    public static final String MAPPING_TIPO_ATTO = "TIPOLOGIA_ATTO"

    @Override
    boolean isVisibile() {
        return true;
    }

    @Override
    String getCodice() {
        return MAPPING_CATEGORIA;
    }

    @Override
    String getDescrizione() {
        return  "Integrazione contabilita' Sfera - ATS Pavia"
    }

    @Override
    List<ParametroIntegrazione> getListaParametri() {
        return [new ParametroIntegrazione(MAPPING_TIPO_ATTO, "Tipologie Atto", LookupTipologiaAtto.INSTANCE, LookupAttoAtsPavia.INSTANCE, true)]
    }

    boolean isAttoConImpegnoSpesa(Long idTipologia) {
        String codice = MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_TIPO_ATTO, idTipologia.toString(), "")
        return ((LookupAttoAtsPavia.ATTO_IMPEGNO_SPESA).equals(codice))
    }

    boolean isAttoConVariazione(Long idTipologia) {
        String codice = MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_TIPO_ATTO, idTipologia.toString(), "")
        return ((LookupAttoAtsPavia.ATTO_VARIAZIONE).equals(codice))
    }

    boolean isAttoTrovaCopertura(Long idTipologia) {
        String codice = MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_TIPO_ATTO, idTipologia.toString(), "")
        return ((LookupAttoAtsPavia.ATTO_TROVA_COPERTURA).equals(codice))
    }
}
