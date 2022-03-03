package it.finmatica.atti.dto.documenti.tipologie

import it.finmatica.ad4.dto.autenticazione.Ad4RuoloDTO
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.tipologie.TipoDeterminaCompetenza
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class TipoDeterminaCompetenzaDTO implements it.finmatica.dto.DTO<TipoDeterminaCompetenza> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    boolean cancellazione;
    boolean lettura;
    boolean modifica;
    Ad4RuoloDTO ruoloAd4;
    TipoDeterminaDTO tipoDetermina;
    String titolo;
    So4UnitaPubbDTO unitaSo4;
    Ad4UtenteDTO utenteAd4;


    public TipoDeterminaCompetenza getDomainObject () {
        return TipoDeterminaCompetenza.get(this.id)
    }

    public TipoDeterminaCompetenza copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
