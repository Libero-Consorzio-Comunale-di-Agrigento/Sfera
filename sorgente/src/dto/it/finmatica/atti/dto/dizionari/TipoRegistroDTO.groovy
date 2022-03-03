package it.finmatica.atti.dto.dizionari

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dizionari.TipoRegistro
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class TipoRegistroDTO implements it.finmatica.dto.DTO<TipoRegistro> {
    private static final long serialVersionUID = 1L;

    Long version;
    boolean automatico;
    boolean chiusuraAutomatica;
    String codice;
    Date dateCreated;
    boolean delibera;
    String descrizione;
    boolean determina;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    String registroEsterno;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;
    boolean visualizzatore;
    boolean paginaUnica;


    public TipoRegistro getDomainObject () {
        return TipoRegistro.get(this.codice)
    }

    public TipoRegistro copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
