package it.finmatica.atti.integrazioni.docer

import groovy.transform.CompileStatic
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.parametri.ModuloIntegrazione
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
@Lazy
@CompileStatic
class DocErConfig implements ModuloIntegrazione {

    @Override
    boolean isVisibile() {
        return true;
    }

    @Override
    String getCodice() {
        return "DOCER"
    }

    @Override
    String getDescrizione() {
        return "Integrazione con Doc-ER"
    }

    @Override
    List<ParametroIntegrazione> getListaParametri() {
        return [new ParametroIntegrazione("", "Url per lo stato di sincronizzazione")
        , new ParametroIntegrazione("", "Utente webservice")
        , new ParametroIntegrazione("", "Url wsdl del webservice di autenticazione")
        , new ParametroIntegrazione("", "Url wsdl del webservice di interrogazione")
        , new ParametroIntegrazione("", "Url wsdl del webservice di registrazione")
        , new ParametroIntegrazione("", "Url wsdl del webservice di fascicolazione")
        , new ParametroIntegrazione("", "Url wsdl del webservice di protocollazione")
        , new ParametroIntegrazione("", "Codice dell'ente")
        , new ParametroIntegrazione("", "Codice dell'aoo")
        , new ParametroIntegrazione("", "Denominazione dell'aoo")
        ]
    }

    String getUrlStatoSincronizzaizone() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "DOCER_STATO_SINCRONIZAZZIONE_URL")
    }

    String getUtenteWebservice() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "DOCER_WS_UTENTE")
    }

    String getUrlWsdlAutenticazione() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "DOCER_AUTH_WSDL")
    }

    String getUrlWsdlRegistrazione() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "DOCER_REGISTRAZIONE_WSDL")
    }

    String getUrlWsdl() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "DOCER_WSDL")
    }

    String getUrlWsdlFascicolazione() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "DOCER_FASCICOLAZIONE_WSDL")
    }

    String getUrlWsdlProtocollazione() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "DOCER_PROTOCOLLAZIONE_WSDL")
    }

    String getCodiceEnte() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "DOCER_WS_CODICE_ENTE")
    }

    String getCodiceAoo() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "DOCER_WS_CODICE_AOO")
    }

    String getDenominazioneAoo() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "DOCER_WS_DENOMINAZIONE_AOO")
    }
}
