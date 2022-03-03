package it.finmatica.atti.integrazioni.ricercadocumenti

import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component

/**
 * Created by dscandurra on 17/11/2017.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class NessunaRicercaAllegati implements RicercaAllegatiDocumentiEsterni {

    @Override
    boolean isAbilitato () {
        return true
    }

    @Override
    String getTitolo () {
        return "-- nessuno --"
    }

    @Override
    String getDescrizione () {
        return "nessuno"
    }

    @Override
    String getZulCampiRicerca () {
        return ""
    }

    @Override
    PagedList<DocumentoEsterno> ricerca (CampiRicerca campiRicerca) {
        return new PagedList<DocumentoEsterno>([], 0)
    }

    @Override
    CampiRicerca getCampiRicerca () {
        return new CampiRicerca()
    }
}
