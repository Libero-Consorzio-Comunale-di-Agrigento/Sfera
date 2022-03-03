package it.finmatica.atti.documenti

import grails.plugin.springsecurity.SpringSecurityService
import it.finmatica.atti.IntegrazioneAlbo
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.exceptions.AttiRuntimeException
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.motore.WkfIterService

class DocumentoService {

    SpringSecurityService springSecurityService

    AttiGestoreCompetenze gestoreCompetenze
    WkfIterService        wkfIterService
    IntegrazioneAlbo      integrazioneAlbo

    boolean isDaPubblicare (IPubblicabile d) {
        Date dataMinPubblicazione = d.dataMinimaPubblicazione

        // se la data minima di pubblicazione è nulla allora verifico se il documento è da pubblicare
        if (dataMinPubblicazione == null) {
            return (d.tipologiaPubblicazione.pubblicazione)
        }

        if ((dataMinPubblicazione.before(new Date())) || (new Date().equals(dataMinPubblicazione))) {
            return (d.tipologiaPubblicazione.pubblicazione)
        } else {

            // FIXME: HORRIBLE thing, settare un campo in un metodo "is"
            // imposto nel documento il fatto che è da pubblicare
            d.daPubblicare = true

            // se è maggiore alla data di oggi restituisco false
            return false
        }
    }

    boolean isPubblicazioneFinita (IPubblicabile d) {
        // se sono in pubblicazione fino a revoca, non ho la data di fine pubblicazione:

        // controllo se sono in prima o seconda pubblicazione:
        if (d.dataPubblicazione2 != null) { // se sono in seconda pubblicazione, controllo questa

            if (d.pubblicaRevoca) {
                // se sono in pubblicazione fino a revoca, ritorno true solo se data fine pubb è not null
                return (d.dataFinePubblicazione2 != null)
            } else {
                return (new Date().after(d.dataFinePubblicazione2)) // oggi è "dopo" la data di fine pubblicazione impostata?
            }
        } else {
            // se data pubb 2 non è null, vuol dire che sono in prima pubblicazione:

            // se non ho neanche iniziato la pubblicazione, ritorno false:
            if (d.dataPubblicazione == null) {
                return false
            }

            if (d.pubblicaRevoca) {
                // se sono in pubblicazione fino a revoca, ritorno true solo se data fine pubb è not null
                return (d.dataFinePubblicazione != null)
            } else {
                return (new Date().after(d.dataFinePubblicazione)) // oggi è "dopo" la data di fine pubblicazione impostata?
            }
        }
    }

    def pubblicazione (IPubblicabile d, Date dataPubblicazione = new Date()) {
        // controllo se devo fare la prima pubblicazione
        if (isDaPubblicare(d) && d.dataPubblicazione == null) {
            d.stato = StatoDocumento.PUBBLICATO
            d.dataPubblicazione = dataPubblicazione

            // se non sono con pubblicazione fino a revoca, devo impostare le date di pubblicazione.
            if (!d.pubblicaRevoca) {
                // se non ho i giorni di pubblicazione, li riempio con il valore in tipologia.
                if (d.giorniPubblicazione == null) {
                    // se anche il valore in tipologia è nullo, blocco tutto e segnalo un errore:
                    if (d.tipologiaPubblicazione.giorniPubblicazione == null) {
                        throw new AttiRuntimeException(
                                "Non è possibile pubblicare il documento: è necessario specificare i giorni di pubblicazione nella tipologia '${d.tipologiaPubblicazione.titolo}'");
                    }
                    d.giorniPubblicazione = d.tipologiaPubblicazione.giorniPubblicazione;
                }
                d.dataFinePubblicazione = d.dataPubblicazione + d.giorniPubblicazione
            }

            // controllo se devo pubblicare e se è abilitata la pubblicazione all'albo, se sì procedo, altrimenti no.
            if (!Impostazioni.INTEGRAZIONE_ALBO.isDisabilitato()) {
                integrazioneAlbo.pubblicaAtto(d)
                integrazioneAlbo.aggiornaDataEsecutivita(d)
            }

        } else if (isDaPubblicare2(d) && d.dataPubblicazione2 == null) {
            // se devo fare la seconda pubblicazione
            d.stato = StatoDocumento.PUBBLICATO
            d.dataPubblicazione2 = dataPubblicazione
            // se non sono con pubblicazione fino a revoca, devo impostare le date di pubblicazione.
            if (!d.pubblicaRevoca) {
                // se non ho i giorni di pubblicazione, li riempio con il valore in tipologia.
                if (d.giorniPubblicazione == null) {
                    // se anche il valore in tipologia è nullo, blocco tutto e segnalo un errore:
                    if (d.tipologiaPubblicazione.giorniPubblicazione == null) {
                        throw new AttiRuntimeException(
                                "Non è possibile pubblicare il documento: è necessario specificare i giorni di pubblicazione nella tipologia '${d.tipologiaPubblicazione.titolo}'");
                    }
                    d.giorniPubblicazione = d.tipologiaPubblicazione.giorniPubblicazione;
                }
                d.dataFinePubblicazione2 = d.dataPubblicazione2 + d.giorniPubblicazione
            }

            // controllo se devo pubblicare e se è abilitata la pubblicazione all'albo, se sì procedo, altrimenti no.
            if (!Impostazioni.INTEGRAZIONE_ALBO.isDisabilitato()) {
                integrazioneAlbo.secondaPubblicazioneAtto(d)
                integrazioneAlbo.aggiornaDataEsecutivita(d)
            }
        }

        d.save()

        return d
    }

