package it.finmatica.gestionedocumenti.documenti

import groovy.transform.CompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dto.impostazioni.CaratteristicaTipologiaDTO
import it.finmatica.dto.DtoUtils
import it.finmatica.gestionetesti.reporter.dto.GestioneTestiModelloDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

@CompileStatic
class TipoDocumentoDTO implements it.finmatica.dto.DTO<TipoDocumento> {
    private static final long serialVersionUID = 1L

    Long                       id
    Long                       version
    String                     codice
    String                     commento
    String                     acronimo
    boolean                    conservazioneSostitutiva
    CaratteristicaTipologiaDTO caratteristicaTipologia
    Date                       dateCreated
    String                     descrizione
    So4AmministrazioneDTO      ente
    Date                       lastUpdated
    Long                       progressivoCfgIter
    boolean                    testoObbligatorio
    Ad4UtenteDTO               utenteIns
    Ad4UtenteDTO               utenteUpd
    boolean                    valido
    GestioneTestiModelloDTO    modelloTesto

    TipoDocumento getDomainObject () {
        return TipoDocumento.get(this.id)
    }

    TipoDocumento copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.
}
