package it.finmatica.atti.zk

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.ISoggettoDocumento
import it.finmatica.atti.dto.impostazioni.TipoSoggettoDTO
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO
import it.finmatica.so4.strutturaPubblicazione.So4ComponentePubb
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb

/**
 * Created by esasdelli on 31/10/2017.
 */
class SoggettoDocumento {
    // id del soggetto su db. se < 0, il soggetto non è ancora stato salvato su db.
    private Long id

    // indica se l'utente ha modificato il soggetto da interfaccia
    private boolean modificato

    // la sequenza per ordinare più soggetti con lo stesso tipoSoggetto
    private int sequenza

    // indica quale dei soggetti con lo stesso tipoSoggetto è quello "attivo", ad esempio, in caso di più FIRMATARI, uno solo alla volta può essere quello "attivo" per la firma
    private boolean attivo

    // l'utente ad4 del soggetto
    private Ad4UtenteDTO utente

    // l'unità so4 del soggetto (può essere nulla se il tipoSoggetto.categoria == UTENTE, altrimenti è obbligatoria)
    private So4UnitaPubbDTO unita

    // descrive il tipo di soggetto (REDATTORE, FIRMATARIO, UNITA, etc)
    private TipoSoggettoDTO tipoSoggetto

    // messaggio di avvertimento in caso ci siano problemi con il componente so4
    private String warnMessage

    // possibile lista di soggetti calcolati. Serve in particolare per mostrare i firmatari multipli di una stampa di verbale: #22789
    private List<SoggettoDocumento> lista

    SoggettoDocumento (TipoSoggetto tipoSoggetto, List soggettiCalcolati) {
        this (tipoSoggetto, soggettiCalcolati[0], 0, true)
        for (int i=0; i<soggettiCalcolati.size(); i++) {
            this.lista << new SoggettoDocumento(tipoSoggetto, soggettiCalcolati[i], i, i == 0)
        }
    }

    SoggettoDocumento (TipoSoggetto tipoSoggetto, So4ComponentePubb componente, int sequenza, boolean attivo) {
        this(-1l, true, sequenza, attivo, componente.soggetto?.utenteAd4?.toDTO(), componente.getUnitaPubb()?.toDTO(), tipoSoggetto.toDTO())
        if (this.tipoSoggetto.categoria == TipoSoggetto.CATEGORIA_COMPONENTE) {
            if (this.unita == null || this.utente == null) {
                warnMessage = "Il Componente con id ${componente.id} ha un utente o una unità organizzativa nulli."
            }
        }
    }

    SoggettoDocumento (TipoSoggetto tipoSoggetto, So4UnitaPubb unita, int sequenza, boolean attivo) {
        this(-1l, true, sequenza, attivo, null, unita.toDTO(), tipoSoggetto.toDTO())
    }

    SoggettoDocumento (TipoSoggetto tipoSoggetto, So4UnitaPubb unita) {
        this(tipoSoggetto, unita, 0, true)
    }

    SoggettoDocumento (TipoSoggetto tipoSoggetto, So4ComponentePubb componente) {
        this(tipoSoggetto, componente, 0, true)
    }

    SoggettoDocumento (SoggettoDocumento soggettoDocumento) {
        this(soggettoDocumento.id, true, soggettoDocumento.sequenza, soggettoDocumento.attivo, soggettoDocumento.utente, soggettoDocumento.unita,
             soggettoDocumento.tipoSoggetto)
    }

    SoggettoDocumento (SoggettoDocumento soggettoDocumento, int sequenza) {
        this(soggettoDocumento.id, true, sequenza, soggettoDocumento.attivo, soggettoDocumento.utente, soggettoDocumento.unita,
             soggettoDocumento.tipoSoggetto)
    }

    SoggettoDocumento (SoggettoDocumento soggetto, List<SoggettoDocumento> listaSoggetti) {
        this(soggetto)
        this.lista = (listaSoggetti?:[]).sort { it.sequenza }
    }

    SoggettoDocumento (Collection<ISoggettoDocumento> soggetti) {
        this((soggetti.find { it.attivo })?:soggetti.last())
        // se ho un solo soggetto, questo è quello attivo e quindi non ho dei soggetti "multipli"
        if (soggetti.size() > 1) {
            lista = soggetti.sort { it.sequenza }.collect { new SoggettoDocumento(it) }
        } else {
            lista = []
        }
    }

    SoggettoDocumento (ISoggettoDocumento soggettoDocumento) {
        this(soggettoDocumento.id ?: -1l, false, soggettoDocumento.sequenza, soggettoDocumento.attivo, soggettoDocumento.utenteAd4?.toDTO(),
             soggettoDocumento.unitaSo4?.toDTO(), soggettoDocumento.tipoSoggetto.toDTO())
    }

    SoggettoDocumento (TipoSoggetto tipoSoggetto, Ad4Utente utente, So4UnitaPubb unita) {
        this(-1l, true, 0, true, utente.toDTO(), unita?.toDTO(), tipoSoggetto.toDTO())
    }

    SoggettoDocumento (Long id, boolean modificato, int sequenza, boolean attivo, Ad4UtenteDTO utente, So4UnitaPubbDTO unita, TipoSoggettoDTO tipoSoggetto) {
        this.id = id
        this.modificato = modificato
        this.sequenza = sequenza
        this.attivo = attivo
        this.utente = utente
        this.unita = unita
        this.tipoSoggetto = tipoSoggetto
        this.lista = []
    }

    long getId () {
        return id
    }

    boolean getModificato () {
        return modificato
    }

    int getSequenza () {
        return sequenza
    }

    boolean getAttivo () {
        return attivo
    }

    Ad4UtenteDTO getUtente () {
        return utente
    }

    So4UnitaPubbDTO getUnita () {
        return unita
    }

    TipoSoggettoDTO getTipoSoggetto () {
        return tipoSoggetto
    }

    String getWarnMessage () {
        return warnMessage
    }

    List<SoggettoDocumento> getLista () {
        return lista
    }

    void spostaSoggettoSu (int index) {
        if (index == 0) {
            return
        }

        this.lista[index].sequenza --
        this.lista[index - 1].sequenza ++
        this.lista.sort(true, { it.sequenza })
    }

    void spostaSoggettoGiu (int index) {
        if ((index + 1) >= this.lista.size()) {
            return
        }

        this.lista[index].sequenza ++
        this.lista[index + 1].sequenza --
        this.lista.sort(true, { it.sequenza })
    }

    /**
     * @return La descrizione del soggetto da mostrare in maschera
     */
    String getDescrizione () {
        if (tipoSoggetto.categoria == TipoSoggetto.CATEGORIA_UNITA && unita != null) {
            return unita.descrizione
        } else if (tipoSoggetto.categoria == TipoSoggetto.CATEGORIA_COMPONENTE && utente != null) {
            return utente.nominativoSoggetto
        } else {
            return "SOGGETTO NON VALIDO"
        }
    }
}
