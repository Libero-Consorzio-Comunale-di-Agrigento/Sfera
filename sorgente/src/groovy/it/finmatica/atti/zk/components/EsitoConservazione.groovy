package it.finmatica.atti.zk.components

import it.finmatica.atti.dto.integrazioni.JConsLogConservazioneDTO
import org.zkoss.zul.Div

/**
 * Created by esasdelli on 28/08/2017.
 */
class EsitoConservazione extends Div {
    private JConsLogConservazioneDTO logConservazione

    void setLogConservazione (JConsLogConservazioneDTO logConservazione) {
        if (logConservazione == this.logConservazione) {
            return
        }
        this.logConservazione = logConservazione

        getChildren().clear()

        new ZkBuilder({
            div {
                a (href:"..${logConservazione.urlRicevuta}", target:"_blank", label:"Id Transazione: ${logConservazione.idTransazione}", tooltiptext:"Apre la ricevuta di conservazione")
                label (value:"Data invio:"+logConservazione.dataInizio?.format("dd/MM/yyyy"), style:"display:block")
            }
            label (value:logConservazione.esito+": ")
            label (value:logConservazione.log, maxlength:100, popup: popup {
                label (pre:true, multiline:true, value:logConservazione.log)
            })
        }).renderTo(this)
    }
}
