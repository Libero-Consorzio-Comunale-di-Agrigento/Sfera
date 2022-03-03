package it.finmatica.atti.zk.components

import it.finmatica.atti.dizionari.TipoDatoAggiuntivoValore
import it.finmatica.atti.dto.dizionari.TipoDatoAggiuntivoValoreDTO
import it.finmatica.atti.dto.documenti.DatoAggiuntivoDTO
import it.finmatica.dto.DTO
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.EventListener
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Combobox
import org.zkoss.zul.ListModelList

/**
 * Created by esasdelli on 11/09/2017.
 */
class DatoAggiuntivoComboBox extends Combobox implements EventListener<Event> {

    private String tipoDato
    private DTO<?> documento

    DatoAggiuntivoComboBox () {
        addEventListener(Events.ON_SELECT, this)
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
        if (tipoDato == null) {
            this.setModel(new ListModelList<TipoDatoAggiuntivoValoreDTO>())
        }

        if (documento == null) {
            ((ListModelList<TipoDatoAggiuntivoValoreDTO>) getModel())?.clearSelection()
        }

        if (tipoDato == null || documento == null) {
            return
        }

        ListModelList<TipoDatoAggiuntivoValoreDTO> tipiDatiAggiuntiviValori = new ListModelList<>(TipoDatoAggiuntivoValore.findAllByCodice(tipoDato, [sort: 'sequenza', order: 'asc']).toDTO())
        setModel(tipiDatiAggiuntiviValori)
        selectDatoAggiuntivo(getDatoAggiuntivo(tipoDato, documento))
    }

    private void selectDatoAggiuntivo (DatoAggiuntivoDTO datoAggiuntivo) {
        if (datoAggiuntivo == null) {
            ((ListModelList<TipoDatoAggiuntivoValoreDTO>) getModel()).clearSelection()
            return
        }

        Collection<TipoDatoAggiuntivoValoreDTO> selection = getModel().findAll { TipoDatoAggiuntivoValoreDTO tipo -> tipo.id == datoAggiuntivo.valoreTipoDato.id }
        ((ListModelList<TipoDatoAggiuntivoValoreDTO>) getModel()).setSelection(selection)
    }

    @Override
    void onEvent (Event event) throws Exception {
        if (event.name == Events.ON_SELECT) {
            saveDatoAggiuntivo(getSelectedIndex())
        }
    }

    private DatoAggiuntivoDTO getDatoAggiuntivo (String tipoDato, DTO<?> documento) {
        return documento.datiAggiuntivi?.find { it.codice == tipoDato }
    }

    private void saveDatoAggiuntivo (int selectedIndex) {
        TipoDatoAggiuntivoValoreDTO valore = (TipoDatoAggiuntivoValoreDTO) getModel().getElementAt(selectedIndex)
        DatoAggiuntivoDTO datoAggiuntivo = getDatoAggiuntivo(tipoDato, documento)
        if (datoAggiuntivo == null) {
            datoAggiuntivo = new DatoAggiuntivoDTO()
            datoAggiuntivo.codice = tipoDato
            documento.addToDatiAggiuntivi(datoAggiuntivo)
        }
        datoAggiuntivo.valoreTipoDato = valore
    }
}
