package it.finmatica.atti.dto.documenti

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.GruppoPoliticoDocumento
import it.finmatica.atti.odg.dizionari.GruppoPolitico
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO
import it.finmatica.atti.dto.documenti.PropostaDeliberaDTO
import it.finmatica.atti.dto.odg.dizionari.GruppoPoliticoDTO
import it.finmatica.atti.dto.documenti.DeliberaDTO
import it.finmatica.as4.dto.As4SoggettoCorrenteDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class GruppoPoliticoDocumentoDTO implements it.finmatica.dto.DTO<GruppoPoliticoDocumento> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Date dateCreated;

    String descrizione;
    String titolo

    GruppoPoliticoDTO gruppoPolitico
    PropostaDeliberaDTO propostaDelibera
    DeliberaDTO delibera
    As4SoggettoCorrenteDTO componente


    Date lastUpdated;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;


    public GruppoPoliticoDocumento getDomainObject () {
        return GruppoPoliticoDocumento.get(this.id)
    }

    public GruppoPoliticoDocumento copyToDomainObject () {
        return null
    }


}
