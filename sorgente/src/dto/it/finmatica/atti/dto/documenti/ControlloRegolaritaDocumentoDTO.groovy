package it.finmatica.atti.dto.documenti

import it.finmatica.atti.documenti.ControlloRegolaritaDocumento
import it.finmatica.atti.dto.dizionari.EsitoControlloRegolaritaDTO
import it.finmatica.dto.DtoUtils

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class ControlloRegolaritaDocumentoDTO implements it.finmatica.dto.DTO<ControlloRegolaritaDocumento> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    ControlloRegolaritaDTO controlloRegolarita;
    DeliberaDTO delibera;
    DeterminaDTO determina;
    EsitoControlloRegolaritaDTO esitoControlloRegolarita;
    String note;
    boolean notificata;


    public ControlloRegolaritaDocumento getDomainObject () {
        return ControlloRegolaritaDocumento.get(this.id)
    }

    public ControlloRegolaritaDocumento copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
