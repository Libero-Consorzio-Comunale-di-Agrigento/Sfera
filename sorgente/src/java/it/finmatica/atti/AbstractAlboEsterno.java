package it.finmatica.atti;

import it.finmatica.atti.documenti.IAtto;
import it.finmatica.atti.documenti.IPubblicabile;

import java.util.Map;

/**
 * Created by czappavigna on 15/10/2018.
 */
public class AbstractAlboEsterno implements IntegrazioneAlbo {
    public void allineaDatePubblicazioni() {

    }

    public boolean hasRelata(IPubblicabile atto) {
        return false;
    }

    public Map getRelata(IPubblicabile atto) {
        return null;
    }

    public void pubblicaAtto(IPubblicabile atto) {

    }

    public void secondaPubblicazioneAtto(IPubblicabile atto) {

    }

    public void annullaAtto(IPubblicabile atto, IAtto attoPrincipale) {

    }

    public void aggiornaDataEsecutivita(IPubblicabile pubblicabile) {

    }

    public void terminaPubblicazioneAtto(IPubblicabile atto) {

    }

    public void terminaSecondaPubblicazioneAtto(IPubblicabile atto) {

    }

    public boolean controllaDocumentiAlboConErrore() {
        return false;
    }
}
