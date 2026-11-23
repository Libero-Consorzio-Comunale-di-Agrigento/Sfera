package atti.integrazioni.contabilita

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.contabilita.MovimentoAnagrafica
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.IProposta
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.dto.contabilita.MovimentoContabileInternoDTO
import it.finmatica.atti.integrazioni.contabilita.Anagrafica
import it.finmatica.atti.integrazioni.contabilita.IntegrazioneContabilitaJSerfin
import org.apache.log4j.Logger
import org.zkoss.bind.BindUtils
import org.zkoss.bind.annotation.*
import org.zkoss.zk.ui.event.Events
import org.zkoss.zk.ui.util.Clients
import org.zkoss.zul.Window

class PopupSceltaSoggettiJSerfinViewModel {

    public static final Logger log = Logger.getLogger(PopupSceltaSoggettiJSerfinViewModel.class);

    // service
    SpringSecurityService springSecurityService

    IntegrazioneContabilitaJSerfin integrazioneContabilitaJSerfin

    // componenti
    Window self

    // dati
    IDocumento atto
    List<Anagrafica> listaAnagrafica
    String codiceFiscale
    Anagrafica anagraficaSelezionata
    MovimentoAnagrafica movimentoAnagrafica //anagrafica nuova selezionata
    boolean isNuovaAnagrafica = false
    boolean abilitaModifica = false

    @org.zkoss.bind.annotation.Init
    init(@ContextParam(ContextType.COMPONENT) Window w
         , @ExecutionArgParam("atto") IProposta atto
         , @ExecutionArgParam("movimento") MovimentoContabileInternoDTO movimento) {

        this.self = w
        this.atto = atto
        anagraficaSelezionata = null

    }

    @NotifyChange(["abilitaModifica"])
    @Command
    void onResetFlag() {
        abilitaModifica = false
        BindUtils.postNotifyChange(null, null, this, "abilitaModifica")
    }


    @NotifyChange(["abilitaModifica"])
    @Command
    void onAbilitaModifica() {
        abilitaModifica = true
        BindUtils.postNotifyChange(null, null, this, "abilitaModifica")
     }

