package it.finmatica.atti.dto.impostazioni

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.impostazioni.RegolaCampo
import grails.compiler.GrailsCompileStatic
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO
import it.finmatica.gestioneiter.dto.configuratore.dizionari.WkfTipoOggettoDTO
import it.finmatica.gestioneiter.dto.configuratore.dizionari.WkfAttoreDTO

@GrailsCompileStatic
public class RegolaCampoDTO implements it.finmatica.dto.DTO<RegolaCampo> {
    private static final long serialVersionUID = 1L;

    Long id;
    WkfTipoOggettoDTO tipoOggetto;
    String blocco;
    String campo;
    WkfAttoreDTO wkfAttore;
    boolean visibile;
    boolean modificabile;
    boolean invertiRegola;
    boolean valido;
    So4AmministrazioneDTO ente;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    Date dateCreated;
    Date lastUpdated;
    Long version;


    public RegolaCampo getDomainObject () {
        return RegolaCampo.get(this.id)
    }

    public RegolaCampo copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
