package it.finmatica.atti.integrazioni.lookup

import groovy.transform.CompileStatic
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.LookupValori

@CompileStatic
class LookupTipoDocumento implements LookupValori {

    public final static LookupTipoDocumento INSTANCE = new LookupTipoDocumento()

    private LookupTipoDocumento() {

    }

    List<CodiceDescrizione> getValori() {
        return [new CodiceDescrizione(Delibera.TIPO_OGGETTO, "Delibera"),
                new CodiceDescrizione(Determina.TIPO_OGGETTO, "Determina")]
    }
}
