package atti.actions.documenti

import it.finmatica.atti.AbstractViewModel
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.odg.SedutaStampa
import it.finmatica.atti.odg.SedutaStampaService
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.annotations.Action
import org.zkoss.zk.ui.Executions

class SedutaStampaAction {

    SedutaStampaService sedutaStampaService

    @Action(tipo		= Action.TipoAzione.PULSANTE,
            tipiOggetto	= [SedutaStampa.TIPO_OGGETTO],
            nome		= "Duplica la stampa e la apre",
            descrizione	= "Duplica la stampa di seduta e la apre in maschera.")
    SedutaStampa duplicaStampa (SedutaStampa sedutaStampa, AbstractViewModel<? extends IDocumentoIterabile> viewModel) {
        SedutaStampa duplica = sedutaStampaService.duplica(sedutaStampa)
        Executions.createComponents("/odg/seduta/sedutaStampa.zul", null, [id:duplica.id])
        return sedutaStampa
    }

    @Action(tipo		= Action.TipoAzione.AUTOMATICA,
            tipiOggetto	= [SedutaStampa.TIPO_OGGETTO],
            nome		= "Aggiunge i Convocati della Seduta ai Destinatari",
            descrizione	= "Aggiunge i Convocati della Seduta ai Destinatari solo se non ce ne sono già di presenti sulla stampa")
    SedutaStampa aggiungiConvocatiDestinatari (SedutaStampa sedutaStampa) {
        if (sedutaStampa.destinatariNotifiche?.size() > 0) {
            return sedutaStampa
        }

        sedutaStampaService.aggiungiConvocatiDestinatari(sedutaStampa)
        return sedutaStampa
    }

    @Action(tipo		= Action.TipoAzione.AUTOMATICA,
            tipiOggetto	= [SedutaStampa.TIPO_OGGETTO],
            nome		= "Aggiunge i Partecipanti della Seduta ai Destinatari",
            descrizione	= "Aggiunge i Partecipanti della Seduta ai Destinatari (sia presenti che assenti) solo se non ce ne sono già di presenti sulla stampa")
    SedutaStampa aggiungiPartecipantiDestinatari (SedutaStampa sedutaStampa) {
        if (sedutaStampa.destinatariNotifiche?.size() > 0) {
            return sedutaStampa
        }

        sedutaStampaService.aggiungiPartecipantiDestinatari(sedutaStampa)
        return sedutaStampa
    }

    @Action(tipo		= Action.TipoAzione.AUTOMATICA,
            tipiOggetto	= [SedutaStampa.TIPO_OGGETTO],
            nome		= "Calcola i destinatari della notifica di CONVOCAZIONE e li aggiunge alla stampa di seduta",
            descrizione	= "Calcola i destinatari della notifica di CONVOCAZIONE e li aggiunge alla stampa di seduta")
    SedutaStampa aggiungiDestinatariNotificaConvocazione (SedutaStampa sedutaStampa) {
        sedutaStampaService.aggiungiDestinatariNotifica(sedutaStampa, TipoNotifica.CONVOCAZIONE_SEDUTA)
        return sedutaStampa
    }

    @Action(tipo		= Action.TipoAzione.AUTOMATICA,
            tipiOggetto	= [SedutaStampa.TIPO_OGGETTO],
            nome		= "Calcola i destinatari della notifica di VERBALE e li aggiunge alla stampa di seduta",
            descrizione	= "Calcola i destinatari della notifica di VERBALE e li aggiunge alla stampa di seduta")
    SedutaStampa aggiungiDestinatariNotificaVerbale (SedutaStampa sedutaStampa) {
        sedutaStampaService.aggiungiDestinatariNotifica(sedutaStampa, TipoNotifica.VERBALE_SEDUTA)
        return sedutaStampa
    }

    @Action(tipo		= Action.TipoAzione.AUTOMATICA,
            tipiOggetto	= [SedutaStampa.TIPO_OGGETTO],
            nome		= "Verifica che sia presente almeno un destinatario",
            descrizione	= "Se non sono presenti destinatari, blocca l'esecuzione con un errore")
    SedutaStampa controllaDestinatari (SedutaStampa sedutaStampa) {
        if (!(sedutaStampa.destinatariNotifiche?.size() > 0)) {
            throw new AttiRuntimeException("Non è possibile proseguire: è necessario aggiungere almeno un destinatario.")
        }

        return sedutaStampa
    }

    @Action(tipo		= Action.TipoAzione.AUTOMATICA,
            tipiOggetto	= [SedutaStampa.TIPO_OGGETTO],
            nome		= "Crea Testo non Firmato come allegato al protocollo",
            descrizione	= "Crea un allegato al protocollo della seduta stampa con il testo non firmato")
    SedutaStampa creaAllegatoTestoNonFirmato (SedutaStampa sedutaStampa) {
        sedutaStampaService.creaAllegatoTestoNonFirmato(sedutaStampa)
        return sedutaStampa
    }
}
