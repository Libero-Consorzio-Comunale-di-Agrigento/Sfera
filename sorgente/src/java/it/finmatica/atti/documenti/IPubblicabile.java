package it.finmatica.atti.documenti;

import java.util.Date;

public interface IPubblicabile {

    ITipologiaPubblicazione getTipologiaPubblicazione ();

    /**
     * Id Del documento Albo su JMESSI
     *
     * @param idDocumentoAlbo
     */
    void setIdDocumentoAlbo (Long idDocumentoAlbo);

    Long getIdDocumentoAlbo ();

    /**
     * il numero dell'albo
     *
     * @param numeroAlbo
     */
    void setNumeroAlbo (Integer numeroAlbo);

    Integer getNumeroAlbo ();

    /**
     * l'anno dell'albo
     *
     * @param annoAlbo
     */
    void setAnnoAlbo (Integer annoAlbo);

    Integer getAnnoAlbo ();

    /**
     * Indica se il documento rimane in pubblicazione fintanto che l'utente non lo toglie
     *
     * @param pubblicaRevoca
     */
    void setPubblicaRevoca (boolean pubblicaRevoca);

    boolean isPubblicaRevoca ();

    /**
     * Indica i giorni della durata della pubblicazione.
     *
     * @param giorniPubblicazione
     */
    void setGiorniPubblicazione (Integer giorniPubblicazione);

    Integer getGiorniPubblicazione ();

    /**
     * Indica la data di inizio pubblicazione.
     *
     * @param dataPubblicazione
     */
    void setDataPubblicazione (Date dataPubblicazione);

    Date getDataPubblicazione ();

    /**
     * Indica la data di seconda pubblicazione.
     *
     * @param dataPubblicazione
     */
    void setDataPubblicazione2 (Date dataPubblicazione);

    Date getDataPubblicazione2 ();

    /**
     * Data di fine pubblicazione
     *
     * @param dataPubblicazione
     */
    void setDataFinePubblicazione (Date dataPubblicazione);

    Date getDataFinePubblicazione ();

    /**
     * Data di fine seconda pubblicazione
     *
     * @param dataPubblicazione
     */
    void setDataFinePubblicazione2 (Date dataPubblicazione);

    Date getDataFinePubblicazione2 ();


    /**
     * Indica la data minima di pubblicazione
     */
    void setDataMinimaPubblicazione (Date dataMinimaPubblicazione);

    Date getDataMinimaPubblicazione ();

    /**
     * Indica se il documento è da pubblicare o no.
     * Serve in particolare per la pubblicazione "nel futuro": ci sono alcuni casi in cui l'utente può scegliere se impostare la data di pubblicazione nel futuro.
     *
     * @param daPubblicare
     */
    void setDaPubblicare (boolean daPubblicare);

    boolean isDaPubblicare ();
}
