package it.finmatica.atti.dto.documenti

import it.finmatica.atti.documenti.OrganoControlloNotificaDocumento
import it.finmatica.dto.DtoUtils

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class OrganoControlloNotificaDocumentoDTO implements it.finmatica.dto.DTO<OrganoControlloNotificaDocumento> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    DeliberaDTO delibera;
    DeterminaDTO determina;
    OrganoControlloNotificaDTO organoControlloNotifica;


    public OrganoControlloNotificaDocumento getDomainObject () {
        return OrganoControlloNotificaDocumento.get(this.id)
    }

    public OrganoControlloNotificaDocumento copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
