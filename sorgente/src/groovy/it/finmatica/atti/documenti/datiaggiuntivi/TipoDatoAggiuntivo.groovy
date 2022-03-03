package it.finmatica.atti.documenti

import grails.compiler.GrailsCompileStatic
import it.finmatica.atti.dizionari.TipoDatoAggiuntivoValore
import it.finmatica.atti.dto.documenti.DatoAggiuntivoDTO
import it.finmatica.atti.impostazioni.Impostazioni

/**
 *
 * Per aggiungere nuovi dati aggiuntivi, è possibile definirli nella lista DATI_AGGIUNTIVI
 *
 * Created by esasdelli on 11/09/2017.
 */
@GrailsCompileStatic
class TipoDatoAggiuntivo {

    public static final String RIFLESSI_CONTABILI              = "RIFLESSI_CONTABILI"
    public static final String PUBBLICAZIONE_PORTALE_REGIONALE = "PUBBLICAZIONE_PORTALE_REGIONALE"
    public static final String PUBBLICAZIONE_TRASPARENZA       = "PUBBLICAZIONE_TRASPARENZA"
    public static final String PUBBLICAZIONE_BURC              = "PUBBLICAZIONE_BURC"
    public static final String ASSENZA_DOPPIA_FIRMA            = "ASSENZA_DOPPIA_FIRMA"
    public static final String MOTIVAZIONE_ASSENZA_DOPPIA_FIRMA= "MOTIVAZIONE_ASSENZA_DOPPIA_FIRMA"
    public static final String PROTOCOLLO_SIAV_STRUTTURA       = "PROTOCOLLO_SIAV_STRUTTURA"
    public static final String ESTRATTO                         = "ESTRATTO"
    public static final String DATA_ISTANZA                     = "DATA_ISTANZA"
    public static final String COMMISSIONE_CONSILIARE           = "COMMISSIONE_CONSILIARE"
    public static final String NOTE_CONVOCAZIONE                = "NOTE_CONVOCAZIONE"
    public static final String CONTABILITA_ASCOT                = "CONTABILITA_ASCOT"
    public static final String CUP                              = "CUP"


    private static final List<TipoDatoAggiuntivo> DATI_AGGIUNTIVI = [
            // http://svi-redmine/issues/16225
            new TipoDatoAggiuntivo(codice: RIFLESSI_CONTABILI, descrizione: "Riflessi Contabili",
                                   tipiDocumento: [Determina.TIPO_OGGETTO]),

            // http://svi-redmine/issues/23393
            new TipoDatoAggiuntivo(codice: PUBBLICAZIONE_PORTALE_REGIONALE, descrizione: "Portale Regionale",
                                   tipiDocumento: [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO], valoreDefault: "Y"),
            new TipoDatoAggiuntivo(codice: PUBBLICAZIONE_TRASPARENZA, descrizione: "Trasparenza",
                                   tipiDocumento: [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO], valoreDefault: "Y"),
            new TipoDatoAggiuntivo(codice: PUBBLICAZIONE_BURC, descrizione: "Burc",
                                   tipiDocumento: [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO], valoreDefault: "Y"),

            // http://svi-redmine/issues/23396
            new TipoDatoAggiuntivo(codice: ASSENZA_DOPPIA_FIRMA, descrizione: "Doppia Firma Assente",
                                   tipiDocumento: [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO]),
            new TipoDatoAggiuntivo(codice: MOTIVAZIONE_ASSENZA_DOPPIA_FIRMA, descrizione: "Motivazione Doppia Firma Assente",
                    tipiDocumento: [Determina.TIPO_OGGETTO, Delibera.TIPO_OGGETTO]),
            new TipoDatoAggiuntivo(codice: PROTOCOLLO_SIAV_STRUTTURA, descrizione: "Settore Protocollo",
                    tipiDocumento: [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO]),
            new TipoDatoAggiuntivo(codice: ESTRATTO, descrizione: "Estratto",
                    tipiDocumento: [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO]),
            new TipoDatoAggiuntivo(codice: DATA_ISTANZA, descrizione: "Data di Presentazione Istanza",
                    tipiDocumento: [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO]),
            new TipoDatoAggiuntivo(codice: COMMISSIONE_CONSILIARE, descrizione: "Commissioni Consiliari",
                    tipiDocumento: [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO]),
            new TipoDatoAggiuntivo(codice: NOTE_CONVOCAZIONE, descrizione: "Note per Convocazione",
                    tipiDocumento: [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO]),
            new TipoDatoAggiuntivo(codice: CONTABILITA_ASCOT, descrizione: "ContabilitaAscot",
                    tipiDocumento: [Determina.TIPO_OGGETTO, PropostaDelibera.TIPO_OGGETTO, Delibera.TIPO_OGGETTO]),
            new TipoDatoAggiuntivo(codice: CUP, descrizione: "CUP",
                tipiDocumento: [Determina.TIPO_OGGETTO])

    ].asImmutable()

    String      codice
    String      descrizione
    String      valoreDefault    // se presente, imposta direttamente questo valore.
    List<String> tipiDocumento

    static List<TipoDatoAggiuntivo> getListaDatiAggiuntivi () {
        return DATI_AGGIUNTIVI.findAll { isAbilitato(it.codice) }
    }

    static TipoDatoAggiuntivo getByCodice (String codice) {
        return getListaDatiAggiuntivi().find { it.codice == codice }
    }

    static List<TipoDatoAggiuntivo> getByTipoOggetto (String tipoOggetto) {
        return getListaDatiAggiuntivi().findAll { it.tipiDocumento.contains(tipoOggetto) }
    }

    static boolean isAbilitato (String codice) {
        return Impostazioni.DATI_AGGIUNTIVI.valori.contains(codice)
    }

    static List<TipoDatoAggiuntivoValore> getListaValori (String tipoDato) {
        return TipoDatoAggiuntivoValore.findAllByCodice(tipoDato, [sort: 'sequenza', order: 'asc'])
    }

    static String getDescrizione (String codice) {
        return getByCodice(codice)?.descrizione
    }

    static String getValore (Collection<DatoAggiuntivoDTO> datiAggiuntivi, String tipoDato) {
        if (datiAggiuntivi == null) {
            return null
        }

        for (DatoAggiuntivoDTO dato : datiAggiuntivi) {
            if (dato.codice == tipoDato) {
                return dato.valore
            }
        }

        return null
    }
}
