package it.finmatica.atti.documenti.storico

import it.finmatica.atti.documenti.*
import it.finmatica.gestioneiter.motore.WkfIter

/**
 * Created by czappavigna on 12/12/2017.
 */
class StoricoService {

    List<WkfIter> getIterStorico (Determina d) {
        return getListaIter(DeterminaStorico, d.id, "idDetermina")
    }

    List<WkfIter> getIterStorico (Delibera d) {
        return getListaIter(DeliberaStorico, d.id, "idDelibera")
    }

    List<WkfIter> getIterStorico (PropostaDelibera p) {
        return getListaIter(PropostaDeliberaStorico, p.id, "idPropostaDelibera")
    }

    List<WkfIter> getIterStorico (VistoParere v) {
        return getListaIter(VistoParereStorico, v.id, "idVistoParere")
    }

    List<WkfIter> getIterStorico (Certificato d) {
        return []
    }

    private List<WkfIter> getListaIter (Class<?> classStorico, Long id, String prop) {
        return classStorico.createCriteria().list {
            projections {
                distinct "iter"
            }
            eq(prop, id)
        }
    }
}
