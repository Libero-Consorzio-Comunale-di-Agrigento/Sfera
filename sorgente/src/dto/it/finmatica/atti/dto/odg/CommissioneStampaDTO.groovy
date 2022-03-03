package it.finmatica.atti.dto.odg

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dto.impostazioni.CaratteristicaTipologiaDTO
import it.finmatica.atti.odg.CommissioneStampa
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class CommissioneStampaDTO implements it.finmatica.dto.DTO<CommissioneStampa> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    String codice;
    String titolo;
    String descrizione;
    CommissioneDTO commissione;
    GestioneTestiModelloDTO modelloTesto;
    CaratteristicaTipologiaDTO caratteristicaTipologia;
    Long progressivoCfgIter;
    boolean usoNelVisualizzatore;
    Date dateCreated;
    Date lastUpdated;
    boolean valido;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;


    public CommissioneStampa getDomainObject () {
        return CommissioneStampa.get(this.id)
    }

    public CommissioneStampa copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
