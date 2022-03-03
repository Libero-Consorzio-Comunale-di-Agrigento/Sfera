package it.finmatica.atti.integrazioni.l190

import grails.util.Holders
import groovy.transform.CompileDynamic
import it.finmatica.atti.config.AbstractWebServiceConfig
import it.finmatica.atti.config.MultiEnteProxyFactoryBean
import it.finmatica.atti.documenti.IProposta
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.CasaDiVetroService
import it.finmatica.atti.integrazioni.lookup.LookupCategoria
import it.finmatica.atti.integrazioni.lookup.LookupOggettoRicorrente
import it.finmatica.atti.integrazioni.lookup.LookupTipologiaAtto
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import it.finmatica.atti.integrazioniws.ads.l190.PubblicaAttoService
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
@Lazy
class CasaDiVetroConfig extends AbstractWebServiceConfig {
    // il codice che identifica l'integrazione
    public static final String MAPPING_CATEGORIA = "CASA_DI_VETRO"

    // i codici dei campi
    public static final String MAPPING_CODICE_CATEGORIA = "CATEGORIA"
    // mappa una certa categoria della tipologia ad una certa sezione della casa di vetro
    public static final String MAPPING_CODICE_TIPOLOGIA = "TIPOLOGIA"
    // mappa una certa tipologia ad una sezione della casa di vetro
    public static final String MAPPING_CODICE_OGGETTO_RICORRENTE = "OGGETTO_RICORRENTE"
    // mappa un certo oggetto ricorrente ad una sezione della casa di vetro

    @Override
    String getCodice() {
        return MAPPING_CATEGORIA
    }

    @Override
    String getDescrizione() {
        return "Casa Di Vetro"
    }

    @Override
    void ricaricaParametri() {

    }

    @Override
    void testWebservice() {
        MultiEnteProxyFactoryBean<PubblicaAttoService> factoryBean = (MultiEnteProxyFactoryBean<PubblicaAttoService>) Holders.getApplicationContext().getBean("&l190ServiceClient")
        factoryBean.invalidateCache()
    }

    @Override
    List<ParametroIntegrazione> getListaParametriAggiuntivi() {
        return [new ParametroIntegrazione("URL_SERVER", "Url di accesso all'applicativo Casa di Vetro"),
                new ParametroIntegrazione("SEZIONE_DELIBERE", "Sezioni di default per le delibere"),
                new ParametroIntegrazione("SEZIONE_DETERMINE", "Sezioni di default per le determine"),
                new ParametroIntegrazione(MAPPING_CODICE_CATEGORIA, "Categoria", LookupCategoria.INSTANCE),
                new ParametroIntegrazione(MAPPING_CODICE_TIPOLOGIA, "Tipologia", LookupTipologiaAtto.INSTANCE),
                new ParametroIntegrazione(MAPPING_CODICE_OGGETTO_RICORRENTE, "Oggetto Ricorrente", LookupOggettoRicorrente.INSTANCE)]
    }

    String getCategoria(IProposta proposta) {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_CODICE_CATEGORIA, proposta.categoria?.codice, null)
    }

    @CompileDynamic
    String getTipologia(IProposta proposta, String valoreDefault) {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_CODICE_TIPOLOGIA, String.valueOf(proposta.tipologiaDocumento?.id), valoreDefault)
    }

    String getOggettoRicorrente(IProposta proposta, String valore) {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_CODICE_OGGETTO_RICORRENTE,
                String.valueOf(proposta.oggettoRicorrente?.id), valore)
    }

    String getUrlCasaDiVetro() {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, "URL_SERVER", MappingIntegrazione.VALORE_INTERNO_TUTTI, "/L190/atti/index.zul")
    }

    String getSezioneDelibere() {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, "SEZIONE_DELIBERE", MappingIntegrazione.VALORE_INTERNO_TUTTI, "DELIBERA")
    }

    String getSezioneDetermine() {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, "SEZIONE_DETERMINE", MappingIntegrazione.VALORE_INTERNO_TUTTI, "DETERMINA")
    }
}
