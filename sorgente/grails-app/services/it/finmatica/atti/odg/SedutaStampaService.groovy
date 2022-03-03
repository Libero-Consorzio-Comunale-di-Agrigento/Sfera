package it.finmatica.atti.odg

import it.finmatica.as4.As4SoggettoCorrente
import it.finmatica.atti.IGestoreFile
import it.finmatica.atti.IProtocolloEsterno
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.dizionari.Email
import it.finmatica.atti.dizionari.Notifica
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.documenti.DestinatarioNotifica
import it.finmatica.atti.documenti.NotificheService
import it.finmatica.atti.documenti.SoggettoNotifica
import it.finmatica.atti.documenti.beans.AttiGestioneTesti
import it.finmatica.atti.dto.dizionari.EmailDTO
import it.finmatica.atti.dto.dizionari.EmailDTOService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.gestioneiter.configuratore.iter.WkfCfgIter
import it.finmatica.gestioneiter.motore.WkfIterService
import it.finmatica.jsign.api.PKCS7Reader

class SedutaStampaService {

    EmailDTOService     emailDTOService
    AttiGestioneTesti   attiGestioneTesti
    NotificheService    notificheService
    WkfIterService      wkfIterService
    IGestoreFile        gestoreFile
    IProtocolloEsterno  protocolloEsterno

    /**
     * Aggiunge i convocati della seduta ai destinatari interni/esterni della stampa-seduta.
     *
     * @param sedutaStampa
     */
    void aggiungiConvocatiDestinatari(SedutaStampa sedutaStampa) {
        for (SedutaPartecipante partecipante : sedutaStampa.seduta.partecipanti) {

            // aggiungo solo i convocati
            if (!partecipante.convocato) {
                continue
            }

            DestinatarioNotifica destinatarioNotifica = creaDestinatario(partecipante)
            sedutaStampa.addToDestinatariNotifiche(destinatarioNotifica)
        }

        sedutaStampa.save()
    }

    /**
     * Aggiunge i partecipanti della seduta, alla stampa-seduta.
     *
     * @param sedutaStampa
     */
    void aggiungiPartecipantiDestinatari(SedutaStampa sedutaStampa) {
        for (SedutaPartecipante partecipante : sedutaStampa.seduta.partecipanti) {
            DestinatarioNotifica destinatarioNotifica = creaDestinatario(partecipante)
            sedutaStampa.addToDestinatariNotifiche(destinatarioNotifica)
        }

        sedutaStampa.save()
    }

    /**
     * Calcola e aggiunge i soggetti della notifica di convocazione da inserire sulla stampa
     *
     * @param sedutaStampa
     */
    void aggiungiDestinatariNotifica(SedutaStampa sedutaStampa, String tipoNotifica) {
        Notifica notifica = Notifica.findByTipoNotificaAndValidoAndCommissioneAndOggettiLike(tipoNotifica, true, sedutaStampa.commissioneStampa.commissione, "%${SedutaStampa.TIPO_OGGETTO}%")
        if (notifica == null) {
            return
        }

        List<SoggettoNotifica> soggetti = notificheService.calcolaSoggettiNotifica(notifica, sedutaStampa)
        for (SoggettoNotifica soggettoNotifica : soggetti) {
            DestinatarioNotifica destinatarioNotifica = creaDestinatario(soggettoNotifica)
            sedutaStampa.addToDestinatariNotifiche(destinatarioNotifica)
        }

        sedutaStampa.save()
    }

    /**
     * Crea un DestinatarioNotifica a partire da un SedutaPartecipante.
     *
     * @param partecipante
     *
     * @return il destinatarioNotifica da aggiungere sul documento.
     */
    DestinatarioNotifica creaDestinatario(SoggettoNotifica soggettoNotifica) {
        DestinatarioNotifica destinatarioNotifica = new DestinatarioNotifica(tipoNotifica: DestinatarioNotifica.TIPO_NOTIFICA_CONOSCENZA)

        As4SoggettoCorrente convocato = soggettoNotifica.soggetto
        if (convocato?.utenteAd4 != null) {
            destinatarioNotifica.utente = convocato.utenteAd4
            destinatarioNotifica.tipoDestinatario = DestinatarioNotifica.TIPO_DESTINATARIO_INTERNO
        } else {
            destinatarioNotifica.tipoDestinatario = DestinatarioNotifica.TIPO_DESTINATARIO_ESTERNO
            if (convocato != null) {
                destinatarioNotifica.email = emailDTOService.getEmail(convocato)
            } else {
                destinatarioNotifica.email = emailDTOService.getEmail(soggettoNotifica.email)
            }
        }

        return destinatarioNotifica
    }

