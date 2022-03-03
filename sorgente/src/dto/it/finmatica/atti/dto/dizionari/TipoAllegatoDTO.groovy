package it.finmatica.atti.dto.dizionari

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dizionari.TipoAllegato
import it.finmatica.atti.documenti.StatoFirma
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class TipoAllegatoDTO implements it.finmatica.dto.DTO<TipoAllegato> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Date dateCreated;
    String descrizione;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    boolean pubblicaAlbo;
    boolean pubblicaCasaDiVetro;
    boolean pubblicaVisualizzatore;
    String titolo;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;
    String codice;
    GestioneTestiModelloDTO modelloTesto;
    boolean modificabile;
    String tipologia;
    boolean modificaCampi;
    boolean stampaUnica;
    StatoFirma statoFirma;
    String codiceEsterno;

    public TipoAllegato getDomainObject () {
        return TipoAllegato.get(this.id)
    }

    public TipoAllegato copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
