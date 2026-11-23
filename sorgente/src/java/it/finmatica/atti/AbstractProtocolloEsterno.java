package it.finmatica.atti;

import it.finmatica.ad4.autenticazione.Ad4Utente;
import it.finmatica.atti.documenti.IFascicolabile;
import it.finmatica.atti.documenti.IProtocollabile;
import it.finmatica.docer.atti.anagrafiche.DatiRicercaDocumento;
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb;

import java.io.InputStream;
import java.util.*;

public class AbstractProtocolloEsterno implements IProtocolloEsterno {

    @Override
    public String getZulClassifica () {
        return "/commons/popupClassificazioni.zul";
    }

    @Override
    public String getZulFascicolo() {
        return "/commons/popupFascicoli.zul";
    }

    @Override
    public void fascicola(IFascicolabile documento) {

    }

    @Override
    public void registra(IProtocollabile documento) {

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
    public List<Fascicolo> getListaFascicoli(String codice, String descrizione, int anno,So4UnitaPubb unitaProponente, String tipologia, Ad4Utente utenteRedattore) {
        return new ArrayList<Fascicolo>();
    }

    @Override
    public List<Fascicolo> getListaFascicoli(String filtro, String codiceClassifica, Date classificaDal, String codiceUoProponente) {
        return new ArrayList<Fascicolo>();
    }

    @Override
    public List<Fascicolo> getListaFascicoliAnno(int anno, String filtro, String codiceClassifica, Date classificaDal, String codiceUoProponente) {
        return new ArrayList<Fascicolo>();
    }

    @Override
    public Map<String, Object> getListaFascicoli(String filtro, String codiceClassifica, Date classificaDal, String codiceUoProponente, Integer annoDal, Integer annoAl, String numeroDal, String numeroAl, Integer size, Integer offset) {
        Map<String, Object> result = new HashMap<String, Object>();
        result.put("lista", new ArrayList<Fascicolo>());
        result.put("total", 0);
        return result;
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
    public void creaAllegatoProtocollo(IProtocollabile documento, String descrizione, String nomeFileAllegato, InputStream is) {

    }
}
