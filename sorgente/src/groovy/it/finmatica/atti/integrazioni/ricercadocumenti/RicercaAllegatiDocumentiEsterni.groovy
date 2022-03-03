package it.finmatica.atti.integrazioni.ricercadocumenti

import it.finmatica.atti.dto.documenti.AllegatoDTO
import it.finmatica.atti.dto.documenti.DocumentoCollegatoDTO
import it.finmatica.atti.integrazioni.ricercadocumenti.CampiRicerca
import it.finmatica.atti.integrazioni.ricercadocumenti.DocumentoEsterno
import it.finmatica.atti.integrazioni.ricercadocumenti.PagedList

/**
 * Descrive le operazioni necessarie per effettuare una ricerca di allegati associati ai documenti su altri documentali
 * per poter poi effettuare inserimento dei file allegati.
 *
 * Created by dscandurra on 15/11/2017.
 */
interface RicercaAllegatiDocumentiEsterni {

    boolean isAbilitato ()

    String getTitolo ()

    String getDescrizione ()

    String getZulCampiRicerca ()

    PagedList<AllegatoEsterno> ricerca (CampiRicerca campiRicerca)

    CampiRicerca getCampiRicerca ()

}
