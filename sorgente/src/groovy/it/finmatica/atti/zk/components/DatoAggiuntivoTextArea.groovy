package it.finmatica.atti.zk.components

import it.finmatica.atti.dto.documenti.DatoAggiuntivoDTO
import it.finmatica.dto.DTO
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.EventListener
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Textbox

/**
 * Created by czappavigna on 23/10/2017.
 */
class DatoAggiuntivoTextArea extends Textbox implements EventListener<Event> {

    private String tipoDato
    private DTO<?> documento

    DatoAggiuntivoTextArea() {
        addEventListener(Events.ON_CHANGE, this)
    }

    void setDatoAggiuntivo (String tipoDato) {
        if (this.tipoDato != tipoDato) {
            this.tipoDato = tipoDato
        }
        update()
    }

    void setDocumento (DTO<?> documento) {
        if (this.documento != documento) {
            this.documento = documento
        }
        update()
    }

    private update(){
        if (documento!= null && tipoDato != null) {
            DatoAggiuntivoDTO datoAggiuntivo = getDatoAggiuntivo(tipoDato, documento)
            if (datoAggiuntivo != null) {
                super.setValue(datoAggiuntivo.valore)
            }
        }
    }

    @Override
    void onEvent (Event event) throws Exception {
        if (event.name == Events.ON_CHANGE) {
            saveDatoAggiuntivo(event.target?.value)
        }
    }

    private DatoAggiuntivoDTO getDatoAggiuntivo (String tipoDato, DTO<?> documento) {
        return documento.datiAggiuntivi?.find { it.codice == tipoDato }
    }

    private void saveDatoAggiuntivo (String text) {
        DatoAggiuntivoDTO datoAggiuntivo = getDatoAggiuntivo(tipoDato, documento)
        if (datoAggiuntivo == null) {
            datoAggiuntivo = new DatoAggiuntivoDTO()
            datoAggiuntivo.codice = tipoDato
            documento.addToDatiAggiuntivi(datoAggiuntivo)
        }
        datoAggiuntivo.valore = text
    }
}
