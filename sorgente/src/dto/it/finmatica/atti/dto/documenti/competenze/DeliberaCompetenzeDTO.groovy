package it.finmatica.atti.dto.documenti.competenze

import it.finmatica.ad4.dto.autenticazione.Ad4RuoloDTO
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.competenze.DeliberaCompetenze
import it.finmatica.atti.dto.documenti.DeliberaDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.dto.configuratore.iter.WkfCfgCompetenzaDTO
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class DeliberaCompetenzeDTO implements it.finmatica.dto.DTO<DeliberaCompetenze> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    boolean cancellazione;
    WkfCfgCompetenzaDTO cfgCompetenza;
    DeliberaDTO delibera;
    boolean lettura;
    boolean modifica;
    Ad4RuoloDTO ruoloAd4;
    So4UnitaPubbDTO unitaSo4;
    Ad4UtenteDTO utenteAd4;


    public DeliberaCompetenze getDomainObject () {
        return DeliberaCompetenze.get(this.id)
    }

    public DeliberaCompetenze copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
