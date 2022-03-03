package it.finmatica.atti.dto.documenti

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.DestinatarioNotificaAttivita
import it.finmatica.atti.dto.dizionari.NotificaDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class DestinatarioNotificaAttivitaDTO implements it.finmatica.dto.DTO<DestinatarioNotificaAttivita> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    String modalitaInvio;
    DestinatarioNotificaDTO destinatarioNotifica;
    String idAttivita;
    String idRiferimento;
    String soggettoNotifica;
    So4UnitaPubbDTO unitaSo4;
    Ad4UtenteDTO utente;
    NotificaDTO notifica;
    String tipoNotifica;


    public DestinatarioNotificaAttivita getDomainObject () {
        return DestinatarioNotificaAttivita.get(this.id)
    }

    public DestinatarioNotificaAttivita copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
