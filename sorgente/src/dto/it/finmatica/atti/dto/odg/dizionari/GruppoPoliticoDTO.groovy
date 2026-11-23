package it.finmatica.atti.dto.odg.dizionari

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.odg.dizionari.GruppoPolitico
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class GruppoPoliticoDTO implements it.finmatica.dto.DTO<GruppoPolitico> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Date dateCreated;
    String descrizione;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    int sequenza;
    String titolo;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;


    public GruppoPolitico getDomainObject () {
        return GruppoPolitico.get(this.id)
    }

    public GruppoPolitico copyToDomainObject () {
        return null
    }


}
