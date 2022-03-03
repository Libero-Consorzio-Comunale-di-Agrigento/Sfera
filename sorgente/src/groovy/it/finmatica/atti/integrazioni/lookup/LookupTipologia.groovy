package it.finmatica.atti.integrazioni.lookup

import grails.compiler.GrailsCompileStatic
import it.finmatica.atti.documenti.tipologie.TipoDelibera
import it.finmatica.atti.documenti.tipologie.TipoDetermina
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.LookupValori

class LookupTipologia implements LookupValori {

    public final static LookupTipologia INSTANCE = new LookupTipologia()

    private LookupTipologia() {

    }

    List<CodiceDescrizione> getValori() {
        return (TipoDetermina.findAllByCodiceEsternoIsNotNullAndValido(true, [sort: "titolo", order: "asc"]) +
                TipoDelibera.findAllByCodiceEsternoIsNotNullAndValido(true, [sort: "titolo", order: "asc"])).collect {
            new CodiceDescrizione(it.codiceEsterno, it.titolo)
        }
    }
}
