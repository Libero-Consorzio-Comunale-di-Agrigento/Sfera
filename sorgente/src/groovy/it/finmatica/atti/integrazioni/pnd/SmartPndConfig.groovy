package it.finmatica.atti.integrazioni.pnd

import it.finmatica.atti.config.AbstractWebServiceConfig
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.lookup.LookupTipoAllegatoCodiceEsterno
import it.finmatica.atti.integrazioni.lookup.LookupTipologiaDeterminaPnd
import it.finmatica.atti.integrazioni.lookup.LookupTutti
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import org.springframework.stereotype.Component

@Component
class SmartPndConfig extends AbstractWebServiceConfig {
/*
url ws:     "http://svi-ag-fin-as3:18110/smartpnd/api/v1"
utente:     "SMARTPND"
password:   "SMARTPND"
 */
    public static final String CODICE_INTEGRAZIONE = "SMARTPND"
    public static final String URL_SMARTPND = "URL_SMARTPND"
    public static final String TAG_PND = "TAG_PND"
    public static final String TIPO_COMUNICAZIONE = "TIPO_COMUNICAZIONE"
    public static final String NOTIFICATIONFEEPOLICY = "NOTIFICATIONFEEPOLICY"
    public static final String PHYSICALCOMMUNICATIONTYPE = "PHYSICALCOMMUNICATIONTYPE"
    public static final String SERVIZIO = "SERVIZIO"
    public static final String GIORNI_SCADENZA = "GIORNI SCADENZA"
    public static final String TIPOLOGIA_ALLEGATO_NOTIFICA_PND = "TIPOLOGIA_ALLEGATO_NOTIFICA_PND"
    public static final String TIPOLOGIA_ALLEGATO_NOTIFICA_DEPAG = "TIPOLOGIA_ALLEGATO__NOTIFICA_DEPAG"

    @Override
    String getCodice() {
        return CODICE_INTEGRAZIONE
    }

    @Override
    String getDescrizione() {
        return "Integrazione con SmartPND"
    }


    @Override
    void ricaricaParametri() {

    }

    @Override
    void testWebservice() {

    }

    @Override
    List<ParametroIntegrazione> getListaParametriAggiuntivi() {
        return [new ParametroIntegrazione(URL_SMARTPND, "Url applicativo SmartPnd"),
                new ParametroIntegrazione(TAG_PND, "Tag invio PND"),
                new ParametroIntegrazione(NOTIFICATIONFEEPOLICY, "Commissioni in carico (valori possibili DELIVERY_MODE, FLAT_RATE)"),
                new ParametroIntegrazione(PHYSICALCOMMUNICATIONTYPE, "Tramite (valori possibili AR_REGISTERED_LETTER, REGISTERED_LETTER_890)"),
                new ParametroIntegrazione(TIPOLOGIA_ALLEGATO_NOTIFICA_PND, "Tipologia Allegato ricevuta", LookupTutti.INSTANCE, LookupTipoAllegatoCodiceEsterno.INSTANCE),
                new ParametroIntegrazione(TIPOLOGIA_ALLEGATO_NOTIFICA_DEPAG, "Tipologia Allegato DePag", LookupTutti.INSTANCE, LookupTipoAllegatoCodiceEsterno.INSTANCE),
                new ParametroIntegrazione(TIPO_COMUNICAZIONE, "Tipo comunicazione", LookupTipologiaDeterminaPnd.INSTANCE),
                new ParametroIntegrazione(SERVIZIO, "Servizio DePag", LookupTipologiaDeterminaPnd.INSTANCE),
                new ParametroIntegrazione(GIORNI_SCADENZA, "Giorni scadenza", LookupTipologiaDeterminaPnd.INSTANCE)]
    }

    String getTagPnd() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "TAG_PND")
    }

    String getUrlSmartPnd() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "URL_SMARTPND")
    }

    String getTipoComunicazione(long idTipologia) {
            return MappingIntegrazione.getValoreEsterno(CODICE_INTEGRAZIONE, TIPO_COMUNICAZIONE, Long.toString(idTipologia), "")
    }

    String getTipoAllegatoNotificaPnd() {
        return MappingIntegrazione.getValoreEsterno (CODICE_INTEGRAZIONE, TIPOLOGIA_ALLEGATO_NOTIFICA_PND, MappingIntegrazione.VALORE_INTERNO_TUTTI, '')
    }

    String getTipoAllegatoNotificaDePag() {
        return MappingIntegrazione.getValoreEsterno (CODICE_INTEGRAZIONE, TIPOLOGIA_ALLEGATO_NOTIFICA_DEPAG, MappingIntegrazione.VALORE_INTERNO_TUTTI, '')
    }

    String getNotificationFeePolicy() {
        String ris
        String valore =  MappingIntegrazione.getValoreEsterno(getCodice(), "NOTIFICATIONFEEPOLICY")
        switch (valore) {
            case "DESTINATARIO":
                ris = "DELIVERY_MODE"
                break

            case "FISSO":
                ris = "FLAT_RATE"
                break

            default:
                ris = valore
                break
        }
        return ris
    }

    String getPhysicalCommunicationType() {
        String ris
        String valore =  MappingIntegrazione.getValoreEsterno(getCodice(), "PHYSICALCOMMUNICATIONTYPE")
        switch (valore) {
            case "RACCOMANDATA":
                ris = "AR_REGISTERED_LETTER"
                break

            case "LETTERA":
                ris = "REGISTERED_LETTER_890"
                break

            default:
                ris = valore
                break
        }
        return ris
    }

    String getServizio(long idTipologia) {
        return MappingIntegrazione.getValoreEsterno(CODICE_INTEGRAZIONE, SERVIZIO, Long.toString(idTipologia), "")
    }

    String getGiorniScadenza(long idTipologia) {
        return MappingIntegrazione.getValoreEsterno(CODICE_INTEGRAZIONE, GIORNI_SCADENZA, Long.toString(idTipologia), "")
    }
}

//class LookupCommissioni implements LookupValori {
//
//    static final LookupCommissioni INSTANCE = new LookupCommissioni()
//
//    private static final List<CodiceDescrizione> VALORI = [
//            new CodiceDescrizione("DELIVERY_MODE, ", "DESTINATARIO")
//            , new CodiceDescrizione("FLAT_RATE", "FISSO")]
//
//    private LookupCommissioni() {
//
//    }
//
//    List<CodiceDescrizione> getValori() {
//        return VALORI
//    }
//}
//
//class LookupTramite implements LookupValori {
//
//    static final LookupTramite INSTANCE = new LookupTramite()
//
//    private static final List<CodiceDescrizione> VALORI = [
//            new CodiceDescrizione("AR_REGISTERED_LETTER", "RACCOMANDATA")
//            , new CodiceDescrizione("REGISTERED_LETTER_890", "LETTERA")]
//
//    private LookupTramite() {
//
//    }
//
//    List<CodiceDescrizione> getValori() {
//        return VALORI
//    }
//}