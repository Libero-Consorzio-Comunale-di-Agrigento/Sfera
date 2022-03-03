package it.finmatica.atti.zk.components

import it.finmatica.atti.dizionari.TipoDatoAggiuntivoValore
import it.finmatica.atti.documenti.viste.RicercaSiav
import it.finmatica.atti.dto.dizionari.TipoDatoAggiuntivoValoreDTO
import it.finmatica.atti.dto.documenti.DatoAggiuntivoDTO
import it.finmatica.atti.dto.documenti.viste.RicercaSiavDTO
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.dto.DTO
import org.zkoss.zk.ui.event.Event
import org.zkoss.zk.ui.event.EventListener
import org.zkoss.zk.ui.event.Events
import org.zkoss.zul.Combobox
import org.zkoss.zul.ListModelList

/**
 * Created by czappavigna on 02/10/2018.
 */
class IntegrazioneSiavComboBox extends Combobox implements EventListener<Event> {

    private String tipoDato
    private DTO<?> documento

    IntegrazioneSiavComboBox() {
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
            this.setModel(new ListModelList<RicercaSiavDTO>())
        }

        if (documento == null) {
            ((ListModelList<RicercaSiavDTO>) getModel())?.clearSelection()
        }

        if (tipoDato == null || documento == null || documento?.domainObject == null) {
            return
        }

        ListModelList<RicercaSiavDTO> tipiDatiAggiuntiviValori = new ListModelList<>(RicercaSiav.findAllByCodiceStruttura(documento.domainObject?.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4?.progr, [sort: "codiceSiav", order: "asc"])).toDTO()
        setModel(tipiDatiAggiuntiviValori)
        selectDatoAggiuntivo(getDatoAggiuntivo(tipoDato, documento))
    }

    private void selectDatoAggiuntivo (DatoAggiuntivoDTO datoAggiuntivo) {
        if (datoAggiuntivo == null) {
            ((ListModelList<RicercaSiavDTO>) getModel()).clearSelection()
            return
        }

        Collection<RicercaSiavDTO> selection = getModel().findAll { RicercaSiavDTO tipo -> tipo.codiceSiav == datoAggiuntivo.valore}
        ((ListModelList<RicercaSiavDTO>) getModel()).setSelection(selection)
    }

    @Override
    void  onEvent (Event event) throws Exception {
        if (event.name == Events.ON_SELECT) {
            saveDatoAggiuntivo(getSelectedIndex())
        }
    }

    private DatoAggiuntivoDTO getDatoAggiuntivo (String tipoDato, DTO<?> documento) {
        return documento.datiAggiuntivi?.find { it.codice == tipoDato }
    }

    private void saveDatoAggiuntivo (int selectedIndex) {
        RicercaSiavDTO valore = (RicercaSiavDTO) getModel().getElementAt(selectedIndex)
        DatoAggiuntivoDTO datoAggiuntivo = getDatoAggiuntivo(tipoDato, documento)
        if (datoAggiuntivo == null) {
            datoAggiuntivo = new DatoAggiuntivoDTO()
            datoAggiuntivo.codice = tipoDato
            documento.addToDatiAggiuntivi(datoAggiuntivo)
        }
        datoAggiuntivo.valore = valore.codiceSiav
    }
}
