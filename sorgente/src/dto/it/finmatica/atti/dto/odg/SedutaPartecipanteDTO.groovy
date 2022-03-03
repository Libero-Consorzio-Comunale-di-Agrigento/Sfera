package it.finmatica.atti.dto.odg

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.dto.odg.dizionari.IncaricoDTO
import it.finmatica.atti.dto.odg.dizionari.RuoloPartecipanteDTO
import it.finmatica.atti.odg.SedutaPartecipante
import it.finmatica.dto.DtoUtils

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class SedutaPartecipanteDTO implements it.finmatica.dto.DTO<SedutaPartecipante> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    boolean assenteNonGiustificato;
    CommissioneComponenteDTO commissioneComponente;
    As4SoggettoCorrenteDTO componenteEsterno;
    boolean convocato;
    Date dateCreated;
    boolean firmatario;
    IncaricoDTO incarico;
    Date lastUpdated;
    Boolean presente;
    RuoloPartecipanteDTO ruoloPartecipante;
    SedutaDTO seduta;
    int sequenza;
    int sequenzaFirma;
    int sequenzaPartecipante;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;


    public SedutaPartecipante getDomainObject () {
        return SedutaPartecipante.get(this.id)
    }

    public SedutaPartecipante copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
