package it.finmatica.atti.dto.commons

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.commons.GestioneTestiLog

@GrailsCompileStatic
public class GestioneTestiLogDTO implements it.finmatica.dto.DTO<GestioneTestiLog> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long idDocumento
    String tipoOggetto

    String stato
    String operazione
    String nomeFile
    String errore
    String estremiDocumento

    Date dataFineElaborazione

    Date dateCreated
    Ad4UtenteDTO utenteIns
    Date lastUpdated
    Ad4UtenteDTO utenteUpd

   // Long version

    public GestioneTestiLog getDomainObject () {
        return GestioneTestiLog.get(this.id)
    }

    public GestioneTestiLog copyToDomainObject () {
        return null
    }
}
