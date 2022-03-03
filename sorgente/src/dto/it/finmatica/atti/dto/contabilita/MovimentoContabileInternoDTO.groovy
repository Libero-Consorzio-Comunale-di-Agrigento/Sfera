package it.finmatica.atti.dto.contabilita

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.contabilita.MovimentoContabileInterno
import it.finmatica.atti.dto.documenti.DeterminaDTO
import it.finmatica.atti.dto.documenti.PropostaDeliberaDTO
import it.finmatica.dto.DTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO

@GrailsCompileStatic
class MovimentoContabileInternoDTO implements DTO<MovimentoContabileInterno> {
    private static final long serialVersionUID = 1L;

    Long id
    Long version;

    DeterminaDTO        determina
    PropostaDeliberaDTO propostaDelibera

    String     esercizio
    String     capitolo
    String     descrizioneCapitolo
    String     articolo
    String     epf
    String     pdcf
    String     eos
    BigDecimal importo
    String     progressivoSoggetto
    String     descrizioneSoggetto
    String     codiceMissione
    String     codiceProgramma
    String     cognome
    String     nome
    String     cf
    String     piva
    String     cfEstero
    String     pivaEstero
    String     indirizzo
    String     localita
    String     comune
    String     provincia
    String     cap
    String     stato
    String     telefono
    String     email
    String     pec
    String     note

    So4AmministrazioneDTO ente
    Date                  dateCreated
    Ad4UtenteDTO          utenteIns
    Date                  lastUpdated
    Ad4UtenteDTO          utenteUpd

    MovimentoContabileInterno getDomainObject() {
        return MovimentoContabileInterno.get(this.id)
    }

    MovimentoContabileInterno copyToDomainObject() {
        return null
    }

    /* * * codice personalizzato * * */
    // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

    public def getProposta() {
        return determina ?: propostaDelibera;
    }
}
