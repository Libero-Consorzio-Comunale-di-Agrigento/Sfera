package it.finmatica.atti.dto.documenti.tipologie

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.tipologie.TipoDocumentoCf
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class TipoDocumentoCfDTO implements it.finmatica.dto.DTO<TipoDocumentoCf> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    String cfTipoDocumentoCodice;
    String cfTipoDocumentoEnte;
    Date dateCreated;
    So4AmministrazioneDTO ente;
    Date lastUpdated;
    TipoDeliberaDTO tipoDelibera;
    TipoDeterminaDTO tipoDetermina;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;


    public TipoDocumentoCf getDomainObject () {
        return TipoDocumentoCf.get(this.id)
    }

    public TipoDocumentoCf copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

    it.finmatica.atti.cf.integrazione.TipoDocumentoCf tipoDocumentoCf
}
