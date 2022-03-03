package it.finmatica.atti.dto.documenti.viste

import grails.compiler.GrailsCompileStatic
import it.finmatica.ad4.dto.autenticazione.Ad4RuoloDTO
import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.viste.DocumentoStep
import it.finmatica.atti.documenti.viste.RicercaSiav
import it.finmatica.atti.dto.documenti.*
import it.finmatica.gestioneiter.dto.motore.WkfStepDTO
import it.finmatica.so4.dto.struttura.So4AmministrazioneDTO
import it.finmatica.so4.dto.strutturaPubblicazione.So4UnitaPubbDTO

@GrailsCompileStatic
public class RicercaSiavDTO implements it.finmatica.dto.DTO<RicercaSiav> {
    private static final long serialVersionUID = 1L;

    String codiceStruttura
    String descrizione
    String codiceSiav

    public RicercaSiav getDomainObject () {
        return RicercaSiav.findByCodiceStrutturaAndCodiceSiav(this.codiceStruttura, codiceSiav)
    }

    public RicercaSiav copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.


}
