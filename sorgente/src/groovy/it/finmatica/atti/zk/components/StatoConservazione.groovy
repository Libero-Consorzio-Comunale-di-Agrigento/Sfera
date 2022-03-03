package it.finmatica.atti.zk.components

import it.finmatica.atti.dto.integrazioni.JConsLogConservazioneDTO
import org.zkoss.zul.Label

/**
 * Created by esasdelli on 28/08/2017.
 */
class StatoConservazione extends Label {
    private JConsLogConservazioneDTO logConservazione;

    void setLogConservazione (JConsLogConservazioneDTO logConservazione) {
        if (logConservazione != this.logConservazione) {
            this.logConservazione = logConservazione
            update (logConservazione)
        }
    }

    private void update (JConsLogConservazioneDTO logConservazione) {
        if (logConservazione == null) {
            setSclass("hidden");
            setValue("");
            return;
        }

        if (it.finmatica.atti.documenti.StatoConservazione.DA_CONSERVARE == logConservazione.statoConservazione) {
            setSclass("hidden");
            setValue("");
            return;
        }

        if (it.finmatica.atti.documenti.StatoConservazione.IN_CONSERVAZIONE == logConservazione.statoConservazione) {
            setSclass("visible");
            setValue("Conservazione in Corso");
            return;
        }

        if (it.finmatica.atti.documenti.StatoConservazione.ERRORE == logConservazione.statoConservazione) {
            setSclass("visible redText");
            setValue("Errore in Conservazione");
            setTooltiptext(logConservazione.log);
            return;
        }

        if (it.finmatica.atti.documenti.StatoConservazione.ERRORE_INVIO == logConservazione.statoConservazione) {
            setSclass("visible redText");
            setValue("Errore di invio in Conservazione");
            setTooltiptext(logConservazione.log);
            return;
        }

        setSclass("visible");
        setValue("Conservato il: "+logConservazione.dataFine?.format("dd/MM/yyyy"));
        setTooltiptext(logConservazione.log);
    }
}
