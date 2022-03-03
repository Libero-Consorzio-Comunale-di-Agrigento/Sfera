package it.finmatica.gestionetesti.lock.dto

import it.finmatica.dto.DtoUtils
import it.finmatica.gestionetesti.lock.GestioneTestiLock

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class GestioneTestiLockDTO implements it.finmatica.dto.DTO<GestioneTestiLock> {
    private static final long serialVersionUID = 1L;

    Long version;
    Set<GestioneTestiDettaglioLockDTO> dettaglioLock;
    String idRiferimentoTesto;
    boolean locked;

    public void addToDettaglioLock (GestioneTestiDettaglioLockDTO gestioneTestiDettaglioLock) {
        if (this.dettaglioLock == null)
            this.dettaglioLock = new HashSet<GestioneTestiDettaglioLockDTO>()
        this.dettaglioLock.add (gestioneTestiDettaglioLock);
        gestioneTestiDettaglioLock.lock = this
    }

    public void removeFromDettaglioLock (GestioneTestiDettaglioLockDTO gestioneTestiDettaglioLock) {
        if (this.dettaglioLock == null)
            this.dettaglioLock = new HashSet<GestioneTestiDettaglioLockDTO>()
        this.dettaglioLock.remove (gestioneTestiDettaglioLock);
        gestioneTestiDettaglioLock.lock = null
    }

    public GestioneTestiLock getDomainObject () {
        return GestioneTestiLock.get(this.idRiferimentoTesto)
    }

    public GestioneTestiLock copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
