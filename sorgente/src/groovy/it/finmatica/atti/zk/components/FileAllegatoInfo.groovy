package it.finmatica.atti.zk.components

import groovy.transform.CompileStatic

/**
 * Created by czappavigna on 24/01/2018.
 */
@CompileStatic
class FileAllegatoInfo {
    private final String descrizione;
    private final String tipologia;
    private final Long id

    FileAllegatoInfo (Long id, String tipologia, String descrizione) {
        this.descrizione = descrizione;
        this.tipologia = tipologia;
        this.id = id
    }

    String getDescrizione () {
        return descrizione;
    }

    String getTipologia () {
        return tipologia;
    }

    Long getId () {
        return id
    }

}
