package it.finmatica.atti.dto.odg.dizionari

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.odg.CommissioneDTO
import it.finmatica.atti.odg.dizionari.Esito
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class EsitoDTO implements it.finmatica.dto.DTO<Esito> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    CommissioneDTO commissione;
    CommissioneDTO commissioneArrivo;
    Date dateCreated;
    String descrizione;
    So4AmministrazioneDTO ente;
    EsitoStandardDTO esitoStandard;
    TipoRegistroDTO registroDelibera;
    boolean gestioneEsecutivita;
    Date lastUpdated;
    boolean notificaVerbalizzazione;
    Long progressivoCfgIter;
    int sequenza;
    boolean testoAutomatico;
    String titolo;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;


    public Esito getDomainObject () {
        return Esito.get(this.id)
    }

    public Esito copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
