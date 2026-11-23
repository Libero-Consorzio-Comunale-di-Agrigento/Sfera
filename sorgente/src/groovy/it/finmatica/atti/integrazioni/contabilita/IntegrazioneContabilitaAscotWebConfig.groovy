package it.finmatica.atti.integrazioni.contabilita


import it.finmatica.atti.config.AbstractWebServiceConfig
import it.finmatica.atti.documenti.IProposta
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.lookup.LookupTipologiaAtto
import it.finmatica.atti.integrazioni.lookup.LookupUfficio
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
@Lazy
class IntegrazioneContabilitaAscotWebConfig extends AbstractWebServiceConfig {

    public static final String MAPPING_CATEGORIA = "CONTABILITA_ASCOT"

    // mappa una tipologia ad un codice di Ascot (la stessa tipologia avrà due codici diversi da usare quando è una proposta o un atto
    public static final String MAPPING_TIPOLOGIA_PROPOSTA = "TIPOLOGIA_PROPOSTA"
    public static final String MAPPING_TIPOLOGIA_ATTO = "TIPOLOGIA_ATTO"
    public static final String MAPPING_TIPOLOGIA_ATTO_ESECUTIVITA = "TIPOLOGIA_ATTO_ESECUTIVITA"

    // mappa un'unità ad un codice di Ascot
    public static final String MAPPING_UNITA = "UNITA_PROTOCOLLO"

    @Autowired IntegrazioneContabilitaAscotWeb integrazioneContabilitaAscotWeb

    /*
     * Implementazione interfaccia ModuloIntegrazione
     */

    @Override
    String getCodice() {
        return MAPPING_CATEGORIA
    }

    @Override
    List<ParametroIntegrazione> getListaParametriAggiuntivi() {
        return [new ParametroIntegrazione(GESTIONE_INTERNA, "Gestione Interna dei movimenti contabili"),
                new ParametroIntegrazione(MAPPING_TIPOLOGIA_PROPOSTA, "Tipologia Proposta", LookupTipologiaAtto.INSTANCE),
                new ParametroIntegrazione(MAPPING_TIPOLOGIA_ATTO, "Tipologia Atto (Adozione)", LookupTipologiaAtto.INSTANCE),
                new ParametroIntegrazione(MAPPING_TIPOLOGIA_ATTO_ESECUTIVITA, "Tipologia Atto (Esecutività)", LookupTipologiaAtto.INSTANCE),
                new ParametroIntegrazione(MAPPING_UNITA, "Unità", LookupUfficio.INSTANCE)
        ]
    }

    @Override
    String getDescrizione() {
        return "Contabilità AscotWeb"
    }

    String getTipologiaProposta(IProposta proposta, String valoreDefault) {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_TIPOLOGIA_PROPOSTA, String.valueOf(proposta.tipologiaDocumento?.id), valoreDefault)
    }

    String getTipologiaAtto(IProposta proposta, String valoreDefault) {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_TIPOLOGIA_ATTO, String.valueOf(proposta.tipologiaDocumento?.id), valoreDefault)
    }

    String getTipologiaAttoEsecutivita(IProposta proposta, String valoreDefault) {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_TIPOLOGIA_ATTO_ESECUTIVITA, String.valueOf(proposta.tipologiaDocumento?.id), valoreDefault)
    }

    String getUnitaProtocollo(So4UnitaPubb so4UnitaPubb) {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_UNITA, so4UnitaPubb.codice, '')
    }

    @Override
    void ricaricaParametri() {
    }

    @Override
    void testWebservice() {
        if (integrazioneContabilitaAscotWeb.getToken() == null){
            throw new AttiRuntimeException("Connessione Fallita");
        }
    }
}
