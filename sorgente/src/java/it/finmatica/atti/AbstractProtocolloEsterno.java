package it.finmatica.atti;

import it.finmatica.atti.documenti.IFascicolabile;
import it.finmatica.atti.documenti.IProtocollabile;
import it.finmatica.docer.atti.anagrafiche.DatiRicercaDocumento;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AbstractProtocolloEsterno implements IProtocolloEsterno {

    @Override
    public void fascicola(IFascicolabile documento) {

    }

    @Override
    public void protocolla(IProtocollabile documento) {

    }

    @Override
    public List<Classifica> getListaClassificazioni(String filtro, String codiceUoProponente) {
        return new ArrayList<Classifica>();
    }

    @Override
    public Classifica getClassifica(String codice, Date dal) {
        return null;
    }

    @Override
    public Fascicolo getFascicolo(Classifica classifica, String numero, int anno) {
        return null;
    }

    @Override
    public List<Fascicolo> getListaFascicoli(String filtro, String codiceClassifica, Date lassificaDal, String codiceUoProponente) {
        return new ArrayList<Fascicolo>();
    }

    @Override
    public void creaFascicolo(String numero, String anno, String descrizione, String parent_progressivo, String classifica) {

    }

    @Override
    public List<Documento> getListaDocumenti(DatiRicercaDocumento datiRicerca) {
        return new ArrayList<Documento>();
    }

    @Override
    public InputStream downloadFile(String docNum) {
        return null;
    }

    @Override
    public void sincronizzaClassificazioniEFascicoli() {

    }

    @Override
    public void creaAllegatoProtocollo(IProtocollabile documento, String descrizione, String nomeFileAllegato, InputStream is){

    }
}
