package it.finmatica.zkutils

import org.zkoss.util.resource.Labels

/**
 * Created by esasdelli on 28/09/2017.
 */
class LabelUtils {

    static String getLabel (String label, Object... args) {
        if (args == null) {
            // può succedere (per sbaglio...) che arrivi NULL come secondo parametro.
            // in questo caso, meglio mostrare la label con scritto NULL piuttosto che dare un errore e bloccare l'esecuzione per questa inezia.
            return Labels.getLabel(label, [null])
        }
        return Labels.getLabel(label, args)
    }
}
