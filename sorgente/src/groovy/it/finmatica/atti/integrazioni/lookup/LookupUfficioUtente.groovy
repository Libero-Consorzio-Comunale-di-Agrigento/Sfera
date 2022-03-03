package it.finmatica.atti.integrazioni.lookup

import grails.plugin.springsecurity.SpringSecurityService
import grails.util.Holders
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.integrazioni.parametri.CodiceDescrizione
import it.finmatica.atti.integrazioni.parametri.LookupValori
import it.finmatica.so4.login.detail.UnitaOrganizzativa

class LookupUfficioUtente implements LookupValori {
    public final static LookupUfficioUtente INSTANCE = new LookupUfficioUtente()

    private LookupUfficioUtente() {

    }
    List<CodiceDescrizione> getValori() {
        SpringSecurityService springSecurityService = (SpringSecurityService) Holders.getApplicationContext().getBean("springSecurityService")
        String ruoloCreaDelibera = Impostazioni.RUOLO_SO4_CREA_PROPOSTA_DELIBERA.valore
        String ruoloCreaDetermina = Impostazioni.RUOLO_SO4_CREA_DETERMINA.valore

        return springSecurityService.principal.uo().findAll {it.ruoli*.codice.contains(ruoloCreaDelibera) or it.ruoli*.codice.contains(ruoloCreaDetermina) }.collect { UnitaOrganizzativa uo->
            new CodiceDescrizione(uo.id.toString(), uo.descrizione)
        }
    }
}
