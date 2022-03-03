package it.finmatica.gestionetesti.lock.dto

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionetesti.lock.GestioneTestiDettaglioLock
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiTipoModelloDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class GestioneTestiDettaglioLockDTO implements it.finmatica.dto.DTO<GestioneTestiDettaglioLock> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Date dataFineLock;
    Date dataInizioLock;
    GestioneTestiLockDTO lock;
    boolean lockPermanente;
    String nomeFile;
    String note;
    GestioneTestiTipoModelloDTO tipoModello;
    String urlDocumento;
    Ad4UtenteDTO utenteFineLock;
    Ad4UtenteDTO utenteInizioLock;


    public GestioneTestiDettaglioLock getDomainObject () {
        return GestioneTestiDettaglioLock.get(this.id)
    }

    public GestioneTestiDettaglioLock copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
