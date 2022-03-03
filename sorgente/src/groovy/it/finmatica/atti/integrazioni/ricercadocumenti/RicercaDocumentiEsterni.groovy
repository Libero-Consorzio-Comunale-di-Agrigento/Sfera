package it.finmatica.atti.integrazioni.ricercadocumenti

import it.finmatica.atti.dto.documenti.DocumentoCollegatoDTO

/**
 * Descrive le operazioni necessarie per effettuare una ricerca di documenti su altri documentali
 * per poter poi effettuare un collegamento tra documenti. (questa seconda parte non è elegantissima... andrebbe separata in un'altra interfaccia)
 *
 * Created by esasdelli on 02/10/2017.
 */
interface RicercaDocumentiEsterni {

    boolean isAbilitato ()

    String getTitolo ()

    String getDescrizione ()

    String getZulCampiRicerca ()

    PagedList<DocumentoEsterno> ricerca (CampiRicerca campiRicerca)

    CampiRicerca getCampiRicerca ()

    DocumentoCollegatoDTO creaDocumentoCollegato (DocumentoEsterno documentoEsterno, String operazione)

}
