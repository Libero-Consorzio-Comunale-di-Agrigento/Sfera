package it.finmatica.atti.impostazioni

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.dto.impostazioni.PreferenzaUtenteDTO
import it.finmatica.atti.exceptions.AttiRuntimeException

/**
 * Created by czappavigna on 26/03/2019.
 */
class PreferenzaUtenteService {
    SpringSecurityService springSecurityService

    public String getPreferenzaUtenteCorrente (String codice) {
        Preferenza preferenza = Preferenza.findByCodiceAndEnte(codice, springSecurityService.principal.amministrazione.codice)
        if (preferenza == null){
            return null;
        }
        PreferenzaUtente preferenzaUtente = PreferenzaUtente.findByUtenteAndPreferenza(springSecurityService.currentUser, preferenza)
        return (preferenzaUtente?.valore ?: preferenza.valoreDefault)
    }

    public String getPreferenzaUtente (String codice, Ad4Utente utente) {
        Preferenza preferenza = Preferenza.findByCodiceAndEnte(codice, springSecurityService.principal.amministrazione.codice)
        if (preferenza == null){
            return null;
        }
        PreferenzaUtente preferenzaUtente = PreferenzaUtente.findByUtenteAndPreferenza(utente, preferenza)
        return (preferenzaUtente?.valore ?: preferenza.valoreDefault)
    }

    public void salva (PreferenzaUtenteDTO preferenzaUtenteDTO) {
        PreferenzaUtente preferenzaUtente = preferenzaUtenteDTO.id > 0 ? PreferenzaUtente.get(preferenzaUtenteDTO.id) : new PreferenzaUtente()
        preferenzaUtente.preferenza = preferenzaUtenteDTO.preferenza.domainObject
        preferenzaUtente.utente     = preferenzaUtenteDTO.utente.domainObject
        preferenzaUtente.valore     = preferenzaUtenteDTO.valore
        preferenzaUtente.save()
    }

    public PreferenzaUtenteDTO salva (String codice, String valore){
        Preferenza preferenza = Preferenza.findByCodiceAndEnte(codice, springSecurityService.principal.amministrazione.codice)
        if (preferenza==null){
            throw new AttiRuntimeException("Impossibile salvare una preferenza utente, non esiste il la preferenza ${codice} per l'amministrazione ${springSecurityService.principal.amministrazione}")
        }
        PreferenzaUtente preferenzaUtente = PreferenzaUtente.findByUtenteAndPreferenza(springSecurityService.currentUser, preferenza)
        if (preferenzaUtente == null){
            preferenzaUtente = new PreferenzaUtente()
            preferenzaUtente.preferenza = preferenza
            preferenzaUtente.utente = springSecurityService.currentUser
        }
        preferenzaUtente.valore = valore
        preferenzaUtente.save()
        return preferenzaUtente.toDTO()
    }

    public void rimuovi (String codice){
        Preferenza preferenza = Preferenza.findByCodiceAndEnte(codice, springSecurityService.principal.amministrazione.codice)
        if (preferenza==null){
            throw new AttiRuntimeException("Impossibile eliminare una preferenza utente, non esiste il la preferenza ${codice} per l'amministrazione ${springSecurityService.principal.amministrazione}")
        }
        PreferenzaUtente preferenzaUtente = PreferenzaUtente.findByUtenteAndPreferenza(springSecurityService.currentUser, preferenza)
        if (preferenzaUtente != null){
            preferenzaUtente.delete()
        }
    }


    public def getPreferenzeUtente (){
        def preferenze = new  ArrayList();
        def listaPreferenze = Preferenza.findAllByEnte(springSecurityService.principal.amministrazione.codice)
        for (Preferenza preferenza : listaPreferenze) {
            PreferenzaUtente preferenzaUtente = PreferenzaUtente.findByUtenteAndPreferenza(springSecurityService.currentUser, preferenza)
            def listaValori = this.class.classLoader.loadClass(preferenza.nomeMetodo).newInstance()?.valori
            def valore = listaValori.findAll {it.codice == (preferenzaUtente?.valore ?: preferenza.valoreDefault)}
            preferenze.add([codice : preferenza.codice,
                           descrizione: preferenza.descrizione,
                           etichetta: preferenza.etichetta,
                           valore : valore.size() > 0 ? valore.get(0): null,
                           listaValori: listaValori])
        }
        return preferenze
    }
}
