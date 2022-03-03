package it.finmatica.atti.integrazioni.protocollo

import it.finmatica.atti.config.AbstractWebServiceConfig
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.lookup.LookupSiNo
import it.finmatica.atti.integrazioni.lookup.LookupUfficio
import it.finmatica.atti.integrazioni.lookup.LookupTipologia
import it.finmatica.atti.integrazioni.lookup.LookupTutti
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
@Lazy
class ProtocolloDOCAreaConfig extends AbstractWebServiceConfig {

    public static final MAPPING_CATEGORIA = "PROTOCOLLO_DOCAREA"
    public static final MAPPING_CODICE_UNITA = "UNITA"
    public static final MAPPING_CODICE_UNITA_DESTINATARIA = "UNITA_DESTINATARIA"
    public static final MAPPING_CODICE_CLASSIFICA = "CODICE_CLASSIFICA"
    public static final MAPPING_FASCICOLO_ANNO = "FASCICOLO_ANNO"
    public static final MAPPING_FASCICOLO_NUMERO = "FASCICOLO_NUMERO"
    public static final MAPPING_FASCICOLO_OGGETTO = "FASCICOLO_OGGETTO"
    public static final MAPPING_CODICE_APPLICATIVO = "CODICE_APPLICATIVO"

//    PROTOCOLLO_WS_UTENTE_CORRENTE		("Utilizza l'utente in sessione per la protocollazione webservice.", "PROTOCOLLA CON UTENTE CORRENTE", "Y", '<rowset><row label="Si" value="Y" /><row label="No" value="N" /></rowset>', true),
//    PROTOCOLLO_WS_UTENTE				("Utente di protocollazione webservice (se non viene usato l'utente corrente)", "UTENTE PROTOCOLLO WS", "GSWS1", null, true),
//    PROTOCOLLO_WS_PASSWORD				("Password di protocollazione webservice (se non viene usato l'utente corrente)", "PASSWORD PROTOCOLLO WS", "GSWS1", null, true),
//    PROTOCOLLO_WS_URL					("Url del wsdl del protocollo webservice", "URL PROTOCOLLO WS", "http://localhost:8080/axis/services/DOCAREAProtoSoap?wsdl", null, true),

//    PROTOCOLLO_WS_CODICE_ENTE			("Codice dell'ente protocollo webservice", "CODICE ENTE PROTOCOLLO WS", "cm_avalt", null, true),
//    PROTOCOLLO_WS_CODICE_AOO			("Codice dell'AOO protocollo webservice", "CODICE AOO PROTOCOLLO WS", "aoo-rPG", null, true),
//    PROTOCOLLO_WS_TIPO_DOCUMENTO_DETERMINA("Codice del tipo di documento dell'allegato principale", "TIPO DOCUMENTO PROTOCOLLO WS", "14", null, true),
//    PROTOCOLLO_WS_TIPO_DOCUMENTO_DELIBERA ("Codice del tipo di documento dell'allegato principale", "TIPO DOCUMENTO PROTOCOLLO WS", "14", null, true),


    @Override
    String getCodice() {
        return MAPPING_CATEGORIA
    }

    @Override
    String getDescrizione() {
        return "Protocollo DOCAREA con parametri"
    }

    @Override
    void ricaricaParametri() {

    }

    @Override
    void testWebservice() {

    }

    @Override
    List<ParametroIntegrazione> getListaParametriAggiuntivi() {
        return [new ParametroIntegrazione(MAPPING_CODICE_APPLICATIVO, "Codice Applicativo"),
                new ParametroIntegrazione("CODICE_ENTE", "Codice Ente"),
                new ParametroIntegrazione("CODICE_AOO", "Codice Aoo"),
                new ParametroIntegrazione("TIPO_DOCUMENTO_DETERMINA", "Codice Tipo Documento Determina"),
                new ParametroIntegrazione("TIPO_DOCUMENTO_DELIBERA", "Codice Tipo Documento Delibera"),
                new ParametroIntegrazione("USA_UTENTE_SESSIONE", "Usare l'utente di sessione", LookupTutti.INSTANCE, LookupSiNo.INSTANCE),
                new ParametroIntegrazione(MAPPING_CODICE_CLASSIFICA, "Classificazione", LookupTipologia.INSTANCE),
                new ParametroIntegrazione(MAPPING_CODICE_UNITA_DESTINATARIA, "Unità Destinataria", LookupTipologia.INSTANCE),
                new ParametroIntegrazione(MAPPING_CODICE_UNITA, "Unità", LookupUfficio.INSTANCE),
                new ParametroIntegrazione(MAPPING_FASCICOLO_ANNO, "Anno Fascicolo", LookupTipologia.INSTANCE),
                new ParametroIntegrazione(MAPPING_FASCICOLO_NUMERO, "Numero Fascicolo", LookupTipologia.INSTANCE),
                new ParametroIntegrazione(MAPPING_FASCICOLO_OGGETTO, "Oggetto Fascicolo", LookupTipologia.INSTANCE)
        ]
    }

    boolean isUsaUtenteCorrente () {
        return MappingIntegrazione.getValoreEsternoBoolean(getCodice(), "USA_UTENTE_SESSIONE", "Y")
    }

    String getCodiceEnte() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "CODICE_ENTE")
    }

    String getCodiceAoo() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "CODICE_AOO")
    }

    String getCodiceTipoDocumentoDetermina() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "TIPO_DOCUMENTO_DETERMINA")
    }

    String getCodiceTipoDocumentoDelibera() {
        return MappingIntegrazione.getValoreEsterno(getCodice(), "TIPO_DOCUMENTO_DELIBERA")
    }

    String getCodiceUnita(IAtto atto) {
        return MappingIntegrazione.getValoreEsterno(getCodice(), MAPPING_CODICE_UNITA, atto.getProposta().getSoggetto(TipoSoggetto.UO_PROPONENTE).unitaSo4.codice)
    }

    String getCodiceUnitaDestinataria(IAtto atto) {
        return MappingIntegrazione.getValoreEsterno(getCodice(), MAPPING_CODICE_UNITA_DESTINATARIA, atto.tipologiaDocumento.codiceEsterno)
    }

    String getCodiceClassifica(IAtto atto) {
        return MappingIntegrazione.getValoreEsterno(getCodice(), MAPPING_CODICE_CLASSIFICA, atto.tipologiaDocumento.codiceEsterno)
    }

    String getFascicoloAnno(IAtto atto) {
        return MappingIntegrazione.getValoreEsterno(getCodice(), MAPPING_FASCICOLO_ANNO, atto.tipologiaDocumento.codiceEsterno, null)
    }

    String getFascicoloNumero(IAtto atto) {
        return MappingIntegrazione.getValoreEsterno(getCodice(), MAPPING_FASCICOLO_NUMERO, atto.tipologiaDocumento.codiceEsterno, null)
    }

    String getFascicoloOggetto(IAtto atto) {
        return MappingIntegrazione.getValoreEsterno(getCodice(), MAPPING_FASCICOLO_OGGETTO, atto.tipologiaDocumento.codiceEsterno, null)
    }

    String getCodiceApplicativo(IAtto atto) {
        return MappingIntegrazione.getValoreEsterno(getCodice(), MAPPING_CODICE_APPLICATIVO, "*")
    }
}
