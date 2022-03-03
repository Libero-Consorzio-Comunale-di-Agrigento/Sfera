package it.finmatica.atti.dto.odg

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.as4.dto.As4SoggettoCorrenteDTO
import it.finmatica.atti.dto.odg.dizionari.IncaricoDTO
import it.finmatica.atti.dto.odg.dizionari.RuoloPartecipanteDTO
import it.finmatica.atti.odg.CommissioneComponente
import it.finmatica.dto.DtoUtils

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class CommissioneComponenteDTO implements it.finmatica.dto.DTO<CommissioneComponente> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    CommissioneDTO commissione;
    As4SoggettoCorrenteDTO componente;
    Date dateCreated;
    boolean firmatario;
    IncaricoDTO incarico;
    Date lastUpdated;
    RuoloPartecipanteDTO ruoloPartecipante;
    int sequenza;
    int sequenzaFirma;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;
    Date validoAl;
    Date validoDal;
    boolean eliminaComponente


    public CommissioneComponente getDomainObject () {
        return CommissioneComponente.get(this.id)
    }

    public CommissioneComponente copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
