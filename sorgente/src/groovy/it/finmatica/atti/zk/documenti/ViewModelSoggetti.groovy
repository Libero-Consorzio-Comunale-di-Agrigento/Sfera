package it.finmatica.atti.zk.documenti

import it.finmatica.atti.zk.SoggettoDocumento
import org.zkoss.bind.annotation.BindingParam
import org.zkoss.bind.annotation.Command
import org.zkoss.bind.annotation.NotifyChange

/**
 * Created by esasdelli on 06/11/2017.
 */
trait ViewModelSoggetti {
    Map<String, SoggettoDocumento> soggetti

    @NotifyChange("soggetti")
    @Command
    void onSoggettoUp (@BindingParam("soggetto") SoggettoDocumento soggetto) {
        soggetti[soggetto.tipoSoggetto.codice].spostaSoggettoSu(soggetto.sequenza)
    }

    @NotifyChange("soggetti")
    @Command
    void onSoggettoDown (@BindingParam("soggetto") SoggettoDocumento soggetto) {
        soggetti[soggetto.tipoSoggetto.codice].spostaSoggettoGiu(soggetto.sequenza)
    }
}
