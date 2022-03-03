package it.finmatica.atti.dto.odg.dizionari

import it.finmatica.atti.odg.dizionari.TipoOrganoControllo
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class TipoOrganoControlloDTO implements it.finmatica.dto.DTO<TipoOrganoControllo> {
    private static final long serialVersionUID = 1L;

    String codice;
    So4AmministrazioneDTO ente;
    String titolo;
    boolean valido;


    public TipoOrganoControllo getDomainObject () {
        return TipoOrganoControllo.get(this.codice)
    }

    public TipoOrganoControllo copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
