/**
 * Questo script genera l'html dei modelli di gdm.
 *
 * Created by esasdelli on 29/11/2017.
 */

//import grails.plugin.svn.SvnClient;

def campi = [DATI_GENERALI: ["ID_DOCUMENTO_GRAILS",
                              "UTENTE_UPD",
                              "UTENTE_INS",
                              "DATA_UPD",
                              "DATA_INS",
                              "ENTE",
                              "STATO_FIRMA",
                              "RISERVATO",
                              "DATA_FIRMA"]
             ,
             DATI_PROTOCOLLO: ["NUMERO",
                                "ANNO",
                                "TIPO_REGISTRO",
                                "TIPO_DOCUMENTO",
                                "IDRIF",
                                "NUMERO_PROTOCOLLO",
                                "ANNO_PROTOCOLLO",
                                "STATO_PR",
                                "UNITA_PROTOCOLLANTE",
                                "CODICE_AMMINISTRAZIONE",
                                "CODICE_AOO",
                                "DATA",
                                "DATA_NUMERO_PROTOCOLLO",
                                "MODALITA",
                                "ID_DOCUMENTO_PROTOCOLLO",
                                "REGISTRO_PROTOCOLLO",
                                "UTENTE_PROTOCOLLANTE",
                                "DESCRIZIONE_TIPO_REGISTRO",
                                // quelli che seguono sono per la spedizione pec
                                "VERIFICA_FIRMA",
                                "DATA_VERIFICA",
                                "SPEDITO",
                                "DATA_SPEDIZIONE",
                                "REGISTRATA_ACCETTAZIONE"]
             ,
             DATI_CLASSIFICAZIONE: ["CLASS_DAL",
                                     "CLASS_DESCR",
                                     "CLASS_COD",
                                     "DA_FASCICOLARE",
                                     "FASCICOLO_OGGETTO",
                                     "FASCICOLO_ANNO",
                                     "FASCICOLO_NUMERO"]
             ,
             DATI_PUBBLICAZIONE: ["ANNO_ALBO",
                                   "NUMERO_ALBO",
                                   "ID_DOCUMENTO_ALBO",
                                   "DATA_PUBBLICAZIONE",
                                   "DATA_FINE_PUBBLICAZIONE",
                                   "GIORNI_PUBBLICAZIONE",
                                   "DATA_PUBBLICAZIONE_2",
                                   "DATA_FINE_PUBBLICAZIONE_2"]
             ,
             DATI_FLUSSO: ["POSIZIONE_FLUSSO"]
             ,
             DATI_ODG: ["ODG_STATO",
                         "ID_COMMISSIONE",
                         "ID_DELEGA",
                         "DESCRIZIONE_DELEGA"]
             ,
             ALLEGATO: ["DESCRIZIONE",
                         "DESCRIZIONE_TIPO_ALLEGATO",
                         "ID_DELIBERA_GRAILS",
                         "ID_DETERMINA_GRAILS",
                         "ID_PROPOSTA_DELIBERA_GRAILS",
                         "ID_TIPO_ALLEGATO",
                         "NUM_PAGINE",
                         "QUANTITA"]
             ,
             CERTIFICATO: ["FIRMATARIO",
                            "ID_DELIBERA_GRAILS",
                            "ID_DETERMINA_GRAILS",
                            "SECONDA_PUBBLICAZIONE",
                            "TIPO",
                            "UTENTE_AD4_FIRMATARIO"]
             ,
            DELIBERA: ["ANNO_DELIBERA",
                         "ASSESSORE",
                         "DATA_ADOZIONE",
                         "DATA_ESECUTIVITA",
                         "DATA_NUMERO_DELIBERA",
                         "DESCR_REGISTRO_DELIBERA",
                         "DESCR_UO_DIRIGENTE",
                         "DESCRIZIONE_CONSIP",
                         "DESCRIZIONE_TIPO_DELIBERA",
                         "ESEGUIBILITA_IMMEDIATA",
                         "ESITO_DISCUSSIONE",
                         "ID_CONSIP",
                         "ID_PROPOSTA_DELIBERA_GRAILS",
                         "ID_REGISTRO_DELIBERA",
                         "ID_TIPO_DELIBERA",
                         "NOTE",
                         "NOTE_CONTABILI",
                         "NUMERO_DELIBERA",
                         "OGGETTO",
                         "PRESIDENTE",
                         "SEGRETARIO",
                         "UTENTE_AD4_PRESIDENTE",
                         "UTENTE_AD4_SEGRETARIO",
                         "UTENTE_AD4_ASSESSORE"]
             ,
             DETERMINA: ["ANNO_DETERMINA",
                          "ANNO_PROPOSTA",
                          "DAL_UNITA_PROPONENTE",
                          "DAL_UNITA_REDATTORE",
                          "DATA_ESECUTIVITA",
                          "DATA_NUMERO_DETERMINA",
                          "DATA_NUMERO_PROPOSTA",
                          "DESCR_REGISTRO_DETERMINA",
                          "DESCR_REGISTRO_PROPOSTA",
                          "DESCR_UO_DIRIGENTE",
                          "DESCRIZIONE_CONSIP",
                          "DESCRIZIONE_TIPO_DETERMINA",
                          "DIRIGENTE",
                          "DOC_COLLEGATO",
                          "FIRMATARIO",
                          "FUNZIONARIO",
                          "ID_CONSIP",
                          "ID_DOCUMENTO_DOCER",
                          "ID_REGISTRO_DETERMINA",
                          "ID_REGISTRO_PROPOSTA",
                          "ID_TIPO_DETERMINA",
                          "NOTE",
                          "NOTE_CONTABILI",
                          "NUMERO_DETERMINA",
                          "NUMERO_PROPOSTA",
                          "OGGETTO",
                          "PROGR_UNITA_PROPONENTE",
                          "PROGR_UNITA_REDATTORE",
                          "REDATTORE",
                          "TIPO_COLLEGATO",
                          "UNITA_PROPONENTE",
                          "UNITA_REDATTORE",
                          "UTENTE_AD4_DIRIGENTE",
                          "UTENTE_AD4_FIRMATARIO",
                          "UTENTE_AD4_FUNZIONARIO",
                          "UTENTE_AD4_REDATTORE"]
             ,
             PROPOSTA_DELIBERA: ["ANNO_PROPOSTA",
                                  "ASSESSORE",
                                  "DAL_UNITA_PROPONENTE",
                                  "DAL_UNITA_REDATTORE",
                                  "DATA_NUMERO_PROPOSTA",
                                  "DESCR_REGISTRO_PROPOSTA",
                                  "DESCRIZIONE_CONSIP",
                                  "DESCRIZIONE_TIPO_DELIBERA",
                                  "DIRIGENTE",
                                  "DOC_COLLEGATO",
                                  "FUNZIONARIO",
                                  "ID_CONSIP",
                                  "ID_REGISTRO_PROPOSTA",
                                  "ID_TIPO_DELIBERA",
                                  "NOTE",
                                  "NOTE_CONTABILI",
                                  "NUMERO_PROPOSTA",
                                  "OGGETTO",
                                  "PROGR_UNITA_PROPONENTE",
                                  "PROGR_UNITA_REDATTORE",
                                  "REDATTORE",
                                  "TIPO_COLLEGATO",
                                  "UNITA_PROPONENTE",
                                  "UNITA_REDATTORE",
                                  "UTENTE_AD4_ASSESSORE",
                                  "UTENTE_AD4_DIRIGENTE",
                                  "UTENTE_AD4_FUNZIONARIO",
                                  "UTENTE_AD4_REDATTORE"]
             ,
             VISTO: ["CONTABILE",
                      "DAL_UNITA_REDAZIONE_VISTO",
                      "DESCRIZIONE_TIPO_VISTOPARERE",
                      "DIRIGENTE_VISTO",
                      "ESITO",
                      "ID_DETERMINA_GRAILS",
                      "ID_PROPOSTA_DELIBERA_GRAILS",
                      "ID_TIPO_VISTOPARERE",
                      "NOTE",
                      "PROGR_UNITA_REDAZIONE_VISTO",
                      "UNITA_REDAZIONE_VISTO",
                      "UTENTE_AD4_DIRIGENTE_VISTO"],
             SEDUTA_STAMPA:["FIRMATARIO", "NOTE", "OGGETTO", "REDATTORE", "DESCRIZIONE_COMMISSIONE"]
]

