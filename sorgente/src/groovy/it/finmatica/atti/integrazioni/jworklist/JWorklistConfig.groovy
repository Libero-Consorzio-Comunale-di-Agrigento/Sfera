package it.finmatica.atti.integrazioni.jworklist

import grails.util.Holders
import groovy.transform.CompileStatic
import it.finmatica.atti.config.AbstractWebServiceConfig
import it.finmatica.atti.config.MultiEnteProxyFactoryBean
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.SmartDesktopService
import it.finmatica.atti.integrazioni.lookup.LookupSiNo
import it.finmatica.atti.integrazioni.lookup.LookupTutti
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import it.finmatica.atti.integrazioniws.ads.jworkflow.JWorklistService
import it.finmatica.atti.integrazioniws.ads.smartdesktop.JWorklist
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@CompileStatic
@Component
@Lazy
class JWorklistConfig extends AbstractWebServiceConfig {

    @Override
    String getCodice() {
        return "JWORKLIST"
    }

    @Override
    String getDescrizione() {
        return "JWorklist"
    }

    @Override
    void ricaricaParametri() {
        MultiEnteProxyFactoryBean<JWorklistService> factoryBean = (MultiEnteProxyFactoryBean<JWorklistService>) Holders.getApplicationContext().getBean("&jworklistServiceClient")
        factoryBean.invalidateCache()
        MultiEnteProxyFactoryBean<JWorklist> factoryBean2 = (MultiEnteProxyFactoryBean<JWorklist>) Holders.getApplicationContext().getBean("&smartDesktopClient")
        factoryBean2.invalidateCache()
    }

    @Override
    void testWebservice() {
        // fa un po' schifino così ma pazienza.
        if (Impostazioni.JWORKLIST.valore == "JWorklistService") {
            JWorklistService service = (JWorklistService) Holders.getApplicationContext().getBean("jworklistServiceClient")
            service.deleteExternalTask("", "", "", "")
        } else if (Impostazioni.JWORKLIST.valore == "smartDesktopService") {
            JWorklist service = (JWorklist) Holders.getApplicationContext().getBean("smartDesktopClient")
            service.eliminaTask(-1)
        }
    }

    @Override
    List<ParametroIntegrazione> getListaParametriAggiuntivi() {
        return [new ParametroIntegrazione("JWORKLIST_URL", "Url per accedere a Sfera dalla Scrivania Virtuale")
                , new ParametroIntegrazione("JWORKLIST_ELIMINA_NOTIFICA_UO", "Eliminare le notifiche per unità?", LookupTutti.INSTANCE, LookupSiNo.INSTANCE)
                , new ParametroIntegrazione("APPLICATIVO_CHIAMANTE", "Nome dell'applicativo")]
    }

    String getUrlJWorklist() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "JWORKLIST_URL", MappingIntegrazione.VALORE_INTERNO_TUTTI, "/Atti")
    }

    boolean isEliminaNotificaPerUo() {
        return MappingIntegrazione.getValoreEsternoBoolean(getCodice(), "JWORKLIST_ELIMINA_NOTIFICA_UO", MappingIntegrazione.VALORE_INTERNO_TUTTI, "N")
    }

    boolean isEliminaNotificaPerUtente() {
        return !isEliminaNotificaPerUo()
    }

    String getApplicativoChiamante() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "APPLICATIVO_CHIAMANTE", MappingIntegrazione.VALORE_INTERNO_TUTTI, "Atti")
    }

}