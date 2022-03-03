package it.finmatica.atti.dto.dizionari

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.dizionari.Delega
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class DelegaDTO implements it.finmatica.dto.DTO<Delega> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    As4SoggettoCorrenteDTO assessore;
    Date dateCreated;
    String descrizioneAssessorato;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    int sequenza;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;
    Long idDelegaStorico;


    public Delega getDomainObject () {
        return Delega.get(this.id)
    }

    public Delega copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
