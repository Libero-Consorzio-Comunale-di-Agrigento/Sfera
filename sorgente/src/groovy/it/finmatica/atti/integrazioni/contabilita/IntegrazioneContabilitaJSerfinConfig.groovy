package it.finmatica.atti.integrazioni.contabilita

import groovy.transform.CompileStatic
import it.finmatica.atti.config.AbstractWebServiceConfig
import it.finmatica.atti.documenti.tipologie.ParametroTipologiaService
import it.finmatica.atti.impostazioni.MappingIntegrazione
import it.finmatica.atti.integrazioni.lookup.LookupTipologiaAtto
import it.finmatica.atti.integrazioni.parametri.ParametroIntegrazione
import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Component

@Component
@Lazy
@CompileStatic
class IntegrazioneContabilitaJSerfinConfig extends AbstractWebServiceConfig {

    public static final String MAPPING_CATEGORIA = "CONTABILITA_JSERFIN"
    public static final String RICERCA_ANAGRAFICA = "RICERCA_ANAGRAFICA"
    public static final String RICERCA_CAPITOLI_CODICE_LAVORO = "RICERCA_CAPITOLI_CODICE_LAVORO"
    public static final String RICERCA_MOVIMENTI_CODICE_GLOBALE = "RICERCA_MOVIMENTI_CODICE_GLOBALE"
    public static final String VISUALIZZA_TOTALI = "VISUALIZZA_TOTALI"
    public static final String UTENTE_SESSIONE = "UTENTE_SESSIONE"
    public static final String CODICE_SETTORE = "CODICE_SETTORE"
    public static final String CODICE_TIPO_ATTO = "CODICE_TIPO_ATTO"
    public static final String CODICE_TIPO_ATTO_DELIBERA = "CODICE_TIPO_ATTO_DELIBERA"
    public static final String VARIAZIONE_SUBIMPEGNO = "VARIAZIONE_SUBIMPEGNO"
    public static final String VISUALIZZA_TIPO_FINANZIAMENTO = "VISUALIZZA_TIPO_FINANZIAMENTO"
    public static final String VARIAZIONE_COMPETENZA_UGUALE_ANNO_IMPEGNO = "VARIAZIONE_COMPETENZA_UGUALE_ANNO_IMPEGNO"
    public static final String VISUALIZZARE_CAPITOLI_FPV = "VISUALIZZARE_CAPITOLI_FPV"
    public static final String MAPPING_TIPO_ATTO = "TIPOLOGIA_ATTO"
    public static final String CODICE_CASSA = "CODICE_CASSA"
    public static final String CODICE_CREDITO_DEBITO = "CODICE_CREDITO_DEBITO"
    public static final String V_LIVELLO_OBBLIGATORIO = "V_LIVELLO_OBBLIGATORIO"

    ParametroTipologiaService parametroTipologiaService

    /*
    url ws test trento: https://trentotest.sicraweb.it/client/services/CmnWSSGateway?wsdl
    url ws test parma: https://parmatest.sicraweb.lepida.cloud/client/services/CmnWSSGateway?wsdl
    Utente web service: @sfera_ws_test (per entrambi i clienti)
     */

    /*
     * Implementazione interfaccia ModuloIntegrazione
     */
    @Override
    String getCodice() {
        return MAPPING_CATEGORIA
    }

    @Override
    List<ParametroIntegrazione> getListaParametriAggiuntivi() {
        return [new ParametroIntegrazione(CODICE_SETTORE,                           "Codice Settore"),
                new ParametroIntegrazione(CODICE_TIPO_ATTO,                         "Codice Tipo Atto - Determina"),
                new ParametroIntegrazione(CODICE_TIPO_ATTO_DELIBERA,                "Codice Tipo Atto - Delibera"),
                new ParametroIntegrazione(CODICE_CASSA,                             "Codice Cassa"),
                new ParametroIntegrazione(CODICE_CREDITO_DEBITO,                    "Codice Credito/Debito"),
                new ParametroIntegrazione(UTENTE_SESSIONE,                          "Usare utente di sessione per le operazioni di inserimento/modifica (Y / N)"),
                new ParametroIntegrazione(RICERCA_ANAGRAFICA,                       "Ricerca in anagrafica (Y / N)"),
                new ParametroIntegrazione(VISUALIZZA_TOTALI,                        "Visualizzare la somma dei movimenti contabili (Y / N)"),
                new ParametroIntegrazione(VISUALIZZA_TIPO_FINANZIAMENTO,            "Visualizzare il tipo di finanziamento (Y / N)"),
                new ParametroIntegrazione(RICERCA_CAPITOLI_CODICE_LAVORO,           "Ricerca capitoli per codice lavoro (Y / N)"),
                new ParametroIntegrazione(RICERCA_MOVIMENTI_CODICE_GLOBALE,         "Ricerca movimenti per codice globale (Y / N)"),
                new ParametroIntegrazione(VARIAZIONE_SUBIMPEGNO,                    "Abilitare variazione dei subimpegni (Y / N)"),
                new ParametroIntegrazione(VARIAZIONE_COMPETENZA_UGUALE_ANNO_IMPEGNO,"Per le variazioni l'anno di competenza e' uguale all'anno di esercizio dell'impegno (Y / N)"),
                new ParametroIntegrazione(VISUALIZZARE_CAPITOLI_FPV,                "Visualizzare i capitoli con fondo pluriennale vincolato (Y / N)"),
                new ParametroIntegrazione(V_LIVELLO_OBBLIGATORIO,                   "V Livello obbligatorio (Y / N)"),
                new ParametroIntegrazione(MAPPING_TIPO_ATTO,                        "Tipologia", LookupTipologiaAtto.INSTANCE)]
    }

