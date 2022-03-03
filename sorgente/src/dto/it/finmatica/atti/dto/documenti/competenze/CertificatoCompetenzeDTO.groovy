package it.finmatica.atti.dto.documenti.competenze

import it.finmatica.ad4.dto.autenticazione.Ad4RuoloDTO
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.competenze.CertificatoCompetenze
import it.finmatica.atti.dto.documenti.CertificatoDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.dto.configuratore.iter.WkfCfgCompetenzaDTO
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class CertificatoCompetenzeDTO implements it.finmatica.dto.DTO<CertificatoCompetenze> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    boolean cancellazione;
    CertificatoDTO certificato;
    WkfCfgCompetenzaDTO cfgCompetenza;
    boolean lettura;
    boolean modifica;
    Ad4RuoloDTO ruoloAd4;
    So4UnitaPubbDTO unitaSo4;
    Ad4UtenteDTO utenteAd4;


    public CertificatoCompetenze getDomainObject () {
        return CertificatoCompetenze.get(this.id)
    }

    public CertificatoCompetenze copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
