package it.finmatica.atti;

import it.finmatica.atti.documenti.IAtto;
import it.finmatica.atti.documenti.IPubblicabile;

import java.util.Map;

/**
 * Created by czappavigna on 15/10/2018.
 */
public interface IntegrazioneAlbo {

    void allineaDatePubblicazioni();
    boolean hasRelata(IPubblicabile atto);
    Map getRelata(IPubblicabile atto);
    void pubblicaAtto(IPubblicabile atto);
    void secondaPubblicazioneAtto(IPubblicabile atto);
    void annullaAtto(IPubblicabile atto, IAtto attoPrincipale);
    void aggiornaDataEsecutivita(IPubblicabile pubblicabile);
    void terminaPubblicazioneAtto(IPubblicabile atto);
    void terminaSecondaPubblicazioneAtto(IPubblicabile atto);
    boolean controllaDocumentiAlboConErrore();
}
