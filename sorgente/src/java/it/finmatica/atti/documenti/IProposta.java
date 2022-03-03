package it.finmatica.atti.documenti;

import it.finmatica.atti.dizionari.Categoria;
import it.finmatica.atti.dizionari.OggettoRicorrente;
import it.finmatica.atti.dizionari.TipoRegistro;

import java.util.Date;

public interface IProposta extends IDocumento, IFascicolabile {

    IAtto getAtto();

    Date getDataNumeroProposta();

    Integer getNumeroProposta();

    Integer getAnnoProposta();

    TipoRegistro getRegistroProposta();

    boolean isDaInviareCorteConti();

    void setDaInviareCorteConti(boolean value);

    void setStatoOdg(StatoOdg stato);

    StatoOdg getStatoOdg();

    boolean isControlloFunzionario();

    String getOggetto();

    Categoria getCategoria();

    OggettoRicorrente getOggettoRicorrente();
}
