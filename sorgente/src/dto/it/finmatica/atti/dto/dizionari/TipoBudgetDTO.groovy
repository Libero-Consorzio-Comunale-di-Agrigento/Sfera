package it.finmatica.atti.dto.dizionari

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.dizionari.TipoBudget
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO

@GrailsCompileStatic
public class TipoBudgetDTO implements it.finmatica.dto.DTO<TipoBudget> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    String          titolo
    String          tipo
    String          contoEconomico
    int             anno
    BigDecimal      importoIniziale
    BigDecimal      importoPrenotato
    BigDecimal      importoAutorizzato
    BigDecimal      importoDisponibile
    Ad4UtenteDTO    utenteAd4
    So4UnitaPubbDTO unitaSo4
    boolean         attivo
    boolean         valido
    So4AmministrazioneDTO ente
    Date            dateCreated
    Ad4UtenteDTO    utenteIns
    Date            lastUpdated
    Ad4UtenteDTO    utenteUpd


    public TipoBudget getDomainObject() {
        return TipoBudget.get(this.id)
    }

    public TipoBudget copyToDomainObject() {
        return null
    }

    /* * * codice personalizzato * * */
    // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