    @NotifyChange(["listaAnagrafica", "isNuovaAnagrafica", "anagraficaSelezionata", "abilitaModifica"])
    @Command
    void onRicercaAnagrafica(@BindingParam("codiceFiscale") String search) {
        if ((codiceFiscale == null) || (codiceFiscale.equals(""))) {
            Clients.showNotification("Inserire il codice fiscale per effettuare la ricerca.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 3000, true)
        } else {
            isNuovaAnagrafica = false
            //svuoto i campi in maschera
            anagraficaSelezionata = new Anagrafica()
            anagraficaSelezionata.ragioneSociale = ''
            anagraficaSelezionata.cognome = ''
            anagraficaSelezionata.nome = ''
            anagraficaSelezionata.codiceFiscale = ''
            anagraficaSelezionata.partitaIva = ''
            anagraficaSelezionata.indirizzo = ''
            anagraficaSelezionata.civico = ''
            anagraficaSelezionata.sesso = ''
            anagraficaSelezionata.civico = ''
            anagraficaSelezionata.stato = ''
            anagraficaSelezionata.provincia = ''
            anagraficaSelezionata.comune = ''
            anagraficaSelezionata.cap = ''
            anagraficaSelezionata.email = ''
            anagraficaSelezionata.applicativo = ''
            anagraficaSelezionata.protetta = true

            listaAnagrafica = integrazioneContabilitaJSerfin.getListaAnagrafica(codiceFiscale)
//            println "listaAnagrafica --> " + listaAnagrafica
            if (listaAnagrafica.size() == 0) {
                isNuovaAnagrafica = true
                abilitaModifica = true
                anagraficaSelezionata.protetta = false
               // anagraficaSelezionata.codiceFiscale = codiceFiscale.toUpperCase()
                Clients.showNotification('Nessuna anagrafica trovata', Clients.NOTIFICATION_TYPE_WARNING, null, "middle_center", 5000, true);
            }
            else {
                isNuovaAnagrafica = false
            }
//            println "isNuovaAnagrafica --> " + isNuovaAnagrafica
            BindUtils.postNotifyChange(null, null, this, "listaAnagrafica")
            BindUtils.postNotifyChange(null, null, this, "isNuovaAnagrafica")
            BindUtils.postNotifyChange(null, null, this, "anagraficaSelezionata")
            BindUtils.postNotifyChange(null, null, this, "abilitaModifica")
        }
    }

    @Command
    onSalva() {
        if (isNuovaAnagrafica) {
            //se creo una nuova anagrafica da Sfera allora devono essere compilati i seguenti campi:
            if ((anagraficaSelezionata.ragioneSociale == null || ''.equals(anagraficaSelezionata.ragioneSociale))
                    && ((anagraficaSelezionata.nome == null || ''.equals(anagraficaSelezionata.nome))
                    || (anagraficaSelezionata.cognome == null || ''.equals(anagraficaSelezionata.cognome)))) {
                Clients.showNotification("Non e' possibile creare una nuova anagrafica: inserire cognome e nome o la ragione sociale.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 3000, true)
                return;
            }

            if (anagraficaSelezionata.codiceFiscale == null || ''.equals(anagraficaSelezionata.codiceFiscale)) {
                Clients.showNotification("Non e' possibile creare una nuova anagrafica: inserire il codice fiscale.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 3000, true)
                return;
            }

            if (anagraficaSelezionata.indirizzo == null || ''.equals(anagraficaSelezionata.indirizzo)) {
                Clients.showNotification("Non e' possibile creare una nuova anagrafica: inserire l'indirizzo.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 3000, true)
                return;
            }

            if (anagraficaSelezionata.comune == null || ''.equals(anagraficaSelezionata.comune)) {
                Clients.showNotification("Non e' possibile creare una nuova anagrafica: inserire il comune.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 3000, true)
                return;
            }

//            if (anagraficaSelezionata.email == null || ''.equals(anagraficaSelezionata.email)) {
//                Clients.showNotification("Non e' possibile creare una nuova anagrafica: inserire l'indirizzo email.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 3000, true)
//                return;
//            }
        }

        if ((anagraficaSelezionata.ragioneSociale == null || ''.equals(anagraficaSelezionata.ragioneSociale)) &&
                ((anagraficaSelezionata.nome == null || ''.equals(anagraficaSelezionata.nome)) || (anagraficaSelezionata.cognome == null || ''.equals(anagraficaSelezionata.cognome))) &&
                ((anagraficaSelezionata.codiceFiscale == null || ''.equals(anagraficaSelezionata.codiceFiscale)) || (anagraficaSelezionata.partitaIva == null || ''.equals(anagraficaSelezionata.partitaIva)))) {

            Clients.showNotification("Non e' possibile salvare l'anagrafica: non tutti i campi obbligatori sono compilati.", Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 3000, true)
            return;
        }

        //verifico se ho già l'anagrafica inserita per questo atto
        if (anagraficaSelezionata.pkid) {
            if (atto instanceof PropostaDelibera)
                movimentoAnagrafica = MovimentoAnagrafica.findByIdApplicativoEsternoAndPropostaDelibera(anagraficaSelezionata.pkid, atto)
            else
                movimentoAnagrafica = MovimentoAnagrafica.findByIdApplicativoEsternoAndDetermina(anagraficaSelezionata.pkid, atto)
        }
        else {
            movimentoAnagrafica = null
        }

        //non ho l'anagrafica e quindi la creo
        if (!movimentoAnagrafica) {
            //posso procedere con la memorizzazione dell'anagrafica
            movimentoAnagrafica = new MovimentoAnagrafica()

            if (atto instanceof Determina) {
                movimentoAnagrafica.determina = atto
            } else if (atto instanceof PropostaDelibera) {
                movimentoAnagrafica.propostaDelibera = atto
            }
        }

        movimentoAnagrafica.ragioneSociale = anagraficaSelezionata.ragioneSociale
        movimentoAnagrafica.cognome = anagraficaSelezionata.cognome
        movimentoAnagrafica.nome = anagraficaSelezionata.nome
        movimentoAnagrafica.codiceFiscale = anagraficaSelezionata.codiceFiscale
        movimentoAnagrafica.partitaIva = anagraficaSelezionata.partitaIva
        movimentoAnagrafica.tipoIndirizzo = anagraficaSelezionata.tipoIndirizzo
        movimentoAnagrafica.indirizzo = anagraficaSelezionata.indirizzo
        movimentoAnagrafica.civico = anagraficaSelezionata.civico
        movimentoAnagrafica.comune = anagraficaSelezionata.comune
        movimentoAnagrafica.siglaProvincia = anagraficaSelezionata.provincia
        movimentoAnagrafica.idApplicativoEsterno = anagraficaSelezionata.pkid

        if (isNuovaAnagrafica) {
            //Prima di creare la nuova anagrafica effettuo una nuova ricerca per codice fiscale e per partita iva
            //solo se non trovo nulla allora posso continuare e creare la nuova anagrafica
            List<Anagrafica> listaAnagraficaControllo = integrazioneContabilitaJSerfin.getListaAnagrafica(anagraficaSelezionata.codiceFiscale)
            if (listaAnagraficaControllo.size() > 0) {
                Clients.showNotification("Attenzione: anagrafica gia' esistente. Ricercare il beneficiario con il corretto codice fiscale.", Clients.NOTIFICATION_TYPE_ERROR, null, "middle_center", 3000, true)
                return;
            }
        }

        if (isNuovaAnagrafica || abilitaModifica) {
            //inserisco su Jserfin l'anagraafica nuova
            if (!anagraficaSelezionata.protetta) {
                String idAnag = integrazioneContabilitaJSerfin.creaAnagrafica(movimentoAnagrafica)

                if (idAnag != null && (!(''.equals(idAnag)))) {
                    movimentoAnagrafica.idApplicativoEsterno = idAnag
                }
            }
        }

        movimentoAnagrafica.save()
        Clients.showNotification("Anagrafica salvata con successo.", Clients.NOTIFICATION_TYPE_INFO, null, "middle_center", 3000, true)

        BindUtils.postNotifyChange(null, null, this, "listaAnagrafica")
        Events.postEvent(Events.ON_CLOSE, self, movimentoAnagrafica.toDTO())
    }

    @Command
    onChiudiPopup() {
        Events.postEvent(Events.ON_CLOSE, self, null)
    }
}