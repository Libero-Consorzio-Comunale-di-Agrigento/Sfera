package it.finmatica.atti.dto.documenti.viste

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.viste.DocumentoStep
import it.finmatica.atti.documenti.viste.So4Delega

@GrailsCompileStatic
public class So4DelegaDTO implements it.finmatica.dto.DTO<So4Delega> {
    private static final long serialVersionUID = 1L;

    Long      id
    Ad4UtenteDTO deleganteUtente
    Ad4UtenteDTO delegatoUtente
    String tipologia
    Long    progressivoUnita
    String  codiceOttica
    String istanzaApplicativo
    String moduloApplicativo

    Ad4UtenteDTO utenteAggiornamento
    Date      dataAggiornamento
    Date dal
    Date al


    public So4Delega getDomainObject () {
        return So4Delega.get(this.id)
    }

    public So4Delega copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
