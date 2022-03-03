package it.finmatica.atti.dto.documenti

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.DeliberaSoggetto
import it.finmatica.atti.dto.impostazioni.TipoSoggettoDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class DeliberaSoggettoDTO implements it.finmatica.dto.DTO<DeliberaSoggetto> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    boolean attivo;
    DeliberaDTO delibera;
    int sequenza;
    TipoSoggettoDTO tipoSoggetto;
    So4UnitaPubbDTO unitaSo4;
    Ad4UtenteDTO utenteAd4;


    public DeliberaSoggetto getDomainObject () {
        return DeliberaSoggetto.get(this.id)
    }

    public DeliberaSoggetto copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
