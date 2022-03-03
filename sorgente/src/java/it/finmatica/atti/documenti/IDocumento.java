package it.finmatica.atti.documenti;

import it.finmatica.ad4.autenticazione.Ad4Utente;
import it.finmatica.atti.IDocumentoEsterno;
import it.finmatica.atti.commons.FileAllegato;
import it.finmatica.gestioneiter.IDocumentoIterabile;
import it.finmatica.gestionetesti.reporter.GestioneTestiModello;
import it.finmatica.so4.struttura.So4Amministrazione;
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb;

import java.util.Collection;
import java.util.Set;

public interface IDocumento extends IDocumentoIterabile, IDocumentoEsterno {

    Long getId();

    String getTipoOggetto();

    ITipologia getTipologiaDocumento();

    StatoDocumento getStato();

    void setStato(StatoDocumento stato);

    StatoFirma getStatoFirma();

    void setStatoFirma(StatoFirma statoFirma);

    FileAllegato getTesto();

    void setTesto(FileAllegato testo);

    FileAllegato getTestoOdt();

    void setTestoOdt(FileAllegato testoOdt);

    GestioneTestiModello getModelloTesto();

    void setModelloTesto(GestioneTestiModello modelloTesto);

    Collection<ISoggettoDocumento> getSoggetti();

    ISoggettoDocumento getSoggetto(String tipoSoggetto);

    void setSoggetto(String tipoSoggetto, Ad4Utente utenteAd4, So4UnitaPubb unitaSo4);

    void setSoggetto(String tipoSoggetto, Ad4Utente utenteAd4, So4UnitaPubb unitaSo4, int sequenza);

    So4UnitaPubb getUnitaProponente();

    Set<Allegato> getAllegati();

    String getNomeFileTestoPdf();

    String getNomeFile();

    boolean isRiservato();

    So4Amministrazione getEnte();

    StatoMarcatura getStatoMarcatura();

    void setStatoMarcatura(StatoMarcatura statoMarcatura);
}
