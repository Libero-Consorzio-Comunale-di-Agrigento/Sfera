package it.finmatica.atti.integrazioni

import it.finmatica.atti.documenti.Budget
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.IProposta
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.tipologie.TipoDelibera
import it.finmatica.atti.dto.documenti.BudgetDTO
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.contabilita.IntegrazioneContabilitaCe4Config

class ContabilitaCe4Service {

    IntegrazioneContabilitaCe4Config integrazioneContabilitaCe4Config

    String getMessaggioAggiornaProposta(IProposta proposta) {

        String tipoDocumento = ""
        if (proposta instanceof PropostaDelibera) {
            tipoDocumento = PropostaDelibera.TIPO_OGGETTO
        } else if (proposta instanceof Determina) {
            tipoDocumento = Determina.TIPO_OGGETTO
        }

        String messaggio = "<![CDATA[<aggiornaProposta>" +
                "<Proposta>" +
                "<Data_proposta>" + proposta.dataNumeroProposta?.format("yyyy-MM-dd") + "</Data_proposta>" +
                "<Tipo_proposta>" + getTipologiaProposta(proposta) + "</Tipo_proposta>" +
                "<Numero_proposta>" + proposta.numeroProposta + "</Numero_proposta>" +
                "<Descrizione>" + proposta.oggetto.replaceAll("[<>]","_") + "</Descrizione>" +
                "<Url_sfera>" + Impostazioni.URL_SERVER.valore + "/Atti/standalone.zul?operazione=APRI_DOCUMENTO&tipoDocumento="+tipoDocumento+"&id="+ proposta.idDocumento + "</Url_sfera>"

        getBudgetDTOList(proposta).each {
            messaggio = messaggio + "<Imputazione>" +
                    "<Conto>" + it.contoEconomico + "</Conto>" +
                    "<Importo>" + it.importo + "</Importo>" +
                    "<Inizio_competenza>" + it.dataInizioValidita?.format("yyyy-MM-dd") + "</Inizio_competenza>" +
                    "<Fine_competenza>" + it.dataFineValidita?.format("yyyy-MM-dd") + "</Fine_competenza>" +
                    "</Imputazione>"
        }

        messaggio = messaggio + "</Proposta></aggiornaProposta>]]>"

        return messaggio
    }

    String getMessaggioAggiornaProposta(IAtto atto) {

        String tipoDocumento= ""
        if (atto instanceof Delibera) {
            tipoDocumento = Delibera.TIPO_OGGETTO
        } else if (atto instanceof Determina) {
            tipoDocumento = Determina.TIPO_OGGETTO
        }

        String messaggio = "<![CDATA[<aggiornaProposta>" +
        "<Proposta>" +
        "<Data_proposta>" + atto.proposta?.dataNumeroProposta?.format("yyyy-MM-dd") + "</Data_proposta>" +
        "<Tipo_proposta>" + getTipologiaAtto(atto) + "</Tipo_proposta>" +
        "<Numero_proposta>" + atto.proposta?.numeroProposta + "</Numero_proposta>" +
        "<Descrizione>" + atto.proposta?.oggetto.replaceAll("[<>]","_") + "</Descrizione>" +
        "<Url_sfera>" + Impostazioni.URL_SERVER.valore + "/Atti/standalone.zul?operazione=APRI_DOCUMENTO&tipoDocumento="+tipoDocumento+"&id="+ atto.proposta.idDocumento + "</Url_sfera>"

        getBudgetDTOList(atto).each {
            messaggio = messaggio + "<Imputazione>" +
            "<Conto>" + it.contoEconomico + "</Conto>" +
            "<Importo>" + it.importo + "</Importo>" +
            "<Inizio_competenza>" + it.dataInizioValidita?.format("yyyy-MM-dd") + "</Inizio_competenza>" +
            "<Fine_competenza>" + it.dataFineValidita?.format("yyyy-MM-dd") + "</Fine_competenza>" +
            "</Imputazione>"
        }

        messaggio = messaggio + "</Proposta></aggiornaProposta>]]>"

        return messaggio
    }

