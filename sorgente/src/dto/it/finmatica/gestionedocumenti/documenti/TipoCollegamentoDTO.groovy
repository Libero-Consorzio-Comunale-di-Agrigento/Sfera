package it.finmatica.gestionedocumenti.documenti

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

public class TipoCollegamentoDTO implements it.finmatica.dto.DTO<TipoCollegamento> {
    private static final long serialVersionUID = 1L;

    Long id;
    String codice;
    String commento;
    Date dateCreated;
    String descrizione;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;


    public TipoCollegamento getDomainObject () {
        return TipoCollegamento.get(this.id)
    }

    public TipoCollegamento copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

	/* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
	// qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

}
