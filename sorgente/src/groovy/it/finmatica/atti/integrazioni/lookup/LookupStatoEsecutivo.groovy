package it.finmatica.atti.integrazioni.lookup

import groovy.transform.CompileStatic
import it.finmatica.atti.documenti.StatoDocumento
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.LookupValori

@CompileStatic
class LookupStatoEsecutivo implements LookupValori {

    public static final LookupStatoEsecutivo INSTANCE = new LookupStatoEsecutivo()

    private LookupStatoEsecutivo() {

    }

    @Override
    List<CodiceDescrizione> getValori() {
        return [new CodiceDescrizione(StatoDocumento.ESECUTIVO.toString(), "Esecutivo")]
    }
}
