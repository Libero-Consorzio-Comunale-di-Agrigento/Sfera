package it.finmatica.atti.dto.documenti

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.ControlloRegolarita
import it.finmatica.atti.dto.dizionari.TipoControlloRegolaritaDTO
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class ControlloRegolaritaDTO implements it.finmatica.dto.DTO<ControlloRegolarita> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    String ambito;
    Integer annoProtocollo;
    Integer attiDaEstrarre;
    String criteriRicerca;
    Date dataEsecutivitaAl;
    Date dataEsecutivitaDal;
    Date dateCreated;
    Date dataEstrazione;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    GestioneTestiModelloDTO modelloTesto;
    Integer numeroProtocollo;
    boolean percentuale;
    String stato;
    TipoControlloRegolaritaDTO tipoControlloRegolarita;
    TipoRegistroDTO tipoRegistro;
    Integer totaleAtti;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;


    public ControlloRegolarita getDomainObject () {
        return ControlloRegolarita.get(this.id)
    }

    public ControlloRegolarita copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
