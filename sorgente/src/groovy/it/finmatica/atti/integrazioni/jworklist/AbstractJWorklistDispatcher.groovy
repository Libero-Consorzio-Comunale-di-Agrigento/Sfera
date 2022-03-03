package it.finmatica.atti.integrazioni.jworklist

import it.finmatica.ad4.autenticazione.Ad4Utente
import it.finmatica.atti.commons.FileAllegato
import it.finmatica.atti.dizionari.DatiAggiuntiviService
import it.finmatica.atti.dizionari.Notifica
import it.finmatica.atti.dizionari.TipoNotifica
import it.finmatica.atti.documenti.IProposta
import it.finmatica.atti.documenti.TipoDatoAggiuntivo
import it.finmatica.atti.documenti.SoggettoNotifica
import it.finmatica.atti.documenti.IDocumentoCollegato
import it.finmatica.atti.documenti.IAtto
import it.finmatica.atti.documenti.IDocumento
import it.finmatica.atti.documenti.Determina
import it.finmatica.atti.documenti.Delibera
import it.finmatica.atti.documenti.VistoParere
import it.finmatica.atti.documenti.DatoAggiuntivo
import it.finmatica.atti.documenti.DocumentoFactory
import it.finmatica.atti.documenti.PropostaDelibera
import it.finmatica.atti.documenti.DestinatarioNotificaAttivita
import it.finmatica.atti.documenti.beans.AttiGestoreCompetenze
import it.finmatica.atti.dto.documenti.DocumentoDTOService
import it.finmatica.atti.impostazioni.Impostazioni
import it.finmatica.atti.impostazioni.TipoSoggetto
import it.finmatica.atti.integrazioni.DistintaAtto
import it.finmatica.gestioneiter.Attore
import it.finmatica.gestioneiter.IDocumentoIterabile
import it.finmatica.gestioneiter.configuratore.dizionari.WkfTipoOggetto
import it.finmatica.so4.strutturaPubblicazione.So4UnitaPubb
import org.springframework.transaction.annotation.Transactional
import org.zkoss.util.resource.Labels

abstract class AbstractJWorklistDispatcher implements JWorklistDispatcher {

    DatiAggiuntiviService datiAggiuntiviService
    DocumentoDTOService   documentoDTOService
    AttiGestoreCompetenze gestoreCompetenze
    JWorklistConfig       JWorklistConfig

    @Override
    @Transactional(readOnly = true)
    boolean esisteNotificaJWorklist (String idRiferimento, Ad4Utente utente) {
        return (DestinatarioNotificaAttivita.countByIdRiferimentoAndUtenteAndModalitaInvio(idRiferimento, utente, DestinatarioNotificaAttivita.TIPO_NOTIFICA_JWORKLIST) > 0)
    }

    @Override
    @Transactional(readOnly = true)
    boolean esisteNotificaJWorklist (String idRiferimento, So4UnitaPubb unitaSo4) {
        return (DestinatarioNotificaAttivita.countByIdRiferimentoAndUnitaSo4AndModalitaInvio(idRiferimento, unitaSo4, DestinatarioNotificaAttivita.TIPO_NOTIFICA_JWORKLIST) > 0)
    }

    @Override
    @Transactional(readOnly = true)
    List<DestinatarioNotificaAttivita> getNotificheDaEliminare (String idRiferimento) {
        return DestinatarioNotificaAttivita.findAllByIdRiferimentoAndModalitaInvio(idRiferimento, DestinatarioNotificaAttivita.TIPO_NOTIFICA_JWORKLIST)
    }

    @Override
    @Transactional(readOnly = true)
    List<DestinatarioNotificaAttivita> getNotificheDaEliminare (String idRiferimento, Notifica notifica) {
        return DestinatarioNotificaAttivita.findAllByIdRiferimentoAndModalitaInvioAndNotifica(idRiferimento, DestinatarioNotificaAttivita.TIPO_NOTIFICA_JWORKLIST, notifica)
    }

    @Override
    @Transactional(readOnly = true)
    List<DestinatarioNotificaAttivita> getNotificheDaEliminare (String idRiferimento, Ad4Utente utente) {
        // se invece ho l'utente, allora devo eliminare la sua notifica oppure quelle dell'unità a seconda dell'impostazione:
        DestinatarioNotificaAttivita notifica = DestinatarioNotificaAttivita.findByIdRiferimentoAndUtenteAndModalitaInvio(idRiferimento, utente, DestinatarioNotificaAttivita.TIPO_NOTIFICA_JWORKLIST)

        // se non trovo notifiche, esco.
        if (notifica == null) {
            return []
        }

        // ritorno la notifica singola se l'impostazione globale dice così oppure se la notifica stessa era "per utente"
        if (DestinatarioNotificaAttivita.NOTIFICA_UTENTE == notifica.soggettoNotifica || JWorklistConfig.isEliminaNotificaPerUtente()) {
            return [notifica]
        }

        // ritorno le notifiche legate ai componenti dell'unità se l'impostazione globale lo consente e se la notifica stessa era "per unita"
        if (DestinatarioNotificaAttivita.NOTIFICA_UNITA == notifica.soggettoNotifica && JWorklistConfig.isEliminaNotificaPerUo() && notifica.unitaSo4 != null) {
            return DestinatarioNotificaAttivita.findAllByIdRiferimentoAndUnitaSo4AndModalitaInvio(idRiferimento, notifica.unitaSo4, DestinatarioNotificaAttivita.TIPO_NOTIFICA_JWORKLIST)
        }

        return []
    }

