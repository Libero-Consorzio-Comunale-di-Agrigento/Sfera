package it.finmatica.atti.dto.dizionari

import it.finmatica.atti.dizionari.Registro
import it.finmatica.dto.DtoUtils

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class RegistroDTO implements it.finmatica.dto.DTO<Registro> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    int anno;
    Date dataUltimoNumero;
    TipoRegistroDTO tipoRegistro;
    int ultimoNumero;
    boolean valido;
    Date validoAl;
    Date validoDal;


    public Registro getDomainObject () {
        return Registro.get(this.id)
    }

    public Registro copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
