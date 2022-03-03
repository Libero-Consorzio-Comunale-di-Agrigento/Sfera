package it.finmatica.atti.dto.dizionari

import it.finmatica.atti.dizionari.TipoControlloRegolarita
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class TipoControlloRegolaritaDTO implements it.finmatica.dto.DTO<TipoControlloRegolarita> {
    private static final long serialVersionUID = 1L;

    Long id;
    String ambito;
    So4AmministrazioneDTO ente;
    Long sequenza;
    String titolo;
    boolean valido;


    public TipoControlloRegolarita getDomainObject () {
        return TipoControlloRegolarita.get(this.id)
    }

    public TipoControlloRegolarita copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
