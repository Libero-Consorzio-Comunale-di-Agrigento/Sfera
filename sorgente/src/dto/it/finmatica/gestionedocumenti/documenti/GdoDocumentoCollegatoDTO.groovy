package it.finmatica.gestionedocumenti.documenti

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.DocumentoCollegato
import it.finmatica.dto.DtoUtils

public class GdoDocumentoCollegatoDTO implements it.finmatica.dto.DTO<DocumentoCollegato> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    DocumentoDTO collegato;
    Date dateCreated;
    DocumentoDTO documento;
    Date lastUpdated;
    TipoCollegamentoDTO tipoCollegamento;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;
    boolean valido;


    public DocumentoCollegato getDomainObject () {
        return DocumentoCollegato.get(this.id)
    }

    public DocumentoCollegato copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