    boolean isDaPubblicare2 (IPubblicabile atto) {
        return (atto.tipologiaPubblicazione.secondaPubblicazione && atto.dataPubblicazione2 == null)
    }

    def attivaPubblicazione (IAtto d) {
        // prima di attivare la pubblicazione, copio le competenze della proposta sulla delibera.
        // vedi bug: http://svi-redmine/issues/10734
        gestoreCompetenze.copiaCompetenze(d.proposta, d, true)

        // attivo la pubblicazione se:
        // 1) devo pubblicare il documento e questo non è già stato pubblicato
        // 2) devo fare la seconda pubblicazione, questa non è già iniziata e la prima pubblicazione è finita.
        // ricontrollo se devo pubblicare, se sì procedo, altrimenti no.
        if (isDaPubblicare(d) && d.dataPubblicazione == null || isPubblicazioneFinita(d) && isDaPubblicare2(d) && d.dataPubblicazione2 == null) {
            log.debug("Attivo il flusso di pubblicazione:")
            // attivo l'iter di pubblicazione
            WkfCfgIter iterPubblicazione = WkfCfgIter.getIterIstanziabile(d.tipologiaPubblicazione.progressivoCfgIterPubblicazione).get();
            wkfIterService.istanziaIter(iterPubblicazione, d);
        } else if (d.tipologiaPubblicazione.conservazioneSostitutiva) {
            // se il documento non è da pubblicare ma è da conservare, allora cambio lo stato di conservazione
            // perché questa azione viene messa alla fine del flusso
            d.statoConservazione = StatoConservazione.DA_CONSERVARE
        }

        return d
    }

    void controllaOggettoRicorrente (IProposta d) {
        if (Impostazioni.OGGETTI_RICORRENTI_CONTROLLO.abilitato && d.oggettoRicorrente != null) {
            String oggettoRicorrente = d.oggettoRicorrente.oggetto

            if (d.oggetto.indexOf("[...]") > 0) {
                throw new AttiRuntimeException("L'oggetto inserito contiene delle parti variabili non modificate.")
            }

            if (oggettoRicorrente.indexOf("[...]") > 0 && d.oggetto.replaceAll("\\s+", " ").equals(
                    oggettoRicorrente.replaceAll("\\[\\.\\.\\.\\]", "").replaceAll("\\s+", " "))) {
                throw new AttiRuntimeException("L'oggetto inserito contiene delle parti variabili eliminate.")
            }

            def sezioni = oggettoRicorrente.split("\\[\\.\\.\\.\\]")
            for (String sezione : sezioni) {
                if (d.oggetto.indexOf(sezione) < 0) {
                    throw new AttiRuntimeException("L'oggetto inserito non rispetta le regole riportate nell'oggetto ricorrente selezionato.")
                }
            }
        }
    }

}