    @Override
    @Transactional(readOnly = true)
    List<DestinatarioNotificaAttivita> getNotificheDaEliminare (String idRiferimento, So4UnitaPubb unitaSo4) {
        return DestinatarioNotificaAttivita.findAllByIdRiferimentoAndUnitaSo4AndModalitaInvio(idRiferimento, unitaSo4, DestinatarioNotificaAttivita.TIPO_NOTIFICA_JWORKLIST);
    }

    @Override
    @Transactional(readOnly = true)
    String getUrlDocumento (def documento) {
        String tipoDocumento = getTipoDocumento(documento);
        return JWorklistConfig.getUrlJWorklist() + "/standalone.zul?id=${documento.id}&operazione=APRI_DOCUMENTO&tipoDocumento=${tipoDocumento}";
    }

    @Override
    @Transactional(readOnly = true)
    String getIdRiferimento (IDocumentoIterabile documentoIterabile, String tipoNotifica) {
        String prefisso = "NOTIFICA_"
        // l'id-riferimento delle notifiche di cambio step sulla jworklist non hanno un "prefisso". Faccio così per mantenere la compatibilità col pregresso.
        // anche la notifica da firmare non prevede il pulsante "presa visione" ma sparisce una volta firmato il documento.
        if (TipoNotifica.ASSEGNAZIONE == tipoNotifica || TipoNotifica.DA_FIRMARE == tipoNotifica) {
            prefisso = ""
        }

        return prefisso + documentoIterabile.getProperties().TIPO_OGGETTO + "_" + documentoIterabile.id
    }

    void aggiungiNotificaJWorklist (String idRiferimento, SoggettoNotifica soggettoNotifica, def documento, Notifica notifica) {
        DestinatarioNotificaAttivita n = new DestinatarioNotificaAttivita();
        n.idRiferimento = idRiferimento
        n.utente = soggettoNotifica.utente
        n.soggettoNotifica = soggettoNotifica.assegnazione
        n.idAttivita = soggettoNotifica.idAttivita
        n.destinatarioNotifica = soggettoNotifica.destinatarioNotifica
        n.unitaSo4 = soggettoNotifica.unitaSo4
        n.modalitaInvio = DestinatarioNotificaAttivita.TIPO_NOTIFICA_JWORKLIST
        n.notifica = notifica
        n.save()

        Attore attore = new Attore()

        // se il soggetto è calcolato sulla base di una unità o un ruolo, allora non devo
        // dare le competenze direttamente all'utente ma al ruolo e/o unità perché altrimenti sballa la gesitone della riservatezza: assegnando
        if (soggettoNotifica.unitaSo4 != null || soggettoNotifica.ruoloAd4 != null) {
            attore.unitaSo4 = soggettoNotifica.unitaSo4;
            attore.ruoloAd4 = soggettoNotifica.ruoloAd4;
        } else {
            attore.utenteAd4 = soggettoNotifica.utente
        }

        // assegno le competenze in lettura all'utente/ruolo/unità che riceve la notifica perché potrebbe non averle.
        gestoreCompetenze.assegnaCompetenze(documento, WkfTipoOggetto.get(documento.TIPO_OGGETTO), attore, true, false, false, null);
    }

    String getTipoDocumento (IDocumentoIterabile documentoIterabile) {
        return documentoIterabile.getProperties().TIPO_OGGETTO
    }

