package it.finmatica.atti.integrazioni.contabilita

import groovy.transform.CompileDynamic
import it.finmatica.atti.config.AbstractWebServiceConfig
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.IProposta
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.lookup.LookupCategoria
import it.finmatica.atti.integrazioni.lookup.LookupOggettoRicorrente
import it.finmatica.atti.integrazioni.lookup.LookupTipologiaAtto
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
@Lazy
class IntegrazioneContabilitaCe4Config extends AbstractWebServiceConfig {

    public static final String MAPPING_CATEGORIA = "CONTABILITA_CE4"
    public static final String MAPPING_CODICE_TIPOLOGIA = "TIPOLOGIA"
    @Autowired
    IntegrazioneContabilitaCe4 integrazioneContabilitaCe4

    /*
     * Implementazione interfaccia ModuloIntegrazione
     */

    @Override
    String getCodice() {
        return MAPPING_CATEGORIA
    }

    @Override
    List<ParametroIntegrazione> getListaParametriAggiuntivi() {
        return [
                new ParametroIntegrazione(MAPPING_CODICE_TIPOLOGIA, "Tipologia", LookupTipologiaAtto.INSTANCE)]
    }

    @CompileDynamic
    String getTipologiaProposta(IProposta proposta, String valoreDefault) {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_CODICE_TIPOLOGIA, String.valueOf(proposta.tipologiaDocumento?.id), valoreDefault)
    }

    @CompileDynamic
    String getTipologiaAtto(IAtto atto, String valoreDefault) {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_CODICE_TIPOLOGIA, String.valueOf(atto.tipologiaDocumento?.id), valoreDefault)
    }

    @Override
    String getDescrizione() {
        return "Contabilità Ce4"
    }

    @Override
    void ricaricaParametri() {
    }

    @Override
    void testWebservice() {
    }

    @Override
    String getUrlWebService () {
        return MappingIntegrazione.getValoreEsterno(getCodice(), MAPPING_URL).trim().replaceAll(".wsdl","")
    }
}
