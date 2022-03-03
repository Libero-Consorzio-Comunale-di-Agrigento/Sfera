package it.finmatica.atti.integrazioni.protocollo

import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.lookup.LookupEnti
import it.finmatica.atti.integrazioni.lookup.LookupTipologiaAtto
import it.finmatica.atti.integrazioni.lookup.LookupUfficio
import it.finmatica.atti.integrazioni.parametri.ModuloIntegrazione
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

/**
 * Integrazione del protocollo con Modena.
 */
@Component
@Lazy
class ProtocolloIrideStringConfig implements ModuloIntegrazione {

    public static final String CODICE_INTEGRAZIONE = "PROTOCOLLO_IRIDE"
    public static final String PARAMETRO_UTENTE = "UTENTE"
    public static final String PARAMETRO_URL = "URL"
    public static final String PARAMETRO_ENTE = "ENTE"
    public static final String PARAMETRO_AOO = "AOO"
    public static final String PARAMETRO_UNITA_PROTOCOLLO = "UNITA_PROTOCOLLO"
    public static final String PARAMETRO_UNITA_INCARICOA = "UNITA_INCARICOA"
    public static final String PARAMETRO_TIPO_DOCUMENTO = "TIPO_DOCUMENTO"
    public static final String PARAMETRO_RUOLO = "RUOLO"
    public static final String PARAMETRO_CODICE_CLASSIFICA = "CODICE_CLASSIFICA"
    public static final String PARAMETRO_PREFISSO_OGGETTO = "PREFISSO_OGGETTO"

    @Override
    boolean isVisibile() {
        return true;
    }

    @Override
    String getCodice() {
        return CODICE_INTEGRAZIONE
    }

    @Override
    String getDescrizione() {
        return "Protocollo via Iride"
    }

    @Override
    List<ParametroIntegrazione> getListaParametri() {
        return [
                new ParametroIntegrazione(PARAMETRO_UTENTE, "Utente Webservice"),
                new ParametroIntegrazione(PARAMETRO_URL, "URL Webservice"),
                new ParametroIntegrazione(PARAMETRO_RUOLO, "Ruolo"),
                new ParametroIntegrazione(PARAMETRO_AOO, "Codice AOO"),
                new ParametroIntegrazione(PARAMETRO_ENTE, "Codice Ente", LookupEnti.INSTANCE),
                new ParametroIntegrazione(PARAMETRO_UNITA_PROTOCOLLO, "Unità Protocollazione", LookupUfficio.INSTANCE),
                new ParametroIntegrazione(PARAMETRO_UNITA_INCARICOA, "Unità In Carico", LookupUfficio.INSTANCE),
                new ParametroIntegrazione(PARAMETRO_TIPO_DOCUMENTO, "Tipo Documento", LookupTipologiaAtto.INSTANCE),
                new ParametroIntegrazione(PARAMETRO_CODICE_CLASSIFICA, "Codice Classifica", LookupTipologiaAtto.INSTANCE),
                new ParametroIntegrazione(PARAMETRO_PREFISSO_OGGETTO, "Prefisso Oggetto", LookupTipologiaAtto.INSTANCE)
        ]
    }

    String getUtenteWebservice() {
        return MappingIntegrazione.getValoreEsterno(CODICE_INTEGRAZIONE, PARAMETRO_UTENTE).trim()
    }

    String getUrlWebservice() {
        return MappingIntegrazione.getValoreEsterno(CODICE_INTEGRAZIONE, PARAMETRO_URL).trim()
    }

    String getEnte(String codiceEnte) {
        return MappingIntegrazione.getValoreEsterno(CODICE_INTEGRAZIONE, PARAMETRO_ENTE, codiceEnte)
    }

    String getAoo() {
        return MappingIntegrazione.getValoreEsterno(CODICE_INTEGRAZIONE, PARAMETRO_AOO)
    }

    String getRuolo() {
        return MappingIntegrazione.getValoreEsterno(CODICE_INTEGRAZIONE, PARAMETRO_RUOLO)
    }

    String getUnitaProtocollo(So4UnitaPubb so4UnitaPubb) {
        return MappingIntegrazione.getValoreEsterno(CODICE_INTEGRAZIONE, PARAMETRO_UNITA_PROTOCOLLO, so4UnitaPubb.codice)
    }

    String getUnitaInCaricoA(So4UnitaPubb so4UnitaPubb) {
        return MappingIntegrazione.getValoreEsterno(CODICE_INTEGRAZIONE, PARAMETRO_UNITA_INCARICOA, so4UnitaPubb.codice, getUnitaProtocollo(so4UnitaPubb))
    }

    String getTipoDocumento(long idTipologia) {
        return MappingIntegrazione.getValoreEsterno(CODICE_INTEGRAZIONE, PARAMETRO_TIPO_DOCUMENTO, Long.toString(idTipologia))
    }

    String getCodiceClassifica(long idTipologia) {
        return MappingIntegrazione.getValoreEsterno(CODICE_INTEGRAZIONE, PARAMETRO_CODICE_CLASSIFICA, Long.toString(idTipologia))
    }

    String getPrefissoOggetto(long idTipologia) {
        return MappingIntegrazione.getValoreEsterno(CODICE_INTEGRAZIONE, PARAMETRO_PREFISSO_OGGETTO, Long.toString(idTipologia), "")
    }
}
