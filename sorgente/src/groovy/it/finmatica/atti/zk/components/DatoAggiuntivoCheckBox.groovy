package it.finmatica.atti.zk.components

import it.finmatica.atti.documenti.TipoDatoAggiuntivo
import it.finmatica.atti.dto.documenti.DatoAggiuntivoDTO
import it.finmatica.dto.DTO
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.EventListener
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Checkbox

/**
 * Created by esasdelli on 11/09/2017.
 */
class DatoAggiuntivoCheckBox extends Checkbox implements EventListener<Event> {

    private String tipoDato
    private DTO<?> documento

    DatoAggiuntivoCheckBox () {
        addEventListener(Events.ON_CHECK, this)
    }

    void setDatoAggiuntivo (String tipoDato) {
        if (this.tipoDato != tipoDato) {
            this.tipoDato = tipoDato
            update(this.tipoDato, this.documento)
        }
    }

    void setDocumento (DTO<?> documento) {
        if (this.documento != documento) {
            this.documento = documento
            update(this.tipoDato, this.documento)
        }
    }

    private void update (String tipoDato, DTO<?> documento) {
        if (tipoDato != null) {
            setLabel(TipoDatoAggiuntivo.getDescrizione(tipoDato))
        }

        if (tipoDato != null && documento != null) {
            setChecked(isChecked(tipoDato, documento))
        }
    }

    @Override
    void onEvent (Event event) throws Exception {
        if (event.name == Events.ON_CHECK) {
            saveDatoAggiuntivo(tipoDato, isChecked())
        }
    }

    private boolean isChecked (String tipoDato, DTO<?> documento) {
        DatoAggiuntivoDTO dato = getDatoAggiuntivo(tipoDato, documento)

        if (dato == null) {
            String valoreDefault = TipoDatoAggiuntivo.getByCodice(tipoDato).valoreDefault
            if (valoreDefault != null) {
                dato = new DatoAggiuntivoDTO(codice: tipoDato, valore: valoreDefault)
                documento.addToDatiAggiuntivi(dato)
            }
        }

        return ("Y" == dato?.valore)
    }

    private void saveDatoAggiuntivo (String tipoDato, boolean selezionato) {
        DatoAggiuntivoDTO datoAggiuntivo = getDatoAggiuntivo(tipoDato, documento)
        if (datoAggiuntivo == null) {
            datoAggiuntivo = new DatoAggiuntivoDTO(codice: tipoDato)
            documento.addToDatiAggiuntivi(datoAggiuntivo)
        }

        if (selezionato) {
            datoAggiuntivo.valore = "Y"
        } else {
            datoAggiuntivo.valore = "N"
        }
    }

    private DatoAggiuntivoDTO getDatoAggiuntivo (String tipoDato, DTO<?> documento) {
        return documento.datiAggiuntivi?.find { it.codice == tipoDato }
    }
}
