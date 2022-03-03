package it.finmatica.atti.dto.dizionari

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dizionari.TipoDatoAggiuntivoValore
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class TipoDatoAggiuntivoValoreDTO implements it.finmatica.dto.DTO<TipoDatoAggiuntivoValore> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    String codice;
    Date dateCreated;
    String descrizione;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    int sequenza;
    String tipoOggetto;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;


    public TipoDatoAggiuntivoValore getDomainObject () {
        return TipoDatoAggiuntivoValore.get(this.id)
    }

    public TipoDatoAggiuntivoValore copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