    @Override
    String getDescrizione() {
        return  "Integrazione con Webservice SICR@WEB - J-Serfin di Maggioli"
    }

    @Override
    void ricaricaParametri() {
    }

    @Override
    void testWebservice() {

    }

    boolean isUtenteSessione() {
        return MappingIntegrazione.getValoreEsternoBoolean(getCodice(), "UTENTE_SESSIONE", MappingIntegrazione.VALORE_INTERNO_TUTTI, "Y")
    }

    boolean isVisualizzaTotali() {
        return MappingIntegrazione.getValoreEsternoBoolean(getCodice(), "VISUALIZZA_TOTALI", MappingIntegrazione.VALORE_INTERNO_TUTTI, "N")
    }

    boolean isVisualizzaTipoFinanziamento() {
        return MappingIntegrazione.getValoreEsternoBoolean(getCodice(), "VISUALIZZA_TIPO_FINANZIAMENTO", MappingIntegrazione.VALORE_INTERNO_TUTTI, "N")
    }


    boolean isRicercaAnagraficaAbilitata() {
        return MappingIntegrazione.getValoreEsternoBoolean(getCodice(), "RICERCA_ANAGRAFICA", MappingIntegrazione.VALORE_INTERNO_TUTTI, "Y")
    }

    boolean isRicercaCapitoliCodiceLavoro() {
        return MappingIntegrazione.getValoreEsternoBoolean(getCodice(), "RICERCA_CAPITOLI_CODICE_LAVORO", MappingIntegrazione.VALORE_INTERNO_TUTTI, "N")
    }

    boolean isRicercaMovimentiCodiceGlobale() {
        return MappingIntegrazione.getValoreEsternoBoolean(getCodice(), "RICERCA_MOVIMENTI_CODICE_GLOBALE", MappingIntegrazione.VALORE_INTERNO_TUTTI, "N")
    }

    boolean isAbilitataVariazioneSubImpegno() {
        return MappingIntegrazione.getValoreEsternoBoolean(getCodice(), "VARIAZIONE_SUBIMPEGNO", MappingIntegrazione.VALORE_INTERNO_TUTTI, "N")
    }

    boolean isInVariazioneAnnoCompetenzaUgualeImpegno() {
        return MappingIntegrazione.getValoreEsternoBoolean(getCodice(), "VARIAZIONE_COMPETENZA_UGUALE_ANNO_IMPEGNO", MappingIntegrazione.VALORE_INTERNO_TUTTI, "N")
    }

    boolean isVisualizzareCapitoliFPV() {
        return MappingIntegrazione.getValoreEsternoBoolean(getCodice(), "VISUALIZZARE_CAPITOLI_FPV", MappingIntegrazione.VALORE_INTERNO_TUTTI, "N")
    }

    boolean isVLivelloObbligatorio() {
        return MappingIntegrazione.getValoreEsternoBoolean(getCodice(), "V_LIVELLO_OBBLIGATORIO", MappingIntegrazione.VALORE_INTERNO_TUTTI, "N")
    }

    String getCodiceSettore() {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, CODICE_SETTORE)
    }

    String getCodiceTipoAtto() {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, CODICE_TIPO_ATTO)
    }

    String getCodiceTipoAttoDelibera() {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, CODICE_TIPO_ATTO_DELIBERA)
    }

    String getCodiceCassa() {
        //il campo potrebbe non essere valorizzato e quindi non esserci il record su db.
        return MappingIntegrazione.getValoreEsterno (MAPPING_CATEGORIA, CODICE_CASSA, '*', '')
    }

    String getCodiceCreditoDebito() {
        //il campo potrebbe non essere valorizzato e quindi non esserci il record su db.
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, CODICE_CREDITO_DEBITO, '*', '')
    }

    String getTipologia(Long idTipologia) {
        return MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_TIPO_ATTO, idTipologia.toString(), "")
    }

    boolean isDeliberaIntegrata(long idTipologia) {
        String codice = MappingIntegrazione.getValoreEsterno(MAPPING_CATEGORIA, MAPPING_TIPO_ATTO, idTipologia.toString(), "")
        return (!("".equals(codice)))
    }
}