    String getMessaggioConfermaProposta(IAtto atto) {

        String tipoDocumento= ""
        if (atto instanceof Delibera) {
            tipoDocumento = Delibera.TIPO_OGGETTO
        } else if (atto instanceof Determina) {
            tipoDocumento = Determina.TIPO_OGGETTO
        }

        String messaggio = "<![CDATA[<confermaProposta>" +
        "<Conferma>" +
        "<Data_proposta>" + atto.proposta?.dataNumeroProposta?.format("yyyy-MM-dd") + "</Data_proposta>" +
        "<Tipo_proposta>" + getTipologiaProposta(atto.proposta) + "</Tipo_proposta>" +
        "<Numero_proposta>" + atto.proposta?.numeroProposta + "</Numero_proposta>" +
        "<Tipo_delibera>" + getTipologiaAtto(atto) + "</Tipo_delibera>" +
        "<Numero_delibera>" + atto.numeroAtto + "</Numero_delibera>" +
        "<Data_delibera>" + atto.dataAtto?.format("yyyy-MM-dd") + "</Data_delibera>" +
        "<Url_sfera>" + Impostazioni.URL_SERVER.valore + "/Atti/standalone.zul?operazione=APRI_DOCUMENTO&tipoDocumento="+tipoDocumento+"&id="+ atto.idDocumento + "</Url_sfera>"

        def listaBudget = getBudgetFornitoreDTOList(atto)

        if (listaBudget.size()>0) {
            messaggio = messaggio + "<Fornitori>"
                listaBudget.each {
                        messaggio = messaggio + "<Conto_fornitore>" + it.codiceFornitore + "</Conto_fornitore>"
                }
            messaggio = messaggio + "</Fornitori>"
        }

        messaggio = messaggio + "</Conferma></confermaProposta>]]>"

        return messaggio
    }

    String getMessaggioRifiutaProposta(IProposta proposta) {

        String messaggio = "<![CDATA[<rifiutaProposta>" +
                "<Rifiuto>" +
                "<Data_proposta>" + proposta.dataNumeroProposta?.format("yyyy-MM-dd") + "</Data_proposta>" +
                "<Tipo_proposta>" + getTipologiaProposta(proposta) + "</Tipo_proposta>" +
                "<Numero_proposta>" + proposta.numeroProposta + "</Numero_proposta>" +
                "<Data_rifiuto>" + new Date().format("yyyy-MM-dd") + "</Data_rifiuto>" +
                "</Rifiuto>" +
                "</rifiutaProposta>]]>"

        return messaggio
    }

    String getMessaggioAnnullaDelibera(IAtto atto) {

        String messaggio = "<![CDATA[<annullaDelibera>" +
        "<Annullo>" +
        "<Numero_delibera>" + atto.numeroAtto + "</Numero_delibera>" +
        "<Data_delibera>" + atto.dataAtto?.format("yyyy-MM-dd") + "</Data_delibera>" +
        "<Tipo_delibera>" + getTipologiaAtto(atto) + "</Tipo_delibera>" +
        "</Annullo>" +
        "</annullaDelibera>]]>"

        return messaggio
    }

    List<BudgetDTO> getBudgetDTOList(IDocumento documento) {
        List<BudgetDTO> listaBudget = null

        documento instanceof Delibera

        documento instanceof PropostaDelibera

        listaBudget = Budget.createCriteria().list() {

            if (documento instanceof Determina) {
                eq("determina.id", documento.id)
            }
            if (documento instanceof PropostaDelibera) {
                eq("propostaDelibera.id", documento.id)
            }
            if (documento instanceof Delibera) {
                eq("propostaDelibera.id", documento?.propostaDelibera?.id)
            }

            order("sequenza", "asc")
        }.toDTO(["tipoBudget"])


        return listaBudget
    }

    List<BudgetDTO> getBudgetFornitoreDTOList(IDocumento documento) {
        List<BudgetDTO> listaBudget = null

        documento instanceof Delibera

        documento instanceof PropostaDelibera

        listaBudget = Budget.createCriteria().list() {

            if (documento instanceof Determina) {
                eq("determina.id", documento.id)
            }
            if (documento instanceof PropostaDelibera) {
                eq("propostaDelibera.id", documento.id)
            }
            if (documento instanceof Delibera) {
                eq("propostaDelibera.id", documento?.propostaDelibera?.id)
            }
            isNotNull("codiceFornitore")
            order("sequenza", "asc")
        }.toDTO(["tipoBudget"])


        return listaBudget
    }

    String getTipologiaProposta(IProposta proposta) {
        String tipologia = "";
        String codiceEsterno = proposta.tipologiaDocumento?.codiceEsterno

        if (codiceEsterno?.length()>0 || codiceEsterno != null) {
            tipologia = integrazioneContabilitaCe4Config.getTipologiaProposta(proposta, codiceEsterno)
        } else
        {
            tipologia = integrazioneContabilitaCe4Config.getTipologiaProposta(proposta,  proposta.registroProposta?.codice)
        }

        if (tipologia.length()>4) {
            tipologia=tipologia.substring(0,4)
        }

        return tipologia
    }

    String getTipologiaAtto(IAtto atto) {
        String tipologia = "";
        String codiceEsterno = atto.tipologiaDocumento?.codiceEsterno

        if (codiceEsterno?.length()>0 || codiceEsterno != null) {
            tipologia = integrazioneContabilitaCe4Config.getTipologiaAtto(atto, codiceEsterno)
        } else
        {
            tipologia = integrazioneContabilitaCe4Config.getTipologiaAtto(atto,  atto.registroAtto?.codice)
        }

        if (tipologia.length()>4) {
            tipologia=tipologia.substring(0,4)
        }

        return tipologia
    }
}
