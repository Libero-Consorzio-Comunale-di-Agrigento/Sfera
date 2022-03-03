package it.finmatica.atti.dto.commons

import it.finmatica.atti.commons.FileAllegatoStorico
import it.finmatica.dto.DtoUtils

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class FileAllegatoStoricoDTO implements it.finmatica.dto.DTO<FileAllegatoStorico> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    byte[] allegato;
    String contentType;
    String contentTypeOriginale;
    long dimensione;
    boolean firmato;
    Long idFileEsterno;
    boolean modificabile;
    String nome;
    String nomeOriginale;
    String testo;


    public FileAllegatoStorico getDomainObject () {
        return FileAllegatoStorico.get(this.id)
    }

    public FileAllegatoStorico copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