def modelli = [
        DETERMINA:["DETERMINA", "DATI_GENERALI", "DATI_PROTOCOLLO", "DATI_CLASSIFICAZIONE", "DATI_FLUSSO", "DATI_PUBBLICAZIONE"],
        DELIBERA: ["DELIBERA", "DATI_GENERALI", "DATI_PROTOCOLLO", "DATI_CLASSIFICAZIONE", "DATI_FLUSSO", "DATI_ODG", "DATI_PUBBLICAZIONE"],
        PROPOSTA_DELIBERA: ["PROPOSTA_DELIBERA", "DATI_GENERALI", "DATI_PROTOCOLLO", "DATI_CLASSIFICAZIONE", "DATI_FLUSSO", "DATI_ODG"],
        VISTO: ["VISTO", "DATI_GENERALI", "DATI_FLUSSO"],
        CERTIFICATO: ["CERTIFICATO", "DATI_GENERALI", "DATI_FLUSSO"],
        SEDUTA_STAMPA: ["SEDUTA_STAMPA", "DATI_GENERALI", "DATI_FLUSSO", "DATI_PROTOCOLLO", "DATI_CLASSIFICAZIONE", "DATI_PUBBLICAZIONE"],
        ALLEGATO: ["ALLEGATO", "DATI_GENERALI"]
]

target (main: "Crea i modelli html di gdm") {
    modelli.each { def modello ->
        String html = ""
        modello.value.each { String sezione ->
            html += "<table><tbody>\n"
            campi[sezione].each { String campo ->
                html += "<tr><td>${campo}</td><td><input type=\"text\" class=\"AFCInput\" style=\"\" title=\"${campo}\" size=\"015\" name=\"_SSS015000_${campo}\" value=\"${campo}\" /></td></tr>\n"
            }
            html += "</tbody></table>"
        }

        new File("${modello.key}.html").delete()
        new File("${modello.key}.html") << html
    }
}

setDefaultTarget (main)






