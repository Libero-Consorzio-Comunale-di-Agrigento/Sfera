package it.finmatica.atti.dto.documenti

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.Firmatario
import it.finmatica.dto.DtoUtils

import grails.compiler.GrailsCompileStatic
import it.finmatica.gestionedocumenti.documenti.DocumentoDTO

@GrailsCompileStatic
public class FirmatarioDTO implements it.finmatica.dto.DTO<Firmatario> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    CertificatoDTO certificato;
    Date dataFirma;
    Date dateCreated;
    DeliberaDTO delibera;
    DeterminaDTO determina;
    DocumentoDTO documento;
    Ad4UtenteDTO firmatario;
    Ad4UtenteDTO firmatarioEffettivo;
    boolean firmato;
    Date lastUpdated;
    PropostaDeliberaDTO propostaDelibera;
    int sequenza;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    VistoParereDTO vistoParere;


    public Firmatario getDomainObject () {
        return Firmatario.get(this.id)
    }

    public Firmatario copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
