package it.finmatica.atti.integrazioni.lookup

import grails.util.Holders
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.LookupValori
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import grails.plugin.springsecurity.SpringSecurityService

class LookupUfficio implements LookupValori {

    public final static LookupUfficio INSTANCE = new LookupUfficio()

    private LookupUfficio() {

    }

    List<CodiceDescrizione> getValori() {
        SpringSecurityService springSecurityService = (SpringSecurityService) Holders.getApplicationContext().getBean("springSecurityService")
        String codiceOttica = springSecurityService.principal.ottica().codice

        return So4UnitaPubb.allaData(new Date()).perOttica(codiceOttica).list(sort: 'descrizione', order: 'asc').collect { So4UnitaPubb unitaPubb ->
               new CodiceDescrizione(unitaPubb.codice, unitaPubb.descrizione)
        }
    }
}
