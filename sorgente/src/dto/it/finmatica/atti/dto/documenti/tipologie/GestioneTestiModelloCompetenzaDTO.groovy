package it.finmatica.atti.dto.documenti.tipologie

import it.finmatica.ad4.dto.autenticazione.Ad4RuoloDTO
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.tipologie.GestioneTestiModelloCompetenza
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class GestioneTestiModelloCompetenzaDTO implements it.finmatica.dto.DTO<GestioneTestiModelloCompetenza> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    boolean cancellazione;
    GestioneTestiModelloDTO gestioneTestiModello;
    boolean lettura;
    boolean modifica;
    Ad4RuoloDTO ruoloAd4;
    String titolo;
    So4UnitaPubbDTO unitaSo4;
    Ad4UtenteDTO utenteAd4;


    public GestioneTestiModelloCompetenza getDomainObject () {
        return GestioneTestiModelloCompetenza.get(this.id)
    }

    public GestioneTestiModelloCompetenza copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
