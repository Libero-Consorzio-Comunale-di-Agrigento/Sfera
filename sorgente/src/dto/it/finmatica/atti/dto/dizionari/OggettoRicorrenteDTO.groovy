package it.finmatica.atti.dto.dizionari

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dizionari.OggettoRicorrente
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class OggettoRicorrenteDTO implements it.finmatica.dto.DTO<OggettoRicorrente> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    Date dateCreated;
    boolean delibera;
    boolean determina;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    String codice;
    String oggetto;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;
    boolean cigObbligatorio;
    String servizioFornitura;
    String tipo;
    String norma;
    String modalita;


    public OggettoRicorrente getDomainObject () {
        return OggettoRicorrente.get(this.id)
    }

    public OggettoRicorrente copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