    /**
     * Crea un DestinatarioNotifica a partire da un SedutaPartecipante.
     *
     * @param partecipante
     *
     * @return il destinatarioNotifica da aggiungere sul documento.
     */
    DestinatarioNotifica creaDestinatario(SedutaPartecipante partecipante) {
        DestinatarioNotifica destinatarioNotifica = new DestinatarioNotifica(tipoNotifica: DestinatarioNotifica.TIPO_NOTIFICA_CONOSCENZA)

        As4SoggettoCorrente convocato = partecipante.commissioneComponente?.componente ?: partecipante.componenteEsterno
        if (convocato.utenteAd4 != null) {
            destinatarioNotifica.utente = convocato.utenteAd4
            destinatarioNotifica.tipoDestinatario = DestinatarioNotifica.TIPO_DESTINATARIO_INTERNO
        } else {
            destinatarioNotifica.email = emailDTOService.getEmail(convocato)
            destinatarioNotifica.tipoDestinatario = DestinatarioNotifica.TIPO_DESTINATARIO_ESTERNO
        }

        return destinatarioNotifica
    }

    /**
     * Crea una nuova Seduta Stampa partendo da una già esistente.
     *
     * @param originale
     *
     * @return la nuova stampa con lo stesso testo della stampa originale.
     */
    SedutaStampa duplica(SedutaStampa originale) {
        SedutaStampa duplica = new SedutaStampa()

        duplica.seduta = originale.seduta
        duplica.commissioneStampa = originale.commissioneStampa

        duplica.classificaCodice = originale.classificaCodice
        duplica.classificaDal = originale.classificaDal
        duplica.classificaDescrizione = originale.classificaDescrizione
        duplica.fascicoloAnno = originale.fascicoloAnno
        duplica.fascicoloNumero = originale.fascicoloNumero
        duplica.fascicoloOggetto = originale.fascicoloOggetto

        duplica.pubblicaVisualizzatore = originale.pubblicaVisualizzatore
        duplica.giorniPubblicazione = originale.giorniPubblicazione
        duplica.note = originale.note

        for (DestinatarioNotifica destinatario : originale.destinatariNotifiche) {
            duplica.addToDestinatariNotifiche(destinatario.duplica())
        }
        duplica.save()

        if (originale.testo != null) {
            duplica.testo = new FileAllegato(nome: originale.testoOdt.nome, contentType: originale.testoOdt.contentType)
            attiGestioneTesti.copiaFileAllegato(originale, originale.testoOdt, duplica, duplica.testo)
        }

        wkfIterService.istanziaIter(WkfCfgIter.getIterIstanziabile(duplica.commissioneStampa.progressivoCfgIter).get(), duplica)

        return duplica
    }

    /**
     * Crea un allegato Seduta Stampa con il testo estratto dal file firmato.
     *
     * @param sedutaStampa
     *
     * @return la seduta stampa.
     */
    SedutaStampa creaAllegatoTestoNonFirmato(SedutaStampa sedutaStampa) {

        if (sedutaStampa.filePrincipale != null && sedutaStampa.filePrincipale.firmato && Impostazioni.PROTOCOLLO_ATTIVO.abilitato) {
            InputStream inputStream = gestoreFile.getFile(sedutaStampa, sedutaStampa.testo)
            PKCS7Reader reader = new PKCS7Reader(inputStream)

            protocolloEsterno.creaAllegatoProtocollo(sedutaStampa, "Testo sbustato" , sedutaStampa.testo.nomeFileSbustato, reader.getOriginalContent())

        }

        return sedutaStampa;
    }

}
