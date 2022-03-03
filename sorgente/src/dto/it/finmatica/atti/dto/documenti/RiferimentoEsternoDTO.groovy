package it.finmatica.atti.dto.documenti

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.IDocumentoEsterno
import it.finmatica.atti.documenti.RiferimentoEsterno
import it.finmatica.dto.DtoUtils
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

class RiferimentoEsternoDTO implements it.finmatica.dto.DTO<RiferimentoEsterno>, IDocumentoEsterno {
    private static final long serialVersionUID = 1L

    Long    id
    Long    version
    Long    idDocumentoEsterno
    String  codiceDocumentaleEsterno
    String  titolo
    String  tipoDocumento
    boolean valido

    So4AmministrazioneDTO  ente
    Date 		    dateCreated
    Date 		    lastUpdated
    Ad4UtenteDTO    utenteIns
    Ad4UtenteDTO 	utenteUpd


    RiferimentoEsterno getDomainObject () {
        return RiferimentoEsterno.get(this.id)
    }

    RiferimentoEsterno copyToDomainObject () {
        return DtoUtils.copyToDomainObject(this)
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
