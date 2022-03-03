package it.finmatica.atti.dto.commons

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.commons.FileFirmatoDettaglio
import it.finmatica.atti.dto.commons.FileAllegatoDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

@GrailsCompileStatic
class FileFirmatoDettaglioDTO implements it.finmatica.dto.DTO<FileFirmatoDettaglio> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    FileAllegatoDTO fileAllegato
    String nominativo
    Date dataFirma
    Date dataVerifica
    String stato
    Long idDocumento

    So4AmministrazioneDTO ente
    Date dateCreated
    Ad4UtenteDTO utenteIns
    Date lastUpdated
    Ad4UtenteDTO utenteUpd

    FileFirmatoDettaglio getDomainObject () {
        return FileFirmatoDettaglio.get(this.id)
    }

    FileFirmatoDettaglio copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue. 
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
