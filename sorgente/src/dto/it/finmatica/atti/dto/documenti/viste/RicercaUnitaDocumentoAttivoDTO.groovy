package it.finmatica.atti.dto.documenti.viste

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.viste.RicercaUnitaDocumentoAttivo
import it.finmatica.atti.dto.dizionari.TipoRegistroDTO
import it.finmatica.atti.dto.impostazioni.TipoSoggettoDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestioneiter.dto.motore.WkfIterDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
class RicercaUnitaDocumentoAttivoDTO implements it.finmatica.dto.DTO<RicercaUnitaDocumentoAttivo> {
    private static final long serialVersionUID = 1L;

    String 	tipoDocumento
    Long	idDocumento
    Long 	idDocumentoPadre
    Long	idAtto
    Long	idProposta
    WkfIterDTO iter
    Long	annoProposta
    Long	numeroProposta
    TipoRegistroDTO registroProposta

    Long	annoAtto
    Long	numeroAtto
    TipoRegistroDTO registroAtto

    String oggetto
    String stato

    Date dataEsecutivita
    Date dataPubblicazioneDal
    Date dataPubblicazioneAl

    TipoSoggettoDTO tipoSoggetto
    Ad4UtenteDTO utenteSoggetto
    So4UnitaPubbDTO unitaSoggetto

    Ad4UtenteDTO utenteAttore
    So4UnitaPubbDTO unitaAttore

    So4AmministrazioneDTO ente


    RicercaUnitaDocumentoAttivo getDomainObject () {
        return RicercaUnitaDocumentoAttivo.get(this.idDocumento)
    }

    public RicercaUnitaDocumentoAttivo copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