    ArrayList getParamInitIter (IDocumentoIterabile documentoIterabile, String idRiferimento = null) {
        def documento = documentoIterabile
        if (documento instanceof IDocumentoCollegato) {
            documento = documento.documentoPrincipale
        }

        String oggetto = documento.oggetto
        if (documento instanceof IAtto) {
            documento = documento.proposta
        }

        String unita     = documento.getSoggetto(TipoSoggetto.UO_PROPONENTE)?.unitaSo4 ?: ""
        String redattore = documento.getSoggetto(TipoSoggetto.REDATTORE)?.utenteAd4?.nominativoSoggetto ?: ""
        String dirigente = documento.getSoggetto(TipoSoggetto.DIRIGENTE)?.utenteAd4?.nominativoSoggetto ?: ""

        def param = []
        param << [key: Labels.getLabel("label.determinaStandard.proponente").replaceAll(":", ""), value:redattore]
        param << [key: Labels.getLabel("label.determinaStandard.unita").replaceAll(":", ""), value:unita]
        param << [key: "Oggetto", value: oggetto]
        param << [key: Labels.getLabel("label.determinaStandard.dirigente").replaceAll(":", ""), value: dirigente]

        DatoAggiuntivo datoAggiuntivo = datiAggiuntiviService.getDatoAggiuntivo(documento, TipoDatoAggiuntivo.RIFLESSI_CONTABILI)
        if (datoAggiuntivo?.valoreTipoDato != null) {
            param << [key: TipoDatoAggiuntivo.getDescrizione(TipoDatoAggiuntivo.RIFLESSI_CONTABILI), value: datoAggiuntivo.valoreTipoDato.descrizione]
        }

        if (idRiferimento != null) {
            FileAllegato testo = documentoIterabile.testo ?: documentoIterabile.testoOdt;
            if (testo != null) {
                param << [key:          "Allegato",
                          value:        testo.nome,
                          firmato:      testo.firmato,
                          priorita:     1,
                          url:          "allegato?file=${testo.id}&rif=${idRiferimento}&doc=${documentoIterabile.idDocumento}&tipo=${documentoIterabile.TIPO_OGGETTO}"]
            }
            for (def allegato : documentoIterabile.allegati.sort {it.sequenza}) {
                for (def file : allegato?.fileAllegati.sort {it.id}) {
                    param << [key:  "Allegato",
                              value: file.nome,
                              firmato: file.firmato,
                              priorita: 0,
                              url: "allegato?file=${file.id}&rif=${idRiferimento}&doc=${allegato.id}&tipo=${allegato.TIPO_OGGETTO}"]
                }
            }
        }

        return param
    }

    String getParamInitIterString (IDocumentoIterabile documentoIterabile) {
        String paramInitIter = ""
        def params = getParamInitIter(documentoIterabile)
        params?.each() {
            paramInitIter += "${it.key}: ${it.value}\n"
        }
        return paramInitIter
    }

    String getNote (IDocumentoIterabile documentoIterabile, String tipoNotifica) {
        // le note di trasmissione vanno inviate solo per le notifiche di tipo "cambio step"
        if (tipoNotifica != TipoNotifica.ASSEGNAZIONE) {
            return ""
        }

        if (documentoIterabile instanceof Determina
                || documentoIterabile instanceof PropostaDelibera
                || documentoIterabile instanceof VistoParere) {
            def note = documentoDTOService.getNoteTrasmissionePrecedenti(documentoIterabile)
            if (note.attorePrecedente) {
                return note.noteTrasmissionePrecedenti[0].noteTrasmissione
            }
        }

        return ""
    }

    Date getScadenza (IDocumentoIterabile documentoIterabile, String tipoNotifica) {
        if (documentoIterabile instanceof Determina && (TipoNotifica.ASSEGNAZIONE == tipoNotifica || TipoNotifica.DA_FIRMARE == tipoNotifica)) {
            if ("DATA_SCADENZA_FATTURE".equals(Impostazioni.NOTIFICHE_DATA_SCADENZA.getValore()) && documentoIterabile.numeroProposta != null){
                return getDistinta(documentoIterabile)?.scadenzaDal
            }
            return documentoIterabile.dataScadenza
        }

        if (documentoIterabile instanceof PropostaDelibera && (TipoNotifica.ASSEGNAZIONE == tipoNotifica || TipoNotifica.DA_FIRMARE == tipoNotifica)) {
            if ("DATA_SCADENZA_FATTURE".equals(Impostazioni.NOTIFICHE_DATA_SCADENZA.getValore()) && documentoIterabile.numeroProposta != null){
                return getDistinta(documentoIterabile)?.scadenzaDal
            }
            return documentoIterabile.dataScadenza
        }

        if (documentoIterabile instanceof VistoParere && !(documentoIterabile.documentoPrincipale instanceof Delibera) && (TipoNotifica.ASSEGNAZIONE == tipoNotifica || TipoNotifica.DA_FIRMARE == tipoNotifica)) {
            return documentoIterabile.documentoPrincipale.dataScadenza
        }

        return null
    }

    @Override
    IDocumento getDocumento (String idRiferimento) {
        if (idRiferimento.startsWith("NOTIFICA_")) {
            idRiferimento = idRiferimento.substring("NOTIFICA_".length())
        }

        int separatore = idRiferimento.lastIndexOf("_")
        String tipoOggetto = idRiferimento.substring(0, separatore)
        String id = idRiferimento.substring(separatore + 1)

        return DocumentoFactory.getDocumento(Long.parseLong(id), tipoOggetto)
    }

    private DistintaAtto getDistinta(IDocumentoIterabile documentoIterabile){
        def proposta = null
        if (documentoIterabile instanceof IDocumentoCollegato) {
            proposta =  documentoIterabile.documentoPrincipale.proposta
        } else if (documentoIterabile instanceof IAtto) {
            proposta =  documentoIterabile.proposta
        } else if (documentoIterabile instanceof IProposta) {
            proposta = documentoIterabile
        }
        if (proposta != null) {
            return DistintaAtto.findByAnnoPropostaAndNumeroPropostaAndUnitaProponente(proposta.annoProposta, proposta.numeroProposta, (proposta.registroProposta?.registroEsterno ?: proposta.registroProposta?.codice))
        }
        return null;
    }
}
