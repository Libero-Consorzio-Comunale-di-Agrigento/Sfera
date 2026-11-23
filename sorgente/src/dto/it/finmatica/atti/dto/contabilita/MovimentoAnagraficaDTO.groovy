package it.finmatica.atti.dto.contabilita

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.contabilita.MovimentoAnagrafica
import it.finmatica.atti.dto.documenti.DeterminaDTO
import it.finmatica.atti.dto.documenti.PropostaDeliberaDTO
import it.finmatica.dto.DTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

class MovimentoAnagraficaDTO implements DTO<MovimentoAnagrafica> {
    private static final long serialVersionUID = 1L;

    Long id
    Long version;

    DeterminaDTO        determina
    PropostaDeliberaDTO propostaDelibera

    long idEsterno  //chiave applicativo contabilità
    String ragioneSociale
    String cognome
    String nome
    String codiceFiscale
    String partitaIva
    String tipo

    Date dataNascita
    String comuneNascita
    String siglaProvinciaNascita

    String comune
    String cap
    String siglaProvincia
    String email
    String telefono
    String cellulare
    String idApplicativoEsterno
    String codiceApplicativoEsterno
    String indirizzo
    String civico
    String tipoIndirizzo

    So4AmministrazioneDTO ente
    Date                  dateCreated
    Ad4UtenteDTO          utenteIns
    Date                  lastUpdated
    Ad4UtenteDTO          utenteUpd

    MovimentoAnagrafica getDomainObject() {
        return MovimentoAnagrafica.get(this.id)
    }

    MovimentoAnagrafica copyToDomainObject() {
        return null
    }

    /* * * codice personalizzato * * */
    // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

    public def getProposta() {
        return determina ?: propostaDelibera;
    }
}
