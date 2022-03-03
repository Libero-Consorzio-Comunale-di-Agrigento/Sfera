package it.finmatica.atti.dto.dizionari

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dizionari.CalendarioFestivita
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

public class CalendarioFestivitaDTO implements it.finmatica.dto.DTO<CalendarioFestivita> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    int giorno;
    int mese;
    Integer anno;
    String descrizione;

    Date dateCreated;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;

    public CalendarioFestivita getDomainObject () {
        return CalendarioFestivita.get(this.id)
    }

    public CalendarioFestivita copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
