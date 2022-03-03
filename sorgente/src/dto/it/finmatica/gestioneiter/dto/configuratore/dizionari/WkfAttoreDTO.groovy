package it.finmatica.gestioneiter.dto.configuratore.dizionari

import it.finmatica.ad4.dto.autenticazione.Ad4RuoloDTO
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.configuratore.dizionari.WkfAttore
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class WkfAttoreDTO implements it.finmatica.dto.DTO<WkfAttore> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Date dateCreated;
    String descrizione;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    WkfAzioneDTO metodoDiCalcolo;
    String nome;
    Ad4RuoloDTO ruoloAd4;
    WkfTipoOggettoDTO tipoOggetto;
    So4UnitaPubbDTO unitaSo4;
    Ad4UtenteDTO utenteAd4;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;


    public WkfAttore getDomainObject () {
        return WkfAttore.get(this.id)
    }

    public WkfAttore copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.



}
