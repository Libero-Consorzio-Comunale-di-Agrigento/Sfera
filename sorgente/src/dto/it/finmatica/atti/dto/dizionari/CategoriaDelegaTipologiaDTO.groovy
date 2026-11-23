package it.finmatica.atti.dto.dizionari

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dizionari.CategoriaDelega
import it.finmatica.atti.dizionari.CategoriaDelegaTipologia
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class CategoriaDelegaTipologiaDTO implements it.finmatica.dto.DTO<CategoriaDelegaTipologia> {
    private static final long serialVersionUID = 1L;

    Long idCategoriaDelega;
    Long idTipoDelibera;

    public CategoriaDelegaTipologia getDomainObject () {
        return CategoriaDelegaTipologia.findByIdCategoriaDelegaAndIdTipoDelibera(idCategoriaDelega,idTipoDelibera)
    }

    public CategoriaDelegaTipologia copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
