package it.finmatica.atti.dto.odg

import it.finmatica.ad4.dto.autenticazione.Ad4UtenteDTO
import it.finmatica.atti.documenti.StatoOdg
import it.finmatica.atti.dto.commons.FileAllegatoDTO
import it.finmatica.atti.dto.dizionari.DelegaDTO
import it.finmatica.atti.dto.documenti.DeliberaDTO
import it.finmatica.atti.dto.documenti.DeterminaDTO
import it.finmatica.atti.dto.documenti.PropostaDeliberaDTO
import it.finmatica.atti.dto.odg.dizionari.EsitoDTO
import it.finmatica.atti.odg.OggettoSeduta
import it.finmatica.dto.DtoUtils

import grails.compiler.GrailsCompileStatic
@GrailsCompileStatic
public class OggettoSedutaDTO implements it.finmatica.dto.DTO<OggettoSeduta> {
    private static final long serialVersionUID = 1L;

    Long id;
    Long version;
    boolean confermaEsito;
    Date dataDiscussione;
    Date dateCreated;
    DelegaDTO delega;
    DeliberaDTO delibera;
    DeterminaDTO determina;
    boolean eseguibilitaImmediata;
    String motivazioniEseguibilita;
    EsitoDTO esito;
    FileAllegatoDTO fileAllegato;
    Date lastUpdated;
    String note;
    boolean notificato;
    boolean oggettoAggiuntivo;
    String oraDiscussione;
    PropostaDeliberaDTO propostaDelibera;
    SedutaDTO seduta;
    int sequenzaConvocazione;
    int sequenzaDiscussione;
    Ad4UtenteDTO utenteIns;
    Ad4UtenteDTO utenteUpd;


    public OggettoSeduta getDomainObject () {
        return OggettoSeduta.get(this.id)
    }

    public OggettoSeduta copyToDomainObject () {
        return null
    }

    /* * * codice personalizzato * * */ // attenzione: non modificare questa riga se si vuole mantenere il codice personalizzato che segue.
    // qui è possibile inserire codice personalizzato che non verrà eliminato dalla rigenerazione dei DTO.

	it.finmatica.atti.documenti.StatoOdg getStatoOdg () {
        // il controllo sulla determina è solo velleitario: di fatto le determine non vanno in odg (per ora...)
		return (propostaDelibera?.statoOdg)?:(determina?.statoOdg)
	}

	boolean isInIstruttoria () {
		return (!confermaEsito && it.finmatica.atti.documenti.StatoOdg.isInIstruttoria(getStatoOdg()))
	}

    Map getIconaStatoOdg () {

        // se la proposta ha un esito confermato, allora non ritorno nulla.
        if (confermaEsito == true) {
            return [url: '', tooltip: '']
        }

        // se la proposta non ha un esito, allora potrebbe essere ancora in istruttoria:
        if (isInIstruttoria()) {
            return [url: '/images/agsde2/22x22/cancel.png', tooltip: 'La proposta è ancora in istruttoria.']
        }

        // se la proposta ha statoodg completo ma il suo cambio di stato è successivo alla data di fine della seduta ( http://svi-redmine/issues/22284 ) mostro un warn rosso:
        if (propostaDelibera.statoOdg == StatoOdg.INSERITO && seduta.dataOraFineSeduta?.before(propostaDelibera.iter.stepCorrente.dataInizio)) {
            return  [url: '/images/agsde2/22x22/warn-red.png', tooltip: 'La proposta ha completato la propria istruttoria dopo la discussione in Ordine del Giorno.']
        }

        // se la proposta ha statoodg completo ma il suo cambio di stato è precedente alla data di fine della seduta ( http://svi-redmine/issues/22284 ) mostro un warn giallo:
        if (propostaDelibera.statoOdg == StatoOdg.INSERITO
            && (seduta.dataOraInizioSeduta != null && seduta.dataOraInizioSeduta.before(propostaDelibera.iter.stepCorrente.dataInizio))
            && (seduta.dataOraFineSeduta   == null || seduta.dataOraFineSeduta.after(propostaDelibera.iter.stepCorrente.dataInizio))) {
            return  [url: '/images/agsde2/22x22/warn.png', tooltip: 'La proposta ha completato la propria istruttoria durante la discussione in Ordine del Giorno.']
        }

        // in tutti gli altri casi, ritorno niente
        return [url: '', tooltip: '']
    }
}
