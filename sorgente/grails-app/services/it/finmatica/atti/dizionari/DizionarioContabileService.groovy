package it.finmatica.atti.dizionari

import grails.transaction.Transactional
import groovy.sql.Sql

class DizionarioContabileService {

    def dataSource

    @Transactional
    void update(List<DizionarioContabile> dizionarioContabileList) throws Exception{
        for(DizionarioContabile dizionarioContabile :dizionarioContabileList){
            DizionarioContabile attuale = check(dizionarioContabile.tipo, dizionarioContabile.codice)
            if(attuale){
                //aggiorno
                attuale.descrizione=dizionarioContabile.descrizione
                attuale.save()
            }else{
                //inserisco
                dizionarioContabile.save()
            }
        }
    }

    @Transactional
    void rebuild(String tipo,String ente, String utente,String xmlDati) throws Exception{
        Sql sql = new Sql(dataSource)
        sql.call("{ call utility_pkg.rebuid_diz_contabile(?,?,?,?) }",[tipo,ente, utente,xmlDati])
    }

    @Transactional(readOnly = true)
    DizionarioContabile check(String tipo, String codice){
        DizionarioContabile ret= DizionarioContabile.findByTipoAndCodiceAndValido(tipo,codice,true)
        return ret
    }

}
