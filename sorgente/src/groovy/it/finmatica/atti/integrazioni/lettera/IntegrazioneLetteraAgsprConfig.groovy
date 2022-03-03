package it.finmatica.atti.integrazioni.lettera

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import groovy.transform.CompileStatic
import it.finmatica.atti.config.AbstractWebServiceConfig
import it.finmatica.atti.config.MultiEnteProxyFactoryBean
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.lookup.LookupCommissioniStampe
import it.finmatica.atti.integrazioni.lookup.LookupEnti
import it.finmatica.atti.integrazioni.lookup.LookupSiNo
import it.finmatica.atti.integrazioni.lookup.LookupTestoOStampaUnica
import it.finmatica.atti.integrazioni.lookup.LookupTipologiaAtto
import it.finmatica.atti.integrazioni.lookup.LookupTutti
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import it.finmatica.atti.integrazioniws.ads.agspr.Protocollo
import it.finmatica.atti.odg.SedutaStampa
import org.springframework.stereotype.Component

/**
 * Created by esasdelli on 07/11/2017.
 */
@Component
@CompileStatic
class IntegrazioneLetteraAgsprConfig extends AbstractWebServiceConfig {

    public static final String MAPPING_CATEGORIA        = "LETTERA_AGSPR"
    public static final String MAPPING_ENTE             = "ENTE"
    public static final String MAPPING_TIPO_PROTOCOLLO  = "TIPO_PROTOCOLLO"
    public static final String MAPPING_TIPOLOGIA        = "TIPOLOGIA"

    @Override
    String getCodice() {
        return MAPPING_CATEGORIA
    }

    @Override
    String getDescrizione() {
        return "Integrazione Lettera AGSPR"
    }

    @Override
    List<ParametroIntegrazione> getListaParametriAggiuntivi() {
        return [new ParametroIntegrazione("URL_SERVER", "Url di accesso all'applicativo Lettera"),
                new ParametroIntegrazione("CODICE_DELIBERE", "Codice di default per le delibere"),
                new ParametroIntegrazione("CODICE_DETERMINE", "Codice di default per le determine"),
                new ParametroIntegrazione("TESTO_STAMPAUNICA", "Inserisci il testo o la stampa unica?", LookupTutti.INSTANCE, LookupTestoOStampaUnica.INSTANCE),
                new ParametroIntegrazione("INSERISCI_ALLEGATI", "Inserisci gli allegati?", LookupTutti.INSTANCE, LookupSiNo.INSTANCE),
                new ParametroIntegrazione("INSERISCI_ALLEGATI_NON_PUBBLICATI", "Inserisci gli allegati non pubblicati all'Albo?", LookupTutti.INSTANCE, LookupSiNo.INSTANCE),
                new ParametroIntegrazione("INSERISCI_VISTI", "Inserisci i visti/pareri?", LookupTutti.INSTANCE, LookupSiNo.INSTANCE),
                new ParametroIntegrazione("INSERISCI_ALLEGATI_VISTI", "Inserisci gli allegati dei visti?", LookupTutti.INSTANCE, LookupSiNo.INSTANCE),
                new ParametroIntegrazione("INSERISCI_CERTIFICATI", "Inserisci i certificati?", LookupTutti.INSTANCE, LookupSiNo.INSTANCE),
                new ParametroIntegrazione(MAPPING_ENTE, "Codice Ente", LookupEnti.INSTANCE),
                new ParametroIntegrazione(MAPPING_TIPO_PROTOCOLLO, "Codice Commissione", LookupCommissioniStampe.INSTANCE)]
    }

    @Override
    void ricaricaParametri() {
        MultiEnteProxyFactoryBean<Protocollo> factoryBean = (MultiEnteProxyFactoryBean<Protocollo>) Holders.getApplicationContext().getBean("&letteraAgsprClient")
        factoryBean.invalidateCache()
    }

    @Override
    void testWebservice() {
        // fa un po' schifino così ma pazienza.
        Protocollo service = (Protocollo) Holders.getApplicationContext().getBean("letteraAgsprClient")
        service.creaLettera(null, -1, null)
    }

    String getCodiceTipologiaProtocollo (SedutaStampa sedutaStampa) {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_TIPO_PROTOCOLLO, sedutaStampa.commissioneStampa.id.toString()).trim()
    }

    long getCodiceEnte(String ente) {
        return (long)MappingIntegrazione.getValoreEsternoInt(MAPPING_CATEGORIA, MAPPING_ENTE, ente)
    }

    String getUrlLettera() {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, "URL_SERVER", MappingIntegrazione.VALORE_INTERNO_TUTTI, "${Impostazioni.URL_SERVER_GDM.valore}/Protocollo/standalone.zul")
    }

    String getCodiceDelibere() {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, "CODICE_DELIBERE", MappingIntegrazione.VALORE_INTERNO_TUTTI, "")
    }

    String getCodiceDetermine() {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, "CODICE_DETERMINE", MappingIntegrazione.VALORE_INTERNO_TUTTI, "")
    }

    String getTestoOStampaUnica() {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, "TESTO_STAMPAUNICA", MappingIntegrazione.VALORE_INTERNO_TUTTI, "TESTO")
    }
    boolean isAllegatiAbilitati() {
        return MappingIntegrazione.getValoreEsternoBoolean(getCodice(), "INSERISCI_ALLEGATI", MappingIntegrazione.VALORE_INTERNO_TUTTI, "N")
    }

    boolean isAllegatiNonPubblicatiAbilitati() {
        return MappingIntegrazione.getValoreEsternoBoolean(getCodice(), "INSERISCI_ALLEGATI_NON_PUBBLICATI", MappingIntegrazione.VALORE_INTERNO_TUTTI, "N")
    }

    boolean isVistiAbilitati() {
        return MappingIntegrazione.getValoreEsternoBoolean(getCodice(), "INSERISCI_VISTI", MappingIntegrazione.VALORE_INTERNO_TUTTI, "N")
    }

    boolean isAllegatiVistiAbilitati() {
        return MappingIntegrazione.getValoreEsternoBoolean(getCodice(), "INSERISCI_ALLEGATI_VISTI", MappingIntegrazione.VALORE_INTERNO_TUTTI, "N")
    }

    boolean isCertificatiAbilitati() {
        return MappingIntegrazione.getValoreEsternoBoolean(getCodice(), "INSERISCI_CERTIFICATI", MappingIntegrazione.VALORE_INTERNO_TUTTI, "N")
    }

}
