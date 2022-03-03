package it.finmatica.atti.dto.odg

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dto.odg.dizionari.RuoloPartecipanteDTO
import it.finmatica.atti.dto.odg.dizionari.VotoDTO
import it.finmatica.atti.odg.OggettoPartecipante
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class OggettoPartecipanteDTO implements it.finmatica.dto.DTO<OggettoPartecipante> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    boolean assenteNonGiustificato;
    Date dateCreated;
    So4AmministrazioneDTO ente;
    boolean firmatario;
    Date lastUpdated;
    OggettoSedutaDTO oggettoSeduta;
    Boolean presente;
    RuoloPartecipanteDTO ruoloPartecipante;
    SedutaPartecipanteDTO sedutaPartecipante;
    int sequenza;
    int sequenzaFirma;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    VotoDTO voto;


    public OggettoPartecipante getDomainObject () {
        return OggettoPartecipante.get(this.id)
    }

    public OggettoPartecipante copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
